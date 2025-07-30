package potatoclient.transit

import cmd.*
import ser.*
import com.google.protobuf.Message
import com.google.protobuf.MessageOrBuilder
import kotlinx.coroutines.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Simplified Command subprocess without protovalidate
 */
class CommandSubprocess(
    private val wsUrl: String,
    private val transitComm: TransitCommunicator
) {
    private val wsClient = CommandWebSocketClient(wsUrl)
    private val cmdBuilder = TransitToProtobufBuilder()
    
    // Metrics
    private val totalReceived = AtomicInteger(0)
    private val totalSent = AtomicInteger(0)
    
    suspend fun run() = coroutineScope {
        // Start WebSocket connection
        launch {
            wsClient.connect()
        }
        
        // Handle messages from Clojure
        launch {
            while (isActive) {
                val msg = transitComm.readMessage()
                if (msg != null) {
                    when (msg["msg-type"]) {
                        "command" -> handleCommand(msg)
                        "control" -> handleControl(msg)
                        else -> println("Unknown message type: ${msg["msg-type"]}")
                    }
                    totalReceived.incrementAndGet()
                }
            }
        }
    }
    
    private suspend fun handleCommand(msg: Map<*, *>) {
        val payload = msg["payload"] as? Map<*, *> ?: return
        val msgId = msg["msg-id"] as? String ?: ""
        
        try {
            val protoCmd = cmdBuilder.buildCommand(payload)
            val rootCmd = JonSharedCmd.Root.newBuilder()
                .setCmd(protoCmd)
                .build()
            
            wsClient.send(rootCmd.toByteArray())
            totalSent.incrementAndGet()
            
            // Send success response
            transitComm.sendMessage(
                mapOf(
                    "msg-type" to "response",
                    "msg-id" to msgId,
                    "timestamp" to System.currentTimeMillis(),
                    "payload" to mapOf("status" to "sent")
                )
            )
        } catch (e: Exception) {
            sendError(msgId, e)
        }
    }
    
    private suspend fun handleControl(msg: Map<*, *>) {
        val payload = msg["payload"] as? Map<*, *> ?: return
        when (payload["action"]) {
            "shutdown" -> {
                wsClient.close()
                transitComm.close()
            }
            "get-stats" -> {
                transitComm.sendMessage(
                    transitComm.createMessage(
                        "stats",
                        mapOf(
                            "received" to totalReceived.get(),
                            "sent" to totalSent.get(),
                            "ws-connected" to wsClient.isConnected()
                        )
                    )
                )
            }
        }
    }
    
    private suspend fun sendError(msgId: String, error: Exception) {
        transitComm.sendMessage(
            mapOf(
                "msg-type" to "response",
                "msg-id" to msgId,
                "timestamp" to System.currentTimeMillis(),
                "payload" to mapOf(
                    "status" to "error",
                    "error" to error.message
                )
            )
        )
    }
}

/**
 * Minimal protobuf builder
 */
class TransitToProtobufBuilder {
    fun buildCommand(payload: Map<*, *>): JonSharedCmd.Cmd {
        val action = payload["action"] as? String
            ?: throw IllegalArgumentException("Missing 'action' field")
        
        return when (action) {
            // System commands
            "ping" -> buildPing()
            "set-localization" -> buildSetLocalization(payload)
            "set-recording" -> buildSetRecording(payload)
            
            // GPS commands
            "gps-set-manual" -> buildGpsSetManual(payload)
            
            // Compass commands
            "compass-set-unit" -> buildCompassSetUnit(payload)
            
            // LRF commands
            "lrf-single-measurement" -> buildLrfSingleMeasurement()
            "lrf-continuous-start" -> buildLrfContinuousStart()
            "lrf-continuous-stop" -> buildLrfContinuousStop()
            
            // Rotary commands
            "rotary-goto" -> buildRotaryGoto(payload)
            "rotary-stop" -> buildRotaryStop()
            "rotary-velocity" -> buildRotaryVelocity(payload)
            
            // Camera commands
            "day-camera-set-zoom" -> buildDayCameraSetZoom(payload)
            "heat-camera-set-zoom" -> buildHeatCameraSetZoom(payload)
            "day-camera-set-focus" -> buildDayCameraSetFocus(payload)
            
            else -> throw IllegalArgumentException("Unknown command action: $action")
        }
    }
    
    private fun buildPing(): JonSharedCmd.Cmd =
        JonSharedCmd.Cmd.newBuilder()
            .setSystem(JonSharedCmdSystem.Cmd.newBuilder()
                .setPing(JonSharedCmdSystem.Ping.newBuilder()))
            .build()
    
    private fun buildSetLocalization(payload: Map<*, *>): JonSharedCmd.Cmd {
        val locale = payload["locale"] as? String ?: "english"
        return JonSharedCmd.Cmd.newBuilder()
            .setSystem(JonSharedCmdSystem.Cmd.newBuilder()
                .setSetLocalization(JonSharedCmdSystem.SetLocalization.newBuilder()
                    .setLang(locale)))
            .build()
    }
    
    private fun buildSetRecording(payload: Map<*, *>): JonSharedCmd.Cmd {
        val enabled = payload["enabled"] as? Boolean ?: false
        return JonSharedCmd.Cmd.newBuilder()
            .setSystem(JonSharedCmdSystem.Cmd.newBuilder()
                .setSetRecording(JonSharedCmdSystem.SetRecording.newBuilder()
                    .setRecording(enabled)))
            .build()
    }
    
    private fun buildGpsSetManual(payload: Map<*, *>): JonSharedCmd.Cmd {
        val lat = (payload["latitude"] as? Number)?.toDouble() ?: 0.0
        val lon = (payload["longitude"] as? Number)?.toDouble() ?: 0.0
        val alt = (payload["altitude"] as? Number)?.toDouble() ?: 0.0
        
        return JonSharedCmd.Cmd.newBuilder()
            .setGps(JonSharedCmdGps.Cmd.newBuilder()
                .setSetManual(JonSharedCmdGps.SetManual.newBuilder()
                    .setLatitude(lat)
                    .setLongitude(lon)
                    .setAltitude(alt)))
            .build()
    }
    
    private fun buildCompassSetUnit(payload: Map<*, *>): JonSharedCmd.Cmd {
        val degrees = payload["degrees"] as? Boolean ?: true
        return JonSharedCmd.Cmd.newBuilder()
            .setCompass(JonSharedCmdCompass.Cmd.newBuilder()
                .setSetUnit(JonSharedCmdCompass.SetUnit.newBuilder()
                    .setDegrees(degrees)))
            .build()
    }
    
    private fun buildLrfSingleMeasurement(): JonSharedCmd.Cmd =
        JonSharedCmd.Cmd.newBuilder()
            .setLrf(JonSharedCmdLrf.Cmd.newBuilder()
                .setSingleMeasurement(JonSharedCmdLrf.SingleMeasurement.newBuilder()))
            .build()
    
    private fun buildLrfContinuousStart(): JonSharedCmd.Cmd =
        JonSharedCmd.Cmd.newBuilder()
            .setLrf(JonSharedCmdLrf.Cmd.newBuilder()
                .setContinuousStart(JonSharedCmdLrf.ContinuousStart.newBuilder()))
            .build()
    
    private fun buildLrfContinuousStop(): JonSharedCmd.Cmd =
        JonSharedCmd.Cmd.newBuilder()
            .setLrf(JonSharedCmdLrf.Cmd.newBuilder()
                .setContinuousStop(JonSharedCmdLrf.ContinuousStop.newBuilder()))
            .build()
    
    private fun buildRotaryGoto(payload: Map<*, *>): JonSharedCmd.Cmd {
        val azimuth = (payload["azimuth"] as? Number)?.toFloat() ?: 0f
        val elevation = (payload["elevation"] as? Number)?.toFloat() ?: 0f
        
        return JonSharedCmd.Cmd.newBuilder()
            .setRotary(JonSharedCmdRotary.Cmd.newBuilder()
                .setGoto(JonSharedCmdRotary.Goto.newBuilder()
                    .setAzimuth(azimuth)
                    .setElevation(elevation)))
            .build()
    }
    
    private fun buildRotaryStop(): JonSharedCmd.Cmd =
        JonSharedCmd.Cmd.newBuilder()
            .setRotary(JonSharedCmdRotary.Cmd.newBuilder()
                .setStop(JonSharedCmdRotary.Stop.newBuilder()))
            .build()
    
    private fun buildRotaryVelocity(payload: Map<*, *>): JonSharedCmd.Cmd {
        val azimuthVel = (payload["azimuth-velocity"] as? Number)?.toFloat() ?: 0f
        val elevationVel = (payload["elevation-velocity"] as? Number)?.toFloat() ?: 0f
        
        return JonSharedCmd.Cmd.newBuilder()
            .setRotary(JonSharedCmdRotary.Cmd.newBuilder()
                .setVelocity(JonSharedCmdRotary.Velocity.newBuilder()
                    .setAzimuthVelocity(azimuthVel)
                    .setElevationVelocity(elevationVel)))
            .build()
    }
    
    private fun buildDayCameraSetZoom(payload: Map<*, *>): JonSharedCmd.Cmd {
        val zoom = (payload["zoom"] as? Number)?.toFloat() ?: 1.0f
        return JonSharedCmd.Cmd.newBuilder()
            .setDayCamera(JonSharedCmdDayCamera.Cmd.newBuilder()
                .setSetZoom(JonSharedCmdDayCamera.SetZoom.newBuilder()
                    .setZoom(zoom)))
            .build()
    }
    
    private fun buildHeatCameraSetZoom(payload: Map<*, *>): JonSharedCmd.Cmd {
        val zoom = (payload["zoom"] as? Number)?.toFloat() ?: 1.0f
        return JonSharedCmd.Cmd.newBuilder()
            .setHeatCamera(JonSharedCmdHeatCamera.Cmd.newBuilder()
                .setSetZoom(JonSharedCmdHeatCamera.SetZoom.newBuilder()
                    .setZoom(zoom)))
            .build()
    }
    
    private fun buildDayCameraSetFocus(payload: Map<*, *>): JonSharedCmd.Cmd {
        val builder = JonSharedCmdDayCamera.SetFocus.newBuilder()
        
        when (payload["mode"]) {
            "auto" -> builder.auto = JonSharedCmdDayCamera.SetFocus.Auto.newBuilder().build()
            "manual" -> {
                val distance = (payload["distance"] as? Number)?.toFloat() ?: 0f
                builder.manual = JonSharedCmdDayCamera.SetFocus.Manual.newBuilder()
                    .setDistance(distance)
                    .build()
            }
            "infinity" -> builder.infinity = JonSharedCmdDayCamera.SetFocus.Infinity.newBuilder().build()
        }
        
        return JonSharedCmd.Cmd.newBuilder()
            .setDayCamera(JonSharedCmdDayCamera.Cmd.newBuilder()
                .setSetFocus(builder))
            .build()
    }
}

/**
 * Simple WebSocket client
 */
class CommandWebSocketClient(private val url: String) {
    private var webSocket: WebSocket? = null
    private val connected = AtomicBoolean(false)
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
    
    suspend fun connect() = coroutineScope {
        try {
            val future = httpClient.newWebSocketBuilder()
                .buildAsync(URI.create(url), object : WebSocket.Listener {
                    override fun onOpen(webSocket: WebSocket) {
                        connected.set(true)
                        println("WebSocket connected to $url")
                        webSocket.request(1)
                    }
                    
                    override fun onBinary(webSocket: WebSocket, data: ByteBuffer, last: Boolean): CompletionStage<*>? {
                        webSocket.request(1)
                        return null
                    }
                    
                    override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String): CompletionStage<*>? {
                        connected.set(false)
                        println("WebSocket closed: $statusCode $reason")
                        return null
                    }
                    
                    override fun onError(webSocket: WebSocket, error: Throwable) {
                        connected.set(false)
                        error.printStackTrace()
                    }
                })
                .await()
            
            webSocket = future
        } catch (e: Exception) {
            println("Failed to connect: ${e.message}")
            throw e
        }
    }
    
    fun send(data: ByteArray) {
        webSocket?.send(ByteBuffer.wrap(data), true)
    }
    
    fun close() {
        webSocket?.sendClose(WebSocket.NORMAL_CLOSURE, "Shutdown")
    }
    
    fun isConnected() = connected.get()
}

// Extension function for CompletableFuture
suspend fun <T> java.util.concurrent.CompletableFuture<T>.await(): T = withContext(Dispatchers.IO) {
    this@await.get()
}

// Main entry point
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: CommandSubprocess <websocket-url>")
        System.exit(1)
    }
    
    val wsUrl = args[0]
    val transitComm = TransitCommunicator(System.`in`, System.out)
    val subprocess = CommandSubprocess(wsUrl, transitComm)
    
    runBlocking {
        subprocess.run()
    }
}