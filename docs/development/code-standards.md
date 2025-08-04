# Code Standards and Practices

This document outlines the coding standards and best practices for PotatoClient development.

## Core Principles

1. **Clean Architecture** - No backward compatibility, clean implementations only
2. **Keywords Everywhere** - All data uses keywords (except log/error text)
3. **Type Safety** - Guardrails and Malli for validation
4. **Performance** - Zero-allocation hot paths, efficient algorithms
5. **Testability** - Pure functions, isolated components

## Guardrails Usage

### The Reality

While our goal is to use Guardrails (`>defn`) for all functions, there are pragmatic exceptions:

**Current Status**:
- Most application code uses Guardrails ✅
- Transit-related namespaces use raw `defn` for performance ⚠️
- 23 functions currently without Guardrails (mostly in transit layer)

**Namespaces with Exceptions**:
- `transit.metadata-handler` - Performance-critical serialization
- `transit.keyword-handlers` - Hot path conversions
- `transit.command-sender` - High-frequency operations
- `instrumentation` - Meta-programming functions

### Guardrails Guidelines

**Always use Guardrails for**:
- UI components and handlers
- Business logic
- State management
- Public APIs

**Consider raw `defn` for**:
- Transit serialization hot paths
- Performance-critical loops
- Meta-programming utilities

### Basic Guardrails Usage

```clojure
(ns your.namespace
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- | ? =>]]))

;; Public function with validation
(>defn process-command
  "Process a command map"
  [command options]
  [::command ::options => ::result]  ; Use namespace-qualified specs
  (merge command options))

;; Private function
(>defn- validate-input
  [input]
  [:string => :boolean]
  (not (clojure.string/blank? input)))

;; Nilable return
(>defn find-stream
  [id]
  [:keyword => (? ::stream)]  ; May return nil
  (get streams id))

;; Constraints
(>defn set-zoom
  [level]
  [:int | #(<= 0 % 10) => :nil]  ; level must be 0-10
  (swap! state assoc :zoom level))
```

## Malli Schemas

### Schema Organization

- **Domain specs**: `potatoclient.ui-specs` namespace
- **Generated specs**: `shared/specs/protobuf/`
- **Transit specs**: Inline with transit code
- **Test specs**: Co-located with tests

### Writing Good Schemas

```clojure
;; Be specific - use enums for known values
(def stream-type
  [:enum :heat :day])

;; Use semantic predicates
(def ndc-coordinate
  [:and :double [:>= -1.0] [:<= 1.0]])

;; Document with metadata
(def command
  ^{:doc "Transit command structure"}
  [:map
   [:action :keyword]
   [:params [:map-of :keyword :any]]])

;; Compose schemas
(def video-config
  [:map
   [:stream stream-type]
   [:resolution resolution]
   [:codec [:enum :h264 :h265]]])
```

## Clojure Style Guide

### Naming Conventions

```clojure
;; Functions: verb or verb-noun
(>defn send-command [cmd] ...)
(>defn validate-state [state] ...)

;; Predicates: end with ?
(>defn valid? [data] ...)
(>defn streaming? [] ...)

;; Side effects: end with !
(>defn save-config! [config] ...)
(>defn reset-state! [] ...)

;; Private: use >defn-
(>defn- internal-helper [] ...)
```

### Data Access

```clojure
;; Prefer keywords for map access
(:key map)  ; Good
(get map :key)  ; Only when key might be nil

;; Use get-in for nested access
(get-in state [:video :config :codec])

;; Destructuring
(let [{:keys [x y]} point]  ; Good
  ...)
```

### State Management

```clojure
;; Single app-db atom
(def app-db (atom initial-state))

;; Always use swap! or reset!
(swap! app-db assoc :key value)
(swap! app-db update :counter inc)

;; Never mutate nested structures
;; Bad: (swap! (:nested @app-db) assoc :key val)
;; Good: (swap! app-db assoc-in [:nested :key] val)
```

## Kotlin Style Guide

### Null Safety

```kotlin
// Use nullable types appropriately
fun processFrame(data: ByteArray?): FrameResult? {
    return data?.let { 
        // Process non-null data
    }
}

// Prefer sealed classes for results
sealed class CommandResult {
    object Success : CommandResult()
    data class Error(val message: String) : CommandResult()
}
```

### Extension Functions

```kotlin
// Add domain-specific operations
fun StreamType.toKeyword(): String = when (this) {
    StreamType.HEAT -> "heat"
    StreamType.DAY -> "day"
}

// But don't overuse - keep it focused
```

### Coroutines

```kotlin
// Use structured concurrency
class VideoProcessor : CoroutineScope {
    override val coroutineContext = Dispatchers.IO + SupervisorJob()
    
    fun process() = launch {
        // Async work
    }
}

// Always handle exceptions
launch {
    try {
        riskyOperation()
    } catch (e: Exception) {
        log.error("Operation failed", e)
    }
}
```

## Testing Standards

### Test Organization

```clojure
;; Use descriptive test names
(deftest command-routing-with-invalid-action
  (testing "Invalid action returns error"
    (is (= :error (route-command {:action :unknown})))))

;; Group related tests
(deftest video-stream-lifecycle
  (testing "Stream startup"
    ...)
  (testing "Stream processing"
    ...)
  (testing "Stream shutdown"
    ...))
```

### Test Data

```clojure
;; Use generators for property tests
(def command-gen
  (mg/generator ::command))

;; Create focused test data
(def test-heat-stream
  {:id :heat
   :type :heat
   :resolution {:width 900 :height 720}})
```

## Performance Guidelines

### Measure First

```clojure
;; Use time for quick checks
(time (process-large-dataset data))

;; Use criterium for accurate benchmarks
(require '[criterium.core :as crit])
(crit/bench (process-data input))
```

### Optimization Patterns

```clojure
;; Use transients for building collections
(persistent!
  (reduce (fn [acc x]
            (conj! acc (process x)))
          (transient [])
          large-seq))

;; Avoid repeated calculations
(let [expensive-calc (calculate-once data)]
  (map #(use-calc % expensive-calc) items))

;; Type hint for Java interop
(>defn process-bytes
  [^bytes data]
  [bytes? => :int]
  (alength data))
```

## Documentation

### Function Documentation

```clojure
(>defn send-command!
  "Sends a command to the command subprocess.
  
  Parameters:
    command - A map with :action and optional :params
    
  Returns:
    A channel that receives the response or error
    
  Example:
    (send-command! {:action :ping})"
  [command]
  [::command => ::channel]
  ...)
```

### Inline Comments

```clojure
;; Explain WHY, not WHAT
;; Bad: (inc x)  ; Increment x
;; Good: (inc retry-count)  ; Exponential backoff requires increasing delays
```

## Git Workflow

### Commit Messages

```
feat: Add gesture recognition to video streams

- Implement tap, double-tap, and pan gestures
- Add CommandBuilder for Transit command generation
- Update MouseEventHandler to use new format

Closes #123
```

### Branch Naming

- `feat/gesture-recognition`
- `fix/memory-leak-video`
- `refactor/transit-handlers`
- `docs/architecture-update`

## Code Review Checklist

- [ ] Uses Guardrails (or justified exception)
- [ ] Has appropriate Malli schemas
- [ ] Includes tests
- [ ] Updates documentation
- [ ] Follows naming conventions
- [ ] No reflection warnings
- [ ] Clean lint report

## Exceptions and Pragmatism

We prioritize:
1. **Working code** over perfect adherence
2. **Performance** where it matters
3. **Clarity** over cleverness
4. **Pragmatism** over dogma

When deviating from standards:
- Document why in code comments
- Get team consensus
- Update this guide if needed

Remember: These are guidelines, not laws. Use good judgment!