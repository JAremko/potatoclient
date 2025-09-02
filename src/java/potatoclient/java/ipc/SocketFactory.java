package potatoclient.java.ipc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory for creating and managing Unix Domain Socket communicators.
 * Handles socket path generation and lifecycle management.
 */
public class SocketFactory {
    private static final String DEFAULT_SOCKET_DIR = "/tmp/potatoclient-sockets";
    private static final ConcurrentMap<String, UnixSocketCommunicator> activeSockets = new ConcurrentHashMap<>();

    static {
        // Ensure socket directory exists
        try {
            Files.createDirectories(Paths.get(DEFAULT_SOCKET_DIR));
        } catch (IOException e) {
            System.err.println("Failed to create socket directory: " + e.getMessage());
        }

        // Register shutdown hook to clean up sockets
        Runtime.getRuntime().addShutdownHook(new Thread(() -> activeSockets.values().forEach(UnixSocketCommunicator::stop)));
    }

    /**
     * Create a server socket communicator.
     *
     * @param socketName Name for the socket (will be created in default directory)
     * @return The created server communicator
     */
    public static UnixSocketCommunicator createServer(String socketName) {
        Path socketPath = generateSocketPath(socketName);
        return createServer(socketPath);
    }

    /**
     * Create a server socket communicator with a specific path.
     *
     * @param socketPath Full path to the socket file
     * @return The created server communicator
     */
    public static UnixSocketCommunicator createServer(Path socketPath) {
        String key = socketPath.toString();

        // Check if socket already exists
        if (activeSockets.containsKey(key)) {
            throw new IllegalStateException("Socket already exists: " + key);
        }

        UnixSocketCommunicator comm = new UnixSocketCommunicator(socketPath, true);
        activeSockets.put(key, comm);
        return comm;
    }

    /**
     * Create a client socket communicator.
     *
     * @param socketName Name of the socket to connect to
     * @return The created client communicator
     */
    public static UnixSocketCommunicator createClient(String socketName) {
        Path socketPath = generateSocketPath(socketName);
        return createClient(socketPath);
    }

    /**
     * Create a client socket communicator with a specific path.
     *
     * @param socketPath Full path to the socket file
     * @return The created client communicator
     */
    public static UnixSocketCommunicator createClient(Path socketPath) {
        UnixSocketCommunicator comm = new UnixSocketCommunicator(socketPath, false);
        String key = "client-" + socketPath.toString() + "-" + System.nanoTime();
        activeSockets.put(key, comm);
        return comm;
    }

    /**
     * Generate a socket path in the default directory.
     *
     * @param socketName Name for the socket
     * @return Full path to the socket file
     */
    public static Path generateSocketPath(String socketName) {
        return Paths.get(DEFAULT_SOCKET_DIR, socketName + ".sock");
    }

    /**
     * Generate a unique socket path for a video stream.
     *
     * @param streamId Stream identifier (e.g., "heat", "day")
     * @return Full path to the socket file
     */
    public static Path generateStreamSocketPath(String streamId) {
        String socketName = String.format("video-stream-%s-%d", streamId, System.currentTimeMillis());
        return generateSocketPath(socketName);
    }

    /**
     * Close and remove a socket from management.
     *
     * @param comm The communicator to close
     */
    public static void close(UnixSocketCommunicator comm) {
        if (comm != null) {
            comm.stop();

            // Remove from active sockets
            activeSockets.values().removeIf(c -> c == comm);
        }
    }

    /**
     * Close all active sockets.
     */
    public static void closeAll() {
        activeSockets.values().forEach(UnixSocketCommunicator::stop);
        activeSockets.clear();
    }

    /**
     * Get the number of active sockets.
     */
    public static int getActiveSocketCount() {
        return activeSockets.size();
    }

    /**
     * Check if a socket path is available (not in use).
     */
    public static boolean isSocketAvailable(Path socketPath) {
        return !Files.exists(socketPath);
    }
}
