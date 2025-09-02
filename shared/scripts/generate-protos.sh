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
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SHARED_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ROOT_DIR="$(cd "$SHARED_DIR/.." && pwd)"
LOCAL_PROTOGEN_DIR="$ROOT_DIR/examples/protogen"

# Output directories for Java - now in shared/src/java
OUTPUT_DIR="$SHARED_DIR/src/java"

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

# Function to apply compatibility fixes for Java
apply_java_fixes() {
    local java_dir="$1"
    
    print_info "Applying Java protobuf compatibility fixes..."
    
    # Run the compatibility fix script if it exists
    if [ -f "$SHARED_DIR/scripts/fix-proto-compatibility.sh" ]; then
        cd "$SHARED_DIR" && ./scripts/fix-proto-compatibility.sh
    fi
    
    print_success "Java compatibility fixes applied"
}

# Function to cleanup Docker resources
cleanup_docker() {
    print_info "Cleaning up Docker resources..."
    
    # Remove the main Docker image (but keep base image)
    if docker image inspect "$DOCKER_IMAGE" &>/dev/null; then
        docker rmi -f "$DOCKER_IMAGE" >/dev/null 2>&1 || true
        print_success "Removed Docker image: $DOCKER_IMAGE"
    fi
    
    # Keep the base Docker image for reuse
    if docker image inspect "$DOCKER_BASE_IMAGE" &>/dev/null; then
        print_info "Keeping base image for faster future builds: $DOCKER_BASE_IMAGE"
    fi
}

# Function to ensure base Docker image exists
ensure_base_image() {
    print_info "Checking for Docker base image..."
    
    if docker image inspect "$DOCKER_BASE_IMAGE" &>/dev/null; then
        print_success "Base image found: $DOCKER_BASE_IMAGE"
        return 0
    fi
    
    print_warning "Base image not found. Need to build it..."
    
    # Check if local protogen exists
    if [ ! -d "$LOCAL_PROTOGEN_DIR" ]; then
        print_error "Local protogen not found at: $LOCAL_PROTOGEN_DIR"
        print_info "Please clone https://github.com/JAremko/protogen.git to examples/protogen"
        return 1
    fi
    
    cd "$LOCAL_PROTOGEN_DIR"
    
    # Try to use pre-built base image tarball if it exists
    BASE_IMAGE_ARCHIVE="jettison-proto-generator-base.tar.gz"
    if [ -f "$BASE_IMAGE_ARCHIVE" ]; then
        print_info "Found base image archive, loading..."
        if docker load < "$BASE_IMAGE_ARCHIVE"; then
            print_success "Base image loaded from archive"
            cd - >/dev/null
            return 0
        else
            print_warning "Failed to load base image from archive, will build from scratch"
        fi
    else
        print_warning "Base image archive not found. Attempting LFS pull..."
        
        # Only attempt LFS pull if we don't have the base image
        if command -v git-lfs &> /dev/null; then
            print_info "Pulling LFS files to get pre-built base image..."
            if timeout 60 git lfs pull --include="$BASE_IMAGE_ARCHIVE" 2>&1; then
                if [ -f "$BASE_IMAGE_ARCHIVE" ]; then
                    print_success "Base image archive downloaded via LFS"
                    if docker load < "$BASE_IMAGE_ARCHIVE"; then
                        print_success "Base image loaded from LFS archive"
                        cd - >/dev/null
                        return 0
                    fi
                fi
            fi
            print_warning "LFS pull failed or timed out"
        fi
    fi
    
    # If we get here, we need to build the base image from scratch
    print_info "Building base image from scratch (this will take a while)..."
    if make base; then
        print_success "Base image built successfully"
    else
        print_error "Failed to build base image"
        cd - >/dev/null
        return 1
    fi
    
    cd - >/dev/null
    return 0
}

# Main execution
main() {
    print_info "PotatoClient Proto Generation (Optimized)"
    print_info "=========================================="
    
    # Check Docker permissions
    print_info "Checking Docker permissions..."
    check_docker_permissions
    docker_status=$?
    
    if [ $docker_status -eq 1 ]; then
        print_error "Docker requires sudo. Please add your user to the docker group:"
        echo "   sudo usermod -aG docker $USER"
        echo "   Then log out and back in"
        exit 1
    elif [ $docker_status -eq 2 ]; then
        print_error "Docker is not installed or not running"
        exit 1
    fi
    
    print_success "Docker is accessible"
    
    # Ensure base image exists (only builds/pulls if needed)
    if ! ensure_base_image; then
        print_error "Failed to ensure base image exists"
        exit 1
    fi
    
    # Check if local protogen directory exists
    if [ ! -d "$LOCAL_PROTOGEN_DIR" ]; then
        print_error "Local protogen directory not found at: $LOCAL_PROTOGEN_DIR"
        print_info "Please ensure examples/protogen exists"
        exit 1
    fi
    
    # Use local protogen directory
    print_info "Using local protogen at: $LOCAL_PROTOGEN_DIR"
    cd "$LOCAL_PROTOGEN_DIR"
    
    # Verify proto files exist
    if [ ! -d "proto" ] || [ -z "$(find proto -name '*.proto' -type f 2>/dev/null)" ]; then
        print_error "No proto files found in $LOCAL_PROTOGEN_DIR/proto"
        exit 1
    fi
    
    print_info "Found $(find proto -name '*.proto' -type f | wc -l) proto files"
    
    # Clean any existing output
    rm -rf output/
    
    # Run make generate which builds the Docker image and generates all bindings
    print_info "Generating proto bindings..."
    if ! make generate; then
        print_error "Proto generation failed"
        exit 1
    fi
    
    print_success "Proto generation completed"
    
    # Copy generated files to shared module
    print_info "Copying generated Java files..."
    
    # Preserve IPC Java files before cleaning
    IPC_BACKUP="/tmp/ipc-backup-$$"
    if [ -d "$OUTPUT_DIR/potatoclient/java/ipc" ]; then
        print_info "Preserving IPC Java files..."
        cp -r "$OUTPUT_DIR/potatoclient/java/ipc" "$IPC_BACKUP"
    fi
    
    # Clean ALL existing Java proto files to prevent stale bindings
    rm -rf "$OUTPUT_DIR/ser" "$OUTPUT_DIR/cmd" "$OUTPUT_DIR/jon" "$OUTPUT_DIR/com" "$OUTPUT_DIR/build" 2>/dev/null || true
    
    # Copy Java files from protogen output
    if [ -d "output/java" ]; then
        mkdir -p "$OUTPUT_DIR"
        cp -r output/java/* "$OUTPUT_DIR/" 2>/dev/null || true
        print_success "Copied Java files to $OUTPUT_DIR/"
    else
        print_error "No Java files found in output directory"
        exit 1
    fi
    
    # Restore IPC Java files
    if [ -d "$IPC_BACKUP" ]; then
        print_info "Restoring IPC Java files..."
        mkdir -p "$OUTPUT_DIR/potatoclient/java"
        cp -r "$IPC_BACKUP" "$OUTPUT_DIR/potatoclient/java/ipc"
        rm -rf "$IPC_BACKUP"
        print_success "IPC Java files restored"
    fi
    
    # Apply compatibility fixes
    apply_java_fixes "$OUTPUT_DIR"
    
    # Set permissions
    chmod -R 777 "$OUTPUT_DIR" 2>/dev/null || true
    
    cd - >/dev/null
    
    # Cleanup only the generated Docker image, not the base
    cleanup_docker
    
    # Summary
    print_info "=========================================="
    print_success "Proto generation completed successfully!"
    print_info "Generated files: $OUTPUT_DIR/"
    print_info "Base image preserved for next run"
}

# Run main function
main "$@"