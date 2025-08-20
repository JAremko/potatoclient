#!/bin/bash

# Compile and run ALL IPC tests

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}Compiling and Running ALL IPC Tests${NC}"
echo "======================================"

# Build classpath
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

# Step 1: Compile Java IPC classes
echo -e "${BLUE}[1/8] Compiling Java IPC classes...${NC}"
javac -cp "$CLASSPATH" -d target/classes src/potatoclient/java/ipc/*.java

# Step 2: Compile Kotlin gesture classes
echo -e "${BLUE}[2/8] Compiling Kotlin gesture classes...${NC}"
./tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH" \
  -d target/classes \
  src/potatoclient/kotlin/gestures/*.kt

# Step 3: Compile Kotlin IPC classes
echo -e "${BLUE}[3/8] Compiling Kotlin IPC classes...${NC}"
./tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH" \
  -d target/classes \
  src/potatoclient/kotlin/ipc/*.kt

# Step 4: Compile all test classes
echo -e "${BLUE}[4/8] Compiling all test classes...${NC}"

# Compile JUnit tests
./tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH" \
  -d target/test-classes \
  test/kotlin/ipc/IpcClientServerTest.kt \
  test/kotlin/ipc/IpcManagerTest.kt \
  test/kotlin/ipc/MessageBuildersTest.kt \
  test/kotlin/ipc/TransitSocketCommunicatorTest.kt

# Compile custom test runners
./tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH" \
  -d target/test-classes \
  test/kotlin/ipc/SimpleIpcTest.kt \
  test/kotlin/ipc/ThreadedIpcTest.kt \
  test/kotlin/ipc/SimpleTestRunner.kt \
  test/kotlin/ipc/IpcClientServerTestRunner.kt

echo ""
echo -e "${GREEN}=== Running Tests ===${NC}"

# Step 5: Run SimpleIpcTest
echo -e "${BLUE}[5/8] Running SimpleIpcTest...${NC}"
./tools/kotlin-2.2.0/bin/kotlin \
  -cp "$CLASSPATH" \
  potatoclient.kotlin.ipc.SimpleIpcTest

# Step 6: Run SimpleTestRunner
echo -e "${BLUE}[6/8] Running SimpleTestRunner...${NC}"
./tools/kotlin-2.2.0/bin/kotlin \
  -cp "$CLASSPATH" \
  potatoclient.kotlin.ipc.SimpleTestRunner || echo -e "${RED}SimpleTestRunner had failures${NC}"

# Step 7: Run IpcClientServerTestRunner
echo -e "${BLUE}[7/8] Running IpcClientServerTestRunner...${NC}"
timeout 30 ./tools/kotlin-2.2.0/bin/kotlin \
  -cp "$CLASSPATH" \
  potatoclient.kotlin.ipc.IpcClientServerTestRunner || echo -e "${RED}IpcClientServerTestRunner had issues${NC}"

# Step 8: Download and run JUnit tests
echo -e "${BLUE}[8/8] Running JUnit tests...${NC}"

# Download JUnit Platform Console if not present
JUNIT_VERSION="1.10.0"
JUNIT_JAR="junit-platform-console-standalone-${JUNIT_VERSION}.jar"

if [ ! -f "target/${JUNIT_JAR}" ]; then
    echo "Downloading JUnit Platform Console..."
    curl -s -L "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/${JUNIT_JAR}" \
         -o "target/${JUNIT_JAR}"
fi

# Run JUnit tests
java -cp "target/${JUNIT_JAR}:$CLASSPATH" \
     org.junit.platform.console.launcher.ConsoleLauncher \
     --class-path target/test-classes:target/classes \
     --scan-class-path \
     --include-package potatoclient.kotlin.ipc \
     --fail-if-no-tests \
     --details tree || echo -e "${RED}Some JUnit tests failed${NC}"

echo ""
echo -e "${GREEN}=== Test Execution Complete ===${NC}"