---
name: requirements-analysis
description: Analyze product intent into a testable, implementation-useful requirements baseline with material NFRs and decision gaps.
---
# Requirements Analysis

1. Restate the business problem, desired outcome, actors, scope, and explicit constraints from evidence.
2. Use Known, Proposed, Assumption, Open question, and Blocker consistently.
3. Convert explicit intent into stable functional requirement IDs and observable acceptance criteria when IDs do not already exist.
4. Add clearly marked Proposed requirements when they are conventional, directly support stated intent, and do not silently choose business policy.
5. Identify missing inputs, outputs, boundaries, failure behavior, compatibility, lifecycle behavior, authentication/authorization, data handling, and operational behavior when material.
6. Capture NFRs that materially affect architecture. Do not demand arbitrary numeric targets; keep them open only when the next decision truly depends on them.
7. Keep open questions short and decision-oriented.
8. Mark a gap Blocker only when the next lifecycle action cannot proceed safely or would likely create invalid behavior, security exposure, contract breakage, or expensive rework.
9. Distinguish implementation blockers from items that can be resolved during architecture, planning, hardening, rollout, or operations.
10. Keep SD-AI runtime/policy diagnostics out of feature requirements unless they directly constrain feature behavior.

Preferred output: disposition; problem/outcome; known facts; proposed functional requirements; acceptance criteria; material NFR/security requirements; assumptions; open questions; blockers; recommended next step.

Do not stop at “requirements are incomplete” when a useful proposed baseline can be produced safely. Do not invent business intent, compliance requirements, ownership, or externally visible policy.
