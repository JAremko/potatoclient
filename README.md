# PotatoClient

Multi-process video streaming client with dual H.264 WebSocket streams.

## Features

- Dual video streams: Heat (900x720) and Day (1920x1080)
- Hardware-accelerated H.264 decoding
- Cross-platform: Windows, macOS, Linux
- Dark/Light themes (Sol Dark, Sol Light, Dark, Hi-Dark)
- Multilingual: English, Ukrainian
- Real-time event logging with filtering
- Comprehensive runtime validation with Malli schemas

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
- Hardware decoder: NVIDIA, Intel QSV, VA-API, or VideoToolbox

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

## Architecture

```
┌─────────────────┐     JSON/IPC      ┌──────────────────┐
│  Main Process   │ ←───────────────→ │ Stream Process 1 │
│   (Clojure)     │                   │     (Java)       │
│  - UI (Swing)   │                   │ - WebSocket      │
│  - State Mgmt   │                   │ - GStreamer      │
│  - Config       │                   └──────────────────┘
└─────────────────┘                   ┌──────────────────┐
                                      │ Stream Process 2 │
                                      └──────────────────┘
```

## Development

```bash
# Build tasks
make clean        # Clean all artifacts
make proto        # Generate protobuf classes
make test         # Run tests
make release      # Build optimized release JAR

# Platform packages
make build-windows  # .exe installer
make build-macos    # .dmg bundle
make build-linux    # AppImage
```

### Build Types

PotatoClient has two distinct build types:

**Development Build** (`make build`, `make dev`):
- Malli instrumentation available for manual activation
- File logging enabled (logs to `logs/` directory)
- Shows `[DEVELOPMENT]` in window title
- Console output: `"Running DEVELOPMENT build - instrumentation available"`

**Release Build** (`make release`):
- No instrumentation overhead
- File logging disabled
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
