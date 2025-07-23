#!/bin/bash

# Fix protobuf generated code compatibility issues

set -e

echo "Fixing protobuf compatibility issues..."

# Find all generated Java files
find src/potatoclient/java -name "*.java" -type f | while read -r file; do
    # Remove makeExtensionsImmutable() calls which were removed in protobuf 4.x
    # Use portable sed syntax that works on both GNU and BSD sed
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS (BSD sed) requires backup extension
        sed -i '' '/makeExtensionsImmutable();/d' "$file"
    else
        # Linux (GNU sed)
        sed -i '/makeExtensionsImmutable();/d' "$file"
    fi
done

echo "Protobuf compatibility fixes applied"