#!/bin/bash

# First ensure we have the protobuf classes compiled
make proto 2>&1 || echo "Proto compilation failed"

# Create target directory
mkdir -p target/classes

# Get the classpath
CP=$(clojure -Spath)

# Add target/classes to classpath
FULL_CP="target/classes:src/potatoclient/java:src:$CP"

echo "Compiling Kotlin with classpath length: ${#FULL_CP}"

# Find all Kotlin files
KOTLIN_FILES=$(find src/potatoclient/kotlin -name "*.kt")

# Compile
tools/kotlin-2.2.0/bin/kotlinc \
    -d target/classes \
    -cp "$FULL_CP" \
    -jvm-target 17 \
    $KOTLIN_FILES 2>&1 | head -100