package potatoclient.transit

import cmd.*
import ser.*
import ser.JonSharedData.JonGUIState
import build.buf.protovalidate.Validator
import build.buf.protovalidate.ValidationResult
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.CompletionStage
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min

/**
 * State subprocess that receives protobuf state updates from WebSocket
 * and forwards them to Clojure via Transit after debouncing and rate limiting
 */
class StateSubprocess(
    private val wsUrl: String,
    private val transitComm: TransitCommunicator
) {
    private val wsClient = ReconnectingWebSocketClient(wsUrl)
    private val stateConverter = ProtobufToTransitConverter()
    private val validator: Validator = Validator.newValidator()
    private val isReleaseBuild = System.getProperty("potatoclient.release") != null ||
                                 System.getenv("POTATOCLIENT_RELEASE") != null
    
    // Rate limiting configuration
    @Volatile
    private var maxRateHz = 30
    private val rateLimiter = AtomicReference(RateLimiter(maxRateHz))
    
    // Debouncing with proto equals
    private var lastSentProto: AtomicReference<JonGUIState?> = AtomicReference(null)
    private var lastSentHash: AtomicInteger = AtomicInteger(0)
    
    // Metrics
    private val totalReceived = AtomicInteger(0)
    private val totalSent = AtomicInteger(0)
    private val totalDropped = AtomicInteger(0)
    
    suspend fun run() = coroutineScope {
        // Start WebSocket connection
        launch {
            wsClient.connect { protoBytes ->
                handleStateUpdate(protoBytes)
            }
        }
        
        // Handle control messages from Clojure
        launch {
            while (isActive) {
                val msg = transitComm.readMessage()
                if (msg != null && msg["msg-type"] == "control") {
                    handleControl(msg)
                }
            }
        }
    }
    
    private suspend fun handleStateUpdate(protoBytes: ByteArray) {
        totalReceived.incrementAndGet()
        
        try {
            val stateProto = JonGUIState.parseFrom(protoBytes)
            
            // Check if we should send this update (debouncing + rate limiting)
            if (!shouldSendUpdate(stateProto)) {
                totalDropped.incrementAndGet()
                return
            }
            
            // Validate if not in release mode
            if (!isReleaseBuild) {
                validateAndReport(stateProto)
            }
            
            // Convert to Transit format
            val transitMap = stateConverter.convert(stateProto)
            
            // Send to Clojure
            transitComm.sendMessage(
                transitComm.createMessage("state", transitMap)
            )
            
            // Update last sent for debouncing
            lastSentProto.set(stateProto)
            lastSentHash.set(stateProto.hashCode())
            totalSent.incrementAndGet()
            
        } catch (e: Exception) {
            System.err.println("Error processing state update: ${e.message}")
        }
    }
    
    private fun shouldSendUpdate(newProto: JonGUIState): Boolean {
        val lastProto = lastSentProto.get()
        
        // First update always goes through
        if (lastProto == null) return true
        
        // Use protobuf's built-in equals() for deep comparison
        if (newProto == lastProto) return false
        
        // Additional hash check for performance
        val newHash = newProto.hashCode()
        if (newHash == lastSentHash.get()) {
            // Hash collision, do full equals check
            if (newProto.equals(lastProto)) return false
        }
        
        // Apply rate limiting
        return rateLimiter.get().tryAcquire()
    }
    
    private suspend fun validateAndReport(proto: JonGUIState) {
        try {
            val result = validator.validate(proto)
            if (!result.isSuccess) {
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
                            "subsystem" to "state",
                            "errors" to errors
                        )
                    )
                )
            }
        } catch (e: Exception) {
            System.err.println("Validation framework error: ${e.message}")
        }
    }
    
    private suspend fun handleControl(msg: Map<*, *>) {
        val payload = msg["payload"] as? Map<*, *> ?: return
        
        when (payload["action"]) {
            "set-rate-limit" -> {
                val newRate = (payload["max-hz"] as? Number)?.toInt() ?: return
                if (newRate in 1..120) {
                    maxRateHz = newRate
                    rateLimiter.get().shutdown()
                    rateLimiter.set(RateLimiter(newRate))
                }
            }
            "get-logs" -> {
                val lines = (payload["lines"] as? Number)?.toInt() ?: 100
                sendLogs(msg["msg-id"] as String, lines)
            }
            "get-metrics" -> {
                sendMetrics(msg["msg-id"] as String)
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
            "total-dropped" to totalDropped.get(),
            "drop-rate" to (totalDropped.get().toDouble() / totalReceived.get()),
            "max-rate-hz" to maxRateHz,
            "current-rate" to rateLimiter.get().getCurrentRate()
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
        val logs = listOf("State subprocess running", "WebSocket connected to $wsUrl")
        
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
        rateLimiter.get().shutdown()
        transitComm.close()
    }
}

/**
 * Rate limiter with configurable rate
 */
class RateLimiter(private val rateHz: Int) {
    private val semaphore = Semaphore(rateHz)
    private val refillJob: Job
    private val startTime = System.currentTimeMillis()
    private val acquired = AtomicInteger(0)
    
    init {
        refillJob = GlobalScope.launch {
            while (isActive) {
                delay(1000L / rateHz)
                if (semaphore.availablePermits() < rateHz) {
                    semaphore.release()
                }
            }
        }
    }
    
    fun tryAcquire(): Boolean {
        val result = semaphore.tryAcquire()
        if (result) acquired.incrementAndGet()
        return result
    }
    
    fun getCurrentRate(): Double {
        val elapsed = System.currentTimeMillis() - startTime
        return if (elapsed > 0) {
            (acquired.get() * 1000.0) / elapsed
        } else {
            0.0
        }
    }
    
    fun shutdown() {
        refillJob.cancel()
    }
}

/**
 * Converts protobuf messages to Transit-friendly maps
 */
class ProtobufToTransitConverter {
    private val jsonPrinter = JsonFormat.printer()
        .omittingInsignificantWhitespace()
    
    fun convert(proto: JonGUIState): Map<String, Any> {
        // Convert protobuf to JSON then to Map
        // In real implementation, would do direct field mapping
        val json = jsonPrinter.print(proto)
        
        // For now, return a simplified structure
        return mapOf(
            "system" to convertSystem(proto.system),
            "gps" to convertGps(proto.gps),
            "compass" to convertCompass(proto.compass),
            "lrf" to convertLrf(proto.lrf),
            "rotary" to convertRotary(proto.rotary)
            // Add other subsystems as needed
        )
    }
    
    private fun convertSystem(system: JonSharedDataSystem.JonGuiDataSystem?): Map<String, Any> {
        return if (system != null) {
            mapOf(
                "battery-level" to system.batteryLevel,
                "localization" to system.localization,
                "recording" to system.recording
            )
        } else {
            emptyMap()
        }
    }
    
    private fun convertGps(gps: JonSharedDataGps.JonGuiDataGps?): Map<String, Any> {
        return if (gps != null) {
            mapOf(
                "latitude" to gps.latitude,
                "longitude" to gps.longitude,
                "altitude" to gps.altitude,
                "fix-type" to gps.fixType.name,
                "satellites" to gps.satellites,
                "hdop" to gps.hdop,
                "use-manual" to gps.useManual
            )
        } else {
            emptyMap()
        }
    }
    
    private fun convertCompass(compass: JonSharedDataCompass.JonGuiDataCompass?): Map<String, Any> {
        return if (compass != null) {
            mapOf(
                "heading" to compass.heading,
                "pitch" to compass.pitch,
                "roll" to compass.roll,
                "unit" to if (compass.unitDegrees) "degrees" else "mils",
                "calibrated" to compass.calibrated
            )
        } else {
            emptyMap()
        }
    }
    
    private fun convertLrf(lrf: JonSharedDataLrf.JonGuiDataLrf?): Map<String, Any> {
        return if (lrf != null) {
            mapOf(
                "distance" to lrf.distance,
                "scan-mode" to lrf.scanMode.name.lowercase(),
                "target-locked" to lrf.targetLocked
            )
        } else {
            emptyMap()
        }
    }
    
    private fun convertRotary(rotary: JonSharedDataRotary.JonGuiDataRotary?): Map<String, Any> {
        return if (rotary != null) {
            mapOf(
                "azimuth" to rotary.azimuth,
                "elevation" to rotary.elevation,
                "azimuth-velocity" to rotary.azimuthVelocity,
                "elevation-velocity" to rotary.elevationVelocity,
                "moving" to rotary.moving,
                "mode" to rotary.mode.name.lowercase()
            )
        } else {
            emptyMap()
        }
    }
}

/**
 * WebSocket client with reconnection support
 */
class ReconnectingWebSocketClient(
    private val url: String,
    private val reconnectDelayMs: Long = 1000,
    private val maxReconnectDelayMs: Long = 30000
) {
    private var currentDelay = reconnectDelayMs
    private var wsClient: WebSocket? = null
    private val httpClient = HttpClient.newHttpClient()
    private val isRunning = AtomicBoolean(true)
    
    suspend fun connect(onMessage: suspend (ByteArray) -> Unit) = coroutineScope {
        while (isRunning.get() && isActive) {
            try {
                val listener = StateWebSocketListener(onMessage)
                
                wsClient = httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(url), listener)
                    .await()
                
                // Reset delay on successful connection
                currentDelay = reconnectDelayMs
                
                // Wait for disconnection
                listener.awaitDisconnection()
                
            } catch (e: Exception) {
                System.err.println("WebSocket connection failed: ${e.message}")
                
                // Exponential backoff
                delay(currentDelay)
                currentDelay = min(currentDelay * 2, maxReconnectDelayMs)
            }
        }
    }
    
    fun close() {
        isRunning.set(false)
        wsClient?.sendClose(WebSocket.NORMAL_CLOSURE, "Shutdown")
    }
}

/**
 * WebSocket listener for state updates
 */
class StateWebSocketListener(
    private val onMessage: suspend (ByteArray) -> Unit
) : WebSocket.Listener {
    
    private val disconnected = CompletableDeferred<Unit>()
    private val buffer = mutableListOf<ByteArray>()
    
    override fun onOpen(webSocket: WebSocket) {
        System.err.println("WebSocket connected")
        webSocket.request(1)
    }
    
    override fun onBinary(webSocket: WebSocket, data: ByteBuffer, last: Boolean): CompletionStage<*>? {
        val bytes = ByteArray(data.remaining())
        data.get(bytes)
        
        if (last) {
            // Complete message received
            val fullMessage = if (buffer.isEmpty()) {
                bytes
            } else {
                buffer.add(bytes)
                val size = buffer.sumOf { it.size }
                ByteArray(size).also { result ->
                    var offset = 0
                    buffer.forEach { chunk ->
                        System.arraycopy(chunk, 0, result, offset, chunk.size)
                        offset += chunk.size
                    }
                }
            }
            buffer.clear()
            
            // Process message asynchronously
            GlobalScope.launch {
                onMessage(fullMessage)
            }
        } else {
            // Partial message, buffer it
            buffer.add(bytes)
        }
        
        webSocket.request(1)
        return null
    }
    
    override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String): CompletionStage<*>? {
        System.err.println("WebSocket closed: $statusCode $reason")
        disconnected.complete(Unit)
        return null
    }
    
    override fun onError(webSocket: WebSocket, error: Throwable) {
        System.err.println("WebSocket error: ${error.message}")
        disconnected.completeExceptionally(error)
    }
    
    suspend fun awaitDisconnection() {
        disconnected.await()
    }
}

/**
 * Main entry point for state subprocess
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("Usage: StateSubprocess <websocket-url>")
        System.exit(1)
    }
    
    val wsUrl = args[0]
    val transitComm = TransitCommunicator()
    val subprocess = StateSubprocess(wsUrl, transitComm)
    
    runBlocking {
        subprocess.run()
    }
}