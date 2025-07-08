PotatoClient for macOS
======================

Installation Instructions:
-------------------------
1. Drag PotatoClient to your Applications folder
2. Install GStreamer (REQUIRED):
   - Option A: Use Homebrew: brew install gstreamer gst-plugins-base gst-plugins-good gst-plugins-bad
   - Option B: Install the official GStreamer package (included in this DMG)

First Launch:
------------
- Right-click PotatoClient → Open → Open (to bypass Gatekeeper)
- If you see "can't find GStreamer", make sure you installed it first

System Requirements:
-------------------
- macOS 11.0 (Big Sur) or newer
- Apple Silicon (M1/M2/M3) processor
- GStreamer 1.0 or newer
- 4GB RAM recommended

Troubleshooting:
---------------
If PotatoClient fails to start:
1. Verify GStreamer is installed: gst-launch-1.0 --version
2. Check Console.app for error messages
3. Try launching from Terminal: /Applications/PotatoClient.app/Contents/MacOS/potatoclient-launcher

Support:
--------
Report issues at: https://github.com/JAremko/potatoclient/issues