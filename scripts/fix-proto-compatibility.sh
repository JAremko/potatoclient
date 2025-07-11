#!/bin/bash

# Fix protobuf 4.x generated code to be compatible with protobuf 3.x runtime

set -e

echo "Fixing protobuf compatibility issues..."

# Find all generated Java files
find src/java -name "*.java" -type f | while read -r file; do
    # Remove @Generated annotation which doesn't exist in protobuf 3.x
    sed -i '/@com.google.protobuf.Generated/d' "$file"
    
    # Remove RuntimeVersion validation which doesn't exist in protobuf 3.x
    sed -i '/com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion/,/);/d' "$file"
    
    # Update protobuf version comment
    sed -i 's/Protobuf Java Version: 4\.[0-9]\+\.[0-9]\+/Protobuf Java Version: 3.15.0/' "$file"
done

echo "Protobuf compatibility fixes applied"