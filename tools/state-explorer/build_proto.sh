#!/bin/bash

# Simple build script that copies already-compiled protobuf classes

echo "Setting up state-explorer protobuf classes..."

# Create target directory
mkdir -p target/classes

# Copy already-compiled protobuf classes from main project
if [ -d "../../target/classes/ser" ]; then
    echo "Copying protobuf classes from main project..."
    cp -r ../../target/classes/ser target/classes/
    echo "Protobuf classes copied successfully!"
else
    echo "Error: Protobuf classes not found in main project!"
    echo "Please run 'make compile-java-proto' from the project root first."
    exit 1
fi

echo "Build complete!"
echo "Protobuf classes are in target/classes/"