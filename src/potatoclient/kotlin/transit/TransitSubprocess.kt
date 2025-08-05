package potatoclient.kotlin.transit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Base class for all Transit-based subprocesses.
 * Provides common functionality for command handling, logging, and lifecycle management.
 */
abstract class TransitSubprocess(
    protected val processType: String,
) {
    // Use the original stdout for Transit communication
    protected val transitComm = TransitCommunicator(System.`in`, StdoutInterceptor.getOriginalStdout())
    protected val messageProtocol = TransitMessageProtocol(processType, transitComm)
    protected val running = AtomicBoolean(true)

    init {
        // Set the message protocol for the interceptor (already installed in main)
        StdoutInterceptor.setMessageProtocol(messageProtocol)
    }

    private val commandScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Track if we're in release mode
    protected val isReleaseBuild =
        System.getProperty("potatoclient.release") != null ||
            System.getenv("POTATOCLIENT_RELEASE") != null

    /**
     * Start the subprocess - main entry point
     */
    fun start() {
        try {
            messageProtocol.sendInfo("Starting $processType subprocess")

            // Initialize the subprocess
            initialize()

            // Start command reader
            commandScope.launch {
                readCommands()
            }

            // Run the main loop
            runMainLoop()
        } catch (e: Exception) {
            messageProtocol.sendException("Fatal error in $processType", e)
        } finally {
            cleanup()
        }
    }

    /**
     * Read and process commands from stdin
     */
    private suspend fun readCommands() {
        while (running.get()) {
            try {
                val message = transitComm.readMessage()
                if (message != null && message[TransitKeys.MSG_TYPE] == "command") {
                    val payload = message[TransitKeys.PAYLOAD] as? Map<*, *>
                    if (payload != null) {
                        handleCommand(payload)
                    }
                }
            } catch (e: Exception) {
                if (running.get()) {
                    messageProtocol.sendException("Command read error", e)
                }
            }
        }
    }

    /**
     * Handle a command - can be overridden by subclasses
     */
    protected open fun handleCommand(payload: Map<*, *>) {
        val action = payload[TransitKeys.ACTION] as? String ?: return

        when (action) {
            "stop", "shutdown" -> {
                messageProtocol.sendInfo("Received shutdown command")
                stop()
            }
            "ping" -> {
                sendResponse("pong", mapOf(TransitKeys.TIMESTAMP to System.currentTimeMillis()))
            }
            "status" -> {
                sendResponse("status", getStatus())
            }
            else -> handleSpecificCommand(action, payload)
        }
    }

    /**
     * Send a response message
     */
    protected fun sendResponse(
        action: String,
        data: Map<Any, Any> = emptyMap(),
    ) {
        val payload =
            mutableMapOf<Any, Any>(
                TransitKeys.ACTION to action,
                TransitKeys.PROCESS to processType,
            ) + data

        runBlocking {
            transitComm.sendMessage(
                mapOf(
                    TransitKeys.MSG_TYPE to "response",
                    TransitKeys.MSG_ID to UUID.randomUUID().toString(),
                    TransitKeys.TIMESTAMP to System.currentTimeMillis(),
                    TransitKeys.PAYLOAD to payload,
                ),
            )
        }
    }

    /**
     * Stop the subprocess gracefully
     */
    protected open fun stop() {
        running.set(false)
        commandScope.cancel()
    }

    /**
     * Cleanup resources
     */
    protected open fun cleanup() {
        try {
            transitComm.close()
        } catch (e: Exception) {
            // Ignore errors on close
        }
    }

    // Abstract methods that subclasses must implement

    /**
     * Initialize the subprocess
     */
    protected abstract fun initialize()

    /**
     * Run the main processing loop
     */
    protected abstract fun runMainLoop()

    /**
     * Handle subprocess-specific commands
     */
    protected abstract fun handleSpecificCommand(
        action: String,
        payload: Map<*, *>,
    )

    /**
     * Get subprocess status information
     */
    protected abstract fun getStatus(): Map<Any, Any>

    // Helper methods for common operations

    protected fun logInfo(message: String) = messageProtocol.sendInfo(message)

    protected fun logError(message: String) = messageProtocol.sendError(message)

    protected fun logWarn(message: String) = messageProtocol.sendWarn(message)

    protected fun logDebug(message: String) = messageProtocol.sendDebug(message)

    protected fun logException(
        context: String,
        ex: Exception,
    ) = messageProtocol.sendException(context, ex)

    companion object {
        // Common exit codes
        const val EXIT_SUCCESS = 0
        const val EXIT_ERROR = 1
        const val EXIT_COMMAND_ERROR = 2
    }
}
