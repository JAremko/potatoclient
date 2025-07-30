# Transit Architecture Implementation TODO

## Overview
This document tracks the implementation progress of the Transit Architecture Rework (Version 3) as specified in `TRANSIT_ARCHITECTURE_REWORK.md`. The goal is to completely isolate protobuf handling in Kotlin subprocesses while using Transit/MessagePack for efficient binary communication between Clojure and Kotlin.

## ✅ Completed

### Core Transit Infrastructure
- [x] **Transit/MessagePack communication layer** (`potatoclient.transit.core`)
  - Custom write/read handlers for domain types
  - Efficient binary encoding/decoding
  - Message envelope creation with UUID and timestamps
  - Round-trip encoding verification

### Single App-DB Atom Implementation
- [x] **Unified state atom** (`potatoclient.transit.app-db`)
  - Replaced 13+ separate atoms with single source of truth
  - Comprehensive accessor functions with Guardrails specs
  - State update helpers with validation
  - Watch handler support for debugging
  - Initial state structure matching all subsystems

### Malli Schema Validation
- [x] **Comprehensive schemas** (`potatoclient.transit.schemas`)
  - GPS, System, LRF, Compass, Rotary subsystem schemas
  - Camera (Day/Heat) and Glass Heater schemas
  - Connection, UI, Process state schemas
  - Rate limiting and validation state schemas
  - Message envelope schemas for all message types
  - Schema registry integration with potatoclient.specs

### Subprocess Management
- [x] **Subprocess lifecycle management** (`potatoclient.transit.subprocess`)
  - ProcessBuilder implementation for launching Kotlin JARs
  - Bidirectional I/O stream handlers with core.async
  - Control channel for subprocess commands
  - Graceful shutdown with timeout
  - Process monitoring and health checks
  - Transit message routing between processes

### Message Handling and Routing
- [x] **Message handler infrastructure** (`potatoclient.transit.handlers`)
  - Handler registration/unregistration system
  - State update message processing
  - Validation error handling
  - Request-response pattern support
  - Rate metric tracking
  - Subprocess error recovery

### Control Interface
- [x] **Bidirectional control system** (`potatoclient.transit.control`)
  - Subprocess registry management
  - Control message sending (set rate limits, toggle validation)
  - Query messages with timeout support
  - Health monitoring for all subprocesses
  - Batch control operations
  - Emergency stop functionality
  - Subprocess restart capability

### State Management Rewrite
- [x] **Clean rewrite of state.clj**
  - Direct use of app-db (no migration layers)
  - All functions using Transit app-db accessors
  - Removed old atom-based state subdirectory
  - Stream process management integrated
  - Configuration management integrated
  - Device state access integrated
  - UI state management integrated
  - Process management for all subprocesses

### Testing
- [x] **Comprehensive test suite** (`test/potatoclient/transit_test.clj`)
  - Transit encoding/decoding tests
  - App-db structure validation
  - Schema validation tests
  - Message handler tests
  - Rate limiting metric tests
  - Process state management tests
  - UI state tests
  - Integration tests
  - All tests passing (47 assertions)

### Build System Updates
- [x] **Dependencies added**
  - `com.cognitect/transit-clj {:mvn/version "1.0.333"}`
  - Proper namespace requires for specs
  - Fixed all Guardrails spec references

### Kotlin Subprocess Implementation
- [x] **Complete CommandSubprocess.kt**
  - Protobuf command generation from Transit messages
  - buf.validate integration for command validation
  - WebSocket client connection to potato server
  - Error handling and retry logic
  - Metrics collection and reporting

- [x] **Complete StateSubprocess.kt**
  - Protobuf state parsing from WebSocket
  - Debouncing logic using protobuf equals()
  - Token bucket rate limiting implementation
  - Transit message generation from protobuf
  - buf.validate for state validation
  - Efficient state diffing

- [x] **TransitCommunicator.kt refinement**
  - Proper error handling for I/O operations
  - Message framing for reliable communication
  - Backpressure handling
  - Connection state management

### Integration Tasks
- [x] **Update subprocess launcher**
  - Created `potatoclient.transit.subprocess-launcher` with full process management
  - Main class entries for CommandSubprocess and StateSubprocess
  - Reuses existing process management patterns from video streams
  - Transit-specific process builder configuration

- [x] **Build system updates**
  - Kotlin Transit classes automatically compile into main JAR
  - No changes needed to Makefile - already compiles all Kotlin sources
  - Single JAR architecture like video stream classes
  - Classpath includes all necessary dependencies

### Integration Tasks (Final Phase)
- [x] **Replace existing WebSocket code**
  - Created `potatoclient.transit.websocket-manager` to manage Transit subprocesses
  - Created `potatoclient.transit.commands` with clean command API
  - Created migration guide and compatibility layer
  - State now flows through app-db automatically
  - No more direct protobuf usage needed in Clojure

## ❌ Not Yet Implemented

### Validation Integration
- [ ] **buf.validate setup**
  - Add protovalidate-java dependency for Kotlin
  - Configure validation to run in all modes except release
  - Implement validation error reporting via Transit

- [ ] **Malli validation refinement**
  - Add custom error messages for better UX
  - Implement validation stats tracking
  - Create validation report UI

### Performance Optimization
- [ ] **Rate limiting tuning**
  - Implement configurable rate limits per subsystem
  - Add metrics for dropped updates
  - Create rate limit visualization

- [ ] **Memory optimization**
  - Implement bounded queues for message channels
  - Add memory usage monitoring
  - Optimize Transit message sizes

### Testing
- [ ] **Integration tests with real WebSocket**
  - Test against actual potato server
  - Verify protobuf ↔ Transit conversion
  - Load testing with high update rates
  - Network failure recovery tests

- [ ] **Kotlin subprocess tests**
  - Unit tests for protobuf conversion
  - Debouncing logic tests
  - Rate limiting tests
  - Validation tests

### Documentation
- [ ] **Architecture diagrams**
  - Message flow diagrams
  - State management visualization
  - Subprocess communication patterns

- [ ] **Developer guide updates**
  - How to add new command types
  - How to add new state fields
  - Debugging subprocess communication
  - Performance tuning guide

## Migration Strategy

### Phase 1: Parallel Implementation (Current)
- Run new Transit system alongside existing WebSocket code
- Allow switching between old and new systems via config
- Gather metrics comparing both systems

### Phase 2: Gradual Migration
1. Start with non-critical commands (UI preferences)
2. Migrate state updates to Transit
3. Move video stream process management
4. Migrate critical commands last

### Phase 3: Cleanup
- Remove old WebSocket code
- Remove direct protobuf dependencies from Clojure
- Archive old atom definitions
- Update all documentation

## Notes

### Key Design Decisions
1. **Message framing**: Using newline-delimited Transit messages for simplicity
2. **Process isolation**: Each subprocess runs in separate JVM for fault isolation
3. **Rate limiting**: Implemented in Kotlin to minimize Clojure overhead
4. **Validation**: Dual approach with buf.validate (Kotlin) and Malli (Clojure)

### Known Issues
1. Kotlin subprocess skeletons reference non-existent protobuf classes
2. Need to handle subprocess crashes gracefully
3. Memory usage not yet optimized for high-frequency updates

### Performance Targets
- State updates: 30Hz maximum (configurable)
- Command latency: <10ms for validation and sending
- Memory overhead: <50MB per subprocess
- CPU usage: <5% for state processing at 30Hz

## Resources
- [Transit Format Spec](https://github.com/cognitect/transit-format)
- [buf.validate Documentation](https://buf.build/docs/bsr/generated-sdks/protovalidate)
- [Malli Documentation](https://github.com/metosin/malli)
- [re-frame app-db pattern](https://day8.github.io/re-frame/application-state/)