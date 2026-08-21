package com.testintegrationorg.scriptsign.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "signing")
public class SigningProperties {

    private long maxScriptBytes = 1_048_576;
    private Path tempDirectory;
    private final Keystore keystore = new Keystore();
    private final Timestamp timestamp = new Timestamp();

    public long getMaxScriptBytes() {
        return maxScriptBytes;
    }

    public void setMaxScriptBytes(long maxScriptBytes) {
        this.maxScriptBytes = maxScriptBytes;
    }

    public Path getTempDirectory() {
        return tempDirectory;
    }

    public void setTempDirectory(Path tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public Keystore getKeystore() {
        return keystore;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public static class Keystore {
        private String path;
        private String storePassword;
        private String keyPassword;
        private String alias;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getStorePassword() {
            return storePassword;
        }

        public void setStorePassword(String storePassword) {
            this.storePassword = storePassword;
        }

        public String getKeyPassword() {
            return keyPassword;
        }

        public void setKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }
    }

    public static class Timestamp {
        private boolean enabled = true;
        private List<String> authorities = new ArrayList<>(List.of("https://timestamp.sectigo.com"));
        private int retries = 2;
        private int retryWaitSeconds = 2;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getAuthorities() {
            return authorities;
        }

        public void setAuthorities(List<String> authorities) {
            this.authorities = authorities == null ? new ArrayList<>() : new ArrayList<>(authorities);
        }

        public int getRetries() {
            return retries;
        }

        public void setRetries(int retries) {
            this.retries = retries;
        }

        public int getRetryWaitSeconds() {
            return retryWaitSeconds;
        }

        public void setRetryWaitSeconds(int retryWaitSeconds) {
            this.retryWaitSeconds = retryWaitSeconds;
        }
    }
}
