# Stream Spawner

A utility for spawning both heat and day video streams that connect to `sych.local` or a custom host.

## Components

### StreamSpawner
The main spawner that launches actual `VideoStreamManager` instances for both heat and day streams.

### TestStreamSpawner  
A test version that creates mock IPC clients to simulate stream behavior without requiring actual WebSocket connections.

## Usage

### Test Mode (Mock IPC Clients)
```bash
# Use default host (sych.local)
./run-test.sh

# Use custom host
./run-test.sh myhost.com
```

This creates mock IPC clients that:
- Connect to local IPC servers
- Send periodic connection events
- Simulate gesture events every 5 seconds
- Log frame processing info

### Real Mode (VideoStreamManager)
```bash
# Use default host (sych.local)
./run.sh

# Use custom host
./run.sh --host myhost.com

# Enable debug output
./run.sh --debug

# Show help
./run.sh --help
```

This spawns actual VideoStreamManager processes that:
- Connect to WebSocket endpoints at `wss://host/ws/ws_rec_video_heat` and `wss://host/ws/ws_rec_video_day`
- Process real video streams
- Handle IPC communication with the main application

## Stream Endpoints

- **Heat stream**: `wss://sych.local/ws/ws_rec_video_heat`
- **Day stream**: `wss://sych.local/ws/ws_rec_video_day`

## Architecture

```
StreamSpawner
    ├── Spawns VideoStreamManager (heat)
    │   ├── Connects to WebSocket endpoint
    │   ├── Creates IpcClient("heat")
    │   └── Processes video frames
    │
    └── Spawns VideoStreamManager (day)
        ├── Connects to WebSocket endpoint
        ├── Creates IpcClient("day")
        └── Processes video frames
```

## Requirements

- Java 17+
- Kotlin compiler in `tools/kotlin-2.2.0/`
- Compiled project classes (`make compile-kotlin`)
- Network access to the configured host

## Troubleshooting

### VideoStreamManager not found
Run `make compile-kotlin` from the project root.

### Connection refused
- Check that the host is reachable: `ping sych.local`
- Verify WebSocket endpoints are available
- Check firewall settings

### IPC errors
- Ensure no other instances are running
- Check socket file permissions in `/tmp/`
- Clean up stale socket files if needed

## Stopping Streams

Press `Ctrl+C` to gracefully shutdown all streams. The spawner will:
1. Send shutdown signals to all stream processes
2. Wait up to 5 seconds for graceful shutdown
3. Force kill if necessary
4. Clean up IPC resources