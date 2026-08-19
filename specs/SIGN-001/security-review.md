# Security Review — SIGN-001

## Trust Boundaries
- Caller → signing API.
- Signing service → DigiCert ONE / KeyLocker.
- Signing service → timestamp authority.
- Signing service → ephemeral filesystem.

## Findings and Controls
- **Private-key theft:** mitigated by remote signing; the production key never enters the application.
- **Credential disclosure:** API key and client-certificate password are external deployment secrets and must not be logged.
- **Signing-oracle abuse:** production endpoint requires authenticated/authorized service-to-service access before broad exposure.
- **Payload abuse:** enforce `.ps1`, nonblank content, and configured maximum bytes.
- **Path traversal:** reduce caller filename to a safe basename and reject unsupported extension.
- **Temporary-data exposure:** delete temp files in `finally`; use encrypted/ephemeral storage and least-privileged runtime identity.
- **Supply-chain risk:** Jsign is security-critical; pin and scan dependency versions.
- **Error leakage:** return controlled errors without provider secrets.

## Production Gate
Authentication/authorization, rate limiting, and auditable signing events are required before exposing the endpoint outside a trusted service boundary.
