#!/bin/bash

# Test Video Streams with GStreamer test patterns

echo "Test Video Streams - GStreamer Test Patterns"
echo "============================================"

# Change to project root
cd ../..

# Get classpath
echo "Setting up classpath..."
CLASSPATH=$(clojure -Spath)

# Compile the test video streams
echo "Compiling TestVideoStreams..."
mkdir -p tools/stream-spawner/target
tools/kotlin-2.2.0/bin/kotlinc \
    -cp "$CLASSPATH:target/java-classes:target/kotlin-classes" \
    tools/stream-spawner/src/TestVideoStreams.kt \
    -d tools/stream-spawner/target

# Run it
echo ""
echo "Starting test video streams..."
echo ""
java -cp "$CLASSPATH:target/java-classes:target/kotlin-classes:tools/stream-spawner/target" \
    streamspawner.TestVideoStreams "$@"