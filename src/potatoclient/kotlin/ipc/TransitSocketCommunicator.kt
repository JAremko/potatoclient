package potatoclient.kotlin.ipc

import com.cognitect.transit.Reader
import com.cognitect.transit.TransitFactory
import com.cognitect.transit.Writer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import potatoclient.java.ipc.SocketFactory
import potatoclient.java.ipc.UnixSocketCommunicator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Transit-based communication layer over Unix Domain Sockets.
 * Provides high-level Transit message exchange using the Java IPC module.
 */
class TransitSocketCommunicator private constructor(
    private val socketComm: UnixSocketCommunicator,
    private val streamId: String
) {
    private val messageQueue = Channel<Map<*, *>>(Channel.UNLIMITED)
    private val isRunning = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Transit handlers for custom types
    private val writeHandlers: Map<Class<*>, com.cognitect.transit.WriteHandler<*, *>> = mapOf()

    private val readHandlers = mapOf(
        "kw" to com.cognitect.transit.ReadHandler<Any, Any> { rep ->
            TransitFactory.keyword(rep.toString())
        },
        "enum" to com.cognitect.transit.ReadHandler<Any, Any> { rep ->
            TransitFactory.keyword(rep.toString())
        }
    )

    /**
     * Start the communicator and begin processing messages.
     */
    suspend fun start() {
        if (isRunning.getAndSet(true)) {
            throw IllegalStateException("Communicator already running")
        }

        withContext(Dispatchers.IO) {
            socketComm.start()
        }

        // Start reader coroutine
        scope.launch {
            startReading()
        }
    }

    /**
     * Send a Transit message through the socket.
     */
    suspend fun sendMessage(message: Map<Any, Any>) {
        withContext(Dispatchers.IO) {
            val baos = ByteArrayOutputStream()
            val writer = createWriter(baos)
            writer.write(message)

            val messageBytes = baos.toByteArray()
            socketComm.send(messageBytes)
        }
    }

    /**
     * Send a message directly without coroutine overhead (for critical paths).
     */
    fun sendMessageDirect(message: Map<Any, Any>) {
        val baos = ByteArrayOutputStream()
        val writer = createWriter(baos)
        writer.write(message)

        val messageBytes = baos.toByteArray()
        socketComm.send(messageBytes)
    }

    /**
     * Read a message from the queue.
     */
    suspend fun readMessage(): Map<*, *>? =
        if (isRunning.get()) {
            messageQueue.receive()
        } else {
            null
        }

    /**
     * Check if a message is available.
     */
    fun hasMessage(): Boolean = !messageQueue.tryReceive().isFailure

    /**
     * Start reading messages from the socket.
     */
    private suspend fun startReading() {
        withContext(Dispatchers.IO) {
            while (isRunning.get() && isActive && socketComm.isRunning()) {
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
                                System.err.println("[$streamId] Message queue full, dropping message")
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    // Normal shutdown
                    break
                } catch (e: Exception) {
                    if (isRunning.get()) {
                        System.err.println("[$streamId] Error reading Transit message: ${e.message}")
                    }
                    break
                }
            }
        }
    }

    /**
     * Create a standard message envelope.
     */
    fun createMessage(
        msgType: com.cognitect.transit.Keyword,
        payload: Map<Any, Any>
    ): Map<Any, Any> = mapOf(
        IpcKeys.MSG_TYPE to msgType,
        IpcKeys.MSG_ID to UUID.randomUUID().toString(),
        IpcKeys.TIMESTAMP to System.currentTimeMillis(),
        IpcKeys.PAYLOAD to payload
    )

    /**
     * Create a standard event message.
     */
    fun createEventMessage(
        eventType: com.cognitect.transit.Keyword,
        eventData: Map<Any, Any>
    ): Map<Any, Any> = createMessage(
        IpcKeys.EVENT,
        eventData + mapOf(IpcKeys.TYPE to eventType)
    )

    /**
     * Create a standard log message.
     */
    fun createLogMessage(
        level: com.cognitect.transit.Keyword,
        message: String,
        data: Map<Any, Any>? = null
    ): Map<Any, Any> = createMessage(
        IpcKeys.LOG,
        buildMap {
            put(IpcKeys.LEVEL, level)
            put(IpcKeys.MESSAGE, message)
            put(IpcKeys.PROCESS, streamId)
            if (data != null) {
                put(IpcKeys.DATA, data)
            }
        }
    )

    /**
     * Stop the communicator and clean up resources.
     */
    fun stop() {
        isRunning.set(false)
        scope.cancel(null as kotlinx.coroutines.CancellationException?)

        try {
            messageQueue.close()
        } catch (e: Exception) {
            // Ignore channel close errors
        }

        socketComm.stop()
    }

    /**
     * Check if the communicator is running.
     */
    fun isRunning(): Boolean = isRunning.get() && socketComm.isRunning()

    private fun createWriter(out: ByteArrayOutputStream): Writer<Any> =
        if (writeHandlers.isNotEmpty()) {
            TransitFactory.writer(
                TransitFactory.Format.MSGPACK,
                out,
                TransitFactory.writeHandlerMap(writeHandlers)
            )
        } else {
            TransitFactory.writer(
                TransitFactory.Format.MSGPACK,
                out,
                null as Map<Class<*>, com.cognitect.transit.WriteHandler<*, *>>?
            )
        }

    private fun createReader(input: ByteArrayInputStream): Reader =
        TransitFactory.reader(
            TransitFactory.Format.MSGPACK,
            input,
            TransitFactory.readHandlerMap(readHandlers)
        )

    companion object {
        @Volatile
        private var instance: TransitSocketCommunicator? = null

        /**
         * Create or get the singleton instance for Kotlin video stream process.
         * Uses a dynamically generated socket path.
         */
        @JvmStatic
        fun getInstance(streamId: String): TransitSocketCommunicator {
            return instance ?: synchronized(this) {
                instance ?: run {
                    // Generate socket path for this stream
                    val socketPath = SocketFactory.generateStreamSocketPath(streamId)

                    // Create client socket (Clojure side will be server)
                    val socketComm = SocketFactory.createClient(socketPath)

                    TransitSocketCommunicator(socketComm, streamId).also {
                        instance = it
                    }
                }
            }
        }

        /**
         * Create a communicator with a specific socket path.
         * Useful for testing or custom configurations.
         */
        @JvmStatic
        fun createWithPath(socketPath: Path, streamId: String, isServer: Boolean = false): TransitSocketCommunicator {
            val socketComm = if (isServer) {
                SocketFactory.createServer(socketPath)
            } else {
                SocketFactory.createClient(socketPath)
            }
            return TransitSocketCommunicator(socketComm, streamId)
        }

        /**
         * Reset the singleton instance (mainly for testing).
         */
        @JvmStatic
        @JvmName("resetInstance")
        fun reset() {
            instance?.stop()
            instance = null
        }
    }
}
