package com.testintegrationorg.scriptsign.signing;

public class SigningException extends RuntimeException {
    public SigningException(String message) {
        super(message);
    }

    public SigningException(String message, Throwable cause) {
        super(message, cause);
    }
}
