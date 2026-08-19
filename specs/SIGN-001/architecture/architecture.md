# Architecture — SIGN-001

## Decision
Use a stateless Spring Boot service with Jsign for Authenticode processing and DigiCert ONE / KeyLocker for remote private-key operations.

## Architecture Drivers
- Private-key isolation and least privilege.
- Native Windows Authenticode compatibility.
- Cross-platform Java deployment.
- Small synchronous request/response contract for initial `.ps1` scope.
- Explicit timestamping, input limits, temp-file cleanup, and controlled failure handling.

## Components
1. **Script Signing API** — validates request, filename, extension, and size.
2. **Signing Application Service** — orchestrates signing and response construction.
3. **Jsign Authenticode Adapter** — creates the PowerShell Authenticode signature.
4. **DigiCert ONE / KeyLocker** — performs remote signing with a non-exportable production private key.
5. **RFC 3161 TSA** — timestamps the signature.
6. **Observability Boundary** — health, structured logs, and future signing audit events.

## Data Flow
1. Caller posts `fileName` and `scriptContent`.
2. API validates `.ps1`, content presence, and maximum byte size.
3. Service creates an ephemeral temporary `.ps1` file.
4. Jsign loads the DigiCert remote signing keystore and requests the signature.
5. Jsign timestamps the signature using RFC 3161.
6. Signed PowerShell content is read and returned.
7. Temporary file is deleted in a `finally` path.

## Trust Boundaries
- Caller → service API: caller identity/authorization must be enforced before broad production exposure.
- Service → DigiCert: API key and client-authentication certificate are deployment secrets.
- DigiCert signing key: remains outside the service process.
- Service filesystem: ephemeral script content may temporarily exist and must be protected/deleted.

## Reliability
- Signing call is synchronous for the initial release.
- Timestamp retry count/wait are configurable.
- Large-scale/bulk signing should evolve to an asynchronous job model rather than unbounded HTTP payloads.

## Observability
- Actuator health endpoint.
- Controlled error responses.
- Production follow-up: correlation ID, signing audit event, latency/error metrics, and provider-specific failure categorization.

## Traceability
- Remote key boundary → REQ-003 / AC-005.
- SHA-256 Authenticode → REQ-002 / AC-002.
- RFC 3161 timestamp → REQ-004.
- Validation and temp cleanup → REQ-005 / REQ-006 / AC-004.
