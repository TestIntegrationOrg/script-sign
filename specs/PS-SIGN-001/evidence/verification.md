# Verification Evidence — PS-SIGN-001

Date: 2026-08-21

## Automated build

Command:

```text
./mvnw --batch-mode --no-transfer-progress clean verify
```

Result: `BUILD SUCCESS`

| Test suite | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| `Pkcs12SigningIdentityTest` | 2 | 0 | 0 | 0 |
| `PowerShellSigningServiceTest` | 5 | 0 | 0 | 0 |
| `ScriptSigningControllerTest` | 4 | 0 | 0 | 0 |
| Total | 11 | 0 | 0 | 0 |

The service test generates an ephemeral RSA code-signing certificate with
`digitalSignature` key usage and code-signing EKU, signs a PowerShell file via
Jsign, confirms the unsigned and signed Authenticode digests match, reparses the
embedded CMS structure, and verifies its signer information using the embedded
certificate. Other tests cover PKCS#12 validation, request validation, controlled
problems, correlation IDs, and cleanup after success and simulated failure.

## HTTP smoke test

A packaged JAR was launched on an ephemeral port with a newly generated test
PKCS#12 identity and timestamping disabled. The test asserted:

- readiness returned `UP`;
- a real `POST /api/v1/powershell/signatures` returned successfully;
- the response reported SHA-256 and contained an Authenticode signature block;
- `X-Correlation-ID: smoke-PS-SIGN-001` was propagated;
- the process used a dedicated ephemeral work directory.

Result: passed.

## SD-AI critical gate

Command:

```text
.venv/bin/sdai validate PS-SIGN-001 --workflow critical --path .
```

Result: `Validation passed for PS-SIGN-001 (critical)`.

## Container verification

The Dockerfile is built by the `container` CI job. No Docker-compatible runtime
was installed in the implementation environment, so the container build is left
as a pull-request check rather than represented as locally executed.
