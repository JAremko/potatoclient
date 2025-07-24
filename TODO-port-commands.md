# TODO: Port Remaining Commands from TypeScript to Clojure

## Overview
This document tracks the commands that need to be ported from the TypeScript implementation to Clojure. Commands are grouped by module and prioritized based on functionality.

## Status Legend
- ✅ Complete
- 🚧 Partial/Stub implementation
- ❌ Not started
- 🔒 Read-only safe (can be used in read-only mode)

## Progress Summary

### Completed Modules (129+ functions total)
- ✅ **Day Camera** (30+ functions) - All zoom, focus, FX modes, calibration commands
- ✅ **Heat Camera** (35+ functions) - Thermal imaging, AGC, filters, DDE, zoom/focus
- ✅ **System** (14 functions) - Power, recording, localization, modes
- ✅ **OSD** (8 functions) - Display screens and overlay control
- ✅ **GPS** (5 functions) - GPS control and manual positioning
- ✅ **LRF** (14 functions) - Laser range finder control, designator, fog mode
- ✅ **LRF Alignment** (6 functions) - Camera offset calibration
- ✅ **Compass** (9 functions) - Compass control and calibration
- ✅ **Computer Vision** (7 functions) - Tracking, stabilization, auto-focus
- ✅ **Glass Heater** (5 functions) - Heater control for day camera

### All Modules Complete! 🎉

## Command Implementation Status

### 1. Core Infrastructure (✅ Complete)
- ✅ Basic commands (ping 🔒, frozen 🔒, noop)
- ✅ Command routing and encoding
- ✅ Read-only mode support
- ✅ Async channel integration

### 2. Rotary Platform (`potatoclient.cmd.rotary`) (✅ Complete)
- ✅ Basic control (start, stop, halt)
- ✅ Position control (azimuth, elevation, bank)
- ✅ Rotation commands (absolute, relative, continuous)
- ✅ GPS integration
- ✅ Scan functionality
- ✅ NDC rotation
- ✅ Compass integration
- ✅ Mode settings

### 3. Day Camera (`potatoclient.cmd.day-camera`) (✅ COMPLETED)
All day camera commands have been successfully implemented.

### 4. Heat Camera (`potatoclient.cmd.heat-camera`) (✅ COMPLETED)
All heat camera commands have been successfully implemented.

### 5. System Commands (`potatoclient.cmd.system`) (✅ COMPLETED)
All system commands have been successfully implemented.

### 6. OSD Commands (`potatoclient.cmd.osd`) (✅ COMPLETED)
All OSD commands have been successfully implemented.

### 7. GPS Commands (`potatoclient.cmd.gps`) (✅ COMPLETED)
All GPS commands have been successfully implemented.

### 8. LRF Commands (`potatoclient.cmd.lrf`) (✅ COMPLETED)
**Proto file**: `jon_shared_cmd_lrf.proto`

#### Basic Operations
- ❌ `lrf-start` - Start LRF
- ❌ `lrf-stop` - Stop LRF
- ❌ `lrf-measure` - Trigger measurement
- ❌ `lrf-start-measure-session` - Begin session
- ❌ `lrf-stop-measure-session` - End session

#### Advanced Features
- ❌ `lrf-set-scan-mode` - Enable/disable scan
- ❌ `lrf-set-fog-mode` - Enable/disable fog mode
- ❌ `lrf-set-designator-mode` - Target designator (A/B/Off)
- ❌ `lrf-set-refine-mode` - Enable/disable refine
- ❌ `lrf-get-meteo` - Get meteorological data 🔒

### 9. LRF Alignment (`potatoclient.cmd.lrf-alignment`) (✅ COMPLETED)
**Proto file**: `jon_shared_cmd_lrf.proto` (shared)

- ❌ `lrf-set-offset-day` - Set day camera offset
- ❌ `lrf-shift-offset-day` - Adjust day offset
- ❌ `lrf-set-offset-heat` - Set heat camera offset
- ❌ `lrf-shift-offset-heat` - Adjust heat offset
- ❌ `lrf-save-offsets` - Save calibration
- ❌ `lrf-reset-offsets` - Reset to defaults

### 10. Compass Commands (`potatoclient.cmd.compass`) (✅ COMPLETED)
**Proto file**: `jon_shared_cmd_compass.proto`

#### Basic Operations
- ❌ `compass-start` - Start compass
- ❌ `compass-stop` - Stop compass
- ❌ `compass-set-declination` - Magnetic declination
- ❌ `compass-set-offset-angles` - Azimuth/elevation offsets

#### Calibration
- ❌ `compass-calibrate-long` - Long calibration
- ❌ `compass-calibrate-short` - Quick calibration
- ❌ `compass-calibrate-next` - Next calibration step
- ❌ `compass-calibrate-cancel` - Cancel calibration
- ❌ `compass-get-meteo` - Get compass data 🔒

### 11. Computer Vision (`potatoclient.cmd.cv`) (✅ COMPLETED)
**Proto file**: `jon_shared_cmd_cv.proto`

- ❌ `cv-start-tracking` - Start tracking at NDC coordinates
- ❌ `cv-stop-tracking` - Stop tracking
- ❌ `cv-set-auto-focus` - Auto focus on/off per channel
- ❌ `cv-set-vampire-mode` - Vampire mode on/off
- ❌ `cv-set-stabilization-mode` - Stabilization on/off
- ❌ `cv-start-video-dump` - Start recording
- ❌ `cv-stop-video-dump` - Stop recording

### 12. Glass Heater (`potatoclient.cmd.glass-heater`) (✅ COMPLETED)
**Proto file**: `jon_shared_cmd_day_cam_glass_heater.proto`

- ❌ `glass-heater-start` - Start heater subsystem
- ❌ `glass-heater-stop` - Stop heater subsystem
- ❌ `glass-heater-on` - Turn heater on
- ❌ `glass-heater-off` - Turn heater off
- ❌ `glass-heater-get-meteo` - Get heater status 🔒

## Quality Assurance Process

### After Each Batch (10 Functions)
1. **Generate Lint Report**: Run `make lint` to check for issues
2. **Fix Lint Issues**: Address any problems found by clj-kondo
3. **Verify Compilation**: Run `make dev` to ensure code still compiles
4. **Run Tests**: Execute relevant test suite for the module
5. **Commit Working State**: Create a checkpoint commit before proceeding

### Lint Report Commands
```bash
# Generate full lint report
make lint

# Generate filtered lint report (removes common false positives)
make report-lint

# Check for unspecced functions
make report-unspecced

# Quick compilation check
make dev
```

## Notes

1. **Read-only Safety**: Commands marked with 🔒 should be allowed in read-only mode as they don't change system state
2. **Enum Imports**: Day camera stubs exist but need proper protobuf enum imports
3. **Testing**: Each namespace should have comprehensive tests similar to rotary commands
4. **Validation**: Use Malli specs for all command parameters
5. **Consistency**: Follow the established pattern from rotary commands
6. **Quality Gates**: Never proceed to next batch if lint or compilation fails

## Next Steps

1. ~~Fix the day camera protobuf imports issue~~ ✅
2. ~~Complete day camera implementation~~ ✅
3. ~~Create heat camera namespace following rotary pattern~~ ✅
4. ~~Create system, OSD, and GPS namespaces~~ ✅
5. Create LRF namespace for laser rangefinder control
6. Add comprehensive tests for each module
7. Update CLAUDE.md with new command documentation