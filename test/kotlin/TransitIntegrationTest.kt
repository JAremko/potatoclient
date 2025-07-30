package potatoclient.transit

import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import com.cognitect.transit.TransitFactory
import com.cognitect.transit.Reader
import com.cognitect.transit.Writer

class TransitIntegrationTest {

    @Test
    fun testFullTransitRoundTrip() {
        // Create a complex nested structure
        val testData = mapOf(
            "msg-type" to "command",
            "msg-id" to "test-123",
            "timestamp" to 1234567890L,
            "payload" to mapOf(
                "action" to "rotary-rotate-both-to",
                "azimuthAngle" to 90.0,
                "azimuthSpeed" to 5.0,
                "elevationAngle" to 45.0,
                "elevationSpeed" to 3.0,
                "nested" to mapOf(
                    "list" to listOf(1, 2, 3),
                    "boolean" to true,
                    "null" to null
                )
            )
        )
        
        // Write to Transit
        val out = ByteArrayOutputStream()
        val writer = TransitFactory.writer(TransitFactory.Format.MSGPACK, out) as Writer<Any>
        writer.write(testData)
        
        // Read back
        val inStream = ByteArrayInputStream(out.toByteArray())
        val reader = TransitFactory.reader(TransitFactory.Format.MSGPACK, inStream)
        val result = reader.read() as Map<*, *>
        
        // Verify structure
        assertEquals("command", result["msg-type"])
        assertEquals("test-123", result["msg-id"])
        assertEquals(1234567890L, result["timestamp"])
        
        val payload = result["payload"] as Map<*, *>
        assertEquals("rotary-rotate-both-to", payload["action"])
        assertEquals(90.0, payload["azimuthAngle"])
        
        val nested = payload["nested"] as Map<*, *>
        assertEquals(listOf(1L, 2L, 3L), nested["list"]) // Transit converts to Long
        assertEquals(true, nested["boolean"])
        assertNull(nested["null"])
    }

    @Test
    fun testTransitCommunicatorIntegration() = runBlocking {
        // Create bidirectional pipes
        val clientToServer = PipedOutputStream()
        val serverToClient = PipedOutputStream()
        
        val clientIn = PipedInputStream(serverToClient)
        val clientOut = clientToServer
        val serverIn = PipedInputStream(clientToServer)
        val serverOut = serverToClient
        
        // Create communicators
        val clientComm = TransitCommunicator(clientIn, clientOut)
        val serverComm = TransitCommunicator(serverIn, serverOut)
        
        // Channel for received messages
        val receivedMessages = Channel<Map<String, Any>>(10)
        
        // Server coroutine
        val serverJob = launch {
            try {
                val message = serverComm.receiveMessage()
                receivedMessages.send(message)
                
                // Send response
                serverComm.sendMessage("response", mapOf(
                    "original-msg-id" to message["msg-id"],
                    "status" to "success"
                ))
            } catch (e: Exception) {
                fail("Server error: ${e.message}")
            }
        }
        
        // Client sends message
        launch {
            clientComm.sendMessage("test-command", mapOf(
                "action" to "ping",
                "data" to "test-data"
            ))
        }
        
        // Wait for message
        val received = withTimeout(1000) {
            receivedMessages.receive()
        }
        
        assertEquals("test-command", received["msg-type"])
        assertNotNull(received["msg-id"])
        assertNotNull(received["timestamp"])
        
        val payload = received["payload"] as Map<*, *>
        assertEquals("ping", payload["action"])
        assertEquals("test-data", payload["data"])
        
        // Wait for response
        delay(100)
        
        // Client receives response
        val response = withTimeout(1000) {
            clientComm.receiveMessage()
        }
        
        assertEquals("response", response["msg-type"])
        val responsePayload = response["payload"] as Map<*, *>
        assertEquals("success", responsePayload["status"])
        
        serverJob.cancel()
    }

    @Test
    fun testFramedStreamWithMultipleMessages() {
        val output = ByteArrayOutputStream()
        val framed = FramedOutputStream(output)
        
        // Write multiple messages
        val messages = listOf(
            "First message",
            "Second message with special chars: \u0000\u0001\u0002",
            "Third message that is quite a bit longer than the others to test different sizes",
            "" // Empty message
        )
        
        messages.forEach { msg ->
            framed.write(msg.toByteArray())
            framed.flush()
        }
        
        // Read them back
        val input = ByteArrayInputStream(output.toByteArray())
        val framedIn = FramedInputStream(input)
        
        messages.forEach { expected ->
            val buffer = ByteArray(1024)
            val bytesRead = framedIn.read(buffer)
            
            if (expected.isEmpty()) {
                assertEquals(0, bytesRead)
            } else {
                assertEquals(expected.length, bytesRead)
                assertEquals(expected, String(buffer, 0, bytesRead))
            }
        }
        
        // Verify no more data
        assertEquals(-1, framedIn.read())
    }

    @Test
    fun testErrorHandlingInCommunicator() = runBlocking {
        // Create a stream that will fail
        val failingInput = object : ByteArrayInputStream(ByteArray(0)) {
            override fun read(): Int {
                throw java.io.IOException("Simulated read failure")
            }
            
            override fun read(b: ByteArray, off: Int, len: Int): Int {
                throw java.io.IOException("Simulated read failure")
            }
        }
        
        val output = ByteArrayOutputStream()
        val comm = TransitCommunicator(failingInput, output)
        
        // Try to receive - should throw
        try {
            comm.receiveMessage()
            fail("Should throw exception on read failure")
        } catch (e: java.io.IOException) {
            assertTrue(e.message?.contains("Simulated read failure") ?: false)
        }
    }

    @Test
    fun testLargeMessageHandling() = runBlocking {
        val output = ByteArrayOutputStream()
        val comm = TransitCommunicator(ByteArrayInputStream(ByteArray(0)), output)
        
        // Create a large payload
        val largeList = (1..10000).map { "Item $it" }
        val largePayload = mapOf(
            "data" to largeList,
            "metadata" to mapOf(
                "count" to largeList.size,
                "type" to "test-data"
            )
        )
        
        // Send large message
        comm.sendMessage("large-data", largePayload)
        
        // Verify it was written
        assertTrue("Should have written data", output.size() > 0)
        
        // Read it back
        val input = ByteArrayInputStream(output.toByteArray())
        val readComm = TransitCommunicator(input, ByteArrayOutputStream())
        
        val received = readComm.receiveMessage()
        assertEquals("large-data", received["msg-type"])
        
        val receivedPayload = received["payload"] as Map<*, *>
        val receivedData = receivedPayload["data"] as List<*>
        assertEquals(10000, receivedData.size)
        assertEquals("Item 1", receivedData[0])
        assertEquals("Item 10000", receivedData[9999])
    }
}