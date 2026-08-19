package com.testintegrationorg.scriptsign.api;

import com.testintegrationorg.scriptsign.signing.ScriptSigningService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scripts")
public class ScriptSigningController {

    private final ScriptSigningService signingService;

    public ScriptSigningController(ScriptSigningService signingService) {
        this.signingService = signingService;
    }

    @PostMapping(value = "/sign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SignScriptResponse sign(@Valid @RequestBody SignScriptRequest request) {
        ScriptSigningService.SignedScript signed = signingService.sign(request.scriptContent(), request.fileName());
        return new SignScriptResponse(signed.fileName(), signed.signedContent(), "AUTHENTICODE_SHA256_RFC3161", signed.signerAlias());
    }

    public record SignScriptRequest(@NotBlank String fileName, @NotBlank String scriptContent) {
    }

    public record SignScriptResponse(String fileName, String signedContent, String signatureType, String signerAlias) {
    }
}
