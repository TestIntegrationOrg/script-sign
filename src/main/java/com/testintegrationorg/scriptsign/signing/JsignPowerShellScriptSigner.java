package com.testintegrationorg.scriptsign.signing;

import com.testintegrationorg.scriptsign.config.SigningProperties;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.List;
import net.jsign.AuthenticodeSigner;
import net.jsign.DigestAlgorithm;
import net.jsign.Signable;
import net.jsign.timestamp.TimestampingMode;
import org.springframework.stereotype.Component;

@Component
public final class JsignPowerShellScriptSigner implements PowerShellScriptSigner {

    private final Pkcs12SigningIdentity identity;
    private final boolean timestampEnabled;
    private final String[] timestampAuthorities;
    private final int timestampRetries;
    private final int timestampRetryWaitSeconds;

    public JsignPowerShellScriptSigner(Pkcs12SigningIdentity identity, SigningProperties properties) {
        this.identity = identity;
        SigningProperties.Timestamp timestamp = properties.getTimestamp();
        this.timestampEnabled = timestamp.isEnabled();
        this.timestampAuthorities = validateAuthorities(timestamp.getAuthorities(), timestampEnabled);
        if (timestamp.getRetries() < 0 || timestamp.getRetryWaitSeconds() < 0) {
            throw new SigningConfigurationException("Timestamp retry values must not be negative");
        }
        this.timestampRetries = timestamp.getRetries();
        this.timestampRetryWaitSeconds = timestamp.getRetryWaitSeconds();
    }

    @Override
    public SigningMetadata sign(Path scriptFile) {
        try (Signable signable = Signable.of(scriptFile.toFile(), StandardCharsets.UTF_8)) {
            if (!signable.getSignatures().isEmpty()) {
                throw new AlreadySignedScriptException("The PowerShell script is already signed");
            }

            AuthenticodeSigner signer = identity.newSigner()
                    .withDigestAlgorithm(DigestAlgorithm.SHA256)
                    .withSignaturesReplaced(false)
                    .withTimestamping(timestampEnabled);

            if (timestampEnabled) {
                signer.withTimestampingMode(TimestampingMode.RFC3161)
                        .withTimestampingAuthority(timestampAuthorities)
                        .withTimestampingRetries(timestampRetries)
                        .withTimestampingRetryWait(timestampRetryWaitSeconds);
            }

            signer.sign(signable);
            if (signable.getSignatures().size() != 1) {
                throw new SigningUnavailableException("The signed script did not contain exactly one signature", null);
            }

            X509Certificate certificate = identity.certificate();
            return new SigningMetadata(
                    "SHA-256",
                    timestampEnabled,
                    certificate.getSubjectX500Principal().getName(),
                    certificate.getSerialNumber().toString(16));
        } catch (AlreadySignedScriptException | SigningUnavailableException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new SigningUnavailableException("PowerShell signing is temporarily unavailable", exception);
        }
    }

    private static String[] validateAuthorities(List<String> authorities, boolean enabled) {
        if (!enabled) {
            return new String[0];
        }
        if (authorities == null || authorities.isEmpty()) {
            throw new SigningConfigurationException("At least one timestamp authority is required when timestamping is enabled");
        }
        return authorities.stream()
                .map(String::trim)
                .peek(JsignPowerShellScriptSigner::requireHttpsAuthority)
                .toArray(String[]::new);
    }

    private static void requireHttpsAuthority(String authority) {
        try {
            URI uri = URI.create(authority);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new SigningConfigurationException("Timestamp authorities must be absolute HTTPS URLs");
            }
        } catch (IllegalArgumentException exception) {
            throw new SigningConfigurationException("Timestamp authorities must be valid HTTPS URLs", exception);
        }
    }
}
