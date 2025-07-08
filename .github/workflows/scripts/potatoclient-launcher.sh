#!/bin/bash
# Simple launcher that avoids complex path calculations

# Find where we are
if [ -n "$APPDIR" ]; then
    # Inside AppImage
    BASE="$APPDIR"
else
    # Outside AppImage (for testing)
    SCRIPT="$(readlink -f "$0")"
    BASE="$(dirname "$(dirname "$(dirname "$SCRIPT")")")"
fi

# Set environment
export LD_LIBRARY_PATH="$BASE/usr/lib:$BASE/usr/lib/jre/lib:$BASE/usr/lib/jre/lib/server:$LD_LIBRARY_PATH"
export GST_PLUGIN_PATH="$BASE/usr/lib/gstreamer-1.0"
export GST_PLUGIN_SCANNER="$BASE/usr/lib/gstreamer1.0/gstreamer-1.0/gst-plugin-scanner"

# Execute Java
exec "$BASE/usr/lib/jre/bin/java" -jar "$BASE/usr/lib/potatoclient.jar" "$@"