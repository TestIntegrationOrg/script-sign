# Security Evidence — SIGN-001

## Implemented Controls
- Remote/non-exported signing private key.
- Externalized DigiCert credentials.
- Request validation and payload-size limit.
- Controlled error mapping.
- Ephemeral temporary file with deletion in `finally`.
- Non-root container runtime.

## Open Security Gates
- Caller authentication and authorization.
- Rate limiting/abuse protection.
- Durable signing audit trail/correlation ID.
- Dependency and container scanning in CI.

## Status
Implementation is suitable for controlled integration testing. It is not yet approved for unrestricted production exposure.
