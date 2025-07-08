#!/bin/bash

# Development build script for macOS
# This creates an unsigned .app bundle for local testing

set -e

echo "Building PotatoClient for macOS (Development)"

# Extract version
VERSION=$(grep defproject project.clj | sed -E 's/.*"([0-9]+\.[0-9]+\.[0-9]+)".*/\1/')
echo "Version: $VERSION"

# Build the JAR
echo "Building JAR..."
lein uberjar

# Create app bundle structure
APP_NAME="PotatoClient"
APP_DIR="$APP_NAME.app"
rm -rf "$APP_DIR"
mkdir -p "$APP_DIR/Contents/MacOS"
mkdir -p "$APP_DIR/Contents/Resources"
mkdir -p "$APP_DIR/Contents/runtime"

# Copy resources
cp .github/actions/macos-assets/Info.plist "$APP_DIR/Contents/"
cp .github/actions/macos-assets/potatoclient-launcher "$APP_DIR/Contents/MacOS/"
chmod +x "$APP_DIR/Contents/MacOS/potatoclient-launcher"

# Copy JAR
cp target/potatoclient.jar "$APP_DIR/Contents/Resources/"

# Option 1: Use system Java (no bundled runtime)
echo "Using system Java runtime..."
# Remove JAVA_HOME setting from launcher for system Java
sed -i '' 's|export JAVA_HOME=.*|# Using system Java|' "$APP_DIR/Contents/MacOS/potatoclient-launcher"
sed -i '' 's|JAVA_BIN=.*|JAVA_BIN="java"|' "$APP_DIR/Contents/MacOS/potatoclient-launcher"

# Convert icon if available
if [ -f "resources/icon.png" ]; then
    echo "Creating icon..."
    mkdir icon.iconset
    sips -z 16 16   resources/icon.png --out icon.iconset/icon_16x16.png 2>/dev/null
    sips -z 32 32   resources/icon.png --out icon.iconset/icon_16x16@2x.png 2>/dev/null
    sips -z 32 32   resources/icon.png --out icon.iconset/icon_32x32.png 2>/dev/null
    sips -z 64 64   resources/icon.png --out icon.iconset/icon_32x32@2x.png 2>/dev/null
    sips -z 128 128 resources/icon.png --out icon.iconset/icon_128x128.png 2>/dev/null
    sips -z 256 256 resources/icon.png --out icon.iconset/icon_128x128@2x.png 2>/dev/null
    sips -z 256 256 resources/icon.png --out icon.iconset/icon_256x256.png 2>/dev/null
    sips -z 512 512 resources/icon.png --out icon.iconset/icon_256x256@2x.png 2>/dev/null
    sips -z 512 512 resources/icon.png --out icon.iconset/icon_512x512.png 2>/dev/null
    sips -z 1024 1024 resources/icon.png --out icon.iconset/icon_512x512@2x.png 2>/dev/null
    iconutil -c icns icon.iconset 2>/dev/null || echo "Icon conversion failed"
    [ -f icon.icns ] && cp icon.icns "$APP_DIR/Contents/Resources/"
    rm -rf icon.iconset icon.icns
fi

# Update version in Info.plist
/usr/libexec/PlistBuddy -c "Set :CFBundleVersion $VERSION" "$APP_DIR/Contents/Info.plist"
/usr/libexec/PlistBuddy -c "Set :CFBundleShortVersionString $VERSION" "$APP_DIR/Contents/Info.plist"

echo ""
echo "Build complete! App bundle created at: $APP_DIR"
echo ""
echo "To run the app:"
echo "  1. Direct: open $APP_DIR"
echo "  2. From Finder: Double-click $APP_DIR"
echo "  3. From Terminal: open $APP_DIR"
echo ""
echo "Note: This is an unsigned build. If you see 'unidentified developer' warning:"
echo "  - Right-click the app → Open → Open"
echo "  - Or: System Preferences → Security & Privacy → Open Anyway"
echo ""

# Optional: Open the app immediately
read -p "Open the app now? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    open "$APP_DIR"
fi