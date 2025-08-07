# Development Guide

This guide covers development practices for the proto-clj-generator tool.

## Guardrails and Malli Integration

The proto-clj-generator uses Fulcrologic Guardrails with Malli schemas for runtime validation during development.

### Using Malli Guardrails

We use `com.fulcrologic.guardrails.malli.core` instead of the regular guardrails namespace to get Malli schema support:

```clojure
(ns generator.deps
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => ?]]))

;; Example function with inline Malli schemas
(>defn extract-file-dependencies
  "Extract dependency information from a file descriptor."
  [file-desc]
  [[:map 
    [:type [:= :file]]
    [:name string?]
    [:package string?]
    [:dependencies {:optional true} [:vector string?]]]
   => 
   [:map
    [:name string?]
    [:package string?]
    [:depends-on [:vector string?]]]]
  {:name (:name file-desc)
   :package (:package file-desc)
   :depends-on (vec (or (:dependencies file-desc) []))})
```

### Key Patterns

1. **Input/Output Specs**: Use the `[input-spec => output-spec]` pattern
2. **Optional Values**: Use `{:optional true}` for optional map keys
3. **Maybe Types**: Use `[:maybe type]` or `(? type)` for nullable returns
4. **Multiple Args**: List each argument spec separately: `[spec1 spec2 spec3 => output]`

### Common Guardrails Patterns

```clojure
;; Simple predicate specs
[int? string? => map?]

;; Inline Malli schemas
[[:map [:name string?]] => string?]

;; Optional return values
[int? => (? string?)]

;; Such-that predicates
[int? int? | #(< %1 %2) => int?]

;; Vector with specific element type
[any? => [:vector string?]]

;; Map with specific key-value types
[map? => [:map-of string? any?]]
```

### Testing with Guardrails

Run tests with Guardrails enabled:

```bash
# Guardrails are enabled by default in test alias
make test

# Or directly with clojure
clojure -J-Dguardrails.enabled=true -X:test
```

### Registry Considerations

When using Malli with Guardrails, be careful about registry setup:

1. Don't set a default Malli registry that might conflict with Guardrails
2. Use inline schemas in guardrails specs rather than registry lookups
3. For complex shared schemas, define them as vars and reference directly

## Specter Usage

The project includes Specter for complex nested data transformations. Consider using Specter for:

- Deep updates to nested IR structures
- Collecting values from deeply nested paths
- Transforming collections within nested maps

Example patterns:

```clojure
(require '[com.rpl.specter :as sp])

;; Transform all fields in all messages
(sp/transform [sp/ALL :messages sp/ALL :fields sp/ALL :type]
              enrich-type
              descriptor-set)

;; Collect all type references
(sp/select [sp/ALL :messages sp/ALL :fields sp/ALL 
            (sp/multi-path [:type :message :type-ref]
                          [:type :enum :type-ref])]
           descriptor-set)
```

## Development Workflow

1. **Make Changes**: Edit source files
2. **Run Tests**: `make test` to ensure nothing breaks
3. **Check Guardrails**: Errors show detailed validation failures
4. **Generate Code**: `make generate` to test generation
5. **Lint Generated**: `make lint-generated` to check output

## Debugging Tips

### Guardrails Errors

When you see a Guardrails error:
1. Check the "Value" section - what was actually passed
2. Check the "Schema" section - what was expected
3. Look for missing keys, wrong types, or invalid values

### Malli Schema Debugging

```clojure
;; In REPL, validate schemas manually
(require '[malli.core :as m])

(m/validate [:map [:name string?]] {:name "test"})
;; => true

(m/explain [:map [:name string?]] {:name 123})
;; => {:schema [:map [:name string?]], 
;;     :value {:name 123}, 
;;     :errors ({:path [:name], :in [:name], :schema string?, :value 123})}
```

### Common Issues

1. **Stack Overflow in Malli**: Usually caused by circular registry references
2. **"Unable to resolve spec"**: Spec not in registry or wrong namespace
3. **Type validation failures**: Check if test data matches expected schema

## Performance Considerations

Guardrails add overhead during development but can be completely removed in production:

```clojure
;; Generate without guardrails for production
(main/generate {:guardrails? false ...})
```

This removes all `>defn` overhead and generates regular `defn` functions.