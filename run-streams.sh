#!/bin/bash

# Simple script to run both video streams connecting to sych.local

echo "Starting Video Streams to sych.local"
echo "====================================="

# Get classpath
CLASSPATH=$(clojure -Spath):target/java-classes:target/kotlin-classes

# Check if VideoStreamManager is compiled
if [ ! -f "target/kotlin-classes/potatoclient/kotlin/VideoStreamManager.class" ]; then
    echo "VideoStreamManager not found. Compiling..."
    make compile-kotlin
fi

# First, compile and run a simple IPC server creator
echo "Creating IPC servers..."
cat > /tmp/create-ipc-servers.kt << 'EOF'
import potatoclient.kotlin.ipc.IpcServer
import potatoclient.kotlin.ipc.IpcKeys

fun main() {
    println("Creating IPC servers for heat and day streams...")
    
    val heatServer = IpcServer.create("heat", true)
    val dayServer = IpcServer.create("day", true)
    
    heatServer.setOnMessage { message ->
        val msgType = message[IpcKeys.MSG_TYPE]
        println("[heat] Received: $msgType")
    }
    
    dayServer.setOnMessage { message ->
        val msgType = message[IpcKeys.MSG_TYPE]
        println("[day] Received: $msgType")
    }
    
    println("IPC servers ready! PID: ${ProcessHandle.current().pid()}")
    println("Starting video streams...")
    
    // Start heat stream in background
    val heatProcess = ProcessBuilder(
        "java", "-cp", System.getProperty("java.class.path"),
        "potatoclient.kotlin.VideoStreamManager",
        "heat", "wss://sych.local/ws/ws_rec_video_heat", "sych.local"
    ).start()
    
    Thread.sleep(500)
    
    // Start day stream in background
    val dayProcess = ProcessBuilder(
        "java", "-cp", System.getProperty("java.class.path"),
        "potatoclient.kotlin.VideoStreamManager",
        "day", "wss://sych.local/ws/ws_rec_video_day", "sych.local"
    ).start()
    
    println("Both streams started!")
    println("Press Ctrl+C to stop")
    
    // Add shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread {
        println("\nShutting down...")
        heatProcess.destroy()
        dayProcess.destroy()
        heatServer.stop()
        dayServer.stop()
    })
    
    // Wait for processes
    heatProcess.waitFor()
    dayProcess.waitFor()
}
EOF

# Compile the IPC server creator
tools/kotlin-2.2.0/bin/kotlinc -cp "$CLASSPATH" /tmp/create-ipc-servers.kt -d /tmp

# Run it
echo ""
echo "Launching streams..."
java -cp "$CLASSPATH:/tmp" CreateIpcServersKt