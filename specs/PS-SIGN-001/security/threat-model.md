# Threat Model — PS-SIGN-001

## Assets

- PKCS#12 private key, certificate chain, and passwords.
- Integrity of signed PowerShell content.
- Service availability and signing-provider quota.
- Caller identity and correlation/audit metadata.

## Trust Boundaries

1. Caller to authenticated TLS ingress.
2. Ingress to signing service.
3. Signing service process to mounted PKCS#12 secret.
4. Signing service to RFC 3161 timestamp authority.
5. Signing service to ephemeral filesystem.

## Threats and Controls

| Threat | Control | Residual risk |
|---|---|---|
| Unauthorized party obtains trusted signatures | Mandatory authenticated/authorized ingress, network allow-list, rate limits | Deployment misconfiguration |
| Path traversal overwrites arbitrary files | Accept simple `.ps1` leaf names only; generate server-owned random temp names | Library/runtime filesystem vulnerability |
| Script or credentials leak through logs/errors | Never log content, signature blocks, passwords, or key material; stable problem details | External infrastructure logging request bodies |
| Oversized payload exhausts memory/disk | Configurable UTF-8 byte limit and ingress body limit | Chunked request buffering before application validation |
| Existing malicious signature is nested/replaced | Reject Authenticode signature markers | Obfuscated non-standard markers |
| Private key is extracted | Read-only secret mount, least-privilege workload identity, restricted filesystem/process access | PKCS#12 model exposes key to the Java process |
| Timestamp response is spoofed | HTTPS TSA endpoints, Jsign RFC 3161 validation, fail closed | Compromised trusted TSA or CA |
| Temp file remains after failure | `finally` deletion and cleanup tests; dedicated restricted temp directory | Process/node crash before cleanup |
| Dependency compromise | Version pinning, Maven Central, CI dependency review on pull requests | Undetected upstream compromise |
| Signing request is repudiated | Correlation ID and outcome metadata without content logging | Full non-repudiation requires an external audit system |

## Security Requirements

- Production ingress MUST authenticate and authorize every signing caller.
- The PKCS#12 file MUST be mounted read-only and passwords MUST come from a secret manager.
- Production timestamping MUST be enabled with approved HTTPS RFC 3161 authorities.
- Script bodies and signed outputs MUST be excluded from access/application logs.
- The deployment MUST apply request-size and rate limits at ingress in addition to application validation.

## Accepted Limitations

The initial PKCS#12 design permits the Java process to access the private key. If organizational policy requires non-exportable keys, replace the signing identity adapter with an HSM or remote signing provider and create a new ADR.
