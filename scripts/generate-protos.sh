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
DOCKER_BASE_IMAGE="jettison-proto-generator-base:latest"
BASE_IMAGE_ARCHIVE="jettison-proto-generator-base.tar.gz"
PROTOGEN_REPO="https://github.com/JAremko/protogen.git"
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

# Function to check if Docker requires sudo
check_docker_permissions() {
    if docker info >/dev/null 2>&1; then
        return 0
    elif sudo docker info >/dev/null 2>&1; then
        return 1
    else
        return 2
    fi
}

# Function to show Docker setup instructions
show_docker_setup() {
    echo
    print_error "Docker permission issue detected!"
    echo
    echo "To use Docker without sudo, you need to add your user to the docker group:"
    echo
    echo -e "${BLUE}1. Add your user to the docker group:${NC}"
    echo "   sudo usermod -aG docker $USER"
    echo
    echo -e "${BLUE}2. Log out and log back in for the changes to take effect${NC}"
    echo "   (or run: newgrp docker)"
    echo
    echo -e "${BLUE}3. Verify Docker works without sudo:${NC}"
    echo "   docker run hello-world"
    echo
    echo "Alternatively, you can run this script with sudo (not recommended)."
    echo
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

# Function to cleanup Docker resources
cleanup_docker() {
    print_info "Cleaning up Docker resources..."
    
    # Remove the main Docker image
    if docker image inspect "$DOCKER_IMAGE" &>/dev/null; then
        docker rmi -f "$DOCKER_IMAGE" >/dev/null 2>&1 || true
        print_success "Removed Docker image: $DOCKER_IMAGE"
    fi
    
    # Keep the base Docker image for reuse - it doesn't affect our protos
    if docker image inspect "$DOCKER_BASE_IMAGE" &>/dev/null; then
        print_info "Keeping base image for faster future builds: $DOCKER_BASE_IMAGE"
    fi
    
    # Prune any dangling images
    docker image prune -f >/dev/null 2>&1 || true
}

# Function to cleanup temporary directory - ALWAYS runs even if disk is full
cleanup_temp_dir() {
    if [ -n "${MAIN_TEMP_DIR:-}" ] && [ -d "${MAIN_TEMP_DIR}" ]; then
        print_info "Cleaning up temporary directory: $MAIN_TEMP_DIR"
        # Use -f to force removal even if disk is full
        rm -rf "$MAIN_TEMP_DIR" 2>/dev/null || {
            print_warning "Failed to remove temp dir with rm -rf, trying alternative methods..."
            # Try to remove files first to free space
            find "$MAIN_TEMP_DIR" -type f -delete 2>/dev/null || true
            # Then remove directories
            find "$MAIN_TEMP_DIR" -type d -empty -delete 2>/dev/null || true
            # Final attempt
            rmdir "$MAIN_TEMP_DIR" 2>/dev/null || true
        }
        if [ -d "${MAIN_TEMP_DIR}" ]; then
            print_error "Could not fully remove temp directory: $MAIN_TEMP_DIR"
            print_error "Please manually remove it to free disk space"
        else
            print_success "Temporary directory cleaned up"
        fi
    fi
}

# Main execution
main() {
    print_info "PotatoClient Proto Generation using Protogen"
    print_info "============================================"
    
    # Clean up any leftover protogen temp directories first
    print_info "Checking for leftover temporary directories..."
    leftover_dirs=$(find /tmp -maxdepth 1 -type d -name "protogen.*" 2>/dev/null || true)
    if [ -n "$leftover_dirs" ]; then
        print_warning "Found leftover protogen directories, cleaning up..."
        echo "$leftover_dirs" | while read -r dir; do
            if [ -d "$dir" ]; then
                rm -rf "$dir" 2>/dev/null || true
                if [ -d "$dir" ]; then
                    print_warning "Could not remove: $dir"
                else
                    print_info "Removed: $dir"
                fi
            fi
        done
    fi
    
    # Check Docker permissions
    print_info "Checking Docker permissions..."
    check_docker_permissions
    docker_status=$?
    
    if [ $docker_status -eq 1 ]; then
        show_docker_setup
        exit 1
    elif [ $docker_status -eq 2 ]; then
        print_error "Docker is not installed or not running"
        echo "Please install Docker first: https://docs.docker.com/get-docker/"
        exit 1
    fi
    
    print_success "Docker is accessible without sudo"
    
    # Create main temporary directory
    MAIN_TEMP_DIR=$(mktemp -d -t protogen.XXXXXX)
    trap "cleanup_temp_dir; cleanup_docker" EXIT
    
    print_info "Using temporary directory: $MAIN_TEMP_DIR"
    
    # Clone protogen repository
    print_info "Cloning protogen repository..."
    PROTOGEN_DIR="$MAIN_TEMP_DIR/protogen"
    
    # Clone the repository (shallow clone for speed, skip LFS)
    export GIT_LFS_SKIP_SMUDGE=1
    if ! git clone --depth 1 "$PROTOGEN_REPO" "$PROTOGEN_DIR"; then
        print_error "Failed to clone protogen repository"
        exit 1
    fi
    
    # Try to pull LFS files with a timeout
    if command -v git-lfs &> /dev/null; then
        print_info "Attempting to pull LFS files (this may take a while)..."
        cd "$PROTOGEN_DIR"
        if timeout 60 git lfs pull 2>&1; then
            print_success "LFS files pulled successfully"
        else
            print_warning "LFS pull timed out or failed, will build base image from scratch"
            print_info "This will take longer but will still work"
        fi
        cd - >/dev/null
    else
        print_warning "Git LFS not found. Base image will be built from scratch."
        print_info "To speed up future builds: sudo apt-get install git-lfs && git lfs install"
    fi
    
    print_success "Protogen repository cloned"
    
    # Check if base image archive exists and is valid
    BASE_IMAGE_PATH="$PROTOGEN_DIR/$BASE_IMAGE_ARCHIVE"
    if [ -f "$BASE_IMAGE_PATH" ]; then
        # Check if it's a valid gzip file (not an LFS pointer)
        if file "$BASE_IMAGE_PATH" | grep -q "gzip compressed"; then
            print_info "Found valid pre-built base image archive: $BASE_IMAGE_ARCHIVE"
            print_info "Importing base image from archive..."
            
            if docker load < "$BASE_IMAGE_PATH"; then
                print_success "Base image imported successfully"
            else
                print_warning "Failed to import base image, will build from scratch"
            fi
        else
            print_info "Base image archive is an LFS pointer file, not the actual archive"
            print_info "Will build base image from scratch"
        fi
    else
        print_info "No pre-built base image found, will build from scratch"
    fi
    
    # Check if base image already exists
    if docker image inspect "$DOCKER_BASE_IMAGE" &>/dev/null; then
        print_success "Base image already exists, skipping base build"
    fi
    
    # Build Docker image
    print_info "Building Docker image..."
    cd "$PROTOGEN_DIR"
    
    # If the base image archive is invalid, remove it to avoid Makefile issues
    if [ -f "$BASE_IMAGE_ARCHIVE" ] && ! file "$BASE_IMAGE_ARCHIVE" | grep -q "gzip compressed"; then
        print_info "Removing invalid LFS pointer file"
        rm -f "$BASE_IMAGE_ARCHIVE"
    fi
    
    # Build the images
    if ! make build; then
        print_error "Failed to build Docker image"
        exit 1
    fi
    
    cd - >/dev/null
    print_success "Docker image built successfully"
    
    # Create output directory
    TEMP_OUTPUT_DIR=$(mktemp -d -t protogen-output.XXXXXX)
    # Setup trap for temp output directory with inline function
    trap 'if [ -n "${TEMP_OUTPUT_DIR:-}" ] && [ -d "${TEMP_OUTPUT_DIR}" ]; then rm -rf "$TEMP_OUTPUT_DIR" 2>/dev/null || true; fi' EXIT
    
    print_info "Using temporary output directory: $TEMP_OUTPUT_DIR"
    
    # Create subdirectories
    docker run --rm -u "$(id -u):$(id -g)" -v "$TEMP_OUTPUT_DIR:/workspace/output" "$DOCKER_IMAGE" -c "
        mkdir -p /workspace/output/java
    "
    
    # Define the Java script inline to match protogen's approach
    JAVA_SCRIPT='
set -e
# Create a temporary directory for proto files
mkdir -p /tmp/java_proto_buf

# Copy proto files to temporary directory
cp -r /workspace/proto/* /tmp/java_proto_buf/

# Copy buf validate proto definitions to the expected location
mkdir -p /tmp/java_proto_buf/buf/validate
cp /opt/protovalidate/proto/protovalidate/buf/validate/validate.proto /tmp/java_proto_buf/buf/validate/

# First, ensure all proto files have proper imports
cd /tmp/java_proto_buf
for proto in *.proto; do
    if [ -f "$proto" ]; then
        /usr/local/bin/add-validate-import.sh "$proto"
    fi
done

# Generate using standard protoc with the validate.proto available
protoc -I/tmp/java_proto_buf \
    --java_out=/workspace/output/java \
    /tmp/java_proto_buf/*.proto
'
    
    # Generate Java bindings WITH buf.validate annotations
    print_info "Generating Java bindings with buf.validate annotations..."
    # Use the Java generation script that handles buf.validate imports
    docker run --rm -u "$(id -u):$(id -g)" -v "$TEMP_OUTPUT_DIR:/workspace/output" "$DOCKER_IMAGE" -c "$JAVA_SCRIPT"
    
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
    
    # Copy Java files with buf.validate annotations
    if [ -d "$TEMP_OUTPUT_DIR/java" ]; then
        mkdir -p "$OUTPUT_DIR"
        cp -r "$TEMP_OUTPUT_DIR/java"/* "$OUTPUT_DIR/" 2>/dev/null || true
        print_success "Copied Java files to $OUTPUT_DIR/"
        print_info "Note: Using protobuf bindings with buf.validate annotations preserved"
    else
        print_error "No Java files found in output directory"
        exit 1
    fi
    
    # Apply compatibility fixes
    apply_java_fixes "$OUTPUT_DIR"
    
    # Set permissions on generated files to 777 so anyone can delete/modify them
    print_info "Setting file permissions on generated files..."
    chmod -R 777 "$OUTPUT_DIR" 2>/dev/null || true
    print_success "File permissions set to 777 for all generated files"
    
    # Summary
    print_info "============================================"
    print_success "Proto generation completed successfully!"
    print_info "Generated files have been copied to:"
    echo "  - Java: $OUTPUT_DIR/"
    echo
    print_info "Note: Protobuf bindings include buf.validate annotations"
    print_info "Validation is conditional - runs in dev/test, skipped in release"
    
    print_info "Cleaning up temporary files and Docker images..."
}

# Run main function
main "$@"