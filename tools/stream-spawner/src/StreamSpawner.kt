package streamspawner

import java.lang.ProcessBuilder
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

/**
 * Stream Spawner - Launches both heat and day video streams
 * connecting to sych.local endpoints.
 */
class StreamSpawner(
    private val host: String = "sych.local",
    private val debug: Boolean = false
) {
    // WebSocket endpoints for each stream
    private val endpoints = mapOf(
        "heat" to "/ws/ws_rec_video_heat",
        "day" to "/ws/ws_rec_video_day"
    )
    
    // Running processes
    private val processes = mutableMapOf<String, Process>()
    private val shutdownLatch = CountDownLatch(1)
    
    /**
     * Build WebSocket URL for a stream
     */
    private fun buildStreamUrl(streamId: String): String {
        val endpoint = endpoints[streamId] 
            ?: throw IllegalArgumentException("Unknown stream ID: $streamId")
        return "wss://$host$endpoint"
    }
    
    /**
     * Get the classpath for running VideoStreamManager
     */
    private fun getClasspath(): String {
        // We need to include all necessary paths
        val paths = listOf(
            "../../target/classes",           // Compiled classes
            "../../target/kotlin-classes",     // Kotlin classes
            "../../lib/*"                      // Any JAR dependencies
        )
        return paths.joinToString(":")
    }
    
    /**
     * Spawn a single stream process
     */
    private fun spawnStream(streamId: String): Process {
        val streamUrl = buildStreamUrl(streamId)
        
        println("[StreamSpawner] Starting $streamId stream:")
        println("  URL: $streamUrl")
        println("  Domain: $host")
        
        // Build the command to run VideoStreamManager
        val command = listOf(
            "java",
            "-cp", getClasspath(),
            "potatoclient.kotlin.VideoStreamManager",
            streamId,
            streamUrl,
            host
        )
        
        if (debug) {
            println("  Command: ${command.joinToString(" ")}")
        }
        
        val processBuilder = ProcessBuilder(command)
            .redirectErrorStream(false)
        
        // Set working directory to project root
        processBuilder.directory(java.io.File("../.."))
        
        val process = processBuilder.start()
        
        // Start threads to consume output
        thread(name = "$streamId-stdout") {
            process.inputStream.bufferedReader().use { reader ->
                reader.lines().forEach { line ->
                    println("[$streamId] $line")
                }
            }
        }
        
        thread(name = "$streamId-stderr") {
            process.errorStream.bufferedReader().use { reader ->
                reader.lines().forEach { line ->
                    System.err.println("[$streamId ERROR] $line")
                }
            }
        }
        
        return process
    }
    
    /**
     * Start both heat and day streams
     */
    fun start() {
        println("========================================")
        println("Stream Spawner - Starting video streams")
        println("========================================")
        println("Host: $host")
        println()
        
        // Spawn both streams
        try {
            processes["heat"] = spawnStream("heat")
            Thread.sleep(500) // Small delay between launches
            processes["day"] = spawnStream("day")
            
            println()
            println("Both streams started successfully!")
            println("Press Ctrl+C to stop all streams")
            println()
            
            // Add shutdown hook to clean up processes
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                shutdown()
            })
            
            // Wait for shutdown signal
            shutdownLatch.await()
            
        } catch (e: Exception) {
            System.err.println("Failed to start streams: ${e.message}")
            e.printStackTrace()
            shutdown()
        }
    }
    
    /**
     * Shutdown all stream processes
     */
    fun shutdown() {
        println("\n[StreamSpawner] Shutting down streams...")
        
        processes.forEach { (streamId, process) ->
            if (process.isAlive) {
                println("  Stopping $streamId stream...")
                process.destroy()
                
                // Give it time to shut down gracefully
                if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    println("  Force killing $streamId stream...")
                    process.destroyForcibly()
                }
            }
        }
        
        processes.clear()
        shutdownLatch.countDown()
        println("[StreamSpawner] All streams stopped")
    }
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Parse command line arguments
            var host = "sych.local"
            var debug = false
            
            var i = 0
            while (i < args.size) {
                when (args[i]) {
                    "--host", "-h" -> {
                        if (i + 1 < args.size) {
                            host = args[++i]
                        } else {
                            printUsage()
                            return
                        }
                    }
                    "--debug", "-d" -> {
                        debug = true
                    }
                    "--help" -> {
                        printUsage()
                        return
                    }
                    else -> {
                        System.err.println("Unknown argument: ${args[i]}")
                        printUsage()
                        return
                    }
                }
                i++
            }
            
            // Create and start spawner
            val spawner = StreamSpawner(host, debug)
            spawner.start()
        }
        
        private fun printUsage() {
            println("""
                Usage: StreamSpawner [options]
                
                Options:
                  --host, -h <hostname>  Host to connect to (default: sych.local)
                  --debug, -d            Enable debug output
                  --help                 Show this help message
                
                Examples:
                  StreamSpawner                    # Use default host (sych.local)
                  StreamSpawner --host myhost.com  # Use custom host
                  StreamSpawner --debug            # Show debug information
            """.trimIndent())
        }
    }
}