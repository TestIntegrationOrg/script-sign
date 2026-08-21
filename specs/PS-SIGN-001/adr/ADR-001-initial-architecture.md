# ADR-001: Use in-process Jsign with external PKCS#12 identity

- **Status:** Accepted
- **Date:** 2026-08-21
- **Decision owners:** Repository owner (implementation authorization) and SD-AI implementation workflow

## Context

PS-SIGN-001 requires a Java/Spring Boot microservice that produces verifiable PowerShell Authenticode signatures, runs in Linux CI, does not commit production credentials, and can be tested with a non-production identity.

## Decision

Use Java 17, Spring Boot 3.5, and Jsign 7.5 in process. Load one PKCS#12 signing identity from deployment configuration at startup, create a request-local `AuthenticodeSigner`, sign with SHA-256, and optionally obtain an RFC 3161 timestamp. Use a unique ephemeral file because the supported signing API operates on a PowerShell signable file; delete it after every request.

Expose one synchronous JSON endpoint. Reject existing signature blocks rather than silently replacing or nesting signatures.

## Consequences

### Positive

- Runs on Java/Linux without invoking a command shell.
- Produces native PowerShell Authenticode blocks.
- Unit and integration tests can generate an isolated PKCS#12 identity.
- The API is independent of the future key-storage provider.

### Negative

- The process can access the mounted PKCS#12 private key and password.
- Jsign and its cryptography dependencies become supply-chain-sensitive dependencies.
- Live RFC 3161 behavior depends on an external timestamp service.

## Controls

- Mount the PKCS#12 file read-only and inject passwords from a secret manager.
- Restrict filesystem, process, and network access at deployment time.
- Pin dependency versions, run dependency/security scanning, and verify signed output.
- Move to a remote HSM/signing adapter if policy forbids process-local private keys.
