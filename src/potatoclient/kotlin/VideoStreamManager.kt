package potatoclient.kotlin

import kotlinx.coroutines.runBlocking
import potatoclient.kotlin.transit.TransitCommunicator
import potatoclient.kotlin.transit.TransitMessageProtocol
import potatoclient.transit.EventType
import potatoclient.transit.MessageKeys
import java.awt.Component
import java.net.URI
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JFrame
import kotlin.system.exitProcess

class VideoStreamCreationException(
    message: String,
    cause: Throwable,
) : Exception(message, cause)

class VideoStreamManager(
    private val streamId: String,
    private val streamUrl: String,
    domain: String,
) : MouseEventHandler.EventCallback,
    WindowEventHandler.EventCallback,
    GStreamerPipeline.EventCallback,
    FrameManager.FrameEventListener,
    potatoclient.kotlin.gestures.FrameDataProvider {
    // Core components - use original stdout for Transit
    private val transitReader =
        TransitCommunicator(
            System.`in`,
            potatoclient.kotlin.transit.StdoutInterceptor
                .getOriginalStdout(),
        )

    // Thread-safe primitives
    private val running = AtomicBoolean(true)
    private val shutdownLatch = CountDownLatch(1)
    private val closeEventSent = AtomicBoolean(false)

    // Frame timing tracking
    private val currentFrameTimestamp = AtomicLong(0)
    private val currentFrameDuration = AtomicLong(0)

    // Executor services
    private val reconnectExecutor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "VideoStream-Reconnect-$streamId").apply { isDaemon = true }
        }
    private val eventThrottleExecutor: ScheduledExecutorService =
        Executors.newScheduledThreadPool(
            Constants.EVENT_THROTTLE_POOL_SIZE,
        ) { r ->
            Thread(r, "VideoStream-EventThrottle-$streamId").apply { isDaemon = true }
        }

    // Module instances
    private val eventFilter = EventFilter()
    private val messageProtocol = TransitMessageProtocol(streamId, transitReader)

    init {
        // Set the message protocol for the interceptor (already installed in main)
        potatoclient.kotlin.transit.StdoutInterceptor
            .setMessageProtocol(messageProtocol)
    }

    private val frameManager = FrameManager(streamId, domain, this, messageProtocol)
    private var mouseEventHandler: MouseEventHandler? = null
    private var windowEventHandler: WindowEventHandler? = null
    private val webSocketClient: WebSocketClientBuiltIn
    private val gstreamerPipeline = GStreamerPipeline(this)

    init {
        webSocketClient = createWebSocketClient()
    }

    private fun createWebSocketClient(): WebSocketClientBuiltIn =
        try {
            val uri = URI(streamUrl)
            val headers =
                mutableMapOf(
                    "Origin" to "https://${uri.host}",
                    "User-Agent" to Constants.WS_USER_AGENT,
                    "Cache-Control" to Constants.WS_CACHE_CONTROL,
                    "Pragma" to Constants.WS_PRAGMA,
                )

            WebSocketClientBuiltIn(
                serverUri = uri,
                headers = headers,
                onBinaryMessage = { data ->
                    // Fast path - atomic check only
                    if (running.get()) {
                        // Check if we have enough data for timestamp (8 bytes) and duration (8 bytes)
                        if (data.remaining() >= 16) {
                            // Save original position
                            val originalPos = data.position()

                            // Set order and read directly from buffer (no duplicate)
                            data.order(ByteOrder.LITTLE_ENDIAN)
                            val timestamp = data.getLong()
                            val duration = data.getLong()

                            // Store current frame timing
                            currentFrameTimestamp.set(timestamp)
                            currentFrameDuration.set(duration)

                            // Create a slice with just the video data (no copy)
                            val videoData = data.slice()

                            // Restore position for buffer pool
                            data.position(originalPos)

                            // Push only the video data to pipeline
                            gstreamerPipeline.pushVideoData(videoData)
                        } else {
                            // Skip malformed frames silently
                        }

                        // Always release buffers back to pool - WebSocket only gives us pooled buffers
                        webSocketClient.getBufferPool().release(data)
                    }
                },
                onConnect = {
                    // Connection established
                },
                onClose = { code, reason ->
                    // Connection closed
                },
                onError = { error ->
                    messageProtocol.sendException("WebSocket error", error as Exception)
                },
            )
        } catch (e: Exception) {
            throw VideoStreamCreationException("Failed to create WebSocket client", e)
        }

    fun start() {
        try {
            messageProtocol.sendResponse("starting", mapOf(MessageKeys.STREAM_ID to streamId))

            // Create and show frame
            frameManager.createFrame()
            frameManager.showFrame()

            // Start command reader thread
            Thread({
                try {
                    readCommands()
                } catch (e: Exception) {
                    messageProtocol.sendException("Command reader error", e)
                }
            }, "VideoStream-CommandReader-$streamId").apply {
                isDaemon = true
                start()
            }

            // Connect WebSocket
            webSocketClient.connect()

            // Wait for shutdown
            try {
                shutdownLatch.await()
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        } catch (e: Exception) {
            messageProtocol.sendException("Startup error", e)
        } finally {
            cleanup()
        }
    }

    private fun readCommands() {
        while (running.get()) {
            try {
                val message =
                    runBlocking {
                        transitReader.readMessage()
                    }
                if (message != null && message["msg-type"] == "command") {
                    val payload = message["payload"] as? Map<*, *>
                    handleCommand(payload ?: emptyMap<String, Any>())
                }
            } catch (e: Exception) {
                if (running.get()) {
                    messageProtocol.sendException("Command parsing error", e)
                }
            }
        }
    }

    private fun handleCommand(command: Map<*, *>) {
        val cmd = command["action"] as? String ?: return

        when (cmd) {
            "stop", "shutdown" -> {
                stop()
            }
            "pause" -> {
                // Could implement pause/resume for video
            }
            "resume" -> {
                // Could implement pause/resume for video
            }
            "show" -> {
                frameManager.showFrame()
            }
            "hide" -> {
                frameManager.hideFrame()
            }
            else -> {
                // Ignore unknown commands
            }
        }
    }

    private fun stop() {
        if (running.compareAndSet(true, false)) {
            shutdownLatch.countDown()
        }
    }

    private fun cleanup() {
        try {
            // Stop accepting new tasks
            running.set(false)

            // Close WebSocket
            webSocketClient.close()

            // Stop GStreamer pipeline
            gstreamerPipeline.stop()

            // Clean up event handlers
            mouseEventHandler?.cleanup()
            windowEventHandler?.cleanup()

            // Dispose frame
            frameManager.disposeFrame()

            // Shutdown executors
            reconnectExecutor.shutdown()
            eventThrottleExecutor.shutdown()

            try {
                if (!reconnectExecutor.awaitTermination(
                        Constants.EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS,
                    )
                ) {
                    reconnectExecutor.shutdownNow()
                }
                if (!eventThrottleExecutor.awaitTermination(
                        Constants.EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS,
                    )
                ) {
                    eventThrottleExecutor.shutdownNow()
                }
            } catch (_: InterruptedException) {
                reconnectExecutor.shutdownNow()
                eventThrottleExecutor.shutdownNow()
                Thread.currentThread().interrupt()
            }

            // Send final response
            messageProtocol.sendResponse("stopped", mapOf(MessageKeys.STREAM_ID to streamId))
        } catch (e: Exception) {
            messageProtocol.sendException("Cleanup error", e)
        }
    }

    // MouseEventHandler.EventCallback implementation
    override fun onNavigationEvent(
        type: EventFilter.EventType,
        eventName: String,
        x: Int,
        y: Int,
        details: Map<String, Any>?,
    ) {
        frameManager.getVideoComponent()?.let { videoComponent ->
            // Include current frame timing information
            val frameTimestamp = currentFrameTimestamp.get()
            val frameDuration = currentFrameDuration.get()

            // Calculate NDC coordinates
            val canvasWidth = videoComponent.width
            val canvasHeight = videoComponent.height
            val ndcX = if (canvasWidth > 0) (2.0 * x / canvasWidth - 1.0) else 0.0
            val ndcY = if (canvasHeight > 0) (1.0 - 2.0 * y / canvasHeight) else 0.0

            messageProtocol.sendNavigationEvent(
                x,
                y,
                frameTimestamp,
                frameDuration,
                canvasWidth,
                canvasHeight,
                eventName,
                ndcX,
                ndcY,
            )
        }
    }

    // WindowEventHandler.EventCallback implementation
    override fun onWindowEvent(
        type: EventFilter.EventType,
        eventName: String,
        details: Map<String, Any>?,
    ) {
        val windowState = details?.get("windowState") as? Int
        val x = details?.get("x") as? Int
        val y = details?.get("y") as? Int
        val width = details?.get("width") as? Int
        val height = details?.get("height") as? Int

        messageProtocol.sendWindowEvent(eventName, windowState, x, y, width, height)
    }

    // GStreamerPipeline.EventCallback implementation
    override fun onLog(
        level: String,
        message: String,
    ) {
        if (running.get()) {
            messageProtocol.sendLog(level, message)
        }
    }

    override fun onPipelineError(message: String) {
        messageProtocol.sendError("Pipeline error: $message")
        // Could trigger reconnection or other error handling
    }

    override fun isRunning(): Boolean = running.get()

    // New MouseEventHandler.EventCallback methods for gestures
    override fun onGestureEvent(event: Map<String, Any>) {
        // Send gesture event to main process via Transit
        messageProtocol.sendEvent(EventType.GESTURE.key, event)
    }

    override fun sendCommand(command: Map<String, Any>) {
        // Send command to main process for forwarding to command subprocess
        messageProtocol.sendRequest("forward-command", command)
    }

    // FrameDataProvider implementation
    override fun getFrameData(): potatoclient.kotlin.gestures.FrameData? {
        val timestamp = currentFrameTimestamp.get()
        val duration = currentFrameDuration.get()
        return if (timestamp > 0) {
            potatoclient.kotlin.gestures.FrameData(timestamp, duration)
        } else {
            null
        }
    }

    override fun getCurrentZoomLevel(): Int {
        // TODO: Track zoom level from state updates
        // For now, return default zoom level 0
        return 0
    }

    // FrameManager.FrameEventListener implementation
    override fun onFrameCreated(
        frame: JFrame,
        videoComponent: Component,
    ) {
        try {
            // Initialize GStreamer pipeline with video component
            gstreamerPipeline.initialize(videoComponent)

            // Set up event handlers with gesture support
            val streamType =
                if (streamId.contains("heat", ignoreCase = true)) {
                    potatoclient.kotlin.gestures.StreamType.HEAT
                } else {
                    potatoclient.kotlin.gestures.StreamType.DAY
                }
            mouseEventHandler =
                MouseEventHandler(
                    videoComponent,
                    this,
                    eventFilter,
                    eventThrottleExecutor,
                    streamType,
                    this, // as FrameDataProvider
                )
            mouseEventHandler?.attachListeners()

            windowEventHandler = WindowEventHandler(frame, this, eventFilter, eventThrottleExecutor)
            windowEventHandler?.attachListeners()
        } catch (e: Exception) {
            messageProtocol.sendException("Frame setup error", e)
        }
    }

    override fun onFrameClosing() {
        // Only send the close event once to avoid multiple messages
        if (closeEventSent.compareAndSet(false, true)) {
            // Send a window-closed event to the main process
            messageProtocol.sendResponse(
                "window-closed",
                mapOf(
                    MessageKeys.STREAM_ID to streamId,
                    "reason" to "user-closed",
                ),
            )
            // Don't stop immediately - let the main process handle shutdown
        } else {
            // Frame closing already handled, ignore duplicate event
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Install stdout interceptor EARLY before any code runs
            potatoclient.kotlin.transit.StdoutInterceptor
                .installEarly()

            // Give the parent process time to set up Transit reader
            Thread.sleep(100)

            if (args.size < 3) {
                System.err.println("Usage: VideoStreamManager <streamId> <streamUrl> <domain>")
                exitProcess(1)
            }

            val streamId = args[0]
            val streamUrl = args[1]
            val domain = args[2]

            // Initialize logging for this subprocess
            potatoclient.kotlin.transit.LoggingUtils
                .initializeLogging("video-stream-$streamId")

            // Install shutdown hook for clean exit
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    potatoclient.kotlin.transit.logInfo("Shutdown hook triggered for video stream $streamId")
                },
            )

            try {
                val manager = VideoStreamManager(streamId, streamUrl, domain)
                manager.start()
            } catch (e: Exception) {
                System.err.println("Fatal error: ${e.message}")
                potatoclient.kotlin.transit.logError("Fatal error in video stream", e)
                exitProcess(1)
            } finally {
                potatoclient.kotlin.transit.LoggingUtils
                    .close()
            }
        }
    }
}
