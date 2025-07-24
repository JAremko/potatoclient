#!/bin/bash
# Fix package names in proto files to use 'ser' consistently

echo "Fixing package names in proto files..."

# Replace cmd.* packages with ser
for file in proto/jon_shared_cmd*.proto; do
    if [[ -f "$file" ]]; then
        echo "Processing $file"
        # Replace package cmd.* with package ser
        sed -i 's/^package cmd\.[^;]*;/package ser;/' "$file"
        # Also fix the main cmd package
        sed -i 's/^package cmd;/package ser;/' "$file"
    fi
done

echo "Package names fixed"