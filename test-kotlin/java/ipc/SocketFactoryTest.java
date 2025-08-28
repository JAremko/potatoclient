package potatoclient.java.ipc;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SocketFactory.
 */
public class SocketFactoryTest {
    
    @AfterEach
    void tearDown() {
        // Clean up all sockets after each test
        SocketFactory.closeAll();
    }
    
    @Test
    @DisplayName("Create server and client using factory")
    void testFactoryCreateServerAndClient() throws Exception {
        String socketName = "test-socket-" + System.nanoTime();
        
        // Create server
        UnixSocketCommunicator server = SocketFactory.createServer(socketName);
        assertNotNull(server);
        
        // Start server in background
        CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        Thread.sleep(100);
        
        // Create and connect client
        UnixSocketCommunicator client = SocketFactory.createClient(socketName);
        client.start();
        
        // Wait for connection
        serverFuture.get(1, TimeUnit.SECONDS);
        
        // Test communication
        String message = "Factory test message";
        client.send(message.getBytes(StandardCharsets.UTF_8));
        
        byte[] received = server.receive();
        assertEquals(message, new String(received, StandardCharsets.UTF_8));
        
        // Clean up
        SocketFactory.close(server);
        SocketFactory.close(client);
    }
    
    @Test
    @DisplayName("Generate socket paths")
    void testGenerateSocketPaths() {
        Path path1 = SocketFactory.generateSocketPath("test");
        assertTrue(path1.toString().contains("test.sock"));
        assertTrue(path1.toString().contains("/tmp/potatoclient-sockets"));
        
        Path path2 = SocketFactory.generateStreamSocketPath("heat");
        assertTrue(path2.toString().contains("video-stream-heat"));
        assertTrue(path2.toString().contains(".sock"));
    }
    
    @Test
    @DisplayName("Prevent duplicate server sockets")
    void testPreventDuplicateServers() throws Exception {
        String socketName = "duplicate-test-" + System.nanoTime();
        
        // Create first server
        UnixSocketCommunicator server1 = SocketFactory.createServer(socketName);
        assertNotNull(server1);
        
        // Try to create duplicate server
        assertThrows(IllegalStateException.class, () -> {
            SocketFactory.createServer(socketName);
        });
        
        // Clean up
        SocketFactory.close(server1);
    }
    
    @Test
    @DisplayName("Multiple clients can connect to same server")
    void testMultipleClients() throws Exception {
        String socketName = "multi-client-" + System.nanoTime();
        Path socketPath = SocketFactory.generateSocketPath(socketName);
        
        // Create multiple clients (they should each get unique tracking)
        UnixSocketCommunicator client1 = SocketFactory.createClient(socketPath);
        UnixSocketCommunicator client2 = SocketFactory.createClient(socketPath);
        
        assertNotNull(client1);
        assertNotNull(client2);
        assertNotSame(client1, client2);
        
        // Clean up
        SocketFactory.close(client1);
        SocketFactory.close(client2);
    }
    
    @Test
    @DisplayName("Active socket tracking")
    void testActiveSocketTracking() throws Exception {
        int initialCount = SocketFactory.getActiveSocketCount();
        
        String socketName1 = "tracking-test-1-" + System.nanoTime();
        String socketName2 = "tracking-test-2-" + System.nanoTime();
        
        UnixSocketCommunicator server1 = SocketFactory.createServer(socketName1);
        assertEquals(initialCount + 1, SocketFactory.getActiveSocketCount());
        
        UnixSocketCommunicator server2 = SocketFactory.createServer(socketName2);
        assertEquals(initialCount + 2, SocketFactory.getActiveSocketCount());
        
        SocketFactory.close(server1);
        assertEquals(initialCount + 1, SocketFactory.getActiveSocketCount());
        
        SocketFactory.closeAll();
        assertEquals(0, SocketFactory.getActiveSocketCount());
    }
    
    @Test
    @DisplayName("Check socket availability")
    void testSocketAvailability() throws Exception {
        Path socketPath = Files.createTempDirectory("test").resolve("availability.sock");
        
        // Should be available initially
        assertTrue(SocketFactory.isSocketAvailable(socketPath));
        
        // Create a file at that path
        Files.createFile(socketPath);
        
        // Should no longer be available
        assertFalse(SocketFactory.isSocketAvailable(socketPath));
        
        // Clean up
        Files.deleteIfExists(socketPath);
    }
    
    @Test
    @DisplayName("Stream socket path generation is unique")
    void testStreamSocketPathUniqueness() throws Exception {
        Path path1 = SocketFactory.generateStreamSocketPath("heat");
        Thread.sleep(2); // Ensure different timestamp
        Path path2 = SocketFactory.generateStreamSocketPath("heat");
        
        assertNotEquals(path1, path2);
        
        // Both should contain stream identifier
        assertTrue(path1.toString().contains("video-stream-heat"));
        assertTrue(path2.toString().contains("video-stream-heat"));
    }
}