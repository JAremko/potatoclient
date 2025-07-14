# Developer Guide

PotatoClient is a multi-process video streaming client with dual H.264 WebSocket streams. Main process (Clojure) handles UI, subprocesses (Java) handle video streams.

## Quick Reference

```bash
make build        # Build JAR and compile protos
make run          # Run application  
make dev          # Run with GStreamer debug
make dev-reflect  # Run with reflection warnings
make nrepl        # REPL on port 7888
make proto        # Generate protobuf classes
make clean        # Clean all artifacts
```

## Architecture

### Key Components

**Clojure (Main Process)**
- `potatoclient.main` - Entry point with dev mode support
- `potatoclient.core` - Application initialization, menu creation
- `potatoclient.state` - Centralized state management with specs
- `potatoclient.process` - Subprocess lifecycle with type hints
- `potatoclient.proto` - Protobuf serialization
- `potatoclient.ipc` - Message routing and dispatch
- `potatoclient.config` - Platform-specific configuration
- `potatoclient.i18n` - Localization (English, Ukrainian)
- `potatoclient.theme` - Theme support (Sol Dark, Sol Light)

**Java (Stream Processes)**
- `VideoStreamManager` - WebSocket + GStreamer pipeline
- Hardware decoder selection and fallback
- Auto-reconnection with exponential backoff

## Development Tasks

**Add Theme**
1. Add theme definition in `potatoclient.theme/themes`
2. Update `get-available-themes` function
3. Theme will appear in View menu

**Add Language**
1. Add translations in `potatoclient.i18n/translations`
2. Add locale option in `potatoclient.state/::locale` spec
3. Update menu in `core.clj` with new language option

**Add Event Type**
1. Define in `potatoclient.events.stream`
2. Handle in `VideoStreamManager.java`
3. Add to `ipc/message-handlers` dispatch table

**Modify Pipeline**
- Edit `VideoStreamManager.java`
- Decoder priority in `findBestH264Decoder()`

**Update Protocol**
1. Edit `.proto` files
2. Run `make proto`
3. Update `potatoclient.proto` accessors

## Configuration

Platform-specific locations:
- Linux: `~/.config/potatoclient/`
- macOS: `~/Library/Application Support/PotatoClient/`
- Windows: `%LOCALAPPDATA%\PotatoClient\`

Config format (EDN):
```clojure
{:theme :sol-dark
 :domain "sych.local"
 :locale :english}
```

## State Management

State is separated by concern:
- `streams-state` - Process references
- `app-config` - Runtime configuration
- `logs-state` - Log entries and buffering
- `ui-refs` - UI component references

All state functions include validation via clojure.spec.

## Development Mode

Enable with:
- `make dev-reflect`
- `-Dpotatoclient.dev=true`
- `POTATOCLIENT_DEV=true`

Features:
- Reflection warnings
- Extended logging (future)
- Assertions (future)

## Technical Details

**Build**: Java 17+, Protobuf 3.15.0 (bundled)
**Streams**: Heat (900x720), Day (1920x1080)

**Hardware Decoders** (priority):
1. NVIDIA (nvh264dec)
2. Direct3D 11 (d3d11h264dec) 
3. Intel QSV (msdkh264dec)
4. VA-API/VideoToolbox
5. Software fallback

**Type Hints**: Added to prevent reflection in:
- JFrame operations
- Process/IO operations
- File operations
- Date formatting