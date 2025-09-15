# Claude AI Assistant Context

## CRITICAL POLICIES & CONVENTIONS

### Code Organization Conventions

#### Arrow Spec Placement
**ALWAYS place arrow specs (m/=>) DIRECTLY AFTER each function definition**

```clojure
;; CORRECT - Arrow spec immediately follows function
(defn process-command
  "Process a command and return result"
  [cmd]
  ...)
(m/=> process-command [:=> [:cat :cmd/root] [:map [:status :keyword]]])

;; WRONG - Arrow specs grouped at end of file
(defn process-command ...)
(defn another-function ...)
;; ... later in file ...
(m/=> process-command ...)
(m/=> another-function ...)
```

### Required Tool Usage After Code Changes

**ALWAYS use these specialized agents proactively:**

#### 1. Docstring Checker Agent
**Run after ANY `def`, `defn`, `defn-`, or `defonce` additions/modifications**
- ALL definitions MUST have docstrings - no exceptions
- Empty strings (`""`) are invalid - provide meaningful documentation

#### 2. Spec Checker Agent  
**Run after adding or modifying functions**
- Essential for `malli.dev/start!` instrumentation
- ALL functions must have tight, precise specs - no `:any`
- Reuse shared specs from `specs/common.clj`

#### 3. Test Runner Analyzer Agent
**MANDATORY: ALWAYS use this agent for running tests - NEVER run `make test` directly**
- **CRITICAL POLICY**: ALWAYS use the test-runner-analyzer agent, NEVER run `make test` directly via Bash
- **WHY**: The agent provides comprehensive failure analysis and detailed reports
- **NEVER disable, skip, or comment out failing tests**
- FIX the code to make tests pass
- All tests MUST pass before any commit

#### 4. I18n Checker Agent
**Run after adding UI text**

### Testing Philosophy

**CRITICAL: Fix failing code, never modify tests to pass**

- ⚠️ **NEVER disable, skip, or comment out failing tests**
- ✅ **FIX the implementation to make tests pass**
- ✅ **Remove tests only when corresponding functionality is removed**
- ❌ **Do NOT modify test assertions to make them pass**

### Quality Standards

1. **100% documentation coverage** - all definitions must have docstrings
2. **100% spec coverage** - all functions must have arrow specs
3. **Closed map specs** with `{:closed true}`
4. **Generative testing** with schemas
5. **Never delete tests** - make them pass

### Malli Schema System

**ALL functions MUST have arrow specs (m/=>) for instrumentation**

```clojure
;; PREFERRED - Arrow spec (auto-discovered by malli.dev/start!)
(defn process-command
  "Process a command and return result"
  [cmd]
  ...)
(m/=> process-command [:=> [:cat :cmd/root] [:map [:status :keyword]]])

;; LEGACY - Metadata schema (NOT auto-discovered, migrate to arrow specs)
(defn process-command
  "Process a command and return result"
  {:malli/schema [:=> [:cat :cmd/root] [:map [:status :keyword]]]}
  [cmd]
  ...)
```

**Spec Requirements:**
- Use **tight, precise specs** - avoid `:any`, `:map`, or overly permissive schemas
- **Reuse shared specs** from `specs/common.clj` - don't reinvent domain concepts
- **Extend existing specs** when adding constraints - use `[:and base-spec constraint]`
- **Register reusable specs** in the global registry for consistency

## Project Structure

### Directory Layout
```
potatoclient/
├── src/
│   ├── java/              # Java sources (protobuf, IPC)
│   │   ├── cmd/           # Generated protobuf command classes
│   │   ├── ser/           # Generated protobuf serialization classes
│   │   └── potatoclient/java/ipc/  # IPC infrastructure
│   └── potatoclient/
│       ├── cmd/           # Command building and validation
│       ├── ipc/           # Inter-process communication
│       ├── kotlin/        # Kotlin integration
│       ├── malli/         # Schema registry and custom specs
│       ├── proto/         # Protobuf serialization/deserialization
│       ├── specs/         # Malli specifications
│       │   ├── cmd/       # Command specifications
│       │   └── state/     # State specifications
│       ├── state/         # Application state management
│       │   └── server/    # WebSocket state ingress
│       ├── streams/       # Video streaming
│       └── ui/            # User interface components
│           └── status_bar/  # Status bar with validation
├── test/                  # Test files (mirror src structure)
├── resources/            # Application resources
├── tools/                # Development tools
│   └── i18n-checker/     # Translation completeness checker
└── target/               # Build artifacts
```

### Key Modules

**Core Infrastructure:**
- `init/` - Centralized initialization for registry and core systems
- `malli/` - Schema registry with custom :oneof spec support
- `ipc/` - Unix Domain Socket IPC with Transit serialization
- `ipc/handlers` - Reusable message handler patterns (Protocol-based)
- `proto/` - Protobuf EDN ↔ binary serialization

**Command System:**
- `cmd/` - Command builders with automatic validation
- `specs/cmd/` - Malli specifications matching proto constraints

**State Management:**
- `state/` - Central app state atom
- `state/server/` - WebSocket ingress with real-time updates
- `specs/state/` - State validation schemas

**User Interface:**
- `ui/` - Seesaw-based UI components
- `ui/status_bar/` - User feedback with automatic validation reporting
- `ui/bind_group/` - Advanced binding management
- `ui/debounce/` - Performance optimization for rapid updates

## Development Tools

### Development Setup

The project has a simplified development setup using `malli.dev/start!` for instrumentation:

**Development Files (in dev/ directory):**
- `init_dev.clj` - Shared initialization logic for both `make dev` and `make nrepl`
- `dev.clj` - Entry point for `make dev` (UI with instrumentation)  
- `user.clj` - NREPL user namespace with development utilities and helper functions

**Features:**
- **Malli instrumentation** with `malli.dev/start!` 
- **CLJ-Kondo config generation** automatic for static type checking
- **Auto-reinstrumentation** when schemas change via registry watcher
- **Pretty error reporting** with configurable formatting

### NREPL Development (`make nrepl`)
Starts NREPL server with development environment initialized:
- **Automatic initialization** using `malli.dev/start!`
- Collects and instruments function schemas from loaded namespaces
- NREPL server on port 7888 with CIDER support
- Can run in parallel with `make dev`

Key REPL functions (available immediately):
- `(help)` - Show all available commands and current status
- `(reinstrument!)` - Re-collect schemas and trigger re-instrumentation
- `(check-functions!)` - Validate all function schemas with generative testing
- `(set-throw-mode!)` - Throw exceptions on validation errors
- `(set-print-mode!)` - Print validation errors (default)
- `(instrumented-count)` - Count instrumented functions
- `(restart-logging!)` - Restart the logging system

### Development UI (`make dev`)
Uses `dev/dev.clj` to run the app with full instrumentation:
- Shows initialization output in terminal
- Collects schemas from loaded namespaces via `malli.instrument/collect!`
- Instruments functions with Malli schemas using `malli.dev/start!`
- Starts the UI with validation enabled
- Prints validation errors to console in real-time
- Auto-reinstruments on schema changes via registry watcher
- Can run in parallel with `make nrepl`

### Make Targets
```bash
# Development
make dev          # Run app with Malli instrumentation and UI
make nrepl        # Start NREPL server on port 7888 for interactive dev
make clean        # Clean build artifacts
make recompile    # Force recompile all Java and Kotlin sources

# Testing
make test                # Run all tests
make test-cmd           # Run command tests only
make test-malli         # Run Malli spec tests
make test-serialization # Run serialization tests
make test-ipc          # Run IPC tests
make test-oneof        # Run oneof spec tests

# Code Quality
make fmt            # Format Clojure and Kotlin code
make lint           # Run linters with filtered report
make lint-raw       # Run linters unfiltered

# Dependencies
make deps-outdated  # Check for outdated dependencies
make deps-upgrade   # Interactive dependency upgrade
```

### I18n Translation Checker
**Location**: `tools/i18n-checker/`

Ensures translation completeness across locales:
- Finds all `i18n/tr` calls using AST parsing
- Identifies missing/unused translation keys
- Generates stub entries for missing translations
- Runtime detection in dev mode shows `[MISSING: :key-name]`

**Usage:**
```bash
cd tools/i18n-checker
./check.sh              # Generate report
./check.sh --stubs uk   # Generate Ukrainian stubs
```

### CLJ-Kondo Integration

CLJ-Kondo configurations for static analysis are **automatically generated** by `malli.dev` when running in NREPL or dev mode.

**How it works:**
- When you start NREPL with `make nrepl`, instrumentation starts automatically
- As you define functions with `:malli/schema` metadata, configs are generated
- Configs are written to `.clj-kondo/configs/malli/`
- Your editor's CLJ-Kondo integration picks them up for instant type checking

**Benefits:**
- **Zero configuration** - just define schemas and they work
- **Always up-to-date** - configs regenerate as schemas change
- **IDE support** - instant static type checking in your editor
- **No manual steps** - everything happens automatically in dev mode

## Specialized Agent Details

### Additional Agent Information

#### Docstring Checker Details
- **Public functions** (`defn`) - MUST document purpose, parameters, and return values
- **Private functions** (`defn-`) - MUST document implementation details and usage
- **State definitions** (`def`, `defonce`) - MUST document purpose and structure
- Supports both string docstrings and `^{:doc "..."}` metadata

#### Spec Checker Details
- Arrow specs enable runtime validation, metadata schemas do not
- **Reuse and extend shared specs** from [`specs/common.clj`](src/potatoclient/specs/common.clj):
  - Domain specs: `:angle/*`, `:position/*`, `:screen/*`, `:time/*`, etc.
  - Proto specs: `:proto/*`, `:enum/*` matching protobuf constraints
  - Composite specs: `:composite/*` for common data structures
- **Register reusable specs** in the global registry via `registry/register-spec!`

#### I18n Checker Details
- Run after adding any new `i18n/tr` calls
- Run when adding new UI components with text
- Generate stubs for missing translations

## Core Technologies

### Registry Management
- Centralized initialization via `potatoclient.init/ensure-registry!`
- Global registry at `potatoclient.malli.registry`
- Common specs registered for reuse via `register-spec!`
- Custom `:oneof` schema for protobuf oneOf fields
- All map specs use `{:closed true}` for strict validation
- Automatic initialization in production (`main`), dev (`make dev`), REPL (`make nrepl`), and tests

### State Management

**Central State Atom:**
```clojure
;; potatoclient.state/app-state
{:ui {:status {...}         ; Status bar state
      :theme :sol-dark      ; Current theme (:sol-dark or :sol-light)
      :locale :english      ; Current locale (:english or :ukrainian)
      :tab-properties {...} ; Tab window states
      :fullscreen false}    ; Fullscreen mode
 :server-state {...}        ; Real-time state from server (or nil)
 :connection {...}          ; Connection state and metrics
 :processes {...}           ; Process states
 :stream-processes {...}    ; Stream process states
 :session {...}}            ; Session information
```

**State Ingress:**
- WebSocket connection to `wss://domain/ws/ws_state`
- Binary protobuf messages deserialized to EDN
- Automatic validation with `:state/root` schema
- Throttled updates (100ms) to prevent UI flooding

### Status Bar System

**CRITICAL: All user actions must be reflected in status bar**

The status bar provides immediate feedback for all operations:

```clojure
(require '[potatoclient.ui.status-bar.messages :as status-bar])

;; Basic updates
(status-bar/set-info! "Operation completed")
(status-bar/set-warning! "Connection unstable")
(status-bar/set-error! "Operation failed")
(status-bar/set-ready!)

;; With validation
(require '[potatoclient.ui.status-bar.validation :as v])
(v/validate :int user-input)  ; Auto-reports errors to status bar
```

**Performance Optimizations:**
- Icon caching prevents reload spam
- Debounced updates (100ms) batch rapid state changes
- Validation only reports failures, not successes

### UI Binding System

**Grouped Bindings** (`potatoclient.ui.bind-group`):
```clojure
(require '[potatoclient.ui.bind-group :as bg])

;; Create/clean binding groups
(bg/bind-group :panel-name source-atom target)
(bg/clean-group :panel-name source-atom)

;; Temporary bindings with auto-cleanup
(bg/with-binding-group [:temp my-atom]
  (bg/bind-group :temp my-atom target))
```

**Important Note:** Seesaw bindings don't immediately propagate initial values.
To ensure widgets display current atom values, trigger a change after binding:
```clojure
(bg/bind-group-property :ui-group my-atom label :text)
(reset! my-atom @my-atom)  ; Force initial propagation
```

**Debouncing** (`potatoclient.ui.debounce`):
```clojure
(require '[potatoclient.ui.debounce :as debounce])

;; Debounce rapid updates
(def state-debounced (debounce/debounce-atom state-atom 300))
```

### IPC Architecture

**Unix Domain Socket IPC:**
- Transport: Unix sockets with framed messages
- Serialization: Transit msgpack format
- Message framing: 4-byte length prefix
- Max message size: 10MB
- Zero-copy ByteBuffer operations

**Message Handler Patterns** (`potatoclient.ipc.handlers`):
- `IMessageHandler` protocol for consistent handling
- Composable handlers: filtering, transforming, logging
- Thread-safe queue processing with error recovery
- Reusable patterns for all IPC communication

**Usage:**
```clojure
(require '[potatoclient.ipc.core :as ipc]
         '[potatoclient.ipc.handlers :as handlers])

;; Simple server
(def server (ipc/create-server :heat
              :on-message handle-message))

;; With advanced handler
(def handler (handlers/create-handler
               {:name "my-handler"
                :handler-fn process-message
                :error-fn handle-error
                :running? running-atom}))

(ipc/send-message server {:type :event :data {...}})
```

**Transit Keywords:**
- Always use Transit Keywords, never strings
- Kotlin: `TransitFactory.keyword("name")`
- Clojure: Automatic conversion to keywords

### Gesture Events Architecture

**Complete Flow from Kotlin to Clojure:**

1. **Kotlin Gesture Recognition** (`MouseEventHandler.kt` → `GestureRecognizer.kt`):
   - Raw AWT/Swing mouse events captured from video component
   - State machine with IDLE → PENDING → PANNING states
   - Gesture thresholds:
     - Move: 20px (pan trigger)
     - Tap duration: 300ms max
     - Double tap: 300ms window, 10px tolerance
     - Pan throttle: 120ms between updates
     - Scroll debounce: 50ms accumulation
   - Each gesture includes frame timestamp for video sync

2. **IPC Message Transport** (`IpcClient.kt`):
   - Gesture events converted to Transit-encoded maps
   - Includes NDC (Normalized Device Coordinates) [-1, 1]
   - Message structure:
     ```clojure
     {:msg-type :event
      :type :gesture
      :gesture-type :tap/:double-tap/:pan-start/:pan-move/:pan-stop/:wheel-up/:wheel-down
      :stream-type :heat/:day
      :x 450 :y 300           ; Pixel coordinates
      :ndc-x 0.5 :ndc-y -0.2  ; NDC coordinates
      :delta-x 10 :delta-y 20 ; For pan-move
      :scroll-amount 3        ; For wheel events
      :frame-timestamp 12345  ; Video frame sync
      :timestamp 67890}       ; System timestamp
     ```
   - Unix Domain Socket with 4-byte framing
   - Transit msgpack serialization

3. **Clojure Event Reception** (`ipc/core.clj`):
   - Dedicated reader thread per stream (heat/day)
   - Messages queued in `LinkedBlockingQueue` (1000 capacity)
   - Processor thread with `IMessageHandler` pattern
   - Routes to `streams/events/handle-message`

4. **Event Processing** (`streams/events.clj`):
   - Message normalization (string → keyword keys)
   - Validation against Malli specs
   - Routes by `:msg-type` → `:event` → `:gesture`
   - Gesture handler logs with debug level:
     ```clojure
     {:id :stream/gesture-event
      :stream :heat
      :gesture :tap
      :coords {:x 450 :y 300}
      :ndc {:x 0.5 :y -0.2}
      :scroll 3}
     ```

5. **Logging Output** (`logging.clj`):
   - Development: `:trace` level (all events)
   - Production: `:warn` level (errors only)
   - Console: Formatted with timestamp
   - File: `logs/potatoclient-{version}-{timestamp}.log`

**Performance Optimizations:**
- Pan update throttling (120ms intervals)
- Scroll event debouncing (50ms accumulation)
- Async reader/processor threads
- Queue buffering (1000 messages)
- Zero-copy ByteBuffer IPC
- Dropping mode async logging

## Development Guidelines

### Code Quality Implementation Details

1. **Comprehensive Tests** - Organized into test suites with extensive coverage
2. **Property-Based Testing** - Extensive use of generative testing with Malli schemas
3. **Clear Documentation** - Meaningful docstrings explaining purpose, not just restating the name
4. **No Legacy Code** - Pre-alpha, make breaking changes when needed

**Testing Practices:**
- **ALWAYS use `test-runner-analyzer` agent** - NEVER run `make test` directly
- Use test suites for focused testing
- Property-based testing with `clojure.test.check`
- Generative testing with Malli schemas
- Test proto serialization roundtrips
- Validate all command constraints
- IPC handler property tests for message processing
- **ALL tests must pass before any commit**

### Performance Considerations

1. **Cache expensive operations** (icons, computed values)
2. **Debounce rapid updates** (UI, validations)
3. **Use throttling for high-frequency events** (state updates)
4. **Batch operations when possible**

### Future Error Handling

**CRITICAL: All futures must have proper error handling**

Futures in Clojure swallow exceptions by default, which can hide critical errors:

```clojure
;; BAD - Exceptions are silently swallowed
(future
  (process-data data))

;; GOOD - Exceptions are caught and logged
(future
  (try
    (process-data data)
    (catch Exception e
      (logging/log-error {:msg "Error processing data" :error e}))))
```

**Best Practices:**
1. **Always wrap future body in try-catch** - Log exceptions at minimum
2. **Consider using callbacks for error handling** - Pass error handler function
3. **Monitor future completion** - Use `realized?` or `deref` with timeout
4. **Avoid fire-and-forget patterns** - Track futures that need monitoring
5. **Use thread pools for better control** - Consider `java.util.concurrent` for complex cases

**Common Patterns:**
```clojure
;; With error callback
(future
  (try
    (let [result (process-data data)]
      (on-success result))
    (catch Exception e
      (on-error e))))

;; With logging and state update
(future
  (try
    (process-data data)
    (catch Exception e
      (logging/log-error {:msg "Processing failed" :error e})
      (swap! state assoc :error (.getMessage e))
      (update-ui-with-error!))))
```

### When Adding Features

1. **Define Malli specs first** - Data contracts before implementation
2. **Add docstrings to all definitions** - Use `docstring-checker` agent to verify
3. **Add arrow specs to all functions** - Use `spec-checker` agent to verify
4. **Add to registry if reusable** - Don't duplicate specs
5. **Update status bar** - User feedback for all actions
6. **Write tests immediately** - Use `test-runner-analyzer` agent to verify
7. **Check i18n completeness** - Use `i18n-checker` agent for new UI text

**Automated Verification Workflow:**
1. After adding/modifying definitions → Use `docstring-checker` agent
2. After adding/modifying functions → Use `spec-checker` agent  
3. After completing implementation → Use `test-runner-analyzer` agent (NEVER `make test` directly)
4. After adding UI text → Use `i18n-checker` agent

### Development Workflow

**Malli Instrumentation Workflow:**
1. Start with `make dev` or `make nrepl` - functions are instrumented via `malli.dev/start!`
2. Edit code and save files
3. Call `(reinstrument!)` to re-collect schemas from all namespaces
4. The registry watcher automatically re-instruments changed functions
5. Validation errors appear immediately in console or REPL

**Re-instrumentation:**
After modifying code with new or changed schemas:
```clojure
(require '[malli.instrument :as mi])
(mi/collect! {:ns (all-ns)})  ; Re-collect schemas
; The dev/start! watcher will auto-reinstrument
```

Or simply use the helper function:
```clojure
(reinstrument!)  ; Available in NREPL
```

**Key Features:**
- **Automatic re-instrumentation** - Registry watcher detects schema changes
- **Pretty error reporting** - Clear validation error messages
- **CLJ-Kondo integration** - Automatic type config generation
- **Generative testing** - Check functions with `(check-functions!)`

### Initialization Pattern

**Production** uses centralized initialization:
```clojure
(require '[potatoclient.init :as init])

;; For production/main
(init/initialize!)

;; For tests
(init/initialize-for-tests!)

;; To ensure registry only (lightweight)
(init/ensure-registry!)
```

**Development** uses shared initialization in `dev/init_dev.clj`:
```clojure
;; Called automatically by dev/user.clj (NREPL) and dev/dev.clj (make dev)
(init-dev/initialize!)
```

This provides:
- Automatic Malli instrumentation via `malli.dev/start!`
- CLJ-Kondo config generation
- Registry watcher for auto-reinstrumentation
- Pretty error reporting

## Project Principles

### Core Philosophy
- **No backward compatibility** - Pre-alpha stage
- **Clean architecture over legacy support**
- **Breaking changes when needed**
- **No deprecated code**

### User Experience
- **Immediate feedback** via status bar
- **Specific error messages** with context
- **Progress indicators** for long operations
- **Consistent UI state** with bindings
