# Architecture — PS-SIGN-001

## Drivers

- Produce standards-compatible PowerShell Authenticode signatures (FR-003).
- Keep private signing material outside the repository (NFR-001).
- Avoid retaining sensitive script content (NFR-002).
- Provide a small synchronous API with deterministic failure behavior (FR-001, FR-005).
- Permit automated verification without production credentials (AC-002, AC-005).

## Alternatives

### Option A — Invoke a platform-specific signing executable

The service shells out to `signtool.exe` or PowerShell. This closely matches Windows tooling, but requires Windows workers, complicates container deployment, increases command-injection risk, and makes Linux CI difficult.

### Option B — In-process Java signing with Jsign

The service loads an externally supplied PKCS#12 identity and uses Jsign to create an Authenticode signature. It is portable, testable, avoids a command shell, and has direct PowerShell support. It adds a security-sensitive library dependency that must be pinned and scanned.

### Option C — Asynchronous remote signing job

The API stores work and submits a job to a dedicated signing worker or provider. This improves burst control and key isolation at large scale, but introduces storage, queues, status APIs, retention decisions, and operational complexity not justified by the current request.

## Decision

Use **Option B**, a stateless Spring Boot service using Jsign 7.5 and an externally mounted PKCS#12 identity. The REST operation is synchronous. Each request uses an isolated temporary `.ps1` file, creates a fresh signer, signs with SHA-256, optionally obtains an RFC 3161 timestamp, reads the result, and deletes the file in a `finally` block.

## Components

- **Signing API:** JSON validation and RFC 9457 problem responses.
- **Application service:** file-name, byte-size, already-signed, and temporary-file lifecycle rules.
- **Jsign adapter:** immutable signing identity plus request-local `AuthenticodeSigner`.
- **Correlation filter:** propagates or creates `X-Correlation-ID` without logging script data.
- **Actuator:** liveness and readiness endpoints.

## Data Flow

No database or queue is used. Request content exists in the HTTP request, Java memory, and a uniquely named temporary file. Signed content is returned in the response. The temporary file is deleted regardless of success or failure.

## Reliability

- Timestamping is fail-closed when enabled.
- Timestamp retries and wait time are bounded by configuration.
- Signing failures are not retried by the service because repeating a private-key operation may consume provider quota; callers may retry using their own idempotency policy.
- The service is horizontally scalable because it has no shared mutable request state.

## Security

The trust model is defined in `../security/threat-model.md`. The service never accepts key material through the signing API. TLS, caller authentication, authorization, rate limits, and network allow-listing are required at the deployment boundary.

## Observability

Health endpoints expose availability. Each response includes a correlation ID. Logs may include correlation ID, normalized file name, outcome category, and duration; script content, signature blocks, passwords, and key material are prohibited.

## Revisit Conditions

Reconsider Option C if measured signing latency exceeds synchronous API budgets, a remote HSM becomes mandatory, provider quotas require centralized scheduling, or requests must survive process restarts.
