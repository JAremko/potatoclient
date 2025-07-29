package potatoclient.java.websocket;

import potatoclient.kotlin.WebSocketClientBuiltIn;
import cmd.JonSharedCmd;
import com.google.protobuf.InvalidProtocolBufferException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * WebSocket client for sending commands to the server.
 * Features:
 * - Lazy connection (connects only when first command sent)
 * - Automatic reconnection with 5-second delay
 * - Command queuing when disconnected (max 100 messages)
 * - Ping every 300ms when connected
 */
public class CommandWebSocketClient {
    private final String domain;
    private final Consumer<String> errorCallback;
    private final boolean isDevelopment;
    private final WebSocketClientBuiltIn wsClient;
    private final ConcurrentLinkedQueue<JonSharedCmd.Root> commandQueue;
    private final AtomicBoolean isConnected;
    private final AtomicBoolean shouldReconnect;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pingTask;
    
    private static final int MAX_QUEUE_SIZE = 100;
    private static final long PING_INTERVAL_MS = 300;
    private static final long RECONNECT_DELAY_MS = 5000;
    
    public CommandWebSocketClient(String domain, Consumer<String> errorCallback, boolean isDevelopment) {
        this.domain = domain;
        this.errorCallback = errorCallback;
        this.isDevelopment = isDevelopment;
        this.commandQueue = new ConcurrentLinkedQueue<>();
        this.isConnected = new AtomicBoolean(false);
        this.shouldReconnect = new AtomicBoolean(true);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CommandWebSocket-Scheduler");
            t.setDaemon(true);
            return t;
        });
        
        // Create WebSocket client with callbacks
        URI uri = URI.create("wss://" + domain + "/ws/ws_cmd");
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Jon-Client-Type", "local-network");
        
        this.wsClient = new WebSocketClientBuiltIn(
            uri,
            headers,
            null, // No binary message handler for command channel
            this::onConnect,
            this::onClose,
            this::onError
        );
    }
    
    private void onConnect() {
        isConnected.set(true);
        
        // Flush command queue
        while (!commandQueue.isEmpty()) {
            JonSharedCmd.Root cmd = commandQueue.poll();
            if (cmd != null) {
                sendCommandInternal(cmd);
            }
        }
        
        // Start ping task
        startPingTask();
    }
    
    private void onClose(int code, String reason) {
        isConnected.set(false);
        stopPingTask();
        
        if (shouldReconnect.get()) {
            scheduler.schedule(this::connect, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }
    
    private void onError(Throwable error) {
        errorCallback.accept("WebSocket error: " + error.getMessage());
    }
    
    private void connect() {
        if (!isConnected.get() && shouldReconnect.get()) {
            wsClient.connect();
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
    
    private void sendCommandInternal(JonSharedCmd.Root command) {
        try {
            byte[] data = command.toByteArray();
            wsClient.send(data);
        } catch (Exception e) {
            errorCallback.accept("Failed to send command: " + e.getMessage());
        }
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
     * Send a command. Will connect lazily if not connected.
     */
    public void sendCommand(JonSharedCmd.Root command) {
        // Add to queue
        if (commandQueue.size() < MAX_QUEUE_SIZE) {
            commandQueue.offer(command);
        } else {
            // Drop oldest command if queue is full
            commandQueue.poll();
            commandQueue.offer(command);
        }
        
        // Connect if not connected (lazy connection)
        if (!isConnected.get()) {
            connect();
        } else {
            // Send immediately if connected
            JonSharedCmd.Root cmd = commandQueue.poll();
            if (cmd != null) {
                sendCommandInternal(cmd);
            }
        }
    }
    
    /**
     * Start the client (does not connect until first command)
     */
    public void start() {
        // Lazy connection - don't connect yet
    }
    
    /**
     * Stop the client and clean up resources
     */
    public void stop() {
        shouldReconnect.set(false);
        stopPingTask();
        wsClient.close();
        scheduler.shutdown();
    }
    
    /**
     * Check if connected
     */
    public boolean isConnected() {
        return isConnected.get();
    }
}