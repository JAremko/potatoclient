# Mock Video Stream Tool

A mock video stream subprocess for testing PotatoClient without requiring real video hardware or GStreamer. This tool simulates the video stream subprocess behavior, generating commands from mouse events and communicating via Transit over stdin/stdout.

## Features

- **Contract Testing**: Validates that video streams send proper commands in the correct format
- **Gesture Simulation**: Converts mouse events to high-level gestures (tap, double-tap, pan)
- **Command Generation**: Produces the same commands as real video streams
- **Test Scenarios**: Pre-defined scenarios with expected outputs
- **Shared NDC Conversion**: Uses the same Java NDC converter as Kotlin code
- **Transit Protocol**: Full Transit/MessagePack communication
- **CLI Interface**: Generate test data, validate scenarios, run examples

## Architecture

```
Mouse Events → Gesture Detection → Command Generation → Transit Messages
     ↓              ↓                    ↓                    ↓
  (x,y,type)   (tap,pan,etc)    (rotary-goto-ndc)    {:msg-type :command}
```

## Usage

### Run as Mock Subprocess

```bash
# Run as heat camera stream
make process STREAM_TYPE=heat

# Run as day camera stream  
make process STREAM_TYPE=day
```

### Generate Test Data

```bash
# Generate all scenarios as JSON files
make generate

# Files are created in ./test-data/
ls test-data/
# tap-center.json, double-tap-track.json, pan-rotate-right.json, etc.
```

### Validate Scenarios

```bash
# Validate all scenarios against specs
make validate
```

### Run Example Scenario

```bash
# List available scenarios
make scenarios

# Run specific scenario
make example SCENARIO=tap-center
make example SCENARIO=pan-diagonal
make example SCENARIO=complex-interaction
```

### Integration Testing

```bash
# Run full integration test suite
make integration-test
```

## Test Scenarios

The tool includes 10+ pre-defined scenarios:

- **tap-center**: Single tap at screen center → rotary-goto-ndc
- **tap-top-left**: Tap at corner → NDC coordinates (-1, 1)
- **double-tap-track**: Double tap → cv-start-track-ndc with frame time
- **pan-rotate-right**: Pan gesture → set-velocity commands
- **pan-diagonal**: Diagonal pan → azimuth + elevation velocity
- **zoom-in-heat**: Mouse wheel on heat → next-zoom-table-pos
- **zoom-out-day**: Mouse wheel on day → prev-zoom-table-pos
- **complex-interaction**: Multiple gestures in sequence
- **rapid-taps**: Multiple taps (not double-tap)

## Command Format

All commands use the new nested format matching protobuf structure:

```clojure
;; Tap → Goto command
{:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.3}}}

;; Double-tap → Track command
{:cv {:start-track-ndc {:channel :heat :x 0.0 :y 0.0 :frame-time 12345}}}

;; Pan → Velocity command
{:rotary {:set-velocity {:azimuth-speed 0.1
                        :elevation-speed 0.05
                        :azimuth-direction :clockwise
                        :elevation-direction :counter-clockwise}}}

;; Pan stop → Halt
{:rotary {:halt {}}}

;; Zoom
{:heat-camera {:next-zoom-table-pos {}}}
{:day-camera {:prev-zoom-table-pos {}}}
```

## Transit Message Protocol

All communication follows the standard Transit message envelope:

```clojure
{:msg-type :command      ; or :event, :log, :response, etc.
 :msg-id "uuid-string"
 :timestamp 1234567890
 :payload {...}}         ; Command or event data
```

## Development

### REPL Development

```bash
make repl

;; In REPL:
(require '[mock-video-stream.core :as core])
(require '[mock-video-stream.scenarios :as scenarios])
(require '[mock-video-stream.gesture-sim :as gesture])

;; Test gesture detection
(def event {:type :mouse-down :x 400 :y 300 :button 1 :timestamp 1000})
(gesture/detect-gesture event core/state)

;; Run a scenario
(def scenario (scenarios/get-scenario :tap-center))
(gesture/process-event-sequence (:events scenario) core/state)
```

### Running Tests

Due to classpath limitations with external dependencies, tests should be run from the main project root:

```bash
# From main project root
cd /home/jare/git/potatoclient
clojure -M:test -n potatoclient.video.ndc-converter-test

# Or run the standalone test script
cd tools/mock-video-stream
clojure -Sdeps '{:paths ["." "../../target/classes"]}' -M run-specific-tests.clj
```

Note: The warning about external paths is expected and can be ignored.

## Integration with Main App

The mock tool can be used in place of real video streams for testing:

1. **Subprocess Mode**: Run as a subprocess communicating via stdin/stdout
2. **Test Data Generation**: Export scenarios for Kotlin tests
3. **Validation**: Ensure commands match expected format

Example integration test:

```clojure
;; Start mock subprocess
(def proc (process/start-subprocess 
           "clojure" ["-M:process" "--stream-type" "heat"]))

;; Send control message
(transit/write (:stdin proc) 
  {:msg-type :control
   :msg-id "123"
   :timestamp (System/currentTimeMillis)
   :payload {:type :get-status}})

;; Read response
(def response (transit/read (:stdout proc)))
```

## Shared Components

The tool uses shared components from the main project:

- **Specs**: `shared/specs/video/stream.clj` - Malli specs for validation
- **NDC Converter**: `src/java/potatoclient/video/NDCConverter.java` - Pixel/NDC conversion
- **Transit Core**: Uses same Transit configuration as main app

## Troubleshooting

### "Unknown message type" warnings
These are expected for message types the mock doesn't handle. The mock only processes:
- `:control` - Configuration and status queries
- `:request` - Mouse event injection (for testing)

### Commands don't match expected
Check that:
1. Canvas dimensions match (default 800x600)
2. Zoom level is set correctly
3. Stream type matches (heat vs day for zoom commands)

### Transit communication issues
- Ensure MessagePack dependencies are present
- Check that stdout is flushed after writes
- Verify message envelope structure

## Known Limitations

1. **External Path Warnings**: Clojure tools.deps shows deprecation warnings for external paths. These are expected and don't affect functionality.

2. **Test Runner**: The standard test runner has issues with external dependencies. Use the provided test scripts or run from the main project root.

3. **Classpath Management**: The tool depends on shared specs and Java classes from the parent project, which requires careful classpath setup.

## Future Enhancements

- [ ] Swipe gesture support (infrastructure ready, not implemented)
- [ ] Multi-touch simulation
- [ ] Frame timing simulation
- [ ] Network latency simulation
- [ ] Error injection for negative testing
- [ ] Standalone JAR packaging to avoid classpath issues