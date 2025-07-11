# PotatoClient

Cross-platform video streaming client with dual H.264 WebSocket streams and hardware acceleration.

## Features

- **Dual Video Streams**: Heat (900x720) and Day (1920x1080) cameras
- **Hardware Acceleration**: NVIDIA, Intel Quick Sync, Direct3D, VA-API, VideoToolbox
- **Multi-process Architecture**: Stable isolated stream handlers
- **Event Logging**: Color-coded real-time event display
- **Protocol Buffers**: Structured remote communication

## Quick Start

### Download Pre-built Releases
Get the latest release from [GitHub Releases](https://github.com/yourusername/potatoclient/releases):
- **Windows**: `PotatoClient-setup.exe` (includes GStreamer)
- **Linux**: `PotatoClient-x86_64.AppImage`
- **macOS**: `PotatoClient-macos-arm64.dmg` (Apple Silicon)

### Build from Source
```bash
# Prerequisites: Java 17+, Clojure CLI, GStreamer 1.0+

git clone https://github.com/yourusername/potatoclient.git
cd potatoclient
make build
make run
```

## System Requirements

- **Windows**: Windows 7 SP1+ (64-bit), 4GB RAM
- **Linux**: Ubuntu 22.04+ (glibc 2.35+), 4GB RAM
- **macOS**: macOS 11.0+, Apple Silicon, 4GB RAM

## Installation

### Windows
Run the installer or extract the portable ZIP. GStreamer and Visual C++ Redistributables are included.

### Linux
```bash
chmod +x PotatoClient-x86_64.AppImage
./PotatoClient-x86_64.AppImage
```

### macOS
1. Install GStreamer: `brew install gstreamer gst-plugins-base gst-plugins-good gst-plugins-bad`
2. Open DMG and drag to Applications
3. First run: Right-click → Open → Open

## Usage

1. Launch the application
2. Enter server domain (e.g., `sych.local`)
3. Click "Show Heat" or "Show Day" to start streams
4. View color-coded events in the log table
5. Export logs with "Save Logs" button

## Development

### Build System
- Uses Clojure CLI tools (deps.edn)
- Creates versioned JAR: `target/potatoclient-<version>.jar`
- Includes Protocol Buffer support with Pronto 3.0

### Commands
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
```
┌─────────────┐
│ Control UI  │ (Clojure/Swing)
└──────┬──────┘
       │ JSON IPC
   ┌───┴───┬───┐
   │ Heat  │Day│ (Java subprocesses)
   │Stream │   │
   └───────┴───┘
```

### Project Structure
```
src/
├── potatoclient/     # Clojure UI and core logic
└── java/com/sycha/   # Java stream handlers
proto/                # Protocol Buffer definitions
deps.edn             # Build configuration
build.clj            # Build script
```

## Contributing

See [CLAUDE.md](CLAUDE.md) for detailed development guidelines.

## License

[LICENSE]