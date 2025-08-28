package potatoclient.kotlin.ipc

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * IPC tests using thread-based implementation.
 */
object IpcTest {
    private var testsPassed = 0
    private var testsFailed = 0
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("Running IPC Tests...\n")
        
        testBasicCommunication()
        testMultipleStreams()
        testConnectionRetry()
        
        println("\n========================================")
        println("Test Results:")
        println("  Passed: $testsPassed")
        println("  Failed: $testsFailed")
        println("========================================")
        
        System.exit(if (testsFailed > 0) 1 else 0)
    }
    
    private fun testBasicCommunication() {
        print("Testing Basic Communication... ")
        try {
            // Create server with proper binding wait
            val server = IpcServer.create("test-basic", awaitBinding = true)
            
            val receivedMessage = AtomicReference<Map<*, *>>()
            val latch = CountDownLatch(1)
            
            server.setOnMessage { message ->
                receivedMessage.set(message)
                latch.countDown()
            }
            
            // Create client with retry
            val serverPid = IpcServer.getCurrentPid()
            val client = IpcClient.create(serverPid, "test-basic", retryOnFailure = true)
            
            // Send message
            client.sendEvent(
                IpcKeys.GESTURE,
                mapOf(
                    IpcKeys.GESTURE_TYPE to IpcKeys.TAP,
                    IpcKeys.X to 100,
                    IpcKeys.Y to 200
                )
            )
            
            // Wait for message
            val received = latch.await(2, TimeUnit.SECONDS)
            assert(received) { "Message not received" }
            
            val message = receivedMessage.get()
            assert(message != null) { "Message is null" }
            assert(message[IpcKeys.MSG_TYPE] == IpcKeys.EVENT) { "Wrong message type" }
            assert(message[IpcKeys.X] == 100) { "Wrong X value" }
            
            // Clean up
            client.disconnect()
            server.stop()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            testsFailed++
        } finally {
            IpcServer.stopAll()
        }
    }
    
    private fun testMultipleStreams() {
        print("Testing Multiple Streams... ")
        try {
            // Create both servers first with proper binding
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
            
            // Create clients with retry
            val serverPid = IpcServer.getCurrentPid()
            val heatClient = IpcClient.create(serverPid, "heat", retryOnFailure = true)
            val dayClient = IpcClient.create(serverPid, "day", retryOnFailure = true)
            
            // Send messages in parallel threads
            val heatThread = Thread {
                heatClient.sendEvent(
                    IpcKeys.GESTURE,
                    mapOf(IpcKeys.X to 100)
                )
            }
            val dayThread = Thread {
                dayClient.sendEvent(
                    IpcKeys.GESTURE,
                    mapOf(IpcKeys.X to 200)
                )
            }
            heatThread.start()
            dayThread.start()
            heatThread.join(1000)
            dayThread.join(1000)
            
            // Wait for both messages
            val heatOk = heatReceived.await(3, TimeUnit.SECONDS)
            val dayOk = dayReceived.await(3, TimeUnit.SECONDS)
            
            assert(heatOk) { "Heat message not received" }
            assert(dayOk) { "Day message not received" }
            
            val heat = heatMessage.get()
            assert(heat != null) { "Heat message is null" }
            assert(heat[IpcKeys.X] == 100) { "Wrong heat X value" }
            assert(heat[IpcKeys.STREAM_TYPE] == IpcKeys.HEAT) { "Wrong heat stream type" }
            
            val day = dayMessage.get()
            assert(day != null) { "Day message is null" }
            assert(day[IpcKeys.X] == 200) { "Wrong day X value" }
            assert(day[IpcKeys.STREAM_TYPE] == IpcKeys.DAY) { "Wrong day stream type" }
            
            // Clean up
            heatClient.disconnect()
            dayClient.disconnect()
            heatServer.stop()
            dayServer.stop()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            testsFailed++
        } finally {
            IpcServer.stopAll()
        }
    }
    
    private fun testConnectionRetry() {
        print("Testing Connection Retry... ")
        try {
            val serverPid = IpcServer.getCurrentPid()
            
            // Try to connect to non-existent server in a thread
            val clientThread = Thread {
                try {
                    // This should retry and eventually fail
                    IpcClient.create(serverPid, "nonexistent", retryOnFailure = true, maxRetries = 2)
                } catch (e: Exception) {
                    // Expected to fail
                }
            }
            clientThread.start()
            
            // Give it some time to retry
            Thread.sleep(500)
            
            // Now create the server
            val server = IpcServer.create("retry-test", awaitBinding = true)
            
            // Create a client that should succeed
            val client = IpcClient.create(serverPid, "retry-test", retryOnFailure = true)
            
            // Verify connection works
            val receivedMessage = AtomicReference<Map<*, *>>()
            val latch = CountDownLatch(1)
            
            server.setOnMessage { message ->
                receivedMessage.set(message)
                latch.countDown()
            }
            
            client.sendEvent(IpcKeys.GESTURE, mapOf(IpcKeys.X to 42))
            
            val received = latch.await(2, TimeUnit.SECONDS)
            assert(received) { "Message not received" }
            
            val message = receivedMessage.get()
            assert(message != null && message[IpcKeys.X] == 42) { "Wrong message received" }
            
            // Clean up
            clientThread.interrupt()
            client.disconnect()
            server.stop()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            testsFailed++
        } finally {
            IpcServer.stopAll()
        }
    }
}