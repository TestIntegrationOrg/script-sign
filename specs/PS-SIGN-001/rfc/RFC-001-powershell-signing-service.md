# RFC-001: PowerShell Script Signing Service

- **Status:** Accepted for implementation
- **Date:** 2026-08-21
- **Feature:** PS-SIGN-001

## Summary

Build a stateless Spring Boot service that signs caller-provided PowerShell scripts with Windows Authenticode using Jsign and a deployment-supplied PKCS#12 identity.

## Motivation

Callers need a portable service rather than direct access to code-signing credentials. The service centralizes validation, signature creation, timestamp policy, error handling, and audit-safe observability.

## Proposal

The normative endpoint is defined in `../contracts/openapi.yaml`. The service validates the request, writes UTF-8 content to a unique temporary `.ps1` file, signs it with SHA-256, optionally timestamps with RFC 3161, reads the signed UTF-8 content, and deletes the file. The response includes signing metadata but never certificate private data.

The PKCS#12 location, store password, key password, alias, byte limit, temporary directory, timestamp authorities, retries, and retry wait are external configuration. Missing or invalid identity configuration prevents the service from becoming ready.

## Compatibility

The API is versioned under `/api/v1`. Additive response fields are permitted. Request-field removal, semantic changes, and new mandatory fields require a new version or a compatibility plan.

## Failure Semantics

- 400: malformed request, unsupported file name, blank content.
- 409: input already contains an Authenticode signature block.
- 413: UTF-8 content exceeds the configured byte limit.
- 500: unexpected internal error with no sensitive details.
- 503: signing identity, cryptographic signing, or timestamp authority unavailable.

## Security and Privacy

See `../security/threat-model.md`. The service does not persist scripts and prohibits content logging. Authenticated ingress, TLS, authorization, rate limits, and restrictive workload permissions are deployment requirements.

## Verification

Automated tests generate a non-production PKCS#12 identity, sign a script, parse the embedded CMS signature, verify signer information cryptographically, compare unsigned digests before and after signing, exercise API failures, and check temporary-file cleanup.

## Rollout

Deploy first with a non-production code-signing certificate and timestamping enabled against the selected TSA. Verify output on Windows with `Get-AuthenticodeSignature`, then enable production callers after access-control and rate-limit checks.
