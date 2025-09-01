package potatoclient.kotlin.ipc

import com.cognitect.transit.Reader
import com.cognitect.transit.TransitFactory
import com.cognitect.transit.Writer
import potatoclient.java.ipc.SocketFactory
import potatoclient.java.ipc.UnixSocketCommunicator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * IPC Server for the main process.
 * Receives messages from video stream clients and can send CLOSE_REQUEST.
 */
class IpcServer(
    private val streamName: String, // e.g., "heat" or "day"
) {
    private lateinit var socketComm: UnixSocketCommunicator
    private val socketPath: Path = generateSocketPath(streamName)
    private val messageQueue = LinkedBlockingQueue<Map<*, *>>()
    private val isRunning = AtomicBoolean(false)

    @Volatile
    private var readerThread: Thread? = null

    @Volatile
    private var processorThread: Thread? = null

    // Message handler callback
    private var onMessage: ((Map<*, *>) -> Unit)? = null

    // Transit handlers for custom types
    private val readHandlers =
        mapOf(
            "kw" to
                com.cognitect.transit.ReadHandler<Any, Any> { rep ->
                    TransitFactory.keyword(rep.toString())
                },
        )

    /**
     * Start the server and begin accepting connections.
     * @param awaitBinding If true, waits for socket to be fully bound before returning
     */
    fun start(awaitBinding: Boolean = true) {
        if (isRunning.getAndSet(true)) {
            throw IllegalStateException("Server already running")
        }

        // Clean up any existing socket file
        Files.deleteIfExists(socketPath)

        // Create server socket
        socketComm = SocketFactory.createServer(socketPath)

        // Start socket - this won't block for servers anymore
        try {
            socketComm.start()
        } catch (e: Exception) {
            System.err.println("[$streamName-server] Failed to start socket: ${e.message}")
            throw IllegalStateException("Failed to start server socket", e)
        }

        if (awaitBinding) {
            // Verify socket file exists
            var retries = 10
            while (!Files.exists(socketPath) && retries > 0) {
                Thread.sleep(10)
                retries--
            }

            if (!Files.exists(socketPath)) {
                throw IllegalStateException("Socket file not created at $socketPath")
            }
        }

        // Start reader thread
        readerThread =
            Thread {
                startReading()
            }.apply {
                name = "$streamName-server-reader"
                isDaemon = true
                start()
            }

        // Start processor thread
        processorThread =
            Thread {
                processMessages()
            }.apply {
                name = "$streamName-server-processor"
                isDaemon = true
                start()
            }
    }

    /**
     * Send a close request to the connected client.
     */
    fun sendCloseRequest() {
        val message =
            mapOf<Any, Any>(
                IpcKeys.MSG_TYPE to IpcKeys.COMMAND,
                IpcKeys.ACTION to IpcKeys.CLOSE_REQUEST,
                IpcKeys.TIMESTAMP to System.currentTimeMillis(),
            )
        sendMessage(message)
    }

    /**
     * Send a raw message to the connected client (for testing).
     */
    fun sendMessage(message: Map<Any, Any>) {
        sendMessageInternal(message)
    }

    /**
     * Set callback for incoming messages.
     */
    fun setOnMessage(callback: (Map<*, *>) -> Unit) {
        onMessage = callback
    }

    /**
     * Get the next message from the queue (blocking).
     */
    fun receiveMessage(
        timeout: Long = 0,
        unit: TimeUnit = TimeUnit.MILLISECONDS,
    ): Map<*, *>? =
        if (isRunning.get()) {
            if (timeout > 0) {
                messageQueue.poll(timeout, unit)
            } else {
                messageQueue.take()
            }
        } else {
            null
        }

    /**
     * Try to get a message without blocking.
     */
    fun tryReceiveMessage(): Map<*, *>? = messageQueue.poll()

    /**
     * Stop the server and clean up resources.
     */
    fun stop() {
        if (!isRunning.getAndSet(false)) {
            return
        }

        // Stop threads
        readerThread?.interrupt()
        processorThread?.interrupt()

        // Stop socket
        if (::socketComm.isInitialized) {
            socketComm.stop()
        }

        // Clean up socket file
        try {
            Files.deleteIfExists(socketPath)
        } catch (_: Exception) {
            // Ignore cleanup errors
        }

        // Clear queue
        messageQueue.clear()
    }

    /**
     * Check if the server is running.
     */
    fun isRunning(): Boolean = isRunning.get() && ::socketComm.isInitialized && socketComm.isRunning

    /**
     * Get the socket path this server is listening on.
     */
    fun getSocketPath(): Path = socketPath

    private fun sendMessageInternal(message: Map<Any, Any>) {
        if (!isRunning.get()) {
            throw IllegalStateException("Server not running")
        }

        try {
            val baos = ByteArrayOutputStream()
            val writer = createWriter(baos)
            writer.write(message)

            val messageBytes = baos.toByteArray()
            socketComm.send(messageBytes)
        } catch (e: Exception) {
            System.err.println("[$streamName-server] Failed to send message: ${e.message}")
        }
    }

    private fun startReading() {
        while (isRunning.get() && !Thread.currentThread().isInterrupted) {
            try {
                val messageBytes = socketComm.receive()
                if (messageBytes != null) {
                    val bais = ByteArrayInputStream(messageBytes)
                    val reader = createReader(bais)
                    val message = reader.read<Any>()

                    if (message is Map<*, *>) {
                        if (!messageQueue.offer(message)) {
                            System.err.println("[$streamName-server] Message queue full, dropping message")
                        }
                    }
                }
            } catch (_: InterruptedException) {
                // Normal shutdown
                Thread.currentThread().interrupt()
                break
            } catch (e: Exception) {
                if (isRunning.get()) {
                    System.err.println("[$streamName-server] Error reading message: ${e.message}")
                    // Brief pause before retry
                    Thread.sleep(100)
                }
            }
        }
    }

    private fun processMessages() {
        while (isRunning.get() && !Thread.currentThread().isInterrupted) {
            try {
                val message = messageQueue.take()
                onMessage?.invoke(message)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            } catch (e: Exception) {
                if (isRunning.get()) {
                    System.err.println("[$streamName-server] Error processing message: ${e.message}")
                }
            }
        }
    }

    private fun createWriter(out: ByteArrayOutputStream): Writer<Any> = TransitFactory.writer(TransitFactory.Format.MSGPACK, out)

    private fun createReader(input: ByteArrayInputStream): Reader =
        TransitFactory.reader(
            TransitFactory.Format.MSGPACK,
            input,
            TransitFactory.readHandlerMap(readHandlers),
        )

    companion object {
        private val servers = ConcurrentHashMap<String, IpcServer>()

        /**
         * Generate a socket path with current process PID and stream name.
         */
        @JvmStatic
        fun generateSocketPath(streamName: String): Path {
            val pid = ProcessHandle.current().pid()
            val socketName = "ipc-$pid-$streamName"
            return SocketFactory.generateSocketPath(socketName)
        }

        /**
         * Create and start a server for the given stream.
         * @param awaitBinding If true, waits for socket to be fully bound before returning
         */
        @JvmStatic
        fun create(
            streamName: String,
            awaitBinding: Boolean = true,
        ): IpcServer {
            if (servers.containsKey(streamName)) {
                throw IllegalStateException("Server already exists for stream: $streamName")
            }

            val server = IpcServer(streamName)
            server.start(awaitBinding)
            servers[streamName] = server
            return server
        }

        /**
         * Get an existing server for the given stream.
         */
        @JvmStatic
        fun get(streamName: String): IpcServer? = servers[streamName]

        /**
         * Stop and remove a server.
         */
        @JvmStatic
        fun remove(streamName: String) {
            servers.remove(streamName)?.stop()
        }

        /**
         * Stop all servers.
         */
        @JvmStatic
        fun stopAll() {
            servers.values.forEach { it.stop() }
            servers.clear()
        }

        /**
         * Get the current process PID.
         */
        @JvmStatic
        fun getCurrentPid(): Long = ProcessHandle.current().pid()
    }
}
