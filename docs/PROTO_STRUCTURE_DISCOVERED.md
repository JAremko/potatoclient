# Discovered Protobuf Structure

This document details the actual protobuf structure discovered through proto-explorer analysis and action validator, documenting the differences between expected and actual proto messages.

## Action Registry vs Proto Commands Validation

Running the action validator tool revealed significant naming mismatches:
- **Registry**: 174 registered actions
- **Proto**: 141 unique proto commands
- **Matching**: 57 actions (33%)
- **Registry-only**: 117 actions (67%)
- **Proto-only**: 84 commands

### Major Naming Discrepancies

| Registry Pattern | Proto Pattern | Example |
|-----------------|---------------|---------|
| `rotary-*` | `rotaryplatform-*` | `rotary-start` vs `rotaryplatform-start` |
| `heat-camera-*` | `heatcamera-*` | `heat-camera-zoom-in` vs `heatcamera-zoom-in` |
| `day-camera-*` | `daycamera-*` | `day-camera-start` vs `daycamera-start` |
| `glass-heater-*` | `daycamglassheater-*` | `glass-heater-start` vs `daycamglassheater-start` |
| `system-*-geodesic-mode` | `system-geodesic-mode-*` | `system-enable-geodesic-mode` vs `system-geodesic-mode-enable` |
| `compass-calibrate-start-*` | `compass-start-calibrate-*` | `compass-calibrate-start-long` vs `compass-start-calibrate-long` |

### Commands Not in Proto

Many registered actions don't exist in the proto definitions:
- All `rotary-axis-*` commands (azimuth/elevation control)
- `lrf-align-*` commands
- Basic commands like `ping`, `noop`, `frozen`
- Camera focus/zoom detailed controls

### Commands Not in Registry

Proto commands that aren't registered:
- `lrf-day`, `lrf-heat` (LRF channel selection)
- `rotaryplatform-axis` (axis control parent)
- `daycamera-focus`, `daycamera-zoom` (parent commands)
- `heatcamera-zoom` (parent command)
- `system-localization` (vs `system-set-localization`)

## Key Discoveries

### 1. Package to Class Name Mappings

The protobuf compiler generates class names that don't always match the package names:

| Package | Expected Class | Actual Class |
|---------|---------------|--------------|
| cmd.CV | JonSharedCmdCV | JonSharedCmdCv |
| cmd.OSD | JonSharedCmdOSD | JonSharedCmdOsd |
| cmd.RotaryPlatform | JonSharedCmdRotaryPlatform | JonSharedCmdRotary |
| cmd.Lrf_calib | JonSharedCmdLrfCalib | JonSharedCmdLrfAlign |

### 2. Setter Method Naming

The setter methods in the root builder follow specific patterns:

- `cmd.CV` → `setCv()` (not `setCV()`)
- `cmd.OSD` → `setOsd()` (not `setOSD()`)
- `cmd.RotaryPlatform` → `setRotary()` (not `setRotaryPlatform()`)
- `cmd.Lira` → `setLira()` (not `setSetlira()`)

### 3. Special Message Name Cases

#### Underscores in Message Names
- `refine_target` → Class: `Refine_target`, Setter: `setRefineTarget()`
- Underscores are preserved in class names but converted to camelCase for setters

#### Hyphenated Names with Special Casing
- `stop-a-ll` → Class: `StopALl`, Setter: `setStopAll()`
- `start-a-ll` → Class: `StartALl`, Setter: `setStartAll()`

#### Kebab-case to PascalCase
- `show-default-screen` → Class: `ShowDefaultScreen`, Setter: `setShowDefaultScreen()`
- `enable-heat-osd` → Class: `EnableHeatOsd`, Setter: `setEnableHeatOsd()`

### 4. Missing Commands

Several commands exist in the Action Registry but not in the actual protobuf:
- Rotary: No `RotateTo`, `RotateToWithTimeout` variants (only specific commands like `rotate-to-gps`, `rotate-to-ndc`)
- System: Commands are `start-rec`/`stop-rec` not `start-recording`/`stop-recording`
- LIRA: Only has `refine-target`, not separate target management commands

### 5. Actual Command Structure

#### LIRA Commands
```
cmd.Lira.Root
└── refine-target
    └── target (JonGuiDataLiraTarget)
        ├── timestamp
        ├── target-longitude
        ├── target-latitude
        ├── target-altitude
        ├── target-azimuth
        ├── target-elevation
        ├── distance
        └── uuid-part[1-4]
```

#### System Commands
```
cmd.System.Root
├── start-rec
├── stop-rec
├── mark-rec-important
├── unmark-rec-important
├── start-a-ll
├── stop-a-ll
├── enter-transport
├── reboot
├── power-off
├── reset-configs
├── enable-geodesic-mode
├── disable-geodesic-mode
└── set-localization
```

#### OSD Commands
```
cmd.OSD.Root
├── show-default-screen
├── show-lrf-measure-screen
├── show-lrf-result-screen
├── show-lrf-result-simplified-screen
├── enable-heat-osd
├── disable-heat-osd
├── enable-day-osd
└── disable-day-osd
```

### 6. Enum Naming Differences

- Expected: `JonGuiDataLRFScanMode`
- Actual: `JonGuiDataLrfScanModes`

### 7. Field Type Discoveries

- Most numeric fields use protobuf optionals (nullable in Kotlin)
- Constraints are defined via buf.validate annotations
- Timestamps are `int64` (Long in Java/Kotlin)
- Coordinates use `double` with range constraints

## Builder Pattern

All commands follow this pattern:
```kotlin
JonSharedCmd.Root.newBuilder()
    .setProtocolVersion(1)
    .set{Package}(
        JonSharedCmd{Package}.Root.newBuilder()
            .set{Command}(
                JonSharedCmd{Package}.{Command}.newBuilder()
                    // Set fields here
                    .build()
            )
            .build()
    )
    .build()
```

## Proto Explorer Usage

Key commands that helped discover this structure:
```bash
# Find all protobuf classes
find target/classes/cmd -name "JonSharedCmd*.class" | grep -v '\$'

# Check Java methods
javap -c target/classes/cmd/Lira/JonSharedCmdLira\$Root\$Builder.class | grep -i set

# Use proto-explorer CLI
bb java-fields Root
bb java-builder cmd.Lira.JonSharedCmdLira\$Root
```

## Lessons Learned

1. **Never assume proto naming** - Always check the generated classes
2. **Case sensitivity matters** - CV vs Cv, OSD vs Osd
3. **Package names don't always match class names** - RotaryPlatform → Rotary
4. **Underscores have special handling** - Preserved in classes, camelCased in methods
5. **Some commands are structured differently** - LIRA has nested target data
6. **Proto-explorer is essential** - Manual inspection would have taken much longer