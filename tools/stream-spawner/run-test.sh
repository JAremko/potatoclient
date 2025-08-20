#!/bin/bash

# Test Stream Spawner - Runs mock IPC clients for testing
# This doesn't actually connect to real WebSocket streams

echo "Test Stream Spawner - Mock IPC Clients"
echo "======================================"

# Change to project root
cd ../..

# Get classpath from Clojure
echo "Setting up classpath..."
CLASSPATH=$(clojure -Spath)

# Compile Java IPC classes if needed
if [ ! -d "target/java-classes" ]; then
    echo "Compiling Java IPC classes..."
    mkdir -p target/java-classes
    javac -cp "$CLASSPATH" -d target/java-classes src/potatoclient/java/ipc/*.java
fi

# Compile Kotlin IPC classes if needed
if [ ! -d "target/kotlin-classes" ]; then
    echo "Compiling Kotlin classes..."
    make compile-kotlin
fi

# Compile the test stream spawner
echo "Compiling TestStreamSpawner..."
mkdir -p tools/stream-spawner/target
tools/kotlin-2.2.0/bin/kotlinc \
    -cp "$CLASSPATH:target/java-classes:target/kotlin-classes" \
    tools/stream-spawner/src/TestStreamSpawner.kt \
    -d tools/stream-spawner/target

# Run the test spawner
echo ""
echo "Starting test streams..."
echo ""
java -cp "$CLASSPATH:target/java-classes:target/kotlin-classes:tools/stream-spawner/target" \
    streamspawner.TestStreamSpawner "$@"