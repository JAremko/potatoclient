# State-Explorer Implementation TODO

## Project Overview
Create a tool to connect to the WebSocket state endpoint, capture binary protobuf payloads, and save them as both binary and EDN formats for analysis.

## Implementation Tasks

### 1. Project Setup ✅
- [x] Create directory structure
- [x] Create deps.edn with dependencies:
  - hato (for WebSocket client)
  - pronto (for protobuf to EDN conversion)
  - clojure.tools.cli (for command-line parsing)
  - clojure.data.json (for JSON output if needed)
- [x] Add output/ directory to .gitignore

### 2. Core WebSocket Implementation ✅
- [x] Create `websocket.clj` namespace
  - [x] Implement WebSocket connection using hato
  - [x] Handle wss:// protocol with SSL
  - [x] Configure connection to `wss://sych.local/ws/ws_state`
  - [x] Handle connection errors and retries
  - [x] Implement graceful shutdown

### 3. Payload Capture Logic ✅
- [x] Create `capture.clj` namespace
  - [x] Implement binary message handler
  - [x] Create timestamp-based filename generator (milliseconds)
  - [x] Save raw binary payload to `output/<timestamp>.bin`
  - [x] Track number of payloads captured
  - [x] Implement capture limit logic

### 4. Protobuf to EDN Conversion ✅
- [x] Create `proto_handler.clj` namespace
  - [x] Parse binary as `ser.JonSharedData$JonGUIState`
  - [x] Convert protobuf to EDN using Pronto
  - [x] Save EDN to `output/<timestamp>.edn`
  - [x] Handle parsing errors gracefully
  - [x] Log conversion statistics

### 5. CLI Interface ✅
- [x] Create `cli.clj` namespace
  - [x] Command-line argument parsing:
    - `--domain` (default: "sych.local")
    - `--count` (default: 1) - number of payloads to capture
    - `--output-dir` (default: "./output")
    - `--verbose` - enable debug logging
    - `--help` - show usage
  - [x] Progress indicator during capture
  - [x] Summary statistics after capture

### 6. Main Entry Point ✅
- [x] Create `core.clj` namespace
  - [x] Coordinate WebSocket connection
  - [x] Manage capture session
  - [x] Handle interrupts (Ctrl+C)
  - [x] Clean shutdown
  - [x] Exit codes for success/failure

### 7. Error Handling ✅
- [x] Connection failure handling
- [x] SSL certificate issues (trust all for dev)
- [x] Protobuf parsing errors
- [x] File I/O errors
- [x] Graceful degradation

### 8. Testing
- [ ] Create test namespace
  - [ ] Mock WebSocket connection tests
  - [ ] Binary payload parsing tests
  - [ ] EDN conversion tests
  - [ ] File naming tests
  - [ ] CLI argument parsing tests

### 9. Documentation ✅
- [x] Write comprehensive README.md:
  - [x] Tool purpose and use cases
  - [x] Installation instructions
  - [x] Usage examples
  - [x] Output format description
  - [x] Troubleshooting guide
- [x] Add inline code documentation
- [x] Create example analysis scripts (in Makefile)

### 10. Build & Deployment ✅
- [x] Create Makefile with targets:
  - [x] `make run` - run with defaults
  - [x] `make capture` - parameterized capture
  - [x] `make clean` - clean output directory
  - [x] `make test` - run tests
  - [x] `make help` - show usage
  - [x] Additional targets: `view-latest`, `analyze`, `watch`
- [x] Add to main project .gitignore

## Technical Specifications

### WebSocket Connection
- URL: `wss://<domain>/ws/ws_state`
- Protocol: WSS (WebSocket Secure)
- Message Type: Binary
- Direction: Receive only (unidirectional)

### Protobuf Message
- Type: `ser.JonSharedData$JonGUIState`
- Package: `ser`
- Contains: System state information

### Output Format
- Binary: `output/<timestamp_ms>.bin`
- EDN: `output/<timestamp_ms>.edn`
- Timestamp: System.currentTimeMillis()

### Dependencies Versions (IMPLEMENTED)
```clojure
{:deps {org.clojure/clojure {:mvn/version "1.11.1"}
        hato/hato {:mvn/version "0.9.0"}
        org.clojure/tools.cli {:mvn/version "1.0.219"}
        org.clojure/data.json {:mvn/version "2.4.0"}
        ;; Pronto from local path
        pronto/pronto {:local/root "../../examples/pronto"}}}
```

**Note**: Default count changed from 10 to 1 per user request.

## Development Workflow

1. Start with WebSocket connection test
2. Verify binary payload reception
3. Implement file saving
4. Add Pronto conversion
5. Build CLI interface
6. Add error handling
7. Write tests
8. Document usage

## Success Criteria ✅

- [x] Successfully connects to wss://sych.local/ws/ws_state
- [x] Captures specified number of payloads (default: 1)
- [x] Saves both .bin and .edn files with matching timestamps
- [x] Handles connection errors gracefully
- [x] Provides clear user feedback
- [x] Clean, maintainable code structure
- [x] Comprehensive documentation
- [x] Easy to use via Makefile

## Notes

- Use proto-explorer as reference for project structure (Claude AI: use proto-class-explorer agent instead of direct tool use)
- Keep tool focused and simple
- Prioritize reliability over features
- Make output easily analyzable
- Consider future extension for cmd endpoint

## Future Enhancements (Out of Scope)

- Real-time EDN viewer
- Payload filtering
- Statistical analysis
- Diff between payloads
- Web UI for browsing captures
- Integration with proto-explorer (Claude AI: use proto-class-explorer agent)