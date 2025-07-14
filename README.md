# PotatoClient

Multi-process video streaming client with dual H.264 WebSocket streams.

## Features

- Dual video streams: Heat (900x720) and Day (1920x1080)
- Hardware-accelerated H.264 decoding
- Cross-platform: Windows, macOS, Linux
- Dark/Light themes (Sol Dark, Sol Light)
- Multilingual: English, Ukrainian
- Real-time event logging with filtering

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

See [CLAUDE.md](CLAUDE.md) for development guide.