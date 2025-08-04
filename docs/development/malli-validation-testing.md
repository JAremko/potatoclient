# Malli Validation and Testing Guide

Comprehensive guide to using Malli for validation, property-based testing, and data generation in PotatoClient.

## Overview

Malli is used throughout PotatoClient for:
- Runtime validation with Guardrails
- Property-based testing
- Test data generation
- API documentation
- Error messages

## Basic Validation

### Defining Schemas

```clojure
(require '[malli.core :as m])

;; Simple schemas
(def UserId :uuid)
(def Temperature [:double {:min -273.15}])

;; Composite schemas
(def Point
  [:map
   [:x :double]
   [:y :double]])

;; Domain schemas
(def RotaryCommand
  [:map
   [:rotary
    [:map
     [:goto-ndc
      [:map
       [:channel [:enum :heat :day]]
       [:x {:min -1.0 :max 1.0} :double]
       [:y {:min -1.0 :max 1.0} :double]]]]]])
```

### Validation

```clojure
;; Basic validation
(m/validate Point {:x 0.5 :y -0.25})
;; => true

;; With explanation
(m/explain Point {:x "not a number"})
;; => {:schema Point
;;     :value {:x "not a number"}
;;     :errors [{:path [:x]
;;               :in [:x]
;;               :schema :double
;;               :value "not a number"}]}

;; Human-readable errors
(require '[malli.error :as me])
(-> {:x "not a number"}
    (m/explain Point)
    (me/humanize))
;; => {:x ["should be a double"]}
```

## Integration with Guardrails

### Function Specs

```clojure
(require '[com.fulcrologic.guardrails.core :refer [>defn >defn- => | ?]])

(>defn calculate-speed
  "Calculate rotation speed from pan gesture"
  [distance config]
  [:double 
   [:map 
    [:max-speed :double]
    [:curve-steepness :double]]
   => :double]
  (* (:max-speed config)
     (Math/pow distance (:curve-steepness config))))

;; Multi-arity with specs
(>defn process-command
  ([cmd]
   [::command => ::response]
   (process-command cmd default-options))
  
  ([cmd options]
   [::command ::options => ::response]
   (merge (execute cmd) options)))
```

### Custom Schemas

```clojure
;; Register reusable schemas
(require '[malli.registry :as mr])

(def registry
  (mr/composite-registry
    m/default-registry
    {::command [:or
                [:map [:ping :map]]
                [:map [:rotary ::rotary-command]]
                [:map [:cv ::cv-command]]]
     ::response [:map
                 [:status [:enum :success :error]]
                 [:data :any]]
     ::ndc-coord [:double {:min -1.0 :max 1.0}]}))

;; Use in specs
(>defn goto-ndc
  [x y]
  [::ndc-coord ::ndc-coord => :nil]
  (send-command {:rotary {:goto-ndc {:x x :y y}}}))
```

## Property-Based Testing

### Test Data Generation

```clojure
(require '[malli.generator :as mg])

;; Generate valid data
(mg/generate Point)
;; => {:x -0.5, :y 0.75}

;; Generate samples
(mg/sample Point 5)
;; => ({:x 0.0, :y 0.0}
;;     {:x -0.25, :y 0.5}
;;     {:x 0.125, :y -0.875}
;;     {:x -1.0, :y 1.0}
;;     {:x 0.625, :y -0.375})

;; Custom generators
(def PositivePoint
  [:map
   [:x [:double {:min 0.0}]]
   [:y [:double {:min 0.0}]]])

(mg/sample PositivePoint 3)
;; => ({:x 0.0, :y 0.0}
;;     {:x 0.5, :y 0.25}
;;     {:x 1.5, :y 2.0})
```

### Property Tests

```clojure
(require '[clojure.test :refer [deftest is]]
         '[malli.generator :as mg])

(deftest command-validation-test
  ;; Test that all generated commands are valid
  (doseq [cmd (mg/sample ::command 100)]
    (is (m/validate ::command cmd)
        "Generated command should be valid")))

(deftest roundtrip-test
  ;; Test encoding/decoding preserves data
  (doseq [state (mg/sample ::system-state 50)]
    (let [encoded (transit/encode state)
          decoded (transit/decode encoded)]
      (is (= state decoded)
          "Roundtrip should preserve data"))))

(deftest speed-calculation-properties
  ;; Test mathematical properties
  (doseq [config (mg/sample ::speed-config 100)]
    (is (>= (:max-speed config) 0)
        "Max speed is non-negative")
    (is (<= (calculate-speed 0 config) 
            (calculate-speed 1 config))
        "Speed increases with distance")))
```

### Stateful Property Testing

```clojure
(require '[clojure.test.check :as tc]
         '[clojure.test.check.properties :as prop]
         '[clojure.test.check.generators :as gen])

;; Define state machine operations
(def gesture-ops
  (gen/one-of
    [(gen/return [:mouse-down])
     (gen/return [:mouse-up])
     (gen/return [:mouse-move])]))

;; Property: gesture state machine is always valid
(deftest gesture-state-machine-test
  (let [property
        (prop/for-all [ops (gen/vector gesture-ops)]
          (let [final-state
                (reduce process-gesture-event
                        initial-gesture-state
                        ops)]
            (valid-gesture-state? final-state)))]
    (is (:pass? (tc/quick-check 1000 property)))))
```

## Schema Inference

### From Examples

```clojure
(require '[malli.provider :as mp])

;; Infer schema from data
(def inferred-schema
  (mp/provide
    [{:id 1 :name "Heat" :active true}
     {:id 2 :name "Day" :active false}
     {:id 3 :name "IR" :active true :zoom 2.0}]))

(m/form inferred-schema)
;; => [:map
;;     [:id :int]
;;     [:name :string]
;;     [:active :boolean]
;;     [:zoom {:optional true} :double]]
```

### Schema Evolution

```clojure
;; Start with loose schema
(def ConfigV1
  [:map
   [:version :int]
   [:settings :map]])

;; Evolve to stricter schema
(def ConfigV2
  [:map
   [:version [:= 2]]
   [:settings
    [:map
     [:theme [:enum :light :dark]]
     [:language [:enum :en :uk]]
     [:gestures ::gesture-config]]]])

;; Migration function
(>defn migrate-config
  [config]
  [ConfigV1 => ConfigV2]
  (case (:version config)
    1 (-> config
          (assoc :version 2)
          (update :settings merge default-v2-settings))
    2 config))
```

## Validation Strategies

### Input Validation

```clojure
(>defn handle-user-input
  [input]
  [:map => [:or ::command [:map [:error :string]]]]
  (if (m/validate ::command input)
    (process-command input)
    {:error (str "Invalid input: " 
                 (me/humanize (m/explain ::command input)))}))
```

### Boundary Validation

```clojure
;; Validate at system boundaries
(>defn receive-transit-message
  [bytes]
  [:bytes => [:or ::message ::error]]
  (try
    (let [data (transit/decode bytes)]
      (if (m/validate ::message data)
        data
        {::error :validation-failed
         ::explanation (m/explain ::message data)}))
    (catch Exception e
      {::error :decode-failed
       ::exception e})))
```

### Development vs Production

```clojure
;; Development: strict validation
(def dev-config
  {:validation {:enabled true
                :throw-on-invalid true
                :log-failures true}})

;; Production: performance-focused
(def prod-config
  {:validation {:enabled false  ; Guardrails disabled
                :throw-on-invalid false
                :log-failures true}})
```

## Error Handling

### Detailed Error Messages

```clojure
(require '[malli.error :as me])

(defn command-error-message [invalid-cmd]
  (let [explanation (m/explain ::command invalid-cmd)
        errors (me/humanize explanation)]
    (str "Invalid command:\n"
         (me/error-value explanation errors))))

;; Example output:
;; Invalid command:
;; {:rotary {:goto-ndc {:x 2.0}}}
;;                      ^^^^^^
;;                      - should be <= 1.0
;;                      - missing required key :y
```

### Custom Error Messages

```clojure
(def Point
  [:map {:error/message "Invalid point coordinates"}
   [:x {:error/message "X must be a number"} :double]
   [:y {:error/message "Y must be a number"} :double]])

(me/humanize 
  (m/explain Point {:x "five" :y nil}))
;; => {:x ["X must be a number"]
;;     :y ["Y must be a number"]}
```

## Performance Optimization

### Validation Caching

```clojure
;; Cache validators for performance
(def validators (atom {}))

(defn cached-validator [schema]
  (or (@validators schema)
      (let [v (m/validator schema)]
        (swap! validators assoc schema v)
        v)))

(defn fast-validate [schema value]
  ((cached-validator schema) value))
```

### Selective Validation

```clojure
;; Validate only critical fields in hot paths
(def QuickValidation
  [:map
   [:msg-type :keyword]
   [:timestamp :int]])

(def FullValidation
  [:and
   QuickValidation
   [:map
    [:correlation-id :uuid]
    [:data ::message-data]]])

(>defn process-message
  [msg]
  [:map => :any]
  (when (m/validate QuickValidation msg)
    ;; Full validation in background
    (future 
      (when-not (m/validate FullValidation msg)
        (log/warn "Invalid message" msg)))
    (handle-message msg)))
```

## Best Practices

### Do's

1. ✓ Define schemas close to usage
2. ✓ Use registry for reusable schemas
3. ✓ Generate test data from schemas
4. ✓ Provide good error messages
5. ✓ Validate at boundaries

### Don'ts

1. ✗ Don't over-specify schemas
2. ✗ Don't validate in hot loops
3. ✗ Don't ignore validation errors
4. ✗ Don't duplicate validation logic
5. ✗ Don't use strings for enums

## Integration Examples

### With Transit

```clojure
;; Ensure Transit compatibility
(def TransitCompatible
  [:map
   [:data [:fn {:error/message "Must be Transit-encodable"}
           #(try (transit/encode %) true
                 (catch Exception _ false))]]])
```

### With UI Components

```clojure
;; Validate UI state
(def UIState
  [:map
   [:theme [:enum :light :dark :hi-dark :sol-light :sol-dark]]
   [:panels [:map
             [:heat-visible :boolean]
             [:day-visible :boolean]
             [:control-visible :boolean]]]
   [:zoom [:map
           [:heat [:int {:min 0 :max 4}]]
           [:day [:int {:min 0 :max 4}]]]]])

(>defn update-ui-state
  [state updates]
  [UIState :map => UIState]
  (merge state updates))
```

## Debugging

### Schema Visualization

```clojure
(require '[malli.dot :as dot])

;; Visualize complex schemas
(dot/draw ::command)  ; Opens GraphViz visualization
```

### Validation Tracing

```clojure
;; Trace validation path
(require '[malli.dev :as dev])

(dev/start!)  ; Enable instrumentation

;; Now all >defn functions log validation failures
```

## See Also

- [Testing Guide](./testing.md)
- [Code Standards](./code-standards.md)
- [Transit Protocol](../architecture/transit-protocol.md)