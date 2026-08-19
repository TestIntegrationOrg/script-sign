package com.testintegrationorg.scriptsign.api;

import com.testintegrationorg.scriptsign.signing.ScriptSigningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScriptSigningController.class)
class ScriptSigningControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ScriptSigningService signingService;

    @Test
    void signsValidPowerShellRequest() throws Exception {
        when(signingService.sign(anyString(), anyString()))
                .thenReturn(new ScriptSigningService.SignedScript("test.ps1", "signed-content", "prod-key"));

        mockMvc.perform(post("/api/v1/scripts/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"test.ps1\",\"scriptContent\":\"Write-Host 'hello'\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.ps1"))
                .andExpect(jsonPath("$.signedContent").value("signed-content"))
                .andExpect(jsonPath("$.signerAlias").value("prod-key"));
    }

    @Test
    void rejectsBlankContent() throws Exception {
        mockMvc.perform(post("/api/v1/scripts/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"test.ps1\",\"scriptContent\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
