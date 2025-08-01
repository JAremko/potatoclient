@file:JvmName("StateSubprocessKt")

package potatoclient.kotlin.transit

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import potatoclient.transit.MessageType
import ser.JonSharedData
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Simplified State subprocess without protovalidate
 */
class StateSubprocess(
    private val wsUrl: String,
    private val transitComm: TransitCommunicator,
) {
    private val wsClient = StateWebSocketClient(wsUrl)
    private val stateConverter = SimpleStateConverter()
    private val messageProtocol = TransitMessageProtocol("state", transitComm)

    // Rate limiting
    private val rateLimiterScope = CoroutineScope(Dispatchers.Default)
    private val rateLimiter = AtomicReference(RateLimiter(30, rateLimiterScope)) // 30Hz default

    // Metrics
    private val totalReceived = AtomicInteger(0)
    private val totalSent = AtomicInteger(0)
    
    /**
     * Create a properly formatted state update message
     */
    private fun createStateUpdateMessage(state: Map<String, Any>): Map<String, Any> {
        return messageProtocol.createMessage(
            MessageType.STATE_UPDATE,
            state
        )
    }

    suspend fun run() =
        coroutineScope {
            // Connect WebSocket and process state updates
            launch {
                wsClient.connect { protoBytes ->
                    handleProtobufState(protoBytes)
                }
            }

            // Handle control messages from Clojure
            launch {
                while (isActive) {
                    val msg = transitComm.readMessage()
                    if (msg != null && msg.isMessageType(MessageType.RESPONSE.keyword)) {
                        handleControl(msg)
                    }
                }
            }
        }

    private suspend fun handleProtobufState(protoBytes: ByteArray) {
        totalReceived.incrementAndGet()

        // Rate limit check
        if (!rateLimiter.get().tryAcquire()) return

        try {
            // Parse and convert
            val protoState = JonSharedData.JonGUIState.parseFrom(protoBytes)
            val transitState = stateConverter.convert(protoState)

            // Send to Clojure using direct write (critical path)
            // Use proper message type for state updates
            transitComm.sendMessageDirect(
                createStateUpdateMessage(transitState as Map<String, Any>)
            )

            totalSent.incrementAndGet()
        } catch (e: Exception) {
            // Silently ignore parse errors - they're rare and not critical
        }
    }

    private suspend fun handleControl(msg: Map<*, *>) {
        val payload = msg.payload ?: return

        when (payload.action) {
            "shutdown" -> {
                // Send shutdown confirmation
                runBlocking {
                    transitComm.sendMessage(
                        messageProtocol.createMessage(
                            MessageType.RESPONSE,
                            mapOf(
                                "status" to "stopped"
                            )
                        )
                    )
                }
                wsClient.close()
                transitComm.close()
            }
            "set-rate-limit" -> {
                val rateHz = payload.rateHz?.toInt() ?: 30
                rateLimiter.set(RateLimiter(rateHz, rateLimiterScope))
            }
            "get-stats" -> {
                val stats =
                    mapOf(
                        "received" to totalReceived.get(),
                        "sent" to totalSent.get(),
                        "ws-connected" to wsClient.isConnected(),
                        "rate-limit-hz" to rateLimiter.get().rateHz,
                    )
                transitComm.sendMessage(
                    messageProtocol.createMessage(
                        MessageType.METRIC,
                        mapOf(
                            "name" to "state-stats",
                            "value" to stats
                        )
                    )
                )
            }
        }
    }
}

/**
 * Simple rate limiter using token bucket
 */
class RateLimiter(
    val rateHz: Int,
    private val scope: CoroutineScope,
) {
    private val bucketSize = rateHz
    private val tokens = AtomicInteger(bucketSize)
    private val lastRefill = AtomicLong(System.currentTimeMillis())
    private val refillJob: Job

    init {
        require(rateHz > 0) { "Rate must be positive" }

        refillJob =
            scope.launch {
                while (isActive) {
                    delay(1000L / rateHz)
                    refill()
                }
            }
    }

    fun tryAcquire(): Boolean {
        refill()
        return tokens.getAndDecrement() > 0
    }

    private fun refill() {
        val now = System.currentTimeMillis()
        val elapsed = now - lastRefill.get()
        val tokensToAdd = (elapsed * rateHz / 1000).toInt()

        if (tokensToAdd > 0) {
            lastRefill.set(now)
            tokens.updateAndGet { current ->
                minOf(current + tokensToAdd, bucketSize)
            }
        }
    }

    fun shutdown() {
        refillJob.cancel()
    }
}

/**
 * Trust manager that accepts all certificates (for internal use)
 */
private val trustAllCerts =
    arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

            override fun checkClientTrusted(
                certs: Array<X509Certificate>,
                authType: String,
            ) {}

            override fun checkServerTrusted(
                certs: Array<X509Certificate>,
                authType: String,
            ) {}
        },
    )

/**
 * WebSocket client for receiving protobuf state
 */
class StateWebSocketClient(
    private val url: String,
) {
    private var webSocket: WebSocket? = null
    private val connected = AtomicBoolean(false)
    private val isRunning = AtomicBoolean(true)

    // Create SSL context that trusts all certificates
    private val sslContext =
        SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, java.security.SecureRandom())
        }

    private val httpClient =
        HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .sslContext(sslContext)
            .build()

    suspend fun connect(onMessage: suspend (ByteArray) -> Unit) =
        coroutineScope {
            while (isRunning.get() && isActive) {
                try {
                    val listener = StateWebSocketListener(onMessage, this)
                    val uri = URI.create(url)
                    val origin = "https://${uri.host}"
                    val future =
                        httpClient
                            .newWebSocketBuilder()
                            .header("Origin", origin)
                            .buildAsync(uri, listener)
                            .await()

                    webSocket = future
                    connected.set(true)

                    // Wait for disconnection
                    listener.awaitDisconnection()
                    connected.set(false)
                } catch (e: Exception) {
                    // Only log critical errors to stderr
                    logError("[StateWebSocket] Connection failed", e)
                    delay(5000) // Wait before reconnect
                }
            }
        }

    fun close() {
        isRunning.set(false)
        webSocket?.sendClose(WebSocket.NORMAL_CLOSURE, "Shutdown")
    }

    fun isConnected() = connected.get()
}

class StateWebSocketListener(
    private val onMessage: suspend (ByteArray) -> Unit,
    private val scope: CoroutineScope,
) : WebSocket.Listener {
    private val messageBuffer = ByteArray(1024 * 1024) // 1MB buffer
    private var bufferPos = 0
    private val disconnected = CompletableDeferred<Unit>()

    override fun onOpen(webSocket: WebSocket) {
        webSocket.request(1)
    }

    override fun onBinary(
        webSocket: WebSocket,
        data: ByteBuffer,
        last: Boolean,
    ): CompletionStage<*>? {
        // Copy data to buffer
        val remaining = data.remaining()
        if (bufferPos + remaining > messageBuffer.size) {
            bufferPos = 0
            webSocket.request(1)
            return null
        }

        data.get(messageBuffer, bufferPos, remaining)
        bufferPos += remaining

        // If message is complete, process it
        if (last) {
            val fullMessage = messageBuffer.copyOfRange(0, bufferPos)
            bufferPos = 0

            scope.launch {
                onMessage(fullMessage)
            }
        }

        webSocket.request(1)
        return null
    }

    override fun onClose(
        webSocket: WebSocket,
        statusCode: Int,
        reason: String,
    ): CompletionStage<*>? {
        disconnected.complete(Unit)
        return null
    }

    override fun onError(
        webSocket: WebSocket,
        error: Throwable,
    ) {
        disconnected.complete(Unit)
    }

    suspend fun awaitDisconnection() = disconnected.await()
}

// Extension function
private suspend fun <T> CompletableFuture<T>.await(): T =
    withContext(Dispatchers.IO) {
        this@await.get()
    }

// Main entry point
fun main(args: Array<String>) {
    // Install stdout interceptor EARLY before any code runs
    StdoutInterceptor.installEarly()

    // Initialize logging for this subprocess
    LoggingUtils.initializeLogging("state-subprocess")

    // Install shutdown hook for clean exit
    Runtime.getRuntime().addShutdownHook(
        Thread {
            logInfo("Shutdown hook triggered for state subprocess")
        },
    )

    try {
        if (args.isEmpty()) {
            logError("Usage: StateSubprocess <websocket-url>")
            System.exit(1)
        }

        val wsUrl = args[0]

        val transitComm = TransitCommunicator(System.`in`, StdoutInterceptor.getOriginalStdout())
        val subprocess = StateSubprocess(wsUrl, transitComm)
        val messageProtocol = TransitMessageProtocol("state", transitComm)

        // Set the message protocol for the interceptor
        StdoutInterceptor.setMessageProtocol(messageProtocol)

        runBlocking {
            subprocess.run()
        }
    } catch (e: Exception) {
        logError("Fatal error in state subprocess", e)
        System.exit(1)
    } finally {
        LoggingUtils.close()
    }
}
