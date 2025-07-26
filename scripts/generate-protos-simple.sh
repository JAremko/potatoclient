#!/usr/bin/env bash

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DOCKER_IMAGE="jettison-proto-generator:latest"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# Output directories for Java
OUTPUT_DIR="$ROOT_DIR/src/potatoclient/java"

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# Function to apply compatibility fixes for Java
apply_java_fixes() {
    local java_dir="$1"
    
    print_info "Applying Java protobuf compatibility fixes..."
    
    # First run the standard fix script if it exists
    if [ -f "$ROOT_DIR/scripts/fix-proto-compatibility.sh" ]; then
        cd "$ROOT_DIR" && ./scripts/fix-proto-compatibility.sh
    fi
    
    # Additional fixes for package names if needed
    if [ -f "$ROOT_DIR/scripts/fix-proto-packages.sh" ]; then
        cd "$ROOT_DIR" && ./scripts/fix-proto-packages.sh
    fi
    
    print_success "Java compatibility fixes applied"
}

# Main execution
main() {
    print_info "PotatoClient Proto Generation using existing Protogen Docker image"
    print_info "=================================================================="
    
    # Check if Docker image exists
    if ! docker image inspect "$DOCKER_IMAGE" &>/dev/null; then
        print_error "Docker image $DOCKER_IMAGE not found!"
        echo "Please run 'make proto' in the jettison project first to build the protogen image"
        exit 1
    fi
    
    print_success "Found protogen Docker image"
    
    # Create output directory
    TEMP_OUTPUT_DIR=$(mktemp -d -t protogen-output.XXXXXX)
    trap "rm -rf $TEMP_OUTPUT_DIR" EXIT
    
    print_info "Using temporary output directory: $TEMP_OUTPUT_DIR"
    
    # Create subdirectories
    docker run --rm -v "$TEMP_OUTPUT_DIR:/workspace/output" "$DOCKER_IMAGE" -c "
        mkdir -p /workspace/output/{java,java-validated}
    "
    
    # Generate standard Java bindings
    print_info "Generating standard Java bindings..."
    docker run --rm -v "$TEMP_OUTPUT_DIR:/workspace/output" "$DOCKER_IMAGE" -c '
        set -e
        mkdir -p /tmp/java_proto_clean
        
        # Copy proto files and remove validate annotations
        for proto in /workspace/proto/*.proto; do
            awk -f /usr/local/bin/proto_cleanup.awk "$proto" > "/tmp/java_proto_clean/$(basename "$proto")"
        done
        
        # Generate all proto files together
        protoc -I/tmp/java_proto_clean \
            --java_out=/workspace/output/java \
            /tmp/java_proto_clean/*.proto
    '
    
    # Generate validated Java bindings
    print_info "Generating validated Java bindings..."
    docker run --rm -v "$TEMP_OUTPUT_DIR:/workspace/output" "$DOCKER_IMAGE" -c '
        set -e
        mkdir -p /tmp/java_proto_val
        
        # Copy proto files and add validate import
        for proto in /workspace/proto/*.proto; do
            cp "$proto" "/tmp/java_proto_val/$(basename "$proto")"
            /usr/local/bin/add-validate-import.sh "/tmp/java_proto_val/$(basename "$proto")"
        done
        
        # Copy validate.proto from protovalidate
        cp -r /opt/protovalidate/proto/protovalidate/buf /tmp/java_proto_val/
        
        # Generate all proto files together with validation
        protoc -I/tmp/java_proto_val \
            --java_out=/workspace/output/java-validated \
            --validate_out="lang=java:/workspace/output/java-validated" \
            /tmp/java_proto_val/*.proto
    '
    
    if [ $? -ne 0 ]; then
        print_error "Proto generation failed"
        exit 1
    fi
    
    print_success "Proto generation completed"
    
    # Copy generated files to PotatoClient structure
    print_info "Cleaning old proto bindings and copying new generated files..."
    
    # Clean ALL existing Java proto files to prevent stale bindings
    print_info "Cleaning Java proto directories..."
    rm -rf "$OUTPUT_DIR/ser" "$OUTPUT_DIR/cmd" "$OUTPUT_DIR/jon" "$OUTPUT_DIR/com" "$OUTPUT_DIR/build" 2>/dev/null || true
    
    # Copy standard Java files
    if [ -d "$TEMP_OUTPUT_DIR/java" ]; then
        mkdir -p "$OUTPUT_DIR"
        cp -r "$TEMP_OUTPUT_DIR/java"/* "$OUTPUT_DIR/" 2>/dev/null || true
        print_success "Copied standard Java files to $OUTPUT_DIR/"
    fi
    
    # Apply compatibility fixes
    apply_java_fixes "$OUTPUT_DIR"
    
    # Set permissions on generated files to 777 so anyone can delete/modify them
    print_info "Setting file permissions on generated files..."
    chmod -R 777 "$OUTPUT_DIR" 2>/dev/null || true
    print_success "File permissions set to 777 for all generated files"
    
    # Summary
    print_info "=================================================================="
    print_success "Proto generation completed successfully!"
    print_info "Generated files have been copied to:"
    echo "  - Standard Java: $OUTPUT_DIR/"
    echo
    print_info "Validated Java bindings are available in: $TEMP_OUTPUT_DIR/java-validated/"
    print_info "To use validation, add to deps.edn:"
    echo '  build.buf/protovalidate/protovalidate-java {:mvn/version "0.3.0"}'
}

# Run main function
main "$@"