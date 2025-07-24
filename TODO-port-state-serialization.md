# TODO: Port State Serialization/Deserialization

**STATUS: ✅ COMPLETE** - All core functionality has been implemented and tested.

This document outlines the tasks needed to port the TypeScript/JavaScript state management patterns from `deviceStateDispatch.ts` to Clojure.

## Overview

The existing TypeScript implementation uses:
- Singleton pattern with BroadcastChannel for state distribution
- Reactive signals for each subsystem (system, lrf, gps, compass, etc.)
- Change detection to avoid unnecessary updates
- Binary protobuf deserialization of `JonGUIState` messages

**Main Reference File**: `examples/frontend/ts/statePub/deviceStateDispatch.ts`

**Note**: This client uses WebSocket Secure (WSS) exclusively. There is no HTTP/HTTPS protocol switching or transport mode selection.

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

### 1. Core Infrastructure (High Priority) ✅

#### Create State Dispatch System ✅
- [x] Create `potatoclient.state.dispatch` namespace
- [x] Implement singleton pattern using defonce (similar to `deviceStateDispatch.ts:108-113`)
- [x] Set up core.async channels for state broadcasting (like BroadcastChannel at `deviceStateDispatch.ts:29`)

#### Set Up Distribution Channels ✅
- [x] Main state channel for raw `JonGUIState` messages (see `deviceStateDispatch.ts:89-90`)
- [x] Subsystem-specific channels for filtered updates

#### Implement Deserialization Handler ✅
- [x] Message handler that receives binary data (see `handleDeviceStateMessage` at `deviceStateDispatch.ts:185-193`)
- [x] Use existing `proto/deserialize-state` function (TypeScript uses `JonGUIState.decode` at line 188)
- [x] Error handling for malformed messages
- [x] Logging of deserialization errors

### 2. State Management (Medium Priority) ✅

#### Create Reactive Atoms ✅
- [x] Define atoms for each subsystem in `potatoclient.state.device` (see signals at `deviceStateDispatch.ts:36-47`):
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
  - `meteo-internal-state` (additional)

#### Implement Change Detection ✅
- [x] Create `compare-and-update!` function (see `compareAndUpdateSignal` at `deviceStateDispatch.ts:210-218`)
- [x] Use EDN comparison (more idiomatic for Clojure than JSON.stringify used in TypeScript)
- [x] Only update atoms when values actually change
- [x] Log state changes when in debug mode

#### Create State Accessors ✅
- [x] Getter functions for each state component (see getters at `deviceStateDispatch.ts:354-401`)
- [x] Nil-safe access with default values
- [x] Convenience functions for common access patterns

#### Add Malli Validation ✅
- [x] Define schemas for all state data types by converting buf.validate specs from proto files
- [x] GPS validation (latitude: -90 to 90, longitude: -180 to 180) - see `proto/jon_shared_data_gps.proto:14-17, 26-29`
- [x] Altitude validation (-433 to 8848.86 meters) - see `proto/jon_shared_data_gps.proto:18-21, 30-33`
- [x] Compass degrees validation (0 to 360) - check `proto/jon_shared_data_compass.proto`
- [x] Fix type enum validation - see `proto/jon_shared_data_gps.proto:34-38`
- [x] Convert all `(buf.validate.field)` constraints from proto files to Malli schemas
- [x] Use these Malli schemas to validate EDN data after deserialization

#### State Logging ✅
- [x] Configurable verbosity levels (via enable-debug!)
- [x] State change history (via debug mode logging)
- [x] Debug mode for tracking all updates

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


## Frame Data Integration

TypeScript version provides methods to get frame data:
- `getDayFrameData()` - Returns day camera frame data
- `getHeatFrameData()` - Returns heat camera frame data

Frame data includes:
- `timestamp` - BigInt nanosecond precision
- `duration` - BigInt frame duration

## Utility Functions Needed ✅

Port these TypeScript utilities (found at `deviceStateDispatch.ts:239-351`):
- [x] `formatTimestamp` - Format nanosecond timestamps to MM:SS.mmm (lines 247-273)
- [x] `formatDuration` - Format durations (e.g., "33.3ms") (lines 280-298)
- [x] `getTimeDifferenceMs` - Calculate time difference (lines 306-316)
- [x] `isWithinSafeIntegerRange` - Check timestamp safety (lines 323-331)
- [x] `timestampToNumber` - Safe bigint to number conversion (lines 338-351)

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

### Roundtrip Testing Script ✅

Create a comprehensive test script that:
1. [x] **Generates test data** from Malli schemas using `malli.generator`
2. [x] **Serializes to protobuf** using existing serialization functions
3. [x] **Deserializes back to EDN** using `proto/deserialize-state`
4. [x] **Validates the result** against Malli schemas
5. [x] **Compares with original** to ensure data integrity

**Note**: Full roundtrip testing implemented in `test/potatoclient/state/dispatch_test.clj` and `test/potatoclient/state/validation_test.clj`

## Implementation Summary

### Completed Components:

1. **State Dispatch System** (`potatoclient.state.dispatch`)
   - Singleton pattern implementation
   - Binary protobuf message handling
   - Change detection to prevent unnecessary updates
   - State validation with Malli schemas
   - Debug mode for development
   - Core.async channel for state distribution

2. **State Management** (`potatoclient.state.device`)
   - Reactive atoms for all 13 subsystems
   - Nil-safe accessor functions
   - Convenience functions (get-current-position, cameras-ready?, etc.)
   - Watch/unwatch support for reactive UI

3. **Malli Schemas** (`potatoclient.state.schemas`)
   - Complete validation schemas for all subsystems
   - Converted from buf.validate protobuf constraints
   - Common constraints (angles, GPS coords, temperatures, etc.)
   - All enum validations

4. **Utility Functions** (`potatoclient.state.utils`)
   - Timestamp and duration formatting
   - Coordinate formatting with hemispheres
   - Distance, temperature, and angle formatting
   - Frame data and FPS calculations

5. **Testing**
   - Comprehensive validation tests
   - Integration tests for dispatch system
   - Test coverage for all major functionality

### Key Differences from TypeScript Implementation:

- Uses Clojure atoms instead of signals/effects
- EDN comparison for change detection (vs JSON.stringify)
- Malli schemas for validation (vs buf.validate)
- Core.async channels for distribution (vs BroadcastChannel)
- No transport mode switching (WSS only)

### Usage:

```clojure
;; Enable validation and debug in development
(dispatch/enable-validation! true)
(dispatch/enable-debug! true)

;; Handle incoming state messages
(dispatch/handle-binary-state binary-data)

;; Access current state
(device/get-gps)
(device/get-heading)
(device/cameras-ready?)

;; Watch for changes
(device/watch-state :gps 
  (fn [old new]
    (println "GPS updated:" new)))

;; Listen to state channel
(go-loop []
  (when-let [state (<! (dispatch/get-state-channel))]
    (println "New state received")
    (recur)))
```

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
