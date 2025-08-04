# Linting Guide

Comprehensive guide to linting in PotatoClient, including configuration, false positives, and best practices.

## Overview

PotatoClient uses multiple linting tools to maintain code quality:

- **clj-kondo** - Fast Clojure/ClojureScript linter
- **ktlint** - Kotlin code style checker
- **detekt** - Kotlin static analysis

## Running Linters

### Quick Check

```bash
# Run all linters
make lint

# Format check only
make fmt-check

# Full report with filtering
make lint-report-filtered
```

### Individual Linters

```bash
# Clojure only
clj-kondo --lint src test

# Kotlin style
./gradlew ktlintCheck

# Kotlin analysis
./gradlew detekt
```

## clj-kondo Configuration

### Configuration File

`.clj-kondo/config.edn`:

```clojure
{:linters {:unresolved-symbol {:exclude [(potatoclient.ui/with-action)]}
           :unused-namespace {:exclude [potatoclient.logging]}
           :unused-private-var {:level :warning}}
 
 :lint-as {seesaw.core/frame clojure.core/let
           seesaw.core/dialog clojure.core/let
           com.fulcrologic.guardrails.core/>defn clojure.core/defn}
 
 :hooks {:analyze-call {seesaw.core/frame hooks.seesaw/frame
                       com.fulcrologic.guardrails.core/>defn hooks.guardrails/defn}}}
```

### Custom Hooks

For complex macros like Seesaw and Guardrails:

```clojure
;; .clj-kondo/hooks/seesaw.clj
(ns hooks.seesaw
  (:require [clj-kondo.hooks-api :as api]))

(defn frame [{:keys [node]}]
  (let [args (rest (:children node))
        props (take-while keyword? args)]
    {:node (api/list-node
            (list* (api/token-node 'let)
                   (api/vector-node [])
                   args))}))
```

## False Positive Patterns

### Seesaw UI Patterns

Seesaw's DSL creates many false positives:

```clojure
;; These trigger false positives:
(frame :title "App"           ; "Function name must be simple symbol"
       :content (border-panel 
                 :center panel ; "unsupported binding form"
                 :south buttons))

;; Suppress with:
;; 1. File-specific config
;; 2. Filtered lint report
;; 3. Custom hooks
```

### Common False Positives

1. **Seesaw keyword arguments**
   - Pattern: `Function name must be simple symbol but got: :title`
   - Reason: Seesaw uses keywords as property names
   - Files: All UI files

2. **Guardrails multi-arity**
   - Pattern: `Missing docstring` on `>defn` with `(`
   - Reason: Multi-arity function syntax
   - Solution: Use filtered report

3. **Telemere logging**
   - Pattern: `Unresolved var: tel/handler:console`
   - Reason: Dynamic handler creation
   - Solution: Add to unresolved-symbol exclusions

4. **Transit layer functions**
   - Pattern: `Redundant fn wrapper`
   - Reason: Intentional for clarity
   - Solution: Acceptable in transit layer

## Filtered Lint Report

The filtered report removes ~56% of false positives:

```bash
# Generate filtered report
make lint-report-filtered

# Or use Babashka script directly
bb scripts/lint-report-filtered.bb
```

### Report Structure

```
reports/lint/
├── clojure-all.md          # All issues
├── clojure-real.md         # Real issues only
├── clojure-false-positives.md # False positives
└── summary-filtered.md     # Summary statistics
```

### Adding New False Positive Patterns

Edit `scripts/lint-report-filtered.bb`:

```clojure
(def false-positive-patterns
  [{:pattern #"Your pattern here"
    :file-pattern #"specific/.*\.clj$"  ; Optional
    :line-pattern #".*>defn.*"          ; Optional
    :reason "Why this is a false positive"}])
```

## Kotlin Linting

### ktlint Configuration

`.editorconfig`:

```ini
[*.{kt,kts}]
indent_size = 4
insert_final_newline = true
max_line_length = 120
ktlint_standard_no-wildcard-imports = disabled
```

### detekt Configuration

`detekt.yml`:

```yaml
complexity:
  ComplexMethod:
    threshold: 15
  LongMethod:
    threshold: 60
  TooManyFunctions:
    thresholdInClasses: 20

style:
  ForbiddenComment:
    active: false
  MaxLineLength:
    maxLineLength: 120
```

## Best Practices

### Do's

1. ✓ Run linters before committing
2. ✓ Use filtered reports for actionable issues
3. ✓ Add project-specific false positive patterns
4. ✓ Configure IDE integration
5. ✓ Fix warnings early

### Don'ts

1. ✗ Don't ignore all warnings
2. ✗ Don't disable linters globally
3. ✗ Don't commit with errors
4. ✗ Don't suppress without understanding
5. ✗ Don't let warnings accumulate

## IDE Integration

### IntelliJ/Cursive

1. Install clj-kondo plugin
2. Configure external tool:
   ```
   Program: clj-kondo
   Arguments: --lint $FilePath$
   Working directory: $ProjectFileDir$
   ```

### VS Code/Calva

1. clj-kondo integrated by default
2. Configure in settings.json:
   ```json
   {
     "clj-kondo.lint-on-save": true,
     "clj-kondo.config-paths": [".clj-kondo/config.edn"]
   }
   ```

### Emacs

```elisp
;; With flycheck-clj-kondo
(require 'flycheck-clj-kondo)

;; Or with LSP
(add-hook 'clojure-mode-hook #'lsp)
```

## Troubleshooting

### Issue: Too Many False Positives

1. Check if using latest clj-kondo
2. Update false positive patterns
3. Consider custom hooks
4. Use filtered report

### Issue: Slow Linting

1. Exclude build directories
2. Use parallel linting
3. Lint only changed files
4. Cache results

### Issue: Conflicting Rules

1. Check .clj-kondo/config.edn
2. Verify no duplicate configs
3. Check for imported configs
4. Use explicit exclusions

## Suppressing Warnings

### Inline Suppression

```clojure
;; Suppress next line
#_{:clj-kondo/ignore [:unresolved-symbol]}
(some-dynamic-var)

;; Suppress specific linter
#_{:clj-kondo/ignore [:unused-binding]}
(let [x 1] ...)
```

### File-Level Suppression

```clojure
;; At top of file
{:clj-kondo/config '{:linters {:unused-namespace {:level :off}}}}
```

### Namespace Configuration

`.clj-kondo/config.edn`:

```clojure
{:config-in-ns
 {potatoclient.ui {:linters {:unresolved-symbol {:level :off}}}}}
```

## Metrics

Current false positive reduction:
- Raw issues: ~450
- After filtering: ~200
- Reduction: ~56%

Most common false positives:
1. Seesaw UI patterns (40%)
2. Guardrails syntax (20%)
3. Dynamic vars (15%)
4. Test patterns (15%)
5. Other (10%)

## See Also

- [Code Standards](./code-standards.md)
- [Development Workflow](./workflow.md)
- [Testing Guide](./testing.md)