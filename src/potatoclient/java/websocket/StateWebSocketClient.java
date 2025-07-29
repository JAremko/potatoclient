package potatoclient.java.websocket;

import potatoclient.kotlin.WebSocketClientBuiltIn;
import ser.JonSharedDataTypes;
import com.google.protobuf.InvalidProtocolBufferException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * WebSocket client for receiving state updates from the server.
 * Features:
 * - Eager connection (connects immediately on start)
 * - Automatic reconnection with 1-second delay
 * - No buffering (missed messages are lost)
 */
public class StateWebSocketClient {
    private final String domain;
    private final Consumer<JonSharedDataTypes.JonGUIState> stateCallback;
    private final Consumer<String> errorCallback;
    private final boolean isDevelopment;
    private final WebSocketClientBuiltIn wsClient;
    private final AtomicBoolean shouldReconnect;
    private final ScheduledExecutorService scheduler;
    
    private static final long RECONNECT_DELAY_MS = 1000;
    
    public StateWebSocketClient(String domain, 
                               Consumer<JonSharedDataTypes.JonGUIState> stateCallback,
                               Consumer<String> errorCallback,
                               boolean isDevelopment) {
        this.domain = domain;
        this.stateCallback = stateCallback;
        this.errorCallback = errorCallback;
        this.isDevelopment = isDevelopment;
        this.shouldReconnect = new AtomicBoolean(true);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "StateWebSocket-Scheduler");
            t.setDaemon(true);
            return t;
        });
        
        // Create WebSocket client with callbacks
        URI uri = URI.create("wss://" + domain + "/ws/ws_state");
        Map<String, String> headers = new HashMap<>();
        
        this.wsClient = new WebSocketClientBuiltIn(
            uri,
            headers,
            this::onBinaryMessage,
            null, // onConnect
            this::onClose,
            this::onError
        );
    }
    
    private void onBinaryMessage(ByteBuffer buffer) {
        try {
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            
            // Parse protobuf message
            JonSharedDataTypes.JonGUIState state = JonSharedDataTypes.JonGUIState.parseFrom(data);
            
            // Deliver to callback
            stateCallback.accept(state);
        } catch (InvalidProtocolBufferException e) {
            errorCallback.accept("Failed to parse state message: " + e.getMessage());
        } catch (Exception e) {
            errorCallback.accept("Error processing state message: " + e.getMessage());
        }
    }
    
    private void onClose(int code, String reason) {
        if (shouldReconnect.get()) {
            scheduler.schedule(this::connect, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }
    
    private void onError(Throwable error) {
        errorCallback.accept("WebSocket error: " + error.getMessage());
    }
    
    private void connect() {
        if (shouldReconnect.get()) {
            wsClient.connect();
        }
    }
    
    /**
     * Start the client (connects immediately)
     */
    public void start() {
        connect();
    }
    
    /**
     * Stop the client and clean up resources
     */
    public void stop() {
        shouldReconnect.set(false);
        wsClient.close();
        scheduler.shutdown();
    }
    
    /**
     * Check if connected
     */
    public boolean isConnected() {
        return wsClient.isOpen();
    }
}