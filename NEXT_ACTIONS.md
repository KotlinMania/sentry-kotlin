# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/31 (16.1%)
- **Function parity:** 15/111 matched (target 154) — 13.5%
- **Class/type parity:** 6/18 matched (target 65) — 33.3%
- **Combined symbol parity:** 21/129 matched (target 219) — 16.3%
- **Average inline-code cosine:** 0.45 (function body across 3 matched files)
- **Average documentation cosine:** 0.00 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 2
- **Critical Issues:** 4 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. sentry.init

- **Target:** `sentry.Init`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 30606.9
- **Functions:** 2/4 matched (target 8)
- **Missing functions:** `deref`, `drop`
- **Types:** 1/2 matched
- **Missing types:** `Target`

### 2. transports.ratelimit

- **Target:** `sentry.RateLimiter`
- **Similarity:** 0.62
- **Dependents:** 0
- **Priority Score:** 1203.8
- **Functions:** 10/10 matched (target 11)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 3/3 matched

### 3. transports.mod

- **Target:** `sentry.Transport [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 1/1 matched (target 5)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_

### 4. sentry.defaults

- **Target:** `sentry.Defaults`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 205.7
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 5. sentry.lib

- **Target:** `sentry.Hub [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 124)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 54)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

