# Keyboard Shortcuts Reference

Complete list of keyboard shortcuts available in PotatoClient.

## Global Shortcuts

These shortcuts work from anywhere in the application.

### Application Control

| Shortcut | Action | Description |
|----------|--------|-------------|
| `Ctrl+Q` / `Cmd+Q` | Quit | Exit the application |
| `F11` | Fullscreen | Toggle fullscreen mode |
| `Ctrl+,` / `Cmd+,` | Preferences | Open preferences dialog |
| `F1` | Help | Show help documentation |
| `Ctrl+Shift+D` | Debug Info | Show debug information panel |

### Connection

| Shortcut | Action | Description |
|----------|--------|-------------|
| `Ctrl+K` | Connect | Connect to server |
| `Ctrl+D` | Disconnect | Disconnect from server |
| `Ctrl+R` | Reconnect | Reconnect to last server |

### Recording

| Shortcut | Action | Description |
|----------|--------|-------------|
| `R` | Start/Stop Recording | Toggle recording |
| `Ctrl+Shift+R` | Recording Settings | Open recording preferences |
| `Alt+R` | Quick Record | Start recording with default settings |

## Video Control

### Display

| Shortcut | Action | Description |
|----------|--------|-------------|
| `1` | Heat Only | Show only heat camera |
| `2` | Day Only | Show only day camera |
| `3` | Side by Side | Show both cameras side by side |
| `4` | Picture in Picture | Show PiP mode |
| `Tab` | Switch Active | Switch active video stream |
| `Space` | Pause/Resume | Pause or resume video streams |

### Zoom Control

| Shortcut | Action | Description |
|----------|--------|-------------|
| `+` / `=` | Zoom In | Increase zoom level |
| `-` | Zoom Out | Decrease zoom level |
| `0` | Reset Zoom | Reset to default zoom |
| `Ctrl+Mouse Wheel` | Zoom | Zoom with mouse wheel |
| `Z` | Zoom Menu | Show zoom preset menu |

## Platform Control

### Movement

| Shortcut | Action | Description |
|----------|--------|-------------|
| `Arrow Keys` | Pan | Pan platform in direction |
| `Shift+Arrows` | Fast Pan | Pan at maximum speed |
| `Ctrl+Arrows` | Fine Pan | Pan at minimum speed |
| `H` | Halt | Stop all platform movement |
| `Home` | Center | Return to home position |

### Presets

| Shortcut | Action | Description |
|----------|--------|-------------|
| `Ctrl+1-9` | Save Preset | Save current position to preset |
| `1-9` | Load Preset | Go to saved preset position |
| `Ctrl+0` | Clear Presets | Clear all position presets |

## Camera Control

### Focus

| Shortcut | Action | Description |
|----------|--------|-------------|
| `F` | Auto Focus | Toggle auto/manual focus |
| `Shift+F` | Focus Near | Adjust focus nearer |
| `Ctrl+F` | Focus Far | Adjust focus farther |

### Exposure

| Shortcut | Action | Description |
|----------|--------|-------------|
| `E` | Auto Exposure | Toggle auto/manual exposure |
| `[` | Decrease Exposure | Reduce exposure |
| `]` | Increase Exposure | Increase exposure |

### Thermal (Heat Camera)

| Shortcut | Action | Description |
|----------|--------|-------------|
| `T` | Temperature Range | Cycle temperature range modes |
| `C` | Calibrate | Perform NUC calibration |
| `P` | Palette | Cycle color palettes |

## Computer Vision

| Shortcut | Action | Description |
|----------|--------|-------------|
| `Ctrl+T` | Start Tracking | Start object tracking at cursor |
| `Escape` | Stop Tracking | Stop current tracking |
| `D` | Detection Mode | Toggle detection overlay |
| `Shift+D` | Detection Settings | Open detection preferences |

## Measurement

| Shortcut | Action | Description |
|----------|--------|-------------|
| `L` | LRF Measure | Take single LRF measurement |
| `Ctrl+L` | Continuous LRF | Start/stop continuous measurement |
| `M` | Measurement Mode | Toggle measurement overlay |

## Interface

### Panels

| Shortcut | Action | Description |
|----------|--------|-------------|
| `Ctrl+P` | Control Panel | Toggle control panel visibility |
| `Ctrl+I` | Info Panel | Toggle information panel |
| `Ctrl+M` | Menu Bar | Toggle menu bar visibility |
| `F9` | All Panels | Toggle all UI panels |

### Themes

| Shortcut | Action | Description |
|----------|--------|-------------|
| `Ctrl+Shift+T` | Theme Menu | Open theme selection |
| `Alt+T` | Toggle Theme | Switch between light/dark |

## Development Shortcuts

These are only available in development mode.

| Shortcut | Action | Description |
|----------|--------|-------------|
| `Ctrl+Shift+I` | Inspector | Open UI inspector |
| `Ctrl+Shift+L` | Logs | Show application logs |
| `Ctrl+Shift+M` | Metrics | Show performance metrics |
| `Ctrl+Shift+R` | Reload | Reload UI without restart |
| `F5` | Refresh State | Force state synchronization |

## Mouse Gestures

### On Video Stream

| Gesture | Action | Description |
|---------|--------|-------------|
| Click | Center Point | Set point of interest |
| Double Click | Start Track | Start tracking at position |
| Right Click | Context Menu | Show context menu |
| Drag | Pan Platform | Pan camera to follow drag |
| Ctrl+Drag | Select Region | Select region of interest |
| Wheel | Zoom | Zoom in/out on video |

### Platform Control

| Gesture | Action | Description |
|---------|--------|-------------|
| Click and Hold | Continuous Pan | Pan while holding |
| Double Tap | Quick Center | Center on tapped position |
| Pinch | Zoom Control | Zoom with trackpad pinch |

## Customization

### Modifying Shortcuts

Shortcuts can be customized in preferences:

1. Open Preferences (`Ctrl+,`)
2. Navigate to "Keyboard Shortcuts"
3. Click on any shortcut to modify
4. Press new key combination
5. Click "Apply"

### Creating Custom Shortcuts

```clojure
;; In user configuration
{:shortcuts
 {:custom-action {:key "ctrl+shift+x"
                  :action :my-custom-command
                  :description "My custom action"}}}
```

### Conflicting Shortcuts

If shortcuts conflict with system shortcuts:

1. Check system preferences
2. Modify PotatoClient shortcuts
3. Use alternative modifiers (Alt instead of Ctrl)

## Platform-Specific

### macOS

- Use `Cmd` instead of `Ctrl` for most shortcuts
- `Cmd+H` hides window (system shortcut)
- `Cmd+M` minimizes window (system shortcut)

### Linux

- Standard `Ctrl` shortcuts work as listed
- Window manager may capture some shortcuts
- Check desktop environment settings

### Windows

- Standard `Ctrl` shortcuts work as listed
- `Alt+F4` closes application (system)
- `Win` key combinations reserved by system

## Quick Reference Card

### Essential Shortcuts

```
Connection:     Ctrl+K (connect), Ctrl+D (disconnect)
Recording:      R (toggle)
Video:          1/2/3 (display modes), Tab (switch)
Platform:       Arrows (pan), H (halt)
Zoom:           +/- (zoom), 0 (reset)
Focus:          F (auto toggle)
Tracking:       Ctrl+T (start), Esc (stop)
Help:           F1
```

## Tips

1. **Learn gradually** - Start with essential shortcuts
2. **Use mnemonics** - R for Record, H for Halt, etc.
3. **Customize conflicts** - Modify shortcuts that conflict
4. **Print reference** - Keep quick reference handy
5. **Practice mode** - Use development mode to practice

## See Also

- [User Interface Guide](../guides/user-interface.md)
- [Configuration Reference](./configuration.md)
- [Gesture Controls](../guides/gesture-controls.md)