package com.testintegrationorg.scriptsign.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "signing")
public record SigningProperties(
        @NotBlank String provider,
        DigiCert digicert,
        Timestamp timestamp,
        @Positive long maxScriptBytes) {

    public record DigiCert(
            String endpoint,
            String apiKey,
            String clientCertificatePath,
            String clientCertificatePassword,
            String alias) {
    }

    public record Timestamp(
            boolean enabled,
            String authority,
            int retries,
            int retryWaitSeconds) {
    }
}
