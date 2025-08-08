#!/usr/bin/env bash

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
PROTOGEN_REPO="https://github.com/JAremko/protogen.git"
DOCKER_IMAGE="jettison-proto-generator:latest"

# Output directory for Java sources
OUTPUT_DIR="$PROJECT_DIR/src/java"

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

# Function to cleanup temporary directory
cleanup_temp_dir() {
    if [ -n "${TEMP_DIR:-}" ] && [ -d "${TEMP_DIR}" ]; then
        print_info "Cleaning up temporary directory: $TEMP_DIR"
        rm -rf "$TEMP_DIR" 2>/dev/null || true
    fi
}

# Main execution
main() {
    print_info "Buff-Validate Proto Generation"
    print_info "==============================="
    
    # Check Docker
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker is not running or not installed"
        exit 1
    fi
    
    # Create temporary directory
    TEMP_DIR=$(mktemp -d -t buff-validate-protogen.XXXXXX)
    trap cleanup_temp_dir EXIT
    
    print_info "Using temporary directory: $TEMP_DIR"
    
    # Clone protogen repository
    print_info "Cloning protogen repository..."
    export GIT_LFS_SKIP_SMUDGE=1
    if ! git clone --depth 1 "$PROTOGEN_REPO" "$TEMP_DIR/protogen"; then
        print_error "Failed to clone protogen repository"
        exit 1
    fi
    
    # Try to pull LFS files
    if command -v git-lfs &> /dev/null; then
        print_info "Attempting to pull LFS files..."
        cd "$TEMP_DIR/protogen"
        timeout 60 git lfs pull 2>&1 || print_warning "LFS pull failed, will build from scratch"
        cd - >/dev/null
    fi
    
    # Check for base image archive
    BASE_IMAGE_PATH="$TEMP_DIR/protogen/jettison-proto-generator-base.tar.gz"
    if [ -f "$BASE_IMAGE_PATH" ] && file "$BASE_IMAGE_PATH" | grep -q "gzip compressed"; then
        print_info "Importing base Docker image..."
        docker load < "$BASE_IMAGE_PATH" || print_warning "Failed to import base image"
    fi
    
    # Build Docker image
    print_info "Building Docker image..."
    cd "$TEMP_DIR/protogen"
    if ! make build; then
        print_error "Failed to build Docker image"
        exit 1
    fi
    cd - >/dev/null
    
    # Create output directory
    TEMP_OUTPUT=$(mktemp -d -t buff-validate-output.XXXXXX)
    trap 'rm -rf "$TEMP_OUTPUT" 2>/dev/null || true' EXIT
    
    # Generate Java with buf.validate
    print_info "Generating Java sources with buf.validate annotations..."
    
    # Mount the protogen proto directory for the container
    mkdir -p "$TEMP_OUTPUT/java"
    
    # Java generation script (matches protogen approach)
    JAVA_SCRIPT='
set -e
mkdir -p /tmp/java_proto_buf
cp -r /workspace/proto/* /tmp/java_proto_buf/
mkdir -p /tmp/java_proto_buf/buf/validate
cp /opt/protovalidate/proto/protovalidate/buf/validate/validate.proto /tmp/java_proto_buf/buf/validate/

cd /tmp/java_proto_buf
for proto in *.proto; do
    if [ -f "$proto" ]; then
        /usr/local/bin/add-validate-import.sh "$proto"
    fi
done

protoc -I/tmp/java_proto_buf \
    --java_out=/workspace/output/java \
    /tmp/java_proto_buf/*.proto
'
    
    # Mount both proto directory and output directory
    docker run --rm -u "$(id -u):$(id -g)" \
        -v "$TEMP_DIR/protogen/proto:/workspace/proto:ro" \
        -v "$TEMP_OUTPUT:/workspace/output" \
        "$DOCKER_IMAGE" -c "$JAVA_SCRIPT"
    
    # Clean old Java sources and copy new ones
    print_info "Installing generated Java sources..."
    rm -rf "$OUTPUT_DIR"
    mkdir -p "$OUTPUT_DIR"
    
    if [ -d "$TEMP_OUTPUT/java" ]; then
        cp -r "$TEMP_OUTPUT/java"/* "$OUTPUT_DIR/"
        
        # Set permissions
        chmod -R 755 "$OUTPUT_DIR"
        
        print_success "Java sources installed to $OUTPUT_DIR"
    else
        print_error "No Java files generated"
        exit 1
    fi
    
    # Summary
    print_info "==============================="
    print_success "Proto generation completed!"
    print_info "Generated Java sources with buf.validate support"
    print_info "Location: $OUTPUT_DIR"
    print_info ""
    print_info "Next step: Run 'make compile' to compile Java sources"
}

# Run main function
main "$@"