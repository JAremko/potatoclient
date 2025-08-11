#!/bin/bash

# Fix enum values to use UPPER_SNAKE_CASE in Malli specs

echo "Fixing enum values to UPPER_SNAKE_CASE..."

SPEC_FILES=$(find /home/jare/git/potatoclient/shared/src/potatoclient/specs -name "*.clj" -type f)

for file in $SPEC_FILES; do
    echo "Processing: $file"
    
    # Fix :jon_gui_data_ enums to use UPPER_SNAKE_CASE
    sed -i 's/:jon_gui_data_/:JON_GUI_DATA_/g' "$file"
    
    # Fix any remaining lowercase enum patterns to uppercase
    # This handles the rest of the enum name after JON_GUI_DATA_
    sed -i 's/:JON_GUI_DATA_\([a-z_]*\)/:JON_GUI_DATA_\U\1/g' "$file"
    
    # Fix :boolean to boolean?
    sed -i 's/:boolean\]/boolean?]/g' "$file"
    sed -i 's/:boolean$/boolean?/g' "$file"
    
    echo "  Fixed $file"
done

echo "Conversion complete!"