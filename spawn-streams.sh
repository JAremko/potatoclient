#!/bin/bash

# Spawn both heat and day video streams from sych.local
# This script creates IPC servers and launches VideoStreamManager instances in SEPARATE JVM processes

echo "===========================================" 
echo "Spawning Video Streams from sych.local"
echo "==========================================="
echo ""

# Get classpath
CLASSPATH=$(clojure -Spath):target/java-classes:target/kotlin-classes

# Check if VideoStreamManager is compiled
if [ ! -f "target/kotlin-classes/potatoclient/kotlin/VideoStreamManager.class" ]; then
    echo "Compiling Kotlin classes..."
    make compile-kotlin
fi

# Create a simple Kotlin launcher that ONLY creates IPC servers
# The VideoStreamManager processes will be spawned as completely separate JVMs
cat > /tmp/IpcServerLauncher.kt << 'EOF'
package launcher

import potatoclient.kotlin.ipc.IpcServer
import potatoclient.kotlin.ipc.IpcKeys
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    val host = if (args.isNotEmpty()) args[0] else "sych.local"
    
    println("IPC Server Launcher")
    println("===================")
    println("Host: $host")
    println("PID: ${ProcessHandle.current().pid()}")
    println("")
    
    // Create IPC servers for both streams
    println("Creating IPC servers...")
    val heatServer = IpcServer.create("heat", true)
    val dayServer = IpcServer.create("day", true)
    
    // Set up message handlers
    heatServer.setOnMessage { message ->
        val msgType = message[IpcKeys.MSG_TYPE]
        val level = message.get(IpcKeys.LEVEL) ?: ""
        val text = message.get(IpcKeys.MESSAGE) ?: ""
        
        when (msgType) {
            IpcKeys.LOG -> println("[heat] [$level] $text")
            IpcKeys.EVENT -> {
                val type = message[IpcKeys.TYPE]
                println("[heat] Event: $type")
            }
            else -> println("[heat] $msgType")
        }
    }
    
    dayServer.setOnMessage { message ->
        val msgType = message[IpcKeys.MSG_TYPE]
        val level = message.get(IpcKeys.LEVEL) ?: ""
        val text = message.get(IpcKeys.MESSAGE) ?: ""
        
        when (msgType) {
            IpcKeys.LOG -> println("[day] [$level] $text")
            IpcKeys.EVENT -> {
                val type = message[IpcKeys.TYPE]
                println("[day] Event: $type")
            }
            else -> println("[day] $msgType")
        }
    }
    
    println("IPC servers ready!")
    println("")
    
    val shutdownLatch = CountDownLatch(1)
    
    // Add shutdown hook
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        println("\nShutting down IPC servers...")
        heatServer.stop()
        dayServer.stop()
        println("IPC servers stopped")
        shutdownLatch.countDown()
    })
    
    println("IPC servers running. Press Ctrl+C to stop.")
    
    // Wait forever
    shutdownLatch.await()
}
EOF

# Compile the IPC server launcher
echo "Compiling IPC server launcher..."
tools/kotlin-2.2.0/bin/kotlinc -cp "$CLASSPATH" /tmp/IpcServerLauncher.kt -d /tmp

# Start IPC servers in background
echo "Starting IPC servers..."
java -cp "$CLASSPATH:/tmp" launcher.IpcServerLauncherKt "$@" &
IPC_PID=$!
echo "IPC server PID: $IPC_PID"

# Wait a bit for servers to start
sleep 2

# Now spawn the VideoStreamManager processes as COMPLETELY SEPARATE JVMs
# Pass the IPC server PID as the parent PID
echo ""
echo "Launching heat stream (separate JVM process)..."
java -cp "$CLASSPATH" \
    -Djava.awt.headless=false \
    -Dgstreamer.plugin.path=/usr/lib/x86_64-linux-gnu/gstreamer-1.0 \
    potatoclient.kotlin.VideoStreamManager \
    heat \
    "wss://sych.local/ws/ws_rec_video_heat" \
    sych.local \
    $IPC_PID &
HEAT_PID=$!
echo "Heat stream PID: $HEAT_PID"

sleep 1

echo "Launching day stream (separate JVM process)..."
java -cp "$CLASSPATH" \
    -Djava.awt.headless=false \
    -Dgstreamer.plugin.path=/usr/lib/x86_64-linux-gnu/gstreamer-1.0 \
    potatoclient.kotlin.VideoStreamManager \
    day \
    "wss://sych.local/ws/ws_rec_video_day" \
    sych.local \
    $IPC_PID &
DAY_PID=$!
echo "Day stream PID: $DAY_PID"

echo ""
echo "All processes launched!"
echo "IPC Server: $IPC_PID"
echo "Heat Stream: $HEAT_PID"
echo "Day Stream: $DAY_PID"
echo ""
echo "Press Ctrl+C to stop all streams"

# Function to cleanup
cleanup() {
    echo ""
    echo "Stopping all processes..."
    
    # Kill video streams first
    if kill -0 $HEAT_PID 2>/dev/null; then
        echo "Stopping heat stream..."
        kill $HEAT_PID
    fi
    
    if kill -0 $DAY_PID 2>/dev/null; then
        echo "Stopping day stream..."
        kill $DAY_PID
    fi
    
    # Give them time to shut down
    sleep 2
    
    # Force kill if needed
    if kill -0 $HEAT_PID 2>/dev/null; then
        kill -9 $HEAT_PID
    fi
    
    if kill -0 $DAY_PID 2>/dev/null; then
        kill -9 $DAY_PID
    fi
    
    # Stop IPC server
    if kill -0 $IPC_PID 2>/dev/null; then
        echo "Stopping IPC server..."
        kill $IPC_PID
        sleep 1
        if kill -0 $IPC_PID 2>/dev/null; then
            kill -9 $IPC_PID
        fi
    fi
    
    echo "All processes stopped"
    exit 0
}

# Set up signal handler
trap cleanup SIGINT SIGTERM

# Wait for processes
wait $IPC_PID $HEAT_PID $DAY_PID