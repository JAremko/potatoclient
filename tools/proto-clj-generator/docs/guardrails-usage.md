# Guardrails Usage Guide

This guide explains how to use Guardrails with the proto-clj-generator and how to check coverage.

## What is Guardrails?

Guardrails is a runtime validation library for Clojure that provides:
- Function input/output validation using `>defn` syntax
- Integration with Malli specs
- Development-time validation (can be disabled in production)
- Better error messages than assertions or pre/post conditions

## Generated Code with Guardrails

All generated functions use the `>defn` macro:

```clojure
(>defn build-root
  "Build a Root protobuf message from a map."
  [m]
  [root-spec => any?]  ; Validates input matches root-spec, output is any (Java object)
  (let [builder (cmd.JonSharedCmd$Root/newBuilder)]
    ;; ... implementation
    (.build builder)))

(>defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.JonSharedCmd$Root proto]
  [any? => root-spec]  ; Validates output matches root-spec
  (cond-> {}
    ;; ... implementation
    ))
```

## Checking Guardrails Coverage

The project includes a guardrails-check tool to ensure all functions have specs.

### Basic Usage

```bash
# Check all generated namespaces
make guardrails-check

# Check specific namespace
clojure -X:guardrails-check :namespaces '[potatoclient.proto.cmd]'

# Check multiple namespaces
clojure -X:guardrails-check :namespaces '[potatoclient.proto.cmd potatoclient.proto.ser]'
```

### Understanding the Output

```
Checking namespace: potatoclient.proto.cmd
Functions using >defn: 12
Functions using >defn-: 8
Regular defn functions: 0
Total functions: 20
✓ All functions are using guardrails!

Checking namespace: potatoclient.proto.cmd.rotaryplatform
Functions using >defn: 45
Functions using >defn-: 12
Regular defn functions: 0
Total functions: 57
✓ All functions are using guardrails!
```

### If Coverage is Missing

If you see unspecced functions:

```
Regular defn functions: 2
Functions without specs:
  - my-helper-function
  - another-function
```

You should either:
1. Convert them to `>defn` with proper specs
2. Mark them as intentionally unspecced (rare)

## Writing Custom Guardrails Functions

When adding custom functions to work with generated code:

```clojure
(require '[com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]])

;; Public function with validation
(>defn process-command
  "Process a command with additional logic"
  [cmd-map context]
  [::cmd/root-spec map? => ::processed-command]
  (merge cmd-map
         {:processed-at (java.time.Instant/now)
          :context context}))

;; Private function with validation
(>defn- validate-payload
  "Validate payload specific rules"
  [payload]
  [map? => boolean?]
  (and (contains? payload :type)
       (some? (:data payload))))
```

## Development vs Production

### Development Mode (Default)
- All validations run
- Detailed error messages
- Performance impact is acceptable

### Production Mode
Generate without guardrails:
```bash
clojure -X:gen :guardrails? false
```

Or disable at runtime:
```clojure
;; In production config
(require '[com.fulcrologic.guardrails.config :as gr])
(gr/clear-config!)
```

## Common Patterns

### Optional Fields
```clojure
(>defn build-with-optional
  [m]
  [[:map [:required-field int?]
         [:optional-field {:optional true} string?]]
   => any?]
  ;; implementation
  )
```

### Complex Validation
```clojure
(>defn build-validated
  [m]
  [[:and
    ::base-spec
    [:fn {:error/message "Custom validation failed"}
     #(valid-custom-rule? %)]]
   => any?]
  ;; implementation
  )
```

### Nil-safe Functions
```clojure
(>defn safe-parse
  [proto]
  [(? any?) => (? ::output-spec)]  ; ? means nilable
  (when proto
    ;; parse logic
    ))
```

## Integration with clj-kondo

The project includes clj-kondo configuration for guardrails:

```clojure
;; .clj-kondo/config.edn
{:lint-as {com.fulcrologic.guardrails.malli.core/>defn clojure.core/defn
           com.fulcrologic.guardrails.malli.core/>defn- clojure.core/defn}}
```

This prevents false positive warnings about unknown macros.

## Troubleshooting

### "Unknown spec" errors
Ensure the spec is defined before the function:
```clojure
(def my-spec [:map [:field int?]])

(>defn my-function
  [m]
  [my-spec => any?]  ; Spec must be defined above
  ;; ...
  )
```

### Performance Issues
If validation is slow:
1. Check for expensive predicates in specs
2. Consider simpler specs for hot paths
3. Disable in production if needed

### Missing Specs in Generated Code
This shouldn't happen, but if it does:
1. Check the generator ran with `:guardrails? true`
2. Regenerate the code
3. File a bug report

## Best Practices

1. **Always spec public functions** - Use `>defn` for all public API
2. **Use `>defn-` for private functions** - Even private functions benefit from validation
3. **Keep specs close to usage** - Define specs in the same namespace
4. **Test with validation on** - Catch issues during development
5. **Profile before disabling** - Only disable if proven performance issue