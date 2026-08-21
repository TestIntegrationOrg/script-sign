package com.testintegrationorg.scriptsign.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.testintegrationorg.scriptsign.config.SigningProperties;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Pkcs12SigningIdentityTest {

    @TempDir
    Path testDirectory;

    @Test
    void loadsCodeSigningIdentityAndCreatesSigner() throws Exception {
        Path keystore = TestSigningIdentityFactory.createPkcs12(testDirectory);
        SigningProperties properties = TestSigningIdentityFactory.properties(keystore, testDirectory.resolve("temp"));

        Pkcs12SigningIdentity identity = new Pkcs12SigningIdentity(properties);

        assertThat(identity.certificate().getSubjectX500Principal().getName())
                .contains("SD-AI Test Code Signing");
        assertThat(identity.newSigner()).isNotNull();
    }

    @Test
    void failsClosedWhenIdentityIsMissing() {
        SigningProperties properties = new SigningProperties();
        properties.getKeystore().setPath(testDirectory.resolve("missing.p12").toString());
        properties.getKeystore().setStorePassword("not-a-secret");

        assertThatThrownBy(() -> new Pkcs12SigningIdentity(properties))
                .isInstanceOf(SigningConfigurationException.class)
                .hasMessageContaining("not a regular file");
    }
}
