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

# Use compatible protoc version if available, otherwise system protoc
if [[ -f "tools/protoc-3.15.0/bin/protoc" ]]; then
    PROTOC="tools/protoc-3.15.0/bin/protoc"
    echo "Using protoc 3.15.0 for compatibility"
else
    PROTOC="protoc"
    echo "Warning: Using system protoc (may cause compatibility issues)"
    echo "Run './scripts/download-protoc-3.15.sh' to download compatible protoc"
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