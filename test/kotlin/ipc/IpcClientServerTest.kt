package potatoclient.kotlin.ipc

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Tests for IpcClient and IpcServer interaction.
 */
class IpcClientServerTest {
    private var server: IpcServer? = null
    private var client: IpcClient? = null
    private val streamName = "test-stream"
    
    @Before
    fun setUp() {
        // Clean up any existing servers
        IpcServer.stopAll()
    }
    
    @After
    fun tearDown() {
        // Clean up
        client?.disconnect()
        server?.stop()
        IpcServer.stopAll()
    }
    
    @Test
    fun testBasicClientServerCommunication() {
        // Create and start server
        server = IpcServer.create(streamName)
        
        // Get server PID and create client
        val serverPid = IpcServer.getCurrentPid()
        client = IpcClient.create(serverPid, streamName)
        
        // Set up message receiver
        val receivedMessage = AtomicReference<Map<*, *>>()
        val latch = CountDownLatch(1)
        
        server!!.setOnMessage { message ->
            receivedMessage.set(message)
            latch.countDown()
        }
        
        // Send event from client
        client!!.sendEvent(
            IpcKeys.GESTURE,
            mapOf(
                IpcKeys.GESTURE_TYPE to IpcKeys.TAP,
                IpcKeys.X to 100,
                IpcKeys.Y to 200
            )
        )
        
        // Wait for message
        assertTrue("Message not received", latch.await(2, TimeUnit.SECONDS))
        
        // Verify message
        val message = receivedMessage.get()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.TAP, message[IpcKeys.GESTURE_TYPE])
        assertEquals(100, message[IpcKeys.X])
        assertEquals(200, message[IpcKeys.Y])
        assertEquals(IpcKeys.streamType(streamName), message[IpcKeys.STREAM_TYPE])
    }
    
    @Test
    fun testCommandMessage() {
        server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        client = IpcClient.create(serverPid, streamName)
        
        val receivedMessage = AtomicReference<Map<*, *>>()
        val latch = CountDownLatch(1)
        
        server!!.setOnMessage { message ->
            receivedMessage.set(message)
            latch.countDown()
        }
        
        // Send command
        client!!.sendCommand(
            IpcKeys.keyword("rotary-goto-ndc"),
            mapOf(
                IpcKeys.NDC_X to 0.5,
                IpcKeys.NDC_Y to -0.5
            )
        )
        
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        
        val message = receivedMessage.get()
        assertEquals(IpcKeys.COMMAND, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.keyword("rotary-goto-ndc"), message[IpcKeys.ACTION])
        assertEquals(0.5, message[IpcKeys.NDC_X])
        assertEquals(-0.5, message[IpcKeys.NDC_Y])
    }
    
    @Test
    fun testLogMessage() {
        server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        client = IpcClient.create(serverPid, streamName)
        
        val receivedMessage = AtomicReference<Map<*, *>>()
        val latch = CountDownLatch(1)
        
        server!!.setOnMessage { message ->
            receivedMessage.set(message)
            latch.countDown()
        }
        
        // Send log
        client!!.sendLog(
            IpcKeys.ERROR,
            "Test error message",
            mapOf("error_code" to 500)
        )
        
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        
        val message = receivedMessage.get()
        assertEquals(IpcKeys.LOG, message[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.ERROR, message[IpcKeys.LEVEL])
        assertEquals("Test error message", message[IpcKeys.MESSAGE])
        assertEquals("$streamName-stream", message[IpcKeys.PROCESS])
        
        val data = message[IpcKeys.DATA] as Map<*, *>
        assertEquals(500, data["error_code"])
    }
    
    @Test
    fun testMetricMessage() {
        server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        client = IpcClient.create(serverPid, streamName)
        
        val receivedMessage = AtomicReference<Map<*, *>>()
        val latch = CountDownLatch(1)
        
        server!!.setOnMessage { message ->
            receivedMessage.set(message)
            latch.countDown()
        }
        
        // Send metric
        client!!.sendMetric(
            "frame_rate",
            30.5,
            mapOf("stream" to streamName)
        )
        
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        
        val message = receivedMessage.get()
        assertEquals(IpcKeys.METRIC, message[IpcKeys.MSG_TYPE])
        assertEquals("frame_rate", message[IpcKeys.keyword("name")])
        assertEquals(30.5, message[IpcKeys.keyword("value")])
        
        val tags = message[IpcKeys.keyword("tags")] as Map<*, *>
        assertEquals(streamName, tags["stream"])
    }
    
    @Test
    fun testCloseRequest() {
        server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        client = IpcClient.create(serverPid, streamName)
        
        val closeRequestReceived = CountDownLatch(1)
        
        // Set close request handler
        client!!.setOnCloseRequest {
            closeRequestReceived.countDown()
        }
        
        // Send close request from server
        server!!.sendCloseRequest()
        
        // Wait for close request
        assertTrue("Close request not received", closeRequestReceived.await(2, TimeUnit.SECONDS))
        assertTrue("Close request flag not set", client!!.hasCloseRequest())
    }
    
    @Test
    fun testMultipleMessages() {
        server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        client = IpcClient.create(serverPid, streamName)
        
        val messageCount = 10
        val latch = CountDownLatch(messageCount)
        val receivedMessages = mutableListOf<Map<*, *>>()
        
        server!!.setOnMessage { message ->
            synchronized(receivedMessages) {
                receivedMessages.add(message)
            }
            latch.countDown()
        }
        
        // Send multiple messages
        for (i in 1..messageCount) {
            client!!.sendEvent(
                IpcKeys.GESTURE,
                mapOf(
                    IpcKeys.GESTURE_TYPE to IpcKeys.PAN_MOVE,
                    IpcKeys.X to i * 10,
                    IpcKeys.Y to i * 20
                )
            )
        }
        
        // Wait for all messages
        assertTrue("Not all messages received", latch.await(5, TimeUnit.SECONDS))
        
        // Verify message count
        assertEquals(messageCount, receivedMessages.size)
        
        // Verify message content
        for ((index, message) in receivedMessages.withIndex()) {
            val i = index + 1
            assertEquals(IpcKeys.EVENT, message[IpcKeys.MSG_TYPE])
            assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
            assertEquals(IpcKeys.PAN_MOVE, message[IpcKeys.GESTURE_TYPE])
            assertEquals(i * 10, message[IpcKeys.X])
            assertEquals(i * 20, message[IpcKeys.Y])
        }
    }
    
    @Test
    fun testDirectSend() {
        server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        client = IpcClient.create(serverPid, streamName)
        
        val receivedMessage = AtomicReference<Map<*, *>>()
        val latch = CountDownLatch(1)
        
        server!!.setOnMessage { message ->
            receivedMessage.set(message)
            latch.countDown()
        }
        
        // Send message directly (non-coroutine)
        val message = mapOf<Any, Any>(
            IpcKeys.MSG_TYPE to IpcKeys.EVENT,
            IpcKeys.TYPE to IpcKeys.WINDOW,
            IpcKeys.ACTION to IpcKeys.RESIZE,
            IpcKeys.WIDTH to 1920,
            IpcKeys.HEIGHT to 1080,
            IpcKeys.TIMESTAMP to System.currentTimeMillis(),
            IpcKeys.STREAM_TYPE to IpcKeys.streamType(streamName)
        )
        
        client!!.sendMessageDirect(message)
        
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        
        val received = receivedMessage.get()
        assertEquals(IpcKeys.EVENT, received[IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.WINDOW, received[IpcKeys.TYPE])
        assertEquals(IpcKeys.RESIZE, received[IpcKeys.ACTION])
        assertEquals(1920, received[IpcKeys.WIDTH])
        assertEquals(1080, received[IpcKeys.HEIGHT])
    }
    
    @Test
    fun testServerReceiveMessage() {
        server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        client = IpcClient.create(serverPid, streamName)
        
        // Send message
        client!!.sendEvent(
            IpcKeys.GESTURE,
            mapOf(IpcKeys.GESTURE_TYPE to IpcKeys.DOUBLE_TAP)
        )
        
        // Receive using blocking method
        Thread.sleep(100) // Give time for message to arrive
        val message = server!!.receiveMessage()
        
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.GESTURE, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.DOUBLE_TAP, message[IpcKeys.GESTURE_TYPE])
    }
    
    @Test
    fun testTryReceiveMessage() {
        server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        client = IpcClient.create(serverPid, streamName)
        
        // Initially no message
        assertNull(server!!.tryReceiveMessage())
        
        // Send message
        client!!.sendEvent(
            IpcKeys.WINDOW,
            mapOf(IpcKeys.ACTION to IpcKeys.FOCUS)
        )
        
        // Give time for message to arrive
        Thread.sleep(100)
        
        // Should be able to receive
        val message = server!!.tryReceiveMessage()
        assertNotNull(message)
        assertEquals(IpcKeys.EVENT, message!![IpcKeys.MSG_TYPE])
        assertEquals(IpcKeys.WINDOW, message[IpcKeys.TYPE])
        assertEquals(IpcKeys.FOCUS, message[IpcKeys.ACTION])
        
        // Should be empty again
        assertNull(server!!.tryReceiveMessage())
    }
    
    @Test
    fun testMultipleStreams() {
        // Create servers for different streams
        val heatServer = IpcServer.create("heat")
        val dayServer = IpcServer.create("day")
        
        val serverPid = IpcServer.getCurrentPid()
        
        // Create clients
        val heatClient = IpcClient.create(serverPid, "heat")
        val dayClient = IpcClient.create(serverPid, "day")
        
        try {
            val heatLatch = CountDownLatch(1)
            val dayLatch = CountDownLatch(1)
            val heatMessage = AtomicReference<Map<*, *>>()
            val dayMessage = AtomicReference<Map<*, *>>()
            
            heatServer.setOnMessage { message ->
                heatMessage.set(message)
                heatLatch.countDown()
            }
            
            dayServer.setOnMessage { message ->
                dayMessage.set(message)
                dayLatch.countDown()
            }
            
            // Send messages to different streams
            heatClient.sendEvent(
                IpcKeys.GESTURE,
                mapOf(IpcKeys.GESTURE_TYPE to IpcKeys.TAP, IpcKeys.X to 100)
            )
            
            dayClient.sendEvent(
                IpcKeys.GESTURE,
                mapOf(IpcKeys.GESTURE_TYPE to IpcKeys.PAN_START, IpcKeys.X to 200)
            )
            
            // Wait for both messages
            assertTrue(heatLatch.await(2, TimeUnit.SECONDS))
            assertTrue(dayLatch.await(2, TimeUnit.SECONDS))
            
            // Verify messages went to correct servers
            val heat = heatMessage.get()
            assertEquals(IpcKeys.TAP, heat[IpcKeys.GESTURE_TYPE])
            assertEquals(100, heat[IpcKeys.X])
            assertEquals(IpcKeys.HEAT, heat[IpcKeys.STREAM_TYPE])
            
            val day = dayMessage.get()
            assertEquals(IpcKeys.PAN_START, day[IpcKeys.GESTURE_TYPE])
            assertEquals(200, day[IpcKeys.X])
            assertEquals(IpcKeys.DAY, day[IpcKeys.STREAM_TYPE])
            
        } finally {
            heatClient.disconnect()
            dayClient.disconnect()
            heatServer.stop()
            dayServer.stop()
        }
    }
    
    @Test
    fun testServerGet() {
        val server1 = IpcServer.create("stream1")
        val server2 = IpcServer.create("stream2")
        
        try {
            // Should be able to get servers by name
            assertEquals(server1, IpcServer.get("stream1"))
            assertEquals(server2, IpcServer.get("stream2"))
            assertNull(IpcServer.get("nonexistent"))
            
            // Remove one
            IpcServer.remove("stream1")
            assertNull(IpcServer.get("stream1"))
            assertEquals(server2, IpcServer.get("stream2"))
            
        } finally {
            IpcServer.stopAll()
        }
    }
    
    @Test(expected = IllegalStateException::class)
    fun testDuplicateServer() {
        IpcServer.create("duplicate")
        IpcServer.create("duplicate") // Should throw
    }
    
    @Test(expected = IllegalStateException::class)
    fun testClientNotConnected() {
        val serverPid = IpcServer.getCurrentPid()
        val socketPath = IpcClient.generateSocketPath(serverPid, "test")
        val client = IpcClient(socketPath, "test")
        
        // Try to send without connecting
        client.sendEvent(IpcKeys.GESTURE, mapOf())
    }
    
    @Test
    fun testLifecycle() {
        val server = IpcServer.create("lifecycle")
        assertTrue(server.isRunning())
        
        val serverPid = IpcServer.getCurrentPid()
        val client = IpcClient.create(serverPid, "lifecycle")
        assertTrue(client.isConnected())
        
        // Disconnect client
        client.disconnect()
        assertFalse(client.isConnected())
        
        // Stop server
        server.stop()
        assertFalse(server.isRunning())
    }
}