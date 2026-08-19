# Test Evidence — SIGN-001

## Automated Tests Present
`ScriptSigningControllerTest` covers API request validation and controller behavior with the signing service isolated from the remote provider.

## Not Yet Executed in This Environment
The connected GitHub workflow used to create these commits does not provide a local Maven runtime or DigiCert credentials, so this artifact does **not** claim that `mvn test` or live signing has been executed here.

## Required Verification
1. Run `mvn clean verify`.
2. Run an integration test against a non-production DigiCert signing identity.
3. On Windows, run `Get-AuthenticodeSignature <signed.ps1>` and verify signer certificate, status, and timestamp.
4. Verify behavior under the target PowerShell execution policy / Trusted Publishers / WDAC or AppLocker policy.

## Status
Automated test source: present. Build execution: pending. Live cryptographic verification: pending.
