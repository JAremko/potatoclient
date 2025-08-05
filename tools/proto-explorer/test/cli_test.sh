#!/bin/bash
# Test script for proto-explorer CLI commands

set -e

echo "Testing proto-explorer CLI..."

# Test find command with JSON output
echo -e "\n1. Testing 'find' command (JSON output):"
OUTPUT=$(bb find "rotary" 2>&1)
if [[ $OUTPUT == *'"found"'* ]] && [[ $OUTPUT == *'"specs"'* ]]; then
    echo "✓ Find command works with JSON output"
else
    echo "✗ Find command failed"
    echo "Output was: $OUTPUT"
    exit 1
fi

# Test spec command with JSON output
echo -e "\n2. Testing 'spec' command (JSON output):"
OUTPUT=$(bb spec ":potatoclient.specs.cmd/ping" 2>&1)
if [[ $OUTPUT == *'"spec"'* ]] && [[ $OUTPUT == *'"definition"'* ]]; then
    echo "✓ Spec command works with JSON output"
else
    echo "✗ Spec command failed"
    echo "Output was: $OUTPUT"
    exit 1
fi

# Test example command with JSON output
echo -e "\n3. Testing 'example' command (JSON output):"
OUTPUT=$(bb example ":potatoclient.specs.cmd/ping" 2>&1)
if [[ $OUTPUT == *'"spec"'* ]] && [[ $OUTPUT == *'"example"'* ]]; then
    echo "✓ Example command works with JSON output"
else
    echo "✗ Example command failed"
    echo "Output was: $OUTPUT"
    exit 1
fi

# Test stats command with JSON output
echo -e "\n4. Testing 'stats' command (JSON output):"
OUTPUT=$(bb stats 2>&1)
if [[ $OUTPUT == *'"total-specs"'* ]] || [[ $OUTPUT == *'"total-packages"'* ]]; then
    echo "✓ Stats command works with JSON output"
else
    echo "✗ Stats command failed"
    echo "Output was: $OUTPUT"
    exit 1
fi

# Test generate-proto-mapping command
echo -e "\n5. Testing 'generate-proto-mapping' command:"
# Create a temp output file
TEMP_FILE="/tmp/test_proto_mapping.clj"
OUTPUT=$(bb generate-proto-mapping output/json-descriptors "$TEMP_FILE")
if [[ $OUTPUT == *"success"* ]] && [[ -f "$TEMP_FILE" ]]; then
    # Check file content
    if grep -q "domain->proto-type" "$TEMP_FILE"; then
        echo "✓ Generate proto mapping command works"
        rm -f "$TEMP_FILE"
    else
        echo "✗ Generated file has wrong content"
        rm -f "$TEMP_FILE"
        exit 1
    fi
else
    echo "✗ Generate proto mapping command failed"
    exit 1
fi

# Test batch processing with JSON output
echo -e "\n6. Testing 'batch' command (JSON output):"
# Create EDN input for batch processing
echo '[{:op :find :pattern "cv"} {:op :spec :spec :potatoclient.specs.cmd/ping}]' > /tmp/batch_input.edn
bb batch < /tmp/batch_input.edn > /tmp/batch_output.json
if [[ -s /tmp/batch_output.json ]] && grep -q '"batch-results"' /tmp/batch_output.json; then
    echo "✓ Batch command works with JSON output"
    rm -f /tmp/batch_input.edn /tmp/batch_output.json
else
    echo "✗ Batch command failed"
    cat /tmp/batch_output.json
    rm -f /tmp/batch_input.edn /tmp/batch_output.json
    exit 1
fi

echo -e "\n✅ All CLI tests passed!"