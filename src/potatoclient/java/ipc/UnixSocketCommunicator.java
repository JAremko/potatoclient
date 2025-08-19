package potatoclient.java.ipc;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bidirectional Unix Domain Socket communicator using Java NIO (Java 16+).
 * Provides framed message communication with length-prefixed packets.
 */
public class UnixSocketCommunicator {
    private static final int MAX_MESSAGE_SIZE = 10 * 1024 * 1024; // 10MB max
    private static final int HEADER_SIZE = 4; // 4 bytes for message length
    
    private final Path socketPath;
    private final boolean isServer;
    private SocketChannel channel;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BlockingQueue<byte[]> incomingQueue = new LinkedBlockingQueue<>();
    private final ReentrantLock writeLock = new ReentrantLock();
    
    private Thread readerThread;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    
    /**
     * Create a Unix Domain Socket communicator.
     * 
     * @param socketPath Path to the Unix domain socket file
     * @param isServer If true, acts as server (binds); if false, acts as client (connects)
     */
    public UnixSocketCommunicator(Path socketPath, boolean isServer) {
        this.socketPath = socketPath;
        this.isServer = isServer;
        this.readBuffer = ByteBuffer.allocateDirect(MAX_MESSAGE_SIZE + HEADER_SIZE);
        this.writeBuffer = ByteBuffer.allocateDirect(MAX_MESSAGE_SIZE + HEADER_SIZE);
        this.readBuffer.order(ByteOrder.BIG_ENDIAN);
        this.writeBuffer.order(ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Start the communicator. For servers, this binds and accepts a connection.
     * For clients, this connects to the server.
     */
    public void start() throws IOException {
        if (running.getAndSet(true)) {
            throw new IllegalStateException("Communicator already running");
        }
        
        var address = UnixDomainSocketAddress.of(socketPath);
        
        if (isServer) {
            // Clean up any existing socket file
            Files.deleteIfExists(socketPath);
            
            // Create server channel and accept connection
            var serverChannel = java.nio.channels.ServerSocketChannel.open(StandardProtocolFamily.UNIX);
            serverChannel.bind(address);
            
            // Accept single connection (blocking)
            channel = serverChannel.accept();
            serverChannel.close(); // Close server channel after accepting
        } else {
            // Connect as client
            channel = SocketChannel.open(StandardProtocolFamily.UNIX);
            channel.connect(address);
        }
        
        // Configure channel
        channel.configureBlocking(true);
        
        // Start reader thread
        readerThread = new Thread(this::readerLoop, "UnixSocket-Reader-" + socketPath.getFileName());
        readerThread.setDaemon(true);
        readerThread.start();
    }
    
    /**
     * Send a message through the socket.
     * 
     * @param data The message bytes to send
     * @throws IOException if sending fails
     */
    public void send(byte[] data) throws IOException {
        if (!running.get()) {
            throw new IllegalStateException("Communicator not running");
        }
        
        if (data.length > MAX_MESSAGE_SIZE) {
            throw new IllegalArgumentException("Message too large: " + data.length + " bytes");
        }
        
        writeLock.lock();
        try {
            writeBuffer.clear();
            
            // Write length prefix
            writeBuffer.putInt(data.length);
            
            // Write message data
            writeBuffer.put(data);
            
            // Flip buffer for writing
            writeBuffer.flip();
            
            // Write entire buffer to channel
            while (writeBuffer.hasRemaining()) {
                channel.write(writeBuffer);
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Receive a message from the socket (blocking).
     * 
     * @return The received message bytes, or null if the communicator is stopped
     * @throws InterruptedException if interrupted while waiting
     */
    public byte[] receive() throws InterruptedException {
        return incomingQueue.take();
    }
    
    /**
     * Try to receive a message from the socket (non-blocking).
     * 
     * @return The received message bytes, or null if no message is available
     */
    public byte[] tryReceive() {
        return incomingQueue.poll();
    }
    
    /**
     * Check if there are messages available to receive.
     */
    public boolean hasMessage() {
        return !incomingQueue.isEmpty();
    }
    
    /**
     * Reader loop that continuously reads framed messages from the socket.
     */
    private void readerLoop() {
        try {
            while (running.get() && channel.isOpen()) {
                readBuffer.clear();
                readBuffer.limit(HEADER_SIZE);
                
                // Read length header
                while (readBuffer.hasRemaining()) {
                    int bytesRead = channel.read(readBuffer);
                    if (bytesRead == -1) {
                        // EOF reached
                        running.set(false);
                        return;
                    }
                }
                
                readBuffer.flip();
                int messageLength = readBuffer.getInt();
                
                // Validate message length
                if (messageLength <= 0 || messageLength > MAX_MESSAGE_SIZE) {
                    System.err.println("Invalid message length: " + messageLength);
                    continue;
                }
                
                // Read message body
                readBuffer.clear();
                readBuffer.limit(messageLength);
                
                while (readBuffer.hasRemaining()) {
                    int bytesRead = channel.read(readBuffer);
                    if (bytesRead == -1) {
                        // EOF reached
                        running.set(false);
                        return;
                    }
                }
                
                // Extract message bytes
                readBuffer.flip();
                byte[] message = new byte[messageLength];
                readBuffer.get(message);
                
                // Queue the message
                if (!incomingQueue.offer(message)) {
                    System.err.println("Incoming queue full, dropping message");
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Reader thread error: " + e.getMessage());
            }
        } finally {
            running.set(false);
        }
    }
    
    /**
     * Stop the communicator and close all resources.
     */
    public void stop() {
        if (!running.getAndSet(false)) {
            return; // Already stopped
        }
        
        // Close the channel
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                // Ignore close errors
            }
        }
        
        // Interrupt reader thread if it's blocked
        if (readerThread != null) {
            readerThread.interrupt();
            try {
                readerThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Clean up socket file if we're the server
        if (isServer) {
            try {
                Files.deleteIfExists(socketPath);
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
        
        // Clear the queue
        incomingQueue.clear();
    }
    
    /**
     * Check if the communicator is running.
     */
    public boolean isRunning() {
        return running.get() && channel != null && channel.isOpen();
    }
    
    /**
     * Get the socket path.
     */
    public Path getSocketPath() {
        return socketPath;
    }
}