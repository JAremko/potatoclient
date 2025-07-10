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
export LD_LIBRARY_PATH="$BASE/usr/lib:$BASE/usr/lib/x86_64-linux-gnu:$BASE/usr/lib/jre/lib:$BASE/usr/lib/jre/lib/server:$LD_LIBRARY_PATH"
export GST_PLUGIN_PATH="$BASE/usr/lib/gstreamer-1.0"
export GST_PLUGIN_PATH_1_0="$BASE/usr/lib/gstreamer-1.0"
export GST_PLUGIN_SYSTEM_PATH_1_0="$BASE/usr/lib/gstreamer-1.0"
export GST_PLUGIN_SCANNER="$BASE/usr/lib/gstreamer1.0/gstreamer-1.0/gst-plugin-scanner"
export GST_PLUGIN_SCANNER_1_0="$BASE/usr/lib/gstreamer1.0/gstreamer-1.0/gst-plugin-scanner"

# Java options for GStreamer - Critical for JNA to find libraries
JAVA_OPTS="-Djna.library.path=$BASE/usr/lib:$BASE/usr/lib/x86_64-linux-gnu"
JAVA_OPTS="$JAVA_OPTS -Dgstreamer.library.path=$BASE/usr/lib:$BASE/usr/lib/x86_64-linux-gnu"
JAVA_OPTS="$JAVA_OPTS -Dgstreamer.plugin.path=$BASE/usr/lib/gstreamer-1.0"
JAVA_OPTS="$JAVA_OPTS -Djava.library.path=$BASE/usr/lib:$BASE/usr/lib/x86_64-linux-gnu"
JAVA_OPTS="$JAVA_OPTS -Djna.nosys=false"

# Execute Java with all options from command line plus our Java options
exec "$BASE/usr/lib/jre/bin/java" $JAVA_OPTS -jar "$BASE/usr/lib/potatoclient.jar" "$@"