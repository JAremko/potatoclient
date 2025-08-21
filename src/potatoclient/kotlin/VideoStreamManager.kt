package potatoclient.kotlin

import potatoclient.kotlin.ipc.IpcClient
import potatoclient.kotlin.ipc.IpcKeys
import potatoclient.kotlin.gestures.FrameDataProvider
import potatoclient.kotlin.gestures.FrameData
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
    parentPid: Long,
) : GStreamerPipeline.EventCallback,
    FrameManager.FrameEventListener,
    FrameDataProvider {

    // IPC communication
    private val ipcClient = IpcClient.create(parentPid, streamId)

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

    // Module instances
    private val frameManager = FrameManager(streamId, domain, this, ipcClient)
    private var mouseEventHandler: MouseEventHandler? = null
    private var windowEventHandler: WindowEventHandler? = null
    private val webSocketClient: WebSocketClientBuiltIn
    private val gstreamerPipeline = GStreamerPipeline(this)

    init {
        // IPC client is already connected via create() in the constructor

        // Register message handler for incoming commands
        ipcClient.onMessage { message ->
            handleIncomingMessage(message)
        }

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
                    // Report websocket connection
                    ipcClient.sendConnectionEvent(IpcKeys.CONNECTED, mapOf(
                        "url" to streamUrl,
                        "stream-id" to streamId
                    ))
                },
                onClose = { code, reason ->
                    // Report websocket disconnection
                    ipcClient.sendConnectionEvent(IpcKeys.DISCONNECTED, mapOf(
                        "code" to code,
                        "reason" to reason,
                        "stream-id" to streamId
                    ))
                },
                onError = { error ->
                    // Report websocket error
                    ipcClient.sendConnectionEvent(IpcKeys.CONNECTION_ERROR, mapOf<Any, Any>(
                        "error" to (error.message ?: "Unknown error"),
                        "stream-id" to streamId
                    ))
                    ipcClient.sendLog(IpcKeys.ERROR, "WebSocket error: ${error.message}")
                },
            )
        } catch (e: Exception) {
            throw VideoStreamCreationException("Failed to create WebSocket client", e)
        }

    fun start() {
        try {
            ipcClient.sendLog(IpcKeys.INFO, "Starting video stream $streamId")

            // Create and show frame
            frameManager.createFrame()

            // Start WebSocket connection
            webSocketClient.connect()

            // Wait for shutdown
            try {
                shutdownLatch.await()
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        } catch (e: Exception) {
            ipcClient.sendLog(IpcKeys.ERROR, "Startup error: ${e.message}")
        } finally {
            cleanup()
        }
    }

    private fun handleIncomingMessage(message: Map<*, *>) {
        val msgType = message[IpcKeys.MSG_TYPE]
        val action = message[IpcKeys.ACTION]

        when (msgType) {
            IpcKeys.COMMAND -> {
                when (action) {
                    IpcKeys.keyword("stop"), IpcKeys.keyword("shutdown") -> {
                        stop()
                    }
                    IpcKeys.keyword("pause") -> {
                        // TODO: Implement pause when gstreamerPipeline supports it
                        // gstreamerPipeline.pause()
                    }
                    IpcKeys.keyword("play") -> {
                        // TODO: Implement play when gstreamerPipeline supports it
                        // gstreamerPipeline.play()
                    }
                    IpcKeys.keyword("reconnect") -> {
                        // TODO: Implement reconnect when webSocketClient supports it
                        // webSocketClient.reconnect()
                    }
                }
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

            try {
                if (!reconnectExecutor.awaitTermination(
                        Constants.EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS,
                    )
                ) {
                    reconnectExecutor.shutdownNow()
                }
            } catch (_: InterruptedException) {
                reconnectExecutor.shutdownNow()
                Thread.currentThread().interrupt()
            }

            // Send final disconnection event
            ipcClient.sendConnectionEvent(IpcKeys.DISCONNECTED, mapOf(
                "stream-id" to streamId
            ))

            // Shutdown IPC
            ipcClient.shutdown()
        } catch (e: Exception) {
            System.err.println("Cleanup error: ${e.message}")
        }
    }

    // GStreamerPipeline.EventCallback implementation
    override fun onLog(
        level: String,
        message: String,
    ) {
        if (running.get()) {
            val logLevel = when(level) {
            "DEBUG" -> IpcKeys.DEBUG
            "INFO" -> IpcKeys.INFO
            "WARN" -> IpcKeys.WARN
            "ERROR" -> IpcKeys.ERROR
            else -> IpcKeys.INFO
        }
        ipcClient.sendLog(logLevel, message)
        }
    }

    override fun onPipelineError(message: String) {
        ipcClient.sendLog(IpcKeys.ERROR, "Pipeline error: $message")
        // Could trigger reconnection or other error handling
    }

    override fun isRunning(): Boolean = running.get()

    // FrameDataProvider implementation
    override fun getFrameData(): FrameData? {
        val timestamp = currentFrameTimestamp.get()
        val duration = currentFrameDuration.get()
        return if (timestamp > 0) {
            FrameData(timestamp, duration)
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

            // Set up mouse event handler with IPC
            mouseEventHandler = MouseEventHandler(
                videoComponent,
                ipcClient,
                this // as FrameDataProvider
            )
            mouseEventHandler?.attachListeners()

            // Set up window event handler with IPC and shutdown callback
            windowEventHandler = WindowEventHandler(
                frame,
                ipcClient,
                throttleMs = 100L,
                onShutdown = {
                    // Additional cleanup when window is closed
                    cleanup()
                }
            )
            windowEventHandler?.attachListeners()
        } catch (e: Exception) {
            ipcClient.sendLog(IpcKeys.ERROR, "Frame setup error: ${e.message}")
        }
    }

    override fun onFrameClosing() {
        // Only send the close event once to avoid multiple messages
        if (closeEventSent.compareAndSet(false, true)) {
            // Clean shutdown
            stop()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 4) {
                System.err.println("Usage: VideoStreamManager <streamId> <streamUrl> <domain> <parentPid>")
                exitProcess(1)
            }

            val streamId = args[0]
            val streamUrl = args[1]
            val domain = args[2]
            val parentPid = args[3].toLong()

            try {
                val manager = VideoStreamManager(streamId, streamUrl, domain, parentPid)
                manager.start()
            } catch (e: Exception) {
                System.err.println("Failed to start video stream: ${e.message}")
                e.printStackTrace()
                exitProcess(1)
            } finally {
                // Exit cleanly
                exitProcess(0)
            }
        }
    }
}
