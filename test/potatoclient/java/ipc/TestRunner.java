package potatoclient.java.ipc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple test runner without JUnit dependency.
 * Runs basic tests for UnixSocketCommunicator.
 */
public class TestRunner {
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("Running Unix Domain Socket IPC Tests...\n");
        
        testBasicConnection();
        testBidirectionalMessaging();
        testMultipleMessages();
        testLargeMessage();
        testFactoryCreation();
        
        System.out.println("\n========================================");
        System.out.println("Test Results:");
        System.out.println("  Passed: " + testsPassed);
        System.out.println("  Failed: " + testsFailed);
        System.out.println("========================================");
        
        System.exit(testsFailed > 0 ? 1 : 0);
    }
    
    private static void testBasicConnection() {
        System.out.print("Testing basic connection... ");
        try {
            Path socketPath = Files.createTempDirectory("test-socket").resolve("test.sock");
            
            UnixSocketCommunicator server = new UnixSocketCommunicator(socketPath, true);
            UnixSocketCommunicator client = new UnixSocketCommunicator(socketPath, false);
            
            // Start server in background
            Thread serverThread = new Thread(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();
            
            Thread.sleep(100);
            client.start();
            Thread.sleep(100);
            
            assert server.isRunning() : "Server should be running";
            assert client.isRunning() : "Client should be running";
            
            server.stop();
            client.stop();
            Files.deleteIfExists(socketPath);
            
            passed();
        } catch (Exception e) {
            failed(e);
        }
    }
    
    private static void testBidirectionalMessaging() {
        System.out.print("Testing bidirectional messaging... ");
        try {
            Path socketPath = Files.createTempDirectory("test-socket").resolve("test.sock");
            
            UnixSocketCommunicator server = new UnixSocketCommunicator(socketPath, true);
            UnixSocketCommunicator client = new UnixSocketCommunicator(socketPath, false);
            
            Thread serverThread = new Thread(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();
            
            Thread.sleep(100);
            client.start();
            Thread.sleep(100);
            
            // Client to server
            String msg1 = "Hello from client";
            client.send(msg1.getBytes(StandardCharsets.UTF_8));
            byte[] received1 = server.receive();
            assert msg1.equals(new String(received1, StandardCharsets.UTF_8)) : "Message mismatch";
            
            // Server to client  
            String msg2 = "Hello from server";
            server.send(msg2.getBytes(StandardCharsets.UTF_8));
            byte[] received2 = client.receive();
            assert msg2.equals(new String(received2, StandardCharsets.UTF_8)) : "Message mismatch";
            
            server.stop();
            client.stop();
            Files.deleteIfExists(socketPath);
            
            passed();
        } catch (Exception e) {
            failed(e);
        }
    }
    
    private static void testMultipleMessages() {
        System.out.print("Testing multiple messages... ");
        try {
            Path socketPath = Files.createTempDirectory("test-socket").resolve("test.sock");
            
            UnixSocketCommunicator server = new UnixSocketCommunicator(socketPath, true);
            UnixSocketCommunicator client = new UnixSocketCommunicator(socketPath, false);
            
            Thread serverThread = new Thread(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();
            
            Thread.sleep(100);
            client.start();
            Thread.sleep(100);
            
            // Send multiple messages
            int messageCount = 10;
            for (int i = 0; i < messageCount; i++) {
                String msg = "Message " + i;
                client.send(msg.getBytes(StandardCharsets.UTF_8));
            }
            
            // Receive and verify
            for (int i = 0; i < messageCount; i++) {
                byte[] received = server.receive();
                String expected = "Message " + i;
                assert expected.equals(new String(received, StandardCharsets.UTF_8)) : 
                    "Message mismatch at index " + i;
            }
            
            server.stop();
            client.stop();
            Files.deleteIfExists(socketPath);
            
            passed();
        } catch (Exception e) {
            failed(e);
        }
    }
    
    private static void testLargeMessage() {
        System.out.print("Testing large message (1MB)... ");
        try {
            Path socketPath = Files.createTempDirectory("test-socket").resolve("test.sock");
            
            UnixSocketCommunicator server = new UnixSocketCommunicator(socketPath, true);
            UnixSocketCommunicator client = new UnixSocketCommunicator(socketPath, false);
            
            Thread serverThread = new Thread(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();
            
            Thread.sleep(100);
            client.start();
            Thread.sleep(100);
            
            // Create large message
            byte[] largeMessage = new byte[1024 * 1024]; // 1MB
            for (int i = 0; i < largeMessage.length; i++) {
                largeMessage[i] = (byte)(i % 256);
            }
            
            client.send(largeMessage);
            byte[] received = server.receive();
            
            assert received.length == largeMessage.length : "Size mismatch";
            for (int i = 0; i < largeMessage.length; i++) {
                if (received[i] != largeMessage[i]) {
                    throw new AssertionError("Data mismatch at byte " + i);
                }
            }
            
            server.stop();
            client.stop();
            Files.deleteIfExists(socketPath);
            
            passed();
        } catch (Exception e) {
            failed(e);
        }
    }
    
    private static void testFactoryCreation() {
        System.out.print("Testing SocketFactory... ");
        try {
            String socketName = "factory-test-" + System.nanoTime();
            
            UnixSocketCommunicator server = SocketFactory.createServer(socketName);
            
            Thread serverThread = new Thread(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            serverThread.start();
            
            Thread.sleep(100);
            
            UnixSocketCommunicator client = SocketFactory.createClient(socketName);
            client.start();
            Thread.sleep(100);
            
            // Test communication
            String msg = "Factory test";
            client.send(msg.getBytes(StandardCharsets.UTF_8));
            byte[] received = server.receive();
            assert msg.equals(new String(received, StandardCharsets.UTF_8)) : "Message mismatch";
            
            SocketFactory.close(server);
            SocketFactory.close(client);
            
            passed();
        } catch (Exception e) {
            failed(e);
        }
    }
    
    private static void passed() {
        System.out.println("PASSED");
        testsPassed++;
    }
    
    private static void failed(Exception e) {
        System.out.println("FAILED");
        System.out.println("  Error: " + e.getMessage());
        e.printStackTrace();
        testsFailed++;
    }
}