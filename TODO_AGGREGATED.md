# PotatoClient TODO - Aggregated Tasks and Technical Debt

This document aggregates all TODO items, analysis results, and implementation plans from various documents in the codebase. It serves as the central tracking point for all pending work.

## 🚨 Critical Issues (Completed)

These issues were breaking functionality and have been fixed:

### ✅ String Key Access in Clojure
- **File**: `src/potatoclient/events/stream.clj` (lines 165, 170, 174)
- **Issue**: Using `(get msg "action")` returns nil because Transit delivers keyword keys
- **Status**: FIXED - Changed to `(:action msg)`

### ✅ Window Close Event Type  
- **File**: `src/potatoclient/kotlin/VideoStreamManager.kt`
- **Issue**: Sending window close as "response" instead of proper "event" type
- **Status**: FIXED - Now sends EVENT with EventType.CLOSE

## 🔧 High Priority Tasks (Partially Complete)

### ✅ String Keys in Kotlin Subprocesses
- **Status**: FIXED - All subprocesses now use Transit message protocol helpers
- **Files Updated**:
  - `src/potatoclient/kotlin/transit/CommandSubprocess.kt` - Using messageProtocol.createMessage()
  - `src/potatoclient/kotlin/transit/StateSubprocess.kt` - Using MessageType.STATE_UPDATE

### ✅ Silent Command Building Failures
- **File**: `src/potatoclient/kotlin/transit/SimpleCommandBuilder.kt`
- **Status**: FIXED - Now returns Result<T> with detailed error messages
- **Before**: Unknown commands defaulted to ping
- **After**: Throws IllegalArgumentException with specific error

### ✅ Transit Keyword Type System
- **Status**: IMPLEMENTED - Transit handlers and protocol documentation complete
- **Design**: Created comprehensive protocol specification in `.claude/transit-protocol.md`
- **Implementation**: 
  - `ProtobufTransitHandlers.kt` - WriteHandlers for all message types
  - StateSubprocess updated to use Transit handlers
  - Protocol emphasizes "Keywords Everywhere" principle
- **Benefits Achieved**:
  - Automatic enum → keyword conversion via Transit handlers
  - No more string/keyword confusion  
  - Type safety with Java enums
  - Clean architecture with no manual serialization
- **Completed Steps**:
  1. Created Transit protocol specification emphasizing keyword-based data model
  2. Updated TransitCommunicator to accept custom handlers
  3. StateSubprocess now uses Transit handlers for automatic serialization
  4. Added CONTROL message type to MessageType enum

## 📋 Medium Priority Tasks

### ✅ Complete Protobuf Converters
- **Status**: REDESIGNED - Now using Transit handlers for automatic serialization
- **Proto Files Location**: [`examples/protogen/proto/`](examples/protogen/proto/)
- **Generated Java Classes**: [`src/potatoclient/java/ser/`](src/potatoclient/java/ser/) and [`src/potatoclient/java/cmd/`](src/potatoclient/java/cmd/)
- **Reference Implementation**: 
  - TypeScript command building: [`examples/web/frontend/ts/cmd/cmdSender/`](examples/web/frontend/ts/cmd/cmdSender/)
  - Example command sender: [`cmdRotary.ts`](examples/web/frontend/ts/cmd/cmdSender/cmdRotary.ts)
  - Example shared utilities: [`cmdSenderShared.ts`](examples/web/frontend/ts/cmd/cmdSender/cmdSenderShared.ts)
- **New Approach**: Transit Handlers
  - [`ProtobufTransitHandlers.kt`](src/potatoclient/kotlin/transit/ProtobufTransitHandlers.kt) - Automatic protobuf serialization (COMPLETED)
  - Provides WriteHandlers for all data types (system, rotary, GPS, compass, LRF, cameras, time)
  - Automatically converts enums to Transit keywords
  - No manual conversion needed - Transit handles serialization
- **Legacy Converters** (kept for reference):
  - [`SimpleStateConverter.kt`](src/potatoclient/kotlin/transit/SimpleStateConverter.kt) - Manual state converter
  - [`SimpleCommandBuilder.kt`](src/potatoclient/kotlin/transit/SimpleCommandBuilder.kt) - Command builder (kept for fallback)
- **Command Building** (Transit → Proto):
  - ✅ Created `ProtobufCommandHandlers.kt` with Transit ReadHandlers
  - ✅ All core commands implemented: rotary, system, GPS, compass, CV, day_camera, heat_camera, LRF, OSD, glass_heater
  - Remaining commands for future implementation: LIRA, LRF_align

### ✅ Update All Subprocesses to Use Transit Handlers
- **Status**: COMPLETED - All major subprocesses updated
- **Goal**: Replace manual serialization with Transit handlers across all subprocesses
- **Implementation**:
  1. ✅ Update TransitCommunicator to use ProtobufTransitHandlers
  2. ✅ StateSubprocess: Replace SimpleStateConverter with automatic protobuf handlers
  3. ✅ VideoStreamManager: Already uses Transit protocol correctly for events
  4. ✅ CommandSubprocess: Updated to use ProtobufCommandHandlers with fallback
  5. ⏳ Error handling: Use error message handlers instead of stderr
- **Benefits**:
  - Consistent serialization across all message types
  - Automatic enum to keyword conversion
  - Type safety with proper Transit tagging
  - Cleaner code without manual map building
- **Handler Coverage**:
  - ✅ Protobuf state messages (all data types)
  - ✅ Gesture events
  - ✅ Navigation events
  - ✅ Window events
  - ✅ Control messages
  - ✅ Error messages
  - ✅ Log messages

### ✅ Create Malli Spec Structure for Protobuf Validation
- **Status**: COMPLETED - All data specs created
- **Goal**: Create Clojure specs mirroring protobuf validation constraints
- **Completed**:
  - ✅ `potatoclient.specs.data.gps` - GPS data validation
  - ✅ `potatoclient.specs.data.system` - System data validation
  - ✅ `potatoclient.specs.data.rotary` - Rotary data validation
  - ✅ `potatoclient.specs.data.compass` - Compass data validation
  - ✅ `potatoclient.specs.data.lrf` - LRF data validation
  - ✅ `potatoclient.specs.data.time` - Time data validation
  - ✅ `potatoclient.specs.data.camera` - Camera data validation (heat and day)
  - ✅ `potatoclient.specs.data.types` - Common enum types
  - ✅ `potatoclient.specs.data.state` - Top-level state structure (includes all data types)
  - ✅ `potatoclient.specs.cmd.rotary` - Rotary command validation
- **Structure**: One spec namespace per proto file:
  - `potatoclient.specs.data.system` for `jon_shared_data_system.proto`
  - `potatoclient.specs.data.rotary` for `jon_shared_data_rotary.proto`
  - `potatoclient.specs.cmd.rotary` for `jon_shared_cmd_rotary.proto`
  - etc.
- **Reference**: Use buf.validate annotations from proto files:
  - Float/double ranges: `[(buf.validate.field).float = {gte: 0, lte: 100}]`
  - Enum constraints: `[(buf.validate.field).enum = {defined_only: true, not_in: [0]}]`
  - Required fields: `[(buf.validate.field).required = true]`
- **Implementation Strategy**:
  1. Parse proto files to extract validation constraints
  2. Generate corresponding Malli specs
  3. Use specs for command validation before sending
  4. Use specs for state validation after receiving
- **Example Spec**:
  ```clojure
  ;; From jon_shared_data_rotary.proto azimuth field
  (def azimuth
    [:float {:min 0 :max 360}])  ; From gte: 0, lt: 360
  ```

### ✅ Implement Transit ReadHandlers for Command Building
- **Status**: COMPLETED
- **Goal**: Replace SimpleCommandBuilder with Transit ReadHandlers
- **Rationale**: Clean architecture - use Transit handlers for both directions
- **Implementation**:
  - ✅ Created `ProtobufCommandHandlers.kt` with ReadHandlers for command building
  - ✅ Maps Transit command structures to protobuf builders
  - ✅ Supports all command types (rotary, system, GPS, compass, CV, cameras, LRF, OSD, glass heater)
  - ✅ Automatic keyword to enum conversion
  - ✅ Updated CommandSubprocess to use handlers with fallback to SimpleCommandBuilder
- **Benefits Achieved**:
  - Symmetric with WriteHandlers for state messages
  - No manual command building logic
  - Type-safe command construction
  - Consistent error handling
  - Backward compatibility maintained

### ⏳ Kotlin Message Validation
- **Status**: DESIGNED - Not implemented  
- **Spec Location**: `src/potatoclient/specs/transit_messages.clj`
- **Components Needed**:
  - `TransitMessageValidator` class
  - Per-subprocess contracts
  - Development-only validation
  - Detailed error logging
- **Note**: Should be implemented AFTER Malli spec structure is created

### ✅ Message Type Corrections
- **Status**: COMPLETED
- **Issue**: Control messages were using "response" type
- **Fix**: Added CONTROL message type to MessageType enum
- **Updated Files**:
  - `MessageType.java` - Added CONTROL("control") enum value
  - `CommandSubprocess.kt` - Now expects MessageType.CONTROL
  - `StateSubprocess.kt` - Now expects MessageType.CONTROL
- **Remaining**: Update Clojure side to send control messages with CONTROL type

## 🧪 Testing Infrastructure

### ✅ Test Logging System
- **Status**: COMPLETE
- **Features**:
  - Automatic logging to `logs/test-runs/YYYYMMDD_HHMMSS/`
  - Test summaries and failure extraction
  - Latest symlink for easy access

### ⏳ Transit Message Tests
- **Status**: Basic tests exist
- **TODO**:
  - Add tests for keyword conversion
  - Test validation in development mode
  - Integration tests for subprocess communication

## ✅ Recent Documentation Updates

### Completed Today (2025-08-01)
- Updated `.claude/transit-architecture.md` with keyword type system section
- Added enum conversion details to `.claude/protobuf-command-system.md`
- Added Transit IPC integration section to `.claude/kotlin-subprocess.md`
- Updated CLAUDE.md with basic keyword type system info
- Fixed broken reference to transit-protocol.md → transit-architecture.md

## 📚 Documentation Updates Still Needed

### [CLAUDE.md](CLAUDE.md) Updates
- [x] Add Transit keyword type system documentation (basic info added)
- [ ] Update subprocess message examples to show keyword usage
- [ ] Document the validation system (once implemented)
- [ ] Add troubleshooting for common Transit issues
- [ ] Add section on protobuf enum conversion patterns

### [README.md](README.md) Updates  
- [ ] Update architecture diagram with Transit layer
- [ ] Add development workflow for Transit messages
- [ ] Document the keyword-based type system

## 🏗️ Architecture Improvements

### Transit Message Flow Improvements
- Standardized message envelope with validation
- Subprocess-specific contracts  
- Automatic keyword conversion
- Type-safe message building
- See [.claude/transit-architecture.md](.claude/transit-architecture.md) for details

### Subprocess Architecture
From analysis documents:
- **CommandSubprocess**: Handles command → protobuf conversion
- **StateSubprocess**: Handles protobuf → state conversion with rate limiting
- **VideoStreamManager**: Handles UI events and video streaming

### Related Documentation
- [`.claude/transit-architecture.md`](.claude/transit-architecture.md) - Complete Transit implementation
- [`.claude/transit-protocol.md`](.claude/transit-protocol.md) - Transit message protocol specification (keywords everywhere!)
- [`.claude/kotlin-subprocess.md`](.claude/kotlin-subprocess.md) - Kotlin subprocess details
- [`.claude/protobuf-command-system.md`](.claude/protobuf-command-system.md) - Command system design
- [`.claude/linting-guide.md`](.claude/linting-guide.md) - Code quality tools and false positive filtering

## 🐛 Known Issues


### WebSocket Error Handling
- **Issue**: Errors logged to stderr
- **Fix**: Send errors via Transit protocol

## 📅 Implementation Phases

### Phase 1: Type System (COMPLETED)
1. ✅ Implement keyword handlers (both WriteHandlers and ReadHandlers)
2. ✅ Update all message creation (using Transit protocol helpers)
3. ✅ Remove string conversions (automatic via handlers)
4. ⏳ Update tests

### Phase 2: Validation System
1. Create TransitMessageValidator
2. Add to all subprocesses
3. Development-only checks
4. Comprehensive error messages

### Phase 3: Cleanup & Documentation
1. Update all documentation
2. Remove deprecated code
3. Add integration tests
4. Performance optimization

## 🔍 Code Locations Reference

### Core Transit Files
- [`src/potatoclient/transit/`](src/potatoclient/transit/) - Clojure Transit code
- [`src/potatoclient/kotlin/transit/`](src/potatoclient/kotlin/transit/) - Kotlin Transit code
- [`src/potatoclient/specs/transit_messages.clj`](src/potatoclient/specs/transit_messages.clj) - Malli specs

### Subprocess Entry Points
- [`src/potatoclient/kotlin/transit/CommandSubprocess.kt`](src/potatoclient/kotlin/transit/CommandSubprocess.kt) - main() at line 255
- [`src/potatoclient/kotlin/transit/StateSubprocess.kt`](src/potatoclient/kotlin/transit/StateSubprocess.kt) - main() at line 326
- [`src/potatoclient/kotlin/VideoStreamManager.kt`](src/potatoclient/kotlin/VideoStreamManager.kt) - main() at line 397

### Key Integration Points
- [`src/potatoclient/ipc.clj`](src/potatoclient/ipc.clj) - Message routing
- [`src/potatoclient/events/stream.clj`](src/potatoclient/events/stream.clj) - Event handling
- [`src/potatoclient/kotlin/transit/TransitMessageProtocol.kt`](src/potatoclient/kotlin/transit/TransitMessageProtocol.kt) - Message creation

## ✨ Quick Wins

1. Remove all `(keyword ...)` calls once handlers are in place
2. Add logging for unknown message types
3. Create helper functions for common patterns
4. Add comments explaining Transit flow
