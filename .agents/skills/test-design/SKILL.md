---
name: test-design
description: Build risk-based tests tied to acceptance criteria, contracts, and failure modes.
---
# Test Design

- Trace tests to acceptance criteria and material NFRs.
- Cover success, boundary, failure, timeout, retry/idempotency, authorization, and concurrency paths where relevant.
- Add integration/contract/resilience tests where unit tests cannot prove system behavior.
- Prefer deterministic tests and explicit test data.
- Flag acceptance criteria that cannot be observed or verified.
