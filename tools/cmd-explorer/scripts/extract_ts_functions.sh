#!/bin/bash

# Extract TypeScript function signatures from cmd implementation
# This script analyzes the TypeScript cmd implementation and extracts
# all exported functions with their signatures and locations

TS_DIR="/home/jare/git/potatoclient/examples/web/frontend/ts/cmd"
OUTPUT_FILE="../ts_functions.md"

echo "# TypeScript CMD Functions Reference" > $OUTPUT_FILE
echo "" >> $OUTPUT_FILE
echo "Auto-generated list of all TypeScript cmd functions for implementation reference." >> $OUTPUT_FILE
echo "Generated on: $(date)" >> $OUTPUT_FILE
echo "" >> $OUTPUT_FILE

# Function to process a TypeScript file
process_file() {
    local file="$1"
    local rel_path="${file#$TS_DIR/}"
    
    # Check if file has exported functions
    if grep -q "^export" "$file"; then
        echo "## $rel_path" >> $OUTPUT_FILE
        echo "" >> $OUTPUT_FILE
        
        # Extract function signatures with line numbers
        grep -n "^export" "$file" | while IFS=: read -r line_num content; do
            # Clean up the signature
            if [[ "$content" =~ export[[:space:]]+(async[[:space:]]+)?function[[:space:]]+([a-zA-Z0-9_]+) ]]; then
                func_name="${BASH_REMATCH[2]}"
                # Get the full signature (may span multiple lines)
                signature=$(sed -n "${line_num}p" "$file" | sed 's/^export //')
                
                # If it ends with {, get just up to that point
                signature=$(echo "$signature" | sed 's/ {$//')
                
                echo "- **Line $line_num**: \`$signature\`" >> $OUTPUT_FILE
            elif [[ "$content" =~ export[[:space:]]+const[[:space:]]+([a-zA-Z0-9_]+)[[:space:]]*=[[:space:]]*function ]]; then
                func_name="${BASH_REMATCH[1]}"
                signature=$(sed -n "${line_num}p" "$file" | sed 's/^export //' | sed 's/ {$//')
                echo "- **Line $line_num**: \`$signature\`" >> $OUTPUT_FILE
            fi
        done
        echo "" >> $OUTPUT_FILE
    fi
}

# Process all TypeScript files
echo "Processing TypeScript files in $TS_DIR..."

# Process cmdSender directory (excluding cmdSenderShared.ts as it's infrastructure, not command bindings)
for file in "$TS_DIR"/cmdSender/*.ts; do
    if [ -f "$file" ] && [ "$(basename "$file")" != "cmdSenderShared.ts" ]; then
        process_file "$file"
    fi
done

# Process poi directory
for file in "$TS_DIR"/poi/*.ts; do
    if [ -f "$file" ]; then
        process_file "$file"
    fi
done

# Process root level files
for file in "$TS_DIR"/*.ts; do
    if [ -f "$file" ]; then
        process_file "$file"
    fi
done

echo "Function extraction complete! Output saved to $OUTPUT_FILE"

# Generate summary
total_functions=$(grep -c "^- \*\*Line" $OUTPUT_FILE)
total_files=$(grep -c "^##" $OUTPUT_FILE)

echo "" >> $OUTPUT_FILE
echo "## Summary" >> $OUTPUT_FILE
echo "- Total files analyzed: $total_files" >> $OUTPUT_FILE
echo "- Total functions found: $total_functions" >> $OUTPUT_FILE