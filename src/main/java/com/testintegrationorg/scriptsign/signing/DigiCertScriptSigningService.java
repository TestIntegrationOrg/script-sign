package com.testintegrationorg.scriptsign.signing;

import com.testintegrationorg.scriptsign.config.SigningProperties;
import net.jsign.AuthenticodeSigner;
import net.jsign.DigestAlgorithm;
import net.jsign.KeyStoreBuilder;
import net.jsign.KeyStoreType;
import net.jsign.Signable;
import net.jsign.timestamp.TimestampingMode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

@Service
public class DigiCertScriptSigningService implements ScriptSigningService {

    private static final String POWERSHELL_EXTENSION = ".ps1";

    private final SigningProperties properties;

    public DigiCertScriptSigningService(SigningProperties properties) {
        this.properties = properties;
    }

    @Override
    public SignedScript sign(String scriptContent, String fileName) {
        validateInput(scriptContent, fileName);

        SigningProperties.DigiCert config = properties.digicert();
        if (config == null) {
            throw new SigningException("DigiCert signing configuration is missing");
        }

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("script-sign-", POWERSHELL_EXTENSION);
            Files.writeString(tempFile, scriptContent, StandardCharsets.UTF_8);

            KeyStore keyStore = buildDigiCertKeyStore(config);
            AuthenticodeSigner signer = new AuthenticodeSigner(keyStore, require(config.alias(), "signing.digicert.alias"), null)
                    .withDigestAlgorithm(DigestAlgorithm.SHA256)
                    .withTimestamping(properties.timestamp() != null && properties.timestamp().enabled());

            configureTimestamping(signer);

            try (Signable signable = Signable.of(tempFile.toFile())) {
                signer.sign(signable);
            }

            String signedContent = Files.readString(tempFile, StandardCharsets.UTF_8);
            return new SignedScript(sanitizeFileName(fileName), signedContent, config.alias());
        } catch (Exception e) {
            throw new SigningException("Failed to Authenticode-sign PowerShell script", e);
        } finally {
            deleteQuietly(tempFile);
        }
    }

    private KeyStore buildDigiCertKeyStore(SigningProperties.DigiCert config) throws Exception {
        String storePassword = String.join("|",
                require(config.apiKey(), "signing.digicert.api-key"),
                require(config.clientCertificatePath(), "signing.digicert.client-certificate-path"),
                require(config.clientCertificatePassword(), "signing.digicert.client-certificate-password"));

        KeyStoreBuilder builder = new KeyStoreBuilder()
                .storetype(KeyStoreType.DIGICERTONE)
                .storepass(storePassword);

        if (config.endpoint() != null && !config.endpoint().isBlank()) {
            builder.keystore(config.endpoint());
        } else {
            builder.keystore("https://clientauth.one.digicert.com");
        }
        return builder.build();
    }

    private void configureTimestamping(AuthenticodeSigner signer) {
        SigningProperties.Timestamp timestamp = properties.timestamp();
        if (timestamp == null || !timestamp.enabled()) {
            return;
        }
        signer.withTimestampingMode(TimestampingMode.RFC3161);
        if (timestamp.authority() != null && !timestamp.authority().isBlank()) {
            signer.withTimestampingAuthority(timestamp.authority());
        }
        if (timestamp.retries() >= 0) {
            signer.withTimestampingRetries(timestamp.retries());
        }
        if (timestamp.retryWaitSeconds() >= 0) {
            signer.withTimestampingRetryWait(timestamp.retryWaitSeconds());
        }
    }

    private void validateInput(String scriptContent, String fileName) {
        if (scriptContent == null || scriptContent.isBlank()) {
            throw new SigningException("Script content must not be blank");
        }
        if (!sanitizeFileName(fileName).toLowerCase().endsWith(POWERSHELL_EXTENSION)) {
            throw new SigningException("Only PowerShell .ps1 scripts are supported");
        }
        if (scriptContent.getBytes(StandardCharsets.UTF_8).length > properties.maxScriptBytes()) {
            throw new SigningException("Script content exceeds configured size limit");
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "script.ps1";
        }
        return Path.of(fileName).getFileName().toString();
    }

    private String require(String value, String property) {
        if (value == null || value.isBlank()) {
            throw new SigningException("Required property is missing: " + property);
        }
        return value;
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Temp-file cleanup failure is non-fatal; OS cleanup still applies.
        }
    }
}
