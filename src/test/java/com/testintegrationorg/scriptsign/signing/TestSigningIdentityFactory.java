package com.testintegrationorg.scriptsign.signing;

import com.testintegrationorg.scriptsign.config.SigningProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class TestSigningIdentityFactory {

    static final String ALIAS = "test-signer";
    static final String PASSWORD = "changeit";

    private TestSigningIdentityFactory() {
    }

    static Path createPkcs12(Path directory) throws IOException, InterruptedException {
        Path keystore = directory.resolve("test-signing-identity.p12");
        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "keytool.exe" : "keytool";
        Path keytool = Path.of(System.getProperty("java.home"), "bin", executable);

        List<String> command = List.of(
                keytool.toString(),
                "-genkeypair",
                "-alias", ALIAS,
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-sigalg", "SHA256withRSA",
                "-validity", "30",
                "-dname", "CN=SD-AI Test Code Signing,O=TestIntegrationOrg,C=US",
                "-storetype", "PKCS12",
                "-keystore", keystore.toString(),
                "-storepass", PASSWORD,
                "-keypass", PASSWORD,
                "-ext", "KeyUsage=digitalSignature",
                "-ext", "ExtendedKeyUsage=codeSigning",
                "-noprompt");

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) {
            throw new IllegalStateException("keytool failed to create the test identity: " + output);
        }
        return keystore;
    }

    static SigningProperties properties(Path keystore, Path tempDirectory) throws IOException {
        Files.createDirectories(tempDirectory);
        SigningProperties properties = new SigningProperties();
        properties.setTempDirectory(tempDirectory);
        properties.setMaxScriptBytes(1_048_576);
        properties.getKeystore().setPath(keystore.toString());
        properties.getKeystore().setStorePassword(PASSWORD);
        properties.getKeystore().setKeyPassword(PASSWORD);
        properties.getKeystore().setAlias(ALIAS);
        properties.getTimestamp().setEnabled(false);
        properties.getTimestamp().setAuthorities(List.of());
        return properties;
    }
}
