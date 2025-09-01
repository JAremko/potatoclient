#!/bin/bash

# Malli Cleanup Migration Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}       Malli Cleanup Migration Tool         ${NC}"
echo -e "${GREEN}============================================${NC}"
echo

# Configuration
BACKUP_DIR="backup-$(date +%Y%m%d-%H%M%S)"
SOURCE_DIRS="../../src ../../shared/src"
OUTPUT_DIR="migrated"

# Parse command line arguments
DRY_RUN=true
ANALYZE_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --apply)
            DRY_RUN=false
            shift
            ;;
        --analyze)
            ANALYZE_ONLY=true
            shift
            ;;
        --help)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  --analyze    Run analysis only"
            echo "  --apply      Apply transformations (default is dry-run)"
            echo "  --help       Show this help message"
            echo ""
            echo "By default, runs in dry-run mode showing what would be changed."
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            exit 1
            ;;
    esac
done

# Step 1: Run analysis
echo -e "${YELLOW}Step 1: Analyzing codebase...${NC}"
clojure -M:run --analyze $SOURCE_DIRS

echo
echo -e "${GREEN}Analysis complete! Check malli-analysis-report.txt for details.${NC}"
echo

if [ "$ANALYZE_ONLY" = true ]; then
    echo "Analysis only mode - exiting."
    exit 0
fi

# Step 2: Dry run or apply
if [ "$DRY_RUN" = true ]; then
    echo -e "${YELLOW}Step 2: Running dry-run...${NC}"
    echo "This will show what changes would be made without modifying files."
    echo
    
    clojure -M:run $SOURCE_DIRS | tee dry-run-report.txt
    
    echo
    echo -e "${GREEN}Dry run complete!${NC}"
    echo "Review dry-run-report.txt for details."
    echo
    echo "To apply these changes, run: $0 --apply"
else
    # Create backup
    echo -e "${YELLOW}Step 2: Creating backup...${NC}"
    mkdir -p "$BACKUP_DIR"
    
    for dir in $SOURCE_DIRS; do
        if [ -d "$dir" ]; then
            echo "Backing up $dir..."
            cp -r "$dir" "$BACKUP_DIR/"
        fi
    done
    
    echo -e "${GREEN}Backup created in $BACKUP_DIR${NC}"
    echo
    
    # Apply transformations
    echo -e "${YELLOW}Step 3: Applying transformations...${NC}"
    clojure -M:run --transform -o "$OUTPUT_DIR" $SOURCE_DIRS
    
    echo
    echo -e "${GREEN}============================================${NC}"
    echo -e "${GREEN}        Migration Complete!                 ${NC}"
    echo -e "${GREEN}============================================${NC}"
    echo
    echo "Transformed files are in: $OUTPUT_DIR"
    echo "Original files backed up in: $BACKUP_DIR"
    echo
    echo "Next steps:"
    echo "1. Review the transformed files in $OUTPUT_DIR"
    echo "2. Run tests to ensure everything works"
    echo "3. If satisfied, copy transformed files over originals:"
    echo "   cp -r $OUTPUT_DIR/* ../../"
fi