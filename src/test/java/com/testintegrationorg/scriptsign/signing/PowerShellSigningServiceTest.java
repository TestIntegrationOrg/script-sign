package com.testintegrationorg.scriptsign.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.testintegrationorg.scriptsign.config.SigningProperties;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import net.jsign.DigestAlgorithm;
import net.jsign.Signable;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PowerShellSigningServiceTest {

    @TempDir
    Path testDirectory;

    private Path keystore;
    private Path signingTempDirectory;
    private PowerShellSigningService service;

    @BeforeEach
    void setUp() throws Exception {
        keystore = TestSigningIdentityFactory.createPkcs12(testDirectory);
        signingTempDirectory = testDirectory.resolve("signing-temp");
        SigningProperties properties = TestSigningIdentityFactory.properties(keystore, signingTempDirectory);
        Pkcs12SigningIdentity identity = new Pkcs12SigningIdentity(properties);
        service = new PowerShellSigningService(new JsignPowerShellScriptSigner(identity, properties), properties);
    }

    @Test
    void signsAndCryptographicallyVerifiesPowerShellScript() throws Exception {
        String original = "param([string]$Name)\nWrite-Output \"Hello $Name\"\n";

        SignedScript result = service.sign(new SignScriptCommand("hello.ps1", original));

        assertThat(result.fileName()).isEqualTo("hello.ps1");
        assertThat(result.digestAlgorithm()).isEqualTo("SHA-256");
        assertThat(result.timestamped()).isFalse();
        assertThat(result.signedScript()).startsWith(original);
        assertThat(result.signedScript()).contains("# SIG # Begin signature block");
        assertThat(result.signerSubject()).contains("SD-AI Test Code Signing");

        Path unsignedFile = testDirectory.resolve("unsigned.ps1");
        Path signedFile = testDirectory.resolve("signed.ps1");
        Files.writeString(unsignedFile, original, StandardCharsets.UTF_8);
        Files.writeString(signedFile, result.signedScript(), StandardCharsets.UTF_8);

        try (Signable unsigned = Signable.of(unsignedFile.toFile(), StandardCharsets.UTF_8);
             Signable signed = Signable.of(signedFile.toFile(), StandardCharsets.UTF_8)) {
            assertThat(signed.computeDigest(DigestAlgorithm.SHA256))
                    .isEqualTo(unsigned.computeDigest(DigestAlgorithm.SHA256));
            assertThat(signed.getSignatures()).hasSize(1);
            verifyEmbeddedCmsSignature(signed.getSignatures().get(0));
        }
    }

    @Test
    void deletesTemporaryFileAfterSuccessfulSigning() throws Exception {
        service.sign(new SignScriptCommand("cleanup.ps1", "Write-Output 'cleanup'"));

        try (var files = Files.list(signingTempDirectory)) {
            assertThat(files.findAny()).isEmpty();
        }
    }

    @Test
    void deletesTemporaryFileAfterSigningFailure() throws Exception {
        SigningProperties properties = TestSigningIdentityFactory.properties(keystore, signingTempDirectory);
        PowerShellScriptSigner failingSigner = path -> {
            throw new SigningUnavailableException("simulated signing failure", null);
        };
        PowerShellSigningService failingService = new PowerShellSigningService(failingSigner, properties);

        assertThatThrownBy(() -> failingService.sign(new SignScriptCommand("failure.ps1", "Write-Output 'failure'")))
                .isInstanceOf(SigningUnavailableException.class);
        try (var files = Files.list(signingTempDirectory)) {
            assertThat(files.findAny()).isEmpty();
        }
    }

    @Test
    void rejectsUnsupportedFileNamesAndExistingSignatures() {
        assertThatThrownBy(() -> service.sign(new SignScriptCommand("../escape.ps1", "Write-Output 'x'")))
                .isInstanceOf(InvalidScriptException.class);
        assertThatThrownBy(() -> service.sign(new SignScriptCommand("script.txt", "Write-Output 'x'")))
                .isInstanceOf(InvalidScriptException.class);
        assertThatThrownBy(() -> service.sign(new SignScriptCommand(
                "signed.ps1", "Write-Output 'x'\n# SIG # Begin signature block")))
                .isInstanceOf(AlreadySignedScriptException.class);
    }

    @Test
    void appliesUtf8ByteLimitBeforeCallingSigner() throws Exception {
        SigningProperties properties = TestSigningIdentityFactory.properties(keystore, signingTempDirectory);
        properties.setMaxScriptBytes(4);
        PowerShellSigningService limitedService = new PowerShellSigningService(
                path -> { throw new AssertionError("signer must not be called"); }, properties);

        assertThatThrownBy(() -> limitedService.sign(new SignScriptCommand("large.ps1", "ééé")))
                .isInstanceOf(ScriptTooLargeException.class);
    }

    private static void verifyEmbeddedCmsSignature(CMSSignedData signedData) throws Exception {
        // Signable loads the embedded block as detached CMS data. Reparse the encoded
        // structure so Bouncy Castle exposes the encapsulated Authenticode content.
        CMSSignedData attachedSignedData = new CMSSignedData(signedData.getEncoded());
        Collection<SignerInformation> signers = attachedSignedData.getSignerInfos().getSigners();
        assertThat(signers).hasSize(1);
        SignerInformation signer = signers.iterator().next();
        Collection<X509CertificateHolder> certificates = attachedSignedData.getCertificates().getMatches(signer.getSID());
        assertThat(certificates).hasSize(1);
        X509CertificateHolder certificate = certificates.iterator().next();
        assertThat(signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certificate))).isTrue();
    }
}
