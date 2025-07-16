# Developer Guide

PotatoClient is a multi-process video streaming client with dual H.264 WebSocket streams. Main process (Clojure) handles UI, subprocesses (Java) handle video streams.

## Important: Function Instrumentation

**ALWAYS** update `/src/potatoclient/instrumentation.clj` when:
- Adding new functions to any namespace
- Modifying function signatures  
- Removing functions

The instrumentation file contains Malli function schemas for runtime validation in development builds. Keep it synchronized with your code changes.

Example for new functions:
```clojure
;; In your namespace:
(defn process-data
  [data options]
  ...)

;; Add to instrumentation.clj:
(m/=> your-ns/process-data [:=> [:cat map? map?] any?])
```

## Quick Reference

```bash
make build        # Build JAR and compile protos (DEVELOPMENT)
make release      # Build optimized JAR (RELEASE) - automatically detected
make run          # Run application  
make dev          # Run with GStreamer debug
make dev-reflect  # Run with reflection warnings
make nrepl        # REPL on port 7888
make proto        # Generate protobuf classes
make clean        # Clean all artifacts
```

## Development vs Release Builds

### Development Build (`make build`)
- **Instrumentation**: Available via `(potatoclient.instrumentation/start!)`
- **Logging**: All levels (DEBUG, INFO, WARN, ERROR) to console and timestamped file in `./logs/`
- **Window Title**: Shows `[DEVELOPMENT]`
- **Console**: "Running DEVELOPMENT build - instrumentation available"
- **Metadata**: Full debugging information included
- **Performance**: Slower due to validation overhead

### Release Build (`make release`)
- **Instrumentation**: Completely disabled
- **Logging**: Only WARN/ERROR to stdout/stderr, no file logging
- **Window Title**: Shows `[RELEASE]`
- **Console**: "Running RELEASE build - instrumentation disabled"
- **Metadata**: Stripped (smaller JAR)
- **Performance**: Optimized with AOT compilation and direct linking
- **Auto-Detection**: Release JARs automatically know they're release builds (no flags needed)

## Architecture

### Key Components

**Clojure (Main Process)**
- `potatoclient.main` - Entry point with dev mode support
- `potatoclient.core` - Application initialization, menu creation
- `potatoclient.state` - Centralized state management with specs
- `potatoclient.process` - Subprocess lifecycle with type hints
- `potatoclient.proto` - Protobuf serialization
- `potatoclient.ipc` - Message routing and dispatch
- `potatoclient.config` - Platform-specific configuration
- `potatoclient.i18n` - Localization (English, Ukrainian)
- `potatoclient.theme` - Theme support (Sol Dark, Sol Light)
- `potatoclient.runtime` - Runtime detection utilities
- `potatoclient.specs` - Centralized Malli schemas
- `potatoclient.instrumentation` - Function schemas (dev only)
- `potatoclient.logging` - Telemere-based logging configuration

**Java (Stream Processes)**
- `VideoStreamManager` - WebSocket + GStreamer pipeline
- Hardware decoder selection and fallback
- Auto-reconnection with exponential backoff
- Buffer pooling for zero-allocation streaming
- Optimized lock-free data pushing
- Direct pipeline without frame rate limiting

## Development Tasks

**Add Theme**
1. Add theme definition in `potatoclient.theme/themes`
2. Update `get-available-themes` function
3. Theme will appear in View menu

**Add Language**
1. Create new translation file in `resources/i18n/{locale}.edn` (e.g., `fr.edn` for French)
2. Add locale to `potatoclient.specs/locale` (e.g., `:fr`)
3. Update `load-translations!` in `i18n.clj` to include new locale
4. Update `tr` function in `i18n.clj` to map new locale
5. Update menu in `core.clj` with new language option

**Add Event Type**
1. Define in `potatoclient.events.stream`
2. Handle in `VideoStreamManager.java`
3. Add to `ipc/message-handlers` dispatch table

**Modify Pipeline**
- Edit `GStreamerPipeline.java` for pipeline structure
- Decoder priority in `GStreamerPipeline.java` constructor
- Pipeline: appsrc → h264parse → decoder → queue → videosink

**Update Protocol**
1. Edit `.proto` files
2. Run `make proto`
3. Update `potatoclient.proto` accessors

## Localization

Translations are stored in separate EDN files under `resources/i18n/`:
- `en.edn` - English translations
- `uk.edn` - Ukrainian translations

**Translation File Format**:
```clojure
{:app-title "PotatoClient"
 :menu-file "File"
 :menu-help "Help"
 ;; ... more translations
}
```

**Reloading Translations (Dev Mode)**:
```clojure
;; In REPL or development:
(potatoclient.i18n/reload-translations!)
```

This allows editing translation files and seeing changes without restarting.

## Logging System

PotatoClient uses [Telemere](https://github.com/taoensso/telemere) for high-performance logging with different behaviors for development and production builds.

### Development Logging
- **All log levels**: DEBUG, INFO, WARN, ERROR
- **Dual output**: Console and timestamped file
- **Log location**: `./logs/potatoclient-{version}-{timestamp}.log`
- **Example**: `./logs/potatoclient-dev-20250716-131710.log`

### Production Logging
- **Critical only**: WARN and ERROR levels
- **Console only**: stdout/stderr, no file creation
- **Zero overhead**: No performance impact from debug logging
- **Clean deployment**: No log file management needed

### Usage in Code
```clojure
(require '[potatoclient.logging :as logging])

;; Standard logging
(logging/log-debug "Debug information")
(logging/log-info "Process started")
(logging/log-warn "Connection slow")
(logging/log-error "Connection failed")

;; Stream events
(logging/log-stream-event :heat :connected {:url "wss://example.com"})
(logging/log-stream-event :day :error {:message "Timeout"})
```

### Java Integration
Java subprocesses send log messages via IPC, which are processed by the Clojure logging system. This ensures consistent logging behavior and allows the main process to control what gets logged based on build type.

## Configuration

Platform-specific locations:
- Linux: `~/.config/potatoclient/`
- macOS: `~/Library/Application Support/PotatoClient/`
- Windows: `%LOCALAPPDATA%\PotatoClient\`

Config format (EDN):
```clojure
{:theme :sol-dark
 :domain "sych.local"
 :locale :english}
```

## State Management

State is separated by concern:
- `streams-state` - Process references
- `app-config` - Runtime configuration
- `ui-refs` - UI component references

All state functions include validation via Malli schemas.

## Malli Integration

PotatoClient uses comprehensive runtime validation through Malli schemas:

### What is Malli?
Malli is a high-performance data and function schema library that provides:
- **Function schemas**: Validate function inputs, outputs, and relationships
- **Data schemas**: Define and validate data structures
- **Better error messages**: Human-readable error explanations
- **Performance**: Faster validation than clojure.spec
- **Clj-kondo support**: Static analysis integration

### Implementation Details

**Centralized Schemas**:
All data schemas are defined in `potatoclient.specs` namespace for reuse across the codebase.

**Function Instrumentation**:
All function schemas are defined in `potatoclient.instrumentation` namespace (excluded from AOT compilation).

**Private Functions**:
Use `defn-` instead of `defn ^:private` for idiomatic Clojure code.

### Adding New Functions

When adding new functions:

1. **Write the function**:
```clojure
(defn calculate-area
  "Calculate area of rectangle"
  [dimensions]
  (* (:width dimensions) (:height dimensions)))

(defn- validate-dimensions
  "Private helper to validate dimensions"
  [dimensions]
  (and (pos? (:width dimensions))
       (pos? (:height dimensions))))
```

2. **Add schemas to instrumentation.clj**:
```clojure
;; In the appropriate section of instrumentation.clj:
(m/=> your-ns/calculate-area [:=> [:cat ::specs/dimensions] pos-int?])
(m/=> your-ns/validate-dimensions [:=> [:cat ::specs/dimensions] boolean?])
```

3. **Define new data schemas if needed** (in specs.clj):
```clojure
(def dimensions
  "Rectangle dimensions"
  [:map
   [:width pos-int?]
   [:height pos-int?]])
```

### Instrumentation Usage

**Development REPL**:
```clojure
;; Enable instrumentation manually:
(require 'potatoclient.instrumentation)
(potatoclient.instrumentation/start!)

;; Now all function calls are validated
(calculate-area {:width -5 :height 10})
;; => Throws detailed error about invalid input
```

## Build Types & Development Mode

### Development Build (default)
- Malli instrumentation available (manual loading)
- Full logging (DEBUG, INFO, WARN, ERROR) to console and `./logs/potatoclient-{version}-{timestamp}.log`
- Full error messages and stack traces
- Window title shows `[DEVELOPMENT]`
- Enable with: `make build`

### Release Build (optimized)
- No instrumentation overhead
- Minimal logging (WARN, ERROR only) to stdout/stderr
- AOT compilation with direct linking
- Metadata stripped (`:doc`, `:file`, `:line`)
- Window title shows `[RELEASE]`
- Enable with: `make release`
- **Self-contained**: Release JARs automatically detect they're release builds

### Build Type Detection
The application detects build type via `potatoclient.runtime/release-build?` which checks:
1. System property: `potatoclient.release`
2. Environment variable: `POTATOCLIENT_RELEASE`
3. Embedded `RELEASE` marker file (in release JARs)

## Technical Details

**Build**: Java 17+, Protobuf 3.15.0 (bundled)
**Streams**: Heat (900x720), Day (1920x1080)

**Performance Optimizations**:
- Zero-allocation streaming with buffer pooling
- Lock-free fast path for video data
- Direct pipeline without unnecessary elements
- Hardware acceleration prioritized

**Hardware Decoders** (priority):
1. NVIDIA (nvh264dec)
2. Direct3D 11 (d3d11h264dec) 
3. Intel QSV (msdkh264dec)
4. VA-API/VideoToolbox
5. Software fallback

**Type Hints**: Added to prevent reflection in:
- JFrame operations
- Process/IO operations
- File operations
- Date formatting

## CI/CD & Release Process

### GitHub Actions Workflow
The CI pipeline (`/.github/workflows/release.yml`) automatically:
1. Builds release versions with embedded release marker
2. Enables AOT compilation and direct linking
3. Creates platform-specific packages:
   - **Linux**: AppImage with bundled JRE and GStreamer
   - **Windows**: Installer (.exe) and portable (.zip) with dependencies
   - **macOS**: DMG with bundled JRE (GStreamer required separately)

### Release Optimization
Release builds from CI have:
- **No instrumentation overhead**: Malli completely disabled
- **AOT compilation**: All Clojure code pre-compiled to bytecode
- **Direct linking**: Function calls are statically linked
- **Stripped metadata**: Smaller JAR size without dev metadata
- **Embedded release marker**: Self-identifying as release build

### Verifying Release Builds
Users can verify they're running an optimized release:
1. Check console output on startup
2. Look for `[RELEASE]` in window title
3. No log files created
4. Only critical messages (warnings/errors) appear on console

## Recent Optimizations

### Video Streaming Performance
1. **Buffer Pooling**: Implemented zero-allocation streaming with reusable buffers
2. **Lock Optimization**: Minimized lock contention by acquiring/releasing locks only during critical operations
3. **Pipeline Simplification**: Removed unnecessary elements (videorate) for better performance
4. **Fixed Keyframe Bug**: Removed faulty keyframe detection that prevented video playback

### Window Event Handling
1. **Fixed X Button**: Removed duplicate window listeners that prevented proper close handling
2. **Consistent Behavior**: All close methods now follow the same IPC message flow

### Logging System Simplification
1. **Telemere Integration**: Replaced custom logging with high-performance Telemere library
2. **Removed Log UI**: Eliminated in-app log viewer for cleaner interface
3. **Smart Logging**: Development builds log everything to timestamped files, release builds only output critical events
4. **Zero Overhead**: Conditional evaluation ensures no performance impact in production

## Best Practices

1. **Always update instrumentation.clj** when adding/modifying functions
2. **Use `defn-` for private functions** (not `defn ^:private`)
3. **Test with instrumentation enabled** during development
4. **Run release builds for production** to avoid validation overhead
5. **Keep schemas in sync** with actual function implementations
6. **Use existing schemas** from `potatoclient.specs` when possible
7. **Use appropriate log levels**: DEBUG for development details, INFO for normal operations, WARN for issues, ERROR for failures
8. **Check logs during development**: Look in `./logs/` directory for timestamped log files
9. **Monitor production logs**: Only warnings and errors appear on stdout/stderr in release builds