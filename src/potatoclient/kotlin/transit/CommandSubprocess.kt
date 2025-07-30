package potatoclient.transit

import cmd.JonSharedCmd
import kotlinx.coroutines.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Simplified Command subprocess without protovalidate
 */
class CommandSubprocess(
    private val wsUrl: String,
    private val transitComm: TransitCommunicator,
) {
    private val wsClient = CommandWebSocketClient(wsUrl)
    private val cmdBuilder = SimpleCommandBuilder()

    // Metrics
    private val totalReceived = AtomicInteger(0)
    private val totalSent = AtomicInteger(0)

    suspend fun run() =
        coroutineScope {
            // Start WebSocket connection
            launch {
                wsClient.connect()
            }

            // Handle messages from Clojure
            launch {
                while (isActive) {
                    val msg = transitComm.readMessage()
                    if (msg != null) {
                        when (msg["msg-type"]) {
                            "command" -> handleCommand(msg)
                            "control" -> handleControl(msg)
                            else -> println("Unknown message type: ${msg["msg-type"]}")
                        }
                        totalReceived.incrementAndGet()
                    }
                }
            }
        }

    private suspend fun handleCommand(msg: Map<*, *>) {
        val payload = msg["payload"] as? Map<*, *> ?: return
        val msgId = msg["msg-id"] as? String ?: ""

        try {
            val action = payload["action"] as? String ?: "ping"
            val rootCmd = cmdBuilder.buildCommand(action)

            wsClient.send(rootCmd.toByteArray())
            totalSent.incrementAndGet()

            // Send success response
            transitComm.sendMessage(
                mapOf(
                    "msg-type" to "response",
                    "msg-id" to msgId,
                    "timestamp" to System.currentTimeMillis(),
                    "payload" to mapOf("status" to "sent"),
                ),
            )
        } catch (e: Exception) {
            sendError(msgId, e)
        }
    }

    private suspend fun handleControl(msg: Map<*, *>) {
        val payload = msg["payload"] as? Map<*, *> ?: return
        when (payload["action"]) {
            "shutdown" -> {
                wsClient.close()
                transitComm.close()
            }
            "get-stats" -> {
                transitComm.sendMessage(
                    transitComm.createMessage(
                        "stats",
                        mapOf(
                            "received" to totalReceived.get(),
                            "sent" to totalSent.get(),
                            "ws-connected" to wsClient.isConnected(),
                        ),
                    ),
                )
            }
        }
    }

    private suspend fun sendError(
        msgId: String,
        error: Exception,
    ) {
        transitComm.sendMessage(
            mapOf(
                "msg-type" to "response",
                "msg-id" to msgId,
                "timestamp" to System.currentTimeMillis(),
                "payload" to
                    mapOf(
                        "status" to "error",
                        "error" to error.message,
                    ),
            ),
        )
    }
}

/**
 * Simple WebSocket client
 */
class CommandWebSocketClient(
    private val url: String,
) {
    private var webSocket: WebSocket? = null
    private val connected = AtomicBoolean(false)
    private val httpClient =
        HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()

    suspend fun connect() =
        coroutineScope {
            try {
                val future =
                    httpClient
                        .newWebSocketBuilder()
                        .buildAsync(
                            URI.create(url),
                            object : WebSocket.Listener {
                                override fun onOpen(webSocket: WebSocket) {
                                    connected.set(true)
                                    println("WebSocket connected to $url")
                                    webSocket.request(1)
                                }

                                override fun onBinary(
                                    webSocket: WebSocket,
                                    data: ByteBuffer,
                                    last: Boolean,
                                ): CompletionStage<*>? {
                                    webSocket.request(1)
                                    return null
                                }

                                override fun onClose(
                                    webSocket: WebSocket,
                                    statusCode: Int,
                                    reason: String,
                                ): CompletionStage<*>? {
                                    connected.set(false)
                                    println("WebSocket closed: $statusCode $reason")
                                    return null
                                }

                                override fun onError(
                                    webSocket: WebSocket,
                                    error: Throwable,
                                ) {
                                    connected.set(false)
                                    error.printStackTrace()
                                }
                            },
                        ).await()

                webSocket = future
            } catch (e: Exception) {
                println("Failed to connect: ${e.message}")
                throw e
            }
        }

    fun send(data: ByteArray) {
        webSocket?.sendBinary(ByteBuffer.wrap(data), true)
    }

    fun close() {
        webSocket?.sendClose(WebSocket.NORMAL_CLOSURE, "Shutdown")
    }

    fun isConnected() = connected.get()
}

// Extension function for CompletableFuture
private suspend fun <T> java.util.concurrent.CompletableFuture<T>.await(): T =
    withContext(Dispatchers.IO) {
        this@await.get()
    }

// Main entry point
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: CommandSubprocess <websocket-url>")
        System.exit(1)
    }

    val wsUrl = args[0]
    val transitComm = TransitCommunicator(System.`in`, System.out)
    val subprocess = CommandSubprocess(wsUrl, transitComm)

    runBlocking {
        subprocess.run()
    }
}