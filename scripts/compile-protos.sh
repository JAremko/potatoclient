#!/bin/bash

# Compile proto files while removing buf.validate annotations

set -e

PROTO_DIR="proto"
TEMP_DIR="temp_proto"
OUTPUT_DIR="src/java"

# Create temp directory
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"

# Preprocess proto files to remove buf.validate annotations
echo "Preprocessing proto files..."
python3 scripts/preprocess_protos.py

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Use protoc from lein-protodeps if available, otherwise system protoc
PROTOC_PATH="$HOME/.lein-protodeps/protoc-installations/protoc-3.25.1-linux-x86_64/bin/protoc"

if [ -f "$PROTOC_PATH" ]; then
    echo "Using protoc from lein-protodeps"
    PROTOC="$PROTOC_PATH"
else
    echo "Using system protoc"
    PROTOC="protoc"
fi

# Compile proto files
echo "Compiling proto files..."
"$PROTOC" \
    --proto_path="$TEMP_DIR" \
    --java_out="$OUTPUT_DIR" \
    "$TEMP_DIR"/*.proto

# Clean up
rm -rf "$TEMP_DIR"

echo "Proto compilation complete. Java files generated in $OUTPUT_DIR"