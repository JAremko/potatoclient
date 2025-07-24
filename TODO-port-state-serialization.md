# TODO: Port State Serialization/Deserialization

This document outlines the tasks needed to port the TypeScript/JavaScript state management patterns from `deviceStateDispatch.ts` to Clojure.

## Overview

The existing TypeScript implementation uses:
- Singleton pattern with BroadcastChannel for state distribution
- Reactive signals for each subsystem (system, lrf, gps, compass, etc.)
- Change detection to avoid unnecessary updates
- Binary protobuf deserialization of `JonGUIState` messages

**Main Reference File**: `examples/frontend/ts/statePub/deviceStateDispatch.ts`

## State Structure

### Main State Message
- `JonGUIState` - Root message containing all subsystems
- Protocol version field for compatibility
- All subsystem fields are required

### Subsystem Data Messages
Each subsystem has its own data message type (defined in `proto/jon_shared_data_*.proto` files):
- `JonGuiDataSystem` - System state
- `JonGuiDataLrf` - Laser range finder data
- `JonGuiDataGps` - GPS coordinates with validation (lat: -90 to 90, lon: -180 to 180)
- `JonGuiDataCompass` - Compass heading
- `JonGuiDataRotary` - Rotary platform position
- `JonGuiDataCameraDay` - Day camera settings
- `JonGuiDataCameraHeat` - Heat camera settings
- `JonGuiDataCompassCalibration` - Compass calibration data
- `JonGuiDataTime` - Time information
- `JonGuiDataRecOsd` - Recording/OSD data
- `JonGuiDataDayCamGlassHeater` - Glass heater status
- `JonGuiDataActualSpaceTime` - Actual space-time data

**Proto files location**: `proto/jon_shared_data_*.proto`

## Existing Clojure Infrastructure

Already implemented:
- `potatoclient.proto/deserialize-state` - Deserializes `JonGUIState` from binary
- `potatoclient.state.streams` - Has atoms for state management
- Process management with atoms

## Proto Validation Note

The proto files use `buf.validate` annotations to specify constraints on fields. These need to be converted to Malli schemas for EDN validation in Clojure. For example:
- `(buf.validate.field).double = { gte: -90.0, lte: 90.0 }` → `[:double {:min -90.0 :max 90.0}]`
- `(buf.validate.field).enum = { defined_only: true, not_in: [0] }` → Custom Malli predicate
- `(buf.validate.field).required = true` → Non-nilable schema

## Implementation Tasks

### 1. Core Infrastructure (High Priority)

#### Create State Dispatch System
- [ ] Create `potatoclient.state.dispatch` namespace
- [ ] Implement singleton pattern using defonce (similar to `deviceStateDispatch.ts:108-113`)
- [ ] Set up core.async channels for state broadcasting (like BroadcastChannel at `deviceStateDispatch.ts:29`)
- [ ] Handle transport mode switching (WebSocket vs WebTransport suffixes) - see `deviceStateDispatch.ts:53-72`

#### Set Up Distribution Channels
- [ ] Main state channel for raw `JonGUIState` messages (see `deviceStateDispatch.ts:89-90`)
- [ ] Subsystem-specific channels for filtered updates
- [ ] Channel naming with transport suffix support (_ws, _wt) - see `getDeviceStateChannelName()` at `deviceStateDispatch.ts:53-72`

#### Implement Deserialization Handler
- [ ] Message handler that receives binary data (see `handleDeviceStateMessage` at `deviceStateDispatch.ts:185-193`)
- [ ] Use existing `proto/deserialize-state` function (TypeScript uses `JonGUIState.decode` at line 188)
- [ ] Error handling for malformed messages
- [ ] Logging of deserialization errors

### 2. State Management (Medium Priority)

#### Create Reactive Atoms
- [ ] Define atoms for each subsystem in `potatoclient.state.device` (see signals at `deviceStateDispatch.ts:36-47`):
  - `system-state`
  - `lrf-state`
  - `gps-state`
  - `compass-state`
  - `rotary-state`
  - `camera-day-state`
  - `camera-heat-state`
  - `compass-calibration-state`
  - `time-state`
  - `rec-osd-state`
  - `day-cam-glass-heater-state`
  - `actual-space-time-state`

#### Implement Change Detection
- [ ] Create `compare-and-update!` function (see `compareAndUpdateSignal` at `deviceStateDispatch.ts:210-218`)
- [ ] Use EDN comparison (more idiomatic for Clojure than JSON.stringify used in TypeScript)
- [ ] Only update atoms when values actually change
- [ ] Log state changes when in debug mode

#### Create State Accessors
- [ ] Getter functions for each state component (see getters at `deviceStateDispatch.ts:354-401`)
- [ ] Nil-safe access with default values
- [ ] Convenience functions for common access patterns

#### Add Malli Validation
- [ ] Define schemas for all state data types by converting buf.validate specs from proto files
- [ ] GPS validation (latitude: -90 to 90, longitude: -180 to 180) - see `proto/jon_shared_data_gps.proto:14-17, 26-29`
- [ ] Altitude validation (-433 to 8848.86 meters) - see `proto/jon_shared_data_gps.proto:18-21, 30-33`
- [ ] Compass degrees validation (0 to 360) - check `proto/jon_shared_data_compass.proto`
- [ ] Fix type enum validation - see `proto/jon_shared_data_gps.proto:34-38`
- [ ] Convert all `(buf.validate.field)` constraints from proto files to Malli schemas
- [ ] Use these Malli schemas to validate EDN data after deserialization

### 3. Integration (Low Priority)

#### UI Bindings
- [ ] Create Seesaw bindings for state atoms
- [ ] Auto-update UI components on state changes
- [ ] Debounced updates for rapidly changing values

#### Frame Correlation
- [ ] Link video frames with device state (see frame data methods at `deviceStateDispatch.ts:159-179`)
- [ ] Store frame timestamps with state snapshots (FrameData interface at `deviceStateDispatch.ts:20-25`)
- [ ] Provide frame-state lookup functions

#### State Logging
- [ ] Configurable verbosity levels
- [ ] State change history
- [ ] Debug mode for tracking all updates

## Code Structure

```clojure
;; potatoclient.state.dispatch
(ns potatoclient.state.dispatch
  (:require [clojure.core.async :as async]
            [potatoclient.proto :as proto]
            [potatoclient.state.device :as device-state]
            [potatoclient.logging :as logging]))

(defonce ^:private instance (atom nil))

(defprotocol IStateDispatch
  (handle-state-message [this binary-data])
  (switch-transport-mode [this mode])
  (get-channel-name [this])
  (dispose [this]))

;; potatoclient.state.device
(ns potatoclient.state.device
  (:require [potatoclient.specs :as specs]
            [malli.core :as m]))

(defonce system-state (atom nil))
(defonce lrf-state (atom nil))
(defonce gps-state (atom nil))
;; ... etc

;; Example Malli schema converted from buf.validate
(def gps-schema
  [:map
   [:longitude [:double {:min -180.0 :max 180.0}]]
   [:latitude [:double {:min -90.0 :max 90.0}]]
   [:altitude [:double {:min -433.0 :max 8848.86}]]
   [:manual-longitude [:double {:min -180.0 :max 180.0}]]
   [:manual-latitude [:double {:min -90.0 :max 90.0}]]
   [:manual-altitude [:double {:min -433.0 :max 8848.86}]]
   [:fix-type [:enum :no-fix :2d-fix :3d-fix :dgps-fix :rtk-fix]]
   [:use-manual :boolean]])

(defn validate-state-data
  "Validates EDN data against Malli schema after deserialization"
  [schema data]
  (when data
    (if (m/validate schema data)
      data
      (do
        (logging/log-warn "State validation failed" {:errors (m/explain schema data)})
        nil))))
```

## Transport Mode Support

The system needs to support different transport modes:
- `tcp` - WebSocket transport (suffix: _ws)
- `udp` - WebTransport (suffix: _wt)
- `auto` - Automatic selection based on mode

Channel names follow pattern: `deviceState_{suffix}`

## Frame Data Integration

TypeScript version provides methods to get frame data:
- `getDayFrameData()` - Returns day camera frame data
- `getHeatFrameData()` - Returns heat camera frame data

Frame data includes:
- `timestamp` - BigInt nanosecond precision
- `duration` - BigInt frame duration

## Utility Functions Needed

Port these TypeScript utilities (found at `deviceStateDispatch.ts:239-351`):
- `formatTimestamp` - Format nanosecond timestamps to MM:SS.mmm (lines 247-273)
- `formatDuration` - Format durations (e.g., "33.3ms") (lines 280-298)
- `getTimeDifferenceMs` - Calculate time difference (lines 306-316)
- `isWithinSafeIntegerRange` - Check timestamp safety (lines 323-331)
- `timestampToNumber` - Safe bigint to number conversion (lines 338-351)

## Development Workflow & Quality Checks

### When to Run Linters and Build

1. **After Creating Each New Namespace**:
   - Run `make lint` to catch issues early
   - Run `make filter-lint-reports` to remove false positives
   - Run `make build` to ensure compilation

2. **After Implementing Major Components**:
   - State dispatch system → lint & build
   - State atoms and schemas → lint & build
   - Change detection logic → lint & build
   - Test scripts → lint & build

3. **Before Committing**:
   - Full lint check: `make lint`
   - Filter false positives: `make filter-lint-reports`
   - Verify build: `make build`
   - Run tests: `make test` (if tests exist)

### Lint Checkpoints

```bash
# After creating state.dispatch namespace
make lint
make filter-lint-reports  # Removes ~56% false positives
make build

# After adding Malli schemas
make lint
# Check for reflection warnings in new code
# Ensure all functions use >defn/>defn-

# After test implementation
make lint
make build
# Run the roundtrip tests

# Final check before marking complete
make lint-summary  # Quick overview
make run          # Test in near-production mode
```

### Common Lint Issues to Fix

1. **Reflection warnings**: Add type hints for Java interop
2. **Missing specs**: Use `>defn` instead of `defn`
3. **Unused namespaces**: Remove unnecessary requires
4. **Missing docstrings**: Add for public functions

## Testing Considerations

### Roundtrip Testing Script

Create a comprehensive test script that:
1. **Generates test data** from Malli schemas using `malli.generator`
2. **Serializes to protobuf** using existing serialization functions
3. **Deserializes back to EDN** using `proto/deserialize-state`
4. **Validates the result** against Malli schemas
5. **Compares with original** to ensure data integrity

```clojure
(ns potatoclient.test.state-roundtrip
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [potatoclient.proto :as proto]
            [potatoclient.state.device :as device]))

(defn test-roundtrip
  "Test serialize -> deserialize roundtrip for state data"
  [schema data-key]
  (let [;; Generate test data from schema
        test-data (mg/generate schema)
        
        ;; Create full state with test data
        full-state (assoc empty-state data-key test-data)
        
        ;; Serialize to protobuf binary
        serialized (proto/serialize-state full-state)
        
        ;; Deserialize back to EDN
        deserialized (proto/deserialize-state serialized)
        
        ;; Extract the specific subsystem data
        result-data (get deserialized data-key)]
    
    ;; Validate against schema
    (assert (m/validate schema result-data)
            (str "Validation failed: " (m/explain schema result-data)))
    
    ;; Check data integrity
    (assert (= test-data result-data)
            (str "Data mismatch: " test-data " != " result-data))
    
    {:original test-data
     :serialized-size (count serialized)
     :roundtrip-success true}))

;; Run tests for all subsystems
(defn run-all-roundtrip-tests []
  (doseq [[schema-key schema] device/all-schemas]
    (println "Testing" schema-key "roundtrip...")
    (dotimes [n 100] ; Test with 100 random values
      (test-roundtrip schema schema-key))))
```

### Other Testing Considerations

- Mock protobuf messages for unit tests
- Test change detection logic
- Verify state update propagation
- Test transport mode switching
- Validate all state constraints
- Property-based testing with generated data
- Edge cases (nil values, empty maps, extreme values)