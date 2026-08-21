package com.testintegrationorg.scriptsign.signing;

public interface ScriptSigningUseCase {

    SignedScript sign(SignScriptCommand command);
}
