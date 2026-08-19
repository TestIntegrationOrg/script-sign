# Script Signing Service

Java/Spring Boot service for signing PowerShell scripts with Microsoft Authenticode using DigiCert ONE / DigiCert KeyLocker through Jsign.

## What it does

- accepts PowerShell `.ps1` content over REST
- signs with SHA-256 Authenticode
- delegates the private-key operation to DigiCert ONE / KeyLocker
- adds an RFC 3161 timestamp
- returns the signed PowerShell content
- does not accept the production signing private key in the request or store it in the application

Jsign supports PowerShell Authenticode and DigiCert ONE / KeyLocker directly, so the service does not need Windows `signtool.exe` or a Windows host.

## API

```http
POST /api/v1/scripts/sign
Content-Type: application/json

{
  "fileName": "install.ps1",
  "scriptContent": "Write-Host 'Hello'"
}
```

Example response:

```json
{
  "fileName": "install.ps1",
  "signedContent": "...PowerShell content with Authenticode signature block...",
  "signatureType": "AUTHENTICODE_SHA256_RFC3161",
  "signerAlias": "production-code-signing-key"
}
```

## Required configuration

Supply secrets through your deployment secret store; never commit them.

| Environment variable | Purpose |
| --- | --- |
| `DIGICERT_ONE_API_KEY` | DigiCert ONE / KeyLocker API key |
| `DIGICERT_ONE_CLIENT_CERT_PATH` | Path to DigiCert client-authentication PKCS#12 certificate |
| `DIGICERT_ONE_CLIENT_CERT_PASSWORD` | Password for the client-authentication PKCS#12 |
| `DIGICERT_ONE_KEYPAIR_ALIAS` | DigiCert signing keypair/certificate alias |
| `DIGICERT_ONE_ENDPOINT` | Optional DigiCert ONE endpoint; defaults to US host |
| `SIGNING_TIMESTAMP_AUTHORITY` | RFC 3161 TSA; defaults to DigiCert timestamp service |
| `SIGNING_MAX_SCRIPT_BYTES` | Maximum accepted script size; defaults to 1 MiB |

The PKCS#12 file above is used to authenticate the application to DigiCert ONE. The production code-signing private key remains in DigiCert ONE / KeyLocker.

## Build and run

```bash
mvn clean verify
java -jar target/script-sign-0.1.0-SNAPSHOT.jar
```

Or build a container after Maven packaging:

```bash
mvn clean package
podman build -t script-sign:local .
```

## Windows verification

After retrieving a signed script on Windows:

```powershell
$sig = Get-AuthenticodeSignature .\install.ps1
$sig | Format-List Status, StatusMessage, SignerCertificate, TimeStamperCertificate
```

A cryptographically valid signature does not by itself guarantee execution on every Windows device. The issuing certificate chain, Trusted Publishers/root trust, PowerShell execution policy, WDAC/AppLocker, and enterprise endpoint policy still apply.

## Production security

Do not publish the signing endpoint directly to the public internet. Protect it with service-to-service authentication and authorization (for example mTLS/workload identity via API gateway or service mesh), rate limiting, audit logging, and least-privilege access to DigiCert credentials. See `specs/SIGN-001/03-security.md`.

## SD-AI

This repository is initialized as an SD-AI project and `SIGN-001` is classified as a **critical** feature because it creates a security-sensitive code-signing boundary.

```bash
sdai step list SIGN-001 --workflow critical
sdai run SIGN-001 --workflow critical
```

The source-of-truth feature artifacts are under `specs/SIGN-001/`.
