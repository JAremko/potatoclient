package potatoclient.kotlin.ipc

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import potatoclient.java.ipc.SocketFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Simple test runner for Kotlin IPC without JUnit dependency.
 */
object SimpleTestRunner {
    private var testsPassed = 0
    private var testsFailed = 0
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("Running Kotlin IPC Tests...\n")
        
        testIpcKeys()
        testMessageBuilders()
        testTransitSocketCommunication()
        testIpcManager()
        
        println("\n========================================")
        println("Test Results:")
        println("  Passed: $testsPassed")
        println("  Failed: $testsFailed")
        println("========================================")
        
        System.exit(if (testsFailed > 0) 1 else 0)
    }
    
    private fun testIpcKeys() {
        print("Testing IpcKeys... ")
        try {
            // Test that keywords are created properly
            assert(IpcKeys.MSG_TYPE.toString() == ":msg-type")
            assert(IpcKeys.EVENT.toString() == ":event")
            assert(IpcKeys.WINDOW.toString() == ":window")
            assert(IpcKeys.CONNECTION.toString() == ":connection")
            
            // Test helper functions
            assert(IpcKeys.streamType("heat") == IpcKeys.HEAT)
            assert(IpcKeys.streamType("day") == IpcKeys.DAY)
            assert(IpcKeys.logLevel("ERROR") == IpcKeys.ERROR)
            assert(IpcKeys.logLevel("INFO") == IpcKeys.INFO)
            
            passed()
        } catch (e: Exception) {
            failed(e)
        }
    }
    
    private fun testMessageBuilders() {
        print("Testing MessageBuilders... ")
        try {
            
            // Test window event
            val window = MessageBuilders.windowEvent(
                IpcKeys.RESIZE,
                width = 1920,
                height = 1080
            )
            assert(window[IpcKeys.MSG_TYPE] == IpcKeys.EVENT)
            assert(window[IpcKeys.TYPE] == IpcKeys.WINDOW)
            assert(window[IpcKeys.ACTION] == IpcKeys.RESIZE)
            assert(window[IpcKeys.WIDTH] == 1920)
            assert(window[IpcKeys.HEIGHT] == 1080)
            
            // Test command
            val command = MessageBuilders.rotaryHalt(IpcKeys.DAY)
            assert(command[IpcKeys.MSG_TYPE] == IpcKeys.COMMAND)
            assert(command[IpcKeys.ACTION] == IpcKeys.ROTARY_HALT)
            assert(command[IpcKeys.STREAM_TYPE] == IpcKeys.DAY)
            
            // Test log message
            val log = MessageBuilders.log(
                IpcKeys.ERROR,
                "Test error",
                "test-process"
            )
            assert(log[IpcKeys.MSG_TYPE] == IpcKeys.LOG)
            assert(log[IpcKeys.LEVEL] == IpcKeys.ERROR)
            assert(log[IpcKeys.MESSAGE] == "Test error")
            
            passed()
        } catch (e: Exception) {
            failed(e)
        }
    }
    
    private fun testTransitSocketCommunication() {
        print("Testing TransitSocketCommunicator... ")
        try {
            runBlocking {
                val socketPath = Files.createTempDirectory("test-transit").resolve("test.sock")
                
                val server = TransitSocketCommunicator.createWithPath(socketPath, "server", true)
                val client = TransitSocketCommunicator.createWithPath(socketPath, "client", false)
                
                // Start server in background
                val serverFuture = CompletableFuture.runAsync {
                    runBlocking { server.start() }
                }
                
                delay(100)
                client.start()
                serverFuture.get(2, TimeUnit.SECONDS)
                
                // Test sending message
                val message: Map<Any, Any> = mapOf(
                    IpcKeys.MSG_TYPE to IpcKeys.COMMAND,
                    IpcKeys.ACTION to IpcKeys.ROTARY_HALT,
                    "test" to "value"
                )
                
                client.sendMessage(message)
                
                // Receive and verify
                val received = server.readMessage()
                assert(received != null)
                assert(received!![IpcKeys.MSG_TYPE] == IpcKeys.COMMAND)
                assert(received[IpcKeys.ACTION] == IpcKeys.ROTARY_HALT)
                assert(received["test"] == "value")
                
                // Test message envelope
                val envelopeMsg = client.createMessage(
                    IpcKeys.LOG,
                    mapOf(IpcKeys.MESSAGE to "Test log")
                )
                assert(envelopeMsg[IpcKeys.MSG_TYPE] == IpcKeys.LOG)
                assert(envelopeMsg[IpcKeys.MSG_ID] != null)
                assert(envelopeMsg[IpcKeys.TIMESTAMP] != null)
                
                // Clean up
                server.stop()
                client.stop()
                Files.deleteIfExists(socketPath)
            }
            
            passed()
        } catch (e: Exception) {
            failed(e)
        }
    }
    
    private fun testIpcManager() {
        print("Testing IpcManager... ")
        try {
            val socketPath = Files.createTempDirectory("test-ipc-mgr").resolve("test.sock")
            
            // Create server socket for testing
            val serverSocket = potatoclient.java.ipc.UnixSocketCommunicator(socketPath, true)
            CompletableFuture.runAsync { serverSocket.start() }
            Thread.sleep(100)
            
            // Create and initialize manager
            val manager = IpcManager.createWithPath("test-stream", socketPath)
            manager.initialize()
            
            // Should receive connection event
            val connBytes = serverSocket.receive()
            assert(connBytes != null) { "Should receive connection event" }
            
            // Test sending command
            manager.sendCommand("rotary-halt", mapOf(IpcKeys.STREAM_TYPE to IpcKeys.HEAT))
            
            val commandBytes = serverSocket.receive()
            assert(commandBytes != null) { "Should receive command" }
            
            // Test sending log
            manager.sendLog("INFO", "Test log message")
            
            val logBytes = serverSocket.receive()
            assert(logBytes != null) { "Should receive log message" }
            
            // Test shutdown
            manager.shutdown()
            
            val disconnBytes = serverSocket.receive()
            assert(disconnBytes != null) { "Should receive disconnection event" }
            
            // Clean up
            serverSocket.stop()
            Files.deleteIfExists(socketPath)
            
            passed()
        } catch (e: Exception) {
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