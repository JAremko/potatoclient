# Clojure Stream Spawner

A Clojure implementation of the video stream spawner that manages heat and day camera streams using Transit-based IPC communication.

## Overview

This tool provides the same functionality as the Kotlin stream-spawner but implemented in idiomatic Clojure:
- Spawns VideoStreamManager processes for heat (thermal) and day (optical) cameras
- Uses Unix Domain Sockets for IPC communication
- Transit msgpack protocol for efficient message serialization
- Direct Java interop with existing `UnixSocketCommunicator` and `SocketFactory` classes
- Java `LinkedBlockingQueue` for thread-safe message queuing

## Architecture

### Key Components

1. **Transit Layer** (`transit.clj`)
   - Handles msgpack serialization/deserialization
   - Automatic Transit Keyword ↔ Clojure keyword conversion
   - Message construction helpers

2. **IPC Server** (`ipc.clj`)
   - Wraps Java `UnixSocketCommunicator` with Transit
   - Thread-based message reading and processing
   - Server pool management for heat/day streams

3. **Process Manager** (`process.clj`)
   - Uses Java `ProcessBuilder` to spawn VideoStreamManager JVMs
   - Stream-specific configuration (heat/day endpoints)
   - Output gobbling threads for stdout/stderr

4. **Coordinator** (`coordinator.clj`)
   - Orchestrates IPC servers and processes
   - State management atom tracking both streams
   - Lifecycle management (start/stop/status)

5. **Event Handlers** (`events.clj`)
   - Multimethod dispatch for event types
   - Formatted logging with stream prefixes
   - Handles: gestures, window events, connections, logs, metrics

## Usage

### Prerequisites

Ensure the Kotlin classes are compiled:
```bash
cd ../..  # Go to project root
make compile-kotlin
```

### Running the Spawner

```bash
# Default (connects to sych.local)
clj -M:run

# Custom host
clj -M:run --host myhost.com

# Debug mode
clj -M:run --debug

# Help
clj -M:run --help
```

### Running Tests

```bash
# Run all tests
clj -M:test

# Run specific test namespace
clj -M:test --namespace clj-stream-spawner.transit-test
```

## Message Protocol

All messages follow the IPC protocol defined in CLAUDE.md:

### Event Messages
```clojure
{:msg-type :event
 :type :gesture/:window/:connection
 :timestamp 1234567890
 ...event-specific-data}
```

### Log Messages
```clojure
{:msg-type :log
 :level :debug/:info/:warn/:error
 :message "Log text"
 :timestamp 1234567890}
```

### Command Messages
```clojure
{:msg-type :command
 :action :close-request/...
 :timestamp 1234567890
 ...command-data}
```

## API Design

The API is specifically designed for heat and day camera streams:

```clojure
;; Initialize coordinator
(coordinator/initialize "sych.local" :debug? true)

;; Start individual streams
(coordinator/start-stream :heat)
(coordinator/start-stream :day)

;; Or start both
(coordinator/start-all-streams)
;; => {:heat true :day true}

;; Check status
(coordinator/get-status)
;; => {:heat {:status :running ...} 
;;     :day {:status :running ...}}

(coordinator/stream-running? :heat)
;; => true

(coordinator/all-streams-running?)
;; => true

;; Send control messages
(coordinator/send-close-request :heat)

;; Shutdown
(coordinator/stop-stream :heat)
(coordinator/stop-all-streams)
(coordinator/shutdown)
```

## Testing Approach

Tests use:
- `matcher-combinators` for expressive assertions
- Java interop testing with actual Unix sockets
- Mocked message queuing via `LinkedBlockingQueue`
- Fixtures for cleanup after each test

## Differences from Kotlin Implementation

### Simplifications
- No intermediate IPC launcher process
- Direct process spawning from Clojure
- Simpler state management with atoms
- No custom thread pools (uses Java threads directly)

### Improvements
- Immutable state management
- Functional event dispatch with multimethods
- REPL-friendly for interactive debugging
- Native Transit support (no custom handlers needed)

### Same Core Functionality
- Unix Domain Socket IPC
- Transit msgpack protocol
- Heat and day stream support
- Process lifecycle management
- Graceful shutdown handling

## Development

### REPL Usage

```clojure
;; Start a REPL
clj -M:dev

;; In REPL
(require '[clj-stream-spawner.coordinator :as c])
(c/initialize "sych.local")
(c/start-stream :heat)
;; ... interact with running stream
(c/stop-all-streams)
```

### Adding New Event Types

Add a new method to the `handle-event` multimethod:

```clojure
(defmethod handle-event :your-event-type
  [stream-type message]
  ;; Handle your event
  )
```

## Troubleshooting

### Socket Already Exists
- The tool cleans up sockets on startup
- Check `/tmp/potatoclient-sockets/` for leftover files

### Process Won't Start
- Ensure Kotlin classes are compiled: `make compile-kotlin`
- Check classpath includes target directories
- Verify GStreamer is installed

### IPC Communication Issues
- Enable debug mode with `--debug` flag
- Check socket file permissions
- Ensure parent PID is passed correctly