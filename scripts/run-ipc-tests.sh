#!/bin/bash

# Run IPC Tests for PotatoClient

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Running IPC Tests${NC}"
echo "===================="

# Set up classpath
CLASSPATH="target/classes:target/test-classes"
CLASSPATH="$CLASSPATH:/home/jare/.m2/repository/com/cognitect/transit-java/1.0.371/transit-java-1.0.371.jar"
CLASSPATH="$CLASSPATH:/home/jare/.m2/repository/org/msgpack/msgpack/0.6.12/msgpack-0.6.12.jar"
CLASSPATH="$CLASSPATH:/home/jare/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar"
CLASSPATH="$CLASSPATH:/home/jare/.m2/repository/org/javassist/javassist/3.18.1-GA/javassist-3.18.1-GA.jar"
CLASSPATH="$CLASSPATH:tools/kotlin-2.2.0/lib/kotlinx-coroutines-core-jvm.jar"

# Compile Java IPC classes
echo -e "${YELLOW}Compiling Java IPC classes...${NC}"
javac -d target/classes src/potatoclient/java/ipc/*.java

# Compile Kotlin IPC classes (excluding IpcManager and MessageBuilders which have dependencies)
echo -e "${YELLOW}Compiling Kotlin IPC classes...${NC}"
./tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH" \
  -d target/classes \
  src/potatoclient/kotlin/ipc/IpcKeys.kt \
  src/potatoclient/kotlin/ipc/IpcClient.kt \
  src/potatoclient/kotlin/ipc/IpcServer.kt \
  src/potatoclient/kotlin/ipc/TransitSocketCommunicator.kt

# Compile test classes
echo -e "${YELLOW}Compiling test classes...${NC}"
./tools/kotlin-2.2.0/bin/kotlinc \
  -cp "$CLASSPATH" \
  -d target/test-classes \
  test/kotlin/ipc/SimpleIpcTest.kt

# Run tests
echo -e "${YELLOW}Running tests...${NC}"
./tools/kotlin-2.2.0/bin/kotlin \
  -cp "$CLASSPATH" \
  potatoclient.kotlin.ipc.SimpleIpcTest

echo -e "${GREEN}IPC Tests Completed${NC}"