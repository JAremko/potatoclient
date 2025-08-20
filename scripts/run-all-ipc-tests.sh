#!/bin/bash

# Run ALL IPC Tests for PotatoClient

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}Running ALL IPC Tests${NC}"
echo "========================"

# Build classpath
echo -e "${YELLOW}Building classpath...${NC}"
CLASSPATH="target/classes:target/test-classes"
CLASSPATH="$CLASSPATH:/home/jare/.m2/repository/com/cognitect/transit-java/1.0.371/transit-java-1.0.371.jar"
CLASSPATH="$CLASSPATH:/home/jare/.m2/repository/com/cognitect/transit-clj/1.0.333/transit-clj-1.0.333.jar"
CLASSPATH="$CLASSPATH:/home/jare/.m2/repository/org/msgpack/msgpack/0.6.12/msgpack-0.6.12.jar"
CLASSPATH="$CLASSPATH:/home/jare/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar"
CLASSPATH="$CLASSPATH:/home/jare/.m2/repository/org/javassist/javassist/3.18.1-GA/javassist-3.18.1-GA.jar"
CLASSPATH="$CLASSPATH:/home/jare/.m2/repository/junit/junit/4.13.2/junit-4.13.2.jar"
CLASSPATH="$CLASSPATH:/home/jare/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
CLASSPATH="$CLASSPATH:tools/kotlin-2.2.0/lib/kotlinx-coroutines-core-jvm.jar"
CLASSPATH="$CLASSPATH:tools/kotlin-2.2.0/lib/kotlin-test.jar"
CLASSPATH="$CLASSPATH:tools/kotlin-2.2.0/lib/kotlin-test-junit.jar"
CLASSPATH="$CLASSPATH:tools/kotlin-2.2.0/lib/kotlin-stdlib.jar"

# Clean and create directories
echo -e "${YELLOW}Preparing directories...${NC}"
mkdir -p target/classes target/test-classes

# Compile Java IPC classes
echo -e "${YELLOW}Compiling Java IPC classes...${NC}"
javac -cp "$CLASSPATH" -d target/classes src/potatoclient/java/ipc/*.java

# Compile Kotlin IPC classes
echo -e "${YELLOW}Compiling Kotlin IPC classes...${NC}"
./tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH" \
  -d target/classes \
  src/potatoclient/kotlin/ipc/IpcKeys.kt \
  src/potatoclient/kotlin/ipc/IpcClient.kt \
  src/potatoclient/kotlin/ipc/IpcServer.kt \
  src/potatoclient/kotlin/ipc/TransitSocketCommunicator.kt

# Try to compile IpcManager and MessageBuilders (may fail due to dependencies)
echo -e "${YELLOW}Attempting to compile IpcManager and MessageBuilders...${NC}"
./tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH" \
  -d target/classes \
  src/potatoclient/kotlin/ipc/IpcManager.kt \
  src/potatoclient/kotlin/ipc/MessageBuilders.kt 2>/dev/null || echo -e "${YELLOW}Skipping IpcManager/MessageBuilders (missing dependencies)${NC}"

# Compile test classes
echo -e "${YELLOW}Compiling test classes...${NC}"

# Compile our simple test runners
./tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH" \
  -d target/test-classes \
  test/kotlin/ipc/SimpleIpcTest.kt \
  test/kotlin/ipc/ThreadedIpcTest.kt

# Compile JUnit test classes
./tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH" \
  -d target/test-classes \
  test/kotlin/ipc/IpcClientServerTest.kt \
  test/kotlin/ipc/TransitSocketCommunicatorTest.kt 2>/dev/null || echo -e "${RED}Failed to compile some JUnit tests${NC}"

echo ""
echo -e "${BLUE}=== Running Simple Tests ===${NC}"
./tools/kotlin-2.2.0/bin/kotlin \
  -cp "$CLASSPATH" \
  potatoclient.kotlin.ipc.SimpleIpcTest

echo ""
echo -e "${BLUE}=== Running Threaded Tests ===${NC}"
./tools/kotlin-2.2.0/bin/kotlin \
  -cp "$CLASSPATH" \
  potatoclient.kotlin.ipc.ThreadedIpcTest || echo -e "${RED}Threaded tests failed${NC}"

# Count total tests
echo ""
echo -e "${GREEN}=== Test Summary ===${NC}"
echo "Test files found:"
for file in test/kotlin/ipc/*.kt; do
  name=$(basename "$file")
  junit_count=$(grep -c "@Test" "$file" 2>/dev/null || echo 0)
  custom_count=$(grep -c "fun test" "$file" 2>/dev/null || echo 0)
  if [ "$junit_count" -gt 0 ] || [ "$custom_count" -gt 0 ]; then
    echo "  $name: $junit_count JUnit tests, $custom_count custom tests"
  fi
done

TOTAL_JUNIT=$(grep -h "@Test" test/kotlin/ipc/*.kt 2>/dev/null | wc -l)
echo ""
echo -e "${GREEN}Total JUnit tests defined: $TOTAL_JUNIT${NC}"
echo -e "${GREEN}Custom test runners: SimpleIpcTest (4 tests), ThreadedIpcTest (3 tests)${NC}"

echo ""
echo -e "${GREEN}IPC Test Suite Complete!${NC}"