# PotatoClient

High-performance multi-process video streaming client with dual H.264 WebSocket streams. Uses Transit-based IPC to isolate protobuf handling in Kotlin subprocesses, ensuring clean separation of concerns and type safety.

## Features

- Dual video streams: Heat (900x720) and Day (1920x1080)
- Hardware-accelerated H.264 decoding with automatic fallback
- Zero-allocation video streaming on the hot path
- Transit-based IPC for all subprocess communication
- Complete protobuf isolation in Kotlin subprocesses
- Individual subprocess logging in development mode
- Cross-platform: Windows, macOS, Linux
- Dark/Light themes (Sol Dark, Sol Light, Dark, Hi-Dark)
- Multilingual: English, Ukrainian
- Smart logging: file logging in dev, console-only in production
- Comprehensive runtime validation with Malli and Guardrails
- Optimized Kotlin implementation for video processing

## Quick Start

```bash
# Build and run
make build
make run

# Development
make dev          # With GStreamer debug
make dev-reflect  # With reflection warnings
make nrepl        # REPL on port 7888
```

## Requirements

- Java 17+ with `--enable-native-access=ALL-UNNAMED`
- GStreamer 1.0+ with H.264 support
- Hardware decoder: NVIDIA (nvh264dec), Intel QSV, VA-API, or VideoToolbox
- Kotlin 2.2.0 (bundled for builds)

**Note**: SSL certificate validation is currently disabled for all builds as this is an internal application. This will be configurable in future releases.

## Configuration

Settings stored in platform-specific locations:
- Linux: `~/.config/potatoclient/potatoclient-config.edn`
- macOS: `~/Library/Application Support/PotatoClient/`
- Windows: `%LOCALAPPDATA%\PotatoClient\`

```clojure
{:theme :sol-dark     ; :sol-dark, :sol-light, :dark, :hi-dark
 :domain "sych.local" ; WebSocket server
 :locale :english}    ; :english or :ukrainian
```

## Performance

PotatoClient is optimized for high-performance video streaming:

### Zero-Allocation Streaming
- **Lock-free buffer pools** for video frames
- **Pre-allocated objects** for event handling
- **Direct ByteBuffers** for optimal native interop
- **Fast path optimization** for single-fragment WebSocket messages

### Hardware Acceleration
- **Automatic decoder selection** (NVIDIA > Intel QSV > VA-API > Software)
- **Direct pipeline** without unnecessary color conversions
- **Try-lock patterns** to avoid blocking the video pipeline

### Kotlin Optimizations
- **Inline functions** for hot path code (where applicable)
- **Volatile fields** for lock-free status checks
- **Thread-local storage** for event objects
- **Pre-calculated values** to avoid repeated computations

## Architecture

```
┌─────────────────┐     Transit/IPC    ┌──────────────────┐
│  Main Process   │ ←────────────────→ │ Command Process  │
│   (Clojure)     │                    │    (Kotlin)      │
│  - UI (Swing)   │                    │ - Protobuf       │
│  - State Mgmt   │                    │ - WebSocket      │
│  - Transit IPC  │                    └──────────────────┘
│  - No Protobuf  │     Transit/IPC    ┌──────────────────┐
└─────────────────┘ ←────────────────→ │ State Process    │
         ↑                             │    (Kotlin)      │
         │                             │ - Protobuf       │
         │         Transit/IPC         │ - Debouncing     │
         ├────────────────────────────→└──────────────────┘
         │                             ┌──────────────────┐
         └────────────────────────────→│ Video Stream 1   │
                                       │    (Kotlin)      │
                   Transit/IPC         │ - H.264/GStreamer│
         └────────────────────────────→│ - Zero-alloc     │
                                       └──────────────────┘
                                       ┌──────────────────┐
                                       │ Video Stream 2   │
                                       └──────────────────┘
```

**Key Architecture Principles:**
- **Protobuf Isolation**: All protobuf handling is isolated in Kotlin subprocesses
- **Transit Protocol**: All IPC uses Transit/MessagePack for type safety
- **Clean Separation**: Main process never touches protobuf directly
- **Unified Communication**: All subprocesses use the same Transit message protocol
- **Graceful Shutdown**: All subprocesses automatically terminate when main process exits

### Command System

PotatoClient uses Transit-based command routing with protobuf isolation:

- **Transit Commands**: Commands created as Transit maps in Clojure
- **Command Subprocess**: Converts Transit to protobuf and sends via WebSocket
- **State Subprocess**: Receives protobuf state, converts to Transit for UI
- **Unified Protocol**: All subprocesses use the same Transit message format

```clojure
;; Commands are sent as Transit messages
(require '[potatoclient.transit.commands :as cmd])

;; Send ping command
(cmd/send-ping!)

;; Platform control
(cmd/send-command! {:action "platform-azimuth" :value 45.0})

;; State updates flow back via Transit
;; The UI automatically updates from the Transit app-db
```

**Transit Message Types:**
- `command` - Control commands to server
- `state` - State updates from server
- `log` - Subprocess logging messages
- `error` - Error reports with stack traces
- `metric` - Performance metrics
- `event` - UI events (navigation, window, frame)
- `status` - Process lifecycle status

## Development

```bash
# Build tasks
make clean        # Clean all artifacts (including Kotlin classes)
make proto        # Generate protobuf classes (cleans old bindings first)
make compile-kotlin # Compile Kotlin sources
make build        # Build JAR with all dependencies

# Testing
make test         # Run tests with automatic logging
make test-summary # View latest test results
make coverage     # Generate test coverage report
make lint         # Run all linters with filtered reports

# Release builds
make release      # Build optimized release JAR
make build-windows  # .exe installer
make build-macos    # .dmg bundle
make build-linux    # AppImage
```

### Testing Infrastructure

- **Automated Test Logging**: All test runs saved to `logs/test-runs/` with timestamps
- **Coverage Reports**: HTML/XML coverage reports via jacoco (`make coverage`)
- **WebSocket Stubbing**: Fast, deterministic tests without real servers
- **Property-Based Testing**: Comprehensive validation with Malli generators
- **Test Analysis**: Automatic log compaction and failure extraction

The project includes a comprehensive test suite with protobuf validation:

```bash
# Run all tests
make test

# Run specific test categories
clojure -M:test -n potatoclient.cmd.comprehensive-command-test
clojure -M:test -n potatoclient.cmd.generator-test
clojure -M:test -n potatoclient.cmd.validation-safety-test
```

The test suite includes:
- Comprehensive command validation tests
- Property-based testing with Malli generators
- Transit communication integration tests
- WebSocket stubbing for deterministic testing

### Build Types

PotatoClient has two distinct build types:

**Development Build** (`make build`, `make dev`):
- Guardrails validation enabled for all functions
- Full logging to console and `./logs/potatoclient-{version}-{timestamp}.log`
- Individual subprocess log files in `./logs/{subprocess}-{timestamp}.log`
- All log levels: DEBUG, INFO, WARN, ERROR
- Shows `[DEVELOPMENT]` in window title
- SSL certificate validation disabled (for internal use)
- Console output: `"Running DEVELOPMENT build - Guardrails validation enabled"`

**Release Build** (`make release`):
- No Guardrails overhead - completely removed from bytecode
- Minimal logging: only WARN/ERROR to platform-specific locations
- No subprocess file logging
- AOT compilation with direct linking
- Optimized for performance
- Shows `[RELEASE]` in window title
- SSL certificate validation disabled (for internal use)
- Console output: `"Running RELEASE build"`
- Self-contained: Release JARs automatically detect they're release builds

### Runtime Validation

PotatoClient uses [Guardrails](https://github.com/fulcrologic/guardrails) with [Malli](https://github.com/metosin/malli) for comprehensive runtime validation:

- **Guardrails**: Function spec validation with `>defn` and `>defn-`
- **Automatic in Dev**: Validation enabled automatically in development
- **Zero overhead in Release**: Guardrails completely removed from bytecode
- **Better error messages**: Human-readable validation errors with precise specs
- **Centralized schemas**: All data schemas in `potatoclient.specs`

#### Function Development

All functions must use Guardrails:

```clojure
(require '[com.fulcrologic.guardrails.malli.core :refer [>defn >defn- =>]])

;; Public function with validation
(>defn process-data
  [data options]
  [map? map? => map?]  ; specs for args and return
  (merge data options))

;; Private function
(>defn- validate-input
  [input]
  [string? => boolean?]
  (not (clojure.string/blank? input)))
```

### Logging System

PotatoClient uses [Telemere](https://github.com/taoensso/telemere) for high-performance logging:

**Development Mode**:
- Logs all levels (DEBUG, INFO, WARN, ERROR) to console
- Main process log: `./logs/potatoclient-{version}-{timestamp}.log`
- Subprocess logs: `./logs/{subprocess-name}-{timestamp}.log`
  - `command-subprocess-{timestamp}.log`
  - `state-subprocess-{timestamp}.log`
  - `video-stream-heat-{timestamp}.log`
  - `video-stream-day-{timestamp}.log`
- Automatic log rotation (keeps last 10 files per subprocess)
- All subprocess logs also sent to main process via Transit

**Production Mode**:
- Only critical messages (WARN, ERROR) to platform-specific locations:
  - Linux: `~/.local/share/potatoclient/logs/`
  - macOS: `~/Library/Application Support/PotatoClient/logs/`
  - Windows: `%LOCALAPPDATA%\PotatoClient\logs\`
- No subprocess file logging
- Zero performance overhead from debug logging

**Checking Logs**:
```bash
# Development: Check all logs
ls -la ./logs/
tail -f ./logs/*.log

# Production: Check platform-specific location
# Linux example:
tail -f ~/.local/share/potatoclient/logs/*.log
```

## Developer Documentation

For detailed implementation information, see:

- **[CLAUDE.md](CLAUDE.md)** - Complete developer guide with architecture details
- **[.claude/transit-architecture.md](.claude/transit-architecture.md)** - Transit IPC implementation
- **[.claude/transit-quick-reference.md](.claude/transit-quick-reference.md)** - Quick reference for Transit commands
- **[.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md)** - Kotlin subprocess details
- **[.claude/protobuf-command-system.md](.claude/protobuf-command-system.md)** - Command system design
- **[.claude/subprocess-communication-plan.md](.claude/subprocess-communication-plan.md)** - Unified subprocess architecture
- **[.claude/linting-guide.md](.claude/linting-guide.md)** - Code quality and linting tools
