package potatoclient.kotlin

import com.fasterxml.jackson.databind.ObjectMapper
import java.awt.Component
import java.net.URI
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JFrame

class VideoStreamManager(
    private val streamId: String,
    private val streamUrl: String,
    private val domain: String
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

    // Executor services
    private val reconnectExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "VideoStream-Reconnect-$streamId").apply { isDaemon = true }
    }
    private val eventThrottleExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(
        Constants.EVENT_THROTTLE_POOL_SIZE
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
    
    private fun createWebSocketClient(): WebSocketClientBuiltIn {
        return try {
            val uri = URI(streamUrl)
            val headers = mutableMapOf(
                "Origin" to "https://${uri.host}",
                "User-Agent" to Constants.WS_USER_AGENT,
                "Cache-Control" to Constants.WS_CACHE_CONTROL,
                "Pragma" to Constants.WS_PRAGMA
            )
            
            WebSocketClientBuiltIn(
                serverUri = uri,
                headers = headers,
                onBinaryMessage = { data ->
                    // Fast path - atomic check only
                    if (running.get()) {
                        // Push to pipeline (it handles its own synchronization)
                        gstreamerPipeline.pushVideoData(data)
                        
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
                }
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to create WebSocket client", e)
        }
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
            } catch (e: InterruptedException) {
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
                    val command = mapper.readValue(line, Map::class.java)
                    handleCommand(command as Map<String, Any>)
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
            } catch (e: InterruptedException) {
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
        details: Map<String, Any>?
    ) {
        frameManager.getVideoComponent()?.let { videoComponent ->
            messageProtocol.sendNavigationEvent(eventName, x, y, videoComponent, details)
        }
    }
    
    // WindowEventHandler.EventCallback implementation
    override fun onWindowEvent(type: EventFilter.EventType, eventName: String, details: Map<String, Any>?) {
        messageProtocol.sendWindowEvent(eventName, details)
    }
    
    // GStreamerPipeline.EventCallback implementation
    override fun onLog(level: String, message: String) {
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
    override fun onFrameCreated(frame: JFrame, videoComponent: Component) {
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
                System.exit(1)
            }
            
            val streamId = args[0]
            val streamUrl = args[1]
            val domain = args[2]
            
            try {
                val manager = VideoStreamManager(streamId, streamUrl, domain)
                manager.start()
            } catch (e: Exception) {
                System.err.println("Fatal error: ${e.message}")
                e.printStackTrace()
                System.exit(1)
            }
        }
    }
}