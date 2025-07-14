# PotatoClient

Multi-process video streaming client with dual H.264 WebSocket streams.

## Features

- Dual video streams: Heat (900x720) and Day (1920x1080)
- Hardware-accelerated H.264 decoding
- Cross-platform: Windows, macOS, Linux
- Dark/Light themes (Sol Dark, Sol Light)
- Multilingual: English, Ukrainian
- Real-time event logging with filtering
- Comprehensive runtime validation with Orchestra & clojure.spec

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
{:theme :sol-dark     ; :sol-dark or :sol-light
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

# Platform packages
make build-windows  # .exe installer
make build-macos    # .dmg bundle
make build-linux    # AppImage
```

### Build Types

PotatoClient has two distinct build types:

**Development Build** (`make build`, `make dev`):
- Full Orchestra instrumentation enabled
- Runtime validation of all function specs
- Shows `[DEVELOPMENT]` in window title
- Console output: `"Running DEVELOPMENT build - enabling instrumentation..."`

**Release Build** (CI builds, `make release`):
- Orchestra instrumentation disabled
- AOT compilation with direct linking
- Optimized for performance
- Shows `[RELEASE]` in window title
- Console output: `"Running RELEASE build - instrumentation disabled"`

### Runtime Validation

PotatoClient uses [Orchestra](https://github.com/jeaye/orchestra) for comprehensive spec instrumentation:

- **Development builds**: Full validation of function inputs, outputs, and relationships
- **Release builds**: Zero overhead - instrumentation automatically disabled
- **Every function** is spec'd using `defn-spec` for type safety and documentation

See [CLAUDE.md](CLAUDE.md) for detailed development guide.