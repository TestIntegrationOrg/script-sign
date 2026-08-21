# Security policy

Report suspected vulnerabilities privately to the repository maintainers. Do not
include a production signing key, PKCS#12 file, password, signed customer script,
or exploit payload in a public issue.

Operational security requirements and trust boundaries are documented in
[`specs/PS-SIGN-001/security/threat-model.md`](specs/PS-SIGN-001/security/threat-model.md).
The signing service must remain behind an authenticated, authorized, rate-limited
TLS ingress. Its signing identity must be injected at runtime from a secret store,
mounted read-only, and restricted to the service identity.

No real signing identity or secret may be committed to this repository. The test
suite generates an ephemeral self-signed code-signing identity in a temporary
directory and removes it with the test workspace.
