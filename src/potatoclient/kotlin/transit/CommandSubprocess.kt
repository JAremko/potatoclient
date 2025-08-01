@file:JvmName("CommandSubprocessKt")

package potatoclient.kotlin.transit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import potatoclient.transit.MessageType
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
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Simplified Command subprocess without protovalidate
 */
class CommandSubprocess(
    private val wsUrl: String,
    private val transitComm: TransitCommunicator,
) {
    private val messageProtocol = TransitMessageProtocol("command", transitComm)
    private val wsClient = CommandWebSocketClient(wsUrl, messageProtocol)
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
                        // Use extension properties for cleaner access
                        when (msg.msgType) {
                            MessageType.COMMAND.keyword -> handleCommand(msg)
                            MessageType.RESPONSE.keyword -> handleControl(msg) // Control messages come as response type
                            else -> messageProtocol.sendWarn("Unknown message type: ${msg.msgType}")
                        }
                        totalReceived.incrementAndGet()
                    }
                }
            }
        }

    private suspend fun handleCommand(msg: Map<*, *>) {
        val payload = msg.payload ?: return
        val msgId = msg.msgId ?: ""

        try {
            val action = payload.action ?: "ping"
            val params = payload.params
            
            // Build command with proper error handling
            cmdBuilder.buildCommand(action, params).fold(
                onSuccess = { rootCmd ->
                    wsClient.send(rootCmd.toByteArray())
                    totalSent.incrementAndGet()
                    
                    // Send success response using direct write (critical path)
                    transitComm.sendMessageDirect(
                        messageProtocol.createMessage(
                            MessageType.RESPONSE.key,
                            mapOf(
                                TransitKeys.ACTION.toString() to action,
                                TransitKeys.STATUS.toString() to "sent"
                            )
                        )
                    )
                },
                onFailure = { error ->
                    // Send error with detailed message
                    messageProtocol.sendError("Command build failed for '$action': ${error.message}")
                    // Also send error response
                    sendError(msgId, error as Exception)
                }
            )
        } catch (e: Exception) {
            sendError(msgId, e)
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
                            MessageType.RESPONSE.key,
                            mapOf(
                                TransitKeys.STATUS.toString() to "stopped"
                            )
                        )
                    )
                }
                wsClient.close()
                transitComm.close()
            }
            "get-stats" -> {
                val stats =
                    mapOf(
                        "received" to totalReceived.get(),
                        "sent" to totalSent.get(),
                        "ws-connected" to wsClient.isConnected(),
                    )
                // Use proper message type for stats
                transitComm.sendMessage(
                    messageProtocol.createMessage(
                        MessageType.METRIC.key,
                        mapOf(
                            TransitKeys.NAME.toString() to "command-stats",
                            TransitKeys.VALUE.toString() to stats
                        )
                    )
                )
            }
        }
    }

    private suspend fun sendError(
        msgId: String,
        error: Exception,
    ) {
        transitComm.sendMessage(
            messageProtocol.createMessage(
                MessageType.ERROR.key,
                mapOf(
                    TransitKeys.CONTEXT.toString() to "command-error",
                    TransitKeys.ERROR.toString() to (error.message ?: "Unknown error")
                )
            )
        )
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
 * Simple WebSocket client
 */
class CommandWebSocketClient(
    private val url: String,
    private val messageProtocol: TransitMessageProtocol,
) {
    private var webSocket: WebSocket? = null
    private val connected = AtomicBoolean(false)

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

    suspend fun connect(): Unit =
        coroutineScope {
            try {
                val uri = URI.create(url)
                val origin = "https://${uri.host}"
                val future =
                    httpClient
                        .newWebSocketBuilder()
                        .header("Origin", origin)
                        .buildAsync(
                            uri,
                            object : WebSocket.Listener {
                                override fun onOpen(webSocket: WebSocket) {
                                    connected.set(true)
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
                                    return null
                                }

                                override fun onError(
                                    webSocket: WebSocket,
                                    error: Throwable,
                                ) {
                                    connected.set(false)
                                    messageProtocol.sendException("Connection error", error as Exception)
                                }
                            },
                        ).await()

                webSocket = future
            } catch (e: Exception) {
                messageProtocol.sendException("Failed to connect", e)
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
    // Install stdout interceptor EARLY before any code runs
    StdoutInterceptor.installEarly()

    // Initialize logging for this subprocess
    LoggingUtils.initializeLogging("command-subprocess")

    // Install shutdown hook for clean exit
    Runtime.getRuntime().addShutdownHook(
        Thread {
            logInfo("Shutdown hook triggered for command subprocess")
        },
    )

    try {
        if (args.isEmpty()) {
            logError("Usage: CommandSubprocess <websocket-url>")
            System.exit(1)
        }

        val wsUrl = args[0]
        val transitComm = TransitCommunicator(System.`in`, StdoutInterceptor.getOriginalStdout())
        val messageProtocol = TransitMessageProtocol("command", transitComm)

        // Set the message protocol for the interceptor
        StdoutInterceptor.setMessageProtocol(messageProtocol)

        runBlocking {
            messageProtocol.sendStatus("starting")

            val subprocess = CommandSubprocess(wsUrl, transitComm)

            try {
                subprocess.run()
            } finally {
                messageProtocol.sendStatus("stopped")
            }
        }
    } catch (e: Exception) {
        logError("Fatal error in command subprocess", e)
        System.exit(1)
    } finally {
        LoggingUtils.close()
    }
}
