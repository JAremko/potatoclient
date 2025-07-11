# Developer Guide

PotatoClient is a multi-process video streaming client with dual H.264 WebSocket streams. Main process (Clojure) handles UI, subprocesses (Java) handle video streams.

## Quick Reference

```bash
make build   # Build JAR and compile protos
make run     # Run application  
make dev     # Run with debug output
make nrepl   # REPL on port 7888
make proto   # Generate protobuf classes
make clean   # Clean all artifacts
```

## Architecture

### Key Components

**Clojure (Main Process)**
- `potatoclient.main` - Entry point
- `potatoclient.state` - Global state atoms
- `potatoclient.process` - Subprocess management
- `potatoclient.proto` - Protobuf serialization

**Java (Stream Processes)**
- `VideoStreamManager` - WebSocket + GStreamer pipeline
- Hardware decoder selection and fallback
- Auto-reconnection with exponential backoff

## Development Tasks

**Add Event Type**
1. Define in `potatoclient.events.stream`
2. Handle in `VideoStreamManager.java`
3. Add color in `events.formatting`

**Modify Pipeline**
- Edit `VideoStreamManager.java`
- Decoder priority in `findBestH264Decoder()`

**Update Protocol**
1. Edit `.proto` files
2. Run `make proto`
3. Update `potatoclient.proto`

## Technical Details

**Build**: Java 17+, Protobuf 3.15.0 (bundled)
**Output**: `target/potatoclient-<version>.jar`
**Streams**: Heat (900x720), Day (1920x1080)

**Hardware Decoders** (priority order):
1. NVIDIA (nvh264dec)
2. Direct3D 11 (d3d11h264dec) 
3. Intel QSV (msdkh264dec)
4. VA-API/VideoToolbox
5. Software fallback

**Platform Notes**:
- Windows: Auto-detects GStreamer
- macOS: VideoToolbox support
- Linux: System GStreamer