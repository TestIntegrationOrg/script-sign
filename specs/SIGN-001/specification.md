# Specification — SIGN-001

## Title
Java Script Signing Service with DigiCert

## Problem
Provide a Java service that accepts PowerShell script content and returns the same script with a Microsoft Authenticode signature produced through DigiCert remote signing so Windows can validate the publisher signature and timestamp according to endpoint trust policy.

## Functional Requirements
- **REQ-001** — Accept PowerShell `.ps1` script content through `POST /api/v1/scripts/sign`.
- **REQ-002** — Produce an embedded Microsoft Authenticode signature using SHA-256.
- **REQ-003** — Use DigiCert ONE / KeyLocker as the remote private-key boundary; the private key MUST NOT be exported into the service.
- **REQ-004** — Apply an RFC 3161 timestamp using the configured timestamp authority.
- **REQ-005** — Reject blank input, unsupported extensions, and payloads larger than the configured limit.
- **REQ-006** — Remove temporary script material after each request.
- **REQ-007** — Return controlled signing errors without exposing secrets.
- **REQ-008** — Keep DigiCert credentials and client-certificate secrets outside request payloads and source control.

## Non-Functional Requirements
- **NFR-001 Security** — Signing credentials and private-key material must remain outside API payloads and logs.
- **NFR-002 Portability** — The signing service must run on Java 17 without requiring Windows `signtool.exe`.
- **NFR-003 Operability** — Health and configuration must be externally observable without exposing secrets.
- **NFR-004 Traceability** — Requirements, architecture, tasks, implementation, tests, and evidence must remain traceable in `specs/SIGN-001`.

## Acceptance Criteria
- **AC-001** — A valid `.ps1` signing request returns signed PowerShell content.
- **AC-002** — The returned content contains an embedded Authenticode signature when DigiCert signing is configured.
- **AC-003** — Windows signature validation succeeds when the certificate chain, publisher trust, timestamp, and endpoint execution policy permit it.
- **AC-004** — Invalid requests are rejected before remote signing is invoked.
- **AC-005** — The service never receives or stores the production signing private key.

## Constraints
- Initial release supports PowerShell `.ps1` only.
- Windows trust is not determined by the signing service alone; Trusted Publishers/root trust, PowerShell execution policy, WDAC/AppLocker, and related controls still apply.
