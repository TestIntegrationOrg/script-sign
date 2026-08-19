# Architecture Decision Matrix — SIGN-001

Score: 1 = poor, 5 = strong.

| Quality Attribute | Jsign + DigiCert remote signing | Windows signtool service | Local PFX in Java |
|---|---:|---:|---:|
| Private-key isolation | 5 | 4 | 1 |
| Cross-platform deployment | 5 | 1 | 5 |
| Native Authenticode compatibility | 5 | 5 | 4 |
| Operational simplicity | 4 | 2 | 4 |
| Security posture | 5 | 4 | 1 |
| Cloud/container suitability | 5 | 2 | 3 |

## Decision
Select **Jsign + DigiCert ONE / KeyLocker remote signing**.

## Rationale
It preserves the private-key boundary, avoids a Windows-only signing host, provides first-class Authenticode support for PowerShell scripts, and integrates with DigiCert remote signing through Java/JCA abstractions.

## Rejected Options
- **Windows signtool service** — valid but adds a Windows host/runtime dependency and process-execution boundary.
- **Local PFX/private key in application** — rejected for production because it weakens private-key isolation and secret-management posture.
