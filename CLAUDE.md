# Developer Guide

PotatoClient is a multi-process video streaming client with dual H.264 WebSocket streams. Main process (Clojure) handles UI, subprocesses (Java) handle video streams.

## Quick Reference

```bash
make build        # Build JAR and compile protos (DEVELOPMENT)
make release      # Build optimized JAR (RELEASE) 
make run          # Run application  
make dev          # Run with GStreamer debug
make dev-reflect  # Run with reflection warnings
make nrepl        # REPL on port 7888
make proto        # Generate protobuf classes
make clean        # Clean all artifacts
```

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

**Java (Stream Processes)**
- `VideoStreamManager` - WebSocket + GStreamer pipeline
- Hardware decoder selection and fallback
- Auto-reconnection with exponential backoff

## Development Tasks

**Add Theme**
1. Add theme definition in `potatoclient.theme/themes`
2. Update `get-available-themes` function
3. Theme will appear in View menu

**Add Language**
1. Create new translation file in `resources/i18n/{locale}.edn` (e.g., `fr.edn` for French)
2. Add locale to `potatoclient.i18n/::locale` spec (e.g., `:fr`)
3. Add locale option in `potatoclient.state/::locale` spec (e.g., `:french`)
4. Update `load-translations!` in `i18n.clj` to include new locale in `locales` vector
5. Update `tr` function in `i18n.clj` to map new locale (e.g., `:french` → `:fr`)
6. Update menu in `core.clj` with new language option

**Add Event Type**
1. Define in `potatoclient.events.stream`
2. Handle in `VideoStreamManager.java`
3. Add to `ipc/message-handlers` dispatch table

**Modify Pipeline**
- Edit `VideoStreamManager.java`
- Decoder priority in `findBestH264Decoder()`

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
- `logs-state` - Log entries and buffering
- `ui-refs` - UI component references

All state functions include validation via clojure.spec.

## Spec & Orchestra Integration

PotatoClient uses comprehensive runtime validation through clojure.spec and Orchestra:

### What is Orchestra?
Orchestra is a drop-in replacement for `clojure.spec.test.alpha` that provides complete instrumentation:
- **Standard spec**: Only validates `:args` (function inputs)
- **Orchestra**: Validates `:args`, `:ret` (return values), and `:fn` (relationships between inputs/outputs)

### Implementation Details

**Every function uses `defn-spec`**:
```clojure
;; Instead of regular defn:
(defn get-theme []
  (:theme (load-config)))

;; We use defn-spec with return type:
(defn-spec get-theme ::theme
  []
  (:theme (load-config)))

;; Private functions use metadata:
(defn-spec ^:private ensure-config-dir! any?
  []
  ...)
```

**Automatic Instrumentation**:
- Development/test builds: Full instrumentation enabled automatically
- Release builds: No instrumentation overhead
- Controlled via environment variables: `POTATOCLIENT_RELEASE=true`

**Benefits**:
1. **Catch bugs early**: Invalid function calls fail immediately with clear error messages
2. **Living documentation**: Specs document expected types for all functions
3. **Development confidence**: Know immediately when data doesn't match expectations
4. **Zero production overhead**: Instrumentation disabled in release builds

### Adding Specs to New Code

When adding new namespaces or functions:

1. **Import Orchestra**:
```clojure
(ns my-namespace
  (:require [orchestra.core :refer [defn-spec]]
            [clojure.spec.alpha :as s]))
```

2. **Define specs for your domain**:
```clojure
(s/def ::width pos-int?)
(s/def ::height pos-int?)
(s/def ::dimensions (s/keys :req-un [::width ::height]))
```

3. **Use defn-spec for all functions**:
```clojure
(defn-spec calculate-area pos-int?
  [dimensions ::dimensions]
  (* (:width dimensions) (:height dimensions)))
```

4. **For private functions**:
```clojure
(defn-spec ^:private helper-fn string?
  [x any?]
  (str x))
```

## Build Types & Development Mode

### Development Build (default)
- Full Orchestra instrumentation enabled
- Runtime validation of all specs
- Helpful error messages for spec violations
- Window title shows `[DEVELOPMENT]`
- Enable with: `make build` or `make dev`

### Release Build (optimized)
- Orchestra instrumentation disabled
- AOT compilation with direct linking
- Metadata stripped (`:doc`, `:file`, `:line`)
- Window title shows `[RELEASE]`
- Enable with: `make release` or `POTATOCLIENT_RELEASE=true`

### Development Mode Features
Enable additional debugging with:
- `make dev-reflect` - Show reflection warnings
- `-Dpotatoclient.dev=true` - Enable dev namespace
- `POTATOCLIENT_DEV=true` - Environment variable

### Build Type Detection
The application detects build type via:
1. System property: `potatoclient.release`
2. Environment variable: `POTATOCLIENT_RELEASE`

Release builds show:
- Console: `"Running RELEASE build - instrumentation disabled"`
- Window: `PotatoClient v1.4.0 [RELEASE]`
- About: `"Orchestra instrumentation: Disabled (optimized)"`

## Technical Details

**Build**: Java 17+, Protobuf 3.15.0 (bundled)
**Streams**: Heat (900x720), Day (1920x1080)

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
1. Builds release versions with `POTATOCLIENT_RELEASE=true`
2. Enables AOT compilation and direct linking
3. Creates platform-specific packages:
   - **Linux**: AppImage with bundled JRE and GStreamer
   - **Windows**: Installer (.exe) and portable (.zip) with dependencies
   - **macOS**: DMG with bundled JRE (GStreamer required separately)

### Release Optimization
Release builds from CI have:
- **No instrumentation overhead**: Orchestra completely disabled
- **AOT compilation**: All Clojure code pre-compiled to bytecode
- **Direct linking**: Function calls are statically linked
- **Stripped metadata**: Smaller JAR size without dev metadata

### Verifying Release Builds
Users can verify they're running an optimized release:
1. Check console output on startup
2. Look for `[RELEASE]` in window title
3. Check Help → About dialog
4. Log shows: `"Control Center started (v1.4.0 RELEASE build)"`