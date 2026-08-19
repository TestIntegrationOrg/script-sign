# Architecture Review Evidence — SIGN-001

## Decision Reviewed
Jsign + DigiCert ONE / KeyLocker remote signing.

## Evidence
- Jsign supports Authenticode signing for PowerShell scripts.
- Jsign provides a DigiCert ONE / KeyLocker remote signing keystore integration.
- The design avoids exporting the production private key and avoids a Windows `signtool.exe` host dependency.

## Review Result
Accepted for the initial synchronous `.ps1` service.

## Follow-up Risks
- Remote signing latency/quota may require asynchronous/batched signing at scale.
- Endpoint trust configuration remains an external dependency.
- Production authorization and auditability must be completed before broad exposure.
