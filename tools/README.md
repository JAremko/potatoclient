# PotatoClient Tools

This directory contains various development and testing tools for the PotatoClient system.

## Table of Contents

- [Stream Spawner](#stream-spawner)
- [Proto Explorer](#proto-explorer)
- [Guardrails Check](#guardrails-check)
- [Kotlin Compiler](#kotlin-compiler)

---

## Stream Spawner

### Overview

The Stream Spawner is a tool for launching and managing multiple video stream processes that connect to WebSocket endpoints and display real-time video feeds. It creates separate JVM processes for each video stream with proper IPC (Inter-Process Communication) support.

### Location

- **Main script**: `/spawn-streams.sh` (in project root)
- **Source code**: `/tools/stream-spawner/src/`
  - `StreamSpawner.kt` - Production stream spawner
  - `TestStreamSpawner.kt` - Mock IPC testing version

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    spawn-streams.sh                      │
│                  (Orchestrator Script)                   │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│                   IPC Server Process                     │
│                    (PID: parent)                         │
│  ┌─────────────────────┬─────────────────────┐         │
│  │   Heat IPC Server    │   Day IPC Server    │         │
│  │  /tmp/*/ipc-*-heat   │  /tmp/*/ipc-*-day   │         │
│  └──────────┬───────────┴──────────┬──────────┘         │
└─────────────┼──────────────────────┼────────────────────┘
              │                      │
              ▼                      ▼
┌──────────────────────┐  ┌──────────────────────┐
│  VideoStreamManager  │  │  VideoStreamManager  │
│   (Heat Stream)      │  │   (Day Stream)       │
│   PID: child_1       │  │   PID: child_2       │
│                      │  │                      │
│  WebSocket Client    │  │  WebSocket Client    │
│  GStreamer Pipeline  │  │  GStreamer Pipeline  │
│  Video Window        │  │  Video Window        │
└──────────────────────┘  └──────────────────────┘
         │                         │
         ▼                         ▼
   wss://host/ws/          wss://host/ws/
   ws_rec_video_heat       ws_rec_video_day
```

### Features

- **Multi-Process Architecture**: Each video stream runs in its own JVM process for isolation and stability
- **IPC Communication**: Transit-based messaging between processes using Unix domain sockets
- **Hardware Acceleration**: Automatic detection and use of NVIDIA/Intel/AMD hardware decoders
- **Automatic Window Management**: Windows appear immediately on launch, positioned side-by-side
- **Detailed Event Logging**: All events are logged with clear source prefixes
- **Graceful Shutdown**: Proper cleanup of all resources on termination

### Usage

#### Basic Usage

```bash
# Start streams connecting to default host (sych.local)
./spawn-streams.sh

# Connect to a custom host
./spawn-streams.sh myhost.local
```

#### What Happens When You Run It

1. **IPC Server Creation**: A parent process creates IPC servers for "heat" and "day" streams
2. **Stream Process Spawning**: Two VideoStreamManager processes are launched with:
   - Stream ID (heat or day)
   - WebSocket URL for the video feed
   - Domain name
   - Parent process PID for IPC connection
3. **Window Creation**: Two video windows appear immediately:
   - Heat stream (thermal camera) - positioned on the left
   - Day stream (visible light camera) - positioned on the right
4. **Video Streaming**: Each process:
   - Connects to its WebSocket endpoint
   - Receives H.264 encoded video data
   - Decodes using GStreamer with hardware acceleration
   - Displays the video in its window
5. **Event Processing**: User interactions and system events are captured and logged

#### Output Format

The tool outputs detailed logs with clear prefixes:

```
[HEAT-LOG] [level] message       # Log messages from heat stream
[DAY-LOG] [level] message        # Log messages from day stream
[HEAT-GESTURE] type at pixel(x,y) ndc(x,y)  # Mouse gestures on heat window
[DAY-GESTURE] type at pixel(x,y) ndc(x,y)   # Mouse gestures on day window
[HEAT-WINDOW] action             # Window events (focus, blur, resize)
[DAY-WINDOW] action              # Window events for day stream
[HEAT-CONNECTION] status         # WebSocket connection status
[DAY-CONNECTION] status          # WebSocket connection status
```

#### Gesture Types

- `tap` - Single click
- `double-tap` - Double click
- `pan-start` - Drag begin
- `pan-move` - Dragging
- `pan-stop` - Drag end
- `wheel-up` - Scroll up
- `wheel-down` - Scroll down

#### Stopping the Streams

Press `Ctrl+C` to gracefully stop all streams. The script will:
1. Stop video stream processes
2. Wait for graceful shutdown (5 seconds)
3. Force kill if necessary
4. Stop IPC servers
5. Clean up all resources

### Requirements

- **Java 17+** - Required for running Kotlin/JVM processes
- **GStreamer 1.0+** - Video decoding and display
  - `gstreamer1.0-plugins-base`
  - `gstreamer1.0-plugins-good`
  - `gstreamer1.0-plugins-bad` (for H.264 support)
  - `gstreamer1.0-libav` (for software decoding fallback)
- **Kotlin Compiler** - Included in `tools/kotlin-2.2.0/`
- **Network Access** - To connect to WebSocket endpoints
- **X11/Wayland** - For displaying video windows on Linux

### Configuration

The tool connects to these WebSocket endpoints by default:
- Heat: `wss://[host]/ws/ws_rec_video_heat`
- Day: `wss://[host]/ws/ws_rec_video_day`

Default host is `sych.local` but can be overridden via command line argument.

### Troubleshooting

#### "VideoStreamManager class not found"
```bash
# Recompile Kotlin sources
make compile-kotlin
```

#### "Socket file does not exist"
- Ensure the IPC server process started successfully
- Check that the parent PID is being passed correctly
- Verify no stale socket files in `/tmp/potatoclient-sockets/`

#### "GStreamer pipeline error"
- Install missing GStreamer plugins
- Check GStreamer version: `gst-inspect-1.0 --version`
- Verify hardware decoder availability: `gst-inspect-1.0 | grep h264`

#### No video displayed
- Check WebSocket connection in logs
- Verify the video source is sending data
- Look for decoder errors in the output

### Development

#### Running Mock Streams (Testing)

For testing without real video streams:

```bash
cd tools/stream-spawner
./run-test.sh [host]
```

This creates mock IPC clients that simulate video stream behavior without WebSocket connections.

#### Modifying the Code

1. **VideoStreamManager** (`src/potatoclient/kotlin/VideoStreamManager.kt`)
   - Main video stream management class
   - Handles WebSocket connection, GStreamer pipeline, and IPC

2. **FrameManager** (`src/potatoclient/kotlin/FrameManager.kt`)
   - Creates and manages video windows
   - Controls window positioning and visibility

3. **IPC Components** (`src/potatoclient/kotlin/ipc/`)
   - IpcServer/IpcClient for inter-process communication
   - Transit-based message serialization

After modifying Kotlin code:
```bash
make compile-kotlin
```

---

## Proto Explorer

Tool for exploring Protocol Buffer definitions and their Java class representations.

**Location**: `/tools/proto-explorer/`

**Usage**: See the [proto-explorer agent documentation](#proto-class-explorer-agent) in CLAUDE.md

---

## Guardrails Check

Babashka-based tool for checking Clojure functions that aren't using Guardrails for runtime validation.

**Location**: `/tools/guardrails-check/`

**Features**:
- Finds functions using raw `defn` instead of `>defn`
- Reports Guardrails adoption statistics
- Identifies namespaces with unspecced functions

**Usage**: See the [guardrails-scanner agent documentation](#guardrails-scanner-agent) in CLAUDE.md

---

## Kotlin Compiler

Bundled Kotlin compiler for building Kotlin components.

**Location**: `/tools/kotlin-2.2.0/`

**Version**: Kotlin 2.2.0

**Usage**:
```bash
# Compile Kotlin files
tools/kotlin-2.2.0/bin/kotlinc -cp "classpath" source.kt -d output/
```

This is automatically used by the build system when running `make compile-kotlin`.

---

## Adding New Tools

When adding new tools to this directory:

1. Create a subdirectory for your tool
2. Include a README.md in the tool's directory
3. Update this main tools README.md
4. Update the project's CLAUDE.md if the tool should be used by AI assistants
5. Add any necessary build/run scripts

## Best Practices

1. **Isolation**: Tools should be self-contained and not interfere with the main application
2. **Documentation**: Include clear usage instructions and examples
3. **Error Handling**: Provide helpful error messages and troubleshooting guides
4. **Logging**: Use consistent logging formats with clear prefixes
5. **Cleanup**: Ensure proper resource cleanup on exit