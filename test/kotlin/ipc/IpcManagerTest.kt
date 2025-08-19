package potatoclient.kotlin.ipc

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import potatoclient.java.ipc.UnixSocketCommunicator
import potatoclient.kotlin.gestures.GestureEvent
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
 * Tests for IpcManager focusing on gesture events.
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
    fun testSendTapGesture() {
        setupManagerWithServer()
        
        // Send tap gesture
        val frameTimestamp = System.currentTimeMillis()
        val tap = GestureEvent.Tap(100, 200, System.currentTimeMillis(), frameTimestamp)
        manager!!.sendGestureEvent(tap)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.TAP, message[IpcKeys.GESTURE_TYPE])
        assertEquals(100, message[IpcKeys.X])
        assertEquals(200, message[IpcKeys.Y])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testSendDoubleTapGesture() {
        setupManagerWithServer()
        
        // Send double tap gesture
        val frameTimestamp = System.currentTimeMillis()
        val doubleTap = GestureEvent.DoubleTap(150, 250, System.currentTimeMillis(), frameTimestamp)
        manager!!.sendGestureEvent(doubleTap)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.DOUBLE_TAP, message[IpcKeys.GESTURE_TYPE])
        assertEquals(150, message[IpcKeys.X])
        assertEquals(250, message[IpcKeys.Y])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testSendPanStartGesture() {
        setupManagerWithServer()
        
        // Send pan start gesture
        val frameTimestamp = System.currentTimeMillis()
        val panStart = GestureEvent.PanStart(300, 400, System.currentTimeMillis(), frameTimestamp)
        manager!!.sendGestureEvent(panStart)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.PAN_START, message[IpcKeys.GESTURE_TYPE])
        assertEquals(300, message[IpcKeys.X])
        assertEquals(400, message[IpcKeys.Y])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testSendPanMoveGesture() {
        setupManagerWithServer()
        
        // Send pan move gesture
        val frameTimestamp = System.currentTimeMillis()
        val panMove = GestureEvent.PanMove(320, 420, 20, 20, System.currentTimeMillis(), frameTimestamp)
        manager!!.sendGestureEvent(panMove)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.PAN_MOVE, message[IpcKeys.GESTURE_TYPE])
        assertEquals(320, message[IpcKeys.X])
        assertEquals(420, message[IpcKeys.Y])
        assertEquals(20, message[IpcKeys.DELTA_X])
        assertEquals(20, message[IpcKeys.DELTA_Y])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testSendPanStopGesture() {
        setupManagerWithServer()
        
        // Send pan stop gesture
        val frameTimestamp = System.currentTimeMillis()
        val panStop = GestureEvent.PanStop(350, 450, System.currentTimeMillis(), frameTimestamp)
        manager!!.sendGestureEvent(panStop)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.PAN_STOP, message[IpcKeys.GESTURE_TYPE])
        assertEquals(350, message[IpcKeys.X])
        assertEquals(450, message[IpcKeys.Y])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testSendWheelUpGesture() {
        setupManagerWithServer()
        
        // Send wheel up gesture
        val frameTimestamp = System.currentTimeMillis()
        val wheelUp = GestureEvent.WheelUp(200, 300, 3, System.currentTimeMillis(), frameTimestamp)
        manager!!.sendGestureEvent(wheelUp)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.WHEEL_UP, message[IpcKeys.GESTURE_TYPE])
        assertEquals(200, message[IpcKeys.X])
        assertEquals(300, message[IpcKeys.Y])
        assertEquals(3, message["scroll-amount"])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testSendWheelDownGesture() {
        setupManagerWithServer()
        
        // Send wheel down gesture
        val frameTimestamp = System.currentTimeMillis()
        val wheelDown = GestureEvent.WheelDown(200, 300, 3, System.currentTimeMillis(), frameTimestamp)
        manager!!.sendGestureEvent(wheelDown)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.WHEEL_DOWN, message[IpcKeys.GESTURE_TYPE])
        assertEquals(200, message[IpcKeys.X])
        assertEquals(300, message[IpcKeys.Y])
        assertEquals(3, message["scroll-amount"])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testSendGestureEventWithKeyword() {
        setupManagerWithServer()
        
        // Send gesture event using keyword directly
        val frameTimestamp = System.currentTimeMillis()
        manager!!.sendGestureEvent(
            IpcKeys.TAP,
            x = 100,
            y = 200,
            frameTimestamp = frameTimestamp
        )
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.TAP, message[IpcKeys.GESTURE_TYPE])
        assertEquals(100, message[IpcKeys.X])
        assertEquals(200, message[IpcKeys.Y])
        assertEquals(frameTimestamp, message[IpcKeys.FRAME_TIMESTAMP])
    }
    
    @Test
    fun testSendWindowEvent() {
        setupManagerWithServer()
        
        // Send window resize event
        manager!!.sendWindowEvent("resize", width = 1920, height = 1080, deltaX = 100, deltaY = 50)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.WINDOW, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.keyword("resize"), message[IpcKeys.ACTION])
        assertEquals(1920, message[IpcKeys.WIDTH])
        assertEquals(1080, message[IpcKeys.HEIGHT])
        assertEquals(100, message[IpcKeys.DELTA_X])
        assertEquals(50, message[IpcKeys.DELTA_Y])
    }
    
    @Test
    fun testSendWindowMoveEvent() {
        setupManagerWithServer()
        
        // Send window move event
        manager!!.sendWindowEvent("window-move", x = 100, y = 200, deltaX = 10, deltaY = 20)
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.WINDOW, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.WINDOW_MOVE, message[IpcKeys.ACTION])
        assertEquals(100, message[IpcKeys.X])
        assertEquals(200, message[IpcKeys.Y])
        assertEquals(10, message[IpcKeys.DELTA_X])
        assertEquals(20, message[IpcKeys.DELTA_Y])
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
    fun testSendConnectionEvent() {
        setupManagerWithServer()
        
        // Send custom connection event
        manager!!.sendConnectionEvent(IpcKeys.RECONNECTING, mapOf("attempt" to 3))
        
        val message = receiveMessageFromManager()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.CONNECTION, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.RECONNECTING, message[IpcKeys.ACTION])
        
        val details = message[IpcKeys.DETAILS] as Map<*, *>
        assertEquals(3, details["attempt"])
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
            IpcKeys.MSG_TYPE to IpcKeys.EVENT,
            IpcKeys.TYPE to IpcKeys.GESTURE,
            IpcKeys.GESTURE_TYPE to IpcKeys.TAP,
            IpcKeys.X to 100,
            IpcKeys.Y to 200
        )
        
        sendMessageToManager(testMessage)
        
        // Wait for handler to receive
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        
        val received = receivedMessage.get()
        assertNotNull(received)
        assertEquals(IpcKeys.EVENT, received[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, received[IpcKeys.TYPE])
        assertEquals(IpcKeys.TAP, received[IpcKeys.GESTURE_TYPE])
        assertEquals(100, received[IpcKeys.X])
        assertEquals(200, received[IpcKeys.Y])
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