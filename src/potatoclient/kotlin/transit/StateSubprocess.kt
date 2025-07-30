package potatoclient.transit

import kotlinx.coroutines.*
import ser.JonSharedData
import ser.JonSharedDataTypes
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Simplified State subprocess without protovalidate
 */
class StateSubprocess(
    private val wsUrl: String,
    private val transitComm: TransitCommunicator,
) {
    private val wsClient = StateWebSocketClient(wsUrl)
    private val stateConverter = SimpleStateConverter()

    // Debouncing
    private val lastSentProto = AtomicReference<JonSharedData.JonGUIState?>(null)
    private val lastSentHash = AtomicInteger(0)

    // Rate limiting
    private val rateLimiter = AtomicReference(RateLimiter(30)) // 30Hz default

    // Metrics
    private val totalReceived = AtomicInteger(0)
    private val totalSent = AtomicInteger(0)
    private val duplicatesSkipped = AtomicInteger(0)

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
                    if (msg != null && msg["msg-type"] == "control") {
                        handleControl(msg)
                    }
                }
            }
        }

    private suspend fun handleProtobufState(protoBytes: ByteArray) {
        totalReceived.incrementAndGet()

        try {
            val protoState = JonSharedData.JonGUIState.parseFrom(protoBytes)

            // Debouncing check
            if (!shouldSendUpdate(protoState)) {
                duplicatesSkipped.incrementAndGet()
                return
            }

            // Convert to Transit
            val transitState = stateConverter.convert(protoState)

            // Send to Clojure
            transitComm.sendMessage(
                transitComm.createMessage("state", transitState as Map<String, Any>),
            )

            totalSent.incrementAndGet()
            lastSentProto.set(protoState)
            lastSentHash.set(protoState.hashCode())
        } catch (e: Exception) {
            println("Error processing state: ${e.message}")
            transitComm.sendMessage(
                transitComm.createMessage(
                    "error",
                    mapOf(
                        "source" to "state-parsing",
                        "error" to (e.message ?: "Unknown error"),
                    ) as Map<String, Any>,
                ),
            )
        }
    }

    private fun shouldSendUpdate(newProto: JonSharedData.JonGUIState): Boolean {
        val lastProto = lastSentProto.get()

        // Always send first update
        if (lastProto == null) return true

        // Skip identical updates (using equals)
        if (newProto == lastProto) return false

        // Hash collision check
        if (newProto.hashCode() == lastSentHash.get()) {
            if (newProto.equals(lastProto)) return false
        }

        // Rate limiting
        return rateLimiter.get().tryAcquire()
    }

    private suspend fun handleControl(msg: Map<*, *>) {
        val payload = msg["payload"] as? Map<*, *> ?: return

        when (payload["action"]) {
            "shutdown" -> {
                wsClient.close()
                transitComm.close()
            }
            "set-rate-limit" -> {
                val rateHz = (payload["rate-hz"] as? Number)?.toInt() ?: 30
                rateLimiter.set(RateLimiter(rateHz))
                println("Rate limit set to $rateHz Hz")
            }
            "get-stats" -> {
                transitComm.sendMessage(
                    transitComm.createMessage(
                        "stats",
                        mapOf(
                            "received" to totalReceived.get(),
                            "sent" to totalSent.get(),
                            "duplicates-skipped" to duplicatesSkipped.get(),
                            "ws-connected" to wsClient.isConnected(),
                            "rate-limit-hz" to rateLimiter.get().rateHz,
                        ),
                    ),
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
) {
    private val bucketSize = rateHz
    private val tokens = AtomicInteger(bucketSize)
    private val lastRefill = AtomicLong(System.currentTimeMillis())
    private val refillJob: Job

    init {
        require(rateHz > 0) { "Rate must be positive" }

        refillJob =
            GlobalScope.launch {
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
 * WebSocket client for receiving protobuf state
 */
class StateWebSocketClient(
    private val url: String,
) {
    private var webSocket: WebSocket? = null
    private val connected = AtomicBoolean(false)
    private val isRunning = AtomicBoolean(true)
    private val httpClient =
        HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()

    suspend fun connect(onMessage: suspend (ByteArray) -> Unit) =
        coroutineScope {
            while (isRunning.get() && isActive) {
                try {
                    println("Connecting to WebSocket: $url")

                    val listener = StateWebSocketListener(onMessage)
                    val future =
                        httpClient
                            .newWebSocketBuilder()
                            .buildAsync(URI.create(url), listener)
                            .await()

                    webSocket = future
                    connected.set(true)

                    // Wait for disconnection
                    listener.awaitDisconnection()
                    connected.set(false)
                } catch (e: Exception) {
                    println("WebSocket error: ${e.message}")
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
) : WebSocket.Listener {
    private val messageBuffer = ByteArray(1024 * 1024) // 1MB buffer
    private var bufferPos = 0
    private val disconnected = CompletableDeferred<Unit>()

    override fun onOpen(webSocket: WebSocket) {
        println("WebSocket connected")
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
            println("Message too large, resetting buffer")
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

            GlobalScope.launch {
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
        println("WebSocket closed: $statusCode $reason")
        disconnected.complete(Unit)
        return null
    }

    override fun onError(
        webSocket: WebSocket,
        error: Throwable,
    ) {
        println("WebSocket error: ${error.message}")
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
    if (args.isEmpty()) {
        println("Usage: StateSubprocess <websocket-url>")
        System.exit(1)
    }

    val wsUrl = args[0]
    val transitComm = TransitCommunicator(System.`in`, System.out)
    val subprocess = StateSubprocess(wsUrl, transitComm)

    runBlocking {
        subprocess.run()
    }
}
