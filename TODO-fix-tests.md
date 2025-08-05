# TODO: Fix All Tests - No Skipping Allowed! 🚫

**Last Updated**: 2025-08-05

## Goal
Fix ALL tests to pass reliably. No disabled tests, no commented-out sections, no skipping. Every test must work with the current architecture.

## Session Progress Tracking

### Session 1 (2025-08-05):
- ✅ Fixed ProcessBuilder$Redirect compilation error in working_subprocess_test.clj
- ✅ Added Kotlin test compilation/execution to Makefile
- ✅ Identified test timeout issue (subprocess stdin handling)
- ✅ Discovered Transit keyword violation in Kotlin code
- ✅ Analyzed all Kotlin tests - found they reference obsolete architecture
- ✅ Investigated tools (transit-test-generator, mock-video-stream)
- 🔄 Started fixing TransitMessageProtocol to use keywords

### Tools Status:
- **transit-test-generator**: ✅ Uses keywords correctly, ready to use for test generation
- **mock-video-stream**: ✅ Uses keywords correctly, can simulate server for integration tests

## Status Overview
- [ ] Remove 5 disabled test files (4 .skip, 1 .disabled)
- [ ] Fix hanging tests (tests timeout after 2 minutes)
- [ ] Fix failing tests (32 failures in gesture/handler tests)
- [ ] Fix compilation error in working_subprocess_test.clj
- [ ] Rewrite outdated tests for current architecture
- [ ] Ensure all tests run in CI
- [ ] Ensure Kotlin tests are actually running

## Phase 1: Analyze Disabled Tests

### Currently Disabled Tests:
1. **Kotlin Tests (.skip files):**
   - `test/kotlin/potatoclient/kotlin/transit/ProtobufRoundtripTest.kt.skip`
   - `test/kotlin/potatoclient/kotlin/transit/TransitToProtobufVerifier.kt.skip`
   - `test/kotlin/potatoclient/kotlin/transit/FullTransitProtobufTest.kt.skip`
   - Uses obsolete `ProtobufCommandBuilder` (replaced by `GeneratedCommandHandlers`)

2. **Clojure Tests:**
   - `test/potatoclient/transit/kotlin_integration_test.clj.skip`
   - `test/potatoclient/transit/malli_kotlin_validation_test.clj.disabled`
   - Uses obsolete `SimpleCommandBuilder` and `MalliPayloadValidator`

### Active Kotlin Tests Issues:
1. **Non-compilable tests (15 files):**
   - Reference non-existent classes: `SimpleStateConverter`, `ProtobufToTransitConverter`, `TransitToProtobufBuilder`
   - Missing imports and wrong package references
   - Tests appear to be from an older architecture

2. **Tests to DELETE:**
   - All current Kotlin tests in `test/kotlin/` - they reference obsolete architecture
   - Need to write new tests that match current `GeneratedHandlers` architecture

## Phase 2: Rewrite Disabled Tests

### Clojure Tests to Rewrite:

#### 1. `test/potatoclient/transit/kotlin_integration_test.clj.skip`
- [ ] Replace `SimpleCommandBuilder` with `GeneratedCommandHandlers`
- [ ] Update to use current subprocess management
- [ ] Convert to keyword-based command structure (`:command`, `:heat`, etc.)
- [ ] Update command API calls to match current `potatoclient.transit.commands`
- [ ] Ensure all test cases pass without timeouts

#### 2. `test/potatoclient/transit/malli_kotlin_validation_test.clj.disabled`
- [ ] Fix spec imports - use specs from `shared/specs/`
- [ ] Create or integrate with existing validation infrastructure
- [ ] Update to current command API patterns
- [ ] Remove dependency on non-existent `MalliPayloadValidator`
- [ ] Ensure Malli → Transit → Kotlin → Protobuf flow is tested

### Kotlin Tests to Rewrite:

#### 3. `test/kotlin/potatoclient/kotlin/transit/FullTransitProtobufTest.kt.skip`
- [ ] Replace `ProtobufCommandBuilder` with `GeneratedCommandHandlers`
- [ ] Update Transit handler setup to match current implementation
- [ ] Keep roundtrip validation approach but update API usage
- [ ] Ensure all test cases pass

#### 4. `test/kotlin/potatoclient/kotlin/transit/ProtobufRoundtripTest.kt.skip`
- [ ] Update to use `GeneratedCommandHandlers` API
- [ ] Align with current protobuf message structure
- [ ] Maintain serialization verification but with current classes
- [ ] Fix all compilation errors

## Phase 3: Fix Immediate Issues

### 1. Fix Compilation Error:
- [x] Fix syntax error in `working_subprocess_test.clj:25:13` (ProcessBuilder$Redirect issue)
  - Fixed: Removed incorrect ProcessBuilder$Redirect usage, properly use process stdin/stdout

### 2. Fix Hanging Tests:
- [x] Tests are timing out after 2 minutes during subprocess startup
- [x] Fixed: Subprocess stdin handling corrected
- [ ] Test failures reveal architecture violation: Transit messages using strings instead of keywords

### 3. Fix Architecture Violation:
- [ ] Transit messages from Kotlin are using string keys instead of keywords
- [ ] Issue: `MessageKeys.java` defines string constants, but should use Transit keywords
- [ ] `TransitMessageProtocol.createMessage()` uses `MessageKeys.MSG_TYPE` (string) instead of `TransitKeys.MSG_TYPE` (keyword)
- [ ] This violates "Keywords Everywhere" principle from CLAUDE.md
- [ ] Add proper timeouts to async tests
- [ ] Fix any deadlocks in channel operations
- [ ] Ensure all go blocks complete properly
- [ ] Add test fixtures that clean up resources

### Common Causes to Check:
- [ ] Unclosed channels in core.async tests
- [ ] Missing timeouts in subprocess communication
- [ ] Deadlocks in Transit message handling
- [ ] Resource cleanup in test fixtures

## Phase 4: Fix Failing Tests

### Current Failures (32 total):
- [ ] Fix gesture handler tests - 18 failures in `handler_test.clj`
  - `test-handle-double-tap-gesture` (5 failures)
  - `test-handle-gesture-event` (2 failures)
  - `test-handle-pan-gestures` (7 failures)
  - `test-handle-tap-gesture` (4 failures)
- [ ] Fix gesture integration tests - 14 failures in `integration_test.clj`
  - `test-gesture-validation` (1 failure)
  - `test-double-tap-with-frame-timing` (2 failures)
  - `test-complete-tap-flow` (4 failures)
  - `test-stream-type-routing` (2 failures)
  - `test-complete-pan-flow` (5 failures)

### Areas to Check:
- [ ] Guardrails spec violations
- [ ] Malli schema mismatches
- [ ] Transit serialization issues
- [ ] Protobuf validation failures
- [ ] Race conditions in async tests

## Phase 5: Test Infrastructure

### Improvements Needed:
- [ ] Add test runner that fails fast on hangs (timeout after 30s)
- [ ] Improve test logging to identify hanging tests
- [ ] Add pre-test validation for required resources
- [ ] Create test helpers for common patterns
- [ ] Add retry mechanism for flaky network tests

## Phase 6: CI Integration

### Ensure:
- [ ] All tests run in CI pipeline
- [ ] No tests are excluded from CI
- [ ] Test failures block merges
- [ ] Test reports are easily accessible
- [ ] Parallel test execution works correctly

## Rules for This Task

### ✅ DO:
- Fix the root cause of failures
- Update tests to match current architecture
- Add proper error messages to assertions
- Use test fixtures for resource management
- Add timeouts to all async operations

### ❌ DON'T:
- Don't skip tests with `.skip` or `.disabled`
- Don't comment out failing assertions
- Don't reduce test coverage
- Don't ignore flaky tests
- Don't use `(is true)` to fake passing tests

## Testing the Current Architecture

### Key Components to Verify:
1. **Transit Protocol**: All messages use keywords (`:command`, `:heat`, etc.)
2. **GeneratedHandlers**: Transit ↔ Protobuf conversion works both ways
3. **Subprocess Communication**: Clean IPC without protobuf in Clojure
4. **Guardrails**: All functions have working specs
5. **Malli Schemas**: All data structures validate correctly

## Phase 7: Ensure Kotlin Tests Run

### Investigation Needed:
- [x] Check if Kotlin tests are compiled during `make test` - NOT COMPILED
- [x] Verify Kotlin test runner is properly configured - NOT CONFIGURED
- [x] Ensure JUnit dependencies are available - AVAILABLE in deps.edn
- [x] Check if test discovery is working for Kotlin files - NOT IMPLEMENTED
- [x] Add Kotlin test execution to Makefile if missing - ADDED

### Fixed:
- Added `compile-kotlin-tests` target to Makefile
- Modified `test` target to:
  1. Compile Kotlin tests before running
  2. Run JUnit tests after Clojure tests
  3. Combine exit codes from both test runs
- Tests will now discover and run all Kotlin test classes

## Success Criteria

- [ ] `make test` completes without hangs
- [ ] All tests pass (0 failures, 0 errors)
- [ ] No `.skip` or `.disabled` files in test directories
- [ ] No commented-out test code
- [ ] CI runs all tests successfully
- [ ] Test execution time < 2 minutes
- [ ] Kotlin tests are included in test runs

## Testing Tools Available

### 1. transit-test-generator (tools/transit-test-generator)
**Purpose**: Generate and validate Transit command messages using Malli specs
**Status**: ✅ Ready to use - follows keyword architecture correctly

**How it helps with testing**:
- Generate valid Transit messages for all command types
- Validate messages conform to specs
- Test Transit roundtrip encoding/decoding
- Create comprehensive test datasets

**Usage for our tests**:
```bash
# Generate test commands
cd tools/transit-test-generator
make build
java -jar target/transit-test-generator-*.jar generate --command ping --format transit

# Validate our fixed Kotlin output
java -jar target/transit-test-generator-*.jar validate --input-file message.transit
```

### 2. mock-video-stream (tools/mock-video-stream)
**Purpose**: Simulate PotatoClient's video streaming server without hardware
**Status**: ✅ Ready to use - follows keyword architecture correctly

**How it helps with testing**:
- Provides WebSocket endpoints for integration tests
- Simulates command handling and state updates
- Can run test scenarios
- No hardware dependencies

**Usage for our tests**:
```bash
# Start mock server for integration tests
cd tools/mock-video-stream
make start-server  # Runs on localhost:8080

# Run test scenarios
make scenario SCENARIO=rapid-commands
```

## Progress Summary (as of current analysis)

### ✅ Completed:
1. Fixed `working_subprocess_test.clj` compilation error (ProcessBuilder issue)
2. Added Kotlin test compilation to Makefile
3. Identified test timeout was due to subprocess stdin handling
4. Discovered architecture violation: Transit messages using strings instead of keywords

### 🔴 Major Issues Found:
1. **All Kotlin tests (20 files) are obsolete** - reference non-existent classes
2. **Transit protocol violation** - Kotlin code using string keys instead of keywords
3. **32 gesture-related test failures** in Clojure tests
4. **5 disabled test files** that need analysis

### 📋 Immediate Actions Needed:
1. Delete all existing Kotlin tests (they're for old architecture)
2. Fix Transit message creation to use keywords (TransitKeys instead of MessageKeys)
3. Write new Kotlin tests for current GeneratedHandlers architecture
4. Fix gesture test failures
5. Analyze and either fix or delete the 5 disabled tests

## Current Work In Progress

### 🔄 Fixing Transit Keyword Issue (CRITICAL)
**File**: `src/potatoclient/kotlin/transit/TransitMessageProtocol.kt`
**Issue**: Using `MessageKeys` (strings) instead of `TransitKeys` (keywords)
**Progress**: Started converting createMessage() function
**TODO**: 
- [ ] Finish updating all MessageKeys references to TransitKeys
- [ ] Update TransitKeys.kt if missing any keys
- [ ] Test with transit-test-generator tool
- [ ] Verify working_subprocess_test.clj passes

## Next Steps

1. **Complete Transit keyword fix** (architecture violation - highest priority)
   - Use transit-test-generator to validate fixed messages
   
2. **Delete obsolete Kotlin tests**
   - All 15 active test files reference non-existent classes
   - Keep the .skip files for reference when writing new tests
   
3. **Write new Kotlin tests using tools**
   - Use transit-test-generator for message generation
   - Use mock-video-stream for integration tests
   - Focus on testing GeneratedHandlers architecture
   
4. **Fix gesture test failures** (32 failures)
   - Investigate root cause
   - May be related to Transit keyword issue
   
5. **Ensure CI runs all tests**
   - Verify Kotlin tests execute in CI
   - Add test summary reporting

---

Remember: **Every test must work!** No exceptions, no workarounds, no skipping. If a test can't be fixed, it should be removed entirely rather than disabled.