#!/bin/bash

echo "Running ByteBufferPool Tests..."
echo "=============================="

# Get classpath from Clojure
CLASSPATH=$(clojure -Spath)

# Compile ByteBufferPool
echo "Compiling ByteBufferPool..."
mkdir -p target/kotlin-classes
tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH" \
  src/potatoclient/kotlin/ByteBufferPool.kt \
  -d target/kotlin-classes

# Compile test
echo "Compiling ByteBufferPoolTest..."
tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH:target/kotlin-classes" \
  test/kotlin/ByteBufferPoolTest.kt \
  -d target/kotlin-classes

# Run test
echo ""
echo "Running tests..."
echo "----------------"
java -cp "$CLASSPATH:target/kotlin-classes" \
  potatoclient.kotlin.ByteBufferPoolTest