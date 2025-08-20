#!/bin/bash

# Stream Spawner - Launches real VideoStreamManager instances
# This attempts to connect to actual WebSocket endpoints

echo "Stream Spawner - Real Video Streams"
echo "===================================="

# Change to project root
cd ../..

# Check if VideoStreamManager exists
if [ ! -f "target/kotlin-classes/potatoclient/kotlin/VideoStreamManager.class" ]; then
    echo "VideoStreamManager not found. Compiling..."
    make compile-kotlin
    
    if [ ! -f "target/kotlin-classes/potatoclient/kotlin/VideoStreamManager.class" ]; then
        echo "ERROR: Failed to compile VideoStreamManager"
        echo "Please run 'make compile-kotlin' from the project root"
        exit 1
    fi
fi

# Get classpath
echo "Setting up classpath..."
CLASSPATH=$(clojure -Spath)

# Compile the stream spawner
echo "Compiling StreamSpawner..."
mkdir -p tools/stream-spawner/target
tools/kotlin-2.2.0/bin/kotlinc \
    -cp "$CLASSPATH:target/java-classes:target/kotlin-classes" \
    tools/stream-spawner/src/StreamSpawner.kt \
    -d tools/stream-spawner/target

# Run the spawner
echo ""
echo "Starting video streams..."
echo "Note: This requires VideoStreamManager to be properly configured"
echo ""
java -cp "$CLASSPATH:target/java-classes:target/kotlin-classes:tools/stream-spawner/target" \
    streamspawner.StreamSpawner "$@"