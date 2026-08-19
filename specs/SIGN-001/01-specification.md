# SIGN-001 Specification

## Requirements

- **REQ-001** — The service shall accept PowerShell `.ps1` script content over a versioned REST API.
- **REQ-002** — The service shall produce a Microsoft Authenticode signature using SHA-256.
- **REQ-003** — The signing private key shall remain outside the application process and be used through DigiCert ONE / KeyLocker remote signing.
- **REQ-004** — The signature shall include an RFC 3161 timestamp so signatures remain verifiable according to platform trust rules after certificate expiration.
- **REQ-005** — DigiCert API credentials and client-authentication certificate secrets shall not be accepted from request payloads or committed to source control.
- **REQ-006** — The service shall reject blank scripts, unsupported file extensions, and scripts larger than the configured maximum.
- **REQ-007** — Temporary script material used for signing shall be deleted after each request.
- **REQ-008** — Signing failures shall return a controlled error response without exposing secrets.

## Acceptance Criteria

- **AC-001** — `POST /api/v1/scripts/sign` with valid `.ps1` content returns the filename and signed script content.
- **AC-002** — Returned PowerShell content contains an embedded Authenticode signature when executed against a configured DigiCert signing account.
- **AC-003** — A Windows host can validate the signature chain and timestamp using standard Authenticode validation mechanisms when the issuing chain/publisher is trusted by that host.
- **AC-004** — Invalid/blank input is rejected without contacting the remote signing provider.
- **AC-005** — No private signing key is stored by or transmitted to this service.

## Constraints and Assumptions

- Initial scope is PowerShell `.ps1` only.
- DigiCert ONE / KeyLocker is the initial remote signing provider.
- Publisher trust on Windows is a device-policy/trust-store concern; a valid DigiCert Authenticode signature alone does not override an enterprise execution policy that explicitly blocks the publisher or script.
