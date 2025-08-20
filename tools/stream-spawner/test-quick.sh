#!/bin/bash

# Quick test that runs for a few seconds then exits

cd ../..

CLASSPATH=$(clojure -Spath)

echo "Quick Stream Spawner Test"
echo "========================="
echo ""

# Run test spawner with timeout
timeout 3 java -cp "$CLASSPATH:target/java-classes:target/kotlin-classes:tools/stream-spawner/target" \
    streamspawner.TestStreamSpawner "$@" 2>&1

echo ""
echo "Test completed (3 second timeout)"