#!/bin/bash

# Update all references from potatoclient.specs to potatoclient.ui-specs

echo "Updating namespace references from specs to ui-specs..."

# Update require statements
find src test -name "*.clj" -type f | while read file; do
    # Skip the old specs files themselves
    if [[ "$file" == *"/specs/"* ]] || [[ "$file" == *"/specs.clj" ]]; then
        continue
    fi
    
    # Update namespace requires
    sed -i 's/\[potatoclient\.specs :as specs\]/[potatoclient.ui-specs :as specs]/g' "$file"
    sed -i 's/\[potatoclient\.specs\]/[potatoclient.ui-specs]/g' "$file"
    sed -i 's/potatoclient\.specs\//potatoclient.ui-specs\//g' "$file"
    
    # Update qualified keywords
    sed -i 's/:potatoclient\.specs\//:potatoclient.ui-specs\//g' "$file"
    sed -i 's/::specs\//::ui-specs\//g' "$file"
done

# Update instrumentation.clj specifically since it uses qualified specs
if [ -f "src/potatoclient/instrumentation.clj" ]; then
    echo "Updating instrumentation.clj..."
    sed -i 's/::specs\//::ui-specs\//g' "src/potatoclient/instrumentation.clj"
    sed -i 's/:potatoclient\.specs\//:potatoclient.ui-specs\//g' "src/potatoclient/instrumentation.clj"
fi

# Update config.clj which uses qualified keywords extensively  
if [ -f "src/potatoclient/config.clj" ]; then
    echo "Updating config.clj..."
    sed -i 's/::specs\/config/::ui-specs\/config/g' "src/potatoclient/config.clj"
    sed -i 's/:potatoclient\.specs\//:potatoclient.ui-specs\//g' "src/potatoclient/config.clj"
fi

echo "Done! Now you can:"
echo "1. Delete src/potatoclient/specs.clj"
echo "2. Delete src/potatoclient/specs/ directory"
echo "3. Run tests to ensure everything works"