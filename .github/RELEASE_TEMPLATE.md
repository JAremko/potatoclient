Release v{{VERSION}} - {{COMMIT_MESSAGE}}

## Downloads

### Windows
- **PotatoClient-{{VERSION}}-windows-x64-setup.exe** - Windows installer (includes GStreamer 1.26.3)

### Linux
- **PotatoClient-{{VERSION}}-linux-x86_64.AppImage** - AppImage for Linux

### macOS
- **PotatoClient-{{VERSION}}-macos-arm64.dmg** - DMG for macOS (Apple Silicon)

## System Requirements

### Windows
- Windows 7 SP1 x64 or newer (64-bit only)
- 4GB RAM minimum recommended
- No Java installation required (bundled Java 17)
- Administrator privileges for installer

### Linux
- Ubuntu 22.04 LTS or newer (glibc 2.35+)
- 64-bit Linux distribution
- GStreamer 1.0+ (bundled in AppImage)
- 4GB RAM minimum recommended
- **Important**: AppImage requires FUSE 2. See [setup guide](https://github.com/AppImage/AppImageKit/wiki/FUSE)

### macOS
- macOS 11.0 (Big Sur) or newer
- Apple Silicon (M1/M2/M3) processor
- GStreamer 1.0+ (install via Homebrew or official package)
- 4GB RAM minimum recommended

## Changes
{{COMMIT_MESSAGE}}

---
*Built from commit: {{COMMIT_SHA}}*