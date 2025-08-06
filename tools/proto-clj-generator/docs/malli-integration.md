# Malli Integration and Cross-Namespace Usage

This guide explains how to work with the generated Malli specs and handle cross-namespace protobuf references.

## Generated Malli Specs

The generator creates Malli specs alongside each message and enum type:

### Message Specs

```clojure
(def root-spec
  "Malli spec for root message"
  [:map
   [:protocol-version [:maybe :int]]
   [:session-id [:maybe :int]]
   [:important [:maybe :boolean]]
   [:from-cv-subsystem [:maybe :boolean]]
   [:client-type [:maybe :ser/jon-gui-data-client-type]]
   [:payload
    [:altn
     {:day-camera [:map [:day-camera :cmd.day-camera/root]]
      :heat-camera [:map [:heat-camera :cmd.heat-camera/root]]
      :gps [:map [:gps :cmd.gps/root]]
      ;; ... other variants
      }]]])
```

### Enum Specs

```clojure
;; Enum specs are keywords from the valid set
(def jon-gui-data-client-type-spec
  [:enum :unknown :smartphone :smartwatch :tablet :desktop :embedded])
```

## Cross-Namespace References

The generator handles protobuf packages that reference types from other packages.

### How It Works

1. **Type Lookup**: A global type lookup maps canonical names to type definitions
2. **Namespace Requires**: Generated requires include all referenced namespaces
3. **Qualified Calls**: Cross-namespace calls use namespace-qualified functions

### Example: Command with Multiple Subsystems

```clojure
;; In potatoclient.proto.cmd
(ns potatoclient.proto.cmd
  (:require [potatoclient.proto.cmd.daycamera :as daycamera]
            [potatoclient.proto.cmd.heatcamera :as heatcamera]
            [potatoclient.proto.cmd.rotaryplatform :as rotaryplatform]
            ;; ... other command namespaces
            ))

;; Oneof builder dispatches to namespace-specific builders
(>defn- build-root-payload
  [builder [field-key value]]
  [any? [:tuple keyword? any?] => any?]
  (case field-key
    :day-camera (.setDayCamera builder (daycamera/build-root value))
    :heat-camera (.setHeatCamera builder (heatcamera/build-root value))
    :rotary (.setRotary builder (rotaryplatform/build-root value))
    ;; ... other cases
    ))
```

## Working with Specs

### Validating Data

```clojure
(require '[malli.core :as m]
         '[potatoclient.proto.cmd :as cmd])

;; Validate before building
(def command-data
  {:protocol-version 1
   :session-id 42
   :payload {:rotary {:halt {}}}})

(m/validate cmd/root-spec command-data)
;; => true

(m/explain cmd/root-spec {:payload {:invalid "data"}})
;; => {:schema [:map ...], :value {:payload {:invalid "data"}}, :errors [...]}
```

### Generating Test Data

```clojure
(require '[malli.generator :as mg])

;; Generate valid command data
(mg/generate cmd/root-spec)
;; => {:protocol-version 23, :session-id 456, :payload {:gps {:start {}}}}

;; Generate multiple samples
(mg/sample cmd/root-spec 5)
;; => ({:protocol-version 0, :payload {:ping {}}}
;;     {:protocol-version 1, :session-id 10, :payload {:noop {}}}
;;     ...)
```

### Extending Specs

```clojure
(ns my-app.commands
  (:require [potatoclient.proto.cmd :as cmd]
            [malli.core :as m]))

;; Add business logic constraints
(def valid-session-command
  [:and
   cmd/root-spec
   [:fn {:error/message "Session ID required for important commands"}
    (fn [{:keys [important session-id]}]
      (or (not important) (some? session-id)))]])

;; Compose specs
(def camera-command-spec
  [:and
   cmd/root-spec
   [:fn {:error/message "Must be a camera command"}
    (fn [cmd]
      (let [payload-type (-> cmd :payload keys first)]
        (#{:day-camera :heat-camera} payload-type)))]])
```

## Cross-Namespace Patterns

### 1. Shared Enums

When an enum is used across multiple namespaces:

```clojure
;; In potatoclient.proto.ser (where enum is defined)
(def jon-gui-data-client-type-spec
  [:enum :unknown :smartphone :smartwatch :tablet])

;; In potatoclient.proto.cmd (where enum is used)
(:require [potatoclient.proto.ser :as types])

;; Spec references the enum from ser namespace
(def root-spec
  [:map
   [:client-type [:maybe :ser/jon-gui-data-client-type]]])
```

### 2. Nested Message References

When messages contain fields of types from other packages:

```clojure
;; GPS status might include position from a geometry package
(def gps-status-spec
  [:map
   [:position [:maybe :geo/position-3d]]
   [:accuracy [:maybe :float]]
   [:satellite-count [:maybe :int]]])
```

### 3. Recursive Specs

For recursive message definitions:

```clojure
;; Use malli's recursive schema support
(def tree-node-spec
  [:schema {:registry {::node [:map
                               [:value :string]
                               [:children [:vector [:ref ::node]]]]}}
   [:ref ::node]])
```

## Best Practices

### 1. Namespace Organization

Keep spec extensions close to their usage:

```
my-app/
├── commands/
│   ├── validation.clj      # Command-specific validations
│   ├── generators.clj      # Custom generators for testing
│   └── transformers.clj    # Data transformations
```

### 2. Spec Reuse

Create composed specs for common patterns:

```clojure
;; Common command patterns
(def query-command-spec
  [:and cmd/root-spec
   [:map [:payload [:map-of keyword? [:map [:query-id :string]]]]]])

(def mutation-command-spec
  [:and cmd/root-spec
   [:map [:important [:= true]]]])
```

### 3. Testing with Specs

Use property-based testing:

```clojure
(require '[clojure.test.check.properties :as prop]
         '[clojure.test.check.clojure-test :refer [defspec]])

(defspec roundtrip-test 100
  (prop/for-all [cmd (mg/generator cmd/root-spec)]
    (= cmd (-> cmd
               cmd/build-root
               cmd/parse-root))))
```

### 4. Error Handling

Provide meaningful error messages:

```clojure
(defn validate-and-build [command-data]
  (if (m/validate cmd/root-spec command-data)
    (cmd/build-root command-data)
    (throw (ex-info "Invalid command"
                    {:explanation (m/explain cmd/root-spec command-data)
                     :data command-data}))))
```

## Advanced Topics

### Custom Schemas

Register custom schemas for domain types:

```clojure
(def registry
  (merge (m/default-schemas)
         {:temperature [:and :double [:fn {:error/message "Must be valid temperature"}
                                      #(<= -273.15 % 1000)]]
          :percentage [:and :double [:fn #(<= 0 % 100)]]}))

(def measurement-spec
  [:map {:registry registry}
   [:temp :temperature]
   [:humidity :percentage]])
```

### Coercion

Transform data to match specs:

```clojure
(require '[malli.transform :as mt])

;; Coerce strings to keywords for enums
(def enum-coercer
  (mt/transformer
    {:name :enum-coercer
     :decoders {:enum (fn [spec]
                       (fn [x]
                         (if (string? x)
                           (keyword x)
                           x)))}}))

(m/decode [:enum :heat :day] "heat" enum-coercer)
;; => :heat
```

### Performance

For hot paths, consider pre-compiling validators:

```clojure
(def validate-command (m/validator cmd/root-spec))
(def explain-command (m/explainer cmd/root-spec))

;; Faster than calling m/validate each time
(validate-command command-data)  ; => true/false
(explain-command invalid-data)   ; => explanation
```