# AI Implementation Brief — PS-SIGN-001

## Source of Truth
- Intake: `specs/PS-SIGN-001/00-intake.md`
- Specification: `specs/PS-SIGN-001/specification.md`
- Architecture: `specs/PS-SIGN-001/architecture/architecture.md`
- Plan: `specs/PS-SIGN-001/plan.md`

## Agent Rules
1. Do not invent new requirements.
2. Do not change a material architecture decision without proposing an ADR.
3. Prefer the smallest change that satisfies acceptance criteria.
4. Add/update automated tests with code changes.
5. Never place secrets, credentials, private keys, or sensitive production data in prompts or logs.
6. Report assumptions and unresolved blockers explicitly.

## Execution Boundary
This MVP creates the implementation contract but does not autonomously modify application source. A coding-agent adapter will consume this artifact in a future milestone.
