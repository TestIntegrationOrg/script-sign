# Security Review — PS-SIGN-001

## Scope and disposition

Reviewed the REST boundary, signing identity loading, Jsign adapter, timestamp
egress, ephemeral filesystem lifecycle, error mapping, logs, container, tests,
and deployment guidance. No blocking application finding remains. Deployment
controls at the ingress and secret store are mandatory preconditions, not
features supplied by this service.

## Required checks

- [x] Authentication and authorization documented: enforced by trusted ingress.
- [x] Least privilege applied: non-root container and read-only secret guidance.
- [x] Input validation and output handling considered: leaf `.ps1` names only,
  unknown JSON fields rejected, UTF-8 byte bound, existing signatures rejected.
- [x] Secret handling documented: runtime-only PKCS#12/password injection; no
  secret, script, or signed output logging.
- [x] Encryption considered: TLS at ingress and HTTPS-only timestamp authorities.
- [x] Sensitive logging prohibited: stable redacted problem responses and
  correlation-only logging.
- [x] Dependency and supply-chain risk considered: pinned Jsign, Maven Central,
  wrapper, and high-severity CI dependency review.
- [x] Abuse and denial-of-service considered: application byte limit plus required
  ingress body/rate/concurrency limits.
- [x] Auditability documented: correlation IDs and external gateway/audit system.

## Findings

| ID | Severity | Finding | Disposition / evidence |
| --- | --- | --- | --- |
| SEC-001 | High if omitted | The service has no internal caller authentication. | Accepted only behind authenticated and authorized ingress; README and threat model make this a deployment requirement. |
| SEC-002 | Medium | PKCS#12 exposes the private key to the Java process. | Accepted for this ADR; mount read-only with least privilege. HSM/remote signing requires a follow-up ADR. |
| SEC-003 | Medium | A crash can leave an ephemeral script until node cleanup. | Dedicated ephemeral volume, non-root process, and `finally` deletion; success/failure cleanup tests pass. |
| SEC-004 | Medium | Application validation occurs after HTTP body parsing. | Enforce the same or smaller body limit and rate limit at ingress. |
| SEC-005 | Low | Timestamping depends on external TSA availability. | HTTPS-only authorities, bounded retries, multiple configurable authorities, and fail-closed signing. |

## Review result

Approved for the defined trust model. Production release is conditional on
deploying the documented ingress, secret-management, logging, egress, and
ephemeral-volume controls.
