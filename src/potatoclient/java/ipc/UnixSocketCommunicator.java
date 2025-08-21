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
    private java.nio.channels.ServerSocketChannel serverChannel;  // Keep server channel for async accept
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final BlockingQueue<byte[]> incomingQueue = new LinkedBlockingQueue<>();
    private final ReentrantLock writeLock = new ReentrantLock();

    private Thread readerThread;
    private Thread acceptThread;  // Thread for accepting connections
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;

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
     * Start the communicator. For servers, this binds and starts accepting connections.
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

            // Create server channel
            serverChannel = java.nio.channels.ServerSocketChannel.open(StandardProtocolFamily.UNIX);
            serverChannel.bind(address);

            // Accept connections in a separate thread to avoid blocking
            acceptThread = new Thread(() -> {
                try {
                    channel = serverChannel.accept();
                    channel.configureBlocking(true);
                    connected.set(true);

                    // Start reader thread once connected
                    readerThread = new Thread(this::readerLoop, "UnixSocket-Reader-" + socketPath.getFileName());
                    readerThread.setDaemon(true);
                    readerThread.start();
                } catch (IOException e) {
                    if (running.get()) {
                        System.err.println("Accept error: " + e.getMessage());
                    }
                }
            }, "UnixSocket-Accept-" + socketPath.getFileName());
            acceptThread.setDaemon(true);
            acceptThread.start();
        } else {
            // Connect as client
            channel = SocketChannel.open(StandardProtocolFamily.UNIX);
            channel.connect(address);
            channel.configureBlocking(true);
            connected.set(true);

            // Start reader thread
            readerThread = new Thread(this::readerLoop, "UnixSocket-Reader-" + socketPath.getFileName());
            readerThread.setDaemon(true);
            readerThread.start();
        }
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

        if (!connected.get()) {
            throw new IllegalStateException("Not connected yet");
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
     * Reads exactly the specified number of bytes from the channel into the buffer.
     * The buffer should be cleared and have its limit set before calling this method.
     * 
     * @return true if EOF was reached, false if read successfully
     * @throws IOException if an I/O error occurs
     */
    private boolean readReachedEOF() throws IOException {
        while (readBuffer.hasRemaining()) {
            int bytesRead = channel.read(readBuffer);
            if (bytesRead == -1) {
                // EOF reached
                running.set(false);
                return true;
            }
        }
        return false;
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
                if (readReachedEOF()) {
                    return;
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

                if (readReachedEOF()) {
                    return;
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

        connected.set(false);

        // Close the channel
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                // Ignore close errors
            }
        }

        // Close server channel if we're a server
        if (serverChannel != null) {
            try {
                serverChannel.close();
            } catch (IOException e) {
                // Ignore close errors
            }
        }

        // Interrupt accept thread if it exists
        if (acceptThread != null) {
            acceptThread.interrupt();
            try {
                acceptThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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
        return running.get();
    }

    /**
     * Check if the communicator is connected.
     */
    public boolean isConnected() {
        return connected.get() && channel != null && channel.isOpen();
    }

}
