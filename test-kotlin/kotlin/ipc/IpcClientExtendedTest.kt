package potatoclient.kotlin.ipc

import potatoclient.kotlin.gestures.GestureEvent
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Tests for the extended IpcClient methods we added from IpcManager.
 */
object IpcClientExtendedTest {
    private var testsPassed = 0
    private var testsFailed = 0
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("Running Extended IPC Client Tests...\n")
        
        // Run all tests
        testSendWindowEvent()
        testSendConnectionEvent()
        testSendGestureEvent()
        testSendGestureEventWithObject()
        testOnMessageHandler()
        testInitializeAndShutdownAliases()
        testGetInstanceSingleton()
        
        println("\n========================================")
        println("Test Results:")
        println("  Passed: $testsPassed")
        println("  Failed: $testsFailed")
        println("========================================")
        
        System.exit(if (testsFailed > 0) 1 else 0)
    }
    
    private fun testSendWindowEvent() {
        print("Testing sendWindowEvent... ")
        try {
            val server = IpcServer.create("test-window", awaitBinding = true)
            val receivedMessage = AtomicReference<Map<*, *>>()
            val latch = CountDownLatch(1)
            
            server.setOnMessage { message ->
                receivedMessage.set(message)
                latch.countDown()
            }
            
            val serverPid = IpcServer.getCurrentPid()
            val client = IpcClient.create(serverPid, "test-window")
            
            // Send window event
            client.sendWindowEvent(
                action = "resize",
                width = 1920,
                height = 1080,
                x = 100,
                y = 200
            )
            
            // Wait for message
            val received = latch.await(2, TimeUnit.SECONDS)
            assert(received) { "Message not received" }
            
            val message = receivedMessage.get()
            assert(message != null) { "Message is null" }
            assert(message[IpcKeys.MSG_TYPE] == IpcKeys.EVENT) { "Wrong message type" }
            assert(message[IpcKeys.TYPE] == IpcKeys.WINDOW) { "Wrong event type" }
            assert(message[IpcKeys.ACTION] == IpcKeys.keyword("resize")) { "Wrong action" }
            assert(message[IpcKeys.WIDTH] == 1920) { "Wrong width" }
            assert(message[IpcKeys.HEIGHT] == 1080) { "Wrong height" }
            assert(message[IpcKeys.X] == 100) { "Wrong X" }
            assert(message[IpcKeys.Y] == 200) { "Wrong Y" }
            
            client.disconnect()
            server.stop()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        } finally {
            IpcServer.stopAll()
        }
    }
    
    private fun testSendConnectionEvent() {
        print("Testing sendConnectionEvent... ")
        try {
            val server = IpcServer.create("test-connection", awaitBinding = true)
            val receivedMessage = AtomicReference<Map<*, *>>()
            val latch = CountDownLatch(1)
            
            server.setOnMessage { message ->
                receivedMessage.set(message)
                latch.countDown()
            }
            
            val serverPid = IpcServer.getCurrentPid()
            val client = IpcClient.create(serverPid, "test-connection")
            
            // Send connection event
            val details = mapOf<Any, Any>(
                "url" to "ws://example.com",
                "stream-id" to "test-stream"
            )
            client.sendConnectionEvent(IpcKeys.CONNECTED, details)
            
            // Wait for message
            val received = latch.await(2, TimeUnit.SECONDS)
            assert(received) { "Message not received" }
            
            val message = receivedMessage.get()
            assert(message != null) { "Message is null" }
            assert(message[IpcKeys.MSG_TYPE] == IpcKeys.EVENT) { "Wrong message type" }
            assert(message[IpcKeys.TYPE] == IpcKeys.CONNECTION) { "Wrong event type" }
            assert(message[IpcKeys.ACTION] == IpcKeys.CONNECTED) { "Wrong action" }
            
            @Suppress("UNCHECKED_CAST")
            val receivedDetails = message[IpcKeys.DETAILS] as? Map<Any, Any>
            assert(receivedDetails != null) { "Details missing" }
            assert(receivedDetails!!["url"] == "ws://example.com") { "Wrong URL" }
            assert(receivedDetails["stream-id"] == "test-stream") { "Wrong stream ID" }
            
            client.disconnect()
            server.stop()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        } finally {
            IpcServer.stopAll()
        }
    }
    
    private fun testSendGestureEvent() {
        print("Testing sendGestureEvent... ")
        try {
            val server = IpcServer.create("test-gesture", awaitBinding = true)
            val receivedMessage = AtomicReference<Map<*, *>>()
            val latch = CountDownLatch(1)
            
            server.setOnMessage { message ->
                receivedMessage.set(message)
                latch.countDown()
            }
            
            val serverPid = IpcServer.getCurrentPid()
            val client = IpcClient.create(serverPid, "test-gesture")
            
            // Send gesture event
            client.sendGestureEvent(
                gestureType = IpcKeys.PAN_MOVE,
                x = 150,
                y = 250,
                frameTimestamp = 123456789L,
                deltaX = 10,
                deltaY = 20
            )
            
            // Wait for message
            val received = latch.await(2, TimeUnit.SECONDS)
            assert(received) { "Message not received" }
            
            val message = receivedMessage.get()
            assert(message != null) { "Message is null" }
            assert(message[IpcKeys.MSG_TYPE] == IpcKeys.EVENT) { "Wrong message type" }
            assert(message[IpcKeys.TYPE] == IpcKeys.GESTURE) { "Wrong event type" }
            assert(message[IpcKeys.GESTURE_TYPE] == IpcKeys.PAN_MOVE) { "Wrong gesture type" }
            assert(message[IpcKeys.X] == 150) { "Wrong X" }
            assert(message[IpcKeys.Y] == 250) { "Wrong Y" }
            assert(message[IpcKeys.FRAME_TIMESTAMP] == 123456789L) { "Wrong frame timestamp" }
            assert(message[IpcKeys.DELTA_X] == 10) { "Wrong delta X" }
            assert(message[IpcKeys.DELTA_Y] == 20) { "Wrong delta Y" }
            assert(message[IpcKeys.STREAM_TYPE] == IpcKeys.streamType("test-gesture")) { "Wrong stream type" }
            
            client.disconnect()
            server.stop()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        } finally {
            IpcServer.stopAll()
        }
    }
    
    private fun testSendGestureEventWithObject() {
        print("Testing sendGestureEvent with GestureEvent object... ")
        try {
            val server = IpcServer.create("test-gesture-obj", awaitBinding = true)
            val receivedMessages = mutableListOf<Map<*, *>>()
            val latch = CountDownLatch(7) // We'll send 7 different gesture types
            
            server.setOnMessage { message ->
                receivedMessages.add(message)
                latch.countDown()
            }
            
            val serverPid = IpcServer.getCurrentPid()
            val client = IpcClient.create(serverPid, "test-gesture-obj")
            
            // Test all gesture types
            val gestures = listOf(
                GestureEvent.Tap(100, 200, 1000L, 2000L),
                GestureEvent.DoubleTap(101, 201, 1001L, 2001L),
                GestureEvent.PanStart(102, 202, 1002L, 2002L),
                GestureEvent.PanMove(103, 203, 5, 10, 1003L, 2003L),
                GestureEvent.PanStop(104, 204, 1004L, 2004L),
                GestureEvent.WheelUp(105, 205, 3, 1005L, 2005L),
                GestureEvent.WheelDown(106, 206, 4, 1006L, 2006L)
            )
            
            // Send all gesture events
            gestures.forEach { gesture ->
                client.sendGestureEvent(gesture)
            }
            
            // Wait for all messages
            val received = latch.await(2, TimeUnit.SECONDS)
            assert(received) { "Not all messages received" }
            assert(receivedMessages.size == 7) { "Expected 7 messages, got ${receivedMessages.size}" }
            
            // Verify each message
            assert(receivedMessages[0][IpcKeys.GESTURE_TYPE] == IpcKeys.TAP) { "Wrong type for Tap" }
            assert(receivedMessages[1][IpcKeys.GESTURE_TYPE] == IpcKeys.DOUBLE_TAP) { "Wrong type for DoubleTap" }
            assert(receivedMessages[2][IpcKeys.GESTURE_TYPE] == IpcKeys.PAN_START) { "Wrong type for PanStart" }
            assert(receivedMessages[3][IpcKeys.GESTURE_TYPE] == IpcKeys.PAN_MOVE) { "Wrong type for PanMove" }
            assert(receivedMessages[3][IpcKeys.DELTA_X] == 5) { "Wrong delta X for PanMove" }
            assert(receivedMessages[3][IpcKeys.DELTA_Y] == 10) { "Wrong delta Y for PanMove" }
            assert(receivedMessages[4][IpcKeys.GESTURE_TYPE] == IpcKeys.PAN_STOP) { "Wrong type for PanStop" }
            assert(receivedMessages[5][IpcKeys.GESTURE_TYPE] == IpcKeys.WHEEL_UP) { "Wrong type for WheelUp" }
            assert(receivedMessages[5][IpcKeys.keyword("scroll-amount")] == 3) { "Wrong scroll amount for WheelUp" }
            assert(receivedMessages[6][IpcKeys.GESTURE_TYPE] == IpcKeys.WHEEL_DOWN) { "Wrong type for WheelDown" }
            assert(receivedMessages[6][IpcKeys.keyword("scroll-amount")] == 4) { "Wrong scroll amount for WheelDown" }
            
            client.disconnect()
            server.stop()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        } finally {
            IpcServer.stopAll()
        }
    }
    
    private fun testOnMessageHandler() {
        print("Testing onMessage handler... ")
        try {
            val server = IpcServer.create("test-handler", awaitBinding = true)
            val serverPid = IpcServer.getCurrentPid()
            val client = IpcClient.create(serverPid, "test-handler")
            
            val receivedMessages = mutableListOf<Map<*, *>>()
            val latch = CountDownLatch(3)
            
            // Register message handler
            client.onMessage { message ->
                receivedMessages.add(message)
                latch.countDown()
            }
            
            // Server sends messages to client
            Thread {
                Thread.sleep(100) // Give client time to set up listener
                server.sendCloseRequest() // This is a COMMAND message
                Thread.sleep(50)
                // Send additional test messages (though server normally only sends CLOSE_REQUEST)
                // For testing purposes, we'll send directly
                val testMessage1 = mapOf<Any, Any>(
                    IpcKeys.MSG_TYPE to IpcKeys.EVENT,
                    IpcKeys.TYPE to IpcKeys.keyword("test"),
                    IpcKeys.keyword("data") to "test1"
                )
                val testMessage2 = mapOf<Any, Any>(
                    IpcKeys.MSG_TYPE to IpcKeys.EVENT,
                    IpcKeys.TYPE to IpcKeys.keyword("test"),
                    IpcKeys.keyword("data") to "test2"
                )
                server.sendMessage(testMessage1)
                server.sendMessage(testMessage2)
            }.start()
            
            // Wait for messages
            val received = latch.await(2, TimeUnit.SECONDS)
            assert(received) { "Not all messages received" }
            assert(receivedMessages.size >= 1) { "No messages received" }
            
            // First message should be close request
            val firstMessage = receivedMessages[0]
            assert(firstMessage[IpcKeys.MSG_TYPE] == IpcKeys.COMMAND) { "First message not a command" }
            assert(firstMessage[IpcKeys.ACTION] == IpcKeys.CLOSE_REQUEST) { "First message not close request" }
            
            client.disconnect()
            server.stop()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        } finally {
            IpcServer.stopAll()
        }
    }
    
    private fun testInitializeAndShutdownAliases() {
        print("Testing initialize/shutdown aliases... ")
        try {
            val server = IpcServer.create("test-aliases", awaitBinding = true)
            val serverPid = IpcServer.getCurrentPid()
            val socketPath = IpcClient.generateSocketPath(serverPid, "test-aliases")
            val client = IpcClient(socketPath, "test-aliases")
            
            // Test initialize (alias for connect)
            client.initialize()
            assert(client.isConnected()) { "Client not connected after initialize" }
            
            // Test sending a message works
            val receivedMessage = AtomicReference<Map<*, *>>()
            val latch = CountDownLatch(1)
            
            server.setOnMessage { message ->
                receivedMessage.set(message)
                latch.countDown()
            }
            
            client.sendLog(IpcKeys.INFO, "Test message")
            
            val received = latch.await(2, TimeUnit.SECONDS)
            assert(received) { "Message not received" }
            
            // Test shutdown (alias for disconnect)
            client.shutdown()
            assert(!client.isConnected()) { "Client still connected after shutdown" }
            
            server.stop()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        } finally {
            IpcServer.stopAll()
        }
    }
    
    private fun testGetInstanceSingleton() {
        print("Testing create and reset methods... ")
        try {
            // Reset singleton first
            IpcClient.reset()
            
            // Create a server
            val server = IpcServer.create("test-singleton", awaitBinding = true)
            val currentPid = ProcessHandle.current().pid()
            
            // Create a client
            val client1 = IpcClient.create(currentPid, "test-singleton")
            assert(client1.isConnected()) { "Client not connected" }
            
            // Test that it works
            val receivedMessage = AtomicReference<Map<*, *>>()
            val latch = CountDownLatch(1)
            
            server.setOnMessage { message ->
                receivedMessage.set(message)
                latch.countDown()
            }
            
            client1.sendLog(IpcKeys.INFO, "Create test")
            
            val received = latch.await(2, TimeUnit.SECONDS)
            assert(received) { "Message not received" }
            
            val message = receivedMessage.get()
            assert(message[IpcKeys.MESSAGE] == "Create test") { "Wrong message" }
            
            // Test reset cleans up properly
            val wasConnected = client1.isConnected()
            assert(wasConnected) { "Client should be connected before reset" }
            
            // Note: reset only affects singleton instance, not manually created clients
            IpcClient.reset()
            
            // Client should still work since it was created directly
            assert(client1.isConnected()) { "Direct client should still be connected" }
            
            // Clean up
            client1.disconnect()
            server.stop()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        } finally {
            IpcServer.stopAll()
            IpcClient.reset()
        }
    }
}