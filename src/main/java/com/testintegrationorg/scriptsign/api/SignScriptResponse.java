package com.testintegrationorg.scriptsign.api;

import com.testintegrationorg.scriptsign.signing.SignedScript;

public record SignScriptResponse(
        String fileName,
        String signedScript,
        String digestAlgorithm,
        boolean timestamped,
        String signerSubject,
        String certificateSerialNumber) {

    public static SignScriptResponse from(SignedScript result) {
        return new SignScriptResponse(
                result.fileName(),
                result.signedScript(),
                result.digestAlgorithm(),
                result.timestamped(),
                result.signerSubject(),
                result.certificateSerialNumber());
    }
}
