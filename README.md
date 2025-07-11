# PotatoClient

Multi-process video streaming client for dual H.264 WebSocket streams with hardware acceleration.

## Features

- Dual video streams: Heat (900x720) and Day (1920x1080)
- Hardware acceleration: NVIDIA, Intel QSV, D3D11, VA-API, VideoToolbox
- Isolated stream processes for stability
- Real-time event logging with color coding
- Protocol Buffer communication

## Quick Start

```bash
# Prerequisites: Java 17+, Clojure CLI, GStreamer 1.0+
git clone <repository>
cd potatoclient
make build
make run
```

## Requirements

- Java 17+ with `--enable-native-access=ALL-UNNAMED`
- Clojure CLI tools
- GStreamer 1.0+ with H.264 decoder plugins
- Protocol Buffers 3.15.0 (bundled)
- AppImage on Linux: Requires FUSE 2. See [FUSE setup guide](https://github.com/AppImage/AppImageKit/wiki/FUSE)

## Development

### Build Commands
```bash
make build        # Build JAR and compile protos
make run          # Run application
make dev          # Run with debug output
make nrepl        # Start REPL (port 7888)
make proto        # Compile Protocol Buffers
make clean        # Clean all build artifacts
make clean-proto  # Clean proto-generated files
```

### Architecture

- **Main Process** (Clojure): UI management, state coordination
- **Stream Processes** (Java): WebSocket handling, GStreamer pipelines
- **IPC**: JSON messages between processes
- **UI**: Swing/Seesaw components

### Key Components

- `potatoclient.main`: Entry point
- `potatoclient.core`: Application initialization
- `potatoclient.state`: Global state management
- `potatoclient.process`: Stream process lifecycle
- `potatoclient.proto`: Protocol Buffer serialization
- `VideoStreamManager.java`: GStreamer pipeline management

### Development Workflow

1. Proto changes: Edit `.proto` files → `make proto`
2. Run with REPL: `make nrepl` → connect on port 7888
3. Debug streams: `make dev` for GStreamer debug output
4. Build release: `make clean build`

See [CLAUDE.md](CLAUDE.md) for detailed guidelines.