package com.testintegrationorg.scriptsign.signing;

public interface ScriptSigningService {
    SignedScript sign(String scriptContent, String fileName);

    record SignedScript(String fileName, String signedContent, String signerAlias) {
    }
}
