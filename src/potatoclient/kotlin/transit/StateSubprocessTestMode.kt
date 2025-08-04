@file:JvmName("StateSubprocessTestModeKt")

package potatoclient.kotlin.transit

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import potatoclient.java.transit.MessageType
import ser.JonSharedData
import java.io.DataInputStream

/**
 * Test mode runner for StateSubprocess
 * Reads protobuf from stdin instead of WebSocket
 */
suspend fun runStateTestMode(
    transitComm: TransitCommunicator,
    messageProtocol: TransitMessageProtocol
) = coroutineScope {
    
    messageProtocol.sendStatus("test-mode-ready")
    
    // Create a minimal StateSubprocess-like handler
    val totalReceived = java.util.concurrent.atomic.AtomicInteger(0)
    val totalSent = java.util.concurrent.atomic.AtomicInteger(0)
    
    // Read protobuf from stdin
    launch {
        logInfo("State test mode: reading protobuf from stdin")
        val dataInput = DataInputStream(System.`in`)
        
        while (isActive) {
            try {
                // Read length-prefixed protobuf
                val length = dataInput.readInt()
                if (length > 0 && length < 1024 * 1024) { // Max 1MB
                    val protoBytes = ByteArray(length)
                    dataInput.readFully(protoBytes)
                    
                    totalReceived.incrementAndGet()
                    
                    // Parse and send via Transit
                    try {
                        val protoState = JonSharedData.JonGUIState.parseFrom(protoBytes)
                        
                        // Wrap in map and send
                        val stateMap = mapOf<String, Any>("state" to protoState)
                        transitComm.sendMessageDirect(
                            messageProtocol.createMessage(
                                MessageType.STATE_UPDATE,
                                stateMap,
                            ),
                        )
                        
                        totalSent.incrementAndGet()
                    } catch (e: Exception) {
                        logError("Failed to parse protobuf state", e)
                        messageProtocol.sendException("Parse error", e)
                    }
                }
            } catch (e: Exception) {
                // EOF or error - exit gracefully
                break
            }
        }
    }
    
    // Handle control messages
    launch {
        while (isActive) {
            val msg = transitComm.readMessage()
            if (msg != null) {
                val msgType = msg[TransitKeys.MSG_TYPE] as? String
                if (msgType == MessageType.CONTROL.key) {
                    val payload = msg[TransitKeys.PAYLOAD] as? Map<*, *>
                    val action = payload?.get(TransitKeys.ACTION) as? String
                    
                    when (action) {
                        "shutdown" -> {
                            transitComm.sendMessage(
                                messageProtocol.createMessage(
                                    MessageType.RESPONSE,
                                    mapOf("status" to "stopped"),
                                ),
                            )
                            transitComm.close()
                            break
                        }
                        "get-stats" -> {
                            val stats = mapOf(
                                "received" to totalReceived.get(),
                                "sent" to totalSent.get(),
                                "ws-connected" to false,
                                "test-mode" to true
                            )
                            transitComm.sendMessage(
                                messageProtocol.createMessage(
                                    MessageType.METRIC,
                                    mapOf(
                                        "name" to "state-stats",
                                        "value" to stats,
                                    ),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Main entry point with test mode support
fun main(args: Array<String>) {
    // Install stdout interceptor EARLY before any code runs
    StdoutInterceptor.installEarly()

    // Initialize logging for this subprocess
    LoggingUtils.initializeLogging("state-subprocess-test")

    // Install shutdown hook for clean exit
    Runtime.getRuntime().addShutdownHook(
        Thread {
            logInfo("Shutdown hook triggered for state subprocess test mode")
        },
    )

    try {
        if (!args.contains("--test-mode")) {
            logError("This entry point is only for test mode. Use --test-mode flag.")
            System.exit(1)
        }

        // Create Transit communicator with protobuf write handlers
        val writeHandlers = ProtobufStateHandlers.createWriteHandlers()
        val transitComm =
            TransitCommunicator(
                System.`in`,
                StdoutInterceptor.getOriginalStdout(),
                writeHandlers,
            )
        val messageProtocol = TransitMessageProtocol("state", transitComm)

        // Set the message protocol for the interceptor
        StdoutInterceptor.setMessageProtocol(messageProtocol)

        runBlocking {
            runStateTestMode(transitComm, messageProtocol)
        }
    } catch (e: Exception) {
        logError("Fatal error in state subprocess test mode", e)
        System.exit(1)
    } finally {
        LoggingUtils.close()
    }
}