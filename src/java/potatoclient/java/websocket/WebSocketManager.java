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
    
    // Inner class for command WebSocket
    private class CommandClient {
        private WebSocketClient wsClient;
        private final BlockingQueue<JonSharedCmd.Root> commandQueue;
        private final AtomicBoolean isConnected;
        private ScheduledFuture<?> pingTask;
        private ScheduledFuture<?> reconnectTask;
        
        CommandClient() {
            this.commandQueue = new LinkedBlockingQueue<>(1000);
            this.isConnected = new AtomicBoolean(false);
        }
        
        void start() {
            startReconnectTask();
            connect();
        }
        
        void stop() {
            stopReconnectTask();
            stopPingTask();
            if (wsClient != null) {
                wsClient.close();
                wsClient = null;
            }
            commandQueue.clear();
        }
        
        private void connect() {
            if (!isConnected.get() && isRunning.get()) {
                try {
                    if (wsClient == null) {
                        createWebSocketClient();
                    }
                    wsClient.connect();
                } catch (Exception e) {
                    errorCallback.accept("Command connection failed: " + e.getMessage());
                }
            }
        }
        
        private void createWebSocketClient() {
            URI uri = URI.create("wss://" + domain + "/ws/ws_cmd");
            
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
        
        private void sendPing() {
            JonSharedCmd.Root ping = JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setPing(JonSharedCmd.Ping.newBuilder().build())
                .build();
            
            if (isConnected.get()) {
                try {
                    wsClient.send(ping.toByteArray());
                } catch (Exception e) {
                    errorCallback.accept("Failed to send ping: " + e.getMessage());
                }
            }
        }
        
        boolean send(JonSharedCmd.Root command) {
            boolean queued = commandQueue.offer(command);
            if (!queued) {
                errorCallback.accept("Command queue full, dropping message");
            }
            
            if (isConnected.get()) {
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
        private WebSocketClient wsClient;
        private final AtomicBoolean isConnected;
        private ScheduledFuture<?> reconnectTask;
        
        StateClient() {
            this.isConnected = new AtomicBoolean(false);
        }
        
        void start() {
            startReconnectTask();
            connect();
        }
        
        void stop() {
            stopReconnectTask();
            if (wsClient != null) {
                wsClient.close();
                wsClient = null;
            }
        }
        
        private void connect() {
            if (!isConnected.get() && isRunning.get()) {
                try {
                    if (wsClient == null) {
                        createWebSocketClient();
                    }
                    wsClient.connect();
                } catch (Exception e) {
                    errorCallback.accept("State connection failed: " + e.getMessage());
                }
            }
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
        
        boolean isConnected() {
            return wsClient != null && wsClient.isOpen();
        }
    }
}