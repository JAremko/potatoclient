package potatoclient.test;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import cmd.JonSharedCmd;
import ser.JonSharedData;
import ser.JonSharedDataSystem;
import ser.JonSharedDataTime;
import ser.JonSharedDataRotary;
import com.google.protobuf.InvalidProtocolBufferException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 * Test WebSocket server for integration testing.
 * Supports both command and state channels.
 */
public class TestWebSocketServer {
    private final int cmdPort;
    private final int statePort;
    private CommandServer cmdServer;
    private StateServer stateServer;
    
    // For tracking received commands
    private final BlockingQueue<JonSharedCmd.Root> receivedCommands = new LinkedBlockingQueue<>();
    
    // For tracking connected state clients
    private final List<WebSocket> stateClients = new CopyOnWriteArrayList<>();
    
    // Latches for synchronization
    private final CountDownLatch cmdConnectionLatch = new CountDownLatch(1);
    private final CountDownLatch stateConnectionLatch = new CountDownLatch(1);
    
    public TestWebSocketServer(int cmdPort, int statePort) {
        this.cmdPort = cmdPort;
        this.statePort = statePort;
    }
    
    public void start() throws Exception {
        cmdServer = new CommandServer(new InetSocketAddress(cmdPort));
        stateServer = new StateServer(new InetSocketAddress(statePort));
        
        cmdServer.start();
        stateServer.start();
        
        // Give servers time to start
        Thread.sleep(100);
    }
    
    public void stop() throws InterruptedException {
        if (cmdServer != null) {
            cmdServer.stop(1000);
        }
        if (stateServer != null) {
            stateServer.stop(1000);
        }
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
        List<JonSharedCmd.Root> commands = new ArrayList<>();
        receivedCommands.drainTo(commands);
        return commands;
    }
    
    /**
     * Send state update to all connected clients
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
    
    // Command server implementation
    private class CommandServer extends WebSocketServer {
        CommandServer(InetSocketAddress address) {
            super(address);
        }
        
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            System.out.println("Command client connected from: " + conn.getRemoteSocketAddress());
            cmdConnectionLatch.countDown();
        }
        
        @Override
        public void onMessage(WebSocket conn, String message) {
            // Command channel uses binary protocol only
        }
        
        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
            try {
                JonSharedCmd.Root cmd = JonSharedCmd.Root.parseFrom(message.array());
                receivedCommands.offer(cmd);
                System.out.println("Received command: " + getCommandType(cmd));
            } catch (InvalidProtocolBufferException e) {
                System.err.println("Failed to parse command: " + e.getMessage());
            }
        }
        
        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("Command client disconnected: " + reason);
        }
        
        @Override
        public void onError(WebSocket conn, Exception ex) {
            System.err.println("Command server error: " + ex.getMessage());
        }
        
        @Override
        public void onStart() {
            System.out.println("Command server started on port " + getPort());
        }
    }
    
    // State server implementation
    private class StateServer extends WebSocketServer {
        StateServer(InetSocketAddress address) {
            super(address);
        }
        
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            System.out.println("State client connected from: " + conn.getRemoteSocketAddress());
            stateClients.add(conn);
            stateConnectionLatch.countDown();
        }
        
        @Override
        public void onMessage(WebSocket conn, String message) {
            // State channel doesn't receive messages
        }
        
        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
            // State channel doesn't receive messages
        }
        
        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("State client disconnected: " + reason);
            stateClients.remove(conn);
        }
        
        @Override
        public void onError(WebSocket conn, Exception ex) {
            System.err.println("State server error: " + ex.getMessage());
        }
        
        @Override
        public void onStart() {
            System.out.println("State server started on port " + getPort());
        }
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
        // Create minimal state for testing
        return JonSharedData.JonGUIState.newBuilder().build();
    }
    
    /**
     * Create test state with timestamp
     */
    public static JonSharedData.JonGUIState createTestStateWithTimestamp() {
        // For now, just return a basic state
        return JonSharedData.JonGUIState.newBuilder().build();
    }
    
    /**
     * Create test state with rotary data
     */
    public static JonSharedData.JonGUIState createTestStateWithRotary(double azimuth, double elevation) {
        // For now, just return a basic state
        // The actual fields will depend on the protobuf definition
        return JonSharedData.JonGUIState.newBuilder().build();
    }
}