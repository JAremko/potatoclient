# Shared Protobuf Specs

This directory contains automatically generated Malli specifications for protobuf messages used in PotatoClient.

## Generated Files

The specs are organized by protobuf package:

- `protobuf/cmd-specs.clj` - Root command specs (cmd package)
- `protobuf/cmd.*.clj` - Sub-package command specs (e.g., cmd.RotaryPlatform, cmd.HeatCamera)
- `protobuf/ser-specs.clj` - State/serialization specs (ser package)

## Usage in Main Application

### 1. Add Shared Specs to Classpath

The specs are already in the shared location. The main application includes them automatically.

### 2. Using Command Specs

```clojure
(require '[potatoclient.specs.cmd :as cmd]
         '[malli.core :as m]
         '[malli.generator :as mg])

;; The specs are defined as vars in the namespace
cmd/Root    ; => [:map [:protocol-version [:maybe :int]] ...]
cmd/Ping    ; => [:map]

;; Validate a command
(m/validate cmd/Root {:protocol-version 1 :payload {:ping {:ping {}}}})

;; Generate example data
(mg/generate cmd/Ping)
```

### 3. Using State Specs

```clojure
(require '[potatoclient.specs.ser :as ser])

;; Validate state message
(m/validate ser/JonGUIState state-data)

;; Access specific state specs
ser/JonGuiDataRotary       ; Rotary platform state
ser/JonGuiDataCameraHeat   ; Heat camera state
ser/JonGuiDataCameraDay    ; Day camera state
```

### 4. Using Sub-package Specs

```clojure
(require '[potatoclient.specs.cmd.RotaryPlatform :as rotary])

;; Access rotary-specific commands
rotary/RotateToNDC         ; => [:map [:channel [:maybe :string]] ...]
rotary/SetVelocity         ; => [:map [:azimuth [:maybe :float]] ...]

;; Validate specific command
(m/validate rotary/RotateToNDC {:channel "heat" :x 0.5 :y -0.5})
```

### 5. Generating Test Data

```clojure
(require '[malli.generator :as mg])

;; Generate a valid command
(mg/generate cmd/Root)

;; Generate with specific size/seed
(mg/generate rotary/SetVelocity {:size 10 :seed 42})
```

### 6. In Guardrails Instrumentation

```clojure
(ns potatoclient.instrumentation
  (:require [potatoclient.specs.cmd :as cmd]
            [potatoclient.specs.ser :as ser]
            [malli.core :as m]))

;; Use in function specs
(m/=> process-command [:=> [:cat cmd/Root] ::response])
(m/=> update-state [:=> [:cat ser/JonGUIState] nil?])
```

### 7. Property-Based Testing

```clojure
(require '[clojure.test.check.properties :as prop]
         '[clojure.test.check.generators :as test.gen]
         '[malli.generator :as mg])

(defspec all-generated-commands-are-valid 100
  (prop/for-all [cmd (mg/generate cmd/Root)]
    (valid-transit-encoding? cmd)))
```

## Important Notes

1. **Auto-Generated**: These specs are automatically generated from protobuf JSON descriptors. Do not edit manually.

2. **Complete Coverage**: The specs include:
   - All message structures with proper field naming (kebab-case)
   - Enum definitions with all valid values
   - Oneof constraints (using custom `:oneof` schema)
   - Proper type mappings (protobuf → Malli)
   - Optional field handling (all fields wrapped with `:maybe`)

3. **Regeneration**: To regenerate specs after protobuf changes:
   ```bash
   cd tools/proto-explorer
   make proto              # Generate new proto files and JSON descriptors
   make generate-specs     # Generate Malli specs from JSON descriptors
   ```

4. **Spec Organization**: 
   - Each protobuf package gets its own namespace
   - Sub-packages become sub-namespaces (e.g., `cmd.RotaryPlatform`)
   - All specs are defined as `def`s for easy access

5. **Field Naming**: All field names are automatically converted from snake_case to kebab-case for Clojure idioms.

## Integration Example

```clojure
(ns potatoclient.transit.commands
  (:require [potatoclient.specs.cmd :as cmd]
            [potatoclient.specs.cmd.RotaryPlatform :as rotary]
            [malli.core :as m]))

(defn validate-command
  "Validate command before sending"
  [command]
  (when-not (m/validate cmd/Root command)
    (throw (ex-info "Invalid command"
                    {:command command
                     :errors (m/explain cmd/Root command)}))))

(defn build-rotary-command
  "Build and validate rotary command"
  [azimuth elevation]
  (let [cmd {:protocol-version 1
             :payload {:rotary 
                      {:rotary {:payload 
                               {:axis {:axis {:azimuth {:cmd {:set-value 
                                                             {:set-value {:value azimuth}}}}
                                             :elevation {:cmd {:set-value 
                                                             {:set-value {:value elevation}}}}}}}}}}}]
    (validate-command cmd)
    cmd))
```

## Spec Structure

Each message spec follows this pattern:

```clojure
[:map
 [:field-name [:maybe field-type]]
 ...]
```

With oneof constraints:
```clojure
[:map
 [:regular-field [:maybe :type]]
 [:payload [:oneof {:option1 [:map [:option1 spec1]]
                   :option2 [:map [:option2 spec2]]}]]]
```

## Available Message Types

### Command Messages (cmd.*)
The command tree is organized hierarchically:

- **Root Package (`cmd`)**:
  - `Root` - Root command container with oneof payload
  - `Ping`, `Noop`, `Frozen` - Basic commands

- **Sub-packages**:
  - `cmd.RotaryPlatform` - 39 rotary platform commands
  - `cmd.HeatCamera` - 37 heat camera commands  
  - `cmd.DayCamera` - 30 day camera commands
  - `cmd.System` - 14 system commands
  - `cmd.Lrf` - 16 laser rangefinder commands
  - `cmd.Compass` - 13 compass commands
  - `cmd.CV` - 10 computer vision commands
  - `cmd.OSD` - 9 on-screen display commands
  - And more...

### State Messages (ser.*)
- **35 message types** including:
  - `JonGUIState` - Main GUI state container
  - `JonGuiDataRotary` - Rotary platform state
  - `JonGuiDataCameraHeat` - Heat camera state
  - `JonGuiDataCameraDay` - Day camera state
  - `JonGuiDataSystem` - System state
  - Various enum types for configuration values

## Generation Details

The specs are generated from protobuf JSON descriptors using:
1. Buf to generate FileDescriptorSets as JSON
2. Proto Explorer to convert JSON → EDN → Malli specs
3. Automatic kebab-case conversion for Clojure idioms
4. Oneof handling with custom `:oneof` Malli schema