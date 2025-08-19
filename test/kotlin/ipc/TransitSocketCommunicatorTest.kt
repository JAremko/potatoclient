package potatoclient.kotlin.ipc

import com.cognitect.transit.TransitFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import potatoclient.java.ipc.SocketFactory
import potatoclient.java.ipc.UnixSocketCommunicator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Tests for TransitSocketCommunicator.
 */
class TransitSocketCommunicatorTest {
    private lateinit var socketPath: Path
    private var serverComm: TransitSocketCommunicator? = null
    private var clientComm: TransitSocketCommunicator? = null
    
    @Before
    fun setUp() {
        // Create unique socket path for each test
        socketPath = Files.createTempDirectory("test-transit-socket").resolve("test.sock")
    }
    
    @After
    fun tearDown() {
        // Clean up communicators
        serverComm?.stop()
        clientComm?.stop()
        
        // Reset singleton
        TransitSocketCommunicator.reset()
        
        // Clean up socket file
        try {
            Files.deleteIfExists(socketPath)
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    @Test
    fun testBasicTransitCommunication() = runBlocking {
        // Create server and client
        serverComm = TransitSocketCommunicator.createWithPath(socketPath, "test-server", true)
        clientComm = TransitSocketCommunicator.createWithPath(socketPath, "test-client", false)
        
        // Start server in background
        val serverFuture = CompletableFuture.runAsync {
            runBlocking {
                serverComm!!.start()
            }
        }
        
        // Give server time to bind
        delay(100)
        
        // Start client
        clientComm!!.start()
        
        // Wait for connection
        serverFuture.get(2, TimeUnit.SECONDS)
        
        // Send message from client to server
        val message = mapOf(
            IpcKeys.MSG_TYPE to IpcKeys.EVENT,
            IpcKeys.TYPE to IpcKeys.GESTURE,
            IpcKeys.X to 100,
            IpcKeys.Y to 200
        )
        
        clientComm!!.sendMessage(message)
        
        // Receive on server
        val received = serverComm!!.readMessage()
        assertNotNull(received)
        assertEquals(IpcKeys.EVENT, received!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, received[IpcKeys.TYPE])
        assertEquals(100, received[IpcKeys.X])
        assertEquals(200, received[IpcKeys.Y])
    }
    
    @Test
    fun testMessageEnvelope() = runBlocking {
        establishConnection()
        
        // Create message with envelope
        val message = clientComm!!.createMessage(
            IpcKeys.LOG,
            mapOf(
                IpcKeys.LEVEL to IpcKeys.INFO,
                IpcKeys.MESSAGE to "Test log message"
            )
        )
        
        // Verify envelope fields
        assertNotNull(message[IpcKeys.MSG_ID])
        assertNotNull(message[IpcKeys.TIMESTAMP])
        assertEquals(IpcKeys.LOG, message[IpcKeys.MSG_TYPE])
        
        // Send and receive
        clientComm!!.sendMessage(message)
        val received = serverComm!!.readMessage()
        
        assertNotNull(received)
        assertEquals(message[IpcKeys.MSG_ID], received!![IpcKeys.MSG_ID])
        assertEquals(IpcKeys.LOG, received[IpcKeys.MSG_TYPE])
        
        val payload = received[IpcKeys.PAYLOAD] as Map<*, *>
        assertEquals(IpcKeys.INFO, payload[IpcKeys.LEVEL])
        assertEquals("Test log message", payload[IpcKeys.MESSAGE])
    }
    
    @Test
    fun testEventMessage() = runBlocking {
        establishConnection()
        
        val eventMessage = clientComm!!.createEventMessage(
            IpcKeys.WINDOW,
            mapOf(
                IpcKeys.ACTION to IpcKeys.RESIZE,
                IpcKeys.WIDTH to 1920,
                IpcKeys.HEIGHT to 1080
            )
        )
        
        clientComm!!.sendMessage(eventMessage)
        val received = serverComm!!.readMessage()
        
        assertNotNull(received)
        assertEquals(IpcKeys.EVENT, received!![IpcKeys.MSG_TYPE])
        
        val payload = received[IpcKeys.PAYLOAD] as Map<*, *>
        assertEquals(IpcKeys.WINDOW, payload[IpcKeys.TYPE])
        assertEquals(IpcKeys.RESIZE, payload[IpcKeys.ACTION])
        assertEquals(1920, payload[IpcKeys.WIDTH])
        assertEquals(1080, payload[IpcKeys.HEIGHT])
    }
    
    @Test
    fun testLogMessage() = runBlocking {
        establishConnection()
        
        val logMessage = clientComm!!.createLogMessage(
            IpcKeys.ERROR,
            "Test error message",
            mapOf("error_code" to 500)
        )
        
        clientComm!!.sendMessage(logMessage)
        val received = serverComm!!.readMessage()
        
        assertNotNull(received)
        assertEquals(IpcKeys.LOG, received!![IpcKeys.MSG_TYPE])
        
        val payload = received[IpcKeys.PAYLOAD] as Map<*, *>
        assertEquals(IpcKeys.ERROR, payload[IpcKeys.LEVEL])
        assertEquals("Test error message", payload[IpcKeys.MESSAGE])
        assertEquals("test-client", payload[IpcKeys.PROCESS])
        
        val data = payload[IpcKeys.DATA] as Map<*, *>
        assertEquals(500, data["error_code"])
    }
    
    @Test
    fun testMultipleMessagesInSequence() = runBlocking {
        establishConnection()
        
        // Send multiple messages
        for (i in 1..10) {
            val message = mapOf(
                IpcKeys.MSG_TYPE to IpcKeys.METRIC,
                "value" to i,
                "name" to "metric_$i"
            )
            clientComm!!.sendMessage(message)
        }
        
        // Receive all messages
        for (i in 1..10) {
            val received = serverComm!!.readMessage()
            assertNotNull(received)
            assertEquals(IpcKeys.METRIC, received!![IpcKeys.MSG_TYPE])
            assertEquals(i, received["value"])
            assertEquals("metric_$i", received["name"])
        }
    }
    
    @Test
    fun testTransitKeywordHandling() = runBlocking {
        establishConnection()
        
        // Create message with various keyword types
        val message = mapOf(
            IpcKeys.MSG_TYPE to IpcKeys.COMMAND,
            IpcKeys.ACTION to IpcKeys.ROTARY_GOTO_NDC,
            IpcKeys.STREAM_TYPE to IpcKeys.HEAT,
            IpcKeys.AZIMUTH_DIRECTION to IpcKeys.CLOCKWISE
        )
        
        clientComm!!.sendMessage(message)
        val received = serverComm!!.readMessage()
        
        assertNotNull(received)
        
        // Verify all keywords are preserved
        assertEquals(IpcKeys.COMMAND, received!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ROTARY_GOTO_NDC, received[IpcKeys.ACTION])
        assertEquals(IpcKeys.HEAT, received[IpcKeys.STREAM_TYPE])
        assertEquals(IpcKeys.CLOCKWISE, received[IpcKeys.AZIMUTH_DIRECTION])
        
        // Verify they are Transit Keywords
        assertTrue(received[IpcKeys.MSG_TYPE] is com.cognitect.transit.Keyword)
        assertTrue(received[IpcKeys.ACTION] is com.cognitect.transit.Keyword)
    }
    
    @Test
    fun testDirectSendMethod() = runBlocking {
        establishConnection()
        
        // Use direct send (non-coroutine)
        val message = mapOf(
            IpcKeys.MSG_TYPE to IpcKeys.LOG,
            IpcKeys.MESSAGE to "Direct send test"
        )
        
        clientComm!!.sendMessageDirect(message)
        
        // Give time for message to arrive
        delay(50)
        
        val received = serverComm!!.readMessage()
        assertNotNull(received)
        assertEquals(IpcKeys.LOG, received!![IpcKeys.MSG_TYPE])
        assertEquals("Direct send test", received[IpcKeys.MESSAGE])
    }
    
    @Test
    fun testHasMessage() = runBlocking {
        establishConnection()
        
        // Initially no messages
        assertFalse(serverComm!!.hasMessage())
        
        // Send a message
        clientComm!!.sendMessage(mapOf(IpcKeys.MSG_TYPE to IpcKeys.EVENT))
        
        // Give time for message to arrive
        delay(100)
        
        // Should have message
        assertTrue(serverComm!!.hasMessage())
        
        // Read the message
        val received = serverComm!!.readMessage()
        assertNotNull(received)
        
        // Should be empty again
        assertFalse(serverComm!!.hasMessage())
    }
    
    @Test
    fun testCommunicatorLifecycle() = runBlocking {
        // Create communicators
        serverComm = TransitSocketCommunicator.createWithPath(socketPath, "server", true)
        clientComm = TransitSocketCommunicator.createWithPath(socketPath, "client", false)
        
        // Initially not running
        assertFalse(serverComm!!.isRunning())
        assertFalse(clientComm!!.isRunning())
        
        // Start them
        val serverFuture = CompletableFuture.runAsync {
            runBlocking { serverComm!!.start() }
        }
        delay(100)
        clientComm!!.start()
        serverFuture.get(2, TimeUnit.SECONDS)
        
        // Should be running
        assertTrue(serverComm!!.isRunning())
        assertTrue(clientComm!!.isRunning())
        
        // Stop client
        clientComm!!.stop()
        assertFalse(clientComm!!.isRunning())
        
        // Server might still be running briefly
        delay(200)
        
        // Stop server
        serverComm!!.stop()
        assertFalse(serverComm!!.isRunning())
    }
    
    /**
     * Helper to establish connection.
     */
    private suspend fun establishConnection() {
        serverComm = TransitSocketCommunicator.createWithPath(socketPath, "test-server", true)
        clientComm = TransitSocketCommunicator.createWithPath(socketPath, "test-client", false)
        
        val serverFuture = CompletableFuture.runAsync {
            runBlocking { serverComm!!.start() }
        }
        
        delay(100)
        clientComm!!.start()
        serverFuture.get(2, TimeUnit.SECONDS)
    }
}