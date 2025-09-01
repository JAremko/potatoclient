#!/bin/bash

# Show changes that will be made by the Malli cleanup tool

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}    Malli Cleanup - Changes Preview         ${NC}"
echo -e "${GREEN}============================================${NC}"
echo

SOURCE_DIRS="../../src ../../shared/src"

# Count total changes
echo -e "${YELLOW}Analyzing changes...${NC}"
RESULT=$(clojure -M:run $SOURCE_DIRS 2>&1 | tail -2)
echo -e "${BLUE}$RESULT${NC}"
echo

# Show specific transformation types
echo -e "${YELLOW}Transformation Types:${NC}"
echo

echo -e "${GREEN}1. Lambda to Partial Conversions:${NC}"
echo "   Before: (fn* [x] (instance? Type x))"
echo "   After:  (partial instance? Type)"
echo

echo -e "${GREEN}2. Lambda to Direct Predicate:${NC}"
echo "   Before: (fn* [x] (string? x))"
echo "   After:  string?"
echo

echo -e "${GREEN}3. Metadata Positioning:${NC}"
echo "   Before: \"Docstring\" {:malli/schema ...}"
echo "   After:  \"Docstring\""
echo "           {:malli/schema ...}"
echo

# Show affected files
echo -e "${YELLOW}Files with lambda transformations:${NC}"
find $SOURCE_DIRS -name "*.clj" -exec grep -l "fn\* \[p1__" {} \; 2>/dev/null | while read file; do
    echo "  - $file"
done
echo

echo -e "${YELLOW}To see detailed changes for a specific file:${NC}"
echo "  clojure -M:run <file-path>"
echo

echo -e "${YELLOW}To apply all changes:${NC}"
echo "  ./apply_migration.sh --apply"
echo