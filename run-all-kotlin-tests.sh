#!/bin/bash

echo "Running all Kotlin IPC tests..."

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

# Compile gesture classes (needed by MessageBuilders)
tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH:target/kotlin-classes:target/java-classes" \
  src/potatoclient/kotlin/gestures/GestureEvent.kt \
  src/potatoclient/kotlin/gestures/FrameDataProvider.kt \
  -d target/kotlin-classes

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

# Compile all tests
echo "Compiling tests..."
tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH:target/kotlin-classes:target/java-classes" \
  test/kotlin/ipc/*.kt \
  -d target/kotlin-classes

# Run each test class
echo ""
echo "========================================" 
echo "Running tests..."
echo "========================================" 
echo ""

TESTS_PASSED=0
TESTS_FAILED=0

for TEST_CLASS in IpcTest IpcClientServerTest MessageBuildersTest ThreadedIpcTest; do
    echo "Running $TEST_CLASS..."
    if java -cp "$CLASSPATH:target/kotlin-classes:target/java-classes" \
         potatoclient.kotlin.ipc.$TEST_CLASS 2>&1; then
        echo "✓ $TEST_CLASS passed"
        ((TESTS_PASSED++))
    else
        echo "✗ $TEST_CLASS failed"
        ((TESTS_FAILED++))
    fi
    echo ""
done

echo "========================================" 
echo "Test Summary:"
echo "  Passed: $TESTS_PASSED"
echo "  Failed: $TESTS_FAILED"
echo "========================================" 

if [ $TESTS_FAILED -gt 0 ]; then
    exit 1
fi