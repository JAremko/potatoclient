# Shared Protobuf Specs

This directory contains automatically generated Malli specifications for protobuf messages used in PotatoClient.

## Generated Files

- `protobuf/cmd_specs.clj` - Malli specs for command messages (cmd.* package)
- `protobuf/state_specs.clj` - Malli specs for state messages (ser.* package)  
- `protobuf/generators.clj` - Test data generators and validation helpers

## Usage in Main Application

### 1. Add Shared Specs to Classpath

In your `deps.edn`, include the shared specs:

```clojure
{:paths ["src" "resources" "shared/specs"]}
```

Or create a symlink:
```bash
ln -s ../../shared/specs/protobuf src/potatoclient/specs/protobuf
```

### 2. Using Command Specs

```clojure
(require '[protobuf.cmd-specs :as cmd-specs])

;; Validate a command
(malli.core/validate [:ref :cmd/Root] my-command cmd-specs/schema)

;; Get spec for specific command
(def rotary-spec [:ref :cmd/RotaryPlatform/RotateToNDC])
```

### 3. Using State Specs

```clojure
(require '[protobuf.state-specs :as state-specs])

;; Validate state message
(malli.core/validate [:ref :ser/JonGUIState] state-data state-specs/schema)
```

### 4. Generating Test Data

```clojure
(require '[protobuf.generators :as gen])

;; Generate a valid command
(def test-cmd (gen/generate-command :cmd/RotaryPlatform/RotateToNDC))

;; Generate with seed for reproducibility
(def test-state (gen/generate-state :ser/JonGUIState :seed 42))

;; Create test factory
(def factory (gen/create-test-factory))
(def cmd ((:generate-command factory) :cmd/Ping :overrides {:important true}))
```

### 5. In Guardrails Instrumentation

```clojure
(ns potatoclient.instrumentation
  (:require [protobuf.cmd-specs :as cmd-specs]
            [protobuf.state-specs :as state-specs]))

;; Use in function specs
(m/=> process-command [:=> [:cat [:ref :cmd/Root]] ::response])
(m/=> update-state [:=> [:cat [:ref :ser/JonGUIState]] nil?])
```

### 6. Property-Based Testing

```clojure
(require '[clojure.test.check.properties :as prop]
         '[clojure.test.check.generators :as test.gen])

(defspec all-generated-commands-are-valid 100
  (prop/for-all [cmd (gen/generate-command :cmd/Root)]
    (valid-transit-encoding? cmd)))
```

## Important Notes

1. **Auto-Generated**: These specs are automatically generated from protobuf classes. Do not edit manually.

2. **Structural Baseline**: These specs provide the structure but may not include all buf.validate constraints. They serve as a baseline for:
   - Basic validation
   - Test data generation
   - Type checking in development

3. **Regeneration**: To regenerate specs after protobuf changes:
   ```bash
   cd tools/proto-explorer
   ./generate-specs.sh
   ```

4. **Oneof Constraints**: The specs include oneof validation as `:and` constraints that ensure exactly one field is set.

5. **Optional Fields**: All protobuf3 fields are optional by default and marked with `{:optional true}` in the specs.

## Integration Example

```clojure
(ns potatoclient.transit.commands
  (:require [potatoclient.specs.cmd :as cmd-specs]
            [malli.core :as m]))

(defn validate-command
  "Validate command before sending"
  [command]
  (when-not (m/validate [:ref :cmd/Root] command cmd-specs/schema)
    (throw (ex-info "Invalid command"
                    {:command command
                     :errors (m/explain [:ref :cmd/Root] command cmd-specs/schema)}))))

(defn build-rotary-command
  "Build and validate rotary command"
  [azimuth elevation]
  (let [cmd {:rotary {:axis {:azimuth {:set_value {:value azimuth}}
                             :elevation {:set_value {:value elevation}}}}}]
    (validate-command cmd)
    cmd))
```

## Spec Structure

Each message spec follows this pattern:

```clojure
[:map {:closed true}
 [:field-name {:optional true} [:maybe field-type]]
 ...]
```

With oneof constraints:
```clojure
[:and
 [:map ...]
 [:and [:fn {:error/message "Exactly one of [...] must be set"} predicate]]]
```

## Available Message Types

### Command Messages (cmd.*)
- 193 message types including:
  - `:cmd/Root` - Root command container
  - `:cmd/RotaryPlatform/*` - Rotary platform commands
  - `:cmd/DayCamera/*` - Day camera commands
  - `:cmd/HeatCamera/*` - Heat camera commands
  - `:cmd/Lrf/*` - Laser rangefinder commands
  - etc.

### State Messages (ser.*)
- 17 message types including:
  - `:ser/JonGUIState` - Main GUI state container
  - `:ser/JonGuiDataCameraDay` - Day camera state
  - `:ser/JonGuiDataCameraHeat` - Heat camera state
  - `:ser/JonGuiDataRotary` - Rotary platform state
  - etc.