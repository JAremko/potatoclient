#!/bin/bash

# Get the real path of the AppImage or AppDir
if [ -n "${APPIMAGE}" ] && [ -z "${APPDIR}" ]; then
    # Running from AppImage - APPDIR is automatically set by AppImage runtime
    export APPDIR="${APPDIR:-${HERE}}"
fi

if [ -z "${APPDIR}" ]; then
    # Running from extracted AppDir or other context
    HERE="$(dirname "$(readlink -f "${0}")")"
    export APPDIR="$(cd "${HERE}/.." && pwd)"
fi

# Source AppRun hooks if they exist
if [ -d "${APPDIR}/apprun-hooks" ]; then
    for hook in "${APPDIR}"/apprun-hooks/*.sh; do
        if [ -f "$hook" ]; then
            source "$hook"
        fi
    done
fi

# Set up environment
# CRITICAL: Put AppDir libraries FIRST to avoid system library conflicts
export LD_LIBRARY_PATH="${APPDIR}/usr/lib:${APPDIR}/usr/lib/x86_64-linux-gnu:${LD_LIBRARY_PATH}"
export PATH="${APPDIR}/usr/lib/jvm/bin:${PATH}"
export JAVA_HOME="${APPDIR}/usr/lib/jvm"

# Unset system GStreamer paths to prevent conflicts
unset GST_PLUGIN_PATH
unset GST_PLUGIN_SYSTEM_PATH
unset GST_REGISTRY_UPDATE

# Java options for GStreamer and UI consistency
JAVA_OPTS="-Djna.library.path=${APPDIR}/usr/lib:${APPDIR}/usr/lib/x86_64-linux-gnu"
# UI consistency flags
JAVA_OPTS="${JAVA_OPTS} -Dswing.noxp=true"
JAVA_OPTS="${JAVA_OPTS} -Dsun.java2d.noddraw=true"
JAVA_OPTS="${JAVA_OPTS} -Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel"
# Enable native access for JNA
JAVA_OPTS="${JAVA_OPTS} --enable-native-access=ALL-UNNAMED"
JAVA_OPTS="${JAVA_OPTS} -Dgstreamer.library.path=${APPDIR}/usr/lib:${APPDIR}/usr/lib/x86_64-linux-gnu"
JAVA_OPTS="${JAVA_OPTS} -Dgstreamer.plugin.path=${APPDIR}/usr/lib/gstreamer-1.0"
JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=${APPDIR}/usr/lib:${APPDIR}/usr/lib/x86_64-linux-gnu"
JAVA_OPTS="${JAVA_OPTS} -Djna.nosys=false"
# Force JNA to look in our AppDir first for native libraries
JAVA_OPTS="${JAVA_OPTS} -Djna.boot.library.path=${APPDIR}/usr/lib:${APPDIR}/usr/lib/x86_64-linux-gnu"
# Enable JNA debug logging if GST_DEBUG is set
if [ "$GST_DEBUG" != "" ]; then
    JAVA_OPTS="${JAVA_OPTS} -Djna.debug_load=true"
fi

# Also set GStreamer environment variables directly
export GST_PLUGIN_PATH="${APPDIR}/usr/lib/gstreamer-1.0"
export GST_PLUGIN_PATH_1_0="${APPDIR}/usr/lib/gstreamer-1.0"
export GST_PLUGIN_SYSTEM_PATH_1_0="${APPDIR}/usr/lib/gstreamer-1.0"

# CRITICAL: Set plugin scanner path to prevent "External plugin loader failed" errors
export GST_PLUGIN_SCANNER_1_0="${APPDIR}/usr/lib/gstreamer1.0/gstreamer-1.0/gst-plugin-scanner"
export GST_PTP_HELPER_1_0="${APPDIR}/usr/lib/gstreamer1.0/gstreamer-1.0/gst-ptp-helper"

# Disable plugin scanner reuse to ensure fresh scans
export GST_REGISTRY_REUSE_PLUGIN_SCANNER="no"

# macOS compatibility flag - only add on macOS
if [[ "$OSTYPE" == "darwin"* ]]; then
    JAVA_OPTS="${JAVA_OPTS} -XstartOnFirstThread"
fi

# Debug output if GST_DEBUG is set
if [ "$GST_DEBUG" != "" ]; then
    echo "=== GStreamer Environment ===" >&2
    echo "APPDIR: ${APPDIR}" >&2
    echo "GST_PLUGIN_PATH_1_0: ${GST_PLUGIN_PATH_1_0}" >&2
    echo "GST_PLUGIN_SCANNER_1_0: ${GST_PLUGIN_SCANNER_1_0}" >&2
    echo "LD_LIBRARY_PATH: ${LD_LIBRARY_PATH}" >&2
    echo "===========================" >&2
fi

# Execute the application
exec "${JAVA_HOME}/bin/java" ${JAVA_OPTS} -jar "${APPDIR}/usr/lib/potatoclient/potatoclient.jar" "$@"