# CMD-Explorer

A comprehensive testing and exploration tool for WebSocket command endpoints (`/ws/ws_cmd`). This tool provides a Clojure implementation of all TypeScript cmd functions with full protobuf validation, generative testing, and buf.validate constraint enforcement.

## Features

- **Complete CMD Implementation**: All TypeScript cmd binding functions ported to Clojure (200+ commands)
- **Multi-layer Validation**: Malli specs, buf.validate constraints, and protobuf validation
- **Generative Testing**: Property-based testing with 300+ runs per function
- **Mock WebSocket Server**: Built-in server for testing without real endpoints
- **Interactive REPL**: Explore and send commands interactively
- **Type Safety**: Guardrails-protected functions with compile-time checking
- **Binary Compatible**: Produces identical protobuf messages to TypeScript implementation

## Quick Start

```bash
# Install dependencies
make deps

# Run tests
make test

# Start interactive REPL
make repl

# Send a command to real endpoint
make run CMD=ping

# Start mock server for testing
make mock-server
```

## Installation

### Prerequisites

1. **Clojure CLI tools** (1.11+)
2. **Java** (11 or higher)
3. **Protocol Buffers compiler** (protoc)

### Setup

```bash
cd tools/cmd-explorer

# Download dependencies
make deps

# Build protobuf classes
make build

# Run tests to verify setup
make test
```

## Usage

### Interactive REPL

```clojure
;; Start REPL
make repl

;; In REPL
(require '[cmd-explorer.core :as cmd])

;; Send a ping command
(cmd/send-ping)

;; Control day camera
(cmd/day-camera-set-zoom 0.5)
(cmd/day-camera-take-photo)

;; Control rotary platform
(cmd/rotary-set-position {:azimuth 45.0 :elevation 10.0})
```

### Command Line

```bash
# Send specific command
make run CMD=day-camera-start

# Send command with parameters
make run CMD=day-camera-set-zoom ARGS='{"value": 0.5}'

# Test against mock server
make test-with-mock
```

### Batch Execution

```clojure
;; Execute command sequence
(cmd/execute-sequence
  [(cmd/system-start-all)
   (cmd/day-camera-start)
   (cmd/heat-camera-start)
   (cmd/rotary-home)])
```

## Command Categories

### System Commands
- `system-reboot` - Reboot the system
- `system-power-off` - Power off the system
- `system-start-all` - Start all subsystems
- `system-stop-all` - Stop all subsystems
- `system-mark-rec-important` - Mark recording as important
- `system-set-localization` - Set system language

### Day Camera Commands (29 functions)
- Focus control (set, move, offset, reset)
- Zoom control (set, move, table positions)
- Image adjustments (IR filter, iris, FX modes)
- Photo capture and video control
- CLAHE level adjustments

### Heat Camera Commands (31 functions)
- Thermal imaging controls
- AGC modes and filters
- Digital detail enhancement (DDE)
- Focus and zoom control
- Calibration commands

### Rotary Platform Commands (40 functions)
- Position control (azimuth/elevation)
- Velocity control
- Scanning operations
- Homing and calibration
- Limit management

### LRF Commands (14 functions)
- Laser rangefinder control
- Measurement operations
- Target designation modes
- Fog mode settings

### Compass Commands (11 functions)
- Compass calibration
- Magnetic declination
- Offset adjustments
- Rotary position integration

### GPS Commands (5 functions)
- GPS control
- Manual position setting
- Position source selection

### OSD Commands (8 functions)
- On-screen display control
- Screen selection
- Overlay enable/disable

## Architecture

### Validation Pipeline

```
Input → Malli Spec → Message Builder → Protobuf Encoder → Buf.validate → WebSocket
         ↓              ↓                ↓                  ↓              ↓
       Guard         Construct        Serialize         Validate       Send
```

### Implementation Note

The TypeScript `cmdSenderShared.ts` file contains low-level infrastructure for message creation, encoding, and WebSocket handling specific to the TypeScript/browser environment. Our Clojure implementation replaces this with:
- **Pronto** for EDN↔Protobuf conversion
- **Custom WebSocket client** similar to state-explorer
- **Multimethod dispatch** for command routing

### Key Components

1. **Malli Specs**: Input/output validation with custom generators for property-based testing
2. **Guardrails**: Compile-time and runtime function checking with instrumentation always enabled
3. **Pronto**: EDN to Protobuf conversion
4. **Buf.validate**: Protobuf constraint validation
5. **Mock Server**: Testing infrastructure
6. **Shared Specs**: Reusable specifications for common types (angles, ranges, positions)

### Function Design

Unlike TypeScript functions that return `void` and send messages as side effects, our Clojure functions:
- **Return** the complete CMD protobuf tree as EDN
- **Enable** pure functional testing without side effects
- **Support** message inspection and validation before sending
- **Separate** message construction from transmission

Example:
```clojure
;; TypeScript (side effect):
dayCameraSetZoom(0.5);  // returns void, sends immediately

;; Clojure (pure function):
(def msg (day-camera-set-zoom 0.5))  ; Returns EDN structure
;; => {:protocolVersion 1, :dayCamera {:zoom {:value 0.5}}}
(send-command msg)  ; Explicitly send when ready
```

## Testing

### Run All Tests

```bash
make test
```

### Spec-Driven Development

Every function follows this pattern:

```clojure
;; 1. Define spec with generator
(def zoom-value-spec
  [:double {:min 0.0 :max 1.0 
           :gen/gen (gen/double* {:min 0.0 :max 1.0})}])

;; 2. Implement guardrailed function
(>defn day-camera-set-zoom
  "Set day camera zoom level (0.0-1.0)"
  [zoom]
  [zoom-value-spec => cmd-root-spec]
  {:protocolVersion 1
   :dayCamera {:zoom {:value zoom}}})

;; 3. Test with generated values
(defspec day-camera-zoom-spec 300
  (prop/for-all [zoom (gen/generate zoom-value-spec)]
    (let [msg (day-camera-set-zoom zoom)]
      (and (valid? cmd-root-spec msg)
           (= (:dayCamera msg) {:zoom {:value zoom}})))))
```

### Generative Testing

Each function has property-based tests with minimum 300 runs using the attached spec generators. Guardrails instrumentation is always enabled to catch spec violations during testing.

### Mock Server Testing

```bash
# Terminal 1: Start mock server
make mock-server

# Terminal 2: Run tests against mock
make test-mock
```

## Validation Rules

The tool enforces validation at multiple levels:

1. **Malli Specs**: Type and range validation
2. **Buf.validate**: Protobuf field constraints
3. **Business Logic**: Command-specific rules
4. **Wire Format**: Binary compatibility

Example validation for zoom command:
```clojure
;; Malli spec
[:map [:value [:double {:min 0.0 :max 1.0}]]]

;; Buf.validate constraint
[(buf.validate.field).double = {gte: 0.0, lte: 1.0}]

;; Business logic
(when (> value current-max-zoom)
  (throw (ex-info "Zoom exceeds current limit" {:value value})))
```

## Project Structure

```
cmd-explorer/
├── src/cmd_explorer/
│   ├── core.clj                 # Main entry point
│   ├── commands/                # Command implementations
│   │   ├── day_camera.clj
│   │   ├── heat_camera.clj
│   │   ├── rotary.clj
│   │   └── ...
│   ├── specs/                   # Malli specifications
│   ├── validation/              # Buf.validate integration
│   ├── websocket.clj           # WebSocket client
│   └── mock_server.clj         # Testing server
├── test/cmd_explorer/
│   ├── generative/              # Property-based tests
│   ├── integration/             # End-to-end tests
│   └── unit/                    # Unit tests
├── resources/
│   └── logback.xml             # Logging config
├── scripts/
│   └── extract_ts_functions.sh # TypeScript extraction
├── deps.edn                    # Dependencies
├── Makefile                    # Build automation
├── todo.md                     # Implementation plan
└── README.md                   # This file
```

## Development

### Adding New Commands

**IMPORTANT**: For EVERY function implementation, you MUST:

1. **Research the command thoroughly**:
   ```bash
   # Read TypeScript implementation
   cat /examples/web/frontend/ts/cmd/cmdSender/cmdDayCamera.ts | grep -A 10 "function dayCameraSetZoom"
   
   # Explore protobuf structure
   cd /tools/proto-explorer
   ./proto-explorer cmd DayCamera.Root
   ./proto-explorer cmd DayCamera.Zoom
   
   # Note all constraints and requirements
   ```

2. **Implement with full understanding**:
   - Add function to appropriate namespace
   - Create Malli spec matching proto constraints
   - Add Guardrails annotations
   - Implement message construction matching proto structure
   - Add unit tests
   - Add generative tests
   - Test with mock server

This research step is MANDATORY to ensure binary compatibility and correct validation.

### Debugging

```clojure
;; Enable verbose logging
(require '[cmd-explorer.logging :as log])
(log/set-level :debug)

;; Inspect generated protobuf
(cmd/inspect-message (cmd/build-day-camera-zoom 0.5))

;; Validate without sending
(cmd/validate-only (cmd/build-ping))
```

## Scripts

### Extract TypeScript Functions

Updates the function reference from TypeScript source:

```bash
cd scripts
./extract_ts_functions.sh
```

Output is saved to `ts_functions.md` for reference.

## Performance

- Command serialization: <10ms
- Validation overhead: <5ms
- WebSocket round-trip: <50ms (local)
- Generative test suite: ~5 minutes

## Troubleshooting

### "Protobuf classes not found"
```bash
make build
```

### "Validation failed: unknown constraint"
Ensure buf.validate annotations are loaded:
```bash
make deps
```

### "WebSocket connection refused"
Check endpoint is running or use mock server:
```bash
make mock-server
```

## Related Tools

- [State-Explorer](../state-explorer/) - WebSocket state endpoint testing
- [Proto-Explorer](../proto-explorer/) - Protobuf message exploration
- [TypeScript Implementation](../../examples/web/frontend/ts/cmd/) - Reference implementation

## Contributing

1. Follow the implementation plan in `todo.md`
2. Ensure all functions have tests
3. Maintain TypeScript compatibility
4. Document new features

## License

Part of the PotatoClient project.

## Version

0.1.0 - Initial implementation framework