# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with this repository.

## Project Overview

PotatoClient is a multi-process video streaming client that displays two H.264 video streams from WebSocket connections. The main process (Clojure) manages the UI while separate Java subprocesses handle each video stream.

## Build Commands

```bash
make build        # Build JAR and compile protos
make run          # Run application  
make dev          # Run with GStreamer debug output
make nrepl        # Start REPL on port 7888
make proto        # Generate Protocol Buffer classes
make clean        # Clean all build artifacts
make clean-proto  # Clean only proto-generated files
```

## Architecture

### Main Process (Clojure)
- **potatoclient.core**: Entry point
- **potatoclient.state**: State management (atoms)
- **potatoclient.process**: Process lifecycle
- **potatoclient.ipc**: Inter-process communication (JSON)
- **potatoclient.ui.***: UI components (Seesaw/Swing)
- **potatoclient.events.***: Event handling
- **proto**: Protocol Buffer support (Pronto 3.0)

### Stream Processes (Java)
- WebSocket H.264 stream handling
- GStreamer pipeline with hardware acceleration
- Auto-reconnection with backoff
- Mouse/window event processing

### Key Files
- `deps.edn`: Build config and dependencies
- `build.clj`: Build script (version 1.2.0)
- `.github/workflows/release.yml`: CI/CD for all platforms
- `src/java/com/sycha/VideoStreamManager.java`: Stream handler
- `proto/`: Protocol Buffer definitions
- `tools/protoc-3.15.0/`: Bundled protoc for compatibility

## Development

### Requirements
- Java 17+ (uses `--enable-native-access=ALL-UNNAMED`)
- Clojure CLI tools (deps.edn)
- GStreamer 1.0+
- Protocol Buffers 3.15.0 (bundled)

### Common Tasks

**Add new event type:**
1. Update `potatoclient.events.stream`
2. Add to `VideoStreamManager.java`
3. Update colors in `potatoclient.events.formatting`

**Modify video pipeline:**
- Edit pipeline in `VideoStreamManager.java`
- Hardware decoders in `GStreamerPipeline.findBestH264Decoder()`

**UI changes:**
- Components in `potatoclient.ui.*`
- State in `potatoclient.state`

**Proto changes:**
1. Edit `.proto` files
2. Run `make proto`
3. Update `proto.clj` if needed

### Important Notes

- **Protobuf Version**: Uses 3.15.0 for Pronto compatibility
- **Build Output**: Creates `target/potatoclient-<version>.jar`
- **Java Compilation**: Targets Java 17 with release flag

### Platform Notes

**Windows**: Auto-detects GStreamer paths
**macOS**: Supports Apple Silicon, VideoToolbox
**Linux**: Uses system GStreamer

### Video Streams
- Heat: 900x720 @ 30fps (port 50002)
- Day: 1920x1080 @ 30fps (port 50001)

### Hardware Decoders Priority
1. NVIDIA (nvh264dec)
2. Direct3D 11 (d3d11h264dec)
3. Intel Quick Sync (msdkh264dec)
4. VA-API/VideoToolbox
5. Software fallback