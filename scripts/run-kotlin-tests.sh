#!/bin/bash

# Run Kotlin tests for PotatoClient

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Running Kotlin Tests${NC}"
echo "===================="

# First compile the Kotlin tests
echo -e "${YELLOW}Compiling Kotlin tests...${NC}"
clojure -T:build compile-kotlin-tests

# Run Kotlin tests using JUnit console runner
echo -e "${YELLOW}Running tests...${NC}"

# Download JUnit Platform Console Standalone if not present
JUNIT_VERSION="1.10.0"
JUNIT_JAR="junit-platform-console-standalone-${JUNIT_VERSION}.jar"
JUNIT_URL="https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/${JUNIT_JAR}"

if [ ! -f "target/${JUNIT_JAR}" ]; then
    echo "Downloading JUnit Platform Console..."
    mkdir -p target
    curl -L -o "target/${JUNIT_JAR}" "${JUNIT_URL}"
fi

# Build classpath
CLASSPATH="target/classes:target/test-classes:src/potatoclient/java"
CLASSPATH="${CLASSPATH}:$(clojure -Spath -A:test)"
CLASSPATH="${CLASSPATH}:target/${JUNIT_JAR}"

# Run tests
java -cp "${CLASSPATH}" org.junit.platform.console.ConsoleLauncher \
    --scan-classpath=target/test-classes \
    --include-package=potatoclient.transit \
    --details=tree

# Check result
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ All Kotlin tests passed!${NC}"
else
    echo -e "${RED}✗ Some Kotlin tests failed${NC}"
    exit 1
fi