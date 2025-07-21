#!/bin/bash

# Download protoc 29.5 for compatibility with protobuf-java 4.29.5

set -e

PROTOC_VERSION="29.5"
TOOLS_DIR="tools"
PROTOC_DIR="$TOOLS_DIR/protoc-$PROTOC_VERSION"

# Detect platform
PLATFORM="unknown"
ARCH=$(uname -m)

if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    if [[ "$ARCH" == "x86_64" ]]; then
        PLATFORM="linux-x86_64"
    elif [[ "$ARCH" == "aarch64" ]]; then
        PLATFORM="linux-aarch_64"
    fi
elif [[ "$OSTYPE" == "darwin"* ]]; then
    if [[ "$ARCH" == "x86_64" ]]; then
        PLATFORM="osx-x86_64"
    elif [[ "$ARCH" == "arm64" ]]; then
        PLATFORM="osx-aarch_64"
    fi
fi

if [[ "$PLATFORM" == "unknown" ]]; then
    echo "Error: Unsupported platform: $OSTYPE $ARCH"
    exit 1
fi

# Create tools directory
mkdir -p "$TOOLS_DIR"

# Check if already downloaded
if [[ -f "$PROTOC_DIR/bin/protoc" ]]; then
    echo "protoc $PROTOC_VERSION already downloaded"
    "$PROTOC_DIR/bin/protoc" --version
    exit 0
fi

# Download URL
PROTOC_ZIP="protoc-$PROTOC_VERSION-$PLATFORM.zip"
DOWNLOAD_URL="https://github.com/protocolbuffers/protobuf/releases/download/v$PROTOC_VERSION/$PROTOC_ZIP"

echo "Downloading protoc $PROTOC_VERSION for $PLATFORM..."
echo "URL: $DOWNLOAD_URL"

# Download
cd "$TOOLS_DIR"
curl -L -O "$DOWNLOAD_URL"

# Extract
echo "Extracting..."
mkdir -p "protoc-$PROTOC_VERSION"
cd "protoc-$PROTOC_VERSION"
unzip -q "../$PROTOC_ZIP"
cd ../..

# Clean up zip
rm "$TOOLS_DIR/$PROTOC_ZIP"

# Make executable
chmod +x "$PROTOC_DIR/bin/protoc"

# Verify
echo "Installed protoc:"
"$PROTOC_DIR/bin/protoc" --version