# Claude AI Assistant Context

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

The project has a unified development setup with comprehensive tooling:

**Core Development Files:**
- `dev/init_dev.clj` - Shared initialization logic for both `make dev` and `make nrepl`
- `dev/dev.clj` - Entry point for `make dev` (UI with instrumentation)
- `dev/nrepl_server.clj` - NREPL server with pre-initialization (optional)
- `dev/user.clj` - NREPL user namespace with development utilities
- `dev/repl.clj` - REPL utility functions (reload, instrumentation control)

**Features:**
- **Automatic namespace discovery** using tools.namespace (finds 90+ namespaces)
- **Full project loading** in dependency order with error handling
- **Malli instrumentation** with `malli.dev/start!` (454+ functions)
- **CLJ-Kondo config generation** automatic for static type checking
- **Hot code reloading** with proper dependency management
- **Auto-reinstrumentation** when schemas change

### NREPL Development (`make nrepl`)
Starts NREPL server with full development environment pre-initialized:
- **Automatic initialization** before server starts (same as `make dev`)
- Loads and instruments all 90+ project namespaces upfront
- Shows "instrumented 454 function vars" in terminal output
- NREPL server on port 7888 with CIDER support
- Can run in parallel with `make dev`

Key REPL functions (available immediately):
- `(help)` - Show all available commands and current status
- `(reload!)` - Reload modified namespaces using tools.namespace
- `(reload-all!)` - Reload all namespaces from scratch
- `(clear-aliases!)` - Clear namespace aliases (fixes reload issues)
- `(check-functions!)` - Validate all function schemas with generative testing
- `(set-throw-mode!)` - Throw exceptions on validation errors
- `(set-print-mode!)` - Print validation errors (default)
- `(instrumented-count)` - Count instrumented functions (shows ~454)
- `(instrumented? 'fn)` - Check if specific function is instrumented
- `(uninstrument! 'fn)` - Remove instrumentation from function
- `(restart-logging!)` - Restart the logging system

### Development UI (`make dev`)
Uses `dev/dev.clj` to run the app with full instrumentation:
- Shows comprehensive initialization output in terminal
- Loads all 90+ project namespaces using tools.namespace
- Instruments 454+ functions with Malli schemas
- Starts the UI with validation enabled
- Prints validation errors to console in real-time
- Auto-reinstruments on schema changes via `malli.dev/start!`
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

## Important: Use Specialized Agents

### Test Runner Analyzer Agent
Use for running tests and getting detailed failure reports:
```
Task: test-runner-analyzer agent
Prompt: "Run tests in /home/jare/git/potatoclient using command: make test"
```

### I18n Checker Agent
Use for translation validation and stub generation:
```
Task: i18n-checker agent
Prompt: "Check translation completeness"
```

## Core Technologies

### Malli Schema System
**ALL functions MUST have `:malli/schema` metadata**

```clojure
(defn process-command
  "Process a command and return result"
  {:malli/schema [:=> [:cat :cmd/root] [:map [:status :keyword]]]}
  [cmd]
  ...)
```

**Registry Management:**
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

## Development Guidelines

### Code Quality Standards

1. **Malli Schemas Required** - Every function must have `:malli/schema`
2. **Comprehensive Tests** - Organized into test suites with extensive coverage
3. **Property-Based Testing** - Extensive use of generative testing with Malli schemas
4. **Clear Documentation** - Docstrings for all public functions
5. **No Legacy Code** - Pre-alpha, make breaking changes when needed

### Testing Philosophy

**Never disable failing tests - fix the code**
- Use test suites for focused testing
- Property-based testing with `clojure.test.check`
- Generative testing with Malli schemas
- Test proto serialization roundtrips
- Validate all command constraints
- IPC handler property tests for message processing

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
2. **Add to registry if reusable** - Don't duplicate specs
3. **Update status bar** - User feedback for all actions
4. **Write tests immediately** - Not after "it works"
5. **Check i18n completeness** - Run i18n-checker for new UI text

### Development Workflow

**tools.namespace Integration:**
The project uses `clojure.tools.namespace` for proper code reloading:
- `(reload!)` - Intelligently reloads only changed namespaces in dependency order
- `(reload-all!)` - Complete reload from scratch when needed
- Proper cleanup of old definitions prevents stale code issues
- Clear error reporting when refresh fails
- `(clear-aliases!)` helper to fix namespace alias conflicts

**Instrumentation Workflow:**
1. Start with `make dev` or `make nrepl` - all functions instrumented
2. Edit code and save files
3. Call `(reload!)` to refresh changed namespaces
4. Functions are automatically re-instrumented via `malli.dev/start!`
5. Validation errors appear immediately in console or REPL

**Key Improvements:**
- **Upfront initialization** - No waiting for instrumentation after NREPL connects
- **Consistent experience** - Both `make dev` and `make nrepl` use same initialization
- **Better error handling** - Refresh operations show clear error messages
- **454+ functions instrumented** - Comprehensive validation coverage
- **Auto-reinstrumentation** - Schema changes are picked up automatically

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

This separation avoids circular dependencies and provides:
- Full namespace discovery with tools.namespace
- Automatic Malli instrumentation
- CLJ-Kondo config generation
- Better REPL workflow with code reloading

## Project Principles

### Core Philosophy
- **No backward compatibility** - Pre-alpha stage
- **Clean architecture over legacy support**
- **Breaking changes when needed**
- **No deprecated code**

### Quality Standards
- **100% Malli coverage** for functions
- **Closed map specs** with `{:closed true}`
- **Generative testing** with schemas
- **Never delete tests** - make them pass

### User Experience
- **Immediate feedback** via status bar
- **Specific error messages** with context
- **Progress indicators** for long operations
- **Consistent UI state** with bindings
