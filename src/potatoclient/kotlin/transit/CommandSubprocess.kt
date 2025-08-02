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
import cmd.JonSharedCmd
import com.cognitect.transit.TransitFactory

/**
 * Simplified Command subprocess without protovalidate
 */
class CommandSubprocess(
    private val wsUrl: String,
    private val transitComm: TransitCommunicator,
) {
    private val messageProtocol = TransitMessageProtocol("command", transitComm)
    private val wsClient = CommandWebSocketClient(wsUrl, messageProtocol)
    private val cmdBuilder = SimpleCommandBuilder()  // Keep for fallback
    
    // Create Transit communicator with protobuf handlers
    private val protobufReadHandlers = ProtobufCommandHandlers.createReadHandlers()
    private val transitWithHandlers = TransitCommunicator(
        System.`in`, 
        StdoutInterceptor.getOriginalStdout(),
        readHandlers = protobufReadHandlers
    )

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
                            MessageType.CONTROL.keyword -> handleControl(msg)
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
            
            // Try to use Transit handlers first
            val rootCmd = tryBuildWithHandlers(action, params)
            
            if (rootCmd != null) {
                // Successfully built with handlers
                wsClient.send(rootCmd.toByteArray())
                totalSent.incrementAndGet()
                
                // Send success response using direct write (critical path)
                transitComm.sendMessageDirect(
                    messageProtocol.createMessage(
                        MessageType.RESPONSE,
                        mapOf(
                            "action" to action,
                            "status" to "sent"
                        )
                    )
                )
            } else {
                // Fallback to SimpleCommandBuilder for compatibility
                cmdBuilder.buildCommand(action, params).fold(
                    onSuccess = { cmd ->
                        wsClient.send(cmd.toByteArray())
                        totalSent.incrementAndGet()
                        
                        // Send success response using direct write (critical path)
                        transitComm.sendMessageDirect(
                            messageProtocol.createMessage(
                                MessageType.RESPONSE,
                                mapOf(
                                    "action" to action,
                                    "status" to "sent"
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
            }
        } catch (e: Exception) {
            sendError(msgId, e)
        }
    }
    
    private fun tryBuildWithHandlers(action: String, params: Map<*, *>?): JonSharedCmd.Root? {
        return try {
            // The command handler in our read handlers expects the command data
            val commandData = mutableMapOf<Any?, Any?>()
            commandData[TransitFactory.keyword("action")] = action
            if (params != null) {
                commandData[TransitFactory.keyword("params")] = params
            }
            
            // Look for the command handler
            val commandHandler = protobufReadHandlers["command"] as? com.cognitect.transit.ReadHandler<Map<*, *>, JonSharedCmd.Root>
            commandHandler?.fromRep(commandData)
        } catch (e: Exception) {
            // If handlers fail, return null to trigger fallback
            messageProtocol.sendDebug("Handler failed for $action: ${e.message}, using fallback")
            null
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
                        MessageType.METRIC,
                        mapOf(
                            "name" to "command-stats",
                            "value" to stats
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
                MessageType.ERROR,
                mapOf(
                    "context" to "command-error",
                    "error" to (error.message ?: "Unknown error")
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
