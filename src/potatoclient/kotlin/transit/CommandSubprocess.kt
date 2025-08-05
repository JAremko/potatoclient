@file:JvmName("CommandSubprocessKt")

package potatoclient.kotlin.transit

import com.cognitect.transit.TransitFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import potatoclient.java.transit.MessageType
import potatoclient.kotlin.TestModeWebSocketStub
import potatoclient.kotlin.transit.generated.GeneratedCommandHandlers
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
                        // Use Transit keys for access
                        val msgTypeValue = msg[TransitKeys.MSG_TYPE]
                        when {
                            msgTypeValue == MessageType.COMMAND.key || msgTypeValue == MessageType.COMMAND.keyword -> handleCommand(msg)
                            msgTypeValue == MessageType.CONTROL.key || msgTypeValue == MessageType.CONTROL.keyword -> handleControl(msg)
                            else -> messageProtocol.sendWarn("Unknown message type: $msgTypeValue")
                        }
                        totalReceived.incrementAndGet()
                    }
                }
            }
        }

    private suspend fun handleCommand(msg: Map<*, *>) {
        val payload = msg[TransitKeys.PAYLOAD] as? Map<*, *> ?: return
        val msgId = (msg[TransitKeys.MSG_ID] ?: msg["msg-id"]) as? String ?: ""

        try {
            // The new architecture expects command data directly in payload
            // Build command using generated handlers (now handles keywords properly)
            val cmd = GeneratedCommandHandlers.buildCommand(payload)

            wsClient.send(cmd.toByteArray())
            totalSent.incrementAndGet()

            // Send success response using direct write (critical path)
            transitComm.sendMessageDirect(
                messageProtocol.createMessage(
                    MessageType.RESPONSE,
                    mapOf(
                        TransitKeys.STATUS to TransitKeys.STATUS_SENT,
                    ),
                ),
            )
        } catch (e: Exception) {
            // Send error with detailed message
            messageProtocol.sendError("Command build failed: ${e.message}")
            // Also send error response
            sendError(msgId, e)
        }
    }

    private suspend fun handleControl(msg: Map<*, *>) {
        val payload = msg[TransitKeys.PAYLOAD] as? Map<*, *> ?: return
        val action = payload[TransitKeys.ACTION] as? String ?: return
        when (action) {
            "shutdown" -> {
                // Send shutdown confirmation
                runBlocking {
                    transitComm.sendMessage(
                        messageProtocol.createMessage(
                            MessageType.RESPONSE,
                            mapOf(
                                TransitKeys.STATUS to TransitKeys.STATUS_STOPPED,
                            ),
                        ),
                    )
                }
                wsClient.close()
                transitComm.close()
            }
            "get-stats" -> {
                val stats =
                    mapOf(
                        TransitKeys.RECEIVED to totalReceived.get(),
                        TransitKeys.SENT to totalSent.get(),
                        TransitKeys.WS_CONNECTED to wsClient.isConnected(),
                    )
                // Use proper message type for stats
                transitComm.sendMessage(
                    messageProtocol.createMessage(
                        MessageType.METRIC,
                        mapOf(
                            TransitKeys.NAME to TransitFactory.keyword("command-stats"),
                            TransitKeys.VALUE to stats,
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
            messageProtocol.createMessage(
                MessageType.ERROR,
                mapOf(
                    TransitKeys.CONTEXT to TransitFactory.keyword("command-error"),
                    TransitKeys.ERROR to (error.message ?: "Unknown error"),
                ),
            ),
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
        val isTestMode = args.contains("--test-mode")

        if (!isTestMode && args.isEmpty()) {
            logError("Usage: CommandSubprocess <websocket-url> [--test-mode]")
            System.exit(1)
        }

        val transitComm = TransitCommunicator(System.`in`, StdoutInterceptor.getOriginalStdout())
        val messageProtocol = TransitMessageProtocol("command", transitComm)

        // Set the message protocol for the interceptor
        StdoutInterceptor.setMessageProtocol(messageProtocol)

        runBlocking {
            messageProtocol.sendStatus(TransitKeys.STATUS_STARTING)

            if (isTestMode) {
                logInfo("Running in test mode - no WebSocket connection")
                runTestMode(transitComm, messageProtocol)
            } else {
                val wsUrl = args[0]
                val subprocess = CommandSubprocess(wsUrl, transitComm)
                try {
                    subprocess.run()
                } finally {
                    messageProtocol.sendStatus(TransitKeys.STATUS_STOPPED)
                }
            }
        }
    } catch (e: Exception) {
        logError("Fatal error in command subprocess", e)
        System.exit(1)
    } finally {
        LoggingUtils.close()
    }
}

/**
 * Test mode - handles commands without WebSocket connection
 */
suspend fun runTestMode(
    transitComm: TransitCommunicator,
    messageProtocol: TransitMessageProtocol,
) = coroutineScope {
    val testStub = TestModeWebSocketStub(transitComm)

    messageProtocol.sendStatus(TransitKeys.STATUS_TEST_MODE_READY)

    // Handle incoming Transit messages
    while (isActive) {
        try {
            val msg = transitComm.readMessage() ?: break

            val msgTypeValue = msg[TransitKeys.MSG_TYPE]

            // Check if it's a command message
            val isCommand =
                when (msgTypeValue) {
                    is String -> msgTypeValue == MessageType.COMMAND.key
                    is com.cognitect.transit.Keyword -> msgTypeValue == MessageType.COMMAND.keyword
                    else -> false
                }

            if (isCommand) {
                val payload = msg[TransitKeys.PAYLOAD] ?: msg["payload"]

                if (payload is Map<*, *>) {
                    // Build protobuf from Transit command (now handles keywords properly)
                    val proto = GeneratedCommandHandlers.buildCommand(payload)

                    if (proto != null) {
                        // Handle command through test stub
                        testStub.handleCommand(proto)
                    } else {
                        messageProtocol.sendError("Failed to build command from Transit data")
                    }
                } else {
                    messageProtocol.sendError("Invalid command payload")
                }
            } else {
                // Check if it's a control message
                val isControl =
                    when (msgTypeValue) {
                        is String -> msgTypeValue == MessageType.CONTROL.key
                        is com.cognitect.transit.Keyword -> msgTypeValue == MessageType.CONTROL.keyword
                        else -> false
                    }

                if (isControl) {
                    val payload = msg[TransitKeys.PAYLOAD] as? Map<*, *>
                    val action = payload?.get(TransitKeys.ACTION) ?: payload?.get("action")
                    if (action == "shutdown") {
                        messageProtocol.sendStatus(TransitKeys.STATUS_SHUTTING_DOWN)
                        break
                    }
                }
            }
        } catch (e: Exception) {
            messageProtocol.sendException("Error processing message", e)
        }
    }

    messageProtocol.sendStatus(TransitKeys.STATUS_TEST_MODE_STOPPED)
}
