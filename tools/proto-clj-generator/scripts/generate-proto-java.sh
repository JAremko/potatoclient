#!/usr/bin/env bash

set -euo pipefail

# Script to generate Java protobuf sources for proto-clj-generator
# This reuses the main project's protogen infrastructure

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOL_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
MAIN_PROJECT_DIR="$(cd "$TOOL_DIR/../.." && pwd)"
OUTPUT_DIR="$TOOL_DIR/src/java"

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

# Main execution
main() {
    print_info "Generating Java protobuf sources for proto-clj-generator"
    
    # Check if main project proto generation script exists
    if [ ! -f "$MAIN_PROJECT_DIR/scripts/generate-protos.sh" ]; then
        print_error "Main project proto generation script not found"
        exit 1
    fi
    
    # First, ensure main project has generated protos
    print_info "Ensuring main project has generated proto files..."
    cd "$MAIN_PROJECT_DIR"
    if [ ! -d "src/potatoclient/java/cmd" ] || [ ! -d "src/potatoclient/java/ser" ]; then
        print_info "Running main project proto generation..."
        ./scripts/generate-protos.sh
    else
        print_success "Main project proto files already exist"
    fi
    
    cd "$TOOL_DIR"
    
    # Clean old Java sources
    print_info "Cleaning old Java sources..."
    rm -rf "$OUTPUT_DIR"
    mkdir -p "$OUTPUT_DIR"
    
    # Copy Java sources from main project
    print_info "Copying Java protobuf sources..."
    for pkg in cmd ser; do
        if [ -d "$MAIN_PROJECT_DIR/src/potatoclient/java/$pkg" ]; then
            print_info "Copying $pkg package..."
            cp -r "$MAIN_PROJECT_DIR/src/potatoclient/java/$pkg" "$OUTPUT_DIR/"
            print_success "Copied $pkg package"
        else
            print_error "Package $pkg not found in main project"
            exit 1
        fi
    done
    
    # Set permissions
    print_info "Setting file permissions..."
    chmod -R 755 "$OUTPUT_DIR"
    
    print_success "Java source generation complete!"
    print_info "Generated sources are in: $OUTPUT_DIR"
    
    # Show what was generated
    echo
    print_info "Generated packages:"
    ls -la "$OUTPUT_DIR/"
    
    echo
    print_info "Total Java files:"
    echo -n "  cmd: "
    find "$OUTPUT_DIR/cmd" -name "*.java" -type f | wc -l
    echo -n "  ser: "
    find "$OUTPUT_DIR/ser" -name "*.java" -type f | wc -l
}

# Run main function
main "$@"