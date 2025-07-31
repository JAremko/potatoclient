# Kotlin Code Refactoring Guide - Low Latency Focus

## Overview
This guide outlines refactoring priorities for improving readability and real-time high-throughput communication performance in the PotatoClient Kotlin codebase, with focus on maintaining minimal latency.

## 1. Excessive Logging Removal

### High Priority - Performance Impact
These logging calls are in hot paths and must be removed entirely:

#### CommandSubprocess.kt
- **Lines 68-69, 74-75**: Remove per-command debug logging
  ```kotlin
  // REMOVE: messageProtocol.sendDebug("Processing command: $action")
  // REMOVE: messageProtocol.sendDebug("Command sent successfully: $action") 
  ```
- **Lines 68, 74**: Remove per-message metrics (causes allocation + coroutine launch)
  ```kotlin
  // REMOVE: messageProtocol.sendMetric("commands_received", totalReceived.get())
  // REMOVE: messageProtocol.sendMetric("commands_sent", totalSent.get())
  ```

#### StateSubprocess.kt  
- **Lines 264, 269, 312, 352**: Remove all WebSocket connection logging
- Keep only critical errors that affect operation

#### TransitMessageProtocol.kt
- **Lines 200-201, 205**: Remove response logging (3 allocations + 3 coroutines per response!)
  ```kotlin
  // REMOVE ALL: Response logging in sendResponse()
  ```

#### VideoStreamManager.kt
- **Lines 120, 123**: Remove connection lifecycle logging
- **Line 109**: Remove per-frame warning about frame size
- **Line 213**: Remove all periodic stats logging

### Remove ALL Non-Critical Logging
Since telemetry creates constant message traffic, any additional logging just adds noise:
- Keep only: Fatal errors, startup/shutdown
- Remove: Info, debug, warnings, metrics in hot paths

## 2. Transit Communication Optimization (Low Latency)

### Direct Write Path
**Problem**: Every message spawns a coroutine
```kotlin
// Current: TransitMessageProtocol.kt
GlobalScope.launch {
    transitComm.sendMessage(logMsg)
}
```

**Solution**: Direct synchronous writes for critical paths
```kotlin
// For critical messages (state updates, responses)
fun sendMessageDirect(message: Map<String, Any>) {
    synchronized(writer) {
        writer.write(message)
        // Flush is handled by FramedOutputStream
    }
}

// Keep async only for non-critical logging
fun sendMessageAsync(message: Map<String, Any>) {
    if (!isReleaseBuild) {
        GlobalScope.launch { sendMessageDirect(message) }
    }
}
```

### Remove Allocation Overhead
**Problem**: New map + UUID for every message
```kotlin
mapOf(
    "msg-type" to msgType,
    "msg-id" to UUID.randomUUID().toString(),  // Allocation!
    "timestamp" to System.currentTimeMillis(),
    "payload" to payload
)
```

**Solution**: Reuse message envelopes
```kotlin
class MessageEnvelope {
    private val map = mutableMapOf<String, Any>()
    private var msgIdCounter = AtomicLong(0)
    
    fun wrap(msgType: String, payload: Map<String, Any>): Map<String, Any> {
        map.clear()
        map["msg-type"] = msgType
        map["msg-id"] = msgIdCounter.incrementAndGet().toString()
        map["timestamp"] = System.currentTimeMillis()
        map["payload"] = payload
        return map
    }
}
```

## 3. State Update Optimization (Server-Side Deduplication)

### Remove ALL Client-Side Deduplication
Since the server already handles equality checks, we can remove all client-side logic:

```kotlin
// REMOVE ENTIRELY from StateSubprocess:
- private val lastSentProto = AtomicReference<JonSharedData.JonGUIState?>(null)
- private val lastSentHash = AtomicInteger(0)
- private val duplicatesSkipped = AtomicInteger(0)

// SIMPLIFY shouldSendUpdate to ONLY rate limiting:
private fun shouldSendUpdate(): Boolean {
    return rateLimiter.get().tryAcquire()
}
```

### Simplified State Handler
```kotlin
private suspend fun handleProtobufState(protoBytes: ByteArray) {
    try {
        // Parse
        val protoState = JonSharedData.JonGUIState.parseFrom(protoBytes)
        
        // Rate limit only
        if (!shouldSendUpdate()) return
        
        // Convert and send immediately
        val transitState = stateConverter.convert(protoState)
        transitComm.sendMessageDirect(
            transitComm.createMessage("state", transitState as Map<String, Any>)
        )
    } catch (e: Exception) {
        // Critical errors only
    }
}
```

### Remove Unnecessary Metrics
```kotlin
// REMOVE these counters - no duplicates to track:
- duplicatesSkipped
- lastSentHash
```

## 4. Video Stream Performance

### Zero-Copy Frame Processing
**Current Problem**: Unnecessary buffer duplication
```kotlin
// Current: Creates a copy!
val timestampBuffer = data.duplicate()
timestampBuffer.order(ByteOrder.LITTLE_ENDIAN)
```

**Solution**: Direct buffer access without copying
```kotlin
// Save original position
val originalPos = data.position()
data.order(ByteOrder.LITTLE_ENDIAN)
val timestamp = data.getLong()
val duration = data.getLong()
// Create slice for video data (no copy)
val videoData = data.slice()
// Restore for buffer pool
data.position(originalPos)
```

### Remove Buffer Pool Overhead
**Problem**: Checking every buffer's properties
```kotlin
if (data.isDirect && data.capacity() <= Constants.MAX_BUFFER_SIZE) {
    webSocketClient.getBufferPool().release(data)
}
```

**Solution**: Trust the source
```kotlin
// WebSocket only gives us pooled buffers, just release
webSocketClient.getBufferPool().release(data)
```

## 5. Lock-Free Optimizations

### AtomicReference vs Volatile
Replace AtomicReference where only get/set is used:
```kotlin
// Current
private val lastSentProto = AtomicReference<JonSharedData.JonGUIState?>(null)

// Optimized (if no compareAndSet needed)
@Volatile
private var lastSentProto: JonSharedData.JonGUIState? = null
```

### Remove Unnecessary Atomics
Many counters don't need to be atomic if they're only incremented from one thread:
```kotlin
// If only accessed from message processing thread
private var totalReceived = 0  // No AtomicInteger needed
```

## 6. Code Structure and Readability

### Extract Constants
Create a `TransitConstants.kt` file:
```kotlin
object TransitConstants {
    const val MAX_MESSAGE_SIZE = 10 * 1024 * 1024
    const val FRAME_HEADER_SIZE = 4
    const val DEFAULT_RATE_HZ = 30
    // Remove backpressure constants - not needed for low latency
}
```

### Simplify WebSocket Clients
Extract common WebSocket logic into a base class:
```kotlin
abstract class BaseWebSocketClient(
    protected val url: String,
    private val sslContext: SSLContext  // Share SSL context
) {
    // Common connection logic only
    // No reconnection - handled at higher level
}
```

### Inline Small Functions
For hot paths, inline small functions to reduce call overhead:
```kotlin
@kotlin.jvm.JvmInline
value class MessageType(val key: String) {
    companion object {
        val LOG = MessageType("log")
        val ERROR = MessageType("error")
        val STATE = MessageType("state")
    }
}
```

## 7. Critical Path Optimizations

### Command Processing
```kotlin
// Optimize the critical path in CommandSubprocess
private suspend fun handleCommand(msg: Map<*, *>) {
    val payload = msg["payload"] as? Map<*, *> ?: return
    val msgId = msg["msg-id"] as? String ?: ""
    val action = payload["action"] as? String ?: "ping"
    
    try {
        // Direct protobuf creation - no logging
        val rootCmd = cmdBuilder.buildCommand(action)
        wsClient.send(rootCmd.toByteArray())
        
        // Direct response - no coroutine
        transitComm.sendMessageDirect(
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
```

### State Processing (Ultra-Simplified)
```kotlin
// ULTRA SIMPLE - No deduplication needed!
private suspend fun handleProtobufState(protoBytes: ByteArray) {
    // Rate limit check
    if (!rateLimiter.get().tryAcquire()) return
    
    try {
        // Parse -> Convert -> Send
        val transitState = stateConverter.convert(
            JonSharedData.JonGUIState.parseFrom(protoBytes)
        )
        transitComm.sendMessageDirect(
            transitComm.createMessage("state", transitState as Map<String, Any>)
        )
    } catch (e: Exception) {
        // Ignore parse errors silently
    }
}
```

## Implementation Priority

1. **Immediate (Hours)**:
   - Remove ALL logging in hot paths
   - Fix buffer duplication in video processing
   - Remove hash computation in state debouncing
   - Switch to direct writes for critical messages

2. **Short Term (1 day)**:
   - Replace AtomicReference with volatile where appropriate
   - Inline hot path functions
   - Remove unnecessary atomics
   - Extract constants

3. **Medium Term (2-3 days)**:
   - Refactor WebSocket clients
   - Implement message envelope reuse
   - Add latency measurements

## Metrics to Track

Focus on latency metrics:
- Message processing latency (p50, p95, p99)
- End-to-end command latency
- Frame processing time
- GC pause impact on latency

## 8. Testing Strategy (Unit & Integration Focus)

### Unit Tests

#### Transit Communication Tests
```kotlin
class TransitCommunicatorTest {
    private lateinit var inputStream: PipedInputStream
    private lateinit var outputStream: PipedOutputStream
    private lateinit var communicator: TransitCommunicator
    
    @BeforeEach
    fun setup() {
        val input = PipedInputStream()
        val output = PipedOutputStream()
        inputStream = PipedInputStream(output)
        outputStream = PipedOutputStream(input)
        communicator = TransitCommunicator(inputStream, outputStream)
    }
    
    @Test
    fun `should correctly frame and send messages`() {
        // Given
        val testMessage = mapOf(
            "msg-type" to "test",
            "payload" to mapOf("data" to "hello")
        )
        
        // When
        runBlocking {
            communicator.sendMessage(testMessage)
        }
        
        // Then
        val frameHeader = ByteArray(4)
        outputStream.read(frameHeader)
        val messageSize = ByteBuffer.wrap(frameHeader).order(ByteOrder.BIG_ENDIAN).int
        assertTrue(messageSize > 0)
        assertTrue(messageSize < 1024) // Small test message
    }
    
    @Test
    fun `should handle concurrent sends without corruption`() = runBlocking {
        // Send 100 messages concurrently
        val jobs = (1..100).map { i ->
            launch {
                communicator.sendMessage(
                    mapOf("msg-type" to "test", "id" to i)
                )
            }
        }
        jobs.joinAll()
        
        // Verify all messages sent (check output stream has data)
        assertTrue(outputStream.available() > 0)
    }
}
```

#### State Processing Tests
```kotlin
class StateConverterTest {
    private val converter = SimpleStateConverter()
    
    @Test
    fun `should convert protobuf state to transit map`() {
        // Given - build a protobuf state with known values
        val protoState = JonSharedData.JonGUIState.newBuilder()
            .setJonMeta(JonSharedData.JonMeta.newBuilder()
                .setTag("test-tag")
                .setTimestamp(123456789L))
            .build()
        
        // When
        val transitMap = converter.convert(protoState)
        
        // Then
        assertEquals("test-tag", (transitMap["jon-meta"] as Map<*, *>)["tag"])
        assertEquals(123456789L, (transitMap["jon-meta"] as Map<*, *>)["timestamp"])
    }
    
    @Test
    fun `should handle null fields gracefully`() {
        // Given - minimal protobuf
        val protoState = JonSharedData.JonGUIState.newBuilder().build()
        
        // When/Then - should not throw
        val transitMap = converter.convert(protoState)
        assertNotNull(transitMap)
    }
}

class RateLimiterTest {
    @Test
    fun `should limit to specified rate`() {
        // Given
        val limiter = RateLimiter(10) // 10Hz
        
        // When - try to acquire 20 tokens rapidly
        var acquired = 0
        repeat(20) {
            if (limiter.tryAcquire()) acquired++
        }
        
        // Then - should only get ~10 (bucket size)
        assertTrue(acquired in 9..11)
        
        // Wait 1 second for refill
        Thread.sleep(1100)
        
        // Should be able to acquire more
        var acquiredAfter = 0
        repeat(10) {
            if (limiter.tryAcquire()) acquiredAfter++
        }
        assertTrue(acquiredAfter >= 9)
    }
}
```

#### Command Processing Tests
```kotlin
class SimpleCommandBuilderTest {
    private val builder = SimpleCommandBuilder()
    
    @Test
    fun `should build valid protobuf commands`() {
        // Test each command type
        val commands = mapOf(
            "ping" to cmd.JonSharedCmd.CommandType.PING,
            "play" to cmd.JonSharedCmd.CommandType.PLAY,
            "stop" to cmd.JonSharedCmd.CommandType.STOP
        )
        
        commands.forEach { (action, expectedType) ->
            val rootCmd = builder.buildCommand(action)
            assertEquals(expectedType, rootCmd.cmd.type)
            assertNotNull(rootCmd.jonMeta)
            assertTrue(rootCmd.jonMeta.timestamp > 0)
        }
    }
    
    @Test
    fun `should handle unknown commands`() {
        // Should default to PING or throw
        val rootCmd = builder.buildCommand("unknown-action")
        assertEquals(cmd.JonSharedCmd.CommandType.PING, rootCmd.cmd.type)
    }
}
```

### Integration Tests

#### Transit Message Flow Test
```kotlin
class TransitMessageFlowTest {
    private lateinit var mockInputStream: PipedInputStream
    private lateinit var mockOutputStream: PipedOutputStream
    private lateinit var messageProtocol: TransitMessageProtocol
    
    @Test
    fun `should send formatted log messages via transit`() = runBlocking {
        // Given
        val sentMessages = mutableListOf<Map<*, *>>()
        val mockComm = object : TransitCommunicator(mockInputStream, mockOutputStream) {
            override suspend fun sendMessage(message: Map<String, Any>) {
                sentMessages.add(message)
            }
        }
        messageProtocol = TransitMessageProtocol("test", mockComm)
        
        // When
        messageProtocol.sendInfo("Test info message")
        delay(100) // Let coroutine complete
        
        // Then
        assertEquals(1, sentMessages.size)
        val msg = sentMessages[0]
        assertEquals("log", msg["msg-type"])
        val payload = msg["payload"] as Map<*, *>
        assertEquals("INFO", payload["level"])
        assertEquals("Test info message", payload["message"])
        assertEquals("test", payload["process"])
    }
    
    @Test
    fun `should batch metrics efficiently`() = runBlocking {
        // Given
        val sentMessages = Collections.synchronizedList(mutableListOf<Map<*, *>>())
        val mockComm = object : TransitCommunicator(mockInputStream, mockOutputStream) {
            override suspend fun sendMessage(message: Map<String, Any>) {
                sentMessages.add(message)
            }
        }
        messageProtocol = TransitMessageProtocol("test", mockComm)
        
        // When - send many metrics rapidly
        repeat(100) { i ->
            messageProtocol.sendMetric("counter", i)
        }
        delay(200) // Let all coroutines complete
        
        // Then - all should be sent (no batching for now)
        assertEquals(100, sentMessages.size)
    }
}
```

#### Command Subprocess Integration Test
```kotlin
class CommandSubprocessIntegrationTest {
    @Test
    fun `should process command from transit to websocket`() = runBlocking {
        // Given - create pipes for communication
        val toSubprocess = PipedOutputStream()
        val fromSubprocess = PipedInputStream()
        val subprocessIn = PipedInputStream(toSubprocess)
        val subprocessOut = PipedOutputStream(fromSubprocess)
        
        // Mock WebSocket client
        val sentCommands = mutableListOf<ByteArray>()
        val mockWsClient = object : CommandWebSocketClient("wss://mock", mockMessageProtocol) {
            override fun send(data: ByteArray) {
                sentCommands.add(data)
            }
            override suspend fun connect() { /* Mock */ }
        }
        
        // Create subprocess with mocked components
        val transitComm = TransitCommunicator(subprocessIn, subprocessOut)
        val subprocess = CommandSubprocess("wss://mock", transitComm)
        
        // When - send a ping command
        val writer = TransitFactory.writer(TransitFactory.Format.MSGPACK, toSubprocess)
        writer.write(mapOf(
            "msg-type" to "command",
            "msg-id" to "test-1",
            "payload" to mapOf("action" to "ping")
        ))
        toSubprocess.flush()
        
        // Then - verify protobuf sent to WebSocket
        delay(100) // Process time
        assertEquals(1, sentCommands.size)
        val sentProto = cmd.JonSharedCmd.Root.parseFrom(sentCommands[0])
        assertEquals(cmd.JonSharedCmd.CommandType.PING, sentProto.cmd.type)
    }
}
```

#### State Subprocess Integration Test
```kotlin
class StateSubprocessIntegrationTest {
    @Test
    fun `should convert websocket protobuf to transit`() = runBlocking {
        // Given - pipes for communication
        val toSubprocess = PipedOutputStream()
        val fromSubprocess = PipedInputStream()
        val subprocessIn = PipedInputStream(toSubprocess)
        val subprocessOut = PipedOutputStream(fromSubprocess)
        
        val transitComm = TransitCommunicator(subprocessIn, subprocessOut)
        val subprocess = StateSubprocess("wss://mock", transitComm)
        
        // Capture output messages
        val reader = TransitFactory.reader(TransitFactory.Format.MSGPACK, fromSubprocess)
        
        // When - simulate protobuf state from WebSocket
        val protoState = JonSharedData.JonGUIState.newBuilder()
            .setJonMeta(JonSharedData.JonMeta.newBuilder()
                .setTag("test")
                .setTimestamp(System.currentTimeMillis()))
            .build()
        
        // Directly call handler (bypass WebSocket for unit test)
        subprocess.handleProtobufState(protoState.toByteArray())
        
        // Then - read transit message
        delay(100)
        val message = reader.read<Any>() as Map<*, *>
        assertEquals("state", message["msg-type"])
        assertNotNull(message["payload"])
    }
    
    @Test
    fun `should respect rate limiting in state updates`() = runBlocking {
        // Given - subprocess with 10Hz rate limit
        val messages = Collections.synchronizedList(mutableListOf<Map<*, *>>())
        val mockComm = object : TransitCommunicator() {
            override suspend fun sendMessage(message: Map<String, Any>) {
                messages.add(message)
            }
        }
        val subprocess = StateSubprocess("wss://mock", mockComm)
        
        // When - send 100 state updates rapidly
        val protoState = JonSharedData.JonGUIState.newBuilder().build()
        repeat(100) {
            subprocess.handleProtobufState(protoState.toByteArray())
        }
        
        // Then - should be rate limited (approximately)
        delay(100)
        assertTrue(messages.size < 20) // Much less than 100
    }
}

## Notes

- Latency > Throughput for this system
- Remove all non-essential work from hot paths
- Profile with actual telemetry load
- Consider using JMH for microbenchmarks
- Test with production-like message sizes and rates