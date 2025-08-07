# Cross-Namespace Registry Fix Progress

## Issue Summary
When generating Clojure code from protobuf descriptors, cross-namespace enum references were causing `:malli.core/invalid-schema` errors. The issue was that namespace A was referencing enums from namespace B using qualified keywords (e.g., `:ser/jon-gui-data-fx-mode-day`), but these weren't resolvable at namespace load time because the registry wasn't set up yet.

## Problem Details

### Original Issue
1. The daycamera namespace was referencing `:ser/jon-gui-data-fx-mode-day` in its spec
2. The enum was defined in the ser namespace
3. At namespace load time, the qualified keyword couldn't be resolved
4. This caused Malli validation errors when guardrails was enabled

### Root Cause
The generator was creating specs with qualified keywords for cross-namespace references, but:
- Each namespace was setting its own default registry
- The registries weren't available at namespace compile time
- Qualified keywords in specs couldn't be resolved

## Solution Approach

### Initial Attempt (Completed)
1. Created a centralized registry approach:
   - Each namespace exports its registry without setting it as default
   - Created `test_registry.clj` helper to combine all registries
   - Tests set up the combined registry before running

### Current Work (In Progress)
2. Modified spec generation to use alias references instead of qualified keywords:
   - Updated `spec_gen.clj` to detect cross-namespace enum references
   - When an enum is from a different namespace, generate a symbol reference (e.g., `types/jon-gui-data-fx-mode-day-spec`) instead of a keyword
   - This allows the spec to reference the actual spec variable through the namespace alias

## Code Changes Made

### 1. Updated spec_gen.clj
```clojure
;; Modified generate-specs-for-namespace to accept more context
(defn generate-specs-for-namespace
  [{:keys [messages enums current-package require-specs] :as namespace-data}]
  (let [;; Build a map of namespace aliases from require-specs
        ns-aliases (into {}
                        (for [[ns-sym :as alias-sym] require-specs
                              :let [ns-str (str ns-sym)
                                    ns-suffix (last (str/split ns-str #"\\."))]
                          [ns-suffix (str alias-sym)]))
        context {:current-package current-package
                 :ns-aliases ns-aliases}]
    ;; ... rest of function
```

```clojure
;; Modified enum field processing to use alias references
(defmethod process-field-type :enum
  [field context]
  (let [type-ref (get-in field [:type :enum :type-ref])
        type-keyword (type-name->keyword type-ref)
        current-package (:current-package context)
        ns-aliases (:ns-aliases context)
        enum-ns (namespace type-keyword)]
    (if (and enum-ns 
             (not= enum-ns current-package)
             (contains? ns-aliases enum-ns))
      ;; Cross-namespace reference - use alias
      (let [alias-name (get ns-aliases enum-ns)
            enum-name (name type-keyword)
            spec-var-name (str (csk/->kebab-case enum-name) "-spec")]
        ;; Return a symbol that references the spec through the alias
        (symbol (str alias-name "/" spec-var-name)))
      ;; Same namespace or no alias - use keyword as before
      type-keyword)))
```

### 2. Updated frontend.clj
```clojure
;; Pass additional context to spec generation
specs-code (when generate-specs?
             (let [{:keys [enum-specs message-specs]} 
                   (spec-gen/generate-specs-for-namespace 
                    {:messages messages 
                     :enums enums
                     :current-package current-package
                     :require-specs require-specs})]
               ;; ... rest
```

### 3. Created test_registry.clj
```clojure
(ns generator.test-registry
  "Registry setup for tests"
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            [test.roundtrip.ser :as ser]
            [test.roundtrip.cmd :as cmd]
            ;; ... other namespaces
            ))

(defn setup-registry!
  "Setup the combined registry for all namespaces"
  []
  (let [combined-registry (merge
                          {:oneof oneof/-oneof-schema}
                          ser/registry
                          cmd/registry
                          ;; ... other registries
                          )]
    (mr/set-default-registry!
     (mr/composite-registry
      (m/default-schemas)
      combined-registry))))
```

## Current Status

### What's Working
1. ✅ Registry generation and export
2. ✅ Centralized registry setup for tests
3. ✅ Modified spec generation to detect cross-namespace references
4. ✅ Code generation runs successfully

### Issues Found
1. ❌ Generated code has syntax errors - the alias reference is being inserted incorrectly
2. ❌ Need to fix the spec generation to properly format the symbol reference

### Next Steps
1. Fix the syntax error in generated specs
2. Ensure the symbol references are properly formatted
3. Run tests to verify the cross-namespace references work
4. Clean up any remaining issues

## Test Results
- Started with 60 test failures
- After various fixes, reduced to 54 failures
- Main remaining issue is the cross-namespace registry access

## Files Modified
1. `/src/generator/spec_gen.clj` - Modified enum field processing
2. `/src/generator/frontend.clj` - Pass context to spec generation
3. `/test/generator/test_registry.clj` - Created registry helper
4. `/test/generator/basic_roundtrip_test.clj` - Use centralized registry

## Related TODO Items
- Fix cross-namespace registry access - ser enums not available in cmd namespaces (IN PROGRESS)
- Change enum spec references to use alias instead of qualified keywords (IN PROGRESS)