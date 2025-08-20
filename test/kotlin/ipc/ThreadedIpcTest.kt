package potatoclient.kotlin.ipc

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

/**
 * Tests IPC communication using separate threads to avoid blocking.
 */
object ThreadedIpcTest {
    private var testsPassed = 0
    private var testsFailed = 0
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("Running Threaded IPC Tests...\n")
        
        testBasicMessageExchange()
        testCloseRequest()
        testMultipleStreams()
        
        println("\n========================================")
        println("Test Results:")
        println("  Passed: $testsPassed")
        println("  Failed: $testsFailed")
        println("========================================")
        
        System.exit(if (testsFailed > 0) 1 else 0)
    }
    
    private fun testBasicMessageExchange() {
        print("Testing Basic Message Exchange... ")
        try {
            val streamName = "test-basic"
            val messageReceived = CountDownLatch(1)
            val receivedMessage = AtomicReference<Map<*, *>>()
            var server: IpcServer? = null
            var client: IpcClient? = null
            
            // Start server in a separate thread
            val serverThread = thread {
                try {
                    server = IpcServer.create(streamName)
                    server!!.setOnMessage { message ->
                        receivedMessage.set(message)
                        messageReceived.countDown()
                    }
                    
                    // Keep server running
                    Thread.sleep(5000)
                }
            }
            
            // Give server time to start
            Thread.sleep(500)
            
            // Start client and send message in another thread
            val clientThread = thread {
                try {
                    val serverPid = IpcServer.getCurrentPid()
                    client = IpcClient.create(serverPid, streamName)
                    
                    // Send a test event
                    client!!.sendEvent(
                        IpcKeys.GESTURE,
                        mapOf(
                            IpcKeys.GESTURE_TYPE to IpcKeys.TAP,
                            IpcKeys.X to 100,
                            IpcKeys.Y to 200
                        )
                    )
                }
            }
            
            // Wait for message to be received
            val received = messageReceived.await(3, TimeUnit.SECONDS)
            
            // Clean up
            try {
                client?.disconnect()
                server?.stop()
            }
            serverThread.interrupt()
            clientThread.interrupt()
            
            // Verify results
            assert(received) { "Message was not received" }
            val message = receivedMessage.get()
            assert(message != null) { "Message is null" }
            assert(message!![IpcKeys.MSG_TYPE] == IpcKeys.EVENT) { "Wrong message type" }
            assert(message[IpcKeys.TYPE] == IpcKeys.GESTURE) { "Wrong event type" }
            assert(message[IpcKeys.X] == 100) { "Wrong X value" }
            
            IpcServer.stopAll()
            passed()
        } catch (e: Exception) {
            IpcServer.stopAll()
            failed(e)
        }
    }
    
    private fun testCloseRequest() {
        print("Testing Close Request... ")
        try {
            val streamName = "test-close"
            val closeReceived = CountDownLatch(1)
            var server: IpcServer? = null
            var client: IpcClient? = null
            
            // Start server
            val serverThread = thread {
                try {
                    server = IpcServer.create(streamName)
                    Thread.sleep(500)  // Wait for client
                    
                    // Send close request
                    server!!.sendCloseRequest()
                    
                    // Keep running
                    Thread.sleep(3000)
                }
            }
            
            Thread.sleep(200)
            
            // Start client
            val clientThread = thread {
                try {
                    val serverPid = IpcServer.getCurrentPid()
                    client = IpcClient.create(serverPid, streamName)
                    
                    client!!.setOnCloseRequest {
                        closeReceived.countDown()
                    }
                    
                    // Keep running to receive close request
                    Thread.sleep(3000)
                }
            }
            
            // Wait for close request
            val received = closeReceived.await(3, TimeUnit.SECONDS)
            
            // Clean up
            try {
                client?.disconnect()
                server?.stop()
            }
            serverThread.interrupt()
            clientThread.interrupt()
            
            assert(received) { "Close request not received" }
            
            IpcServer.stopAll()
            passed()
        } catch (e: Exception) {
            IpcServer.stopAll()
            failed(e)
        }
    }
    
    private fun testMultipleStreams() {
        print("Testing Multiple Streams... ")
        try {
            try {
                // Create servers with proper synchronization
                val heatServer = IpcServer.create("heat", awaitBinding = true)
                val dayServer = IpcServer.create("day", awaitBinding = true)
                
                val heatReceived = CountDownLatch(1)
                val dayReceived = CountDownLatch(1)
                val heatMessage = AtomicReference<Map<*, *>>()
                val dayMessage = AtomicReference<Map<*, *>>()
                
                heatServer.setOnMessage { message ->
                    heatMessage.set(message)
                    heatReceived.countDown()
                }
                
                dayServer.setOnMessage { message ->
                    dayMessage.set(message)
                    dayReceived.countDown()
                }
                
                // Create clients with retry logic
                val serverPid = IpcServer.getCurrentPid()
                val heatClient = IpcClient.create(serverPid, "heat", retryOnFailure = true)
                val dayClient = IpcClient.create(serverPid, "day", retryOnFailure = true)
                
                // Send messages
                heatClient.sendEvent(
                    IpcKeys.GESTURE,
                    mapOf(IpcKeys.X to 100)
                )
                
                dayClient.sendEvent(
                    IpcKeys.GESTURE,
                    mapOf(IpcKeys.X to 200)
                )
                
                // Wait for messages with timeout
                val heatOk = heatReceived.await(3, TimeUnit.SECONDS)
                val dayOk = dayReceived.await(3, TimeUnit.SECONDS)
                
                // Verify
                assert(heatOk) { "Heat message not received" }
                assert(dayOk) { "Day message not received" }
                
                val heat = heatMessage.get()
                assert(heat != null) { "Heat message is null" }
                assert(heat[IpcKeys.X] == 100) { "Wrong heat X value: ${heat[IpcKeys.X]}" }
                assert(heat[IpcKeys.STREAM_TYPE] == IpcKeys.HEAT) { "Wrong heat stream type" }
                
                val day = dayMessage.get()
                assert(day != null) { "Day message is null" }
                assert(day[IpcKeys.X] == 200) { "Wrong day X value: ${day[IpcKeys.X]}" }
                assert(day[IpcKeys.STREAM_TYPE] == IpcKeys.DAY) { "Wrong day stream type" }
                
                // Clean up
                heatClient.disconnect()
                dayClient.disconnect()
                heatServer.stop()
                dayServer.stop()
            }
            
            IpcServer.stopAll()
            passed()
        } catch (e: Exception) {
            IpcServer.stopAll()
            failed(e)
        }
    }
    
    private fun passed() {
        println("PASSED")
        testsPassed++
    }
    
    private fun failed(e: Exception) {
        println("FAILED")
        println("  Error: ${e.message}")
        e.printStackTrace()
        testsFailed++
    }
}