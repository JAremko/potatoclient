package potatoclient.kotlin

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.awt.Component
import java.net.URI
import java.nio.ByteOrder
import java.util.Scanner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JFrame
import kotlin.system.exitProcess

class VideoStreamManager(
    private val streamId: String,
    private val streamUrl: String,
    domain: String,
) : MouseEventHandler.EventCallback,
    WindowEventHandler.EventCallback,
    GStreamerPipeline.EventCallback,
    FrameManager.FrameEventListener {
    // Core components
    private val scanner = Scanner(System.`in`)
    private val mapper = ObjectMapper() // For command parsing

    // Thread-safe primitives
    private val running = AtomicBoolean(true)
    private val shutdownLatch = CountDownLatch(1)

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
    private val messageProtocol = MessageProtocol(streamId)
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
                            // Extract timestamp and duration from the beginning of the message
                            // Create a view with little-endian byte order for reading
                            val timestampBuffer = data.duplicate()
                            timestampBuffer.order(ByteOrder.LITTLE_ENDIAN)

                            // Read 64-bit timestamp and duration
                            val timestamp = timestampBuffer.getLong()
                            val duration = timestampBuffer.getLong()

                            // Store current frame timing
                            currentFrameTimestamp.set(timestamp)
                            currentFrameDuration.set(duration)

                            // Skip the 16-byte prefix and create a slice with just the video data
                            data.position(data.position() + 16)
                            val videoData = data.slice()

                            // Push only the video data to pipeline
                            gstreamerPipeline.pushVideoData(videoData)
                        } else {
                            messageProtocol.sendLog("WARN", "Frame too short to contain timing info: ${data.remaining()} bytes")
                        }

                        // Return buffer to pool if it's from the pool
                        // Only direct buffers from the pool need to be released
                        if (data.isDirect && data.capacity() <= Constants.MAX_BUFFER_SIZE) {
                            webSocketClient.getBufferPool().release(data)
                        }
                    }
                },
                onConnect = {
                    messageProtocol.sendLog("INFO", "WebSocket connected to $streamUrl")
                },
                onClose = { code, reason ->
                    messageProtocol.sendLog("INFO", "WebSocket closed: $reason (code: $code)")
                },
                onError = { error ->
                    messageProtocol.sendException("WebSocket error", error as Exception)
                },
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to create WebSocket client", e)
        }

    fun start() {
        try {
            messageProtocol.sendLog("INFO", "Starting video stream manager for $streamId")
            messageProtocol.sendResponse("starting", streamId)

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
        while (running.get() && scanner.hasNextLine()) {
            try {
                val line = scanner.nextLine()
                if (!line.isNullOrBlank()) {
                    val command = mapper.readValue(line, object : TypeReference<Map<String, Any>>() {})
                    handleCommand(command)
                }
            } catch (e: Exception) {
                if (running.get()) {
                    messageProtocol.sendException("Command parsing error", e)
                }
            }
        }
    }

    private fun handleCommand(command: Map<String, Any>) {
        val cmd = command["command"] as? String ?: return

        when (cmd) {
            "stop" -> {
                messageProtocol.sendLog("INFO", "Received stop command")
                stop()
            }
            "pause" -> {
                // Could implement pause/resume for video
                messageProtocol.sendLog("INFO", "Pause not implemented")
            }
            "resume" -> {
                // Could implement pause/resume for video
                messageProtocol.sendLog("INFO", "Resume not implemented")
            }
            else -> {
                messageProtocol.sendLog("WARN", "Unknown command: $cmd")
            }
        }
    }

    private fun stop() {
        if (running.compareAndSet(true, false)) {
            messageProtocol.sendLog("INFO", "Stopping video stream manager")
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
                if (!reconnectExecutor.awaitTermination(Constants.EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    reconnectExecutor.shutdownNow()
                }
                if (!eventThrottleExecutor.awaitTermination(Constants.EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    eventThrottleExecutor.shutdownNow()
                }
            } catch (_: InterruptedException) {
                reconnectExecutor.shutdownNow()
                eventThrottleExecutor.shutdownNow()
                Thread.currentThread().interrupt()
            }

            // Send final response
            messageProtocol.sendResponse("stopped", streamId)
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
            messageProtocol.sendNavigationEvent(
                eventName,
                x,
                y,
                videoComponent,
                details,
                frameTimestamp,
                frameDuration,
            )
        }
    }

    // WindowEventHandler.EventCallback implementation
    override fun onWindowEvent(
        type: EventFilter.EventType,
        eventName: String,
        details: Map<String, Any>?,
    ) {
        messageProtocol.sendWindowEvent(eventName, details)
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
        messageProtocol.sendLog("ERROR", "Pipeline error: $message")
        // Could trigger reconnection or other error handling
    }

    override fun isRunning(): Boolean = running.get()

    // FrameManager.FrameEventListener implementation
    override fun onFrameCreated(
        frame: JFrame,
        videoComponent: Component,
    ) {
        try {
            // Initialize GStreamer pipeline with video component
            gstreamerPipeline.initialize(videoComponent)

            // Set up event handlers
            mouseEventHandler = MouseEventHandler(videoComponent, this, eventFilter, eventThrottleExecutor)
            mouseEventHandler?.attachListeners()

            windowEventHandler = WindowEventHandler(frame, this, eventFilter, eventThrottleExecutor)
            windowEventHandler?.attachListeners()

            messageProtocol.sendLog("INFO", "Frame created and event handlers attached")
        } catch (e: Exception) {
            messageProtocol.sendException("Frame setup error", e)
        }
    }

    override fun onFrameClosing() {
        messageProtocol.sendLog("INFO", "Frame closing requested")
        stop()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 3) {
                System.err.println("Usage: VideoStreamManager <streamId> <streamUrl> <domain>")
                exitProcess(1)
            }

            val streamId = args[0]
            val streamUrl = args[1]
            val domain = args[2]

            try {
                val manager = VideoStreamManager(streamId, streamUrl, domain)
                manager.start()
            } catch (e: Exception) {
                System.err.println("Fatal error: ${e.message}")
                // Stack trace already printed to stderr via exception message
                exitProcess(1)
            }
        }
    }
}
