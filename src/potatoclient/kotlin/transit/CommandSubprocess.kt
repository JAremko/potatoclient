package potatoclient.transit

import cmd.*
import ser.*
import build.buf.protovalidate.Validator
import build.buf.protovalidate.ValidationResult
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
 * Command subprocess that receives commands from Clojure via Transit
 * and forwards them to WebSocket as protobuf (no rate limiting)
 */
class CommandSubprocess(
    private val wsUrl: String,
    private val transitComm: TransitCommunicator
) {
    private val wsClient = CommandWebSocketClient(wsUrl)
    private val cmdBuilder = TransitToProtobufBuilder()
    private val validator: Validator = Validator.newValidator()
    private val isReleaseBuild = System.getProperty("potatoclient.release") != null ||
                                 System.getenv("POTATOCLIENT_RELEASE") != null
    
    // Metrics
    private val totalReceived = AtomicInteger(0)
    private val totalSent = AtomicInteger(0)
    private val validationErrors = AtomicInteger(0)
    
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
                    }
                }
            }
        }
    }
    
    private suspend fun handleCommand(msg: Map<*, *>) {
        totalReceived.incrementAndGet()
        
        val payload = msg["payload"] as? Map<*, *> ?: return
        val msgId = msg["msg-id"] as? String ?: return
        
        try {
            // Build protobuf command from Transit data
            val protoCmd = cmdBuilder.buildCommand(payload)
            
            // Validate if not in release mode
            if (!isReleaseBuild) {
                val result = validator.validate(protoCmd)
                if (!result.isSuccess) {
                    validationErrors.incrementAndGet()
                    sendValidationError(msgId, result, payload)
                    return
                }
            }
            
            // Send immediately - no rate limiting for commands
            wsClient.sendCommand(protoCmd.toByteArray())
            totalSent.incrementAndGet()
            
            // Send acknowledgment
            transitComm.sendMessage(
                mapOf(
                    "msg-type" to "response",
                    "msg-id" to msgId,
                    "timestamp" to System.currentTimeMillis(),
                    "payload" to mapOf(
                        "status" to "sent",
                        "command" to (payload["action"] ?: "unknown")
                    )
                )
            )
            
        } catch (e: Exception) {
            sendError(msgId, e)
        }
    }
    
    private suspend fun sendValidationError(
        msgId: String,
        result: ValidationResult,
        payload: Map<*, *>
    ) {
        val errors = result.violations.map { violation ->
            mapOf(
                "field" to violation.fieldPath.toString(),
                "constraint" to violation.constraintId,
                "message" to violation.message
            )
        }
        
        transitComm.sendMessage(
            transitComm.createMessage(
                "validation-error",
                mapOf(
                    "source" to "buf-validate",
                    "command" to (payload["action"] ?: "unknown"),
                    "errors" to errors
                )
            )
        )
        
        // Also send error response
        transitComm.sendMessage(
            mapOf(
                "msg-type" to "response",
                "msg-id" to msgId,
                "timestamp" to System.currentTimeMillis(),
                "payload" to mapOf(
                    "status" to "validation-error",
                    "errors" to errors
                )
            )
        )
    }
    
    private suspend fun sendError(msgId: String, error: Exception) {
        transitComm.sendMessage(
            mapOf(
                "msg-type" to "response",
                "msg-id" to msgId,
                "timestamp" to System.currentTimeMillis(),
                "payload" to mapOf(
                    "status" to "error",
                    "message" to (error.message ?: "Unknown error")
                )
            )
        )
    }
    
    private suspend fun handleControl(msg: Map<*, *>) {
        val payload = msg["payload"] as? Map<*, *> ?: return
        val msgId = msg["msg-id"] as? String
        
        when (payload["action"]) {
            "get-logs" -> {
                val lines = (payload["lines"] as? Number)?.toInt() ?: 100
                if (msgId != null) sendLogs(msgId, lines)
            }
            "get-metrics" -> {
                if (msgId != null) sendMetrics(msgId)
            }
            "shutdown" -> {
                gracefulShutdown()
            }
        }
    }
    
    private suspend fun sendMetrics(msgId: String) {
        val metrics = mapOf(
            "total-received" to totalReceived.get(),
            "total-sent" to totalSent.get(),
            "validation-errors" to validationErrors.get(),
            "error-rate" to (validationErrors.get().toDouble() / totalReceived.get()),
            "websocket-connected" to wsClient.isConnected()
        )
        
        transitComm.sendMessage(
            mapOf(
                "msg-type" to "response",
                "msg-id" to msgId,
                "timestamp" to System.currentTimeMillis(),
                "payload" to mapOf(
                    "status" to "ok",
                    "data" to metrics
                )
            )
        )
    }
    
    private suspend fun sendLogs(msgId: String, lines: Int) {
        // In real implementation, would read from actual log buffer
        val logs = listOf("Command subprocess running", "WebSocket connected to $wsUrl")
        
        transitComm.sendMessage(
            mapOf(
                "msg-type" to "response",
                "msg-id" to msgId,
                "timestamp" to System.currentTimeMillis(),
                "payload" to mapOf(
                    "status" to "ok",
                    "data" to logs
                )
            )
        )
    }
    
    private fun gracefulShutdown() {
        wsClient.close()
        transitComm.close()
    }
}

/**
 * Builds protobuf commands from Transit data
 */
class TransitToProtobufBuilder {
    
    fun buildCommand(payload: Map<*, *>): Message {
        val action = payload["action"] as? String
            ?: throw IllegalArgumentException("Missing 'action' field")
        
        val params = payload["params"] as? Map<*, *> ?: emptyMap<String, Any>()
        
        return when (action) {
            // System commands
            "set-localization" -> buildSetLocalization(params)
            "set-recording" -> buildSetRecording(params)
            
            // GPS commands
            "set-gps-manual" -> buildSetGpsManual(params)
            
            // Compass commands
            "set-compass-unit" -> buildSetCompassUnit(params)
            
            // LRF commands
            "lrf-single-measurement" -> buildLrfSingleMeasurement()
            "lrf-continuous-start" -> buildLrfContinuousStart()
            "lrf-continuous-stop" -> buildLrfContinuousStop()
            
            // Rotary commands
            "rotary-goto" -> buildRotaryGoto(params)
            "rotary-stop" -> buildRotaryStop()
            "rotary-set-velocity" -> buildRotarySetVelocity(params)
            
            // Camera commands
            "day-camera-zoom" -> buildDayCameraZoom(params)
            "heat-camera-zoom" -> buildHeatCameraZoom(params)
            "day-camera-focus" -> buildDayCameraFocus(params)
            
            else -> throw IllegalArgumentException("Unknown command action: $action")
        }
    }
    
    private fun buildSetLocalization(params: Map<*, *>): Message {
        val locale = params["locale"] as? String ?: "en"
        return JonSharedCmd.Root.newBuilder()
            .setSystem(
                JonSharedCmdSystem.Cmd.newBuilder()
                    .setSetLocalization(
                        JonSharedCmdSystem.SetLocalization.newBuilder()
                            .setLocalization(locale)
                    )
            )
            .build()
    }
    
    private fun buildSetRecording(params: Map<*, *>): Message {
        val enabled = params["enabled"] as? Boolean ?: false
        return JonSharedCmd.Root.newBuilder()
            .setSystem(
                JonSharedCmdSystem.Cmd.newBuilder()
                    .setSetRecording(
                        JonSharedCmdSystem.SetRecording.newBuilder()
                            .setRecording(enabled)
                    )
            )
            .build()
    }
    
    private fun buildSetGpsManual(params: Map<*, *>): Message {
        val useManual = params["use-manual"] as? Boolean ?: false
        val latitude = (params["latitude"] as? Number)?.toDouble() ?: 0.0
        val longitude = (params["longitude"] as? Number)?.toDouble() ?: 0.0
        val altitude = (params["altitude"] as? Number)?.toDouble() ?: 0.0
        
        return JonSharedCmd.Root.newBuilder()
            .setGps(
                JonSharedCmdGps.Cmd.newBuilder()
                    .setSetManual(
                        JonSharedCmdGps.SetManual.newBuilder()
                            .setUseManual(useManual)
                            .setLatitude(latitude)
                            .setLongitude(longitude)
                            .setAltitude(altitude)
                    )
            )
            .build()
    }
    
    private fun buildSetCompassUnit(params: Map<*, *>): Message {
        val unit = params["unit"] as? String ?: "degrees"
        val useDegrees = unit == "degrees"
        
        return JonSharedCmd.Root.newBuilder()
            .setCompass(
                JonSharedCmdCompass.Cmd.newBuilder()
                    .setSetUnit(
                        JonSharedCmdCompass.SetUnit.newBuilder()
                            .setUnitDegrees(useDegrees)
                    )
            )
            .build()
    }
    
    private fun buildLrfSingleMeasurement(): Message {
        return JonSharedCmd.Root.newBuilder()
            .setLrf(
                JonSharedCmdLrf.Cmd.newBuilder()
                    .setSingleMeasurement(
                        JonSharedCmdLrf.SingleMeasurement.newBuilder()
                    )
            )
            .build()
    }
    
    private fun buildLrfContinuousStart(): Message {
        return JonSharedCmd.Root.newBuilder()
            .setLrf(
                JonSharedCmdLrf.Cmd.newBuilder()
                    .setContinuousStart(
                        JonSharedCmdLrf.ContinuousStart.newBuilder()
                    )
            )
            .build()
    }
    
    private fun buildLrfContinuousStop(): Message {
        return JonSharedCmd.Root.newBuilder()
            .setLrf(
                JonSharedCmdLrf.Cmd.newBuilder()
                    .setContinuousStop(
                        JonSharedCmdLrf.ContinuousStop.newBuilder()
                    )
            )
            .build()
    }
    
    private fun buildRotaryGoto(params: Map<*, *>): Message {
        val azimuth = (params["azimuth"] as? Number)?.toDouble() ?: 0.0
        val elevation = (params["elevation"] as? Number)?.toDouble() ?: 0.0
        
        return JonSharedCmd.Root.newBuilder()
            .setRotary(
                JonSharedCmdRotary.Cmd.newBuilder()
                    .setGoto(
                        JonSharedCmdRotary.Goto.newBuilder()
                            .setAzimuth(azimuth)
                            .setElevation(elevation)
                    )
            )
            .build()
    }
    
    private fun buildRotaryStop(): Message {
        return JonSharedCmd.Root.newBuilder()
            .setRotary(
                JonSharedCmdRotary.Cmd.newBuilder()
                    .setStop(
                        JonSharedCmdRotary.Stop.newBuilder()
                    )
            )
            .build()
    }
    
    private fun buildRotarySetVelocity(params: Map<*, *>): Message {
        val azimuthVel = (params["azimuth-velocity"] as? Number)?.toDouble() ?: 0.0
        val elevationVel = (params["elevation-velocity"] as? Number)?.toDouble() ?: 0.0
        
        return JonSharedCmd.Root.newBuilder()
            .setRotary(
                JonSharedCmdRotary.Cmd.newBuilder()
                    .setVelocity(
                        JonSharedCmdRotary.Velocity.newBuilder()
                            .setAzimuthVelocity(azimuthVel)
                            .setElevationVelocity(elevationVel)
                    )
            )
            .build()
    }
    
    private fun buildDayCameraZoom(params: Map<*, *>): Message {
        val zoom = (params["zoom"] as? Number)?.toDouble() ?: 1.0
        
        return JonSharedCmd.Root.newBuilder()
            .setDayCamera(
                JonSharedCmdDayCamera.Cmd.newBuilder()
                    .setSetZoom(
                        JonSharedCmdDayCamera.SetZoom.newBuilder()
                            .setZoom(zoom)
                    )
            )
            .build()
    }
    
    private fun buildHeatCameraZoom(params: Map<*, *>): Message {
        val zoom = (params["zoom"] as? Number)?.toDouble() ?: 1.0
        
        return JonSharedCmd.Root.newBuilder()
            .setHeatCamera(
                JonSharedCmdHeatCamera.Cmd.newBuilder()
                    .setSetZoom(
                        JonSharedCmdHeatCamera.SetZoom.newBuilder()
                            .setZoom(zoom)
                    )
            )
            .build()
    }
    
    private fun buildDayCameraFocus(params: Map<*, *>): Message {
        val mode = params["mode"] as? String ?: "auto"
        val distance = (params["distance"] as? Number)?.toDouble()
        
        val builder = JonSharedCmdDayCamera.SetFocus.newBuilder()
        
        when (mode) {
            "auto" -> builder.setAuto(true)
            "manual" -> {
                builder.setManual(true)
                if (distance != null) {
                    builder.setDistance(distance)
                }
            }
            "infinity" -> builder.setInfinity(true)
        }
        
        return JonSharedCmd.Root.newBuilder()
            .setDayCamera(
                JonSharedCmdDayCamera.Cmd.newBuilder()
                    .setSetFocus(builder)
            )
            .build()
    }
}

/**
 * WebSocket client for sending commands
 */
class CommandWebSocketClient(
    private val url: String
) {
    private var wsClient: WebSocket? = null
    private val httpClient = HttpClient.newHttpClient()
    private val connected = AtomicBoolean(false)
    
    suspend fun connect() = coroutineScope {
        try {
            val listener = CommandWebSocketListener()
            
            wsClient = httpClient.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(URI.create(url), listener)
                .await()
            
            connected.set(true)
            
        } catch (e: Exception) {
            System.err.println("Failed to connect WebSocket: ${e.message}")
            throw e
        }
    }
    
    fun sendCommand(data: ByteArray) {
        val ws = wsClient
        if (ws != null && connected.get()) {
            ws.sendBinary(ByteBuffer.wrap(data), true)
        } else {
            throw IllegalStateException("WebSocket not connected")
        }
    }
    
    fun isConnected(): Boolean = connected.get()
    
    fun close() {
        connected.set(false)
        wsClient?.sendClose(WebSocket.NORMAL_CLOSURE, "Shutdown")
    }
}

/**
 * WebSocket listener for command connection
 */
class CommandWebSocketListener : WebSocket.Listener {
    
    override fun onOpen(webSocket: WebSocket) {
        System.err.println("Command WebSocket connected")
        webSocket.request(1)
    }
    
    override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String): CompletionStage<*>? {
        System.err.println("Command WebSocket closed: $statusCode $reason")
        return null
    }
    
    override fun onError(webSocket: WebSocket, error: Throwable) {
        System.err.println("Command WebSocket error: ${error.message}")
    }
}

/**
 * Main entry point for command subprocess
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("Usage: CommandSubprocess <websocket-url>")
        System.exit(1)
    }
    
    val wsUrl = args[0]
    val transitComm = TransitCommunicator()
    val subprocess = CommandSubprocess(wsUrl, transitComm)
    
    runBlocking {
        subprocess.run()
    }
}