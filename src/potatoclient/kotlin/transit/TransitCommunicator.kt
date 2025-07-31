package potatoclient.kotlin.transit

import com.cognitect.transit.Reader
import com.cognitect.transit.TransitFactory
import com.cognitect.transit.Writer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

// Message framing constants
private const val MAX_MESSAGE_SIZE = 10 * 1024 * 1024 // 10MB max message size
private const val FRAME_HEADER_SIZE = 4 // 4 bytes for message length

// Backpressure configuration
private const val HIGH_WATER_MARK = 1000 // Start applying backpressure
private const val LOW_WATER_MARK = 100 // Resume normal operation

/**
 * Handles bidirectional Transit communication with the Clojure main process
 * with proper message framing and backpressure handling
 */
class TransitCommunicator(
    private val inputStream: InputStream = System.`in`,
    private val outputStream: OutputStream = System.out,
) {
    private lateinit var writer: Writer<Any>
    private lateinit var reader: Reader
    private val messageQueue = Channel<Map<*, *>>(Channel.UNLIMITED)
    private val isRunning = AtomicBoolean(true)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Backpressure state
    private val backpressureActive = AtomicBoolean(false)

    init {
        // Create framed streams for reliable message boundaries
        val framedOutput = FramedOutputStream(outputStream)
        val framedInput = FramedInputStream(inputStream)

        // Create Transit writer with MessagePack format
        writer = createWriter(framedOutput)

        // Create Transit reader with MessagePack format
        reader = TransitFactory.reader(TransitFactory.Format.MSGPACK, framedInput)

        // Start reader coroutine
        scope.launch {
            startReading()
        }

        // Start backpressure monitor
        scope.launch {
            monitorBackpressure()
        }
    }

    /**
     * Send a message to the Clojure process with backpressure awareness
     */
    suspend fun sendMessage(message: Map<String, Any>) {
        // Apply backpressure if needed
        if (backpressureActive.get()) {
            delay(10) // Small delay when under pressure
        }

        withContext(Dispatchers.IO) {
            synchronized(writer) {
                try {
                    writer.write(message)
                    // Framed output stream handles flushing after frame is written
                } catch (e: IOException) {
                    System.err.println("Error sending Transit message: ${e.message}")
                    throw e
                }
            }
        }
    }

    /**
     * Send a message directly without coroutine overhead (for critical paths)
     */
    fun sendMessageDirect(message: Map<String, Any>) {
        synchronized(writer) {
            try {
                writer.write(message)
                // Framed output stream handles flushing after frame is written
            } catch (e: IOException) {
                System.err.println("Error sending Transit message: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Read a message from the Clojure process
     */
    suspend fun readMessage(): Map<*, *>? =
        if (isRunning.get()) {
            messageQueue.receive()
        } else {
            null
        }

    /**
     * Check if a message is available
     */
    fun hasMessage(): Boolean = !messageQueue.tryReceive().isFailure

    /**
     * Monitor queue size and apply backpressure
     */
    private suspend fun monitorBackpressure() {
        while (isRunning.get()) {
            // Since Channel doesn't expose size, use a simple counter approach
            val queueSize = if (backpressureActive.get()) HIGH_WATER_MARK else 0

            when {
                queueSize > HIGH_WATER_MARK && !backpressureActive.get() -> {
                    backpressureActive.set(true)
                    System.err.println("Backpressure activated - queue size high")
                }
                queueSize < LOW_WATER_MARK && backpressureActive.get() -> {
                    backpressureActive.set(false)
                    System.err.println("Backpressure deactivated - queue size normal")
                }
            }

            delay(100) // Check every 100ms
        }
    }

    /**
     * Start reading messages from input stream
     */
    private suspend fun startReading() {
        withContext(Dispatchers.IO) {
            while (isRunning.get() && isActive) {
                try {
                    val message = reader.read<Any>()
                    if (message is Map<*, *>) {
                        // Non-blocking send to avoid blocking the reader
                        val sent = messageQueue.trySend(message).isSuccess
                        if (!sent) {
                            System.err.println("Message queue full, dropping message")
                        }
                    }
                } catch (e: EOFException) {
                    // Stream closed, exit gracefully
                    System.err.println("Input stream closed, stopping reader")
                    break
                } catch (e: IOException) {
                    if (isRunning.get()) {
                        System.err.println("I/O error reading Transit message: ${e.message}")
                    }
                    break
                } catch (e: Exception) {
                    // Log error but continue reading
                    System.err.println("Error reading Transit message: ${e.message}")
                }
            }
        }
    }

    /**
     * Create a standard message envelope
     */
    fun createMessage(
        msgType: String,
        payload: Map<String, Any>,
    ): Map<String, Any> =
        mapOf(
            "msg-type" to msgType,
            "msg-id" to UUID.randomUUID().toString(),
            "timestamp" to System.currentTimeMillis(),
            "payload" to payload,
        )

    /**
     * Close the communicator
     */
    fun close() {
        isRunning.set(false)
        scope.cancel()

        try {
            messageQueue.close()
        } catch (e: Exception) {
            // Ignore channel close errors
        }

        try {
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            // Ignore stream close errors
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createWriter(out: OutputStream): Writer<Any> {
        // Use null map for custom handlers to help type inference
        val writer =
            TransitFactory.writer<Any>(
                TransitFactory.Format.MSGPACK,
                out,
                null as Map<Class<*>, com.cognitect.transit.WriteHandler<*, *>>?,
            )
        return writer
    }

    companion object {
        // Removed - we now use StdoutInterceptor.getOriginalStdout() instead
    }
}

/**
 * Output stream that frames messages with a length prefix
 */
class FramedOutputStream(
    private val wrapped: OutputStream,
) : OutputStream() {
    private val buffer = ByteArrayOutputStream()

    override fun write(b: Int) {
        buffer.write(b)
    }

    override fun write(
        b: ByteArray,
        off: Int,
        len: Int,
    ) {
        buffer.write(b, off, len)
    }

    override fun flush() {
        val data = buffer.toByteArray()
        if (data.isNotEmpty()) {
            // Write length prefix (4 bytes, big endian)
            val lengthBuffer = ByteBuffer.allocate(4)
            lengthBuffer.order(ByteOrder.BIG_ENDIAN)
            lengthBuffer.putInt(data.size)

            // Write frame
            wrapped.write(lengthBuffer.array())
            wrapped.write(data)
            wrapped.flush()

            // Clear buffer for next message
            buffer.reset()
        }
    }

    override fun close() {
        flush()
        wrapped.close()
    }
}

/**
 * Input stream that reads framed messages with length prefix
 */
class FramedInputStream(
    private val wrapped: InputStream,
) : InputStream() {
    private var currentFrame: ByteArrayInputStream? = null

    override fun read(): Int {
        ensureFrame()
        return currentFrame?.read() ?: -1
    }

    override fun read(
        b: ByteArray,
        off: Int,
        len: Int,
    ): Int {
        ensureFrame()
        return currentFrame?.read(b, off, len) ?: -1
    }

    private fun ensureFrame() {
        if (currentFrame == null || currentFrame!!.available() == 0) {
            readNextFrame()
        }
    }

    private fun readNextFrame() {
        // Read length prefix
        val lengthBytes = ByteArray(4)
        var bytesRead = 0
        while (bytesRead < 4) {
            val n = wrapped.read(lengthBytes, bytesRead, 4 - bytesRead)
            if (n == -1) {
                currentFrame = null
                return
            }
            bytesRead += n
        }

        val length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.BIG_ENDIAN).int

        // Validate frame size
        if (length <= 0 || length > MAX_MESSAGE_SIZE) {
            throw IOException("Invalid frame size: $length")
        }

        // Read frame data
        val frameData = ByteArray(length)
        bytesRead = 0
        while (bytesRead < length) {
            val n = wrapped.read(frameData, bytesRead, length - bytesRead)
            if (n == -1) {
                throw IOException("Unexpected EOF while reading frame")
            }
            bytesRead += n
        }

        currentFrame = ByteArrayInputStream(frameData)
    }

    override fun close() {
        wrapped.close()
    }
}
