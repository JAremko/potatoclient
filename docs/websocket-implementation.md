# WebSocket Implementation for PotatoClient

## Overview

The system uses two unidirectional WebSocket connections for command and state channels:
- **Command WebSocket**: Client sends commands to server (one-way)
- **State WebSocket**: Server sends state updates to client (one-way)

Both channels use Protocol Buffer (protobuf) encoding for messages.

## WebSocket Endpoints

### Standard HTTPS Endpoints (Port 443)
From nginx.conf, the relevant WebSocket endpoints are:

1. **Command WebSocket**: `/ws/ws_cmd`
   - Proxies to: `http://127.0.0.1:8083/ws/ws_cmd`
   - Direction: Client → Server
   - Purpose: Send commands to the system
   - Headers: `X-Jon-Client-Type: local-network`

2. **State WebSocket**: `/ws/ws_state`
   - Proxies to: `http://127.0.0.1:8099/ws/ws_state`
   - Direction: Server → Client
   - Purpose: Receive system state updates
   - Headers: Standard WebSocket upgrade headers

3. **Video Streams** (already implemented in PotatoClient):
   - `/ws/ws_video_heat`: Heat camera stream
   - `/ws/ws_video_day`: Day camera stream

### Client Certificate Endpoints (Port 444)
These endpoints require client certificates and are not used for now:
- Same paths as above but with additional certificate validation
- Headers include: `X-SSL-Client-Verify` and `X-SSL-Client-DN`

## Frontend Implementation Details

### Command Channel Architecture

1. **TypeScript Layer** (`cmdSenderShared.ts`):
   ```typescript
   // Commands are sent via BroadcastChannel
   export const cmdChannel = new BroadcastChannel('cmd');
   
   // Message format
   cmdChannel.postMessage({
     pld: encodedProtobufMessage,  // Uint8Array
     shouldBuffer: true            // Whether to queue if disconnected
   });
   ```

2. **Command Worker** (`cmdWorker.js`):
   - Shared worker that manages WebSocket connection
   - Lazy connection: Only connects when there's a message to send
   - Automatic reconnection on disconnect
   - Message queuing when disconnected (up to 100 messages)
   - Binary message format: `arraybuffer`

3. **Command Manager** (`cmdConnectionManager.js`):
   - Determines transport type (WebSocket vs WebTransport)
   - For standard mode: Uses WebSocket at `wss://domain/ws/ws_cmd`
   - Initializes and manages the command worker

### State Channel Architecture

1. **TypeScript Layer** (`deviceStateDispatch.ts`):
   ```typescript
   // State updates received via BroadcastChannel
   private deviceStateChannel: BroadcastChannel;
   
   // Channel name varies by transport mode:
   // - WebSocket: 'deviceState_ws'
   // - WebTransport: 'deviceState_wt'
   ```

2. **State Worker** (`webSocketManagerWorker.js`):
   - Uses `WebSocketManager` class for connection management
   - Automatic reconnection every 5 seconds on disconnect
   - Binary message format: `arraybuffer`
   - Forwards received messages to BroadcastChannel

3. **WebSocketManager** (`vsocketProxyUtil.js`):
   - Core WebSocket management class
   - Handles connection lifecycle
   - Fixed 5-second reconnect interval
   - Binary message handling

### Protocol Buffer Messages

1. **Command Messages**:
   - Root message: `Cmd.Root`
   - Protocol version: 1
   - Common commands:
     - `ping`: Keep-alive message
     - `frozen`: Freeze state command
     - `rotary`: Platform control commands

2. **State Messages**:
   - Root message: `JonGUIState`
   - Contains subsystems:
     - `system`: System status
     - `gps`: GPS data
     - `compass`: Compass data
     - `rotary`: Platform position
     - `cameraDay`/`cameraHeat`: Camera settings
     - `lrf`: Laser range finder data
     - And more...

### Transport Mode Selection

The system supports multiple transport modes:

1. **URL Parameters**:
   - `?transport=tcp`: Force WebSocket
   - `?transport=udp`: Force WebTransport
   - `?transport=auto`: Auto-select (default)
   - `?mode=legacy`: Force WebSocket for all connections

2. **Channel Naming**:
   - Command channels: No suffix (e.g., `cmd`, `cmdFeedback`)
   - State channels: Transport-specific suffix
     - WebSocket: `_ws` suffix (e.g., `deviceState_ws`)
     - WebTransport: `_wt` suffix (e.g., `deviceState_wt`)

## Key Implementation Notes

1. **Certificate Validation**: 
   - Currently ignoring certificate errors (similar to video streams)
   - Using standard HTTPS endpoints (port 443)

2. **Message Flow**:
   - Commands: UI → BroadcastChannel → Worker → WebSocket → Server
   - State: Server → WebSocket → Worker → BroadcastChannel → UI

3. **Error Handling**:
   - Automatic reconnection for both channels
   - Message queuing for commands when disconnected
   - State updates are dropped when disconnected

4. **Binary Protocol**:
   - All messages use protobuf binary encoding
   - WebSocket `binaryType` set to `arraybuffer`

## Implementation Requirements for Clojure

To implement WebSocket support in PotatoClient:

1. **WebSocket Client Library**: Need a Clojure WebSocket client that supports:
   - Binary messages (arraybuffer)
   - Auto-reconnection
   - SSL/TLS with certificate validation bypass

2. **Protocol Buffer Support**: 
   - Already implemented in PotatoClient
   - Use existing protobuf encoding/decoding

3. **Command Channel**:
   - Connect to: `wss://[domain]/ws/ws_cmd`
   - Send protobuf-encoded `Cmd.Root` messages
   - One-way communication (send only)

4. **State Channel**:
   - Connect to: `wss://[domain]/ws/ws_state`
   - Receive protobuf-encoded `JonGUIState` messages
   - One-way communication (receive only)

5. **Threading Model**:
   - Separate thread/process for each WebSocket connection
   - Non-blocking message handling
   - Queue for outgoing commands

## Clojure Implementation Plan

### 1. WebSocket Library Selection

Since we just need two simple WebSocket clients for binary protobuf messages, the best approach is:

**Direct Jetty WebSocket Client** (Recommended)
- No extra Clojure wrappers needed
- Direct control over binary message handling
- Already proven in production (Gniazdo just wraps this)
- Minimal dependencies
- Perfect for simple use cases

Why this approach:
- We only need 2 WebSocket connections (command and state)
- Messages are binary protobuf (Java objects)
- No complex WebSocket features needed
- Avoids extra abstraction layers
- Similar to how video streams use direct Java APIs

### 2. Proposed Architecture

```
src/
├── potatoclient/
│   ├── kotlin/
│   │   ├── websocket/
│   │   │   ├── CommandWebSocketClient.kt  ; Command channel with lazy reconnect
│   │   │   ├── StateWebSocketClient.kt    ; State channel with eager reconnect
│   │   │   └── WebSocketClientBase.kt     ; Shared WebSocket functionality
│   └── clojure/
│       └── websocket.clj                   ; Clojure interface to Kotlin classes
```

### 3. Implementation Steps

#### Step 1: Base WebSocket Client (Kotlin)
```kotlin
// WebSocketClientBase.kt
package potatoclient.kotlin.websocket

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

abstract class WebSocketClientBase(
    protected val url: String,
    protected val reconnectDelayMs: Long
) {
    protected val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    protected val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    protected var session: DefaultWebSocketSession? = null
    protected var isRunning = true
    
    protected val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 20_000
        }
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            }
        }
    }
    
    abstract suspend fun onConnect(session: DefaultWebSocketSession)
    abstract suspend fun onMessage(message: Frame.Binary)
    abstract fun onError(error: String)
    abstract fun onClose(reason: String?)
    
    protected suspend fun connectLoop() {
        while (isRunning) {
            try {
                client.webSocket(url) {
                    session = this
                    _isConnected.value = true
                    onConnect(this)
                    
                    try {
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Binary -> onMessage(frame)
                                is Frame.Close -> break
                                else -> {} // Ignore other frame types
                            }
                        }
                    } finally {
                        _isConnected.value = false
                        session = null
                        val reason = closeReason.await()
                        onClose(reason?.message)
                    }
                }
            } catch (e: Exception) {
                _isConnected.value = false
                session = null
                onError("Connection failed: ${e.message}")
            }
            
            if (isRunning) {
                delay(reconnectDelayMs)
            }
        }
    }
    
    open fun start() {
        scope.launch {
            connectLoop()
        }
    }
    
    open fun stop() {
        isRunning = false
        scope.cancel()
        runBlocking {
            session?.close(CloseReason(CloseReason.Codes.GOING_AWAY, "Client shutdown"))
        }
        client.close()
    }
}
```

#### Step 2: Command WebSocket Client (Kotlin)
```kotlin
// CommandWebSocketClient.kt
package potatoclient.kotlin.websocket

import cmd.JonSharedCmd
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class CommandWebSocketClient(
    domain: String,
    private val errorCallback: (String) -> Unit,
    private val isDevelopment: Boolean
) : WebSocketClientBase("wss://$domain/ws/ws_cmd", 5000) {
    
    private val commandQueue = ConcurrentLinkedQueue<JonSharedCmd.Root>()
    private var isConnectedToServer = false
    private var pingJob: Job? = null
    
    override suspend fun onConnect(session: DefaultWebSocketSession) {
        isConnectedToServer = true
        
        // Send any queued commands
        while (commandQueue.isNotEmpty()) {
            commandQueue.poll()?.let { command ->
                sendCommandInternal(session, command)
            }
        }
        
        // Start ping sender coroutine (matches TypeScript - every 300ms)
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isConnected.value) {
                delay(300) // Send ping every 300ms
                sendPing()
            }
        }
    }
    
    override suspend fun onMessage(message: Frame.Binary) {
        // Command channel is one-way, no messages expected from server
    }
    
    override fun onError(error: String) {
        errorCallback(error)
    }
    
    override fun onClose(reason: String?) {
        isConnectedToServer = false
        pingJob?.cancel()
        pingJob = null
        // Will reconnect automatically due to lazy reconnection
    }
    
    private suspend fun sendCommandInternal(session: DefaultWebSocketSession, command: JonSharedCmd.Root) {
        try {
            // Validate in development mode
            if (isDevelopment) {
                // Add proto validation here if using buf.validate
                // command.validate()
            }
            
            val data = command.toByteArray()
            session.send(Frame.Binary(true, data))
        } catch (e: Exception) {
            onError("Failed to send command: ${e.message}")
        }
    }
    
    // Override start to implement lazy connection (like TypeScript)
    override fun start() {
        // Don't connect immediately, wait for first command
    }
    
    private fun ensureConnected() {
        if (!isConnectedToServer && isRunning) {
            scope.launch {
                connectLoop()
            }
        }
    }
    
    fun sendCommand(command: JonSharedCmd.Root) {
        commandQueue.offer(command)
        
        // Lazy connection - connect only when there's something to send
        ensureConnected()
        
        // If connected, send immediately
        session?.let { sess ->
            scope.launch {
                commandQueue.poll()?.let { cmd ->
                    sendCommandInternal(sess, cmd)
                }
            }
        }
    }
    
    fun sendCommandBuilder(builderConfig: (JonSharedCmd.Root.Builder) -> Unit) {
        val builder = JonSharedCmd.Root.newBuilder()
        builder.protocolVersion = 1
        builderConfig(builder)
        sendCommand(builder.build())
    }
    
    fun sendPing() {
        sendCommandBuilder { builder ->
            builder.ping = JonSharedCmd.Ping.newBuilder().build()
        }
    }
}
```

#### Step 3: State WebSocket Client (Kotlin)
```kotlin
// StateWebSocketClient.kt
package potatoclient.kotlin.websocket

import ser.JonSharedDataTypes
import io.ktor.websocket.*
import kotlinx.coroutines.*
import com.google.protobuf.InvalidProtocolBufferException

class StateWebSocketClient(
    domain: String,
    private val stateCallback: (JonSharedDataTypes.JonGUIState) -> Unit,
    private val errorCallback: (String) -> Unit,
    private val isDevelopment: Boolean
) : WebSocketClientBase("wss://$domain/ws/ws_state", 1000) { // 1 second reconnect delay
    
    override suspend fun onConnect(session: DefaultWebSocketSession) {
        // State channel connects immediately and stays connected
    }
    
    override suspend fun onMessage(message: Frame.Binary) {
        try {
            val data = message.readBytes()
            
            // Parse protobuf message
            val state = JonSharedDataTypes.JonGUIState.parseFrom(data)
            
            // Validate in development mode
            if (isDevelopment) {
                // Add proto validation here if using buf.validate
                // state.validate()
            }
            
            // Deliver to callback - Clojure will handle thread switching
            stateCallback(state)
        } catch (e: InvalidProtocolBufferException) {
            onError("Failed to parse state message: ${e.message}")
        } catch (e: Exception) {
            onError("Error processing state message: ${e.message}")
        }
    }
    
    override fun onError(error: String) {
        errorCallback(error)
    }
    
    override fun onClose(reason: String?) {
        // Will reconnect automatically with 1 second delay
    }
}
```

#### Step 4: Clojure Interface
```clojure
(ns potatoclient.websocket
  (:require [potatoclient.state :as state]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime])
  (:import [potatoclient.kotlin.websocket CommandWebSocketClient StateWebSocketClient]
           [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Frozen]
           [cmd JonSharedCmd$RotaryPlatform$Root JonSharedCmd$RotaryPlatform$Axis]
           [ser JonSharedDataTypes$JonGUIState]))

(defonce ^:private websocket-clients (atom {}))

(defn- handle-state-update
  "Process incoming state updates"
  [^JonSharedDataTypes$JonGUIState state-msg]
  ;; Update application state atoms
  (when-let [system (.getSystem state-msg)]
    (swap! state/app-state assoc :system system))
  (when-let [rotary (.getRotary state-msg)]
    (swap! state/app-state assoc :rotary rotary))
  ;; etc...
  )

(defn- handle-error
  "Handle WebSocket errors"
  [source error]
  (logging/log-error (str "WebSocket error [" source "]: " error)))

(defn start-websockets!
  "Initialize WebSocket connections"
  [domain]
  (let [is-dev (runtime/development-build?)
        
        ;; Command channel (lazy connection)
        cmd-client (CommandWebSocketClient. 
                     domain
                     (partial handle-error "command")
                     is-dev)
        
        ;; State channel (eager connection)
        state-client (StateWebSocketClient.
                       domain
                       handle-state-update
                       (partial handle-error "state")
                       is-dev)]
    
    ;; Start connections
    (.start cmd-client)    ; Won't connect until first command
    (.start state-client)  ; Connects immediately
    
    ;; Store clients
    (swap! websocket-clients assoc
           :command cmd-client
           :state state-client)
    
    ;; Return control functions
    {:send-command (fn [cmd-msg] (.sendCommand cmd-client cmd-msg))
     :send-ping (fn [] 
                  (.sendCommandBuilder cmd-client
                    (fn [^JonSharedCmd$Root$Builder builder]
                      (.setPing builder (JonSharedCmd$Ping/newBuilder)))))
     :stop (fn []
             (.stop cmd-client)
             (.stop state-client)
             (reset! websocket-clients {}))}))

;; Convenience functions
(defn send-command!
  "Send a protobuf command"
  [cmd-msg]
  (when-let [client (:command @websocket-clients)]
    (.sendCommand client cmd-msg)))

(defn send-ping!
  "Send a ping command"
  []
  (when-let [client (:command @websocket-clients)]
    (.sendCommandBuilder client
      (fn [^JonSharedCmd$Root$Builder builder]
        (.setPing builder (JonSharedCmd$Ping/newBuilder))))))
```

### 4. Connection Behavior (Matching TypeScript)

**Command Channel**:
- **Lazy connection**: Only connects when there's a command to send
- **Reconnect**: After disconnect, waits for next command before reconnecting
- **Ping**: Sends ping every 300ms when connected (matches TypeScript)
- **Queue**: Buffers commands when disconnected (max 100 messages)
- **Reconnect delay**: 5 seconds
- **Ping buffering**: Pings are NOT buffered when disconnected

**State Channel**:
- **Eager connection**: Connects immediately on start
- **Reconnect**: Immediately after disconnect (1 second delay)
- **Direction**: Receive-only (no sending)
- **No buffering**: Messages missed while disconnected are lost
- **Reconnect delay**: 1 second (no backoff)

### 5. Implementation Notes from TypeScript

**Important details from the TypeScript implementation:**

1. **BroadcastChannel Pattern**: TypeScript uses BroadcastChannels for IPC between main thread and workers. In Kotlin/Clojure, we use direct callbacks instead.

2. **Message Format**: Commands are sent as `{pld: Uint8Array, shouldBuffer: boolean}` where:
   - `pld`: The protobuf-encoded message
   - `shouldBuffer`: Whether to queue if disconnected (false for pings)

3. **Connection State**: Command worker tracks `isConnecting` to prevent multiple simultaneous connection attempts

4. **Visibility Check**: TypeScript only sends pings when document is visible (`document.visibilityState === 'visible'`). In desktop app, this isn't needed.

5. **Queue Management**: 
   - Maximum 100 messages in queue
   - FIFO - oldest messages dropped when full
   - Queue flushed on reconnect

6. **Error Recovery**: If send fails while connected, message is re-queued

7. **Worker Architecture**: TypeScript uses SharedWorkers for WebSocket management. Our Kotlin implementation achieves the same with coroutines.

8. **WebSocket Close Codes**: Both implementations handle standard WebSocket close codes (1000-1015) with proper logging.

### 6. Key Benefits of This Approach

1. **Clean Separation**:
   - Java handles all WebSocket complexity
   - Protobuf handling stays in Java (natural fit)
   - Clojure gets a simple, clean interface

2. **Type Safety**:
   - Java provides compile-time type checking for protobuf
   - Validation can be toggled for dev/prod
   - Clear error handling boundaries

3. **Reusability**:
   - Java classes can be used by other JVM languages
   - Easy to test in isolation
   - Similar pattern to Kotlin video handlers

4. **Performance**:
   - Direct protobuf serialization in Java
   - No conversion overhead
   - Efficient binary message handling

5. **Maintainability**:
   - WebSocket logic isolated from business logic
   - Easy to debug and monitor
   - Clear upgrade path if protocols change

### 5. Development Phases

**Phase 1: Basic Implementation**
- WebSocket client wrapper with reconnection
- Simple command sending
- Basic state receiving

**Phase 2: Integration**
- Hook into existing UI components
- Add command builders for common operations
- State update dispatching

**Phase 3: Enhanced Features**
- Command queuing and retry
- Connection status indicators
- Performance optimization

### 6. Dependencies to Add

```clojure
;; In deps.edn (add to existing Kotlin dependencies)
{:deps {io.ktor/ktor-client-core {:mvn/version "2.3.12"}
        io.ktor/ktor-client-cio {:mvn/version "2.3.12"}
        io.ktor/ktor-client-websockets {:mvn/version "2.3.12"}}}
```

**Note**: Using Kotlin with Ktor is the best choice because:
- PotatoClient already uses Kotlin for video streams
- Ktor provides excellent coroutine-based WebSocket support
- Matches the TypeScript implementation behavior exactly
- Natural protobuf handling with Kotlin
- Consistent with existing architecture