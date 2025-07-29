# Test Failure Analysis

## Summary
- Total tests run: 116
- Total assertions: 3418
- Failures: 59
- Errors: 40

## Main Issues

### 1. WebSocket Integration Tests
Several tests are failing because they still depend on the removed WebSocket server classes:
- Already disabled: websocket_edge_cases_test.clj, websocket_full_integration_test.clj, etc.

### 2. Generator Tests Failing
Multiple failures in `generator_test.clj`:
- test-rotary-movement-commands - commands not being captured (returning nil)
- test-day-camera-basic-commands - commands not being captured 
- test-day-camera-focus-commands - commands not being captured
- test-rotary-axis-command - commands not being captured

These tests likely need to be updated to use the mock WebSocket manager properly.

### 3. Command Structure Validation Errors
Errors in test-command-structure-validation related to:
- Reflector.java:455 - likely reflection issues
- JsonFormat.java:763 - protobuf JSON formatting issues

### 4. WebSocket Stubbed Test Issues
- test-rotary-set-value-commands-stubbed - expecting 2 commands, getting 0
- test-multiple-commands-stubbed - expecting 3 commands, getting 2

These suggest the mock isn't properly capturing all commands.

### 5. WebSocket Integration Test
- test-command-sending - cmd/send-cmd-ping and cmd/send-cmd-frozen returning true instead of nil

## Recommendations
1. Update generator tests to use the new mock WebSocket manager
2. Fix the stubbed test mock to properly capture all commands
3. Investigate protobuf/reflection issues in validation tests
4. Ensure all command functions return nil as expected