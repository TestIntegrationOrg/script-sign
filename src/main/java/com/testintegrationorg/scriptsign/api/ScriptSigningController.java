package com.testintegrationorg.scriptsign.api;

import com.testintegrationorg.scriptsign.signing.ScriptSigningUseCase;
import com.testintegrationorg.scriptsign.signing.SignScriptCommand;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/powershell/signatures")
public class ScriptSigningController {

    private final ScriptSigningUseCase signingService;

    public ScriptSigningController(ScriptSigningUseCase signingService) {
        this.signingService = signingService;
    }

    @PostMapping
    public ResponseEntity<SignScriptResponse> sign(@Valid @RequestBody SignScriptRequest request) {
        var result = signingService.sign(new SignScriptCommand(request.fileName(), request.scriptContent()));
        return ResponseEntity.ok(SignScriptResponse.from(result));
    }
}
