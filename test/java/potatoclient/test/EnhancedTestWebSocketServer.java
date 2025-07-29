package potatoclient.test;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import cmd.JonSharedCmd;
import ser.JonSharedData;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Enhanced test server that can send invalid data for testing error handling
 */
public class EnhancedTestWebSocketServer extends UnifiedTestWebSocketServer {
    
    private final List<String> parseErrors = new CopyOnWriteArrayList<>();
    
    public EnhancedTestWebSocketServer(int port) {
        super(port);
    }
    
    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        String clientType = getClientType(conn);
        if ("command".equals(clientType)) {
            try {
                JonSharedCmd.Root cmd = JonSharedCmd.Root.parseFrom(message.array());
                handleCommand(cmd);
            } catch (Exception e) {
                parseErrors.add("Failed to parse command: " + e.getMessage());
                System.err.println("Parse error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Send invalid binary data to state clients
     */
    public void sendInvalidBinaryData() {
        byte[] garbage = new byte[]{
            (byte)0xFF, (byte)0xFE, (byte)0xFD, (byte)0xFC,
            (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03,
            (byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD
        };
        
        for (WebSocket client : getStateClients()) {
            if (client.isOpen()) {
                client.send(garbage);
            }
        }
    }
    
    /**
     * Send truncated protobuf data
     */
    public void sendTruncatedProtobuf() {
        try {
            // Create a valid state message
            JonSharedData.JonGUIState state = JonSharedData.JonGUIState.newBuilder().build();
            byte[] fullData = state.toByteArray();
            
            // Send only half of it
            if (fullData.length > 2) {
                byte[] truncated = new byte[fullData.length / 2];
                System.arraycopy(fullData, 0, truncated, 0, truncated.length);
                
                for (WebSocket client : getStateClients()) {
                    if (client.isOpen()) {
                        client.send(truncated);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating truncated protobuf: " + e);
        }
    }
    
    /**
     * Send empty data
     */
    public void sendEmptyData() {
        byte[] empty = new byte[0];
        for (WebSocket client : getStateClients()) {
            if (client.isOpen()) {
                client.send(empty);
            }
        }
    }
    
    /**
     * Send very large data
     */
    public void sendLargeData() {
        byte[] large = new byte[1024 * 1024]; // 1MB of zeros
        for (WebSocket client : getStateClients()) {
            if (client.isOpen()) {
                client.send(large);
            }
        }
    }
    
    /**
     * Get parse errors
     */
    public List<String> getParseErrors() {
        return new CopyOnWriteArrayList<>(parseErrors);
    }
    
    /**
     * Clear parse errors
     */
    public void clearParseErrors() {
        parseErrors.clear();
    }
    
    // Helper methods to access parent's private fields via reflection
    private String getClientType(WebSocket conn) {
        try {
            java.lang.reflect.Field field = UnifiedTestWebSocketServer.class.getDeclaredField("clientTypes");
            field.setAccessible(true);
            java.util.Map<WebSocket, String> clientTypes = 
                (java.util.Map<WebSocket, String>) field.get(this);
            return clientTypes.get(conn);
        } catch (Exception e) {
            return null;
        }
    }
    
    private List<WebSocket> getStateClients() {
        try {
            java.lang.reflect.Field field = UnifiedTestWebSocketServer.class.getDeclaredField("stateClients");
            field.setAccessible(true);
            return (List<WebSocket>) field.get(this);
        } catch (Exception e) {
            return new CopyOnWriteArrayList<>();
        }
    }
    
    private void handleCommand(JonSharedCmd.Root cmd) {
        try {
            java.lang.reflect.Field field = UnifiedTestWebSocketServer.class.getDeclaredField("receivedCommands");
            field.setAccessible(true);
            java.util.concurrent.BlockingQueue<JonSharedCmd.Root> queue = 
                (java.util.concurrent.BlockingQueue<JonSharedCmd.Root>) field.get(this);
            queue.offer(cmd);
            
            // Log command type
            System.out.println("Received command: " + getCommandType(cmd));
        } catch (Exception e) {
            System.err.println("Error handling command: " + e);
        }
    }
    
    private String getCommandType(JonSharedCmd.Root cmd) {
        if (cmd.hasPing()) return "PING";
        if (cmd.hasFrozen()) return "FROZEN";
        if (cmd.hasNoop()) return "NOOP";
        if (cmd.hasRotary()) return "ROTARY";
        if (cmd.hasDayCamera()) return "DAY_CAMERA";
        return "UNKNOWN";
    }
}