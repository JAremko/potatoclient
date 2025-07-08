#!/bin/bash

# abort on all errors
set -e

if [ "$DEBUG" != "" ]; then
    set -x
fi

script=$(readlink -f "$0")

show_usage() {
    echo "Usage: $script --appdir <path to AppDir>"
    echo
    echo "Bundles GStreamer plugins into an AppDir"
    echo
    echo "Required variables:"
    echo "  LINUXDEPLOY=\".../linuxdeploy\" path to linuxdeploy (e.g., AppImage); set automatically when plugin is run directly by linuxdeploy"
    echo
    echo "Optional variables:"
    echo "  GSTREAMER_INCLUDE_BAD_PLUGINS=\"1\" (default: enabled for H.264 support)"
    echo "  GSTREAMER_PLUGINS_DIR=\"...\" (directory containing GStreamer plugins; default: guessed based on main distro architecture)"
    echo "  GSTREAMER_HELPERS_DIR=\"...\" (directory containing GStreamer helper tools like gst-plugin-scanner; default: guessed based on main distro architecture)"
    echo "  GSTREAMER_VERSION=\"1.0\" (default: 1.0)"
}

while [ "$1" != "" ]; do
    case "$1" in
        --plugin-api-version)
            echo "0"
            exit 0
            ;;
        --appdir)
            APPDIR="$2"
            shift
            shift
            ;;
        --help)
            show_usage
            exit 0
            ;;
        *)
            echo "Invalid argument: $1"
            echo
            show_usage
            exit 1
            ;;
    esac
done

if [ "$APPDIR" == "" ]; then
    show_usage
    exit 1
fi

if ! which patchelf &>/dev/null && ! type patchelf &>/dev/null; then
    echo "Error: patchelf not found"
    echo
    show_usage
    exit 2
fi

if [[ "$LINUXDEPLOY" == "" ]]; then
    echo "Warning: \$LINUXDEPLOY not set, will skip dependency deployment"
fi

mkdir -p "$APPDIR"

export GSTREAMER_VERSION="${GSTREAMER_VERSION:-1.0}"
export GSTREAMER_INCLUDE_BAD_PLUGINS="${GSTREAMER_INCLUDE_BAD_PLUGINS:-1}"

plugins_target_dir="$APPDIR"/usr/lib/gstreamer-"$GSTREAMER_VERSION"
helpers_target_dir="$APPDIR"/usr/lib/gstreamer"$GSTREAMER_VERSION"/gstreamer-"$GSTREAMER_VERSION"

if [ "$GSTREAMER_PLUGINS_DIR" != "" ]; then
    plugins_dir="${GSTREAMER_PLUGINS_DIR}"
elif [ -d /usr/lib/"$(uname -m)"-linux-gnu/gstreamer-"$GSTREAMER_VERSION" ]; then
    plugins_dir=/usr/lib/$(uname -m)-linux-gnu/gstreamer-"$GSTREAMER_VERSION"
else
    plugins_dir=/usr/lib/gstreamer-"$GSTREAMER_VERSION"
fi

if [ "$GSTREAMER_HELPERS_DIR" != "" ]; then
    helpers_dir="${GSTREAMER_HELPERS_DIR}"
else
    # Try multiple locations for helpers
    if [ -d /usr/lib/$(uname -m)-linux-gnu/gstreamer"$GSTREAMER_VERSION"/gstreamer-"$GSTREAMER_VERSION" ]; then
        helpers_dir=/usr/lib/$(uname -m)-linux-gnu/gstreamer"$GSTREAMER_VERSION"/gstreamer-"$GSTREAMER_VERSION"
    elif [ -d /usr/lib/gstreamer-"$GSTREAMER_VERSION" ]; then
        helpers_dir=/usr/lib/gstreamer-"$GSTREAMER_VERSION"
    else
        helpers_dir=/usr/libexec/gstreamer-"$GSTREAMER_VERSION"
    fi
fi

if [ ! -d "$plugins_dir" ]; then
    echo "Error: could not find plugins directory: $plugins_dir"
    exit 1
fi

mkdir -p "$plugins_target_dir"

echo "Copying plugins into $plugins_target_dir"

# Essential plugins for H.264 video streaming
essential_plugins=(
    "libgstcoreelements.so"
    "libgstcoretracers.so"
    "libgstadder.so"
    "libgstapp.so"
    "libgstaudioconvert.so"
    "libgstaudiomixer.so"
    "libgstaudiorate.so"
    "libgstaudioresample.so"
    "libgstaudiotestsrc.so"
    "libgstautodetect.so"
    "libgstgio.so"
    "libgstpbtypes.so"
    "libgstplayback.so"
    "libgsttypefindfunctions.so"
    "libgstvideorate.so"
    "libgstvideotestsrc.so"
    "libgstvolume.so"
    "libgstximagesink.so"
    "libgstxvimagesink.so"
    # H.264 specific plugins
    "libgstvideoparsersbad.so"
    "libgstmpegtsdemux.so"
    "libgstmpegpsmux.so"
    "libgstmpegtsmux.so"
    "libgstrtpmanager.so"
    "libgstrtp.so"
    "libgstrtsp.so"
    "libgstudp.so"
    "libgsttcp.so"
    # Include libav for H.264 decoding
    "libgstlibav.so"
    "libgstx264.so"
    "libgstopenh264.so"
    "libgstvpx.so"
)

# Copy essential plugins
for plugin_name in "${essential_plugins[@]}"; do
    if [ -f "$plugins_dir/$plugin_name" ]; then
        echo "Copying essential plugin: $plugin_name"
        cp "$plugins_dir/$plugin_name" "$plugins_target_dir"
    else
        echo "Warning: Essential plugin not found: $plugin_name"
    fi
done


# Copy all plugins if GSTREAMER_COPY_ALL is set
if [ "$GSTREAMER_COPY_ALL" == "1" ]; then
    echo "Copying all plugins..."
    for i in "$plugins_dir"/*; do
        [ -d "$i" ] && continue
        [ ! -f "$i" ] && echo "File does not exist: $i" && continue
        plugin_name=$(basename "$i")
        # Skip if already copied
        if [ ! -f "$plugins_target_dir/$plugin_name" ]; then
            echo "Copying additional plugin: $plugin_name"
            cp "$i" "$plugins_target_dir"
        fi
    done
fi

# Run linuxdeploy to copy dependencies (if available)
if [[ "$LINUXDEPLOY" != "" ]]; then
    "$LINUXDEPLOY" --appdir "$APPDIR"
fi

# Fix rpath for all plugins
for i in "$plugins_target_dir"/*; do
    [ -d "$i" ] && continue
    [ ! -f "$i" ] && echo "File does not exist: $i" && continue
    (file "$i" | grep -v ELF --silent) && echo "Ignoring non ELF file: $i" && continue

    echo "Manually setting rpath for $i"
    patchelf --set-rpath '$ORIGIN/..:$ORIGIN' "$i"
done

# Create helpers directory
mkdir -p "$helpers_target_dir"

echo "Copying helpers in $helpers_target_dir"

# Essential helpers
essential_helpers=(
    "gst-plugin-scanner"
    "gst-ptp-helper"
)

# First try to find helpers in the expected directory
for helper_name in "${essential_helpers[@]}"; do
    if [ -f "$helpers_dir/$helper_name" ]; then
        echo "Copying helper from helpers_dir: $helper_name"
        cp "$helpers_dir/$helper_name" "$helpers_target_dir"
    else
        # Try to find it elsewhere
        echo "Helper not found in $helpers_dir, searching system..."
        helper_path=$(which "$helper_name-$GSTREAMER_VERSION" 2>/dev/null || which "$helper_name" 2>/dev/null || true)
        if [ -n "$helper_path" ]; then
            echo "Found helper at: $helper_path"
            cp "$helper_path" "$helpers_target_dir/$helper_name"
        else
            echo "Warning: Could not find helper: $helper_name"
        fi
    fi
done

# Fix rpath for helpers
for i in "$helpers_target_dir"/*; do
    [ -d "$i" ] && continue
    [ ! -f "$i" ] && echo "File does not exist: $i" && continue
    (file "$i" | grep -v ELF --silent) && echo "Ignoring non ELF file: $i" && continue

    echo "Manually setting rpath for $i"
    patchelf --set-rpath '$ORIGIN/../..' "$i"
done

# Copy GStreamer libraries
echo "Copying GStreamer libraries..."
gst_lib_dir="/usr/lib/$(uname -m)-linux-gnu"
target_lib_dir="$APPDIR/usr/lib"

for lib in libgstreamer-1.0.so* libgstbase-1.0.so* libgstapp-1.0.so* libgstvideo-1.0.so* libgstaudio-1.0.so* libgstpbutils-1.0.so* libgsttag-1.0.so* libgstrtp-1.0.so* libgstrtsp-1.0.so* libgstsdp-1.0.so* libgstnet-1.0.so*; do
    if [ -f "$gst_lib_dir/$lib" ]; then
        echo "Copying library: $lib"
        cp -P "$gst_lib_dir/$lib" "$target_lib_dir/"
    fi
done

echo "Installing AppRun hook"
mkdir -p "$APPDIR"/apprun-hooks

if [ "$GSTREAMER_VERSION" == "1.0" ]; then
    cat > "$APPDIR"/apprun-hooks/linuxdeploy-plugin-gstreamer.sh <<\EOF
#!/bin/bash

export GST_REGISTRY_REUSE_PLUGIN_SCANNER="no"
export GST_PLUGIN_SYSTEM_PATH_1_0="${APPDIR}/usr/lib/gstreamer-1.0"
export GST_PLUGIN_PATH_1_0="${APPDIR}/usr/lib/gstreamer-1.0"

export GST_PLUGIN_SCANNER_1_0="${APPDIR}/usr/lib/gstreamer1.0/gstreamer-1.0/gst-plugin-scanner"
export GST_PTP_HELPER_1_0="${APPDIR}/usr/lib/gstreamer1.0/gstreamer-1.0/gst-ptp-helper"

# Ensure GStreamer can find its libraries
export LD_LIBRARY_PATH="${APPDIR}/usr/lib:${LD_LIBRARY_PATH}"

# For debugging
if [ "$GST_DEBUG" != "" ]; then
    echo "GStreamer AppImage environment:" >&2
    echo "  GST_PLUGIN_SYSTEM_PATH_1_0=$GST_PLUGIN_SYSTEM_PATH_1_0" >&2
    echo "  GST_PLUGIN_SCANNER_1_0=$GST_PLUGIN_SCANNER_1_0" >&2
    echo "  Plugins directory contents:" >&2
    ls -la "$GST_PLUGIN_SYSTEM_PATH_1_0" 2>&1 | head -20 >&2
fi
EOF
elif [ "$GSTREAMER_VERSION" == "0.10" ]; then
    cat > "$APPDIR"/apprun-hooks/linuxdeploy-plugin-gstreamer.sh <<\EOF
#!/bin/bash

export GST_REGISTRY_REUSE_PLUGIN_SCANNER="no"
export GST_PLUGIN_SYSTEM_PATH_0_10="${APPDIR}/usr/lib/gstreamer-0.10"

export GST_PLUGIN_SCANNER_0_10="${APPDIR}/usr/lib/gstreamer1.0/gstreamer-0.10/gst-plugin-scanner"
export GST_PTP_HELPER_0_10="${APPDIR}/usr/lib/gstreamer1.0/gstreamer-0.10/gst-ptp-helper"
EOF
else
    echo "Warning: unknown GStreamer version: $GSTREAMER_VERSION, cannot install AppRun hook"
fi

chmod +x "$APPDIR"/apprun-hooks/linuxdeploy-plugin-gstreamer.sh

echo "GStreamer plugin bundling complete!"
echo "Plugins copied to: $plugins_target_dir"
echo "Helpers copied to: $helpers_target_dir"
echo "AppRun hook installed at: $APPDIR/apprun-hooks/linuxdeploy-plugin-gstreamer.sh"