package potatoclient.kotlin.ipc

import com.cognitect.transit.Keyword
import com.cognitect.transit.Reader
import com.cognitect.transit.TransitFactory
import com.cognitect.transit.Writer
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import potatoclient.java.ipc.SocketFactory
import potatoclient.java.ipc.UnixSocketCommunicator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * IPC Server for the main process.
 * Receives messages from video stream clients and can send CLOSE_REQUEST.
 */
class IpcServer(
    private val streamName: String  // e.g., "heat" or "day"
) {
    private lateinit var socketComm: UnixSocketCommunicator
    private val socketPath: Path = generateSocketPath(streamName)
    private val messageQueue = Channel<Map<*, *>>(Channel.UNLIMITED)
    private val isRunning = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Message handler callback
    private var onMessage: ((Map<*, *>) -> Unit)? = null
    
    // Transit handlers for custom types
    private val readHandlers = mapOf(
        "kw" to com.cognitect.transit.ReadHandler<Any, Any> { rep ->
            TransitFactory.keyword(rep.toString())
        }
    )
    
    /**
     * Start the server and begin accepting connections.
     */
    suspend fun start() {
        if (isRunning.getAndSet(true)) {
            throw IllegalStateException("Server already running")
        }
        
        // Clean up any existing socket file
        Files.deleteIfExists(socketPath)
        
        withContext(Dispatchers.IO) {
            socketComm = SocketFactory.createServer(socketPath)
            socketComm.start()
        }
        
        // Start message reader coroutine
        scope.launch {
            startReading()
        }
        
        // Start message processor coroutine
        scope.launch {
            processMessages()
        }
    }
    
    /**
     * Send a close request to the connected client.
     */
    suspend fun sendCloseRequest() {
        val message = mapOf<Any, Any>(
            IpcKeys.MSG_TYPE to IpcKeys.COMMAND,
            IpcKeys.ACTION to IpcKeys.CLOSE_REQUEST,
            IpcKeys.TIMESTAMP to System.currentTimeMillis()
        )
        sendMessage(message)
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
    suspend fun receiveMessage(): Map<*, *>? =
        if (isRunning.get()) {
            messageQueue.receive()
        } else {
            null
        }
    
    /**
     * Try to get a message without blocking.
     */
    fun tryReceiveMessage(): Map<*, *>? {
        val result = messageQueue.tryReceive()
        return if (result.isSuccess) result.getOrNull() else null
    }
    
    /**
     * Stop the server and clean up resources.
     */
    fun stop() {
        isRunning.set(false)
        scope.cancel()
        
        try {
            messageQueue.close()
        } catch (e: Exception) {
            // Ignore channel close errors
        }
        
        if (::socketComm.isInitialized) {
            socketComm.stop()
        }
        
        // Clean up socket file
        try {
            Files.deleteIfExists(socketPath)
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Check if the server is running.
     */
    fun isRunning(): Boolean = isRunning.get() && ::socketComm.isInitialized && socketComm.isRunning()
    
    /**
     * Get the socket path this server is listening on.
     */
    fun getSocketPath(): Path = socketPath
    
    private suspend fun sendMessage(message: Map<Any, Any>) {
        if (!isRunning.get()) {
            throw IllegalStateException("Server not running")
        }
        
        withContext(Dispatchers.IO) {
            val baos = ByteArrayOutputStream()
            val writer = createWriter(baos)
            writer.write(message)
            
            val messageBytes = baos.toByteArray()
            socketComm.send(messageBytes)
        }
    }
    
    private suspend fun startReading() {
        withContext(Dispatchers.IO) {
            while (isRunning.get() && isActive) {
                try {
                    val messageBytes = socketComm.receive()
                    if (messageBytes != null) {
                        val bais = ByteArrayInputStream(messageBytes)
                        val reader = createReader(bais)
                        val message = reader.read<Any>()
                        
                        if (message is Map<*, *>) {
                            // Non-blocking send to avoid blocking the reader
                            val sent = messageQueue.trySend(message).isSuccess
                            if (!sent) {
                                System.err.println("[$streamName-server] Message queue full, dropping message")
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    // Normal shutdown
                    break
                } catch (e: Exception) {
                    if (isRunning.get()) {
                        System.err.println("[$streamName-server] Error reading message: ${e.message}")
                    }
                    // Don't break - try to recover and continue reading
                }
            }
        }
    }
    
    private suspend fun processMessages() {
        while (isRunning.get() && scope.isActive) {
            try {
                val message = messageQueue.receive()
                onMessage?.invoke(message)
            } catch (e: Exception) {
                if (isRunning.get()) {
                    System.err.println("[$streamName-server] Error processing message: ${e.message}")
                }
            }
        }
    }
    
    private fun createWriter(out: ByteArrayOutputStream): Writer<Any> =
        TransitFactory.writer(TransitFactory.Format.MSGPACK, out)
    
    private fun createReader(input: ByteArrayInputStream): Reader =
        TransitFactory.reader(
            TransitFactory.Format.MSGPACK,
            input,
            TransitFactory.readHandlerMap(readHandlers)
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
         */
        @JvmStatic
        suspend fun create(streamName: String): IpcServer {
            if (servers.containsKey(streamName)) {
                throw IllegalStateException("Server already exists for stream: $streamName")
            }
            
            val server = IpcServer(streamName)
            server.start()
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