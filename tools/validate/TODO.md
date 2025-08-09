# Validate Tool - Malli Spec Creation and Property-Based Testing TODO

## Project Goal
Create comprehensive Malli specs for State and Cmd proto messages with property-based testing against buf.validate constraints. These specs will validate Pronto maps (EDN representations) and ensure compatibility with protobuf validation constraints.

## Critical Design Decision: Closed Map Specs
**ALL MAP SPECS MUST BE CLOSED** - Use `:map` with `{:closed true}` to catch typos and invalid keys. This ensures:
- Field name typos are caught immediately
- No extra unexpected fields are silently accepted
- Strict validation matching protobuf schema exactly
- Better error messages when keys don't match expected schema

Example:
```clojure
[:map {:closed true}  ; ALWAYS use closed maps
 [:latitude double?]
 [:longitude double?]]
```

## Architecture Overview
```
validate/
├── src/
│   └── validate/
│       ├── specs/          # Malli specs for proto messages
│       │   ├── state/      # State message specs (hierarchical)
│       │   ├── cmd/        # Command message specs (hierarchical)
│       │   └── shared.clj  # Symlink to shared/src/potatoclient/specs/
│       ├── generators/     # Custom generators for complex types
│       ├── buff/           # Buff validate integration utilities
│       └── property/       # Property-based testing infrastructure
└── test/
    └── validate/
        ├── specs/          # Spec validation tests
        ├── property/       # Property-based tests against buff.validate
        └── generators/     # Generator tests
```

## Phase 1: Foundation Setup ✅
- [x] Research Malli documentation for generators and property-based testing
- [x] Explore existing shared specs structure and understand patterns
- [ ] Set up symlinks from shared specs to validate tool classpath
- [ ] Create directory structure for specs, generators, and tests
- [ ] Initialize global Malli registry with oneof-pronto schema
  ```clojure
  ;; Required initialization (see cmd-explorer example)
  (registry/setup-global-registry!
    {:oneof-pronto (oneof/register-oneof-pronto-schema!)})
  ```

## Phase 2: Proto Discovery and Analysis ✅
- [x] Use proto-class-explorer agent to document State proto structure
  - [x] Get complete field hierarchy for JonSharedData$JonGUIState
  - [x] Document all buf.validate constraints per field
  - [x] Map Java class names to proto message names
  - [x] Identify all nested message types
- [x] Use proto-class-explorer agent to document Cmd proto structure
  - [x] Get complete field hierarchy for JonSharedCmd$Root
  - [x] Document all buf.validate constraints per field
  - [x] Map Java class names to proto message names
  - [x] Identify all nested message types
- [x] Create reference document with all constraints and field types

### Discovered Proto Structures

#### State Message (ser.JonSharedData$JonGUIState)
- **protocol_version**: uint32 > 0 (required)
- **14 nested messages** (all required):
  - system, meteo_internal, lrf, time, gps, compass, rotary
  - camera_day, camera_heat, compass_calibration, rec_osd
  - day_cam_glass_heater, actual_space_time

#### GPS Constraints (Critical)
- **latitude**: double ∈ [-90, 90]
- **longitude**: double ∈ [-180, 180]
- **altitude**: double ∈ [-433, 8848.86]
- **fix_type**: enum, cannot be UNSPECIFIED (0)

#### Command Message (cmd.JonSharedCmd$Root)
- **protocol_version**: uint32 > 0
- **client_type**: enum, cannot be UNSPECIFIED (0)
- **15 command payloads** (oneof structure):
  - cv, day_camera, heat_camera, gps, compass, lrf, lrf_calib
  - rotary, osd, ping, noop, frozen, system, day_cam_glass_heater, lira

#### Rotary Speed Constraints
- **All rotary speeds**: double/float > 0 and ≤ 1
- Applies to: RotateAzimuth, RotateElevation, ScanNode operations

## Phase 3: Shared Base Specs Development 🔧
- [ ] Review and enhance existing common specs with buf.validate constraints:
  - [ ] GPS coordinates with exact buf.validate ranges:
    - Latitude: [-90, 90]
    - Longitude: [-180, 180]
    - Altitude: [-433, 8848.86]
  - [ ] Protocol version: must be > 0
  - [ ] Client type: cannot be UNSPECIFIED
  - [ ] Rotary speed: > 0 and ≤ 1
- [ ] Create additional shared specs:
  - [ ] Timestamp specs with proper ranges
  - [ ] ID specs with validation
  - [ ] Status/mode enums with constraints
- [ ] Add comprehensive generators for all base specs
- [ ] Test generators produce valid values within constraints

## Phase 4: State Message Specs (Bottom-Up) 🏗️
### Level 1: Leaf Message Specs
- [ ] GPS message spec with all fields and constraints
- [ ] System message spec (CPU, GPU, disk space, etc.)
- [ ] Camera Day message spec
- [ ] Camera Heat message spec
- [ ] Rotary message spec with scan node constraints
- [ ] Compass message spec
- [ ] LRF (Laser Range Finder) message spec
- [ ] Target message specs

### Level 2: Composite Message Specs
- [ ] Combine leaf specs into larger message structures
- [ ] Add cross-field validation rules
- [ ] Ensure all required fields are present

### Level 3: Root State Spec
- [ ] Create JonGUIState root spec combining all sub-messages
- [ ] Add protocol version validation
- [ ] Add client type validation
- [ ] Validate complete message structure

## Phase 5: Command Message Specs (Bottom-Up) 🎮
### Level 1: Individual Command Specs
- [ ] Rotary control commands
- [ ] Camera control commands
- [ ] System commands
- [ ] LRF commands
- [ ] Recording commands
- [ ] Configuration commands

### Level 2: Command Groups
- [ ] Group related commands into logical units
- [ ] Add command-specific validation rules
- [ ] Ensure mutual exclusivity where needed

### Level 3: Root Command Spec
- [ ] Create JonSharedCmd$Root spec with oneof structure
- [ ] Validate only one command type is present
- [ ] Add command-specific constraints

## Phase 6: Buff Validate Integration 🔌
- [ ] Create utility functions for buff validation:
  ```clojure
  (defn validate-with-buff
    "Validate generated data using buff.validate"
    [proto-type data])
  
  (defn buff-constraints->malli
    "Convert buff.validate constraints to Malli specs"
    [constraint-map])
  ```
- [ ] Create conversion functions:
  - [ ] Pronto map → protobuf binary
  - [ ] Malli spec → buff.validate constraint checker
- [ ] Set up validation pipeline:
  - [ ] Generate data from Malli spec
  - [ ] Convert to protobuf
  - [ ] Validate with buff.validate
  - [ ] Report any mismatches

## Phase 7: Property-Based Testing 🧪
### State Message Testing
- [ ] Generate 1000+ random valid State messages
- [ ] Validate each with buff.validate
- [ ] Track any validation failures
- [ ] Refine specs based on failures
- [ ] Achieve 100% pass rate

### Command Message Testing
- [ ] Generate 1000+ random valid Command messages
- [ ] Validate each with buff.validate
- [ ] Test all command types
- [ ] Verify oneof constraints
- [ ] Achieve 100% pass rate

### Edge Case Testing
- [ ] Test boundary values for all numeric fields
- [ ] Test empty/minimal messages
- [ ] Test maximum size messages
- [ ] Test invalid enum values
- [ ] Test missing required fields

## Phase 8: Integration and Documentation 📚
- [ ] Update Makefile with new targets:
  - `make spec-test` - Run all spec tests
  - `make generate-samples` - Generate sample messages
  - `make validate-specs` - Validate specs against buff
  - `make property-test` - Run property-based tests
- [ ] Create comprehensive documentation:
  - [ ] Spec usage guide
  - [ ] Generator documentation
  - [ ] Property testing guide
  - [ ] Buff validate integration guide
- [ ] Add examples for common use cases
- [ ] Create troubleshooting guide

## Phase 9: Performance Optimization ⚡
- [ ] Profile spec validation performance
- [ ] Optimize generator performance
- [ ] Cache compiled validators
- [ ] Benchmark against raw buff.validate
- [ ] Document performance characteristics

## Success Criteria ✅
1. **Complete Coverage**: All proto message fields have corresponding Malli specs
2. **Constraint Compatibility**: 100% of buff.validate constraints are enforced in Malli specs
3. **Generator Quality**: Generated data passes buff.validate 100% of the time
4. **Performance**: Spec validation < 5ms per message
5. **Documentation**: Complete usage and integration guides
6. **Testing**: 1000+ successful property-based test runs per message type

## Technical Notes 📋

### Global Registry Setup (CRITICAL)
The project uses a GLOBAL default Malli registry (not local registries). All specs must be registered globally:
```clojure
(ns validate.core
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-pronto :as oneof]
   [potatoclient.specs.common])) ; Auto-registers on load

;; Initialize ONCE at application startup
(defn initialize!
  []
  (registry/setup-global-registry!
    {:oneof-pronto (oneof/register-oneof-pronto-schema!)}))
```

### Oneof-Pronto Schema
Custom Malli schema for protobuf oneofs in Pronto proto-maps:
- Location: `/shared/src/potatoclient/specs/oneof_pronto.clj`
- Purpose: Validates that exactly one field is set in a oneof group
- Required for Command message specs (uses oneof for command type)
- Example usage in cmd_root.clj for JonCommand root

### Malli Generator Tips (2025)
- Use `:gen/gen` property for custom generators
- Use `:gen/fmap` for transformations
- Use `:gen/elements` for specific value sets
- Control size with `:gen/min` and `:gen/max`
- Combine generators with `gen/bind` for dependent fields

### Buff Validate Constraints to Implement
- `double.gte_lte`: Range constraints for doubles
- `uint32.gt`: Greater than for unsigned integers
- `enum.defined_only`: Only defined enum values
- `double.gt_lte`: Greater than with upper bound
- Required field validation
- Oneof field validation

### State Explorer Output Reference
- Location: `/tools/state-explorer/output/*.edn`
- Contains real captured state messages
- Use for validation and testing
- Reference for field structure and values

### Proto-Class Explorer Usage
```bash
# Search for messages
make search QUERY=root

# Get detailed info
make info QUERY='cmd.JonSharedCmd$Root'
```

## Current Status: Phase 1 - Foundation Setup
Next Step: Complete directory structure setup and symlinks