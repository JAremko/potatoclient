#!/bin/bash

echo "This script will delete all legacy specs and test files."
echo "Make sure you have committed your changes before running this!"
echo ""
read -p "Are you sure you want to proceed? (y/N): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Aborted."
    exit 1
fi

echo "Deleting legacy spec files..."

# Delete the main legacy specs file
rm -f src/potatoclient/specs.clj
echo "✓ Deleted src/potatoclient/specs.clj"

# Delete the entire specs directory
rm -rf src/potatoclient/specs/
echo "✓ Deleted src/potatoclient/specs/ directory"

# Delete legacy test files
echo ""
echo "Deleting legacy test files..."

# Files that use old action/params format
rm -f test/potatoclient/protobuf_handler_test.clj
rm -f test/potatoclient/transit_handler_test.clj
rm -f test/potatoclient/transit_handlers_working_test.clj
rm -f test/potatoclient/transit_keyword_test.clj
rm -f test/potatoclient/transit_minimal_test.clj
rm -f test/potatoclient/transit_simple_test.clj
rm -f test/potatoclient/transit/keyword_conversion_test.clj
rm -f test/potatoclient/transit/metadata_command_test.clj

echo "✓ Deleted legacy test files"

# Delete all .skip files
echo ""
echo "Deleting .skip files..."
find test -name "*.clj.skip" -type f -delete
echo "✓ Deleted all .skip files"

# Delete the test utils protobuf file if it's legacy
rm -f test/potatoclient/test_utils/protobuf.clj
echo "✓ Deleted test_utils/protobuf.clj"

# Delete other legacy files
rm -f src/potatoclient/transit/proto_type_registry.clj
echo "✓ Deleted transit/proto_type_registry.clj"

echo ""
echo "Legacy cleanup complete!"
echo ""
echo "Next steps:"
echo "1. Run 'make test' to ensure nothing is broken"
echo "2. Check for any remaining references to deleted files"
echo "3. Commit the cleanup"