# SIGN-001 Architecture

## Decision
Use a stateless Spring Boot service with Jsign as the Authenticode implementation and DigiCert ONE / KeyLocker as the remote private-key boundary.

## Components

```text
Caller
  |
  | POST /api/v1/scripts/sign
  v
Script Signing API
  |
  +--> validation / size limits
  |
  +--> ephemeral .ps1 file
  |       |
  |       v
  |    Jsign AuthenticodeSigner
  |       |
  |       +--> DigiCert ONE / KeyLocker signing API
  |       |      (private key never leaves provider)
  |       |
  |       +--> DigiCert RFC3161 Timestamp Authority
  |
  +--> signed PowerShell content
```

## Architecture Drivers

- Security: private-key isolation, no credential material in requests, controlled temp-file lifecycle.
- Compatibility: produce embedded Microsoft Authenticode signatures consumable by Windows/PowerShell.
- Portability: Java service can run on Linux or Windows because Jsign performs Authenticode signing cross-platform.
- Operability: configuration is environment-driven; health endpoint is exposed through Spring Boot Actuator.

## Key Decisions

### ADR-001 — Remote private key
The application uses DigiCert ONE / KeyLocker through Jsign `DIGICERTONE`; it does not import the production private signing key.

### ADR-002 — Authenticode implementation
Use Jsign rather than invoking Windows `signtool.exe`. This removes the requirement for Windows hosts and avoids shell-process invocation.

### ADR-003 — Timestamping
Use SHA-256 signing and RFC 3161 timestamping. Timestamp retries are configurable.

### ADR-004 — Initial API model
The first release returns signed content inline. For high-throughput or large-artifact workloads, evolve to object-storage input/output plus asynchronous jobs rather than increasing request payload sizes indefinitely.

## Trust Boundary
A successful signing operation proves possession/use of the configured signing identity. Windows execution trust additionally depends on certificate validity/chain, timestamp validation, local Trusted Publishers/root stores, PowerShell execution policy, WDAC/AppLocker, and other endpoint controls.
