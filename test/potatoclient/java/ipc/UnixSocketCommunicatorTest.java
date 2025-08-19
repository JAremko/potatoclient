package potatoclient.java.ipc;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for UnixSocketCommunicator.
 */
public class UnixSocketCommunicatorTest {
    private Path socketPath;
    private UnixSocketCommunicator server;
    private UnixSocketCommunicator client;
    
    @BeforeEach
    void setUp() throws IOException {
        // Create unique socket path for each test
        socketPath = Files.createTempDirectory("test-socket").resolve("test.sock");
    }
    
    @AfterEach
    void tearDown() {
        // Clean up communicators
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.stop();
        }
        
        // Clean up socket file
        try {
            Files.deleteIfExists(socketPath);
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }
    
    @Test
    @DisplayName("Basic connection establishment")
    void testBasicConnection() throws Exception {
        server = new UnixSocketCommunicator(socketPath, true);
        client = new UnixSocketCommunicator(socketPath, false);
        
        // Start server in background
        CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        // Give server time to bind
        Thread.sleep(100);
        
        // Connect client
        client.start();
        
        // Wait for server to accept
        serverFuture.get(1, TimeUnit.SECONDS);
        
        assertTrue(server.isRunning());
        assertTrue(client.isRunning());
    }
    
    @Test
    @DisplayName("Bidirectional message exchange")
    void testBidirectionalMessaging() throws Exception {
        establishConnection();
        
        String message1 = "Hello from client";
        String message2 = "Hello from server";
        
        // Client sends to server
        client.send(message1.getBytes(StandardCharsets.UTF_8));
        
        // Server receives from client
        byte[] received1 = server.receive();
        assertEquals(message1, new String(received1, StandardCharsets.UTF_8));
        
        // Server sends to client
        server.send(message2.getBytes(StandardCharsets.UTF_8));
        
        // Client receives from server
        byte[] received2 = client.receive();
        assertEquals(message2, new String(received2, StandardCharsets.UTF_8));
    }
    
    @Test
    @DisplayName("Large message handling")
    void testLargeMessage() throws Exception {
        establishConnection();
        
        // Create a large message (1MB)
        byte[] largeMessage = new byte[1024 * 1024];
        for (int i = 0; i < largeMessage.length; i++) {
            largeMessage[i] = (byte)(i % 256);
        }
        
        // Send large message
        client.send(largeMessage);
        
        // Receive and verify
        byte[] received = server.receive();
        assertArrayEquals(largeMessage, received);
    }
    
    @Test
    @DisplayName("Multiple messages in sequence")
    void testMultipleMessages() throws Exception {
        establishConnection();
        
        int messageCount = 100;
        
        // Send multiple messages
        for (int i = 0; i < messageCount; i++) {
            String message = "Message " + i;
            client.send(message.getBytes(StandardCharsets.UTF_8));
        }
        
        // Receive and verify all messages
        for (int i = 0; i < messageCount; i++) {
            byte[] received = server.receive();
            String expected = "Message " + i;
            assertEquals(expected, new String(received, StandardCharsets.UTF_8));
        }
    }
    
    @Test
    @DisplayName("Non-blocking receive")
    void testNonBlockingReceive() throws Exception {
        establishConnection();
        
        // Try to receive when no message available
        assertNull(server.tryReceive());
        assertFalse(server.hasMessage());
        
        // Send a message
        String message = "Test message";
        client.send(message.getBytes(StandardCharsets.UTF_8));
        
        // Give time for message to arrive
        Thread.sleep(100);
        
        // Now should have message
        assertTrue(server.hasMessage());
        byte[] received = server.tryReceive();
        assertNotNull(received);
        assertEquals(message, new String(received, StandardCharsets.UTF_8));
        
        // Queue should be empty again
        assertFalse(server.hasMessage());
        assertNull(server.tryReceive());
    }
    
    @Test
    @DisplayName("Concurrent send operations")
    void testConcurrentSends() throws Exception {
        establishConnection();
        
        int threadCount = 10;
        int messagesPerThread = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // Create sender threads
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < messagesPerThread; i++) {
                        String message = String.format("Thread-%d-Message-%d", threadId, i);
                        client.send(message.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for all sends to complete
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        assertEquals(0, errorCount.get());
        
        // Receive all messages
        int totalMessages = threadCount * messagesPerThread;
        for (int i = 0; i < totalMessages; i++) {
            byte[] received = server.receive();
            assertNotNull(received);
            String message = new String(received, StandardCharsets.UTF_8);
            assertTrue(message.matches("Thread-\\d+-Message-\\d+"));
        }
    }
    
    @Test
    @DisplayName("Message size validation")
    void testMessageSizeValidation() throws Exception {
        establishConnection();
        
        // Try to send a message that's too large
        byte[] tooLarge = new byte[11 * 1024 * 1024]; // 11MB, exceeds max
        
        assertThrows(IllegalArgumentException.class, () -> {
            client.send(tooLarge);
        });
    }
    
    @Test
    @DisplayName("Graceful shutdown")
    void testGracefulShutdown() throws Exception {
        establishConnection();
        
        // Send a message
        String message = "Final message";
        client.send(message.getBytes(StandardCharsets.UTF_8));
        
        // Receive it
        byte[] received = server.receive();
        assertEquals(message, new String(received, StandardCharsets.UTF_8));
        
        // Stop client
        client.stop();
        assertFalse(client.isRunning());
        
        // Server should detect disconnection
        Thread.sleep(100);
        
        // Try to receive should eventually return null or throw
        CompletableFuture<byte[]> future = CompletableFuture.supplyAsync(() -> {
            try {
                return server.receive();
            } catch (InterruptedException e) {
                return null;
            }
        });
        
        // Should complete within reasonable time
        assertDoesNotThrow(() -> future.get(2, TimeUnit.SECONDS));
    }
    
    @Test
    @DisplayName("Socket file cleanup")
    void testSocketFileCleanup() throws Exception {
        server = new UnixSocketCommunicator(socketPath, true);
        
        // Start server
        CompletableFuture.runAsync(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        Thread.sleep(100);
        
        // Socket file should exist
        assertTrue(Files.exists(socketPath));
        
        // Stop server
        server.stop();
        
        // Socket file should be cleaned up
        assertFalse(Files.exists(socketPath));
    }
    
    @Test
    @DisplayName("Message framing integrity")
    void testMessageFramingIntegrity() throws Exception {
        establishConnection();
        
        // Send messages of various sizes
        int[] sizes = {0, 1, 100, 1000, 10000, 100000, 1000000};
        
        for (int size : sizes) {
            byte[] message = new byte[size];
            for (int i = 0; i < size; i++) {
                message[i] = (byte)(i % 256);
            }
            
            client.send(message);
            byte[] received = server.receive();
            
            assertEquals(size, received.length);
            assertArrayEquals(message, received);
        }
    }
    
    @Test
    @DisplayName("Empty message handling")
    void testEmptyMessage() throws Exception {
        establishConnection();
        
        // Send empty message
        client.send(new byte[0]);
        
        // Should receive empty message
        byte[] received = server.receive();
        assertNotNull(received);
        assertEquals(0, received.length);
    }
    
    /**
     * Helper method to establish connection between server and client.
     */
    private void establishConnection() throws Exception {
        server = new UnixSocketCommunicator(socketPath, true);
        client = new UnixSocketCommunicator(socketPath, false);
        
        // Start server in background
        CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        // Give server time to bind
        Thread.sleep(100);
        
        // Connect client
        client.start();
        
        // Wait for server to accept
        serverFuture.get(1, TimeUnit.SECONDS);
    }
}