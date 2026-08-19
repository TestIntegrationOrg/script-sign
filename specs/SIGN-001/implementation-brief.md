# AI Implementation Brief — SIGN-001

## Source of Truth
- Intake: `specs/SIGN-001/00-intake.md`
- Specification: `specs/SIGN-001/specification.md`
- Architecture: `specs/SIGN-001/architecture/architecture.md`
- ADR: `specs/SIGN-001/adr/ADR-001-initial-architecture.md`
- Security review: `specs/SIGN-001/security-review.md`
- Plan: `specs/SIGN-001/plan.md`
- Tasks: `specs/SIGN-001/tasks.yaml`

## Implementation Boundary
Implement only the approved PowerShell `.ps1` synchronous signing capability. Do not add local private-key storage or Windows-only signing dependencies.

## Key Rules
1. Private signing key remains in DigiCert ONE / KeyLocker.
2. Secrets come from deployment configuration, never request payloads.
3. Use Authenticode SHA-256 and RFC 3161 timestamping.
4. Enforce script-size and extension validation before remote signing.
5. Delete temporary material on success and failure.
6. Do not claim Windows execution trust unless endpoint trust policy permits it.
7. Add tests for API validation and controlled errors.
