package com.testintegrationorg.scriptsign.signing;

public record SigningMetadata(
        String digestAlgorithm,
        boolean timestamped,
        String signerSubject,
        String certificateSerialNumber) {
}
