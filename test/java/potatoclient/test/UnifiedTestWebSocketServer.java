package potatoclient.test;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import cmd.JonSharedCmd;
import ser.JonSharedData;
import com.google.protobuf.InvalidProtocolBufferException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Unified test WebSocket server that handles both command and state endpoints
 * on the same port, matching production behavior.
 */
public class UnifiedTestWebSocketServer extends WebSocketServer {
    private final BlockingQueue<JonSharedCmd.Root> receivedCommands = new LinkedBlockingQueue<>();
    private final List<WebSocket> stateClients = new CopyOnWriteArrayList<>();
    private final Map<WebSocket, String> clientTypes = new ConcurrentHashMap<>();
    
    private final CountDownLatch cmdConnectionLatch = new CountDownLatch(1);
    private final CountDownLatch stateConnectionLatch = new CountDownLatch(1);
    
    public UnifiedTestWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }
    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String path = handshake.getResourceDescriptor();
        System.out.println("Client connected to: " + path + " from: " + conn.getRemoteSocketAddress());
        
        if (path.equals("/ws/ws_cmd")) {
            clientTypes.put(conn, "command");
            cmdConnectionLatch.countDown();
        } else if (path.equals("/ws/ws_state")) {
            clientTypes.put(conn, "state");
            stateClients.add(conn);
            stateConnectionLatch.countDown();
        } else {
            System.err.println("Unknown path: " + path);
            conn.close(1002, "Invalid path");
        }
    }
    
    @Override
    public void onMessage(WebSocket conn, String message) {
        // Neither channel uses text messages
    }
    
    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        String clientType = clientTypes.get(conn);
        if ("command".equals(clientType)) {
            try {
                JonSharedCmd.Root cmd = JonSharedCmd.Root.parseFrom(message.array());
                receivedCommands.offer(cmd);
                System.out.println("Received command: " + getCommandType(cmd));
            } catch (InvalidProtocolBufferException e) {
                System.err.println("Failed to parse command: " + e.getMessage());
            }
        }
        // State channel doesn't receive messages
    }
    
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String clientType = clientTypes.remove(conn);
        System.out.println(clientType + " client disconnected: " + reason);
        if ("state".equals(clientType)) {
            stateClients.remove(conn);
        }
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }
    
    @Override
    public void onStart() {
        System.out.println("Unified WebSocket server started on port " + getPort());
    }
    
    /**
     * Wait for command client connection
     */
    public boolean awaitCmdConnection(long timeout, TimeUnit unit) throws InterruptedException {
        return cmdConnectionLatch.await(timeout, unit);
    }
    
    /**
     * Wait for state client connection
     */
    public boolean awaitStateConnection(long timeout, TimeUnit unit) throws InterruptedException {
        return stateConnectionLatch.await(timeout, unit);
    }
    
    /**
     * Get next received command with timeout
     */
    public JonSharedCmd.Root pollCommand(long timeout, TimeUnit unit) throws InterruptedException {
        return receivedCommands.poll(timeout, unit);
    }
    
    /**
     * Get all received commands
     */
    public List<JonSharedCmd.Root> getAllCommands() {
        List<JonSharedCmd.Root> commands = new CopyOnWriteArrayList<>();
        receivedCommands.drainTo(commands);
        return commands;
    }
    
    /**
     * Send state update to all connected state clients
     */
    public void sendState(JonSharedData.JonGUIState state) {
        byte[] data = state.toByteArray();
        for (WebSocket client : stateClients) {
            if (client.isOpen()) {
                client.send(data);
            }
        }
    }
    
    /**
     * Get number of connected state clients
     */
    public int getStateClientCount() {
        return (int) stateClients.stream().filter(WebSocket::isOpen).count();
    }
    
    /**
     * Clear received commands
     */
    public void clearCommands() {
        receivedCommands.clear();
    }
    
    // Helper to identify command type
    private String getCommandType(JonSharedCmd.Root cmd) {
        if (cmd.hasPing()) return "PING";
        if (cmd.hasFrozen()) return "FROZEN";
        if (cmd.hasNoop()) return "NOOP";
        if (cmd.hasRotary()) return "ROTARY";
        if (cmd.hasDayCamera()) return "DAY_CAMERA";
        if (cmd.hasHeatCamera()) return "HEAT_CAMERA";
        if (cmd.hasLrf()) return "LRF";
        if (cmd.hasGps()) return "GPS";
        if (cmd.hasCompass()) return "COMPASS";
        if (cmd.hasSystem()) return "SYSTEM";
        if (cmd.hasOsd()) return "OSD";
        if (cmd.hasCv()) return "CV";
        return "UNKNOWN";
    }
    
    /**
     * Simple builder for creating test state messages
     */
    public static JonSharedData.JonGUIState createTestState() {
        return JonSharedData.JonGUIState.newBuilder().build();
    }
}