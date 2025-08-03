# Developer Guide

PotatoClient is a high-performance multi-process live video streaming client with dual H.264 WebSocket streams. The architecture uses Transit-based IPC to completely isolate protobuf handling in Kotlin subprocesses, while the main Clojure process handles UI and coordination.

## Architecture Overview

- **Main Process (Clojure)**: UI, state management with Transit app-db, subprocess coordination
- **Command Subprocess (Kotlin)**: Receives Transit commands, converts to protobuf, sends via WebSocket
- **State Subprocess (Kotlin)**: Receives protobuf state from WebSocket, converts to Transit, sends to main process
- **Video Subprocesses (Kotlin)**: Handle H.264 video streams with hardware acceleration

**Key architectural principles**: 
- Protobuf is completely isolated in Kotlin subprocesses
- All Clojure functions use Guardrails `>defn` for runtime validation
- Single app-db atom following re-frame pattern for state management
- Transit/MessagePack for all IPC communication
- **Clean Architecture**: Single approach using Transit handlers - no manual serialization
- **Keywords Everywhere**: All data is keywords and numbers in Clojure (except log message text)
- **No Legacy Code**: Clean implementations only - no backward compatibility layers

**Documentation:**
- **Transit Architecture**: [.claude/transit-architecture.md](.claude/transit-architecture.md) - Complete Transit implementation details
- **Transit Protocol**: [.claude/transit-protocol.md](.claude/transit-protocol.md) - Message protocol specification (keywords everywhere!)
- **Kotlin Subprocesses**: [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md) - Video streaming and subprocess details
- **Protobuf Commands**: [.claude/protobuf-command-system.md](.claude/protobuf-command-system.md) - Command system implementation
- **Proto Explorer**: [tools/proto-explorer/README.md](tools/proto-explorer/README.md) - Malli spec generation from protobuf
- **Linting Guide**: [.claude/linting-guide.md](.claude/linting-guide.md) - Code quality tools and false positive filtering
- **TODO and Technical Debt**: [TODO_AGGREGATED.md](TODO_AGGREGATED.md) - Comprehensive tracking of all pending work

## Important: Function Validation with Guardrails

**ALWAYS** use Guardrails' `>defn` and `>defn-` instead of regular `defn` and `defn-` for all functions. This provides runtime validation in development and is automatically removed in release builds.

**Key requirements**:
- Every function must use `>defn` or `>defn-` (never raw `defn`)
- All data schemas must be defined in `potatoclient.specs` namespace
- Generated protobuf specs are in `potatoclient.specs.*` namespaces
- Function instrumentation goes in `potatoclient.instrumentation` (excluded from AOT)
- Use `make report-unspecced` to find functions missing Guardrails

### Basic Usage

```clojure
(ns your.namespace
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn >defn- >def | ? =>]]))

;; Public function
(>defn process-data
  "Process data with options"
  [data options]
  [map? map? => map?]  ; arg specs followed by => and return spec
  (merge data options))

;; Private function  
(>defn- validate-input
  "Check if input is valid"
  [input]
  [string? => boolean?]
  (not (clojure.string/blank? input)))

;; Multi-arity function
(>defn fetch-data
  ([id]
   [int? => map?]
   (fetch-data id {}))
  ([id options]
   [int? map? => map?]
   (retrieve-from-db id options)))

;; Using qualified specs
(>defn save-config!
  [config]
  [:potatoclient.specs/config => boolean?]
  (write-to-disk config))

;; Nilable values with ?
(>defn find-user
  [id]
  [int? => (? map?)]  ; returns map or nil
  (get users id))

;; Such-that constraints with |
(>defn divide
  [x y]
  [number? number? | #(not= y 0) => number?]
  (/ x y))

;; Variadic keyword arguments
(>defn create-user
  [name & {:keys [age email] :or {age 0}}]
  [string? (? (s/* any?)) => map?]  ; Use (? (s/* any?)) for & {:keys ...}
  {:name name :age age :email email})
```

### Checking for Unspecced Functions

To find functions still using raw `defn`/`defn-`:

```bash
# Generate report of functions without Guardrails
make report-unspecced
```

Alternatively, in the REPL:
```clojure
(require '[potatoclient.guardrails.check :as check])

;; Print report
(check/print-report)

;; Get data structure
(check/find-unspecced-functions)
```

### Configuration

Guardrails is controlled by:
1. JVM option `-Dguardrails.enabled=true` (set in deps.edn aliases)
2. Configuration file `guardrails.edn` in project root
3. Runtime checks only in development, automatically disabled in release builds

### Important Notes

- **Mandatory**: Use `>defn` and `>defn-` for ALL functions (never raw `defn`)
- Guardrails is automatically enabled with `make dev` and REPL aliases (nrepl, mcp)
- Release builds have zero overhead - all checks are compiled away
- Use precise specs from `potatoclient.specs` namespace
- Private functions use `>defn-` (not `defn ^:private`)
- All validation happens at runtime during development only
- **Prefer updating specs over changing function behavior** - If a function naturally returns a useful value (e.g., `:stopped`, a process object, etc.), update the spec to expect that value rather than forcing the function to return `nil`
- For variadic keyword arguments `& {:keys [...]}`, use `(? (s/* any?))` in the spec
- Remember to require `[clojure.spec.alpha :as s]` when using spec-based Guardrails

### Handling Spec Failures

When Guardrails reports a spec failure, evaluate which needs adjustment:

1. **Prefer informative return values** - If a function naturally returns meaningful data, update the spec
   ```clojure
   ;; BAD: Forcing nil return when function has useful result
   (>defn- stop-process
           [process]
           [::process => nil?]
           (do-stop process)
           nil)  ; Artificial constraint
   
   ;; GOOD: Let function return its natural, informative value
   (>defn- stop-process
           [process]
           [::process => ::process-state]  ; Returns :stopped, :failed, etc.
           (do-stop process))
   ```

2. **Check the function's actual behavior** - Does it naturally return a value?
   ```clojure
   ;; If clipboard/contents! returns the clipboard object for chaining:
   (>defn- copy-to-clipboard
           [text]
           [string? => any?]  ; Fix the spec, not the function
           (clipboard/contents! text))
   ```

3. **Side-effect-only functions should return nil** - Only when the return value is truly meaningless
   ```clojure
   ;; Functions that only perform side effects with no useful return:
   (>defn- log-message
           [msg]
           [string? => nil?]
           (println msg)
           nil)  ; Explicit nil for pure side-effect
   ```

4. **Consider idiomatic Clojure** - Many functions return useful values for chaining or debugging
   - Process operations often return status (`:stopped`, `:running`)
   - File operations often return the file object
   - State mutations often return the new state
   - UI operations often return the component for chaining

**Rule of thumb**: Preserve the natural, informative behavior of functions. Only enforce `nil` returns for pure side-effect operations with no meaningful return value. Update specs to match the function's natural behavior rather than artificially constraining it.

## Build System

**For all build commands and detailed documentation, run: `make help`**

The Makefile is self-documenting and includes comprehensive inline documentation for all targets:
- Primary development commands with startup times and features
- Build and release targets with optimization details
- Code quality tools with version numbers and configurations
- Lint report generators with false positive filtering capabilities (~56% false positive reduction)

Key development commands:
- `make dev` - Primary development command with full validation
- `make nrepl` - REPL development on port 7888
- `make mcp-server` - MCP server for Claude integration on port 7888
- `make lint` - Run all code quality checks (clj-kondo, ktlint, detekt)
- `make lint-report-filtered` - Generate lint report with false positives filtered out
- `make test` - Run tests with automatic logging to `logs/test-runs/`
- `make test-summary` - View latest test run summary
- `make report-unspecced` - Find functions without Guardrails specs
- `make help` - View all available commands with descriptions

## Development vs Release Builds

### Development Mode (`make dev`)
- **Guardrails**: Full validation with detailed error messages
- **Reflection**: Warnings enabled to catch performance issues
- **Logging**: All levels (DEBUG, INFO, WARN, ERROR) to console and timestamped file in `./logs/`
- **GStreamer**: Debug output enabled (GST_DEBUG=3)
- **Window Title**: Shows `[DEVELOPMENT]`
- **Performance**: Slower due to validation and logging overhead
- **How it works**: Runs directly with Clojure using `:run` alias

### Production-like (`make run`)
- **Guardrails**: Disabled
- **Reflection**: No warnings
- **Logging**: Standard levels to console and file
- **Window Title**: Shows `[DEVELOPMENT]`
- **Performance**: Near-production speed
- **How it works**: Builds and runs JAR file

### Release Build (`make release`)
- **Guardrails**: Completely removed from bytecode
- **Logging**: Only WARN/ERROR to platform-specific locations
- **Window Title**: Shows `[RELEASE]`
- **Metadata**: Stripped (smaller JAR)
- **Performance**: Optimized with AOT compilation and direct linking
- **Auto-Detection**: Release JARs automatically know they're release builds (no flags needed)

## Code Quality and Linting

For comprehensive information on code quality, linting tools, and static analysis, see:

**[.claude/linting-guide.md](.claude/linting-guide.md)**

This guide covers:
- **Clojure**: clj-kondo (v2025.06.05) with Guardrails, Seesaw, and Telemere support
- **Kotlin**: ktlint (v1.5.0) and detekt (v1.23.7) for style and analysis
- Running linters and generating reports
- Advanced false positive filtering (removes ~56% of false positives)
- Custom lint-as mappings for Clojure macros
- Integration with development workflow
- Best practices for maintaining code quality

**Quick commands**:
- `make lint` - Run all linters
- `make lint-report-filtered` - Generate report with false positives filtered out

## Testing Infrastructure

### Test Execution and Logging

The test suite includes comprehensive logging and analysis capabilities:

**Key Commands**:
- `make test` - Run all tests with automatic logging to timestamped directories
- `make test-summary` - View the latest test run summary
- `make test-coverage` - Generate comprehensive coverage report for Clojure, Java, and Kotlin
- `make report-unspecced` - Find functions without Guardrails specs

**Test Logging System**:
- All test runs are automatically logged to `logs/test-runs/YYYYMMDD_HHMMSS/`
- Each run generates:
  - `test-full.log` - Complete test output
  - `test-full-summary.txt` - Compact analysis of results
  - `test-full-failures.txt` - Extracted failures for quick review
- Logs are automatically compacted and analyzed by `scripts/compact-test-logs.sh`
- Latest test run is symlinked to `logs/test-runs/latest/`

**Coverage Reports**:
- Coverage analysis uses JaCoCo via the `cloverage` tool
- Reports generated in `target/coverage/` include:
  - HTML reports for browser viewing
  - XML reports for CI integration
  - Console summary of coverage percentages
- Run `make test-coverage` then open `target/coverage/index.html`

### Test Infrastructure

Tests use Transit-based architecture with proper isolation:

**Key Components**:
- `test/potatoclient/transit_integration_test.clj` - Transit integration tests
- `test/potatoclient/test_utils/protobuf.clj` - Protobuf test utilities
- App-db reset fixtures for test isolation
- Condition waiting helpers for async testing

**Test Categories**:
- **Unit Tests**: Transit encoding/decoding, command generation, state management
- **Integration Tests**: Subprocess communication, event handling, app-db updates
- **Property Tests**: Malli schema validation and generators

**Example Test Pattern**:
```clojure
(use-fixtures :each reset-app-db-fixture)

(deftest test-transit-encoding-decoding
  (testing "Transit message encoding and decoding"
    (let [test-data {:action "ping" :params {:test true}}
          encoded (encode-transit test-data)
          decoded (decode-transit encoded)]
      (is (= test-data decoded)))))
```

### Test Organization

**Unit Tests**:
- Transit encoding/decoding and message envelopes
- Command generation with Transit
- App-db state management
- Guardrails validation

**Integration Tests**:
- Transit-based subprocess communication
- App-db subsystem updates
- Process lifecycle management
- Event system integration

**Quality Assurance**:
- Guardrails catches runtime type errors in development
- Comprehensive linting with false positive filtering
- Test coverage analysis for all languages
- Automated test logging and summary generation

## Architecture

### Transit-Based IPC Architecture

The system uses Transit/MessagePack for all inter-process communication:

**Clean Transit Handler Architecture**
- All message serialization uses Transit handlers - no manual map building
- Protobuf objects automatically serialize with proper tagging
- Event messages use type-safe handler classes
- Consistent approach across entire system

**Keyword-Based Data Model**
- **In Clojure**: Everything is keywords and numbers (plus booleans)
- **Only Strings**: Log message text and error descriptions
- **Automatic Conversion**: All enum-like values become keywords
- **Examples**:
  - Message types: `:command`, `:event`, `:state`
  - Event types: `:gesture`, `:navigation`, `:window`
  - Stream types: `:heat`, `:day`
  - Gesture types: `:tap`, `:double-tap`, `:pan`, `:swipe`
- **See [.claude/transit-protocol.md](.claude/transit-protocol.md) for the complete protocol specification**

**Transit Handler Coverage**
- ✅ Protobuf state messages (all data types)
- ✅ Gesture events with proper keyword conversion
- ✅ Navigation events (mouse interactions)
- ✅ Window events (resize, focus, minimize)
- ✅ Control messages (rate limiting, configuration)
- ✅ Error messages (preserving text content)
- ✅ Log messages (preserving text content)

**Transit Namespaces**
- `potatoclient.transit.core` - Transit reader/writer creation and message envelope handling
- `potatoclient.transit.app-db` - Single source of truth atom following re-frame pattern
- `potatoclient.transit.commands` - Command API that creates Transit messages
- `potatoclient.transit.subprocess-launcher` - Process lifecycle management for Transit subprocesses
- `potatoclient.transit.handlers` - Message handlers for incoming Transit messages
- `potatoclient.transit.keyword-handlers` - Automatic keyword conversion for enums

**Key Components**

**Clojure (Main Process)**
- `potatoclient.main` - Entry point with dev mode support
- `potatoclient.core` - Application initialization, menu creation
- `potatoclient.state` - Centralized state management with Transit app-db
- `potatoclient.process` - Subprocess lifecycle with type hints
- `potatoclient.ipc` - Message routing and dispatch
- `potatoclient.config` - Platform-specific configuration
- `potatoclient.i18n` - Localization (English, Ukrainian)
- `potatoclient.theme` - Theme support (Sol Dark, Sol Light)
- `potatoclient.runtime` - Runtime detection utilities
- `potatoclient.specs` - Centralized Malli schemas
- `potatoclient.instrumentation` - Function schemas (dev only)
- `potatoclient.logging` - Telemere-based logging configuration
- `potatoclient.gestures.handler` - Gesture event processing and command generation
- `potatoclient.gestures.config` - Zoom-based speed configurations

**Kotlin (Subprocess Components)**
- `CommandSubprocess` - Receives Transit commands, converts to protobuf, sends via WebSocket
- `StateSubprocess` - Receives protobuf state, uses Transit handlers for automatic serialization
- `VideoStreamManager` - Video streaming with Transit-based communication and gesture support
- `TransitCommunicator` - Handles Transit message framing and serialization over stdin/stdout
- `TransitMessageProtocol` - Unified message protocol for subprocess communication
- `VideoStreamTransitAdapter` - Adapter for video streams to use Transit protocol
- `TransitKeys` - Pre-created Transit keyword constants for performance
- `TransitExtensions` - Kotlin extension properties for clean keyword-based map access
- `LoggingUtils` - Individual log files per subprocess in development mode
- `SimpleCommandBuilder` - Creates protobuf commands from Transit message data (legacy, use handlers)
- `SimpleProtobufHandlers` - Transit WriteHandlers for automatic protobuf serialization
- `SimpleCommandHandlers` - Builds protobuf commands from Transit messages (replaces builder)
- `StdoutInterceptor` - Captures stdout for clean subprocess communication
- `GestureRecognizer` - Detects tap, double-tap, pan, and swipe gestures
- `PanController` - Manages pan gesture state and throttling
- `MouseEventHandler` - Processes mouse events and delegates to gesture recognizer
- For detailed implementation, see [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md)

### Transit Message Flow

1. **Commands (Clojure → Server)**:
   ```
   Clojure UI → Transit command → CommandSubprocess → Protobuf → WebSocket → Server
   ```

2. **State Updates (Server → Clojure)**:
   ```
   Server → WebSocket → Protobuf → StateSubprocess → Transit WriteHandlers → Clojure app-db
   ```

3. **Video Streams (Server → UI)**:
   ```
   Server → WebSocket → H.264 → VideoStreamManager → Transit events → Clojure UI
   ```

4. **Subprocess Communication**:
   ```
   All subprocesses → TransitMessageProtocol → Transit messages → Main process
   ```

5. **Key Features**:
   - Complete protobuf isolation in Kotlin
   - Unified Transit protocol for all IPC
   - Individual subprocess logging in development
   - Debouncing to prevent duplicate state updates
   - Token bucket rate limiting (configurable via Transit messages)
   - Automatic reconnection with exponential backoff
   - Clean subprocess lifecycle management
   - SSL certificate validation disabled for internal use

### Gesture Recognition System

**Supported Gestures**:
- **Tap**: Single click → `rotary-goto-ndc` command
- **Double-tap**: Double click → `cv-start-track-ndc` command  
- **Pan**: Click and drag → `rotary-set-velocity` commands with zoom-based speed
- **Swipe**: Quick flick gesture (detected but not currently used)

**Gesture Configuration** (`resources/config/gestures.edn`):
- Movement thresholds for gesture detection
- Timing parameters (tap duration, double-tap interval)
- Zoom-based speed configurations per camera type
- Dead zone and curve steepness for precise control

**Speed Calculation**:
- Speed varies by zoom level (5 levels: 0-4)
- Each camera type (heat/day) has unique speed curves
- Dead zone filtering prevents micro-movements
- Exponential curve mapping for intuitive control
- NDC (Normalized Device Coordinates) for resolution independence

### Transit Message Protocol

All inter-process communication follows a standardized message protocol defined in `TRANSIT_MESSAGE_PROTOCOL_SPEC.md`. The key aspects are:

**Message Envelope Structure**:
```clojure
{:msg-type keyword?   ; Message type from MessageType enum (e.g., :command)
 :msg-id   string?    ; UUID for message tracking  
 :timestamp long?     ; Unix timestamp in milliseconds
 :payload  map?}      ; Message-specific payload
```

**Java Enum Integration**:
- Message types defined in `potatoclient.transit.MessageType` enum
- Event types defined in `potatoclient.transit.EventType` enum  
- Enums automatically convert to keywords in Clojure (e.g., `MessageType.COMMAND` → `:command`)
- Use `EventType.GESTURE.keyword` in Kotlin to access Transit keyword

**Automatic Keyword Conversion**:
- Transit configured to automatically convert string keys to keywords
- All enum values become keywords
- No manual `keywordize-keys` needed in Clojure handlers
- Kotlin uses Transit handlers for automatic serialization

**Message Validation**:
- All messages validated with Malli schemas in `potatoclient.specs`
- Use `potatoclient.transit.validation/validate-message` to check messages
- Schemas for: command, response, request, log, error, status, metric, event

**Kotlin MessageBuilder**:
```kotlin
// In any subprocess
val messageBuilder = protocol.messageBuilder()

// Send a gesture event
val message = messageBuilder.gestureEvent(
    EventType.TAP,
    timestamp = System.currentTimeMillis(),
    canvasWidth = 800,
    canvasHeight = 600,
    aspectRatio = 1.33,
    streamType = "heat",
    additionalData = mapOf("x" to 100, "y" to 200)
)

// Send a command
val cmd = messageBuilder.command(
    "rotary-goto-ndc",
    mapOf("channel" to "heat", "x" to 0.5, "y" to -0.5)
)
```

**Clojure Usage**:
```clojure
;; Messages arrive with keyword keys automatically
(defmethod handle-message :event
  [_ stream-key payload]
  (case (:type payload)  ; Already a keyword!
    :gesture (handle-gesture (:gesture-type payload))
    :navigation (handle-nav payload)))

;; All data uses keywords
(let [gesture-type (:gesture-type event)  ; :tap, :double-tap, :pan, :swipe
      stream-type (:stream-type event)    ; :heat or :day
      msg-type (:msg-type message)]       ; :command, :event, :state
  (process-gesture gesture-type stream-type))
```

**Kotlin Usage with Extensions**:
```kotlin
// Clean access using extension properties
when (msg.msgType) {
    MessageType.COMMAND.keyword -> {
        val action = msg.payload?.action ?: "ping"
        val params = msg.payload?.params
        handleCommand(action, params)
    }
    MessageType.EVENT.keyword -> {
        val eventType = msg.payload?.get(TransitKeys.TYPE)
        when (eventType) {
            EventType.GESTURE.keyword -> handleGesture(msg.payload)
            EventType.NAVIGATION.keyword -> handleNavigation(msg.payload)
        }
    }
}

// No more string-based map access everywhere!
val msgId = msg.msgId  // Clean property access
val timestamp = msg.timestamp
val batteryLevel = stateUpdate.system?.batteryLevel
```

For the complete protocol specification, see `.claude/transit-protocol.md`.

## Transit Handler Architecture (COMPLETED)

The codebase now uses Transit handlers for all message serialization, providing a clean, consistent architecture:

**Single Approach**: All messages use Transit handlers - no manual serialization
- ✅ Protobuf state messages → Automatically tagged and serialized via `SimpleProtobufHandlers`
- ✅ Commands → Built via `SimpleCommandHandlers` from Transit messages
- ✅ All enums → Automatically converted to keywords
- ✅ Error and log messages → Proper handling with text preservation

**Key Benefits**:
- **Type Safety**: Protobuf types ensure compile-time checking
- **Zero Manual Conversion**: Handlers do all the work
- **Consistency**: All messages follow same pattern
- **Performance**: No manual map building overhead
- **Clean Code**: StateSubprocess just sends protobuf objects directly

**Implementation Status**:
- ✅ Kotlin: `SimpleProtobufHandlers.kt` - WriteHandlers for all protobuf types
- ✅ Kotlin: `SimpleCommandHandlers.kt` - Command building from Transit
- ✅ Integration: Both subprocesses updated to use handlers
- ✅ Testing: Verified handlers work correctly

## UI Utilities

PotatoClient includes a set of UI utility functions in `potatoclient.ui.utils` adapted from the ArcherBC2 example:

### Debouncing for Seesaw Bindings

The `mk-debounced-transform` function prevents rapid UI updates by only propagating changes when values actually differ:

```clojure
(require '[potatoclient.ui.utils :as ui-utils]
         '[seesaw.bind :as bind])

;; Debounce slider updates
(bind/bind *state
           (bind/some (ui-utils/mk-debounced-transform #(:brightness %)))
           (bind/value brightness-slider))

;; Debounce complex state extraction
(bind/bind *state
           (bind/some (ui-utils/mk-debounced-transform 
                       #(get-in % [:profile :settings :volume])))
           (bind/value volume-slider))
```

### General Purpose Debouncing

For non-binding scenarios, use the `debounce` function:

```clojure
;; Auto-save with 1-second delay
(def save-settings! 
  (ui-utils/debounce 
    (fn [settings] 
      (config/save-config! settings))
    1000))

;; Search as you type
(def search!
  (ui-utils/debounce
    (fn [query]
      (perform-search query))
    300))
```

### Throttling

Unlike debouncing, throttling guarantees regular execution during continuous calls:

```clojure
;; Update preview at most every 100ms during typing
(def update-preview!
  (ui-utils/throttle 
    (fn [text]
      (render-markdown-preview text))
    100))
```

### Other UI Helpers

```clojure
;; Batch multiple UI updates
(ui-utils/batch-updates
  [#(seesaw/config! label1 :text "Updated")
   #(seesaw/config! label2 :visible? false)  
   #(seesaw/config! button :enabled? true)])

;; Show busy cursor during long operations
(ui-utils/with-busy-cursor frame
  (fn []
    (process-large-file)))

;; Preserve text selection during updates
(ui-utils/preserve-selection text-area
  (fn []
    (seesaw/text! text-area (format-code (seesaw/text text-area)))))
```


### Common Development Tasks

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

**Add or Modify Gesture**
1. Update gesture detection logic in `potatoclient.kotlin.gestures.GestureRecognizer`
2. Add/modify gesture type in `GestureType.kt` enum
3. Update handler in `potatoclient.gestures.handler` namespace
4. Adjust thresholds in `resources/config/gestures.edn` if needed
5. Add tests for new gesture behavior

**Adjust Pan Speed by Zoom**
1. Edit `resources/config/gestures.edn` zoom-speed-config section
2. Modify max-rotation-speed values for each zoom-table-index (0-4)
3. Adjust curve-steepness for different acceleration curves
4. Test with different dead-zone-radius values for precision

**Help Menu Features**
- **About Dialog**: Shows application version, build type (DEVELOPMENT/RELEASE)
- **View Logs**: Opens log directory in system file explorer
  - Development: Shows `./logs/` directory
  - Release: Shows platform-specific user directory (see Logging System)

**Add Event Type**
1. Define in `potatoclient.transit.EventType` Java enum with Transit Keyword
2. Handle in Kotlin subprocess (see [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md#event-system-integration))
3. Add handler in `potatoclient.events.stream` or `potatoclient.gestures.handler` for processing

**Event Types** (defined in `EventType.java` enum):
- `NAVIGATION` - Mouse interactions (move, click, drag, wheel)
- `WINDOW` - Window state changes (resize, focus, minimize)
- `FRAME` - Frame timing and rendering events
- `ERROR` - Video stream errors
- `GESTURE` - Touch gestures (tap, double-tap, pan, swipe)

**Protobuf and Command System**
See [.claude/protobuf-command-system.md](.claude/protobuf-command-system.md) for:
- Command system architecture and package structure
- Command validation and domain specs
- Infrastructure for all platforms (core, rotary, day camera)
- Command flow and JSON output format
- Testing and debugging utilities
- Protobuf implementation details and troubleshooting

**Testing and Validation**
See [docs/TESTING_AND_VALIDATION.md](docs/TESTING_AND_VALIDATION.md) for:
- Comprehensive test suite documentation
- Protobuf validation with buf.validate annotations
- TypeScript reference implementation location
- Test organization and running instructions
- Debugging test failures and common issues

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
- **Dual output**: Console AND file logging for critical events
- **Platform-specific log locations**:
  - Linux: `~/.local/share/potatoclient/logs/`
  - macOS: `~/Library/Application Support/PotatoClient/logs/`
  - Windows: `%LOCALAPPDATA%\PotatoClient\logs\`
- **File naming**: `potatoclient-{version}-{timestamp}.log`
- **Minimal overhead**: Only warnings and errors are logged

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

### Subprocess Logging
- **Development Mode**: Each subprocess creates individual log files in `./logs/{subprocess-name}-{timestamp}.log`
- **Release Mode**: No file logging for subprocesses
- **Log Rotation**: Keeps last 10 log files per subprocess type
- **Integration**: All subprocess logs are also sent to main process via Transit

### Kotlin/Java Integration
Kotlin subprocesses integrate with the main Clojure logging system via Transit IPC. Messages types include:
- `log` - Log messages with level, message, and process info
- `error` - Exceptions with stack traces
- `metric` - Performance metrics
- `status` - Process lifecycle status
- `event` - Application events (navigation, window, frame)
- `response` - Responses to commands

For details on Kotlin logging integration, see [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md#logging-integration).

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

## Swing EDT and Seesaw Invoke Strategy

### Event Dispatch Thread (EDT) Requirements

All Swing UI operations must run on the Event Dispatch Thread (EDT). PotatoClient uses Seesaw's invoke helpers to ensure proper thread safety:

- **`seesaw/invoke-now`**: Blocks until the operation completes on EDT
- **`seesaw/invoke-later`**: Schedules the operation on EDT and returns immediately

### Common Pitfalls and Solutions

**Avoid Double-Nesting invoke-later**:
```clojure
;; BAD - causes race conditions and hangs
(seesaw/invoke-later
  (fn []
    (seesaw/invoke-later  ;; Double nesting!
      (create-ui))))

;; GOOD - single EDT invocation
(seesaw/invoke-later
  (fn []
    (dispose-old-frame)
    (create-new-frame)))  ;; All in one EDT cycle
```

**Frame Recreation Pattern**:
When recreating frames (e.g., theme switching), perform all operations in a single EDT cycle:
```clojure
(defn reload-frame [old-frame create-fn]
  (seesaw/invoke-later
    (let [state (preserve-window-state old-frame)]
      (.dispose old-frame)
      (let [new-frame (create-fn state)]
        (seesaw/show! new-frame)))))
```

**Key Principles**:
1. UI creation/modification must happen on EDT
2. Use `invoke-later` for non-blocking operations (preferred)
3. Use `invoke-now` only when you need the result immediately
4. Avoid nested `invoke-later` calls - they create separate EDT cycles
5. Group related UI operations in a single EDT invocation

## Idiomatic Seesaw Patterns

### Core Principles

Seesaw provides a Clojure-friendly wrapper over Swing. Follow these patterns for maintainable UI code:

### 1. Use Actions Instead of Separate Buttons and Handlers

**Bad**:
```clojure
(let [button (seesaw/button :text "Click Me" :icon my-icon)
      handler (fn [e] (do-something))]
  (seesaw/listen button :action handler))
```

**Good**:
```clojure
(seesaw/action 
  :name "Click Me"
  :icon my-icon
  :handler (fn [e] (do-something)))
```

### 2. Layout with Keyword Parameters

**Bad**:
```clojure
(let [panel (JPanel. (BorderLayout.))]
  (.add panel component BorderLayout/CENTER)
  (.setHgap (.getLayout panel) 10))
```

**Good**:
```clojure
(seesaw/border-panel
  :center component
  :hgap 10
  :vgap 10
  :border 10)
```

### 3. Event Handling with listen

**Bad**:
```clojure
(.addActionListener button listener)
(.addMouseListener component mouse-listener)
```

**Good**:
```clojure
(seesaw/listen component
  :action (fn [e] ...)
  :mouse-clicked (fn [e] ...)
  :selection (fn [e] ...))
```

### 4. Component Selection and IDs

Always add `:id` to components for easy selection:

```clojure
(seesaw/text :id :username-field :columns 20)
;; Later...
(seesaw/select frame [:#username-field])
```

### 5. Dynamic Updates with config!

**Bad**:
```clojure
(.setText label "New Text")
(.setEnabled button false)
```

**Good**:
```clojure
(seesaw/config! label :text "New Text")
(seesaw/config! button :enabled? false)
```

### 6. Proper Resource Management

**Bad**:
```clojure
(.dispose frame)
(.show frame)
```

**Good**:
```clojure
(seesaw/dispose! frame)
(seesaw/show! frame)
(seesaw/pack! frame)  ; Size to preferred dimensions
```

### 7. Table Management

Use Seesaw's table abstractions:

```clojure
;; Create table with model
(seesaw/table 
  :id :data-table
  :model (table/table-model 
          :columns [{:key :name :text "Name"}
                    {:key :age :text "Age"}]))

;; Update data
(table/clear! table)
(table/insert-at! table 0 {:name "John" :age 30})
```

### 8. Common Layout Patterns

```clojure
;; Main window structure
(seesaw/frame
  :title "My App"
  :content (seesaw/border-panel
            :north toolbar
            :center (seesaw/scrollable main-content)
            :south status-bar
            :border 5))

;; Form layout
(seesaw/mig-panel
  :constraints ["wrap 2"]
  :items [["Username:"] [username-field "growx"]
          ["Password:"] [password-field "growx"]])

;; Button panel
(seesaw/flow-panel
  :align :center
  :hgap 5
  :items [ok-action cancel-action])
```

### 9. Threading Considerations

- **From menu/button handlers**: Already on EDT, use `invoke-now` if needed
- **From background threads**: Use `invoke-later` or `invoke-now`
- **For immediate UI updates**: Prefer `invoke-now` to avoid visual delays

```clojure
(defn show-dialog []
  (seesaw/invoke-now
    (let [dialog (create-dialog)]
      (seesaw/pack! dialog)
      (seesaw/show! dialog))))
```

### 10. Essential Seesaw Utilities

#### Dialog Functions

```clojure
;; Simple alert
(seesaw/alert "Hello World!")
(seesaw/alert parent "Error occurred!" :type :error)

;; Input dialog
(seesaw/input "Enter your name:")
(seesaw/input "Choose option:" 
              :choices ["Option 1" "Option 2"]
              :value "Option 1")

;; Custom dialog with return value
(seesaw/custom-dialog
  :parent frame
  :title "Settings"
  :modal? true
  :content my-panel
  :on-close :dispose)
```

#### Value and Selection

```clojure
;; Get/set values uniformly
(seesaw/value text-field)           ; get text
(seesaw/value checkbox)             ; get boolean
(seesaw/value! slider 50)           ; set value

;; Get all values from a container
(seesaw/value panel)                ; {:field-id "text", :check-id true}

;; Selection handling
(seesaw/selection list-box)         ; get selected items
(seesaw/selection! table [0 2 4])   ; select rows
```

#### Widget Selection

```clojure
;; CSS-like selectors
(seesaw/select frame [:#my-button])      ; by id
(seesaw/select frame [:.my-class])       ; by class
(seesaw/select frame [:JButton])         ; by type
(seesaw/select panel [:#panel :> :*])    ; children

;; Group widgets by ID
(let [{:keys [name email phone]} (seesaw/group-by-id form)]
  (println (seesaw/value name)))
```

#### Scrolling

```clojure
;; Make scrollable
(seesaw/scrollable text-area)
(seesaw/scrollable table 
                   :hscroll :as-needed
                   :vscroll :always)

;; Programmatic scrolling
(seesaw/scroll! widget :to :top)
(seesaw/scroll! widget :to [:point 0 100])
```

#### Common Widget Patterns

```clojure
;; Text components
(seesaw/text :id :input
             :columns 20
             :editable? true)

(seesaw/text :id :output
             :multi-line? true
             :wrap-lines? true
             :editable? false)

;; Lists and tables
(seesaw/listbox :id :items
                :model ["A" "B" "C"]
                :selection-mode :multiple)

(seesaw/table :id :data
              :model [:columns [:name :age]
                      :rows [{:name "John" :age 30}]])

;; Forms with MigLayout
(seesaw/mig-panel
  :constraints ["wrap 2", "[right]rel[grow,fill]"]
  :items [["Name:"] [(seesaw/text :id :name)]
          ["Email:"] [(seesaw/text :id :email)]
          [""] [(seesaw/flow-panel 
                 :items [(seesaw/button :text "OK")
                         (seesaw/button :text "Cancel")])
                "span 2, align right"]])
```

#### Utility Functions

```clojure
;; Convert between Seesaw widgets and Swing components
(seesaw/to-widget component)     ; Swing -> Seesaw
(seesaw/to-root widget)          ; Find containing frame/dialog

;; Sizing
(seesaw/width widget)            ; Get width
(seesaw/height widget)           ; Get height
```

## Advanced Patterns from ArcherBC2 Example

### Data Binding with seesaw.bind

The ArcherBC2 example demonstrates sophisticated data binding patterns:

```clojure
;; Basic atom binding
(require '[seesaw.bind :as sb])

(def *state (atom {:name "John" :age 30}))

;; Bind atom to widget
(sb/bind *state
         (sb/transform #(get % :name))
         (sb/value text-field))

;; Two-way binding
(sb/bind (sb/funnel 
          [(sb/selection list-box) 
           (sb/value text-field)]
          vector)
         (sb/transform process-inputs)
         (sb/value output-label))

;; Debounced binding (prevent rapid updates)
;; Use the utility from potatoclient.ui.utils
(require '[potatoclient.ui.utils :as ui-utils])

(sb/bind *state
         (sb/some (ui-utils/mk-debounced-transform #(get % :value)))
         (sb/value slider))

;; Complex binding with tee (multiple destinations)
(sb/bind *state
         (sb/tee
          (sb/bind (sb/transform #(if (:valid %) :green :red))
                   (sb/property label :foreground))
          (sb/bind (sb/transform #(:message %))
                   (sb/value label))))
```

### Custom Formatters for JFormattedTextField

```clojure
;; Number formatter with validation
(defn mk-number-formatter [fallback-val fraction-digits]
  (proxy [javax.swing.text.DefaultFormatter] []
    (stringToValue [s]
      (or (parse-double s) fallback-val))
    (valueToString [value]
      (format (str "%." fraction-digits "f") 
              (or value fallback-val)))))

;; Create formatted text field
(defn input-number [*state path spec & opts]
  (let [fmt (mk-number-formatter 0.0 2)
        field (seesaw/formatted-text-field 
               :formatter fmt
               :columns 10)]
    ;; Bind to state
    (sb/bind *state
             (sb/transform #(get-in % path))
             (sb/value field))
    field))
```

### Drag and Drop Support

```clojure
(require '[seesaw.dnd :as dnd])

;; File drop handler
(defn make-file-drop-handler [handle-fn]
  (dnd/default-transfer-handler
   :import [dnd/file-list-flavor
            (fn [{:keys [data drop?]}]
              (when drop?
                (let [file (first data)]
                  (handle-fn (.getAbsolutePath file)))))]
   :export {:actions (constantly :none)}))

;; Apply to component
(seesaw/config! panel :transfer-handler (make-file-drop-handler process-file))

;; List reordering with DnD
(defn make-list-dnd-handler [*state list-box]
  (dnd/default-transfer-handler
   :import [dnd/string-flavor
            (fn [{:keys [data drop? drop-location]}]
              (when (and drop? (:insert? drop-location))
                (let [src-idx (:index data)
                      dst-idx (:index drop-location)]
                  (swap! *state move-item src-idx dst-idx))))]
   :export {:actions (constantly :copy)
            :start (fn [_] [dnd/string-flavor 
                           {:index (.getSelectedIndex list-box)}])}))
```

### Frame Lifecycle Management

```clojure
;; Preserve and restore window state
(defn preserve-window-state [frame]
  {:bounds (.getBounds frame)
   :extended-state (.getExtendedState frame)
   :divider-locations (map #(.getDividerLocation %)
                          (seesaw/select frame [:JSplitPane]))})

(defn reload-frame! [old-frame frame-constructor]
  (seesaw/invoke-later
    (let [state (preserve-window-state old-frame)]
      (seesaw/config! old-frame :on-close :nothing)
      (.dispose old-frame)
      (-> (frame-constructor state)
          (restore-window-state! state)
          seesaw/pack!
          seesaw/show!))))
```

### Tree Widget Patterns

```clojure
;; Custom tree model
(defn make-tree-model [root-data]
  (seesaw/simple-tree-model
    :children (fn [node] (:children node))
    :root root-data))

;; Tree with custom rendering
(seesaw/tree 
  :id :nav-tree
  :model tree-model
  :renderer (seesaw/default-tree-renderer
              (fn [value]
                {:text (:name value)
                 :icon (get-icon-for (:type value))}))
  :listen [:selection (fn [e]
                       (handle-selection 
                         (seesaw/selection e)))])
```

### Status Bar Pattern

```clojure
;; Reactive status bar
(defn make-status-bar [*status]
  (let [icon (seesaw/label :id :status-icon)
        text (seesaw/label :id :status-text)]
    (sb/bind *status
             (sb/tee
              (sb/bind (sb/transform #(if (:ok %) ok-icon err-icon))
                       (sb/property icon :icon))
              (sb/bind (sb/transform :message)
                       (sb/value text))))
    (seesaw/horizontal-panel
      :items [icon text :fill-h])))
```

### Menu and Action Management

```clojure
;; Centralized action creation with keyboard shortcuts
(defn make-actions [*state frame]
  {:save (seesaw/action
          :name "Save"
          :icon (get-icon :file-save)
          :key "control S"
          :handler (fn [_] (save-file *state)))
   
   :open (seesaw/action
          :name "Open..."
          :icon (get-icon :file-open)
          :key "control O"
          :handler (fn [_] (open-file-dialog frame)))})

;; Apply global key mappings
(defn setup-global-keys [frame actions]
  (doseq [[k action] actions]
    (when-let [key (:key (meta action))]
      (seesaw/map-key frame key 
                   (:handler (meta action))
                   :scope :global))))
```

### Forms and Validation

```clojure
;; Form with real-time validation
(defn validated-form [*state validators]
  (let [fields (atom {})
        validate! (fn [field-id value]
                   (let [validator (get validators field-id)
                         valid? (validator value)]
                     (swap! fields assoc-in [field-id :valid?] valid?)
                     valid?))]
    
    (seesaw/mig-panel
      :items (for [[id label validator] validators]
               [[label "right"]
                [(doto (seesaw/text :id id)
                   (seesaw/listen 
                     :document
                     (fn [_] 
                       (let [value (seesaw/value (seesaw/select root [id]))]
                         (validate! id value)))))]
                "growx, wrap"]))))
```

### Widget Factories

```clojure
;; Consistent widget creation
(defn input-field
  [*state path spec & {:keys [columns tip units]}]
  (let [field (seesaw/formatted-text-field
                :columns (or columns 10)
                :tip tip)]
    ;; Add validation
    (seesaw/listen field :focus-lost
                (fn [e]
                  (when-not (s/valid? spec (seesaw/value field))
                    (seesaw/alert "Invalid input!"))))
    ;; Add binding
    (sb/bind *state
             (sb/transform #(get-in % path))
             (sb/value field))
    ;; Add units label if specified
    (if units
      (seesaw/horizontal-panel 
        :items [field (seesaw/label (str " " units))])
      field)))
```

## Performance Considerations

```clojure
;; Lazy rendering for large lists
(seesaw/table :model model
           :renderer (proxy [DefaultTableCellRenderer] []
                      (getTableCellRendererComponent [table value selected focus row col]
                        ;; Only render visible cells
                        (when (.isShowing table)
                          (proxy-super getTableCellRendererComponent 
                                      table value selected focus row col)))))

;; For debouncing, throttling, and batch updates, see the UI Utilities section above
```

## Common Pitfalls to Avoid

1. **Forgetting EDT requirements** - Always use invoke-now/invoke-later for UI operations
2. **Memory leaks with listeners** - Remove listeners when components are disposed
3. **Blocking EDT** - Move long operations to background threads
4. **Inefficient layouts** - Prefer MigLayout for complex forms
5. **Not disposing resources** - Always dispose frames, dialogs, and timers
6. **Maximize animation on close** - When closing maximized frames, use this hack to avoid the minimize animation:
   ```clojure
   ;; HACK: Always minimize to taskbar first, then dispose after delay
   ;; to avoid the jarring unmaximize animation
   (>defn- dispose-frame!
           [frame]
           [[:fn {:error/message "must be a JFrame"}
             #(instance? javax.swing.JFrame %)] => nil?]
           (let [frame-title (.getTitle frame)]
             ;; Always minimize to taskbar first (regardless of current state)
             (.setExtendedState frame Frame/ICONIFIED)
             ;; Use a Swing Timer to delay disposal - invoke-later doesn't work after iconification!
             (let [timer (javax.swing.Timer. 
                          100  ; 100ms delay
                          (reify java.awt.event.ActionListener
                            (actionPerformed [_ _]
                              ;; Ensure disposal happens on EDT
                              (seesaw/invoke-now
                                (seesaw/dispose! frame)))))]
               (.setRepeats timer false)
               (.start timer)))
           nil)
   
   ;; IMPORTANT: Set :on-close :nothing and handle X button yourself
   (seesaw/frame :on-close :nothing ...)
   (seesaw/listen frame :window-closing
                  (fn [_] (dispose-frame! frame)))
   ```
   Note: This is a workaround for a Swing quirk where maximized frames animate to normal state before disposal. The frame briefly appears in the taskbar before disappearing. We use a Timer instead of invoke-later because invoke-later callbacks don't execute after frame iconification.

## Guardrails and Malli Integration

PotatoClient uses Guardrails with Malli for comprehensive runtime validation:

### What is Guardrails?
Guardrails is a runtime validation library that:
- **Enforces contracts**: Validates function inputs and outputs in development
- **Zero overhead in production**: Completely removed from release builds
- **Better error messages**: Clear, actionable error reports
- **Works with Malli**: Uses Malli schemas for validation
- **clj-kondo support**: Static analysis understands `>defn` syntax

### Implementation Requirements

**Mandatory for all functions**:
- Use `>defn` for public functions
- Use `>defn-` for private functions
- Never use raw `defn` or `defn-`

**Schema Organization**:
- All data schemas in `potatoclient.specs` namespace
- Function instrumentation in `potatoclient.instrumentation` (excluded from AOT)
- Prefer precise schemas over broad types like `any?`

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

### Writing Precise Specs

**ALWAYS write precise (narrow) specs rather than overly broad types.** This improves error messages, catches bugs early, and serves as documentation.

**For Command and State Functions**: When adding functions that handle commands or state derived from protobuf messages, see [.claude/protobuf-command-system.md](.claude/protobuf-command-system.md#command-validation-and-specs).

**Guidelines for precise specs**:

1. **Avoid `any?` except when necessary**:
   - ❌ Bad: `[:=> [:cat any?] any?]`
   - ✅ Good: `[:=> [:cat ::specs/theme-key] nil?]`
   - Legitimate uses: Java interop objects, higher-order function returns

2. **Use domain-specific types**:
   - ❌ Bad: `keyword?` for any keyword
   - ✅ Good: `::specs/stream-type` for `:heat` or `:day`
   - ❌ Bad: `string?` for any string
   - ✅ Good: `::specs/url` for WebSocket URLs

3. **Specify return types precisely**:
   - Side-effect functions should return `nil?`
   - Pure functions should specify exact return type
   - Functions returning Java objects should document the class

4. **Use union types for multiple valid inputs**:
   ```clojure
   ;; Instead of any?
   [:or ::specs/theme-key ::specs/locale ::specs/domain]
   ```

5. **Validate complex structures**:
   ```clojure
   ;; For core.async channels
   [:fn {:error/message "must be a core.async channel"}
    #(instance? clojure.core.async.impl.channels.ManyToManyChannel %)]
   ```

6. **Document when `any?` is appropriate**:
   ```clojure
   ;; Protobuf objects have dynamic types
   (m/=> proto/encode [:=> [:cat any?] bytes?])
   
   ;; Higher-order functions with caller-defined returns
   (m/=> map [:=> [:cat ifn? seqable?] any?])
   ```

### Checking for Unspecced Functions

To ensure all functions have Malli specs:

Use `make report-unspecced` to generate a report of functions without specs. The report will be saved to `./reports/unspecced-functions.md`.

This report will:
- List all functions that lack Malli instrumentation
- Group them by namespace
- Provide statistics on coverage
- Only include actual functions (not schema definitions)

#### Guardrails Check Tool

The `report-unspecced` command uses the **Guardrails Check Tool** located in `tools/guardrails-check/`. This is a standalone Babashka-based tool that can find functions using raw `defn`/`defn-` instead of Guardrails' `>defn`/`>defn-`.

**Features**:
- Fast analysis using Babashka (no JVM startup overhead)
- Multiple output formats (EDN, Markdown)
- Pattern-based function search
- Statistics and namespace listing
- Can be used independently of the main project

**Standalone Usage**:
```bash
cd tools/guardrails-check

# Check for unspecced functions (EDN output)
bb check ../../src/potatoclient

# Generate markdown report
bb report ../../src/potatoclient

# Find specific functions by pattern
bb find process ../../src/potatoclient

# Show statistics
bb stats ../../src/potatoclient
```

The tool uses regex-based parsing to identify function definitions and generates reports grouped by namespace. It's particularly useful for maintaining consistent Guardrails usage across the entire codebase.

### Guardrails Usage

**Development Mode** (`make dev`, `make nrepl`):
- Guardrails is automatically enabled via JVM option `-Dguardrails.enabled=true`
- All function calls are validated at runtime
- Detailed error messages for contract violations

**Production Mode** (`make release`):
- All Guardrails code is completely removed from bytecode
- Zero runtime overhead
- Window title shows `[RELEASE]` to indicate production build

## Best Practices

### Development Workflow

1. **Fix Guardrails errors immediately** - They indicate real bugs in your code
2. **Always use `>defn` or `>defn-`** - Never use raw `defn`
3. **Write precise specs** - Use domain-specific types from `potatoclient.specs`
4. **Check reflection warnings** - They indicate performance issues
5. **Run `make lint-report-filtered`** - Review real issues without false positives
6. **Run `make report-unspecced`** - Ensure all functions have Guardrails

### Function Development

1. **Use `>defn` or `>defn-` for all functions** - Never use raw `defn`
2. **Return `nil` for side effects** - Not `true` or other values
3. **Add specs immediately** - Don't defer this
4. **Run `make report-unspecced`** - Regularly check for missing specs

### Before Release

Only use these commands when preparing for production:

1. **Test with `make run`** - Tests JAR in production-like mode (rarely needed)
2. **Build with `make release`** - Creates optimized production JAR
3. **Test release JAR** - Ensure it runs without development dependencies

### Build Type Detection
The application detects build type via `potatoclient.runtime/release-build?` which checks:
1. System property: `potatoclient.release`
2. Environment variable: `POTATOCLIENT_RELEASE`
3. Embedded `RELEASE` marker file (in release JARs)

## Proto Explorer and Malli Specs

PotatoClient uses the **Proto Explorer** tool to generate comprehensive Malli specifications from Protocol Buffer definitions. This provides compile-time validation, constraint-aware test data generation, and eliminates runtime protobuf reflection.

### Architecture

Proto Explorer uses a JSON-based architecture:
1. **Protogen** generates protobuf classes and JSON descriptors using Buf CLI
2. **Proto Explorer** converts JSON descriptors → EDN → Malli specs with constraints
3. **Specs** are exported to `shared/specs/protobuf/` for use in the main application

### Key Features

- **Complete buf.validate Support**: All constraints are extracted and applied to specs
- **Constraint-Aware Generation**: Test data respects all validation rules
- **Idiomatic Clojure**: Automatic kebab-case conversion for all names
- **No Runtime Reflection**: Specs are pre-generated at build time
- **Property-Based Testing**: Generators produce valid data for property tests

### Using Generated Specs

```clojure
;; Require the generated specs
(require '[potatoclient.specs.cmd :as cmd])
(require '[potatoclient.specs.cmd.RotaryPlatform :as rotary])
(require '[malli.core :as m])
(require '[malli.generator :as mg])

;; Validate commands
(m/validate cmd/Root {:protocol-version 1 :cmd {:ping {}}})
;; => true

;; Get validation errors
(m/explain rotary/set-azimuth-value {:value 400})
;; => {:errors [{:path [:value], :in [:value], :schema [:< 360], :value 400}]}

;; Generate constraint-aware test data
(mg/generate rotary/set-azimuth-value)
;; => {:value 234.5 :direction 1}  ; value always 0-360, direction never 0
```

### Constraint Examples

Specs include all buf.validate constraints:

```clojure
;; SetAzimuthValue: value must be [0, 360), direction cannot be 0
potatoclient.specs.cmd.RotaryPlatform/set-azimuth-value
;; => [:map 
;;     [:value [:and [:maybe :double] [:>= 0] [:< 360]]]
;;     [:direction [:and [:maybe :potatoclient.specs.ser/jon-gui-data-rotary-direction]
;;                  [:not [:enum [0]]]]]]

;; SetElevationValue: value must be [-90, 90]
potatoclient.specs.cmd.RotaryPlatform/set-elevation-value
;; => [:map [:value [:and [:maybe :double] [:>= -90] [:<= 90]]]]
```

### Proto Explorer CLI

Proto Explorer provides a Babashka CLI for querying specs:

```bash
cd tools/proto-explorer

# Find specs by pattern
bb find rotary

# Get spec definition
bb spec :cmd.RotaryPlatform/set-velocity

# Generate example data
bb example :cmd.RotaryPlatform/set-azimuth-value
# => {:spec :cmd.RotaryPlatform/set-azimuth-value
#     :example {:value 245.7 :direction 1}}

# Generate multiple examples
bb examples :cmd.RotaryPlatform/set-elevation-value 5
```

### Regenerating Specs

After protobuf changes:

```bash
cd tools/proto-explorer
make proto          # Generate proto files and JSON descriptors
make generate-specs # Convert to Malli specs
```

### Benefits

1. **Type Safety**: All protobuf messages have corresponding Malli specs
2. **Validation**: Commands are validated before sending to Kotlin subprocesses
3. **Test Data**: Generated examples always respect constraints
4. **Documentation**: Specs serve as living documentation
5. **Performance**: No runtime reflection or protobuf class loading

For detailed documentation, see:
- [Proto Explorer README](tools/proto-explorer/README.md)
- [Generated Specs README](shared/specs/protobuf/README.md)

## Technical Details

**Build**: Java 17+, Kotlin 2.2.0, Protobuf 4.29.5 (bundled)
**Streams**: Heat (900x720), Day (1920x1080)
**Protobuf**: Direct implementation with custom kebab-case conversion (no external wrapper libraries)
**Kotlin Architecture**: See [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md) for subprocess implementation details

**Performance Optimizations**:
- Hardware-accelerated video decoding
- Zero-allocation streaming architecture
- Lock-free data structures
- For detailed performance optimizations and hardware decoder information, see [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md#performance-optimizations)

**Type Hints**: Added to prevent reflection in:
- JFrame operations
- Process/IO operations
- File operations
- Date formatting

## CI/CD & Release Process

The CI pipeline automatically builds optimized release versions with:
- **Platform packages**: Linux (AppImage), Windows (.exe/.zip), macOS (DMG)
- **Build steps**: Proto generation → Kotlin compilation → Java compilation → Clojure AOT
- **Release optimizations**: AOT compilation, direct linking, no dev overhead
- **Kotlin compilation**: Downloads Kotlin 2.2.0 compiler during build (see [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md#build-integration))
- **Self-identification**: Release builds show `[RELEASE]` in window title

### Build System Details

**For protobuf-specific build system details**, including:
- Dynamic classpath configuration for protobuf classes
- Important build sequence
- Common build issues and solutions
- Troubleshooting protobuf-related errors

See [.claude/protobuf-command-system.md](.claude/protobuf-command-system.md#build-system-integration)

### Protocol Buffer Generation

PotatoClient uses the [protogen](https://github.com/JAremko/protogen) Docker-based tool for generating Java protobuf classes, following the same approach as jettison:

**Key Features**:
- Proto definitions are maintained in the protogen repository (not local)
- Always generates from the latest proto definitions
- Supports both standard and validated Java bindings
- No local proto files to maintain or sync
- Fully isolated from jettison - builds its own Docker images

**Generation Process** (`make proto`):
1. Clones the latest protogen repository to a temporary directory
2. Attempts to import pre-built base image from Git LFS (60s timeout)
3. If LFS unavailable, builds base image from scratch (takes longer but works)
4. Builds protogen Docker image with bundled proto definitions
5. **Cleans existing proto directories** to prevent stale binding conflicts
6. Generates Java classes with custom package structure:
   - Command messages: `cmd` package (e.g., `cmd.JonSharedCmd$Root`)
   - Data types: `ser` package (e.g., `ser.JonSharedDataTypes`)
7. Applies compatibility fixes for Java/Clojure integration
8. **Cleans up the main Docker image** (keeps base image for faster rebuilds)
9. Removes temporary directory

**Requirements**:
- Docker (user must be in docker group - script provides setup instructions)
- Internet connection (to clone protogen)
- Git LFS (optional - speeds up builds by using pre-built base image)

**Note**: The validated Java bindings require the `protovalidate-java` library if you want to use them. Standard bindings work without additional dependencies.


# important-instruction-reminders
Do what has been asked; nothing more, nothing less.
NEVER create files unless they're absolutely necessary for achieving your goal.
ALWAYS prefer editing an existing file to creating a new one.
NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.

      
      IMPORTANT: this context may or may not be relevant to your tasks. You should not respond to this context unless it is highly relevant to your task.