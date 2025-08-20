#!/bin/bash

echo "Running Extended IPC Client Tests..."

# Get classpath from Clojure
CLASSPATH=$(clojure -Spath)

# Compile Java IPC classes first
echo "Compiling Java IPC classes..."
mkdir -p target/java-classes
javac -cp "$CLASSPATH" -d target/java-classes src/potatoclient/java/ipc/*.java

# Compile Kotlin IPC classes
echo "Compiling Kotlin IPC classes..."
mkdir -p target/kotlin-classes

# Compile IPC Keys first (no dependencies)
tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH:target/java-classes" \
  src/potatoclient/kotlin/ipc/IpcKeys.kt \
  -d target/kotlin-classes

# Compile gesture classes (needed by MessageBuilders and tests)
tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH:target/kotlin-classes:target/java-classes" \
  src/potatoclient/kotlin/gestures/GestureEvent.kt \
  src/potatoclient/kotlin/gestures/FrameDataProvider.kt \
  src/potatoclient/kotlin/gestures/GestureRecognizer.kt \
  -d target/kotlin-classes 2>/dev/null || true

# Compile MessageBuilders
tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH:target/kotlin-classes:target/java-classes" \
  src/potatoclient/kotlin/ipc/MessageBuilders.kt \
  -d target/kotlin-classes

# Compile IpcServer and IpcClient
tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH:target/kotlin-classes:target/java-classes" \
  src/potatoclient/kotlin/ipc/IpcServer.kt \
  src/potatoclient/kotlin/ipc/IpcClient.kt \
  -d target/kotlin-classes

# Compile test
echo "Compiling extended test..."
tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH:target/kotlin-classes:target/java-classes" \
  test/kotlin/ipc/IpcClientExtendedTest.kt \
  -d target/kotlin-classes

# Run test
echo "Running extended test..."
java -cp "$CLASSPATH:target/kotlin-classes:target/java-classes" \
  potatoclient.kotlin.ipc.IpcClientExtendedTest