# Architecture Decision Matrix

Scores use 1 (poor) through 5 (strong); higher weighted totals are better.

| Criterion | Weight | Platform executable | In-process Jsign | Async worker |
|---|---:|---:|---:|---:|
| Linux/container portability | 5 | 1 | 5 | 4 |
| Authenticode correctness | 5 | 5 | 5 | 5 |
| Automated testability | 4 | 2 | 5 | 3 |
| Secret isolation | 5 | 3 | 3 | 5 |
| Operational simplicity | 4 | 2 | 5 | 1 |
| Command-injection exposure | 4 | 1 | 5 | 4 |
| Current-scope fit | 5 | 2 | 5 | 2 |
| **Weighted total** |  | **65** | **135** | **100** |

In-process Jsign is selected. Secret isolation is bounded by the requested PKCS#12 deployment model; a future remote-HSM adapter can replace the identity implementation without changing the API.
