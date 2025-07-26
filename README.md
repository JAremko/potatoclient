# PotatoClient

High-performance multi-process video streaming client with dual H.264 WebSocket streams. Features zero-allocation streaming and hardware-accelerated decoding.

## Features

- Dual video streams: Heat (900x720) and Day (1920x1080)
- Hardware-accelerated H.264 decoding with automatic fallback
- Zero-allocation video streaming on the hot path
- Cross-platform: Windows, macOS, Linux
- Dark/Light themes (Sol Dark, Sol Light, Dark, Hi-Dark)
- Multilingual: English, Ukrainian
- Smart logging: file logging in dev, console-only in production
- Comprehensive runtime validation with Malli schemas
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
┌─────────────────┐     JSON/IPC      ┌──────────────────┐
│  Main Process   │ ←───────────────→ │ Stream Process 1 │
│   (Clojure)     │                   │    (Kotlin)      │
│  - UI (Swing)   │                   │ - WebSocket      │
│  - State Mgmt   │                   │ - GStreamer      │
│  - Config       │                   │ - Zero-alloc     │
│  - Commands     │                   └──────────────────┘
└─────────────────┘                   ┌──────────────────┐
                                      │ Stream Process 2 │
                                      └──────────────────┘
```

### Command System

PotatoClient includes a command system for sending control messages via Protobuf:

- **Command Infrastructure**: Core async channel-based message routing
- **Rotary Platform**: Full gimbal control (azimuth, elevation, scanning)
- **Camera Control**: Zoom, focus, exposure, image enhancement
- **Read-only Mode**: Safety mode allowing only ping/frozen commands
- **JSON Output**: Commands serialized as Base64-encoded Protobuf

```clojure
;; Initialize command system
(require '[potatoclient.cmd.core :as cmd])
(cmd/init!)

;; Send commands
(require '[potatoclient.cmd.rotary :as rotary])
(rotary/rotary-start)
(rotary/set-platform-azimuth 45.0)

;; Camera control
(require '[potatoclient.cmd.day-camera :as camera])
(camera/zoom-in)
(camera/set-stabilization true)
```

## Development

```bash
# Build tasks
make clean        # Clean all artifacts
make proto        # Generate protobuf classes (cleans old bindings first)
make compile-kotlin # Compile Kotlin sources
make test         # Run tests
make release      # Build optimized release JAR

# Platform packages
make build-windows  # .exe installer
make build-macos    # .dmg bundle
make build-linux    # AppImage
```

### Testing

The project includes a comprehensive test suite with protobuf validation:

```bash
# Run all tests
make test

# Run specific test categories
clojure -M:test -n potatoclient.cmd.comprehensive-command-test
clojure -M:test -n potatoclient.cmd.generator-test
clojure -M:test -n potatoclient.cmd.validation-safety-test
```

See [docs/TESTING_AND_VALIDATION.md](docs/TESTING_AND_VALIDATION.md) for:
- Test organization and structure
- Protobuf validation with `buf.validate` annotations
- TypeScript reference implementation in `examples/web/frontend/ts/`
- Debugging test failures

### Build Types

PotatoClient has two distinct build types:

**Development Build** (`make build`, `make dev`):
- Malli instrumentation available for manual activation
- Full logging to console and `./logs/potatoclient-{version}-{timestamp}.log`
- All log levels: DEBUG, INFO, WARN, ERROR
- Shows `[DEVELOPMENT]` in window title
- Console output: `"Running DEVELOPMENT build - instrumentation available"`

**Release Build** (`make release`):
- No instrumentation overhead
- Minimal logging: only WARN/ERROR to stdout/stderr
- No file logging for clean deployments
- AOT compilation with direct linking
- Optimized for performance
- Shows `[RELEASE]` in window title
- Console output: `"Running RELEASE build - instrumentation disabled"`
- Self-contained: Release JARs automatically detect they're release builds

### Runtime Validation

PotatoClient uses [Malli](https://github.com/metosin/malli) for comprehensive runtime validation:

- **High performance**: Faster than clojure.spec
- **Better error messages**: Human-readable validation errors
- **Development builds**: Instrumentation available via REPL
- **Release builds**: Zero overhead - instrumentation completely excluded
- **Centralized schemas**: All data schemas in `potatoclient.specs`
- **Function instrumentation**: All function schemas in `potatoclient.instrumentation`

#### Using Instrumentation in Development

```clojure
;; In REPL during development:
(require 'potatoclient.instrumentation)
(potatoclient.instrumentation/start!)

;; Now all function calls are validated
;; Invalid calls will throw detailed error messages
```

### Logging System

PotatoClient uses [Telemere](https://github.com/taoensso/telemere) for high-performance logging:

**Development Mode**:
- Logs all levels (DEBUG, INFO, WARN, ERROR) to console
- Creates timestamped log files: `./logs/potatoclient-{version}-{timestamp}.log`
- Example: `./logs/potatoclient-1.5.3-20250716-131710.log`
- Perfect for debugging and development

**Production Mode**:
- Only critical messages (WARN, ERROR) to stdout/stderr
- No file logging for clean deployments
- Zero performance overhead from debug logging
- Ideal for production environments

**Checking Logs**:
```bash
# Development: Check timestamped logs
ls -la ./logs/
tail -f ./logs/potatoclient-*.log

# Production: Monitor console output
./potatoclient | grep -E "WARN|ERROR"
```
