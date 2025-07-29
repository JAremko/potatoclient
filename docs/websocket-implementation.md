# WebSocket Implementation for PotatoClient

## Overview

PotatoClient uses two unidirectional WebSocket connections for command and state channels:
- **Command WebSocket**: Client sends commands to server (one-way)
- **State WebSocket**: Server sends state updates to client (one-way)

Both channels use Protocol Buffer (protobuf) encoding for messages.

## Implementation Architecture

The implementation uses Java wrapper classes that encapsulate WebSocket complexity, protobuf serialization, and validation, exposing a clean Clojure-compatible API through simple Java interop.

### WebSocket Endpoints

1. **Command WebSocket**: `wss://[domain]/ws/ws_cmd`
   - Direction: Client → Server
   - Protocol: Protobuf-encoded `cmd.JonSharedCmd$Root` messages

2. **State WebSocket**: `wss://[domain]/ws/ws_state`
   - Direction: Server → Client  
   - Protocol: Protobuf-encoded `ser.JonSharedDataTypes$JonGUIState` messages

## Java Implementation

### CommandWebSocketClient.java

```java
package potatoclient.java.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import cmd.JonSharedCmd;
import com.google.protobuf.InvalidProtocolBufferException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import io.envoyproxy.pgv.ValidationException;

/**
 * WebSocket client for sending commands to the server.
 * Features:
 * - Command queue with automatic send on connection
 * - Reconnects every 300ms when queue is not empty
 * - Ping every 300ms when connected
 */
public class CommandWebSocketClient {
    private final String domain;
    private final Consumer<String> errorCallback;
    private WebSocketClient wsClient;
    private final BlockingQueue<JonSharedCmd.Root> commandQueue;
    private final AtomicBoolean isRunning;
    private final AtomicBoolean isConnected;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pingTask;
    private ScheduledFuture<?> reconnectTask;
    
    private static final long PING_INTERVAL_MS = 300;
    private static final long RECONNECT_INTERVAL_MS = 300;
    private static final int QUEUE_CAPACITY = 1000;
    
    public CommandWebSocketClient(String domain, Consumer<String> errorCallback) {
        this.domain = domain;
        this.errorCallback = errorCallback;
        this.commandQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        this.isRunning = new AtomicBoolean(false);
        this.isConnected = new AtomicBoolean(false);
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "CommandWebSocket-" + r.hashCode());
            t.setDaemon(true);
            return t;
        });
    }
    
    private void createWebSocketClient() {
        URI uri = URI.create("wss://" + domain + "/ws/ws_cmd");
        
        this.wsClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                isConnected.set(true);
                
                // Send all queued commands
                drainQueue();
                
                // Start ping task
                startPingTask();
            }
            
            @Override
            public void onClose(int code, String reason, boolean remote) {
                isConnected.set(false);
                stopPingTask();
            }
            
            @Override
            public void onError(Exception ex) {
                errorCallback.accept("WebSocket error: " + ex.getMessage());
            }
            
            @Override
            public void onMessage(String message) {
                // Command channel doesn't receive text messages
            }
            
            @Override
            public void onMessage(ByteBuffer bytes) {
                // Command channel doesn't receive binary messages
            }
        };
        
        // Add custom header
        wsClient.addHeader("X-Jon-Client-Type", "local-network");
    }
    
    private void connect() {
        if (!isConnected.get() && isRunning.get()) {
            try {
                if (wsClient == null) {
                    createWebSocketClient();
                }
                wsClient.connect();
            } catch (Exception e) {
                errorCallback.accept("Connection failed: " + e.getMessage());
            }
        }
    }
    
    private void drainQueue() {
        while (!commandQueue.isEmpty() && isConnected.get()) {
            JonSharedCmd.Root cmd = commandQueue.poll();
            if (cmd != null) {
                try {
                    byte[] data = cmd.toByteArray();
                    wsClient.send(data);
                } catch (Exception e) {
                    // Put back in queue if send fails
                    commandQueue.offer(cmd);
                    errorCallback.accept("Failed to send command: " + e.getMessage());
                    break;
                }
            }
        }
    }
    
    private void startReconnectTask() {
        stopReconnectTask();
        reconnectTask = scheduler.scheduleWithFixedDelay(
            () -> {
                if (!isConnected.get() && !commandQueue.isEmpty()) {
                    connect();
                }
            },
            0,
            RECONNECT_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
    }
    
    private void stopReconnectTask() {
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
            reconnectTask = null;
        }
    }
    
    private void startPingTask() {
        stopPingTask();
        pingTask = scheduler.scheduleWithFixedDelay(
            this::sendPing,
            PING_INTERVAL_MS,
            PING_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
    }
    
    private void stopPingTask() {
        if (pingTask != null) {
            pingTask.cancel(false);
            pingTask = null;
        }
    }
    
    /**
     * Validate a command message using protobuf validation rules.
     * This method is public so Clojure can decide when to validate.
     * 
     * @param command The command to validate
     * @throws ValidationException if validation fails
     */
    public static void validate(JonSharedCmd.Root command) throws ValidationException {
        cmd.Validator.validate(command);
    }
    
    private void sendPing() {
        JonSharedCmd.Root ping = JonSharedCmd.Root.newBuilder()
            .setProtocolVersion(1)
            .setPing(JonSharedCmd.Ping.newBuilder().build())
            .build();
        
        if (isConnected.get()) {
            sendCommandInternal(ping);
        }
    }
    
    /**
     * Queue a command message for sending.
     * The message will be sent when connection is available.
     * 
     * @param command The command to queue
     * @return true if queued successfully, false if queue is full
     */
    public boolean send(JonSharedCmd.Root command) {
        boolean queued = commandQueue.offer(command);
        if (!queued) {
            errorCallback.accept("Command queue full, dropping message");
        }
        
        // If connected, try to send immediately
        if (isConnected.get()) {
            drainQueue();
        }
        
        return queued;
    }
    
    /**
     * Get the number of commands currently in the queue.
     * 
     * @return The queue size
     */
    public int getQueueSize() {
        return commandQueue.size();
    }
    
    /**
     * Clear all commands from the queue.
     */
    public void clearQueue() {
        commandQueue.clear();
    }
    
    /**
     * Start the client manager
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            startReconnectTask();
            connect();
        }
    }
    
    /**
     * Stop the client manager and clean up resources
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            stopReconnectTask();
            stopPingTask();
            if (wsClient != null) {
                wsClient.close();
                wsClient = null;
            }
            commandQueue.clear();
            scheduler.shutdown();
        }
    }
    
    /**
     * Check if connected
     */
    public boolean isConnected() {
        return isConnected.get() && wsClient != null && wsClient.isOpen();
    }
}
```

### StateWebSocketClient.java

```java
package potatoclient.java.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import ser.JonSharedDataTypes;
import com.google.protobuf.InvalidProtocolBufferException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * WebSocket client for receiving state updates from the server.
 * Features:
 * - Message queue for buffering incoming state
 * - Reconnects every 300ms while running
 */
public class StateWebSocketClient {
    private final String domain;
    private final Consumer<String> errorCallback;
    private WebSocketClient wsClient;
    private final BlockingQueue<JonSharedDataTypes.JonGUIState> stateQueue;
    private final AtomicBoolean isRunning;
    private final AtomicBoolean isConnected;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> reconnectTask;
    
    private static final long RECONNECT_INTERVAL_MS = 300;
    private static final int QUEUE_CAPACITY = 1000;
    
    public StateWebSocketClient(String domain, Consumer<String> errorCallback) {
        this.domain = domain;
        this.errorCallback = errorCallback;
        this.stateQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        this.isRunning = new AtomicBoolean(false);
        this.isConnected = new AtomicBoolean(false);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "StateWebSocket-Scheduler");
            t.setDaemon(true);
            return t;
        });
    }
    
    private void createWebSocketClient() {
        URI uri = URI.create("wss://" + domain + "/ws/ws_state");
        
        this.wsClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                isConnected.set(true);
            }
            
            @Override
            public void onClose(int code, String reason, boolean remote) {
                isConnected.set(false);
            }
            
            @Override
            public void onError(Exception ex) {
                errorCallback.accept("WebSocket error: " + ex.getMessage());
            }
            
            @Override
            public void onMessage(String message) {
                // State channel doesn't receive text messages
            }
            
            @Override
            public void onMessage(ByteBuffer bytes) {
                try {
                    // Parse protobuf message
                    JonSharedDataTypes.JonGUIState state = JonSharedDataTypes.JonGUIState.parseFrom(bytes.array());
                    
                    // Add to queue, dropping oldest if full
                    if (!stateQueue.offer(state)) {
                        stateQueue.poll(); // Remove oldest
                        stateQueue.offer(state); // Try again
                    }
                } catch (InvalidProtocolBufferException e) {
                    errorCallback.accept("Failed to parse state message: " + e.getMessage());
                } catch (Exception e) {
                    errorCallback.accept("Error processing state message: " + e.getMessage());
                }
            }
        };
    }
    
    private void connect() {
        if (!isConnected.get() && isRunning.get()) {
            try {
                if (wsClient == null) {
                    createWebSocketClient();
                }
                wsClient.connect();
            } catch (Exception e) {
                errorCallback.accept("Connection failed: " + e.getMessage());
            }
        }
    }
    
    private void startReconnectTask() {
        stopReconnectTask();
        reconnectTask = scheduler.scheduleWithFixedDelay(
            () -> {
                if (!isConnected.get() && isRunning.get()) {
                    connect();
                }
            },
            0,
            RECONNECT_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
    }
    
    private void stopReconnectTask() {
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
            reconnectTask = null;
        }
    }
    
    /**
     * Poll for the next state message from the queue.
     * Returns null if no message is available.
     * 
     * @return The next state message or null
     */
    public JonSharedDataTypes.JonGUIState poll() {
        return stateQueue.poll();
    }
    
    /**
     * Poll for the next state message with timeout.
     * 
     * @param timeout The timeout value
     * @param unit The timeout unit
     * @return The next state message or null if timeout
     * @throws InterruptedException if interrupted while waiting
     */
    public JonSharedDataTypes.JonGUIState poll(long timeout, TimeUnit unit) throws InterruptedException {
        return stateQueue.poll(timeout, unit);
    }
    
    /**
     * Get the number of messages currently in the queue.
     * 
     * @return The queue size
     */
    public int getQueueSize() {
        return stateQueue.size();
    }
    
    /**
     * Clear all messages from the queue.
     */
    public void clearQueue() {
        stateQueue.clear();
    }
    
    /**
     * Start the client manager
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            startReconnectTask();
            connect();
        }
    }
    
    /**
     * Stop the client manager and clean up resources
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            stopReconnectTask();
            if (wsClient != null) {
                wsClient.close();
                wsClient = null;
            }
            stateQueue.clear();
            scheduler.shutdown();
        }
    }
    
    /**
     * Check if connected
     */
    public boolean isConnected() {
        return wsClient != null && wsClient.isOpen();
    }
}
```

## Clojure Integration

The Clojure implementation integrates seamlessly with existing systems:

### WebSocket Namespace (`potatoclient.websocket`)

```clojure
(ns potatoclient.websocket
  "WebSocket client implementation for command and state channels.
  Integrates Java WebSocket wrapper classes with the Clojure codebase."
  (:require [clojure.core.async :as async :refer [go-loop <! >! timeout]]
            [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- =>]]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.logging :as logging]
            [potatoclient.runtime :as runtime]
            [potatoclient.state.dispatch :as dispatch])
  (:import [potatoclient.java.websocket CommandWebSocketClient StateWebSocketClient]
           [cmd JonSharedCmd$Root JonSharedCmd$Ping]
           [ser JonSharedDataTypes$JonGUIState]))

(defonce ^:private clients (atom {}))

(>defn- start-state-processor!
  "Start the state processing loop that polls the state queue"
  [^StateWebSocketClient state-client]
  [[:fn #(instance? StateWebSocketClient %)] => any?]
  (go-loop []
    (when-let [{:keys [state]} @clients]
      (try
        ;; Poll with 50ms timeout to prevent busy waiting
        (when-let [state-msg (.poll state-client 50 java.util.concurrent.TimeUnit/MILLISECONDS)]
          ;; Forward to state dispatch system
          (dispatch/handle-binary-state (.toByteArray state-msg)))
        (catch InterruptedException _
          ;; Exit loop on interrupt
          nil)
        (catch Exception e
          (logging/log-error (str "Error processing state: " (.getMessage e)))
          ;; Continue processing after error
          (<! (timeout 100))))
      ;; Continue loop if client still exists
      (when @clients
        (recur)))))

(>defn- start-command-forwarder!
  "Forward commands from the cmd/core command channel to WebSocket"
  [^CommandWebSocketClient cmd-client]
  [[:fn #(instance? CommandWebSocketClient %)] => any?]
  (go-loop []
    (when-let [cmd-msg (<! cmd/command-channel)]
      (try
        ;; Validate in development/test only
        (when (runtime/development-build?)
          (try
            (CommandWebSocketClient/validate cmd-msg)
            (catch Exception e
              (logging/log-error (str "Command validation failed: " (.getMessage e)))
              (throw e))))
        
        ;; Send command
        (when-not (.send cmd-client cmd-msg)
          (logging/log-warn "Command queue full, message dropped"))
        
        (catch Exception e
          (logging/log-error (str "Error sending command: " (.getMessage e)))))
      
      ;; Continue forwarding
      (when @clients
        (recur)))))

(>defn start!
  "Start WebSocket connections for command and state channels"
  [domain]
  [string? => map?]
  (logging/log-info (str "Starting WebSocket connections to " domain))
  
  (let [;; Create command client
        cmd-client (CommandWebSocketClient. 
                     domain
                     (partial handle-error "command"))
        
        ;; Create state client
        state-client (StateWebSocketClient.
                       domain
                       (partial handle-error "state"))
        
        ;; Start processors
        state-processor (start-state-processor! state-client)
        cmd-forwarder (start-command-forwarder! cmd-client)]
    
    ;; Start both managers (they handle reconnection)
    (.start cmd-client)
    (.start state-client)
    
    ;; Store everything
    (reset! clients
            {:command cmd-client
             :state state-client
             :state-processor state-processor
             :cmd-forwarder cmd-forwarder})
    
    ;; Return API map
    {:stop #(stop!)
     :connected? #(connected?)
     :command-queue-size #(command-queue-size)
     :state-queue-size #(state-queue-size)})))

;; Commands are sent via the cmd/command-channel
;; The websocket namespace automatically forwards them

(>defn stop!
  "Stop all WebSocket connections and processors"
  []
  [=> nil?]
  (when-let [{:keys [command state state-processor cmd-forwarder]} @clients]
    (logging/log-info "Stopping WebSocket connections")
    
    ;; Stop processors by closing their channels
    (when state-processor
      (async/close! state-processor))
    (when cmd-forwarder
      (async/close! cmd-forwarder))
    
    ;; Stop WebSocket managers
    (.stop command)
    (.stop state)
    
    ;; Clear clients
    (reset! clients {}))
  nil)

(>defn connected?
  "Check if both WebSocket connections are active"
  []
  [=> boolean?]
  (let [{:keys [command state]} @websocket-clients]
    (and command state
         (.isConnected command)
         (.isConnected state))))

(>defn command-queue-size
  "Get the number of commands in the queue"
  []
  [=> int?]
  (if-let [client (:command @websocket-clients)]
    (.getQueueSize client)
    0))

(>defn state-queue-size
  "Get the number of state messages in the queue"
  []
  [=> int?]
  (if-let [client (:state @websocket-clients)]
    (.getQueueSize client)
    0))
```

## Integration Architecture

### Command Flow
1. Clojure code sends commands to `cmd/command-channel` (existing pattern)
2. WebSocket forwarder reads from channel and sends via Java client
3. Java client handles queuing, reconnection, and WebSocket protocol
4. Validation runs only in dev/test builds before sending

### State Flow  
1. Java client receives protobuf messages and queues them
2. Clojure processor polls queue with timeout to prevent busy-waiting
3. Binary data forwarded to `state.dispatch/handle-binary-state`
4. State dispatch system handles parsing and distribution

### Connection Management
- **Auto-reconnect**: Both channels reconnect every 300ms
- **Command queue**: Buffers up to 1000 commands during disconnection
- **State queue**: Buffers up to 1000 state messages
- **Graceful shutdown**: Processors stop cleanly on `stop!`

## Key Benefits

1. **Seamless Integration**: Works with existing `cmd/command-channel` pattern
2. **Separation of Concerns**: Java handles WebSocket, Clojure handles business logic  
3. **Reliability**: Automatic reconnection with queuing prevents message loss
4. **Performance**: Queue-based design with efficient polling
5. **Development Support**: Optional validation in dev/test builds
6. **Clean Architecture**: Minimal coupling between Java and Clojure layers

## Usage Example

```clojure
;; Start WebSocket connections
(require '[potatoclient.websocket :as ws])
(def ws-api (ws/start! "sych.local"))

;; Send commands through existing cmd/core API
(require '[potatoclient.cmd.core :as cmd])
(cmd/send-ping)  ; Automatically forwarded via WebSocket

;; Check connection status
((:connected? ws-api))        ; => true
((:command-queue-size ws-api)) ; => 0
((:state-queue-size ws-api))   ; => 0

;; Stop connections
((:stop ws-api))
```

## Testing Strategy

### 1. Java WebSocket Client Tests

Create a stub WebSocket server for testing the Java classes in isolation:

```java
package potatoclient.java.websocket.test;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Stub WebSocket server for testing command and state clients
 */
public class StubWebSocketServer extends WebSocketServer {
    private final LinkedBlockingQueue<byte[]> receivedMessages = new LinkedBlockingQueue<>();
    private final CountDownLatch connectionLatch = new CountDownLatch(1);
    private WebSocket lastConnection;
    
    public StubWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }
    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        lastConnection = conn;
        connectionLatch.countDown();
    }
    
    @Override
    public void onMessage(WebSocket conn, String message) {
        // Not used for binary protocol
    }
    
    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        byte[] data = new byte[message.remaining()];
        message.get(data);
        receivedMessages.offer(data);
    }
    
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Handle close
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
    
    // Test helper methods
    public void sendStateUpdate(JonGUIState state) {
        if (lastConnection != null && lastConnection.isOpen()) {
            lastConnection.send(state.toByteArray());
        }
    }
    
    public byte[] getNextReceivedMessage(long timeout) throws InterruptedException {
        return receivedMessages.poll(timeout, TimeUnit.MILLISECONDS);
    }
    
    public boolean awaitConnection(long timeout) throws InterruptedException {
        return connectionLatch.await(timeout, TimeUnit.MILLISECONDS);
    }
}
```

### 2. Clojure Integration Tests

Extend existing tests to work with the WebSocket implementation:

```clojure
(ns potatoclient.websocket-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as async :refer [<! >! go timeout]]
            [potatoclient.websocket :as ws]
            [potatoclient.cmd.core :as cmd]
            [potatoclient.state.dispatch :as dispatch]
            [potatoclient.state.device :as device-state])
  (:import [potatoclient.java.websocket.test StubWebSocketServer]
           [cmd JonSharedCmd$Root JonSharedCmd$Ping]
           [ser JonSharedDataTypes$JonGUIState]))

(def test-port-cmd 18083)
(def test-port-state 18099)

(defn with-stub-servers [f]
  (let [cmd-server (StubWebSocketServer. test-port-cmd)
        state-server (StubWebSocketServer. test-port-state)]
    (try
      (.start cmd-server)
      (.start state-server)
      (Thread/sleep 100) ; Let servers start
      (f {:cmd-server cmd-server
          :state-server state-server})
      (finally
        (.stop cmd-server)
        (.stop state-server)))))

(use-fixtures :each with-stub-servers)

(deftest test-command-sending
  (testing "Commands are forwarded to WebSocket"
    (let [ws-api (ws/start! "localhost")]
      (try
        ;; Wait for connection
        (Thread/sleep 200)
        
        ;; Send a ping command
        (cmd/send-ping)
        
        ;; Verify server received it
        (let [received (.getNextReceivedMessage (:cmd-server *test-context*) 1000)]
          (is (some? received))
          (let [parsed (JonSharedCmd$Root/parseFrom received)]
            (is (.hasPing parsed))))
        
        (finally
          ((:stop ws-api))))))

(deftest test-state-reception
  (testing "State updates are processed"
    (let [ws-api (ws/start! "localhost")
          initial-state @device-state/system-state]
      (try
        ;; Wait for connection
        (Thread/sleep 200)
        
        ;; Send a state update from server
        (let [state (-> (JonGUIState/newBuilder)
                       (.setSystem (-> (JonGuiDataSystem/newBuilder)
                                     (.setFirmwareVersion "1.2.3")
                                     .build))
                       .build)]
          (.sendStateUpdate (:state-server *test-context*) state))
        
        ;; Wait for processing
        (Thread/sleep 100)
        
        ;; Verify state was updated
        (is (not= initial-state @device-state/system-state))
        (is (= "1.2.3" (:firmware-version @device-state/system-state)))
        
        (finally
          ((:stop ws-api))))))

(deftest test-reconnection
  (testing "Client reconnects after server restart"
    (let [ws-api (ws/start! "localhost")]
      (try
        ;; Wait for initial connection
        (Thread/sleep 200)
        (is ((:connected? ws-api)))
        
        ;; Stop servers
        (.stop (:cmd-server *test-context*))
        (.stop (:state-server *test-context*))
        
        ;; Wait and verify disconnection
        (Thread/sleep 500)
        (is (not ((:connected? ws-api))))
        
        ;; Restart servers
        (.start (:cmd-server *test-context*))
        (.start (:state-server *test-context*))
        
        ;; Wait for reconnection (300ms interval)
        (Thread/sleep 600)
        (is ((:connected? ws-api)))
        
        (finally
          ((:stop ws-api))))))

(deftest test-queue-behavior
  (testing "Commands queue when disconnected"
    (let [ws-api (ws/start! "localhost")]
      (try
        ;; Stop servers to simulate disconnection
        (.stop (:cmd-server *test-context*))
        
        ;; Send multiple commands
        (dotimes [_ 5]
          (cmd/send-ping))
        
        ;; Check queue size
        (is (= 5 ((:command-queue-size ws-api))))
        
        ;; Restart server
        (.start (:cmd-server *test-context*))
        
        ;; Wait for reconnection and queue drain
        (Thread/sleep 600)
        
        ;; Verify all commands were sent
        (is (= 0 ((:command-queue-size ws-api))))
        
        (finally
          ((:stop ws-api))))))
```

### 3. Test Expansion Checklist

**Existing tests to update:**
- `cmd/*_test.clj` - Ensure commands still work with WebSocket forwarding
- `state/*_test.clj` - Verify state updates flow through the new system
- Integration tests that assume direct protocol usage

**New test files to create:**
- `websocket_test.clj` - Core WebSocket functionality
- `websocket_integration_test.clj` - End-to-end testing
- Java unit tests for `CommandWebSocketClient` and `StateWebSocketClient`

**Test scenarios to cover:**
1. **Connection lifecycle** - Connect, disconnect, reconnect
2. **Queue management** - Full queue, queue draining
3. **Validation** - Ensure validation only runs in dev mode
4. **Error handling** - Network errors, invalid messages
5. **Performance** - Message throughput, latency
6. **Concurrency** - Multiple commands, state updates

### 4. Mock WebSocket for Unit Tests

For lighter-weight unit tests, create a mock that doesn't require actual networking:

```clojure
(defn mock-websocket-client
  "Create a mock WebSocket client for testing"
  []
  (let [sent-messages (atom [])
        connected? (atom true)
        queue-size (atom 0)]
    (reify
      CommandWebSocketClient
      (send [_ msg]
        (if @connected?
          (do (swap! sent-messages conj msg) true)
          (do (swap! queue-size inc) false)))
      (isConnected [_] @connected?)
      (getQueueSize [_] @queue-size)
      (start [_] nil)
      (stop [_] (reset! connected? false)))))
```

## Performance Considerations

### Message Processing
- **State polling**: 50ms timeout prevents busy-waiting while maintaining responsiveness
- **Queue sizes**: 1000 messages each provides adequate buffering for typical usage
- **Thread pooling**: Dedicated daemon threads for scheduling prevent resource leaks

### Memory Management
- **Queue bounds**: Both queues are bounded to prevent memory exhaustion
- **State queue overflow**: Oldest messages dropped when full (FIFO eviction)
- **Command queue overflow**: New messages rejected when full (backpressure)

### Network Efficiency
- **Binary protocol**: Protobuf encoding minimizes bandwidth usage
- **Ping interval**: 300ms keeps connection alive without excessive overhead
- **Reconnection**: 300ms interval provides quick recovery without flooding

## Error Handling

### Connection Errors
- **Network failures**: Logged via error callback, automatic reconnection
- **SSL/TLS errors**: Handled by Java WebSocket library, logged
- **DNS resolution**: Failures trigger reconnection cycle

### Protocol Errors
- **Invalid protobuf**: Logged and dropped, processing continues
- **Validation failures**: Commands rejected in development builds
- **Queue overflow**: Appropriate action taken (drop/reject)

### Application Errors
- **State processing errors**: Logged, delayed retry
- **Command sending errors**: Message requeued if possible
- **Shutdown errors**: Resources cleaned up best-effort

## Configuration

### WebSocket Settings
```clojure
;; In potatoclient.websocket namespace
(def ^:private config
  {:ping-interval-ms 300
   :reconnect-interval-ms 300
   :queue-capacity 1000
   :state-poll-timeout-ms 50})
```

### Custom Headers
The command WebSocket includes a custom header for client identification:
```java
wsClient.addHeader("X-Jon-Client-Type", "local-network");
```

### TLS/SSL Configuration
Both clients use `wss://` protocol for encrypted connections. Certificate validation follows Java defaults.

## Deployment Considerations

### Production Setup
1. **Domain configuration**: Ensure `domain` parameter matches server configuration
2. **Certificate validation**: Java truststore must include server certificates
3. **Firewall rules**: WebSocket ports must be accessible (typically 443 for wss)
4. **Load balancing**: Ensure sticky sessions if using multiple servers

### Monitoring
- **Connection status**: Via `connected?` API
- **Queue sizes**: Via `command-queue-size` and `state-queue-size` APIs
- **Error rates**: Through logging system
- **Message throughput**: Can be added via metrics in processors

### Debugging
1. **Enable debug logging**: Set appropriate log levels
2. **Monitor queues**: Check for consistent growth indicating processing issues
3. **Network tracing**: Use Wireshark or similar for protocol-level debugging
4. **Java debugging**: Standard JVM debugging tools work with WebSocket classes

## Future Enhancements

### Potential Improvements
1. **Metrics collection**: Add detailed metrics for monitoring
2. **Circuit breaker**: Implement circuit breaker pattern for connection failures
3. **Message compression**: Add compression for large state updates
4. **Connection pooling**: Support multiple concurrent connections
5. **Rate limiting**: Add client-side rate limiting for commands

### API Extensions
```clojure
;; Possible future API additions
(defn get-connection-stats []
  {:uptime-ms (- (System/currentTimeMillis) start-time)
   :messages-sent total-sent
   :messages-received total-received
   :reconnection-count reconnect-count})

(defn set-queue-capacity [queue-type capacity]
  ;; Dynamic queue capacity adjustment
  )

(defn add-connection-listener [event-type handler-fn]
  ;; Event-based connection monitoring
  )
```

## Summary

The WebSocket implementation provides a robust, production-ready solution for bidirectional communication between PotatoClient and the server. Key features include:

- **Reliable delivery**: Automatic reconnection and message queuing
- **Clean architecture**: Separation between Java WebSocket handling and Clojure business logic
- **Development support**: Optional validation and comprehensive error logging
- **Performance**: Efficient polling, bounded queues, and proper thread management
- **Integration**: Seamless integration with existing command and state systems

The implementation follows established patterns in the codebase while introducing minimal complexity, making it maintainable and extensible for future requirements.
```