# ADR-001: Use Jsign with DigiCert Remote Signing

- **Status:** Accepted
- **Feature:** SIGN-001
- **Date:** 2026-08-18

## Context
The service must sign PowerShell scripts so Windows can validate a Microsoft Authenticode signature while keeping the production private key outside the Java application.

## Decision
Use Jsign as the Authenticode implementation and its `DIGICERTONE` remote keystore integration with DigiCert ONE / KeyLocker. Use SHA-256 and RFC 3161 timestamping. Do not invoke `signtool.exe` and do not load a production PFX signing private key into the service.

## Consequences
### Positive
- Non-exportable/remote private-key boundary.
- Cross-platform Java deployment.
- Native Authenticode script support.
- No shell invocation of platform signing tools.

### Negative
- Signing depends on DigiCert availability and credentials.
- The initial synchronous API can be constrained by remote-signing latency/quota.
- Endpoint trust policy remains outside this service.

## Follow-up
Add workload authentication/authorization, signing audit events, rate limiting, and asynchronous bulk-signing if throughput requirements demand it.
