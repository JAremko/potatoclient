# Subprocess Testing Improvements TODO

## Overview
This document tracks the necessary improvements for subprocess testing in PotatoClient to ensure integration tests properly catch issues like the `:command` vs `:cmd` mismatch that was discovered in production.

## Prerequisites
- [ ] Run `make fmt` to fix code formatting
- [ ] Run `make lint` and fix any linting issues
- [ ] Ensure all existing tests pass before making changes

## Current Issues

### 1. Command/State Subprocess Testing
- [x] Fixed mock functions using wrong subprocess key (`:command` → `:cmd`)
- [ ] E2E tests bypass subprocess launcher - they start processes directly
- [ ] No tests for subprocess registry mechanism
- [ ] Tests operate at wrong abstraction level (direct Transit vs full integration)

### 2. Video Stream Process Testing
- [ ] Video stream processes not covered by integration tests
- [ ] Mock-video-stream tool exists but not integrated into test suite
- [ ] No tests verify video stream subprocess registration
- [ ] Missing tests for video stream IPC communication

## Required Improvements

### Phase 1: Fix Existing Tests
- [x] Update gesture tests to use `:cmd` instead of `:command`
- [ ] Create integration tests that use actual subprocess launcher
- [ ] Add tests for subprocess registry (register/lookup/remove)
- [ ] Test subprocess lifecycle (start/stop/restart)

### Phase 2: Command/State Subprocess Tests
- [ ] Create test harness that starts real subprocesses via launcher
- [ ] Test message routing through subprocess registry
- [ ] Verify Transit message format conversions
- [ ] Test error handling and subprocess crashes
- [ ] Add tests for concurrent subprocess operations

### Phase 3: Video Stream Process Tests
- [ ] Integrate mock-video-stream tool into test suite
- [ ] Create tests for video stream subprocess registration
- [ ] Test gesture → command flow through real subprocesses
- [ ] Verify Transit message protocol for video streams
- [ ] Test video stream lifecycle events (open/close/error)

### Phase 4: Mock-Video-Stream Improvements
- [ ] Add test mode flags similar to command/state subprocesses
- [ ] Create integration tests using mock-video-stream
- [ ] Add negative testing scenarios (errors, timeouts)
- [ ] Improve gesture simulation accuracy
- [ ] Add frame timing simulation

## Implementation Plan

### 1. Create Subprocess Test Utilities
```clojure
(ns potatoclient.test.subprocess-utils
  (:require [potatoclient.transit.subprocess-launcher :as launcher]))

(defn with-test-subprocesses
  "Fixture that starts test-mode subprocesses"
  [f]
  (let [cmd-proc (launcher/start-command-subprocess "test://cmd" "test")
        state-proc (launcher/start-state-subprocess "test://state" "test")]
    (try
      (f)
      (finally
        (launcher/stop-subprocess :cmd)
        (launcher/stop-subprocess :state)))))

(defn wait-for-subprocess
  "Wait for subprocess to be ready"
  [subprocess-type timeout-ms]
  ;; Implementation
  )
```

### 2. Add Subprocess Registry Tests
```clojure
(deftest test-subprocess-registry
  (testing "Subprocess registration and lookup"
    (with-test-subprocesses
      (is (some? (launcher/get-subprocess :cmd)))
      (is (some? (launcher/get-subprocess :state)))
      (is (= :running (launcher/get-subprocess-state :cmd))))))
```

### 3. Create Video Stream Integration Tests
```clojure
(deftest test-video-stream-integration
  (testing "Video stream subprocess with mock"
    (let [proc (process/start-video-stream-subprocess 
                 "mock-heat" "mock://heat" "test")]
      (try
        ;; Test gesture processing
        (process/send-message proc {:type :mouse-down :x 400 :y 300})
        (let [cmd (wait-for-command 1000)]
          (is (= :cmd (:subprocess-type cmd)))
          (is (= "rotary-goto-ndc" (get-in cmd [:payload :action]))))
        (finally
          (process/stop-subprocess proc))))))
```

### 4. Mock-Video-Stream Test Mode
```bash
# Add test mode to mock-video-stream
clojure -M:process --stream-type heat --test-mode

# Should output test-ready signal
{"msg-type":"status","payload":{"status":"test-mode-ready"}}
```

## Test Coverage Goals

### Subprocess Launcher
- [ ] Start/stop subprocesses
- [ ] Message routing
- [ ] Error handling
- [ ] Subprocess crashes
- [ ] Registry operations

### Command Subprocess
- [ ] Transit → Protobuf conversion
- [ ] Command validation
- [ ] Response handling
- [ ] WebSocket connection
- [ ] Error scenarios

### State Subprocess  
- [ ] Protobuf → Transit conversion
- [ ] State updates
- [ ] Rate limiting
- [ ] Connection status
- [ ] Error scenarios

### Video Stream Processes
- [ ] Gesture recognition
- [ ] Command generation
- [ ] Transit protocol
- [ ] Window events
- [ ] Frame timing

## Success Criteria

1. **Integration tests catch subprocess key mismatches**
   - Tests fail if `:cmd` vs `:command` mismatch
   - Tests fail if subprocess not registered

2. **Full subprocess lifecycle coverage**
   - All subprocesses tested through launcher
   - Registry operations verified
   - Error scenarios handled

3. **Mock-video-stream integration**
   - Used in CI/CD pipeline
   - Covers all gesture types
   - Validates command format

4. **No direct subprocess spawning in tests**
   - All tests use subprocess launcher
   - Proper abstraction levels maintained

## Timeline

- Week 1: Fix existing tests and create test utilities
- Week 2: Implement command/state subprocess tests
- Week 3: Integrate mock-video-stream tool
- Week 4: Add video stream process tests
- Week 5: Error scenario testing and documentation

## Notes

- Consider using test containers for WebSocket mocking
- May need to add debug logging to subprocess launcher
- Performance tests should use mock subprocesses
- Consider adding subprocess health checks