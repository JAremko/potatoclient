# Protobuf Isolation Plan

## Overview

This document outlines a plan to isolate protobuf usage in PotatoClient to improve testability and reduce coupling. The goal is to maintain protobuf for wire protocol compatibility while using pure EDN structures internally for state management.

## Current Architecture Problems

1. **Direct Protobuf Dependencies**: State atoms directly store protobuf objects, making testing difficult
2. **Shadow State Complexity**: The dispatch system maintains a mutable protobuf builder for comparison
3. **Testing Challenges**: Tests require protobuf classes and can't easily mock state
4. **Coverage Gaps**: Protobuf serialization/deserialization logic is hard to test in isolation

## Proposed Architecture

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

### Key Components

#### 1. EDN State Structures (`potatoclient.state.edn`)

Define pure Clojure data structures that mirror the protobuf schema:

```clojure
(def gui-state-schema
  [:map
   [:system {:optional true} ::system-state]
   [:day-camera {:optional true} ::day-camera-state]
   [:heat-camera {:optional true} ::heat-camera-state]
   [:gps {:optional true} ::gps-state]
   [:compass {:optional true} ::compass-state]
   [:lrf {:optional true} ::lrf-state]
   [:rotary {:optional true} ::rotary-state]
   [:osd {:optional true} ::osd-state]
   [:cv {:optional true} ::cv-state]
   [:glass-heater {:optional true} ::glass-heater-state]
   [:lrf-alignment {:optional true} ::lrf-alignment-state]])

;; Example subsystem state
(def system-state-schema
  [:map
   [:uptime-seconds {:optional true} int?]
   [:cpu-temperature {:optional true} number?]
   [:gpu-temperature {:optional true} number?]
   [:memory-usage-percent {:optional true} number?]])
```

#### 2. Proto Bridge (`potatoclient.state.proto-bridge`)

Isolate all protobuf conversion logic:

```clojure
(ns potatoclient.state.proto-bridge
  "Isolated protobuf conversion layer"
  (:require [potatoclient.proto :as proto])
  (:import (ser JonSharedData$JonGUIState)))

(defn binary->edn-state
  "Convert binary protobuf message to EDN state map"
  [^bytes binary-data]
  (let [proto-msg (JonSharedData$JonGUIState/parseFrom binary-data)]
    (proto/proto-map->clj-map proto-msg)))

(defn edn-state->binary
  "Convert EDN state map to binary protobuf message"
  [state-map]
  (let [builder (JonSharedData$JonGUIState/newBuilder)]
    (-> builder
        (proto/clj-map->proto-builder state-map)
        .build
        .toByteArray)))

(defn changed?
  "Check if two EDN states are different"
  [old-state new-state]
  (not= old-state new-state))
```

#### 3. Updated State Dispatch (`potatoclient.state.dispatch`)

Remove protobuf shadow state, use pure EDN:

```clojure
;; Replace protobuf shadow state with EDN
(defonce ^:private shadow-state (atom {}))

(defn- update-subsystem-if-changed!
  [subsystem-atom subsystem-data subsystem-key]
  (let [old-data (get @shadow-state subsystem-key)]
    (when (not= old-data subsystem-data)
      (reset! subsystem-atom subsystem-data)
      (swap! shadow-state assoc subsystem-key subsystem-data)
      true)))

;; Main dispatch - now works with EDN
(handle-state-message [_ binary-data]
  (try
    (let [state-map (proto-bridge/binary->edn-state binary-data)]
      ;; Validate against EDN schemas
      (when-let [validation-error (m/explain gui-state-schema state-map)]
        (log/warn "State validation failed" validation-error))
      
      ;; Update subsystems with EDN data
      (doseq [[k v] state-map
              :when (contains? subsystem-atoms k)]
        (update-subsystem-if-changed! (get subsystem-atoms k) v k))
      
      ;; Send to channel subscribers
      (put! @state-channel state-map))
    (catch Exception e
      (log/error "Failed to process state message" e))))
```

## Implementation Steps

### Phase 1: Create EDN Infrastructure
1. Create `potatoclient.state.edn` with EDN schemas matching proto definitions
2. Create `potatoclient.state.proto-bridge` with conversion functions
3. Write comprehensive tests for EDN<->Proto conversion

### Phase 2: Update State Management
1. Modify `state.dispatch` to use EDN shadow state instead of protobuf
2. Update subsystem atoms to store EDN data
3. Ensure all state consumers work with EDN structures

### Phase 3: Testing Infrastructure
1. Create test helpers that work with EDN structures
2. Write property-based tests for state transformations
3. Create mock state generators using Malli schemas

### Phase 4: Migration
1. Update existing code to use EDN structures
2. Move protobuf imports to proto-bridge only
3. Update documentation

## Benefits

### Improved Testability
- Pure data structures are easy to create and compare in tests
- No need for protobuf classes in test code
- Can use standard Clojure test utilities

### Better Separation of Concerns
- Application logic doesn't know about protobuf
- Wire format changes don't affect business logic
- Easier to swap protocols if needed

### Enhanced Developer Experience
- REPL-friendly EDN structures
- Better error messages with Malli validation
- Easier to inspect and debug state

### Coverage Improvements
- Can test state logic without protobuf
- Can test protobuf conversion in isolation
- Can achieve near 100% coverage of state management

## Risks and Mitigations

### Risk: Performance Impact
**Mitigation**: Profile the conversion overhead. The current code already converts to EDN for most operations, so impact should be minimal.

### Risk: Conversion Bugs
**Mitigation**: Comprehensive test suite with property-based testing to ensure conversion fidelity.

### Risk: Schema Drift
**Mitigation**: Generate EDN schemas from proto definitions or maintain strict documentation.

## Success Metrics

1. **Test Coverage**: Achieve >90% coverage of state management code
2. **Performance**: No measurable performance regression
3. **Code Quality**: Reduced coupling metrics
4. **Developer Velocity**: Easier to write and debug state-related code

## Timeline

- **Week 1**: Create EDN schemas and proto-bridge
- **Week 2**: Update state dispatch and write tests
- **Week 3**: Migrate existing code
- **Week 4**: Documentation and optimization

## Example Test

With the new architecture, tests become much simpler:

```clojure
(deftest test-state-update
  (let [old-state {:system {:uptime-seconds 100}}
        new-state {:system {:uptime-seconds 101}}]
    
    ;; Test without protobuf
    (is (state/changed? old-state new-state))
    
    ;; Test subsystem update
    (let [system-atom (atom nil)]
      (state/update-subsystem-if-changed! system-atom 
                                         (:system new-state) 
                                         :system)
      (is (= @system-atom {:uptime-seconds 101})))))
```

## Conclusion

This isolation strategy maintains backward compatibility while significantly improving the codebase's testability and maintainability. The investment in creating this abstraction layer will pay dividends in reduced debugging time and increased confidence in the state management system.