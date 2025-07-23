#!/bin/bash

# Download and set up Kotlin compiler

set -e

KOTLIN_VERSION="2.2.0"
KOTLIN_DIR="tools/kotlin-${KOTLIN_VERSION}"

if [ -d "$KOTLIN_DIR" ]; then
    echo "Kotlin $KOTLIN_VERSION already installed at $KOTLIN_DIR"
    exit 0
fi

echo "Downloading Kotlin $KOTLIN_VERSION..."
mkdir -p tools
cd tools

# Download Kotlin compiler
wget -q "https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip"

echo "Extracting Kotlin compiler..."
unzip -q "kotlin-compiler-${KOTLIN_VERSION}.zip"
mv kotlinc "kotlin-${KOTLIN_VERSION}"
rm "kotlin-compiler-${KOTLIN_VERSION}.zip"

cd ..

echo "Kotlin $KOTLIN_VERSION installed at $KOTLIN_DIR"
echo ""
echo "To use kotlinc, add to PATH:"
echo "export PATH=\"\$PATH:$(pwd)/$KOTLIN_DIR/bin\""