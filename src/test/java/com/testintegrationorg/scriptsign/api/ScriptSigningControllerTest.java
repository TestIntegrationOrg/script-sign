package com.testintegrationorg.scriptsign.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testintegrationorg.scriptsign.signing.ScriptSigningUseCase;
import com.testintegrationorg.scriptsign.signing.SignScriptCommand;
import com.testintegrationorg.scriptsign.signing.ScriptTooLargeException;
import com.testintegrationorg.scriptsign.signing.SignedScript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScriptSigningController.class)
@Import(ScriptSigningControllerTest.StubConfiguration.class)
class ScriptSigningControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    StubSigningUseCase signingService;

    @BeforeEach
    void resetSigningService() {
        signingService.reset();
    }

    @Test
    void returnsSignedScriptAndPropagatesCorrelationId() throws Exception {
        signingService.result = new SignedScript(
                "hello.ps1",
                "Write-Output 'hello'\r\n# SIG # Begin signature block\r\n# SIG # End signature block\r\n",
                "SHA-256",
                true,
                "CN=Signer",
                "abc123");

        mockMvc.perform(post("/api/v1/powershell/signatures")
                        .header(CorrelationIdFilter.HEADER_NAME, "request-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new SignScriptRequest("hello.ps1", "Write-Output 'hello'"))))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.HEADER_NAME, "request-123"))
                .andExpect(jsonPath("$.fileName").value("hello.ps1"))
                .andExpect(jsonPath("$.digestAlgorithm").value("SHA-256"))
                .andExpect(jsonPath("$.timestamped").value(true))
                .andExpect(jsonPath("$.signedScript").value(org.hamcrest.Matchers.containsString("signature block")));
    }

    @Test
    void rejectsInvalidFileNameWithoutEchoingScript() throws Exception {
        mockMvc.perform(post("/api/v1/powershell/signatures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"../bad.ps1\",\"scriptContent\":\"secret-script\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists(CorrelationIdFilter.HEADER_NAME))
                .andExpect(jsonPath("$.title").value("Invalid signing request"))
                .andExpect(jsonPath("$.detail").value("Request validation failed"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secret-script"))));
    }

    @Test
    void mapsSizeFailureToPayloadTooLargeProblem() throws Exception {
        signingService.failure = new ScriptTooLargeException("scriptContent exceeds the configured byte limit");

        mockMvc.perform(post("/api/v1/powershell/signatures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"large.ps1\",\"scriptContent\":\"12345\"}"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.title").value("PowerShell script is too large"));
    }

    @Test
    void rejectsUnknownJsonFields() throws Exception {
        mockMvc.perform(post("/api/v1/powershell/signatures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"hello.ps1\",\"scriptContent\":\"Write-Output 1\",\"key\":\"forbidden\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid JSON request"));
    }

    @TestConfiguration
    static class StubConfiguration {

        @Bean
        StubSigningUseCase signingUseCase() {
            return new StubSigningUseCase();
        }
    }

    static final class StubSigningUseCase implements ScriptSigningUseCase {

        private SignedScript result;
        private RuntimeException failure;

        @Override
        public SignedScript sign(SignScriptCommand command) {
            if (failure != null) {
                throw failure;
            }
            return result;
        }

        void reset() {
            result = null;
            failure = null;
        }
    }
}
