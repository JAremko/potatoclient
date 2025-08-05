#!/usr/bin/env bash

# Remove buf.validate dependencies from Java proto files for testing

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVA_DIR="$(cd "$SCRIPT_DIR/../src/java" && pwd)"

echo "Removing buf.validate dependencies from Java files..."

# Remove import statements
find "$JAVA_DIR" -name "*.java" -type f -exec sed -i '/import build\.buf\.validate/d' {} \;

# Remove references to ValidateProto
find "$JAVA_DIR" -name "*.java" -type f -exec sed -i '/build\.buf\.validate\.ValidateProto/d' {} \;

# Remove registry.add lines that reference buf.validate
find "$JAVA_DIR" -name "*.java" -type f -exec sed -i '/registry\.add.*build\.buf\.validate/d' {} \;

echo "Cleaned buf.validate dependencies"