#!/bin/bash

echo "Running ALL IPC Tests..."
echo "========================"

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

# Compile gesture classes
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

# Compile all tests
echo "Compiling tests..."
tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH:target/kotlin-classes:target/java-classes" \
  test/kotlin/ipc/IpcTest.kt \
  test/kotlin/ipc/IpcClientExtendedTest.kt \
  -d target/kotlin-classes

echo ""
echo "========================================" 
echo "Running all tests..."
echo "========================================" 
echo ""

TOTAL_PASSED=0
TOTAL_FAILED=0

# Run basic IPC tests
echo "1. Basic IPC Tests"
echo "==================="
if java -cp "$CLASSPATH:target/kotlin-classes:target/java-classes" \
     potatoclient.kotlin.ipc.IpcTest 2>&1; then
    ((TOTAL_PASSED+=3))
else
    ((TOTAL_FAILED+=3))
fi

echo ""
echo "2. Extended IPC Client Tests"
echo "============================="
if java -cp "$CLASSPATH:target/kotlin-classes:target/java-classes" \
     potatoclient.kotlin.ipc.IpcClientExtendedTest 2>&1; then
    ((TOTAL_PASSED+=7))
else
    ((TOTAL_FAILED+=7))
fi

echo ""
echo "========================================" 
echo "OVERALL TEST SUMMARY:"
echo "  Total Tests Passed: $TOTAL_PASSED"
echo "  Total Tests Failed: $TOTAL_FAILED"
echo "========================================" 

if [ $TOTAL_FAILED -gt 0 ]; then
    echo "❌ Some tests failed"
    exit 1
else
    echo "✅ All tests passed!"
    exit 0
fi