# Action Registry to Proto Command Mapping

This document provides the mapping between Action Registry names and actual protobuf command names.

## Naming Convention Fixes Needed

### 1. Rotary Platform Commands

| Current Registry | Proto Command | Action |
|-----------------|---------------|---------|
| `rotary-start` | `rotaryplatform-start` | Update registry |
| `rotary-stop` | `rotaryplatform-stop` | Update registry |
| `rotary-halt` | `rotaryplatform-halt` | Update registry |
| `rotary-get-meteo` | `rotaryplatform-get-meteo` | Update registry |
| `rotary-set-mode` | `rotaryplatform-set-mode` | Update registry |
| `rotary-set-origin-gps` | `rotaryplatform-set-origin-gps` | Update registry |
| `rotary-rotate-to-gps` | `rotaryplatform-rotate-to-gps` | Update registry |
| `rotary-rotate-to-ndc` | `rotaryplatform-rotate-to-ndc` | Update registry |
| `rotary-scan-*` | `rotaryplatform-scan-*` | Update all scan commands |
| `rotary-set-platform-*` | `rotaryplatform-set-platform-*` | Update platform commands |
| `rotary-set-use-rotary-as-compass` | `rotaryplatform-set-use-rotary-as-compass` | Update registry |

### 2. Heat Camera Commands

| Current Registry | Proto Command | Action |
|-----------------|---------------|---------|
| `heat-camera-start` | `heatcamera-start` | Update registry |
| `heat-camera-stop` | `heatcamera-stop` | Update registry |
| `heat-camera-zoom-in` | `heatcamera-zoom-in` | Update registry |
| `heat-camera-zoom-out` | `heatcamera-zoom-out` | Update registry |
| `heat-camera-zoom-stop` | `heatcamera-zoom-stop` | Update registry |
| `heat-camera-focus-*` | `heatcamera-focus-*` | Update all focus commands |
| `heat-camera-set-*` | `heatcamera-set-*` | Update all set commands |
| `heat-camera-*` | `heatcamera-*` | Update all remaining commands |

### 3. Day Camera Commands

| Current Registry | Proto Command | Action |
|-----------------|---------------|---------|
| `day-camera-start` | `daycamera-start` | Update registry |
| `day-camera-stop` | `daycamera-stop` | Update registry |
| `day-camera-photo` | `daycamera-photo` | Update registry |
| `day-camera-get-meteo` | `daycamera-get-meteo` | Update registry |
| `day-camera-halt-all` | `daycamera-halt-all` | Update registry |
| `day-camera-*` | `daycamera-*` | Update all remaining commands |

### 4. Glass Heater Commands

| Current Registry | Proto Command | Action |
|-----------------|---------------|---------|
| `glass-heater-start` | `daycamglassheater-start` | Update registry |
| `glass-heater-stop` | `daycamglassheater-stop` | Update registry |
| `glass-heater-turn-on` | `daycamglassheater-turn-on` | Update registry |
| `glass-heater-turn-off` | `daycamglassheater-turn-off` | Update registry |
| `glass-heater-get-meteo` | `daycamglassheater-get-meteo` | Update registry |

### 5. System Commands

| Current Registry | Proto Command | Action |
|-----------------|---------------|---------|
| `system-enable-geodesic-mode` | `system-geodesic-mode-enable` | Update registry |
| `system-disable-geodesic-mode` | `system-geodesic-mode-disable` | Update registry |
| `system-set-localization` | `system-localization` | Update registry |

### 6. Compass Commands

| Current Registry | Proto Command | Action |
|-----------------|---------------|---------|
| `compass-calibrate-start-long` | `compass-start-calibrate-long` | Update registry |
| `compass-calibrate-start-short` | `compass-start-calibrate-short` | Update registry |

## Commands to Remove from Registry

These commands don't exist in proto and should be removed or mapped to existing commands:

### Rotary Axis Commands
- `rotary-axis-azimuth-*` (6 commands)
- `rotary-axis-elevation-*` (6 commands)
→ These might be handled by `rotaryplatform-axis` parent command

### LRF Align Commands
- `lrf-align-*` (6 commands)
→ These appear to be in a separate `cmd.Lrf-calib` package

### Basic Commands
- `ping`
- `noop`
- `frozen`
→ These might be test/debug commands not in production proto

### Detailed Camera Controls
Many fine-grained camera controls exist in registry but not in proto:
- `day-camera-focus-*` (8 commands) → Use `daycamera-focus`
- `day-camera-zoom-*` (9 commands) → Use `daycamera-zoom`
- `heat-camera-zoom-set-table-value` → Use `heatcamera-zoom`
- `heat-camera-zoom-next-table-pos` → Use `heatcamera-zoom`
- `heat-camera-zoom-prev-table-pos` → Use `heatcamera-zoom`

## Commands to Add to Registry

These proto commands are missing from the registry:

### LRF Commands
- `lrf-day` - Select day channel for LRF
- `lrf-heat` - Select heat channel for LRF

### Parent Commands
- `rotaryplatform-axis` - Parent for axis control
- `daycamera-focus` - Parent for focus control
- `daycamera-zoom` - Parent for zoom control
- `heatcamera-zoom` - Parent for zoom control
- `heatcamera-set-filter` - Set heat camera filter (vs set-filters)

### System Commands
- `system-localization` - Different from `system-set-localization`

## Implementation Strategy

1. **Update Registry Names**: Change action names to match proto conventions
2. **Remove Non-Proto Commands**: Either remove or document as extensions
3. **Add Missing Proto Commands**: Ensure all proto commands are registered
4. **Update Command Builders**: Ensure builders match the new names
5. **Update Tests**: All tests should use the new naming convention

## Validation Results

After implementing these changes, re-run the action validator:
```bash
make validate-actions
```

The goal is to achieve:
- 100% of proto commands present in registry
- Clear documentation for any registry-only commands
- Consistent naming between registry and proto