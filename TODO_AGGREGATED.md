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

### ⏳ Transit Keyword Type System (NEW)
- **Status**: DESIGNED - Implementation pending
- **Design**: `src/potatoclient/transit/keyword_handlers.clj` (created, not integrated)
- **Research**: Based on protobuf analysis showing only enums and numbers in our system
- **Benefits**:
  - Automatic enum → keyword conversion
  - No more string/keyword confusion  
  - Type safety with Java enums
  - Eliminates all manual string/keyword conversions
- **Implementation Strategy**:
  - Custom Transit handlers for automatic string→keyword conversion
  - Preserve strings only for designated text fields (log messages)
  - Java enums with Transit keyword support
- **Next Steps**:
  1. Integrate Transit handlers into core reader/writer creation
  2. Update all Kotlin subprocesses to use enum handlers
  3. Remove manual keyword conversions throughout codebase
  4. Add tests for automatic conversion

## 📋 Medium Priority Tasks

### ⏳ Kotlin Message Validation
- **Status**: DESIGNED - Not implemented
- **Spec Location**: `src/potatoclient/specs/transit_messages.clj`
- **Components Needed**:
  - `TransitMessageValidator` class
  - Per-subprocess contracts
  - Development-only validation
  - Detailed error logging

### ⏳ Complete State Converter
- **File**: `src/potatoclient/kotlin/transit/SimpleStateConverter.kt`
- **Status**: Stub implementation only
- **TODO**: 
  - Convert all protobuf fields to Transit maps
  - Handle battery level properly
  - Add all system state fields

### ⏳ Message Type Corrections
- **Issue**: Control messages using "response" type
- **Should Be**: CTL type for control messages
- **Affects**: Both CommandSubprocess and StateSubprocess

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
- [.claude/transit-architecture.md](.claude/transit-architecture.md) - Complete Transit implementation
- [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md) - Kotlin subprocess details
- [.claude/protobuf-command-system.md](.claude/protobuf-command-system.md) - Command system design

## 🐛 Known Issues

### Rate Limiter Observability
- **Location**: `src/potatoclient/kotlin/transit/StateSubprocess.kt`
- **Issue**: No metrics on dropped messages
- **Fix**: Add periodic metric reporting

### WebSocket Error Handling
- **Issue**: Errors logged to stderr
- **Fix**: Send errors via Transit protocol

## 📅 Implementation Phases

### Phase 1: Type System (Current Priority)
1. Implement keyword handlers
2. Update all message creation
3. Remove string conversions
4. Update tests

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
- `src/potatoclient/transit/` - Clojure Transit code
- `src/potatoclient/kotlin/transit/` - Kotlin Transit code
- `src/potatoclient/specs/transit_messages.clj` - Malli specs

### Subprocess Entry Points
- `src/potatoclient/kotlin/transit/CommandSubprocess.kt` - main() at line 255
- `src/potatoclient/kotlin/transit/StateSubprocess.kt` - main() at line 326
- `src/potatoclient/kotlin/VideoStreamManager.kt` - main() at line 397

### Key Integration Points
- `src/potatoclient/ipc.clj` - Message routing
- `src/potatoclient/events/stream.clj` - Event handling
- `src/potatoclient/kotlin/transit/TransitMessageProtocol.kt` - Message creation

## ✨ Quick Wins

1. Remove all `(keyword ...)` calls once handlers are in place
2. Add logging for unknown message types
3. Create helper functions for common patterns
4. Add comments explaining Transit flow
