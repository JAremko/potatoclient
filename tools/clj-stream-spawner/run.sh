#!/bin/bash

# Clojure Stream Spawner Runner
# Ensures Kotlin classes are compiled before running

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "========================================="
echo "Clojure Stream Spawner"
echo "========================================="

# Check if Kotlin classes are compiled
if [ ! -f "$PROJECT_ROOT/target/classes/potatoclient/kotlin/VideoStreamManager.class" ]; then
    echo "Compiling Kotlin classes..."
    cd "$PROJECT_ROOT"
    make compile-kotlin
    cd "$SCRIPT_DIR"
fi

# Check if Java classes are compiled
if [ ! -d "$PROJECT_ROOT/target/java-classes/potatoclient/java/ipc" ]; then
    echo "Compiling Java classes..."
    cd "$PROJECT_ROOT"
    make compile-java
    cd "$SCRIPT_DIR"
fi

# Run the Clojure stream spawner
echo "Starting stream spawner..."
echo ""
clj -M:run "$@"