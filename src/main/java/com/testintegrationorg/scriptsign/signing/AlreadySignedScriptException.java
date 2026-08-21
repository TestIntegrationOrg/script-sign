package com.testintegrationorg.scriptsign.signing;

public class AlreadySignedScriptException extends RuntimeException {

    public AlreadySignedScriptException(String message) {
        super(message);
    }
}
