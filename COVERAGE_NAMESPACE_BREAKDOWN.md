# Coverage Namespace Breakdown

## Total Namespaces: 45

### ❌ Excluded Namespaces (19)

These namespaces had to be excluded from coverage due to protobuf dependencies:

#### Command Namespaces (14)
All `potatoclient.cmd.*` namespaces use protobuf-generated classes:
- `potatoclient.cmd.compass` - Compass control commands
- `potatoclient.cmd.core` - Core command infrastructure (WebSocket manager)
- `potatoclient.cmd.cv` - Computer vision commands
- `potatoclient.cmd.day_camera` - Day camera control commands
- `potatoclient.cmd.debug` - Debug/utility commands
- `potatoclient.cmd.glass_heater` - Glass heater control
- `potatoclient.cmd.gps` - GPS commands
- `potatoclient.cmd.heat_camera` - Heat camera control commands
- `potatoclient.cmd.lrf` - Laser Range Finder commands
- `potatoclient.cmd.lrf_alignment` - LRF alignment/calibration
- `potatoclient.cmd.osd` - On-Screen Display commands
- `potatoclient.cmd.rotary` - Rotary platform control
- `potatoclient.cmd.system` - System-level commands
- `potatoclient.cmd.test` - Test utilities for commands

#### Core Infrastructure (5)
- `potatoclient.proto` - Protobuf serialization/deserialization
- `potatoclient.ipc` - Inter-Process Communication (uses protobuf)
- `potatoclient.process` - Process management (starts Kotlin subprocesses)
- `potatoclient.state.dispatch` - State dispatch (uses proto/proto-map->clj-map)
- `potatoclient.instrumentation` - Malli instrumentation (dev-only)

### ✅ Included Namespaces (26)

These namespaces can be successfully instrumented:

#### Core Application (7)
- `potatoclient.config` - Configuration management
- `potatoclient.core` - Main application entry
- `potatoclient.dev` - Development utilities
- `potatoclient.i18n` - Internationalization
- `potatoclient.logging` - Logging configuration
- `potatoclient.main` - Main entry point
- `potatoclient.runtime` - Runtime detection

#### State Management (7)
- `potatoclient.state` - Core state atoms
- `potatoclient.state.config` - Configuration state
- `potatoclient.state.device` - Device state management
- `potatoclient.state.schemas` - Malli schemas for state
- `potatoclient.state.streams` - Stream state management
- `potatoclient.state.ui` - UI state
- `potatoclient.state.utils` - State utilities

#### UI Components (5)
- `potatoclient.ui.control_panel` - Control panel UI
- `potatoclient.ui.log_viewer` - Log viewer window
- `potatoclient.ui.main_frame` - Main application window
- `potatoclient.ui.startup_dialog` - Startup configuration dialog
- `potatoclient.ui.utils` - UI utilities

#### Other (7)
- `potatoclient.events.stream` - Event stream handling
- `potatoclient.guardrails.check` - Guardrails checking utility
- `potatoclient.guardrails_test` - Guardrails test namespace
- `potatoclient.reports` - Report generation
- `potatoclient.specs` - Centralized Malli specifications
- `potatoclient.theme` - Theme management
- `potatoclient.runtime` - Runtime environment detection

## Impact Analysis

### Coverage Gap
- **42% of namespaces excluded** (19 out of 45)
- All command-sending functionality excluded
- Core infrastructure for WebSocket/IPC excluded
- Protobuf serialization excluded
- State dispatch system excluded

### What We Can Measure
- Pure Clojure business logic
- State management (except dispatch)
- UI components
- Configuration and theming
- Internationalization
- Logging and utilities

### What We Cannot Measure
- Command generation and validation
- WebSocket communication
- Protobuf serialization/deserialization
- Inter-process communication
- Process lifecycle management
- State dispatch and change detection

## Reason for Exclusions

All excluded namespaces have one or more of these characteristics:

1. **Direct protobuf imports**: 
   ```clojure
   (:import [cmd.RotaryPlatform JonSharedCmdRotary$Root])
   ```

2. **Dependency on protobuf-using namespaces**:
   ```clojure
   (:require [potatoclient.proto :as proto])
   ```

3. **Java/Kotlin process management**:
   ```clojure
   (:import [potatoclient.java.process StreamProcessManager])
   ```

These cause `ClassNotFoundException` during Cloverage instrumentation because the classes aren't available when Cloverage loads the namespace for instrumentation.