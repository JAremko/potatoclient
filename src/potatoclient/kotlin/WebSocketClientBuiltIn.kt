package potatoclient.kotlin

import java.io.IOException
import java.net.Socket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager
import javax.net.ssl.X509TrustManager

/**
 * WebSocket client implementation using Java 17's built-in HttpClient.
 * Supports ignoring SSL certificate errors for development/testing.
 */
class WebSocketClientBuiltIn(
    private val serverUri: URI,
    private val headers: Map<String, String>,
    private val onBinaryMessage: ((ByteBuffer) -> Unit)?,
    private val onConnect: (() -> Unit)?,
    private val onClose: ((Int, String) -> Unit)?,
    private val onError: ((Throwable) -> Unit)?,
) {
    private val webSocketRef = AtomicReference<WebSocket?>()
    private val isConnecting = AtomicBoolean(false)
    private val shouldReconnect = AtomicBoolean(false)
    private val httpClient: HttpClient = createHttpClient()

    // Buffer pooling for zero-allocation streaming
    private val bufferPool = ByteBufferPool(BUFFER_POOL_SIZE, BUFFER_SIZE, true)

    // Buffer for accumulating partial messages - use direct buffer for better performance
    private var messageBuffer: ByteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
    private val lastBufferTrimTime = AtomicLong(System.currentTimeMillis())

    // Statistics tracking
    private val messagesReceived = AtomicLong(0)
    private val bytesReceived = AtomicLong(0)
    private val statsExecutor =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "WebSocket-Stats").apply { isDaemon = true }
        }

    companion object {
        private const val BUFFER_POOL_SIZE = 20
        private const val BUFFER_SIZE = 2 * 1024 * 1024 // 2MB per buffer
        private const val BUFFER_TRIM_INTERVAL_MS = 60_000L // Trim every minute
    }

    init {
        // Schedule periodic stats logging
        statsExecutor.scheduleWithFixedDelay(::logStats, 30, 30, TimeUnit.SECONDS)
    }

    private fun createHttpClient(): HttpClient =
        try {
            // Create trust-all SSL context
            val sslContext = createTrustAllSSLContext()

            HttpClient
                .newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(10))
                .build()
        } catch (e: NoSuchAlgorithmException) {
            System.err.println("Failed to create HttpClient with custom SSL context, using default: ${e.message}")
            HttpClient.newHttpClient()
        } catch (e: KeyManagementException) {
            System.err.println("Failed to create HttpClient with custom SSL context, using default: ${e.message}")
            HttpClient.newHttpClient()
        }

    private fun createTrustAllSSLContext(): SSLContext {
        val trustAll = TrustAllTrustManager()
        return SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<X509TrustManager>(trustAll), SecureRandom())
        }
    }

    fun connect() {
        if (isConnecting.compareAndSet(false, true)) {
            shouldReconnect.set(true)
            performConnect()
        }
    }

    private fun performConnect() {
        try {
            potatoclient.transit.logInfo("Connecting to WebSocket: $serverUri")

            val builder = httpClient.newWebSocketBuilder()

            // Add custom headers
            headers.forEach { (key, value) -> builder.header(key, value) }

            val future = builder.buildAsync(serverUri, WebSocketListener())

            future.whenComplete { webSocket, throwable ->
                if (throwable != null) {
                    handleConnectionError(throwable)
                } else {
                    webSocketRef.set(webSocket)
                    isConnecting.set(false)
                    onConnect?.invoke()
                }
            }
        } catch (e: IOException) {
            handleConnectionError(e)
        } catch (e: InterruptedException) {
            handleConnectionError(e)
        }
    }

    private fun handleConnectionError(error: Throwable) {
        System.err.println("WebSocket connection error: ${error.message}")
        isConnecting.set(false)
        onError?.invoke(error)

        // Attempt reconnection if enabled
        if (shouldReconnect.get()) {
            scheduleReconnect()
        }
    }

    private fun scheduleReconnect() {
        Thread {
            try {
                Thread.sleep(1000)
                if (shouldReconnect.get() && !isConnecting.get()) {
                    performConnect()
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }.start()
    }

    fun send(data: ByteArray) {
        val ws = webSocketRef.get()
        if (ws != null && !ws.isOutputClosed) {
            ws
                .sendBinary(ByteBuffer.wrap(data), true)
                .exceptionally { throwable ->
                    System.err.println("Failed to send binary data: ${throwable.message}")
                    onError?.invoke(throwable)
                    null
                }
        }
    }

    fun close() {
        shouldReconnect.set(false)
        val ws = webSocketRef.getAndSet(null)
        if (ws != null && !ws.isOutputClosed) {
            ws
                .sendClose(WebSocket.NORMAL_CLOSURE, "Client closing")
                .exceptionally { throwable ->
                    System.err.println("Error during close: ${throwable.message}")
                    null
                }
        }

        // Clean up resources
        statsExecutor.shutdown()
        bufferPool.clear()
    }

    /**
     * Get the buffer pool for external buffer management
     */
    fun getBufferPool(): ByteBufferPool = bufferPool

    /**
     * Check if message buffer needs trimming and trim if necessary
     */
    private fun checkAndTrimMessageBuffer() {
        val now = System.currentTimeMillis()
        val lastTrim = lastBufferTrimTime.get()

        if (now - lastTrim > BUFFER_TRIM_INTERVAL_MS && messageBuffer.capacity() > BUFFER_SIZE * 2) {
            if (lastBufferTrimTime.compareAndSet(lastTrim, now)) {
                // Trim buffer back to default size if it's grown too large
                if (messageBuffer.position() == 0) {
                    messageBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
                    potatoclient.transit.logInfo("Trimmed WebSocket message buffer back to $BUFFER_SIZE bytes")
                }
            }
        }
    }

    /**
     * Log performance statistics
     */
    private fun logStats() {
        val poolStats = bufferPool.getStats()
        val messages = messagesReceived.get()
        val bytes = bytesReceived.get()

        potatoclient.transit.logDebug("WebSocket Stats: messages=$messages, bytes=$bytes, $poolStats")
    }

    fun isOpen(): Boolean {
        val ws = webSocketRef.get()
        return ws != null && !ws.isOutputClosed && !ws.isInputClosed
    }

    private inner class WebSocketListener : WebSocket.Listener {
        // Pre-allocated for hot path
        private val completedFuture = CompletableFuture.completedFuture<Void>(null)

        override fun onOpen(webSocket: WebSocket) {
            potatoclient.transit.logInfo("WebSocket opened")
            webSocket.request(1)
        }

        @Suppress("NOTHING_TO_INLINE")
        private inline fun handleSingleFragment(
            data: ByteBuffer,
            webSocket: WebSocket,
        ): CompletionStage<*> {
            // Fast path for single-fragment messages (most common case)
            messagesReceived.incrementAndGet()
            bytesReceived.addAndGet(data.remaining().toLong())

            // Direct pass-through without any copying
            onBinaryMessage?.invoke(data)

            webSocket.request(1)
            return completedFuture
        }

        override fun onBinary(
            webSocket: WebSocket,
            data: ByteBuffer,
            last: Boolean,
        ): CompletionStage<*> {
            // Fast path for single-fragment messages
            if (last && messageBuffer.position() == 0) {
                return handleSingleFragment(data, webSocket)
            }

            // Slower path for multi-fragment messages
            return try {
                // Track statistics
                bytesReceived.addAndGet(data.remaining().toLong())

                // Only check trim on fragment boundaries to reduce overhead
                if (last) {
                    checkAndTrimMessageBuffer()
                }

                // Handle the binary message
                val dataRemaining = data.remaining()
                if (messageBuffer.remaining() < dataRemaining) {
                    // Expand buffer if needed - use direct buffer
                    val newCapacity = messageBuffer.capacity() + dataRemaining + BUFFER_SIZE
                    val newBuffer = ByteBuffer.allocateDirect(newCapacity)
                    messageBuffer.flip()
                    newBuffer.put(messageBuffer)
                    messageBuffer = newBuffer
                }

                messageBuffer.put(data)

                if (last) {
                    // Complete message received
                    messagesReceived.incrementAndGet()
                    messageBuffer.flip()

                    // Multi-fragment message - use pooled buffer
                    val pooledBuffer = bufferPool.acquireWithCapacity(messageBuffer.remaining())
                    pooledBuffer.put(messageBuffer)
                    pooledBuffer.flip()

                    // Pass pooled buffer and let consumer release it
                    onBinaryMessage?.invoke(pooledBuffer)
                    // Note: Consumer is responsible for releasing the buffer back to pool

                    messageBuffer.clear()
                }

                webSocket.request(1)
                completedFuture
            } catch (e: IOException) {
                System.err.println("Error processing binary message: ${e.message}")
                onError?.invoke(e)
                webSocket.request(1)
                completedFuture
            }
        }

        override fun onText(
            webSocket: WebSocket,
            data: CharSequence,
            last: Boolean,
        ): CompletionStage<*> {
            // We don't expect text messages for video streaming
            potatoclient.transit.logWarn("Unexpected text message received: $data")
            webSocket.request(1)
            return CompletableFuture.completedFuture(null)
        }

        override fun onError(
            webSocket: WebSocket,
            error: Throwable,
        ) {
            System.err.println("WebSocket error: ${error.message}")
            onError?.invoke(error)

            // Trigger reconnection
            webSocketRef.set(null)
            if (shouldReconnect.get()) {
                scheduleReconnect()
            }
        }

        override fun onClose(
            webSocket: WebSocket,
            statusCode: Int,
            reason: String,
        ): CompletionStage<*> {
            potatoclient.transit.logInfo("WebSocket closed: $statusCode - $reason")
            webSocketRef.set(null)

            onClose?.invoke(statusCode, reason)

            // Trigger reconnection if not a normal closure
            if (shouldReconnect.get() && statusCode != WebSocket.NORMAL_CLOSURE) {
                scheduleReconnect()
            }

            return CompletableFuture.completedFuture(null)
        }
    }

    // Trust manager that accepts all certificates (insecure!)
    private class TrustAllTrustManager : X509ExtendedTrustManager() {
        override fun checkClientTrusted(
            chain: Array<X509Certificate>,
            authType: String,
        ) {
            // Trust all certificates for local testing
        }

        override fun checkServerTrusted(
            chain: Array<X509Certificate>,
            authType: String,
        ) {
            // Trust all certificates for local testing
        }

        override fun checkClientTrusted(
            chain: Array<X509Certificate>,
            authType: String,
            socket: Socket,
        ) {
            // Trust all certificates for local testing
        }

        override fun checkServerTrusted(
            chain: Array<X509Certificate>,
            authType: String,
            socket: Socket,
        ) {
            // Trust all certificates for local testing
        }

        override fun checkClientTrusted(
            chain: Array<X509Certificate>,
            authType: String,
            engine: SSLEngine,
        ) {
            // Trust all certificates for local testing
        }

        override fun checkServerTrusted(
            chain: Array<X509Certificate>,
            authType: String,
            engine: SSLEngine,
        ) {
            // Trust all certificates for local testing
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }
}
