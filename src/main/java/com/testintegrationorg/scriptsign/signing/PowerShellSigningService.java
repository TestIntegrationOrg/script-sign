package com.testintegrationorg.scriptsign.signing;

import com.testintegrationorg.scriptsign.config.SigningProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public final class PowerShellSigningService implements ScriptSigningUseCase {

    private static final Pattern POWERSHELL_FILE_NAME =
            Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._ -]{0,123}\\.[Pp][Ss]1$");
    private static final String SIGNATURE_START = "# SIG # Begin signature block";
    private static final String SIGNATURE_END = "# SIG # End signature block";

    private final PowerShellScriptSigner signer;
    private final long maxScriptBytes;
    private final Path tempDirectory;

    public PowerShellSigningService(PowerShellScriptSigner signer, SigningProperties properties) {
        this.signer = signer;
        if (properties.getMaxScriptBytes() < 1) {
            throw new SigningConfigurationException("signing.max-script-bytes must be greater than zero");
        }
        this.maxScriptBytes = properties.getMaxScriptBytes();
        this.tempDirectory = initializeTempDirectory(properties.getTempDirectory());
    }

    @Override
    public SignedScript sign(SignScriptCommand command) {
        validate(command);

        Path temporaryFile = null;
        RuntimeException pendingFailure = null;
        try {
            temporaryFile = createTemporaryFile();
            Files.writeString(temporaryFile, command.scriptContent(), StandardCharsets.UTF_8);
            SigningMetadata metadata = signer.sign(temporaryFile);
            String signedContent = Files.readString(temporaryFile, StandardCharsets.UTF_8);
            return new SignedScript(
                    command.fileName(),
                    signedContent,
                    metadata.digestAlgorithm(),
                    metadata.timestamped(),
                    metadata.signerSubject(),
                    metadata.certificateSerialNumber());
        } catch (RuntimeException exception) {
            pendingFailure = exception;
            throw exception;
        } catch (IOException exception) {
            pendingFailure = new SigningUnavailableException("Unable to process the PowerShell script", exception);
            throw pendingFailure;
        } finally {
            deleteTemporaryFile(temporaryFile, pendingFailure);
        }
    }

    private void validate(SignScriptCommand command) {
        if (command == null) {
            throw new InvalidScriptException("A signing request is required");
        }
        if (command.fileName() == null || !POWERSHELL_FILE_NAME.matcher(command.fileName()).matches()) {
            throw new InvalidScriptException("fileName must be a simple PowerShell .ps1 file name");
        }
        if (command.scriptContent() == null || command.scriptContent().isBlank()) {
            throw new InvalidScriptException("scriptContent must not be blank");
        }
        long contentBytes = command.scriptContent().getBytes(StandardCharsets.UTF_8).length;
        if (contentBytes > maxScriptBytes) {
            throw new ScriptTooLargeException("scriptContent exceeds the configured byte limit");
        }
        if (command.scriptContent().contains(SIGNATURE_START) || command.scriptContent().contains(SIGNATURE_END)) {
            throw new AlreadySignedScriptException("scriptContent already contains an Authenticode signature block");
        }
    }

    private Path createTemporaryFile() throws IOException {
        return tempDirectory == null
                ? Files.createTempFile("script-sign-", ".ps1")
                : Files.createTempFile(tempDirectory, "script-sign-", ".ps1");
    }

    private static Path initializeTempDirectory(Path configuredDirectory) {
        if (configuredDirectory == null) {
            return null;
        }
        try {
            Path normalized = configuredDirectory.toAbsolutePath().normalize();
            Files.createDirectories(normalized);
            if (!Files.isDirectory(normalized) || !Files.isWritable(normalized)) {
                throw new SigningConfigurationException("The signing temporary directory is not writable");
            }
            return normalized;
        } catch (IOException exception) {
            throw new SigningConfigurationException("Unable to initialize the signing temporary directory", exception);
        }
    }

    private static void deleteTemporaryFile(Path temporaryFile, RuntimeException pendingFailure) {
        if (temporaryFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException cleanupFailure) {
            if (pendingFailure != null) {
                pendingFailure.addSuppressed(cleanupFailure);
                return;
            }
            throw new SigningUnavailableException("Unable to remove the temporary script", cleanupFailure);
        }
    }
}
