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

### NREPL Development
The project includes automatic NREPL initialization via `dev/user.clj`:
- Registry automatically initialized on NREPL startup
- Helper functions available: `(restart-logging!)`, `(reload-registry!)`
- Full development environment with instrumentation support

### Make Targets
```bash
# Development
make dev          # Run with full validation and debug logging
make nrepl        # Start NREPL server on port 7888 with auto-init
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
make kondo-configs  # Generate clj-kondo type configs

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
- Automatic initialization in main, dev, nrepl, and test entry points

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
2. **Comprehensive Tests** - 358 tests with 3091 assertions, organized into suites
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

### When Adding Features

1. **Define Malli specs first** - Data contracts before implementation
2. **Add to registry if reusable** - Don't duplicate specs
3. **Update status bar** - User feedback for all actions
4. **Write tests immediately** - Not after "it works"
5. **Check i18n completeness** - Run i18n-checker for new UI text

### Initialization Pattern

All entry points should use centralized initialization:
```clojure
(require '[potatoclient.init :as init])

;; For production/main
(init/initialize!)

;; For tests
(init/initialize-for-tests!)

;; For NREPL
(init/initialize-for-nrepl!)

;; To ensure registry only (lightweight)
(init/ensure-registry!)
```

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
