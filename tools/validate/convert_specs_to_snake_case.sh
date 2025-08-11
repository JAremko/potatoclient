#!/bin/bash

# Convert kebab-case keywords to snake_case in Malli specs
# This script converts field names from :kebab-case to :snake_case

echo "Converting Malli specs from kebab-case to snake_case..."

# List of spec files to convert
SPEC_FILES=$(find /home/jare/git/potatoclient/shared/src/potatoclient/specs -name "*.clj" -type f)

for file in $SPEC_FILES; do
    echo "Processing: $file"
    
    # Create backup
    cp "$file" "${file}.bak"
    
    # Convert kebab-case keywords to snake_case
    # This handles keywords like :actual-space-time -> :actual_space_time
    sed -i 's/:\([a-z][a-z0-9]*\)-\([a-z]\)/:\1_\2/g' "$file"
    
    # Handle multiple dashes in a single keyword
    # Run multiple times to catch all cases
    for i in {1..5}; do
        sed -i 's/:\([a-z][a-z0-9_]*\)-\([a-z]\)/:\1_\2/g' "$file"
    done
    
    # Also convert enum values that are keywords
    # :jon-gui-data-* -> :JON_GUI_DATA_*
    sed -i 's/:jon-gui-data-/:JON_GUI_DATA_/g' "$file"
    
    # Convert remaining dashes in JON enums to underscores
    sed -i 's/:JON_GUI_DATA_\([A-Z0-9_]*\)-/:JON_GUI_DATA_\1_/g' "$file"
    for i in {1..10}; do
        sed -i 's/\(JON_GUI_DATA[A-Z0-9_]*\)-/\1_/g' "$file"
    done
    
    echo "  Converted $file"
done

echo "Conversion complete!"
echo "Backup files created with .bak extension"
echo ""
echo "To verify changes, you can diff the files:"
echo "  diff file.clj file.clj.bak"