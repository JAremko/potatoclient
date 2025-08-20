package streamspawner

import potatoclient.kotlin.ipc.IpcServer
import potatoclient.kotlin.ipc.IpcClient
import potatoclient.kotlin.ipc.IpcKeys
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

/**
 * Test Stream Spawner - Creates mock IPC clients that connect to a test server
 * This simulates what the real VideoStreamManager would do.
 */
class TestStreamSpawner(
    private val host: String = "sych.local"
) {
    private val shutdownLatch = CountDownLatch(1)
    private val clients = mutableMapOf<String, IpcClient>()
    
    /**
     * Create a mock stream client
     */
    private fun createStreamClient(streamId: String): IpcClient {
        val serverPid = IpcServer.getCurrentPid()
        val client = IpcClient.create(serverPid, streamId)
        
        println("[$streamId] Client created and connected")
        
        // Simulate stream behavior
        thread(name = "$streamId-simulator") {
            Thread.sleep(1000) // Initial delay
            
            // Send connection event
            client.sendConnectionEvent(IpcKeys.CONNECTED, mapOf<Any, Any>(
                "url" to "wss://$host/ws/ws_rec_video_$streamId",
                "stream-id" to streamId
            ))
            
            // Periodically send events
            var frameCount = 0
            while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(5000) // Every 5 seconds
                    
                    // Send a gesture event (simulating user interaction)
                    client.sendGestureEvent(
                        gestureType = IpcKeys.TAP,
                        x = (100..500).random(),
                        y = (100..300).random(),
                        frameTimestamp = System.currentTimeMillis()
                    )
                    
                    frameCount++
                    if (frameCount % 10 == 0) {
                        client.sendLog(IpcKeys.INFO, "[$streamId] Processed $frameCount frames")
                    }
                    
                } catch (e: InterruptedException) {
                    break
                }
            }
            
            // Send disconnection event
            client.sendConnectionEvent(IpcKeys.DISCONNECTED, mapOf<Any, Any>(
                "reason" to "Shutdown requested",
                "stream-id" to streamId
            ))
        }
        
        return client
    }
    
    /**
     * Start test streams
     */
    fun start() {
        println("========================================")
        println("Test Stream Spawner - Mock IPC Clients")
        println("========================================")
        println("Host: $host")
        println()
        
        // First create IPC servers for each stream
        val heatServer = IpcServer.create("heat", awaitBinding = true)
        val dayServer = IpcServer.create("day", awaitBinding = true)
        
        // Set up message handlers
        heatServer.setOnMessage { message ->
            println("[Server-heat] Received: ${message[IpcKeys.MSG_TYPE]}")
        }
        
        dayServer.setOnMessage { message ->
            println("[Server-day] Received: ${message[IpcKeys.MSG_TYPE]}")
        }
        
        println("IPC Servers created for heat and day streams")
        
        // Create clients
        try {
            clients["heat"] = createStreamClient("heat")
            Thread.sleep(500)
            clients["day"] = createStreamClient("day")
            
            println()
            println("Both mock streams started!")
            println("Press Ctrl+C to stop")
            println()
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                shutdown()
                heatServer.stop()
                dayServer.stop()
            })
            
            // Wait for shutdown
            shutdownLatch.await()
            
        } catch (e: Exception) {
            System.err.println("Failed to start mock streams: ${e.message}")
            e.printStackTrace()
            shutdown()
            heatServer.stop()
            dayServer.stop()
        }
    }
    
    /**
     * Shutdown all clients
     */
    fun shutdown() {
        println("\n[TestStreamSpawner] Shutting down...")
        
        clients.forEach { (streamId, client) ->
            println("  Disconnecting $streamId client...")
            client.disconnect()
        }
        
        clients.clear()
        shutdownLatch.countDown()
        println("[TestStreamSpawner] All clients disconnected")
    }
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val host = if (args.isNotEmpty()) args[0] else "sych.local"
            val spawner = TestStreamSpawner(host)
            spawner.start()
        }
    }
}