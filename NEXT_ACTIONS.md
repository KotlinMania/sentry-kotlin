# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/11 (45.5%)
- **Function parity:** 15/47 matched (target 154) — 31.9%
- **Class/type parity:** 6/15 matched (target 65) — 40.0%
- **Combined symbol parity:** 21/62 matched (target 219) — 33.9%
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

### 1. init

- **Target:** `sentry.Init [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 30606.9
- **Functions:** 2/4 matched (target 8)
- **Missing functions:** `deref`, `drop`
- **Types:** 1/2 matched
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/init.rs` vs expected `init.rs`
- **Proposed provenance header:** `// port-lint: source init.rs` (current: `// port-lint: source sentry/src/init.rs`)
- **Lint issues:** 1

### 2. transports.ratelimit

- **Target:** `sentry.RateLimiter [PROVENANCE-FALLBACK]`
- **Similarity:** 0.62
- **Dependents:** 0
- **Priority Score:** 1203.8
- **Functions:** 10/10 matched (target 11)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/transports/ratelimit.rs` vs expected `transports/ratelimit.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:sentry/src/transports/ratelimit.rs` vs expected `transports/ratelimit.rs`
- **Proposed provenance header:** `// port-lint: source transports/ratelimit.rs` (current: `// port-lint: source sentry/src/transports/ratelimit.rs`)
- **Proposed provenance header:** `// port-lint: tests transports/ratelimit.rs` (current: `// port-lint: tests sentry/src/transports/ratelimit.rs`)
- **Lint issues:** 2

### 3. transports.mod

- **Target:** `sentry.Transport [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 1/1 matched (target 5)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/transports/mod.rs` vs expected `transports/mod.rs`
- **Proposed provenance header:** `// port-lint: source transports/mod.rs` (current: `// port-lint: source sentry/src/transports/mod.rs`)
- **Lint issues:** 1

### 4. defaults

- **Target:** `sentry.Defaults [PROVENANCE-FALLBACK]`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 205.7
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/defaults.rs` vs expected `defaults.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:sentry/src/defaults.rs` vs expected `defaults.rs`
- **Proposed provenance header:** `// port-lint: source defaults.rs` (current: `// port-lint: source sentry/src/defaults.rs`)
- **Proposed provenance header:** `// port-lint: tests defaults.rs` (current: `// port-lint: tests sentry/src/defaults.rs`)
- **Lint issues:** 2

### 5. lib

- **Target:** `sentry.Hub [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 124)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 54)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `sentry/src/lib.rs` vs expected `lib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:sentry/src/lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source sentry/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source sentry/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source sentry/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source sentry/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source sentry/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source sentry/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source sentry/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source sentry/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source sentry/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source sentry/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source sentry/src/lib.rs`)
- **Proposed provenance header:** `// port-lint: tests lib.rs` (current: `// port-lint: tests sentry/src/lib.rs`)
- **Lint issues:** 12

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

