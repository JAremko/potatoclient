# Protobuf Isolation Implementation

## Overview

This document describes the completed implementation of protobuf isolation in PotatoClient. The goal was to isolate protobuf usage to improve testability while maintaining wire protocol compatibility.

## Architecture

### Layer Separation

```
┌─────────────────────────────────────────┐
│         Application Code                │
│  (Works with EDN structures only)       │
└─────────────────────┬───────────────────┘
                      │
┌─────────────────────▼───────────────────┐
│         State Management                │
│  (Pure EDN atoms, schemas, dispatch)    │
└─────────────────────┬───────────────────┘
                      │
┌─────────────────────▼───────────────────┐
│       Proto Bridge Layer                │
│  (Isolated conversion logic)            │
└─────────────────────┬───────────────────┘
                      │
┌─────────────────────▼───────────────────┐
│       Wire Protocol (IPC)               │
│  (Protobuf binary messages)             │
└─────────────────────────────────────────┘
```

## Key Components

### 1. EDN State Structures (`potatoclient.state.edn`)

Defines pure Clojure data structures with Malli schemas and Guardrails:

```clojure
;; State schemas imported from existing definitions
(def gui-state-schema schemas/jon-gui-state-schema)
(def system-state-schema schemas/system-schema)
;; ... other schemas

;; Subsystem key validation
(def subsystem-key
  [:enum :system :lrf :time :gps :compass :rotary :camera-day :camera-heat
   :compass-calibration :rec-osd :day-cam-glass-heater :actual-space-time
   :meteo-internal])
```

Key functions with Guardrails:
- `create-empty-state` - Creates minimal state
- `validate-state` - Validates against schema
- `subsystem-changed?` - Checks for changes
- `update-subsystem` - Updates state

### 2. Proto Bridge (`potatoclient.state.proto-bridge`)

Isolates all protobuf conversion logic:

```clojure
(>defn binary->edn-state
  "Convert binary protobuf message to EDN state map."
  [binary-data]
  [bytes? => (? map?)]
  ;; Implementation details...)

;; Note: edn-state->binary is not implemented for production
;; as state only flows from server to client
```

Key functions:
- `binary->edn-state` - Converts wire format to EDN
- `proto-msg->edn-state` - Converts protobuf object to EDN
- `extract-subsystem-edn` - Extracts specific subsystem
- `has-subsystem?` - Checks subsystem presence
- `parse-gui-state` - Parses binary to protobuf

### 3. Updated State Dispatch (`potatoclient.state.dispatch`)

Now uses EDN shadow state instead of protobuf:

```clojure
;; Shadow state - EDN map for efficient comparison
(defonce ^:private shadow-state (atom {}))

(>defn- subsystem-changed?
  "Check if a subsystem has changed using EDN equality."
  [old-data new-data]
  [(? any?) (? any?) => boolean?]
  (not= old-data new-data))
```

Key changes:
- Removed protobuf shadow state builder
- Uses EDN equality for change detection
- Preserves atom references when data unchanged

### 4. Test Infrastructure

#### EDN Test Utilities (`potatoclient.test-utils.edn`)
- Create test states without protobuf dependencies
- State comparison helpers
- Validation utilities

#### Proto Test Helper (`potatoclient.state.proto-test-helper`)
- For tests only - creates protobuf from EDN
- Simplified implementation for common test scenarios

#### Atom Watch Tests (`potatoclient.state.atom-watch-test`)
- Verifies watches don't trigger on unchanged data
- Tests reference preservation
- Validates dispatch behavior

## Benefits Achieved

### 1. Improved Testability
- Tests use pure EDN structures
- No protobuf classes needed in most tests
- Easier to create test data

### 2. Better Separation of Concerns
- Protobuf isolated to bridge layer
- Application logic works with EDN only
- Clear boundaries between layers

### 3. Enhanced Performance
- Unchanged subsystems preserve atom references
- Watches don't trigger unnecessarily
- EDN equality is efficient

### 4. Developer Experience
- REPL-friendly EDN structures
- Better error messages with Malli
- Guardrails validation in development

## Implementation Notes

### Change Detection
The system now uses EDN equality for change detection:
1. Incoming protobuf is converted to EDN
2. Compared with shadow state (EDN)
3. Only changed subsystems update atoms
4. Identical references preserved for unchanged data

### Testing Strategy
- Unit tests work with EDN structures
- Integration tests use proto-test-helper
- Atom watch tests verify efficiency

### Production Considerations
- EDN→protobuf conversion not needed (client receives only)
- All protobuf handling isolated to bridge
- Zero overhead in release builds (Guardrails compiled out)

## Migration Guide

For code using the old system:
1. Replace protobuf imports with EDN imports
2. Use EDN test utilities instead of protobuf builders
3. State management code remains largely unchanged

## Future Improvements

1. Generate EDN schemas from proto definitions
2. Property-based testing with generators
3. Performance benchmarking suite
4. Schema evolution strategy