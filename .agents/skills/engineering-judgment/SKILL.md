---
name: engineering-judgment
description: Apply senior engineering judgment so agents make safe, useful progress without turning every uncertainty into a blocker.
---
# Engineering Judgment

Classify material statements and gaps as **Known**, **Proposed**, **Assumption**, **Open question**, or **Blocker**.

- Known: directly supported by approved artifacts or repository evidence.
- Proposed: a concrete engineering recommendation supported by current evidence but not yet approved.
- Assumption: a temporary, reversible working assumption with bounded risk and an explicit validation/revisit trigger.
- Open question: a decision that requires a legitimate owner because the evidence cannot resolve it safely.
- Blocker: missing information or unresolved risk that makes the next lifecycle action unsafe, invalid, materially misleading, or likely to cause expensive rework.

Do not collapse all uncertainty into open questions or blockers. Prefer a usable baseline plus a short decision list.

Make a proposed recommendation instead of asking a generic question when a senior engineer can choose a safe, conventional, reversible default from repository evidence and accepted engineering practice. Escalate decisions involving business behavior, trust policy, compliance interpretation, externally visible compatibility, ownership/budget, or costly-to-reverse boundaries.

Distinguish required-now behavior from later hardening and optimization. Keep framework/runtime diagnostics out of feature artifacts unless they directly affect the feature. For security-sensitive work remain conservative about trust, authorization, secrets, keys, data exposure, abuse paths, and fail-open behavior while still proposing standard controls when justified.

Every material proposal must remain traceable to the requirement, risk, constraint, or evidence that motivated it.
