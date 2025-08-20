package potatoclient.kotlin.ipc

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Simple test runner for IpcClient and IpcServer without JUnit.
 */
object IpcClientServerTestRunner {
    private var testsPassed = 0
    private var testsFailed = 0
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("Running IpcClient/IpcServer Tests...\n")
        
        runTest("Basic Client-Server Communication") { testBasicCommunication() }
        runTest("Command Message") { testCommandMessage() }
        runTest("Log Message") { testLogMessage() }
        runTest("Metric Message") { testMetricMessage() }
        runTest("Close Request") { testCloseRequest() }
        runTest("Multiple Messages") { testMultipleMessages() }
        runTest("Multiple Streams") { testMultipleStreams() }
        runTest("Server Lifecycle") { testServerLifecycle() }
        
        println("\n========================================")
        println("Test Results:")
        println("  Passed: $testsPassed")
        println("  Failed: $testsFailed")
        println("========================================")
        
        System.exit(if (testsFailed > 0) 1 else 0)
    }
    
    private fun runTest(name: String, test: suspend () -> Unit) {
        print("Testing $name... ")
        try {
            // Clean up before each test
            IpcServer.stopAll()
            
            runBlocking {
                test()
            }
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED")
            println("  Error: ${e.message}")
            e.printStackTrace()
            testsFailed++
        } finally {
            // Clean up after each test
            IpcServer.stopAll()
        }
    }
    
    private suspend fun testBasicCommunication() {
        val streamName = "test-stream"
        val server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        val client = IpcClient.create(serverPid, streamName)
        
        val receivedMessage = AtomicReference<Map<*, *>>()
        val latch = CountDownLatch(1)
        
        server.setOnMessage { message ->
            receivedMessage.set(message)
            latch.countDown()
        }
        
        // Send event from client
        client.sendEvent(
            IpcKeys.GESTURE,
            mapOf(
                IpcKeys.GESTURE_TYPE to IpcKeys.TAP,
                IpcKeys.X to 100,
                IpcKeys.Y to 200
            )
        )
        
        assert(latch.await(2, TimeUnit.SECONDS)) { "Message not received" }
        
        val message = receivedMessage.get()
        assert(message != null) { "Message is null" }
        assert(message[IpcKeys.MSG_TYPE] == IpcKeys.EVENT) { "Wrong message type" }
        assert(message[IpcKeys.TYPE] == IpcKeys.GESTURE) { "Wrong event type" }
        assert(message[IpcKeys.X] == 100) { "Wrong X coordinate" }
        
        client.disconnect()
        server.stop()
    }
    
    private suspend fun testCommandMessage() {
        val streamName = "cmd-test"
        val server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        val client = IpcClient.create(serverPid, streamName)
        
        val receivedMessage = AtomicReference<Map<*, *>>()
        val latch = CountDownLatch(1)
        
        server.setOnMessage { message ->
            receivedMessage.set(message)
            latch.countDown()
        }
        
        client.sendCommand(
            IpcKeys.ROTARY_GOTO_NDC,
            mapOf(
                IpcKeys.NDC_X to 0.5,
                IpcKeys.NDC_Y to -0.5
            )
        )
        
        assert(latch.await(2, TimeUnit.SECONDS)) { "Command not received" }
        
        val message = receivedMessage.get()
        assert(message[IpcKeys.MSG_TYPE] == IpcKeys.COMMAND) { "Wrong message type" }
        assert(message[IpcKeys.ACTION] == IpcKeys.ROTARY_GOTO_NDC) { "Wrong action" }
        
        client.disconnect()
        server.stop()
    }
    
    private suspend fun testLogMessage() {
        val streamName = "log-test"
        val server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        val client = IpcClient.create(serverPid, streamName)
        
        val receivedMessage = AtomicReference<Map<*, *>>()
        val latch = CountDownLatch(1)
        
        server.setOnMessage { message ->
            receivedMessage.set(message)
            latch.countDown()
        }
        
        client.sendLog(
            IpcKeys.ERROR,
            "Test error message",
            mapOf("error_code" to 500)
        )
        
        assert(latch.await(2, TimeUnit.SECONDS)) { "Log not received" }
        
        val message = receivedMessage.get()
        assert(message[IpcKeys.MSG_TYPE] == IpcKeys.LOG) { "Wrong message type" }
        assert(message[IpcKeys.LEVEL] == IpcKeys.ERROR) { "Wrong log level" }
        assert(message[IpcKeys.MESSAGE] == "Test error message") { "Wrong log message" }
        
        client.disconnect()
        server.stop()
    }
    
    private suspend fun testMetricMessage() {
        val streamName = "metric-test"
        val server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        val client = IpcClient.create(serverPid, streamName)
        
        val receivedMessage = AtomicReference<Map<*, *>>()
        val latch = CountDownLatch(1)
        
        server.setOnMessage { message ->
            receivedMessage.set(message)
            latch.countDown()
        }
        
        client.sendMetric(
            "frame_rate",
            30.5,
            mapOf("stream" to streamName)
        )
        
        assert(latch.await(2, TimeUnit.SECONDS)) { "Metric not received" }
        
        val message = receivedMessage.get()
        assert(message[IpcKeys.MSG_TYPE] == IpcKeys.METRIC) { "Wrong message type" }
        assert(message[IpcKeys.keyword("name")] == "frame_rate") { "Wrong metric name" }
        assert(message[IpcKeys.keyword("value")] == 30.5) { "Wrong metric value" }
        
        client.disconnect()
        server.stop()
    }
    
    private suspend fun testCloseRequest() {
        val streamName = "close-test"
        val server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        val client = IpcClient.create(serverPid, streamName)
        
        val closeRequestReceived = CountDownLatch(1)
        
        client.setOnCloseRequest {
            closeRequestReceived.countDown()
        }
        
        // Send close request from server
        server.sendCloseRequest()
        
        assert(closeRequestReceived.await(2, TimeUnit.SECONDS)) { "Close request not received" }
        assert(client.hasCloseRequest()) { "Close request flag not set" }
        
        client.disconnect()
        server.stop()
    }
    
    private suspend fun testMultipleMessages() {
        val streamName = "multi-msg-test"
        val server = IpcServer.create(streamName)
        val serverPid = IpcServer.getCurrentPid()
        val client = IpcClient.create(serverPid, streamName)
        
        val messageCount = 5
        val latch = CountDownLatch(messageCount)
        val receivedMessages = mutableListOf<Map<*, *>>()
        
        server.setOnMessage { message ->
            synchronized(receivedMessages) {
                receivedMessages.add(message)
            }
            latch.countDown()
        }
        
        // Send multiple messages
        for (i in 1..messageCount) {
            client.sendEvent(
                IpcKeys.GESTURE,
                mapOf(
                    IpcKeys.GESTURE_TYPE to IpcKeys.PAN_MOVE,
                    IpcKeys.X to i * 10
                )
            )
        }
        
        assert(latch.await(5, TimeUnit.SECONDS)) { "Not all messages received" }
        assert(receivedMessages.size == messageCount) { "Wrong message count" }
        
        client.disconnect()
        server.stop()
    }
    
    private suspend fun testMultipleStreams() {
        val heatServer = IpcServer.create("heat")
        val dayServer = IpcServer.create("day")
        
        val serverPid = IpcServer.getCurrentPid()
        
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
                mapOf(IpcKeys.X to 100)
            )
            
            dayClient.sendEvent(
                IpcKeys.GESTURE,
                mapOf(IpcKeys.X to 200)
            )
            
            assert(heatLatch.await(2, TimeUnit.SECONDS)) { "Heat message not received" }
            assert(dayLatch.await(2, TimeUnit.SECONDS)) { "Day message not received" }
            
            val heat = heatMessage.get()
            assert(heat[IpcKeys.X] == 100) { "Wrong heat X value" }
            assert(heat[IpcKeys.STREAM_TYPE] == IpcKeys.HEAT) { "Wrong heat stream type" }
            
            val day = dayMessage.get()
            assert(day[IpcKeys.X] == 200) { "Wrong day X value" }
            assert(day[IpcKeys.STREAM_TYPE] == IpcKeys.DAY) { "Wrong day stream type" }
            
        } finally {
            heatClient.disconnect()
            dayClient.disconnect()
            heatServer.stop()
            dayServer.stop()
        }
    }
    
    private suspend fun testServerLifecycle() {
        val streamName = "lifecycle-test"
        val server = IpcServer.create(streamName)
        assert(server.isRunning()) { "Server should be running" }
        
        val serverPid = IpcServer.getCurrentPid()
        val client = IpcClient.create(serverPid, streamName)
        assert(client.isConnected()) { "Client should be connected" }
        
        // Test get method
        val retrievedServer = IpcServer.get(streamName)
        assert(retrievedServer == server) { "Should retrieve same server instance" }
        
        // Disconnect and stop
        client.disconnect()
        assert(!client.isConnected()) { "Client should be disconnected" }
        
        server.stop()
        assert(!server.isRunning()) { "Server should be stopped" }
        
        // Server should be removed
        val removedServer = IpcServer.get(streamName)
        assert(removedServer == null) { "Server should be removed" }
    }
}