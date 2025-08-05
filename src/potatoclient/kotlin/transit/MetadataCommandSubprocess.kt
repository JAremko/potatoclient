package potatoclient.kotlin.transit

import com.google.protobuf.Message
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import potatoclient.java.transit.MessageType
import java.util.concurrent.atomic.AtomicInteger

/**
 * Command subprocess that uses metadata to determine protobuf types.
 * This replaces action-based routing with type metadata.
 */
class MetadataCommandSubprocess(
    private val wsUrl: String,
    private val transitComm: TransitCommunicator,
) {
    private val messageProtocol = TransitMessageProtocol("metadata-command", transitComm)
    private val wsClient = CommandWebSocketClient(wsUrl, messageProtocol)
    private val protobufHandler = MetadataCommandProcessor()

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
                        val msgType = msg[TransitKeys.MSG_TYPE] as? String
                        when (msgType) {
                            MessageType.COMMAND.key -> handleCommand(msg)
                            MessageType.CONTROL.key -> handleControl(msg)
                            else -> messageProtocol.sendWarn("Unknown message type: $msgType")
                        }
                        totalReceived.incrementAndGet()
                    }
                }
            }
        }

    private suspend fun handleCommand(msg: Map<*, *>) {
        val payload = msg[TransitKeys.PAYLOAD] as? Map<*, *> ?: return
        val msgId = msg[TransitKeys.MSG_ID] as? String ?: ""

        try {
            // Process command using metadata
            val result = protobufHandler.processCommand(payload)

            result.fold(
                onSuccess = { protobuf ->
                    // Send protobuf to server
                    wsClient.send(protobuf.toByteArray())
                    totalSent.incrementAndGet()

                    // Send success response
                    transitComm.sendMessageDirect(
                        messageProtocol.createMessage(
                            MessageType.RESPONSE,
                            mapOf(
                                "status" to "sent",
                                "proto-type" to protobuf.javaClass.name,
                                "size" to protobuf.toByteArray().size,
                            ),
                        ),
                    )
                },
                onFailure = { error ->
                    // Send error response
                    messageProtocol.sendError(
                        "Command processing failed: ${error.message}",
                    )
                    sendError(msgId, error as Exception)
                },
            )
        } catch (e: Exception) {
            sendError(msgId, e)
        }
    }

    private suspend fun handleControl(msg: Map<*, *>) {
        val payload = msg[TransitKeys.PAYLOAD] as? Map<*, *> ?: return
        val action = payload[TransitKeys.ACTION] as? String ?: return

        when (action) {
            "shutdown" -> {
                // Send shutdown confirmation
                transitComm.sendMessage(
                    messageProtocol.createMessage(
                        MessageType.RESPONSE,
                        mapOf("status" to "stopped"),
                    ),
                )
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
                transitComm.sendMessage(
                    messageProtocol.createMessage(
                        MessageType.METRIC,
                        mapOf(
                            "name" to "command-stats",
                            "value" to stats,
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
                    "context" to "metadata-command-error",
                    "error" to (error.message ?: "Unknown error"),
                    "type" to error.javaClass.simpleName,
                ),
            ),
        )
    }
}

/**
 * Process commands using metadata instead of action routing
 */
class MetadataCommandProcessor {
    private val handler = ProtobufTransitHandler.ProtobufReadHandler()

    fun processCommand(payload: Map<*, *>): Result<Message> =
        try {
            // The payload should be a wrapped protobuf command
            val protobuf = handler.fromRep(payload)
            Result.success(protobuf)
        } catch (e: Exception) {
            Result.failure(e)
        }
}

// Main entry point
fun main(args: Array<String>) {
    // Install stdout interceptor
    StdoutInterceptor.installEarly()

    // Initialize logging
    LoggingUtils.initializeLogging("metadata-command-subprocess")

    // Install shutdown hook
    Runtime.getRuntime().addShutdownHook(
        Thread {
            logInfo("Shutdown hook triggered for metadata command subprocess")
        },
    )

    try {
        if (args.isEmpty()) {
            logError("Usage: MetadataCommandSubprocess <websocket-url>")
            System.exit(1)
        }

        val wsUrl = args[0]
        val transitComm =
            TransitCommunicator(
                System.`in`,
                StdoutInterceptor.getOriginalStdout(),
            )
        val messageProtocol = TransitMessageProtocol("metadata-command", transitComm)

        // Set the message protocol for the interceptor
        StdoutInterceptor.setMessageProtocol(messageProtocol)

        runBlocking {
            messageProtocol.sendStatus(TransitKeys.STATUS_STARTING)

            val subprocess = MetadataCommandSubprocess(wsUrl, transitComm)

            try {
                subprocess.run()
            } finally {
                messageProtocol.sendStatus(TransitKeys.STATUS_STOPPED)
            }
        }
    } catch (e: Exception) {
        logError("Fatal error in metadata command subprocess", e)
        System.exit(1)
    } finally {
        LoggingUtils.close()
    }
}
