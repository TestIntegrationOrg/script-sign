# Requirement Traceability — PS-SIGN-001

| Requirement | Implementation | Verification |
| --- | --- | --- |
| FR-001 | `ScriptSigningController`, `PowerShellSigningService` | controller success test; HTTP smoke test |
| FR-002 | request annotations and service leaf-name/signature checks | invalid name, extension, and existing-signature tests |
| FR-003 | `Pkcs12SigningIdentity`, `JsignPowerShellScriptSigner` | digest comparison and CMS cryptographic verification |
| FR-004 | RFC 3161 Jsign configuration and HTTPS authority validation | configuration tests; README operations table |
| FR-005 | `ApiExceptionHandler` RFC problem responses | controller validation/size/unknown-field tests |
| FR-006 | Spring Boot Actuator probes | readiness HTTP smoke test |
| NFR-001 | runtime-only identity properties, ignore rules, non-root image | PKCS#12 validation tests; secret handling review |
| NFR-002 | random temporary file and `finally` deletion | success and failure cleanup tests |
| NFR-003 | configurable UTF-8 byte count | multibyte limit test |
| NFR-004 | immutable identity and per-call signer/temp file | implementation inspection and independent test workspaces |
| NFR-005 | correlation filter, redacted errors/log patterns | correlation and no-script-in-problem tests |
| NFR-006 | Java/Jsign Linux implementation | Linux Maven verification; Windows verification instructions |
| AC-001 | end-to-end signing path | service test and HTTP smoke test |
| AC-002 | embedded CMS verification | `signsAndCryptographicallyVerifiesPowerShellScript` |
| AC-003 | validation and RFC problem mapping | service and controller negative tests |
| AC-004 | temporary lifecycle | success/failure cleanup tests |
| AC-005 | `TestSigningIdentityFactory` | all tests run without external credentials/TSA |
| AC-006 | Maven build | clean verify passed, 11/11 tests |
| AC-007 | SD-AI gate | critical validation passed |
