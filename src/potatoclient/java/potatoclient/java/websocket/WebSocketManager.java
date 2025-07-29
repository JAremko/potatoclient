package potatoclient.java.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import cmd.JonSharedCmd;
import ser.JonSharedData;
import com.google.protobuf.InvalidProtocolBufferException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Unified WebSocket manager for command and state channels.
 * Handles both outgoing commands and incoming state updates.
 */
public class WebSocketManager {
    private final String domain;
    private final Consumer<String> errorCallback;
    private final Consumer<byte[]> stateCallback;
    
    private CommandClient commandClient;
    private StateClient stateClient;
    
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isRunning;
    
    private static final long PING_INTERVAL_MS = 300;
    private static final long RECONNECT_INTERVAL_MS = 300;
    
    public WebSocketManager(String domain, Consumer<String> errorCallback, Consumer<byte[]> stateCallback) {
        this.domain = domain;
        this.errorCallback = errorCallback;
        this.stateCallback = stateCallback;
        this.isRunning = new AtomicBoolean(false);
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "WebSocket-" + r.hashCode());
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Start both WebSocket connections
     */
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            commandClient = new CommandClient();
            stateClient = new StateClient();
            
            commandClient.start();
            stateClient.start();
        }
    }
    
    /**
     * Stop both WebSocket connections
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            if (commandClient != null) {
                commandClient.stop();
            }
            if (stateClient != null) {
                stateClient.stop();
            }
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
    }
    
    /**
     * Send a command (built from Clojure as a protobuf message)
     */
    public boolean sendCommand(JonSharedCmd.Root command) {
        if (commandClient != null) {
            return commandClient.send(command);
        }
        return false;
    }
    
    /**
     * Check if both connections are active
     */
    public boolean isConnected() {
        return commandClient != null && commandClient.isConnected() &&
               stateClient != null && stateClient.isConnected();
    }
    
    /**
     * Get command queue size
     */
    public int getCommandQueueSize() {
        return commandClient != null ? commandClient.getQueueSize() : 0;
    }
    
    /**
     * Helper to create WebSocket URI
     */
    private URI createWebSocketURI(String path) {
        // Parse domain which might include port (e.g., "localhost:8080")
        String host = domain;
        String port = "";
        int colonIndex = domain.lastIndexOf(':');
        if (colonIndex > 0 && colonIndex < domain.length() - 1) {
            // Check if it's a port (not part of IPv6 address)
            String portPart = domain.substring(colonIndex + 1);
            try {
                Integer.parseInt(portPart);
                host = domain.substring(0, colonIndex);
                port = ":" + portPart;
            } catch (NumberFormatException e) {
                // Not a port, use whole domain
            }
        }
        
        // Use ws:// for localhost/127.0.0.1, wss:// for others
        String protocol = (host.equals("localhost") || host.equals("127.0.0.1")) ? "ws://" : "wss://";
        return URI.create(protocol + host + port + path);
    }
    
    // Inner class for command WebSocket
    private class CommandClient {
        private volatile WebSocketClient wsClient;
        private final BlockingQueue<JonSharedCmd.Root> commandQueue;
        private final AtomicBoolean isConnected;
        private ScheduledFuture<?> pingTask;
        private ScheduledFuture<?> reconnectTask;
        
        CommandClient() {
            this.commandQueue = new LinkedBlockingQueue<>(100);
            this.isConnected = new AtomicBoolean(false);
        }
        
        void start() {
            startReconnectTask();
        }
        
        void stop() {
            stopReconnectTask();
            stopPingTask();
            isConnected.set(false);
            if (wsClient != null) {
                try {
                    wsClient.closeBlocking();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                wsClient = null;
            }
            commandQueue.clear();
        }
        
        private synchronized void connect() {
            if (!isRunning.get()) {
                return;
            }
            
            // Always create a new client for connection/reconnection
            if (wsClient != null) {
                try {
                    wsClient.closeBlocking();
                } catch (Exception e) {
                    // Ignore errors when closing old client
                }
                wsClient = null;
            }
            
            try {
                createWebSocketClient();
                wsClient.connect();
            } catch (Exception e) {
                errorCallback.accept("Command connection failed: " + e.getMessage());
                wsClient = null;  // Clear reference on failure
            }
        }
        
        private void createWebSocketClient() {
            URI uri = createWebSocketURI("/ws/ws_cmd");
            
            this.wsClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    isConnected.set(true);
                    drainQueue();
                    startPingTask();
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected.set(false);
                    stopPingTask();
                    // Schedule reconnection if still running
                    if (isRunning.get()) {
                        scheduler.schedule(() -> connect(), RECONNECT_INTERVAL_MS, TimeUnit.MILLISECONDS);
                    }
                }
                
                @Override
                public void onError(Exception ex) {
                    errorCallback.accept("Command WebSocket error: " + ex.getMessage());
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
            
            wsClient.addHeader("X-Jon-Client-Type", "local-network");
        }
        
        private void drainQueue() {
            while (!commandQueue.isEmpty() && isConnected.get()) {
                JonSharedCmd.Root cmd = commandQueue.poll();
                if (cmd != null) {
                    try {
                        byte[] data = cmd.toByteArray();
                        wsClient.send(data);
                    } catch (Exception e) {
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
                    if (!isConnected.get() && isRunning.get()) {
                        connect();
                    }
                },
                RECONNECT_INTERVAL_MS,
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
        
        private void sendPing() {
            if (isConnected.get()) {
                JonSharedCmd.Root ping = JonSharedCmd.Root.newBuilder()
                    .setProtocolVersion(3)
                    .setPing(JonSharedCmd.Ping.newBuilder().build())
                    .build();
                send(ping);
            }
        }
        
        boolean send(JonSharedCmd.Root command) {
            boolean queued = commandQueue.offer(command);
            
            if (queued && isConnected.get()) {
                drainQueue();
            }
            
            return queued;
        }
        
        boolean isConnected() {
            return isConnected.get() && wsClient != null && wsClient.isOpen();
        }
        
        int getQueueSize() {
            return commandQueue.size();
        }
    }
    
    // Inner class for state WebSocket
    private class StateClient {
        private volatile WebSocketClient wsClient;
        private final AtomicBoolean isConnected;
        private ScheduledFuture<?> reconnectTask;
        
        StateClient() {
            this.isConnected = new AtomicBoolean(false);
        }
        
        void start() {
            startReconnectTask();
        }
        
        void stop() {
            stopReconnectTask();
            isConnected.set(false);
            if (wsClient != null) {
                try {
                    wsClient.closeBlocking();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                wsClient = null;
            }
        }
        
        private synchronized void connect() {
            if (!isRunning.get()) {
                return;
            }
            
            // Always create a new client for connection/reconnection
            if (wsClient != null) {
                try {
                    wsClient.closeBlocking();
                } catch (Exception e) {
                    // Ignore errors when closing old client
                }
                wsClient = null;
            }
            
            try {
                createWebSocketClient();
                wsClient.connect();
            } catch (Exception e) {
                errorCallback.accept("State connection failed: " + e.getMessage());
                wsClient = null;  // Clear reference on failure
            }
        }
        
        private void createWebSocketClient() {
            URI uri = createWebSocketURI("/ws/ws_state");
            
            this.wsClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    isConnected.set(true);
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected.set(false);
                    // Schedule reconnection if still running
                    if (isRunning.get()) {
                        scheduler.schedule(() -> connect(), RECONNECT_INTERVAL_MS, TimeUnit.MILLISECONDS);
                    }
                }
                
                @Override
                public void onError(Exception ex) {
                    errorCallback.accept("State WebSocket error: " + ex.getMessage());
                }
                
                @Override
                public void onMessage(String message) {
                    // State channel doesn't receive text messages
                }
                
                @Override
                public void onMessage(ByteBuffer bytes) {
                    try {
                        // Forward raw bytes to Clojure for processing
                        stateCallback.accept(bytes.array());
                    } catch (Exception e) {
                        errorCallback.accept("Error processing state message: " + e.getMessage());
                    }
                }
            };
            
            wsClient.addHeader("X-Jon-Client-Type", "local-network");
        }
        
        private void startReconnectTask() {
            stopReconnectTask();
            reconnectTask = scheduler.scheduleWithFixedDelay(
                () -> {
                    if (!isConnected.get() && isRunning.get()) {
                        connect();
                    }
                },
                RECONNECT_INTERVAL_MS,
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
        
        boolean isConnected() {
            return isConnected.get() && wsClient != null && wsClient.isOpen();
        }
    }
}