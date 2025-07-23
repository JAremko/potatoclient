# Lint False Positives Documentation

This document describes the false positive patterns that are automatically filtered by `make lint-report-filtered`.

## Overview

The PotatoClient codebase uses several libraries and patterns that confuse static analyzers, particularly clj-kondo. Our filtered lint report identifies and removes these false positives, reducing noise from ~358 issues to ~156 real issues (56% false positive rate).

## False Positive Categories

### 1. Seesaw UI Construction Patterns

Seesaw uses a DSL-like syntax with keyword arguments that clj-kondo misinterprets as function calls.

**Pattern**: `Function name must be simple symbol but got: :(id|text|items|title|border|action|name|icon|font|align|center|north|south|east|west|hgap|vgap)`

**Example**:
```clojure
(seesaw/label :id :my-label :text "Hello")  ; :id and :text flagged as function names
(seesaw/border-panel :center content :border 10)  ; :center and :border flagged
```

**Count**: ~122 issues

### 2. Seesaw Keyword Arguments

Related to above but specifically for function argument validation errors.

**Pattern**: `Function arguments should be wrapped in vector` in UI files

**Example**:
```clojure
(seesaw/menu :text (i18n/tr :menu-file)
             :items [file-menu-items])  ; :items value flagged as needing vector wrapping
```

**Count**: ~31 issues

### 3. Telemere Logging Functions

Telemere uses function names with colons that clj-kondo doesn't recognize.

**Pattern**: `Unresolved var: tel/handler:console` and similar

**Functions affected**:
- `handler:console`
- `format-signal-fn`
- `add-handler!`
- `remove-handler!`
- `set-min-level!`
- `set-ns-filter!`
- `stop-handlers!`

**Solution**: Added these to lint-as configuration mapping them to appropriate Clojure core functions.

### 4. Guardrails Spec Symbols

Guardrails requires spec symbols that are often not used directly in the file.

**Pattern**: `#'com.fulcrologic.guardrails.(core|malli.core)/(>|=>|\||>def|\?) is referred but never used`

**Example**:
```clojure
(:require [com.fulcrologic.guardrails.malli.core :refer [>defn | =>]])
;; | and => used in specs, not in regular code
```

**Count**: ~20 issues

### 5. Standard Library Namespaces

False warnings about common Clojure namespaces.

**Patterns**:
- `namespace clojure.(string|java.io) is required but never used`
- `Unresolved namespace clojure.(string|java.io)`

**Count**: ~8 issues

### 6. Seesaw Size Specifications

Seesaw's size syntax using vectors with `:by` keyword.

**Pattern**: `unsupported binding form (120|:by|35)`

**Example**:
```clojure
(seesaw/button :preferred-size [120 :by 35])  ; Valid Seesaw syntax
```

**Count**: ~6 issues

### 7. UI Event Handlers

Redundant do warnings in Seesaw event handlers are often false positives.

**Pattern**: `redundant do` in UI files

**Example**:
```clojure
(seesaw/listen button :action 
  (fn [e] 
    (do  ; Often needed for side effects
      (update-state!)
      (refresh-ui!))))
```

**Count**: ~6 issues

### 8. Other Patterns

- **Menu creation functions**: `unsupported binding form (create-.*-menu.*)`
- **Swing Box components**: `unsupported binding form (Box/.*)`
- **Seesaw text construction**: `a string is not a function` in UI files
- **Boolean properties**: `Boolean cannot be called as a function` in UI files

## Configuration Files

### `.clj-kondo/config.edn`

Enhanced with:
- Telemere function mappings in `:lint-as`
- Namespace configurations via config paths

### `.clj-kondo/configs/potatoclient-namespaces.edn`

Defines vars for:
- `taoensso.telemere` - All special function names
- `clojure.string` - Common string functions
- `clojure.java.io` - IO functions
- `seesaw.core` - Core Seesaw functions

## Using the Filtered Report

```bash
# Generate standard report (includes all false positives)
make lint-report

# Generate filtered report (recommended for development)
make lint-report-filtered
```

The filtered report provides:
- Summary of real vs false positives
- Breakdown by false positive category
- Links to both filtered and unfiltered reports
- Focus on the ~156 real issues that need attention

## Future Improvements

1. **Custom Seesaw Hooks**: Could write more sophisticated clj-kondo hooks to properly parse Seesaw's keyword argument syntax
2. **Upstream Contributions**: Some of these patterns could be contributed back to clj-kondo or the libraries' own configurations
3. **Dynamic Filtering**: The filter patterns could be made configurable via a separate EDN file