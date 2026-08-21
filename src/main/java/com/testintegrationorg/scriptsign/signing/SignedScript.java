package com.testintegrationorg.scriptsign.signing;

public record SignedScript(
        String fileName,
        String signedScript,
        String digestAlgorithm,
        boolean timestamped,
        String signerSubject,
        String certificateSerialNumber) {
}
