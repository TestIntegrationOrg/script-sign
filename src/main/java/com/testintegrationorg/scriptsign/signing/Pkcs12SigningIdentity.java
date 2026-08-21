package com.testintegrationorg.scriptsign.signing;

import com.testintegrationorg.scriptsign.config.SigningProperties;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import net.jsign.AuthenticodeSigner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public final class Pkcs12SigningIdentity {

    private static final String CODE_SIGNING_EKU = "1.3.6.1.5.5.7.3.3";

    private final KeyStore keyStore;
    private final String alias;
    private final String keyPassword;
    private final X509Certificate certificate;

    public Pkcs12SigningIdentity(SigningProperties properties) {
        SigningProperties.Keystore settings = properties.getKeystore();
        requireText(settings.getPath(), "signing.keystore.path is required");
        requireText(settings.getStorePassword(), "signing.keystore.store-password is required");

        Path path = Path.of(settings.getPath()).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            throw new SigningConfigurationException("The configured PKCS#12 signing identity is not a regular file");
        }

        try (InputStream input = Files.newInputStream(path)) {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(input, settings.getStorePassword().toCharArray());
            alias = resolveAlias(keyStore, settings.getAlias());
            keyPassword = StringUtils.hasText(settings.getKeyPassword())
                    ? settings.getKeyPassword()
                    : settings.getStorePassword();
            certificate = (X509Certificate) keyStore.getCertificate(alias);
            validateCertificate(certificate);
            if (keyStore.getKey(alias, keyPassword.toCharArray()) == null) {
                throw new SigningConfigurationException("The configured signing key entry has no private key");
            }
        } catch (SigningConfigurationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new SigningConfigurationException("Unable to load the configured PKCS#12 signing identity", exception);
        }
    }

    public AuthenticodeSigner newSigner() {
        try {
            return new AuthenticodeSigner(keyStore, alias, keyPassword);
        } catch (Exception exception) {
            throw new SigningConfigurationException("Unable to access the configured signing key", exception);
        }
    }

    public X509Certificate certificate() {
        return certificate;
    }

    private static String resolveAlias(KeyStore keyStore, String configuredAlias) throws Exception {
        if (StringUtils.hasText(configuredAlias)) {
            if (!keyStore.isKeyEntry(configuredAlias)) {
                throw new SigningConfigurationException("The configured signing alias is not a private-key entry");
            }
            return configuredAlias;
        }

        List<String> keyAliases = Collections.list(keyStore.aliases()).stream()
                .filter(alias -> isKeyEntry(keyStore, alias))
                .toList();
        if (keyAliases.size() != 1) {
            throw new SigningConfigurationException(
                    "signing.keystore.alias is required when the PKCS#12 file does not contain exactly one private-key entry");
        }
        return keyAliases.get(0);
    }

    private static boolean isKeyEntry(KeyStore keyStore, String alias) {
        try {
            return keyStore.isKeyEntry(alias);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void validateCertificate(X509Certificate certificate) throws Exception {
        if (certificate == null) {
            throw new SigningConfigurationException("The signing certificate is missing");
        }
        certificate.checkValidity();
        boolean[] keyUsage = certificate.getKeyUsage();
        if (keyUsage != null && (keyUsage.length == 0 || !keyUsage[0])) {
            throw new SigningConfigurationException("The signing certificate does not permit digital signatures");
        }
        List<String> extendedKeyUsage = certificate.getExtendedKeyUsage();
        if (extendedKeyUsage != null && !extendedKeyUsage.contains(CODE_SIGNING_EKU)) {
            throw new SigningConfigurationException("The signing certificate is not valid for code signing");
        }
    }

    private static void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new SigningConfigurationException(message);
        }
    }
}
