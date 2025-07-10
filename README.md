# PotatoClient

A high-performance, cross-platform video streaming client built with Clojure and Java, featuring dual WebSocket H.264 video streams with hardware acceleration support.

## Features

- **Dual Video Streams**: Manage two independent H.264 video streams simultaneously
  - Heat Stream: 900x720 @ 30fps on port 50002
  - Day Stream: 1920x1080 @ 30fps on port 50001
- **Hardware Acceleration**: Automatic detection and use of hardware decoders
  - NVIDIA (NVDEC/nvh264dec)
  - Intel Quick Sync (msdkh264dec)
  - Direct3D 11 (d3d11h264dec)
  - Automatic fallback to software decoding
- **Real-time Event Logging**: Color-coded event display with export capabilities
- **Comprehensive Event Tracking**:
  - Mouse events with NDC coordinate transformation
  - Window events (resize, move, focus, etc.)
  - Navigation events with intelligent rate limiting
- **Protocol Buffer Support**: 
  - State synchronization from remote systems
  - Command sending with structured messages
  - Integration with Pronto 3.0 for idiomatic Clojure usage
- **Robust Architecture**:
  - Multi-process design for stability
  - Modular Clojure namespace structure
  - Automatic WebSocket reconnection
  - Lock-free atomic operations
  - Optimized memory management

## System Requirements

### Windows
- **Minimum**: Windows 7 SP1 x64
- **Recommended**: Windows 10/11 x64
- 4GB RAM minimum
- GStreamer 1.26.3 or newer (included in installer)
- Visual C++ Redistributables (included in installer)

### Linux
- Ubuntu 22.04 LTS or newer (glibc 2.35+)
- 64-bit x86_64 architecture
- GStreamer 1.0+ with plugins-base, plugins-good, plugins-bad
- 4GB RAM minimum

### macOS
- macOS 11.0 (Big Sur) or newer
- Apple Silicon (M1/M2/M3) native support
- GStreamer 1.0+ (install via Homebrew or official package)
- 4GB RAM minimum

## Installation

### Windows

#### Option 1: Installer (Recommended)
1. Download `PotatoClient-setup.exe` from [Releases](https://github.com/yourusername/potatoclient/releases)
2. Run the installer (requires administrator privileges)
3. The installer will automatically install:
   - GStreamer 1.26.3 runtime and development files
   - Visual C++ Redistributables
   - Java 17 runtime (bundled)

#### Option 2: Manual Installation
1. Install GStreamer:
   - Download from https://gstreamer.freedesktop.org/download/
   - Choose **MSVC 64-bit** runtime and development installers
   - Install both for full codec support
2. Install Visual C++ Redistributables if needed
3. Run the application

### Linux

#### AppImage (Recommended)
```bash
# Download the AppImage
wget https://github.com/yourusername/potatoclient/releases/download/latest/PotatoClient-x86_64.AppImage

# Make it executable
chmod +x PotatoClient-x86_64.AppImage

# Run it
./PotatoClient-x86_64.AppImage
```

#### Build from Source
```bash
# Install dependencies
sudo apt-get update
sudo apt-get install -y \
  default-jdk \
  leiningen \
  gstreamer1.0-tools \
  gstreamer1.0-plugins-base \
  gstreamer1.0-plugins-good \
  gstreamer1.0-plugins-bad \
  gstreamer1.0-plugins-ugly \
  gstreamer1.0-libav \
  libgstreamer1.0-0 \
  libgstreamer-plugins-base1.0-0

# Clone and build
git clone https://github.com/yourusername/potatoclient.git
cd potatoclient
make build
make run
```

### macOS

#### DMG Installation (Recommended)
1. Download `PotatoClient-{version}-macos-arm64.dmg` from [Releases](https://github.com/yourusername/potatoclient/releases)
2. Open the DMG file
3. Drag PotatoClient to your Applications folder
4. Install GStreamer (if not already installed):
   ```bash
   # Option A: Using Homebrew (recommended)
   brew install gstreamer gst-plugins-base gst-plugins-good gst-plugins-bad gst-plugins-ugly gst-libav
   
   # Option B: Official package
   # Download from https://gstreamer.freedesktop.org/download/#macos
   ```
5. First launch: Right-click PotatoClient → Open → Open (to bypass Gatekeeper)

#### Build from Source
```bash
# Install dependencies
brew install leiningen gstreamer gst-plugins-base gst-plugins-good gst-plugins-bad

# Clone and build
git clone https://github.com/yourusername/potatoclient.git
cd potatoclient
make build-macos-dev  # Creates unsigned .app for development
# or
make run              # Run directly
```

## Usage

1. **Launch the application**
   - Windows: Start from Start Menu or desktop shortcut
   - Linux: Run the AppImage or use `make run`
   - macOS: Open from Applications folder or use `make run`

2. **Configure server**
   - Enter your server domain in the text field
   - The application will use WebSocket Secure (WSS) protocol

3. **Control streams**
   - Click "Show Heat" for thermal camera stream
   - Click "Show Day" for daylight camera stream
   - Click again to hide streams

4. **Monitor events**
   - Events are color-coded in the log table:
     - 🟢 Green: System info and connections
     - 🟡 Yellow: Navigation events
     - 🟠 Orange: Window events  
     - 🔴 Red: Errors and disconnections
     - 🔵 Blue: Responses and debug info

5. **Export logs**
   - Click "Save Logs" to export timestamped event history
   - Click "Clear Log" to reset the display

## Development

### Prerequisites
- JDK 17 or newer
- Leiningen 2.9+
- GStreamer development libraries

### Building
```bash
# Clean build
make clean

# Build JAR (includes proto compilation)
make build

# Generate Protocol Buffer classes
make proto

# Clean generated proto files
make clean-proto

# Run with debug output
make dev

# Start REPL for development
make nrepl
```

### Architecture

The application uses a multi-process architecture for stability:

```
┌─────────────────────┐
│   Control Center    │  (Clojure/Swing UI)
│  - Event Display    │
│  - User Controls    │
└──────────┬──────────┘
           │ JSON over stdin/stdout
    ┌──────┴──────┐
    │             │
┌───▼───┐    ┌───▼───┐
│ Heat  │    │  Day  │  (Java Subprocesses)
│Stream │    │Stream │
│Process│    │Process│
└───┬───┘    └───┬───┘
    │             │
    └─────┬───────┘
          │ WebSocket
    ┌─────▼─────┐
    │  Server   │
    │ H.264 WSS │
    └───────────┘
```

### Key Components

#### Clojure (Main Process)
- **potatoclient.core**: Application entry point
- **potatoclient.state**: Centralized state management
- **potatoclient.process**: Process lifecycle management
- **potatoclient.ipc**: Inter-process communication
- **potatoclient.ui.***: UI components (control panel, log table)
- **potatoclient.events.***: Event handling subsystem
- **proto**: Protocol Buffer support with Pronto 3.0

#### Java (Stream Processes)
- **VideoStreamManager.java**: WebSocket client and GStreamer pipeline
- **GStreamerPipeline.java**: Video decoding with hardware acceleration
- **WebSocketManager.java**: Network communication with auto-reconnect
- **MessageProtocol.java**: Inter-process communication protocol

### H.264 Decoder Priority

1. Hardware decoders (checked first):
   - `nvh264dec` - NVIDIA GPU
   - `d3d11h264dec` - Windows Direct3D
   - `msdkh264dec` - Intel Quick Sync
   - `vaapih264dec` - Linux VA-API
   - `vtdec_h264` - macOS VideoToolbox

2. Software decoders (fallback):
   - `avdec_h264` - FFmpeg/libav
   - `openh264dec` - OpenH264
   - `decodebin` - Auto-negotiation

## Project Structure

```
potatoclient/
├── src/
│   ├── potatoclient/         # Modular Clojure namespaces
│   │   ├── core.clj          # Main entry point
│   │   ├── state.clj         # State management
│   │   ├── process.clj       # Process lifecycle
│   │   ├── ipc.clj           # Inter-process communication
│   │   ├── ui/               # UI components
│   │   └── events/           # Event handlers
│   ├── java/com/sycha/       # Java stream processors
│   ├── proto.clj             # Protocol Buffer support
│   └── video_control_center.clj # Legacy entry point
├── proto/                    # Protocol Buffer definitions
├── scripts/                  # Build and utility scripts
└── resources/               # Assets and configuration
```
