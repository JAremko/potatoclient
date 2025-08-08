# WebSocket Endpoints Documentation

## Overview

The PotatoClient uses four WebSocket endpoints to communicate with the server for real-time command sending, state updates, and video streaming. The architecture separates concerns between command transmission (client to server), state reception (server to client), and video data streaming, with dedicated handlers for each endpoint.

### Key Architectural Principles

1. **Unidirectional Communication**: The command (`/ws/ws_cmd`) and state (`/ws/ws_state`) endpoints are strictly unidirectional:
   - Commands flow only from client → server
   - State updates flow only from server → client
   - No request-response pattern on these channels

2. **Protocol Buffer Wire Format**: Command and state endpoints use pure protobuf binary serialization:
   - Direct binary transmission over WebSocket
   - No additional framing or encoding
   - Efficient, type-safe message serialization

3. **Separation of Concerns**: Each endpoint has a single, well-defined purpose:
   - `/ws/ws_cmd`: Send commands to control the system
   - `/ws/ws_state`: Receive system state updates
   - `/ws/ws_rec_video_heat`: Stream thermal video
   - `/ws/ws_rec_video_day`: Stream day camera video

## Endpoints

### 1. Command Endpoint (`/ws/ws_cmd`)

**Purpose**: Transmits user commands from the client to the server.

**Full URL Format**: `wss://<domain>/ws/ws_cmd`

**Handler Class**: `CommandSubprocess` (src/potatoclient/kotlin/transit/CommandSubprocess.kt)

**Communication**: **Unidirectional** (client → server only)

**Data Flow**:
```
User Input → Clojure Core → Transit Message → Kotlin Subprocess → Protobuf → WebSocket → Server
```

**Wire Protocol**: Binary Protocol Buffers
- Root message type: `JonSharedCmd.Root`
- Generated from `.proto` files in `examples/protogen/proto/`
- Binary format sent directly over WebSocket
- No additional framing beyond WebSocket protocol

**Key Features**:
- Strictly one-way communication (client to server)
- No response data expected on this channel
- Sends confirmation back to Clojure layer via Transit after successful transmission
- Commands are fire-and-forget at the WebSocket level

### 2. State Endpoint (`/ws/ws_state`)

**Purpose**: Receives real-time state updates from the server.

**Full URL Format**: `wss://<domain>/ws/ws_state`

**Handler Class**: `StateSubprocess` (src/potatoclient/kotlin/transit/StateSubprocess.kt)

**Communication**: **Unidirectional** (server → client only)

**Data Flow**:
```
Server → WebSocket → Protobuf → Kotlin Subprocess → Transit Message → Clojure Core → UI Update
```

**Wire Protocol**: Binary Protocol Buffers
- Main message type: `JonSharedData.JonGUIState`
- Contains comprehensive system state information
- Binary format received directly over WebSocket
- No additional framing beyond WebSocket protocol
- Buffer size: 1MB for message accumulation

**Key Features**:
- Strictly one-way communication (server to client)
- Continuous stream of state updates
- Rate-limited to prevent overwhelming the client (default 30Hz)
- Token bucket algorithm for rate limiting
- No acknowledgments sent back to server

### 3. Heat Camera Video Stream (`/ws/ws_rec_video_heat`)

**Purpose**: Streams thermal/heat camera video data from the server.

**Full URL Format**: `wss://<domain>/ws/ws_rec_video_heat`

**Handler Class**: `VideoStreamManager` (src/potatoclient/kotlin/VideoStreamManager.kt)

**Video Specifications**:
- Resolution: 900x720
- Codec: H.264 (video/x-h264, byte-stream format)
- Stream format: NAL unit aligned

**Binary Frame Format**:
```
[8 bytes: timestamp][8 bytes: duration][remaining: H.264 video data]
```

**Key Features**:
- Continuous video stream
- Direct feed to GStreamer pipeline
- Frame logging every 300 frames
- Automatic reconnection on failure

### 4. Day Camera Video Stream (`/ws/ws_rec_video_day`)

**Purpose**: Streams day camera video data from the server.

**Full URL Format**: `wss://<domain>/ws/ws_rec_video_day`

**Handler Class**: `VideoStreamManager` (src/potatoclient/kotlin/VideoStreamManager.kt)

**Video Specifications**:
- Resolution: 1920x1080 (Full HD)
- Codec: H.264 (video/x-h264, byte-stream format)
- Stream format: NAL unit aligned

**Binary Frame Format**:
```
[8 bytes: timestamp][8 bytes: duration][remaining: H.264 video data]
```

**Key Features**:
- Continuous video stream
- Direct feed to GStreamer pipeline
- Frame logging every 300 frames
- Automatic reconnection on failure

## Wire Protocol Details

### Protocol Buffer Binary Format

The command and state endpoints use **pure Protocol Buffer binary serialization** without additional framing:

1. **Direct Binary Transmission**: Protobuf messages are serialized to binary and sent directly over WebSocket
2. **No Custom Framing**: Relies entirely on WebSocket protocol for message boundaries
3. **No Text Encoding**: Pure binary format, no Base64 or other text encoding
4. **Efficient Parsing**: Binary format allows zero-copy deserialization where possible

### Message Structure

#### Command Messages (`/ws/ws_cmd`)

The root command message structure:
```protobuf
message Root {
  uint32 protocol_version = 1;
  uint32 session_id = 2;
  
  // Subsystem commands (oneof pattern)
  cmd.DayCamera.Root day_camera = 20;
  cmd.HeatCamera.Root heat_camera = 21;
  cmd.RotaryPlatform.Root rotary = 22;
  cmd.Compass.Root compass = 23;
  // ... more subsystems
  
  // Control commands
  Ping ping = 100;
  Noop noop = 101;
}
```

#### State Messages (`/ws/ws_state`)

The GUI state message structure:
```protobuf
message JonGUIState {
  JonGuiDataCameraDay camera_day = 1;
  JonGuiDataCameraHeat camera_heat = 2;
  JonGuiDataRotary rotary = 3;
  JonGuiDataCompass compass = 4;
  JonGuiDataGps gps = 5;
  // ... more state components
}
```

### Proto Explorer Tool

To explore and understand the protobuf message definitions, use the **Proto-Explorer** tool included in the project:

**Location**: `tools/proto-explorer/`

**Documentation**: [Proto-Explorer README](../tools/proto-explorer/README.md)

#### Quick Usage

```bash
# Search for messages by name
make proto-search QUERY=gps

# List all available messages
make proto-list

# Get detailed information about a specific message
make proto-info CLASS='cmd.JonSharedCmd$Root'

# View Clojure EDN representation
make proto-info CLASS='ser.JonSharedData$JonGUIState'
```

#### Key Features

- **Message Discovery**: Search and list all available protobuf messages
- **Java Mapping**: See how proto messages map to Java classes
- **EDN Structure**: View Clojure-friendly representations via Pronto
- **Field Details**: Complete field information with types and numbers

#### Example: Exploring a Command

```bash
$ make proto-info CLASS='cmd.RotaryPlatform$RotateAzimuthTo'

=== FIELD DETAILS ===
  [ 1] angle                     : type-float
  [ 2] speed                     : type-float

=== PRONTO EDN STRUCTURE ===
{:angle 0.0, :speed 0.0}
```

This helps understand:
- The exact field names and types
- The field numbers for wire format
- How to construct the message in Clojure

### Protocol Versioning

The command protocol includes versioning support:
- `protocol_version` field in root command message
- Allows backward compatibility
- Server can handle multiple protocol versions

## Connection Management

### Initial Connection

Both endpoints are initialized when a stream starts (src/potatoclient/core.clj:153-154):

```clojure
(let [ws-url (str "wss://" domain "/ws/ws_cmd")
      state-url (str "wss://" domain "/ws/ws_state")]
  ;; Launch subprocesses with these URLs
)
```

### Connection Parameters

**Protocol**: WebSocket Secure (WSS)
- Always uses `wss://` for encrypted connections
- Falls back to accepting all SSL certificates for development environments

**Headers**:
- `Origin`: Set to `https://<host>` for CORS compliance
- `User-Agent`: `VideoStreamManager/1.0` (for video streams)
- `Cache-Control`: `no-cache` (for video streams)
- `Pragma`: `no-cache` (for video streams)

**Timeouts**:
- Connection timeout: 10 seconds
- No explicit idle timeout (maintains persistent connection)

### SSL/TLS Configuration

**Development Mode**:
- Uses a trust-all SSL context for self-signed certificates
- Implemented via `TrustAllTrustManager` class
- **WARNING**: This should only be used in development/testing environments

**Production Recommendations**:
- Use proper SSL certificate validation
- Remove trust-all configuration
- Implement certificate pinning for enhanced security

## Reconnection Policy

### Command Endpoint Reconnection

The command endpoint WebSocket client (`CommandWebSocketClient`) implements a simple connection strategy:

1. **Initial Connection**: Attempts to connect with a 10-second timeout
2. **Connection Failure**: Logs error and reports to Transit layer
3. **No Automatic Reconnection**: Command endpoint does not auto-reconnect
4. **Manual Retry**: New connections initiated on new command sessions

### State Endpoint Reconnection

The state endpoint WebSocket client (`StateWebSocketClient`) has similar behavior:

1. **Initial Connection**: Attempts connection on subprocess start
2. **Connection Loss**: Logs disconnection
3. **No Automatic Reconnection**: Relies on subprocess restart for new connection
4. **Subprocess Management**: Parent process can restart subprocess on failure

### Video Stream WebSocket Reconnection

The main video stream WebSocket (`WebSocketClientBuiltIn`) has more sophisticated reconnection:

1. **Automatic Reconnection**: Enabled by default via `shouldReconnect` flag
2. **Reconnection Delay**: 1 second between attempts
3. **Reconnection Trigger Events**:
   - Connection error during initial connect
   - WebSocket error during operation
   - Abnormal closure (non-NORMAL_CLOSURE status codes)
4. **Reconnection Process**:
   ```kotlin
   private fun scheduleReconnect() {
       Thread {
           Thread.sleep(1000)  // 1-second delay
           if (shouldReconnect.get() && !isConnecting.get()) {
               performConnect()
           }
       }.start()
   }
   ```
5. **Stopping Reconnection**: Call `close()` which sets `shouldReconnect` to `false`

## Rate Limiting and Backpressure

### State Updates Rate Limiting

The state subprocess implements configurable rate limiting:

**Default Rate**: 30Hz (30 updates per second)

**Configuration**: Can be adjusted via control messages:
```clojure
{:msg-type :control
 :payload {:action "set-rate-limit"
           :rate-hz 60}}  ; Set to 60Hz
```

**Implementation**: Token bucket algorithm
- Tokens replenished at configured rate
- Bucket size equals rate (e.g., 30 tokens for 30Hz)
- Updates dropped if no tokens available
- Prevents client overload during high-frequency updates

### Message Queue Backpressure

**Transit Communication**:
- Queue overflow detection with message dropping
- Warning logged: "Message queue full, dropping message"
- High water mark: 1000 messages
- Low water mark: 100 messages

## Message Size Limits

### Buffer and Frame Size Constraints

**Video Stream Buffers**:
- Buffer pool size: 20 buffers (configurable: 10-20)
- Individual buffer size: 2MB
- Direct ByteBuffers for better performance

**State Message Buffers**:
- Message accumulation buffer: 1MB
- Expandable on demand for larger messages

**Transit Frame Limits**:
- Maximum frame size: 10MB
- 4-byte length prefix (big-endian)
- Frame validation on read

**Constants** (from Constants.kt):
```kotlin
const val BUFFER_POOL_SIZE = 10
const val MAX_BUFFER_SIZE = 2 * 1024 * 1024  // 2MB
```

## Error Handling

### Connection Errors

**Command Endpoint**:
- Sends error message via Transit to Clojure layer
- Returns error response with detailed message
- Logs to subprocess-specific log file

**State Endpoint**:
- Silently drops malformed messages
- Continues processing subsequent messages
- No error propagation for parse failures (resilient design)

### Message Processing Errors

**Transit Communication Errors**:
- Logged via `TransitMessageProtocol`
- Error context includes subprocess name
- Errors reported to main process via error messages

**Protobuf Parsing Errors**:
- Command building errors result in error response
- State parsing errors are silently ignored
- Maintains stream continuity

## Subprocess Architecture

### Process Isolation

Each WebSocket endpoint runs in a separate JVM subprocess:
- **Command Subprocess**: Handles outgoing commands
- **State Subprocess**: Handles incoming state

### Inter-Process Communication

**Protocol**: Transit over stdio
- Input: System.in (framed messages from Clojure)
- Output: System.out (framed messages to Clojure)
- Logging: Separate log files via intercepted stdout

**Message Framing**:
- 4-byte length prefix (big-endian)
- Maximum frame size: 10MB
- Ensures reliable message boundaries

### Subprocess Lifecycle

1. **Startup**:
   - Launched by Clojure main process
   - Receives WebSocket URL as command-line argument
   - Establishes Transit communication first
   - Connects to WebSocket endpoint

2. **Operation**:
   - Continuous message processing loop
   - Handles control messages (shutdown, stats, rate-limit)
   - Maintains metrics (messages sent/received)

3. **Shutdown**:
   - Initiated by control message
   - Closes WebSocket connection
   - Sends confirmation to main process
   - Exits cleanly

## Testing Support

### Test Mode

Both command and state subprocesses support a test mode for development:

**Activation**: Pass `--test-mode` flag
```bash
java -cp <classpath> potatoclient.kotlin.transit.CommandSubprocessKt --test-mode
java -cp <classpath> potatoclient.kotlin.transit.StateSubprocessKt --test-mode
```

**Behavior**:
- No actual WebSocket connection established
- Uses `TestModeWebSocketStub` for command handling
- Processes Transit messages normally
- Simulates validation and error conditions
- Provides mock responses for all command types
- Useful for integration testing without server

**Test Stub Features** (TestModeWebSocketStub.kt):
- Handles all command types with mock responses
- Simulates command validation
- Provides error injection for testing error paths
- Supports ping-pong testing

## Monitoring and Metrics

### Available Metrics

Both endpoints track and report:
- Total messages received
- Total messages sent  
- WebSocket connection status
- Current rate limit (state endpoint only)

### Retrieving Stats

Send control message to subprocess:
```clojure
{:msg-type :control
 :payload {:action "get-stats"}}
```

Response format:
```clojure
{:msg-type :metric
 :payload {:name :command-stats  ; or :state-stats
           :value {:received 1234
                   :sent 5678
                   :ws-connected true
                   :rate-limit-hz 30}}}  ; state only
```

## Performance Optimizations

### Buffer Pooling

The video stream WebSocket uses buffer pooling:
- Pool size: 20 buffers
- Buffer size: 2MB each
- Direct ByteBuffers for better performance
- Automatic buffer recycling

### Message Batching

- State updates are rate-limited to prevent overwhelming
- Commands are sent immediately without batching
- Transit messages use framing for efficient parsing

### Zero-Copy Operations

Where possible, the implementation uses:
- Direct ByteBuffers for WebSocket data
- Pass-through of binary data without copying
- Efficient protobuf parsing without intermediate representations

## Ping-Pong Mechanism

### Application-Level Heartbeat

The system implements application-level ping-pong, not WebSocket-level:

**Ping Command**:
```kotlin
// Sent via protobuf command
"ping" -> builder.setPing(buildPing(value as Map<*, *>))
```

**Pong Response**:
```kotlin
// TestModeWebSocketStub.kt
command.hasPing() -> createPongResponse()
```

**Key Points**:
- Uses protobuf messages for heartbeat
- Not using WebSocket protocol ping/pong frames
- Handled at application layer for better control

## Authentication and Authorization

### Current Implementation

**No formal authentication mechanism** - The system currently relies on:
- Network-level security (VPN, firewall rules)
- SSL/TLS encryption for data protection
- Origin header validation for basic CORS

**Headers Used**:
```kotlin
"Origin" -> "https://${uri.host}"
"User-Agent" -> "VideoStreamManager/1.0"
"Cache-Control" -> "no-cache"
"Pragma" -> "no-cache"
```

### Recommended Enhancements

For production deployments:
- Add JWT or API key authentication
- Implement session management
- Add rate limiting per client
- Monitor for abnormal connection patterns

## Security Considerations

### Development vs Production

**Development Settings** (current implementation):
- Accepts all SSL certificates via `TrustAllTrustManager`
- No certificate validation
- Suitable for local testing only

**Production Requirements**:
- Proper SSL certificate validation
- Certificate pinning recommended
- Remove trust-all implementations
- Implement proper authentication headers

### Data Protection

- All connections use WSS (encrypted)
- Binary protobuf format (not human-readable)
- No sensitive data logging
- Subprocess isolation for security boundaries

## Environment Variables and Configuration

### Environment Variables

The system uses several environment variables for configuration:

**Development/Release Flags**:
- `POTATOCLIENT_DEV`: Enables development mode features
- `POTATOCLIENT_RELEASE`: Indicates release build

**Configuration Paths** (platform-specific):
- Windows: `LOCALAPPDATA`, `APPDATA`
- Linux/Mac: `XDG_CONFIG_HOME`, `XDG_DATA_HOME`

**Note**: WebSocket URLs and settings are not configurable via environment variables - they are determined by the domain configuration in the application.

### Constants and Limits

Key constants defined in the codebase:

```kotlin
// From Constants.kt
const val BUFFER_POOL_SIZE = 10
const val MAX_BUFFER_SIZE = 2 * 1024 * 1024  // 2MB
const val USER_AGENT = "VideoStreamManager/1.0"

// From TransitCommunicator.kt
const val MAX_MESSAGE_SIZE = 10 * 1024 * 1024  // 10MB

// From WebSocketClientBuiltIn.kt
const val BUFFER_TRIM_INTERVAL_MS = 60_000L  // 1 minute
```

## Common Issues and Troubleshooting

### Connection Refused

**Symptoms**: WebSocket fails to connect
**Common Causes**:
- Server not running
- Incorrect domain/port
- Firewall blocking connection

**Resolution**:
- Verify server is accessible
- Check domain configuration
- Test with curl/wscat tools

### SSL Certificate Errors

**Symptoms**: SSL handshake failure
**Common Causes**:
- Self-signed certificate in production
- Certificate expired
- Hostname mismatch

**Resolution**:
- Use proper certificates in production
- Ensure test mode for development
- Verify certificate chain

### Message Processing Delays

**Symptoms**: Lag in state updates or command execution
**Common Causes**:
- Rate limiting too aggressive
- Network latency
- Subprocess CPU bottleneck

**Resolution**:
- Adjust rate limit settings
- Monitor subprocess CPU usage
- Check network conditions

### Subprocess Crashes

**Symptoms**: Connection lost, subprocess exits
**Common Causes**:
- Out of memory
- Unhandled exception
- Parent process terminated

**Resolution**:
- Check subprocess logs
- Increase JVM heap size if needed
- Implement subprocess restart logic