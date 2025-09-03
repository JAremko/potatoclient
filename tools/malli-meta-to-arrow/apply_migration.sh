#!/bin/bash

# Script to apply metadata to arrow migration to potatoclient codebase

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR/../.."
TOOL_PATH="$SCRIPT_DIR"

echo "======================================"
echo "Malli Metadata to Arrow Migration"
echo "======================================"
echo
echo "This will convert :malli/schema metadata to m/=> declarations"
echo "for better instrumentation support with malli.dev/start!"
echo

# Parse arguments
DRY_RUN=false
if [[ "$1" == "--dry-run" ]]; then
    DRY_RUN=true
    echo "[DRY RUN MODE] No files will be modified"
    echo
fi

# Find all files with :malli/schema metadata
echo "Scanning for files with :malli/schema metadata..."
FILES_WITH_SCHEMA=$(cd "$PROJECT_ROOT" && grep -r ":malli/schema" src --include="*.clj" --files-with-matches | sort)
FILE_COUNT=$(echo "$FILES_WITH_SCHEMA" | wc -l)

echo "Found $FILE_COUNT files with :malli/schema metadata"
echo

# Create backup
if [[ "$DRY_RUN" == "false" ]]; then
    BACKUP_DIR="$TOOL_PATH/backup-$(date +%Y%m%d-%H%M%S)"
    echo "Creating backup in: $BACKUP_DIR"
    mkdir -p "$BACKUP_DIR"
    
    # Backup all files
    for file in $FILES_WITH_SCHEMA; do
        file_dir=$(dirname "$file")
        mkdir -p "$BACKUP_DIR/$file_dir"
        cp "$PROJECT_ROOT/$file" "$BACKUP_DIR/$file"
    done
    echo "Backup complete"
    echo
fi

# Run migration
echo "Running migration..."
echo

cd "$TOOL_PATH"

if [[ "$DRY_RUN" == "true" ]]; then
    clojure -M:run -i "$PROJECT_ROOT/src" -o "$PROJECT_ROOT/src" --dry-run --verbose
else
    clojure -M:run -i "$PROJECT_ROOT/src" -o "$PROJECT_ROOT/src" --verbose --backup
fi

echo
echo "======================================"
echo "Migration Complete!"
echo "======================================"
echo

if [[ "$DRY_RUN" == "false" ]]; then
    echo "Next steps:"
    echo "1. Review the changes with: git diff"
    echo "2. Run tests with: make test"
    echo "3. If tests pass, commit the changes"
    echo "4. If tests fail, restore from backup: $BACKUP_DIR"
else
    echo "This was a dry run. To apply changes, run without --dry-run"
fi