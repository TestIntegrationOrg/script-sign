# SIGN-001 Security Review

## Threats and Controls

- **Private-key theft** — Production private key is never loaded by the service; signing is delegated to DigiCert ONE / KeyLocker.
- **Credential disclosure** — DigiCert API key and client-certificate password are external configuration only and must be supplied through the deployment secret store.
- **Signing-oracle abuse** — The signing endpoint must be protected by authenticated/authorized service-to-service access at the deployment boundary before production exposure. Recommended controls: mTLS or workload identity plus an API gateway/service mesh policy; do not expose this endpoint directly to the public internet.
- **Oversized payload / resource exhaustion** — Script size is capped by `signing.max-script-bytes`.
- **Path traversal** — Caller-supplied filename is reduced to its basename; only `.ps1` is accepted.
- **Temporary-data exposure** — Temporary signing file is created by the runtime and deleted in `finally`; deployment should use an encrypted/ephemeral filesystem and restrictive container identity.
- **Secret leakage in errors** — API error responses contain controlled messages and do not echo configured secrets.
- **Supply-chain risk** — Jsign is a security-critical dependency and should be pinned, scanned, and upgraded deliberately.

## Production Gate
Do not expose `/api/v1/scripts/sign` outside a trusted service boundary until authentication and authorization are enforced by the deployment platform or implemented in-app. Rate limiting and audit logging should be enabled before broad production use.
