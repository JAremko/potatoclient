# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PotatoClient is a multi-process video streaming client that displays two H.264 video streams from WebSocket connections. The main process (Clojure) manages the UI while separate Java subprocesses handle each video stream for stability and performance.

The application also includes Protocol Buffer support for structured communication with remote systems, enabling state synchronization and command sending.

## System Requirements

### Build Requirements
- **Java**: JDK 17 or newer (we use BellSoft Liberica JDK 17)
- **Build Tool**: Leiningen 2.9+
- **GStreamer**: 1.0+ development libraries (1.20+ recommended for videoconvertscale)
- **Protocol Buffers**: protoc compiler (handled automatically by build)

### Target Platform Requirements
- **Windows**: Windows 7 SP1 x64 or newer (64-bit only)
  - GStreamer 1.26.3 (latest stable)
  - Visual C++ Redistributables required
- **Linux**: Ubuntu 22.04 LTS or compatible (glibc 2.35+)
  - GStreamer 1.0+ with plugins-base, plugins-good, plugins-bad, libav
- **macOS**: macOS 11.0 (Big Sur) or newer
  - Apple Silicon (M1/M2/M3) native support
  - GStreamer 1.0+ via Homebrew or official framework
  - Hardware acceleration via VideoToolbox (vtdec_h264)
- **Architecture**: x86_64/amd64 (Windows/Linux), ARM64 (macOS)

## Build and Development Commands

### Essential Commands
```bash
# Build the application (creates standalone JAR, compiles protos)
make build

# Run the application
make run

# Start REPL for interactive development
make nrepl

# Run with GStreamer debug output
make dev

# Generate Protocol Buffer Java classes
make proto

# Clean generated proto files
make clean-proto

# Run tests (note: no tests currently exist)
make test

# Launch Claude Code
make mcp
```

### Direct Leiningen Commands
```bash
# Build standalone JAR
lein uberjar

# Run application
lein run

# Run with specific profile
lein with-profile dev run
```

## Architecture Overview

### Namespace Structure (Clojure)

The codebase follows a modular, idiomatic Clojure structure:

- **`potatoclient.core`**: Main application entry point
- **`potatoclient.state`**: Centralized state management with atoms
- **`potatoclient.process`**: Process lifecycle management
- **`potatoclient.ipc`**: Inter-process communication handling
- **`potatoclient.ui.*`**: UI components (control panel, log table, export)
- **`potatoclient.events.*`**: Event handling (log events, stream events)
- **`proto`**: Protocol Buffer support for state/command serialization
- **`video-control-center`**: Legacy entry point (delegates to core)

### Process Architecture

1. **Main Process** (Clojure): Application core handling:
   - UI management using Seesaw (Swing wrapper)
   - Process lifecycle management for video streams
   - Event logging and display with color coding
   - Inter-process communication via JSON over stdin/stdout
   - Protocol Buffer message handling
   - Child process launching with proper JVM flags

2. **Stream Processes** (`src/java/com/sycha/VideoStreamManager.java`): Java subprocesses for:
   - WebSocket H.264 video stream handling
   - GStreamer pipeline management with hardware acceleration
   - Mouse/window event processing with NDC coordinates
   - Automatic reconnection with exponential backoff

### Key Design Patterns

- **Process Isolation**: Each video stream runs in a separate Java subprocess
- **Message Passing**: JSON messages for inter-process communication
- **Protocol Buffers**: Structured data exchange with remote systems
- **Lock-free Concurrency**: Atomic operations in Java for thread safety
- **Event Throttling**: Rate limiting for mouse events (250ms interval)
- **Modular Architecture**: Clear separation of concerns in namespaces

### Configuration

- **Java Version**: Targets Java 17 (compatible with newer versions)
  - Uses Java 17 features: records, switch expressions, var keyword
  - Requires `--enable-native-access=ALL-UNNAMED` for JNA (Java 17+)
  - Forward compatible with Java 18+

- **Video Streams**:
  - Heat Stream: 900x720 @ 30fps on WebSocket port 50002
  - Day Stream: 1920x1080 @ 30fps on WebSocket port 50001

- **JVM Options** (in `project.clj`):
  - `-Djava.library.path` for GStreamer native libraries
  - `--enable-native-access=ALL-UNNAMED` for JNA native access
  - Child processes inherit these flags automatically

- **Hardware Acceleration Priority**:
  1. NVIDIA (nvh264dec/nvdec)
  2. Direct3D 11 (d3d11h264dec) - Windows
  3. Intel Quick Sync (msdkh264dec)
  4. VA-API (vaapih264dec) - Linux
  5. VideoToolbox (vtdec_h264) - macOS
  6. Software fallback (avdec_h264, openh264dec)

### Important Files

- `project.clj`: Leiningen build configuration and dependencies
- `Makefile`: Convenient development commands
- `.github/workflows/release.yml`: CI/CD for all platform builds
- `resources/logback.xml`: Logging configuration
- `src/java/com/sycha/GStreamerUtils.java`: Windows GStreamer path detection
- `src/java/com/sycha/MessageProtocol.java`: Inter-process communication protocol
- `src/proto.clj`: Protocol Buffer integration with Pronto 3.0
- `src/potatoclient/`: Modular Clojure namespace structure
- `proto/`: Protocol Buffer definition files
- `scripts/compile-protos.sh`: Proto compilation script
- `.github/actions/macos-assets/`: macOS-specific build assets

### Development Tips

- When modifying video processing, test with `make dev` to see GStreamer debug output
- The application requires GStreamer to be installed on the system
- Use `make nrepl` for interactive development with hot-reloading capabilities
- JSON messages between processes follow this format:
  ```json
  {"type": "event_type", "data": {...}}
  ```

### Common Tasks

- **Adding new event types**: 
  - Update event handlers in `potatoclient.events.stream`
  - Add Java event processing in `VideoStreamManager.java`
- **Modifying video pipeline**: Edit GStreamer pipeline strings in `VideoStreamManager.java`
- **UI changes**: 
  - Modify components in `potatoclient.ui.*` namespaces
  - Update state atoms in `potatoclient.state`
- **Build configuration**: Update `project.clj` for dependencies or JVM options
- **Protocol Buffer changes**:
  - Add/modify `.proto` files in `proto/` directory
  - Run `make proto` to regenerate Java classes
  - Update `proto.clj` mappers if needed
- **Adding new features**: Follow the modular structure
  - State in `potatoclient.state`
  - UI in `potatoclient.ui.*`
  - Events in `potatoclient.events.*`
  - IPC in `potatoclient.ipc`

### Platform-Specific Compatibility

#### Windows
The application automatically detects and configures GStreamer on Windows:
- Searches common installation paths (C:\gstreamer, Program Files)
- Checks environment variables (GSTREAMER_1_0_ROOT_MSVC_X86_64)
- Configures library and plugin paths dynamically
- See `GStreamerUtils.java` for implementation details

#### macOS
The application supports macOS with:
- Native Apple Silicon (ARM64) support
- Automatic detection of GStreamer installations (Homebrew and official)
- VideoToolbox hardware acceleration (vtdec_h264)
- User-friendly error messages if GStreamer is missing
- See `.github/actions/macos-assets/potatoclient-launcher` for implementation

### GitHub Actions CI/CD

The `.github/workflows/release.yml` workflow:
- Builds Windows installer with GStreamer 1.26.3
- Creates Linux AppImage with bundled dependencies
- Builds macOS DMG for Apple Silicon with bundled JRE
- Automatically releases on push to main branch
- Includes all redistributables in Windows installer
- Creates .app bundle with proper Info.plist for macOS
