# Mock Video Stream Tool

A development tool that simulates PotatoClient's video streaming server for testing without hardware dependencies.

## Overview

The Mock Video Stream tool provides:
- WebSocket server simulating real hardware endpoints
- State management with protobuf messages
- Simulated H.264 video streams (solid colors)
- Command processing and validation
- Scenario-based testing
- Performance profiling

## Architecture

```
┌─────────────────┐         WebSocket          ┌──────────────────┐
│  PotatoClient   │ ←───────────────────────→  │  Mock Server     │
│                 │                             │                  │
│  - Commands     │ ──→ Protobuf Commands ──→  │  - State Mgmt    │
│  - State Sub    │ ←── Protobuf State    ←──  │  - Video Gen     │
│  - Video Subs   │ ←── H.264 Frames      ←──  │  - Scenarios     │
└─────────────────┘                             └──────────────────┘
```

## Quick Start

```bash
cd tools/mock-video-stream

# Terminal 1: Start mock server
make start-server

# Terminal 2: Run PotatoClient
make dev

# Or run a specific scenario
make scenario SCENARIO=zoom-cycle
```

## Server Endpoints

The mock server provides these WebSocket endpoints:

- `ws://localhost:8080/state` - State updates (protobuf)
- `ws://localhost:8080/command` - Command reception (protobuf)
- `ws://localhost:8080/stream/heat` - Heat camera H.264 stream
- `ws://localhost:8080/stream/day` - Day camera H.264 stream

## Configuration

### Server Settings

Edit `resources/config.edn`:

```clojure
{:server {:host "0.0.0.0"
          :port 8080
          :ssl? false}
          
 :video {:heat {:width 900
                :height 720
                :fps 30
                :color "#FF6B6B"}  ; Red
                
         :day {:width 1920
               :height 1080
               :fps 30
               :color "#4ECDC4"}}  ; Teal
               
 :state {:update-interval-ms 100
         :initial-state {:rotary {:azimuth 0.0
                                 :elevation 0.0
                                 :azimuth-rate 0.0
                                 :elevation-rate 0.0}
                        :cameras {:heat {:zoom-level 0
                                       :focus-mode :auto}
                                 :day {:zoom-level 0
                                      :focus-mode :auto}}}}}
```

### Video Stream Colors

Customize stream colors for different test scenarios:

```clojure
;; Distinct colors for multi-stream testing
{:video {:heat {:color "#FF0000"}   ; Pure red
         :day {:color "#00FF00"}}}   ; Pure green

;; Dark colors for performance testing  
{:video {:heat {:color "#1a1a1a"}   ; Near black
         :day {:color "#2a2a2a"}}}
```

## Command Support

The mock server handles all PotatoClient commands:

### Platform Commands

```clojure
;; Rotary control
{:rotary {:goto-ndc {:x 0.5 :y -0.5}}}
{:rotary {:set-velocity {:velocity 0.1}}}
{:rotary {:stop {}}}

;; Computer Vision
{:cv {:start-track-ndc {:channel :heat :x 0.5 :y 0.5}}}
{:cv {:stop-track {}}}
```

### Camera Commands

```clojure
;; Zoom control
{:heat-camera {:next-zoom-table-pos {}}}
{:day-camera {:prev-zoom-table-pos {}}}

;; Focus control
{:day-camera {:set-focus-mode {:mode :manual}}}
```

### System Commands

```clojure
;; Recording
{:system {:start-rec {}}}
{:system {:stop-rec {}}}

;; Configuration
{:gui {:select-video-channel {:channel :heat}}}
```

## Testing Scenarios

### Running Scenarios

```bash
# List available scenarios
make list-scenarios

# Run specific scenario
make scenario SCENARIO=rapid-zoom

# Run with custom timing
SCENARIO_DELAY=500 make scenario SCENARIO=pan-sequence
```

### Creating Scenarios

Create EDN files in `resources/scenarios/`:

```clojure
;; resources/scenarios/my-test.edn
{:name "My Test Scenario"
 :description "Test specific behavior"
 :delay-ms 100  ; Delay between commands
 
 :commands [{:rotary {:goto-ndc {:x 0.0 :y 0.0}}}
            {:wait 1000}  ; Wait 1 second
            {:cv {:start-track-ndc {:channel :heat :x 0.5 :y 0.5}}}
            {:wait 2000}
            {:cv {:stop-track {}}}]
            
 :expected-states [{:path [:rotary :azimuth] :value 0.0}
                   {:path [:cv :tracking?] :value true}
                   {:path [:cv :tracking?] :value false}]
                   
 :assertions [{:type :state-value
               :path [:cv :track-quality]
               :predicate #(> % 0.8)}]}
```

### Scenario Commands

Special commands for testing:

```clojure
{:wait 1000}                    ; Pause execution
{:assert-state [:path] value}   ; Verify state
{:set-state [:path] value}      ; Override state
{:disconnect :command}          ; Simulate disconnect
{:corrupt-next-frame :heat}     ; Send bad frame
```

## State Management

### State Structure

The mock server maintains state matching the real system:

```clojure
{:rotary {:azimuth 45.0          ; Current position
          :elevation 10.0
          :azimuth-rate 0.1      ; Current velocity
          :elevation-rate 0.0}
          
 :cv {:tracking? false
      :track-id nil
      :track-quality 0.0
      :target-x 0.0
      :target-y 0.0}
      
 :cameras {:heat {:zoom-level 2    ; 0-4
                  :zoom-fov 15.0
                  :recording? false}
           :day {:zoom-level 0
                 :focus-mode :auto
                 :recording? false}}
                 
 :system {:recording? false
          :recording-time 0
          :selected-channel :heat}}
```

### State Updates

State updates are sent at configured intervals:

```clojure
;; Smooth position updates
(defn update-rotary-position [state dt]
  (-> state
      (update-in [:rotary :azimuth] 
                 #(+ % (* (:azimuth-rate (:rotary state)) dt)))
      (update-in [:rotary :elevation]
                 #(+ % (* (:elevation-rate (:rotary state)) dt)))))
```

## Performance Testing

### Load Testing

```bash
# High frequency state updates
make start-server STATE_HZ=100

# Multiple clients
make stress-test CLIENTS=10

# Profile server performance
make profile
```

### Metrics

The server logs performance metrics:

```
INFO [mock-video-stream.server] Metrics:
  Commands processed: 1523
  State updates sent: 3000
  Video frames sent: 1800 (heat), 1800 (day)
  Average latency: 2.3ms
  Memory usage: 125MB
```

## Development

### Adding Commands

1. Update command handler in `handle-command`:

```clojure
(defmethod handle-command :new-system
  [state {:keys [new-system]}]
  (case (first (keys new-system))
    :configure (handle-configure state (:configure new-system))
    :reset (reset-system state)
    state))
```

2. Update state transformation:

```clojure
(defn handle-configure [state config]
  (-> state
      (assoc-in [:new-system :config] config)
      (assoc-in [:new-system :configured?] true)))
```

### Custom Video Generation

Replace solid colors with patterns:

```clojure
(defn generate-test-pattern-frame [width height frame-number]
  (let [pixels (byte-array (* width height 3))]
    ;; Generate moving gradient
    (doseq [y (range height)
            x (range width)]
      (let [idx (* 3 (+ (* y width) x))
            r (int (/ (* x 255) width))
            g (int (/ (* y 255) height))
            b (mod frame-number 255)]
        (aset pixels idx (unchecked-byte r))
        (aset pixels (+ idx 1) (unchecked-byte g))
        (aset pixels (+ idx 2) (unchecked-byte b))))
    pixels))
```

### Debugging

Enable verbose logging:

```clojure
;; In REPL
(require '[taoensso.timbre :as log])
(log/set-level! :debug)

;; Or via environment
LOG_LEVEL=debug make start-server
```

## Integration with PotatoClient

### Configuration

Point PotatoClient to mock server:

```clojure
;; In PotatoClient config
{:domain "localhost:8080"
 :use-ssl? false}
```

### Verification

Check connections in PotatoClient logs:

```
INFO [potatoclient.process] Starting command subprocess
INFO [potatoclient.process] Starting state subprocess  
INFO [command-subprocess] Connected to ws://localhost:8080/command
INFO [state-subprocess] Connected to ws://localhost:8080/state
INFO [video-stream-heat] Connected to ws://localhost:8080/stream/heat
```

## Troubleshooting

### Server Won't Start

```bash
# Check port availability
lsof -i :8080

# Use different port
make start-server PORT=8090
```

### No Video Display

- Verify H.264 encoding works: `make test-encode`
- Check GStreamer installation
- Try different colors (some codecs struggle with solid black)

### State Not Updating

- Check WebSocket connections in browser: http://localhost:8080/status
- Verify protobuf generation is current: `make proto`
- Enable debug logging for state namespace

## Best Practices

1. **Use scenarios** for reproducible tests
2. **Monitor metrics** during load testing
3. **Test edge cases** (disconnects, corruption)
4. **Vary timing** to catch race conditions
5. **Profile regularly** to catch regressions

## See Also

- [Debugging Subprocesses](../guides/debugging-subprocesses.md)
- [Command System](../architecture/command-system.md)
- [Video Streaming](../architecture/video-streaming.md)