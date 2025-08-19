package potatoclient.kotlin.ipc

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import potatoclient.java.ipc.UnixSocketCommunicator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import com.cognitect.transit.TransitFactory

/**
 * Tests for IpcManager.
 */
class IpcManagerTest {
    private lateinit var socketPath: Path
    private var manager: IpcManager? = null
    private var serverSocket: UnixSocketCommunicator? = null
    
    @Before
    fun setUp() {
        // Reset singleton
        IpcManager.reset()
        
        // Create unique socket path
        socketPath = Files.createTempDirectory("test-ipc-manager").resolve("test.sock")
    }
    
    @After
    fun tearDown() {
        manager?.shutdown()
        serverSocket?.stop()
        
        IpcManager.reset()
        TransitSocketCommunicator.reset()
        
        try {
            Files.deleteIfExists(socketPath)
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    @Test
    fun testManagerInitialization() {
        // Create manager with specific path
        manager = IpcManager.createWithPath("test-stream", socketPath)
        
        // Initially not running
        assertFalse(manager!!.isRunning())
        
        // Set up server socket
        setupServerSocket()
        
        // Initialize manager
        manager!!.initialize()
        
        // Should be running
        assertTrue(manager!!.isRunning())
        
        // Should send connection event
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.CONNECTION, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.CONNECTED, message[IpcKeys.ACTION])
    }
    
    @Test
    fun testSendRotaryGotoNdc() {
        setupManagerWithServer()
        
        // Send rotary goto NDC command with Keyword channel
        manager!!.sendRotaryGotoNdc(IpcKeys.HEAT, 0.5, -0.5)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.COMMAND, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ROTARY_GOTO_NDC, message[IpcKeys.ACTION])
        assertEquals(IpcKeys.HEAT, message[IpcKeys.CHANNEL])
        assertEquals(0.5, message[IpcKeys.NDC_X])
        assertEquals(-0.5, message[IpcKeys.NDC_Y])
    }
    
    @Test
    fun testSendRotaryGotoNdcWithString() {
        setupManagerWithServer()
        
        // Send rotary goto NDC command with string channel
        manager!!.sendRotaryGotoNdc("day", 0.25, 0.75)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.COMMAND, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ROTARY_GOTO_NDC, message[IpcKeys.ACTION])
        assertEquals(IpcKeys.DAY, message[IpcKeys.CHANNEL])
        assertEquals(0.25, message[IpcKeys.NDC_X])
        assertEquals(0.75, message[IpcKeys.NDC_Y])
    }
    
    @Test
    fun testSendWindowEvent() {
        setupManagerWithServer()
        
        // Send window resize event
        manager!!.sendWindowEvent("resize", width = 1920, height = 1080)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.WINDOW, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.keyword("resize"), message[IpcKeys.ACTION])
        assertEquals(1920, message[IpcKeys.WIDTH])
        assertEquals(1080, message[IpcKeys.HEIGHT])
    }
    
    
    @Test
    fun testSendLogMessage() {
        setupManagerWithServer()
        
        // Send log message
        manager!!.sendLog("ERROR", "Test error", mapOf("code" to 500))
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.LOG, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ERROR, message[IpcKeys.LEVEL])
        assertEquals("Test error", message[IpcKeys.MESSAGE])
        assertEquals("test-stream", message[IpcKeys.PROCESS])
        
        val data = message[IpcKeys.DATA] as Map<*, *>
        assertEquals(500, data["code"])
    }
    
    @Test
    fun testSendCvStartTrackNdc() {
        setupManagerWithServer()
        
        // Send CV start track NDC command
        val frameTime = System.currentTimeMillis()
        manager!!.sendCvStartTrackNdc(IpcKeys.DAY, -0.3, 0.4, frameTime)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.COMMAND, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.CV_START_TRACK_NDC, message[IpcKeys.ACTION])
        assertEquals(IpcKeys.DAY, message[IpcKeys.CHANNEL])
        assertEquals(-0.3, message[IpcKeys.NDC_X])
        assertEquals(0.4, message[IpcKeys.NDC_Y])
        assertEquals(frameTime, message[IpcKeys.FRAME_TIME])
    }
    
    @Test
    fun testSendRotarySetVelocity() {
        setupManagerWithServer()
        
        // Send rotary set velocity command
        manager!!.sendRotarySetVelocity(
            0.5, IpcKeys.CLOCKWISE,
            0.3, IpcKeys.COUNTER_CLOCKWISE
        )
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.COMMAND, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ROTARY_SET_VELOCITY, message[IpcKeys.ACTION])
        assertEquals(0.5, message[IpcKeys.AZIMUTH_SPEED])
        assertEquals(IpcKeys.CLOCKWISE, message[IpcKeys.AZIMUTH_DIRECTION])
        assertEquals(0.3, message[IpcKeys.ELEVATION_SPEED])
        assertEquals(IpcKeys.COUNTER_CLOCKWISE, message[IpcKeys.ELEVATION_DIRECTION])
    }
    
    @Test
    fun testSendRotarySetVelocityWithStrings() {
        setupManagerWithServer()
        
        // Send rotary set velocity command with string directions
        manager!!.sendRotarySetVelocity(
            0.8, "clockwise",
            0.2, "counter-clockwise"
        )
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.COMMAND, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ROTARY_SET_VELOCITY, message[IpcKeys.ACTION])
        assertEquals(0.8, message[IpcKeys.AZIMUTH_SPEED])
        assertEquals(IpcKeys.CLOCKWISE, message[IpcKeys.AZIMUTH_DIRECTION])
        assertEquals(0.2, message[IpcKeys.ELEVATION_SPEED])
        assertEquals(IpcKeys.COUNTER_CLOCKWISE, message[IpcKeys.ELEVATION_DIRECTION])
    }
    
    @Test
    fun testSendRotaryHalt() {
        setupManagerWithServer()
        
        // Send rotary halt command
        manager!!.sendRotaryHalt()
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.COMMAND, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ROTARY_HALT, message[IpcKeys.ACTION])
    }
    
    @Test
    fun testSendCommand() {
        setupManagerWithServer()
        
        // Send generic command
        manager!!.sendCommand("some-action", mapOf(IpcKeys.STREAM_TYPE to IpcKeys.HEAT))
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.COMMAND, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.keyword("some-action"), message[IpcKeys.ACTION])
        assertEquals(IpcKeys.HEAT, message[IpcKeys.STREAM_TYPE])
    }
    
    @Test
    fun testMessageHandler() {
        setupManagerWithServer()
        
        val receivedMessage = AtomicReference<Map<*, *>>()
        val latch = CountDownLatch(1)
        
        // Register handler
        manager!!.onMessage { message ->
            receivedMessage.set(message)
            latch.countDown()
        }
        
        // Send message from server to manager
        val testMessage = mapOf(
            IpcKeys.MSG_TYPE to IpcKeys.COMMAND,
            IpcKeys.ACTION to IpcKeys.ROTARY_GOTO_NDC,
            IpcKeys.NDC_X to 0.5,
            IpcKeys.NDC_Y to -0.5
        )
        
        sendMessageToManager(testMessage)
        
        // Wait for handler to receive
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        
        val received = receivedMessage.get()
        assertNotNull(received)
        assertEquals(IpcKeys.COMMAND, received[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ROTARY_GOTO_NDC, received[IpcKeys.ACTION])
        assertEquals(0.5, received[IpcKeys.NDC_X])
        assertEquals(-0.5, received[IpcKeys.NDC_Y])
    }
    
    @Test
    fun testMultipleHandlers() {
        setupManagerWithServer()
        
        val handler1Messages = mutableListOf<Map<*, *>>()
        val handler2Messages = mutableListOf<Map<*, *>>()
        val latch = CountDownLatch(2)
        
        // Register multiple handlers
        manager!!.onMessage { message ->
            handler1Messages.add(message)
            latch.countDown()
        }
        
        manager!!.onMessage { message ->
            handler2Messages.add(message)
            latch.countDown()
        }
        
        // Send message
        val testMessage = mapOf(IpcKeys.MSG_TYPE to IpcKeys.EVENT)
        sendMessageToManager(testMessage)
        
        // Both handlers should receive
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertEquals(1, handler1Messages.size)
        assertEquals(1, handler2Messages.size)
    }
    
    @Test
    fun testSendMessageSync() {
        setupManagerWithServer()
        
        // Send message synchronously
        val message = mapOf<Any, Any>(
            IpcKeys.MSG_TYPE to IpcKeys.LOG,
            IpcKeys.MESSAGE to "Sync message"
        )
        
        manager!!.sendMessageSync(message)
        
        val received = receiveMessageFromManager()
        assertNotNull(received)
        assertEquals(IpcKeys.LOG, received!![IpcKeys.MSG_TYPE])
        assertEquals("Sync message", received[IpcKeys.MESSAGE])
    }
    
    @Test
    fun testShutdown() {
        setupManagerWithServer()
        
        assertTrue(manager!!.isRunning())
        
        // Shutdown should send disconnection event
        manager!!.shutdown()
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.CONNECTION, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.DISCONNECTED, message[IpcKeys.ACTION])
        
        assertFalse(manager!!.isRunning())
    }
    
    @Test
    fun testDoubleInitialize() {
        setupManagerWithServer()
        
        // First initialize
        manager!!.initialize()
        assertTrue(manager!!.isRunning())
        
        // Clear connection message
        receiveMessageFromManager()
        
        // Second initialize should be no-op
        manager!!.initialize()
        assertTrue(manager!!.isRunning())
        
        // Should not send another connection event
        Thread.sleep(100)
        assertNull(receiveMessageFromManagerNonBlocking())
    }
    
    /**
     * Set up manager with server socket.
     */
    private fun setupManagerWithServer() {
        manager = IpcManager.createWithPath("test-stream", socketPath)
        setupServerSocket()
        manager!!.initialize()
        
        // Clear the initial connection event
        receiveMessageFromManager()
    }
    
    /**
     * Set up server socket for testing.
     */
    private fun setupServerSocket() {
        serverSocket = UnixSocketCommunicator(socketPath, true)
        
        CompletableFuture.runAsync {
            serverSocket!!.start()
        }
        
        Thread.sleep(100)
    }
    
    /**
     * Receive a Transit message from the manager.
     */
    private fun receiveMessageFromManager(): Map<*, *>? {
        return try {
            val messageBytes = serverSocket!!.receive()
            if (messageBytes != null) {
                deserializeTransitMessage(messageBytes)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Receive a message non-blocking.
     */
    private fun receiveMessageFromManagerNonBlocking(): Map<*, *>? {
        val messageBytes = serverSocket!!.tryReceive()
        return if (messageBytes != null) {
            deserializeTransitMessage(messageBytes)
        } else null
    }
    
    /**
     * Send a Transit message to the manager.
     */
    private fun sendMessageToManager(message: Map<*, *>) {
        val messageBytes = serializeTransitMessage(message)
        serverSocket!!.send(messageBytes)
    }
    
    /**
     * Serialize a message to Transit bytes.
     */
    private fun serializeTransitMessage(message: Map<*, *>): ByteArray {
        val baos = ByteArrayOutputStream()
        val writer = TransitFactory.writer<Any>(
            TransitFactory.Format.MSGPACK,
            baos,
            null as Map<Class<*>, com.cognitect.transit.WriteHandler<*, *>>?
        )
        writer.write(message)
        return baos.toByteArray()
    }
    
    /**
     * Deserialize Transit bytes to a message.
     */
    private fun deserializeTransitMessage(bytes: ByteArray): Map<*, *> {
        val bais = ByteArrayInputStream(bytes)
        val readHandlers = mapOf(
            "kw" to com.cognitect.transit.ReadHandler<Any, Any> { rep ->
                TransitFactory.keyword(rep.toString())
            }
        )
        val reader = TransitFactory.reader(
            TransitFactory.Format.MSGPACK,
            bais,
            TransitFactory.readHandlerMap(readHandlers)
        )
        return reader.read() as Map<*, *>
    }
}