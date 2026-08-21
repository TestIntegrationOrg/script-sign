# Specification — PS-SIGN-001

## Status

Approved for implementation by the repository owner through the request to build and commit the service.

## Problem

Provide a Java microservice that accepts an unsigned PowerShell `.ps1` script and returns the same script with a Windows Authenticode signature. The service must be testable without production signing credentials while keeping deployable signing material outside source control.

## Functional Requirements

- **FR-001 — Sign script:** `POST /api/v1/powershell/signatures` MUST accept a JSON request containing `fileName` and `scriptContent` and return signed script content.
- **FR-002 — PowerShell only:** The service MUST accept only a simple `.ps1` file name and MUST reject path segments, other extensions, blank content, and content containing an existing Authenticode signature block.
- **FR-003 — Authentic signature:** The returned script MUST contain a SHA-256 Authenticode signature produced with the configured PKCS#12 private key and certificate chain.
- **FR-004 — Timestamping:** Deployments MUST be able to enable RFC 3161 timestamping, configure one or more trusted timestamp authorities, and configure bounded retries.
- **FR-005 — Controlled errors:** Validation, size, already-signed, configuration, and signing failures MUST produce stable problem responses without returning secrets or script content.
- **FR-006 — Health:** The service MUST expose liveness and readiness health endpoints.

## Non-Functional Requirements

- **NFR-001 — Secret isolation:** PKCS#12 files and passwords MUST be injected at deployment time and MUST NOT be committed, logged, or returned by the API.
- **NFR-002 — Data minimization:** Script content MUST be processed in memory and an ephemeral file only, MUST NOT be logged, and the temporary file MUST be deleted after every outcome.
- **NFR-003 — Request bound:** UTF-8 script content MUST be limited by a configurable byte threshold that defaults to 1 MiB.
- **NFR-004 — Concurrency:** Signing state MUST be request-scoped or immutable so concurrent requests cannot corrupt each other.
- **NFR-005 — Observability:** Responses MUST carry a correlation identifier and logs MUST describe outcomes without recording script content or credentials.
- **NFR-006 — Portability:** The service and tests MUST run on Linux while producing signatures verifiable by Windows PowerShell.

## API Contract

The normative contract is `contracts/openapi.yaml`.

## Assumptions

- Callers send JSON over TLS through an authenticated service boundary or API gateway.
- The configured PKCS#12 identity contains an RSA or EC private-key entry and a code-signing certificate chain.
- Timestamp authority availability is external to this service; a timestamp-enabled signing request fails closed when all configured authorities fail.
- The service is stateless and does not persist input or output scripts.

## Non-Goals

- Certificate issuance, renewal, or private-key generation.
- User-facing certificate trust decisions.
- Signing file types other than PowerShell `.ps1`.
- Long-term script storage, workflow orchestration, or asynchronous queues.
- Implementing an identity provider; authentication is enforced by the deployment boundary.

## Acceptance Criteria

- **AC-001:** A valid `.ps1` request returns HTTP 200, preserves the unsigned script body, and appends a parseable Authenticode signature block.
- **AC-002:** An automated test cryptographically verifies the embedded CMS signer information with the certificate embedded in the signature.
- **AC-003:** Unsupported names, blank scripts, oversized scripts, and already-signed scripts are rejected with controlled RFC 9457 problem responses.
- **AC-004:** A temporary file is absent after both successful and failed signing operations.
- **AC-005:** Tests generate an isolated non-production signing identity and do not require external credentials or a live timestamp authority.
- **AC-006:** `mvn clean verify` succeeds.
- **AC-007:** `sdai validate PS-SIGN-001` reports no blocking violations.

## Traceability

| Requirement | Tasks | Evidence |
|---|---|---|
| FR-001, FR-002, FR-005 | T-001, T-003 | API and controller tests |
| FR-003, NFR-002, NFR-004 | T-002 | cryptographic service tests |
| FR-004 | T-002, T-004 | signer configuration and documentation |
| FR-006, NFR-005 | T-003 | actuator and correlation filter |
| NFR-001, NFR-003 | T-001, T-004 | configuration, validation, threat model |
| AC-006, AC-007 | T-005 | verification evidence |
