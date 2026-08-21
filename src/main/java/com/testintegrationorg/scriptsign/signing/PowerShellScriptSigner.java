package com.testintegrationorg.scriptsign.signing;

import java.nio.file.Path;

public interface PowerShellScriptSigner {

    SigningMetadata sign(Path scriptFile);
}
