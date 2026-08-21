package com.testintegrationorg.scriptsign.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignScriptRequest(
        @NotBlank
        @Size(max = 128)
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._ -]*\\.[Pp][Ss]1$")
        String fileName,

        @NotNull
        @Size(min = 1)
        String scriptContent) {
}
