#!/bin/bash

# Fix protobuf generated code compatibility issues

set -e

echo "Fixing protobuf compatibility issues..."

# Find all generated Java files
find src/potatoclient/java -name "*.java" -type f | while read -r file; do
    # Remove makeExtensionsImmutable() calls which were removed in protobuf 4.x
    sed -i '/makeExtensionsImmutable();/d' "$file"
done

echo "Protobuf compatibility fixes applied"