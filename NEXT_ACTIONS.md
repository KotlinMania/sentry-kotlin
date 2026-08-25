# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/11 (45.5%)
- **Function parity:** 15/47 matched (target 47) — 31.9%
- **Class/type parity:** 5/15 matched (target 11) — 33.3%
- **Combined symbol parity:** 20/62 matched (target 58) — 32.3%
- **Average inline-code cosine:** 0.34 (function body across 4 matched files)
- **Average documentation cosine:** 0.00 (doc text across 4 matched files)
- **Cheat-zeroed Files:** 2
- **Critical Issues:** 4 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. init

- **Target:** `sentry.Init`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 30606.9
- **Functions:** 2/4 matched (target 8)
- **Missing functions:** `deref`, `drop`
- **Types:** 1/2 matched
- **Missing types:** `Target`

### 2. transports.mod

- **Target:** `sentry.Transport [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10410.0
- **Functions:** 1/1 matched (target 5)
- **Missing functions:** _none_
- **Types:** 2/3 matched
- **Missing types:** `HttpTransport`

### 3. transports.ratelimit

- **Target:** `sentry.RateLimiter`
- **Similarity:** 0.62
- **Dependents:** 0
- **Priority Score:** 1203.8
- **Functions:** 10/10 matched (target 11)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 3/3 matched

### 4. defaults

- **Target:** `sentry.Defaults`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 205.7
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 5. lib

- **Target:** `sentry.Lib [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 17)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

