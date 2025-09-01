# Claude AI Assistant Context

## Current Architecture Status

### Refactored Clojure UI (December 2024)
The Clojure codebase has been significantly simplified to prepare for future Kotlin integration:

**Current State:**
- **UI-Only Focus**: Main app simplified to provide only UI components (startup dialog, main frame)
- **State Management**: New `state.clj` namespace with UI-only state atom in main app
- **Stream Controls**: Stream toggle buttons are present but noop - ready for Kotlin integration
- **Shared Module IPC**: Complete IPC infrastructure available in shared module using Unix Domain Sockets
- **Preserved Components**: Java/Kotlin code intact for future integration

**Key Changes:**
- Moved app state atom to dedicated `potatoclient.state` namespace
- Fixed all namespace/file naming conventions (underscores in filenames)
- IPC infrastructure consolidated in shared module (`potatoclient.ipc.core` and `potatoclient.ipc.transit`)
- Stream control buttons remain in UI but perform no operations (logging only)
- Configuration and theme management remain functional

**Architecture Notes:**
- Main app IPC code was removed in favor of shared module's robust IPC implementation
- Shared module provides `UnixSocketCommunicator.java` for high-performance socket communication
- Transit serialization with msgpack format for efficient binary encoding
- Complete message framing protocol with length-prefixed packets

**Next Steps:**
- Integrate Kotlin video stream processes using shared module's IPC
- Connect stream controls to Kotlin backend via Unix Domain Sockets
- Leverage existing IPC infrastructure from shared module for Clojure-Kotlin communication


## Development Tools

### I18n Translation Checker
**Location**: `tools/i18n-checker/`

A tool for checking translation completeness and consistency across locales.

**Features:**
- Finds all `i18n/tr` calls in the codebase using rewrite-clj for accurate parsing
- Identifies missing translation keys (used in code but not defined)
- Identifies unused translation keys (defined but not used in code)
- Checks consistency between locales (keys missing in some locales)
- Generates stub entries for missing keys

**Usage:**
```bash
cd tools/i18n-checker
./check.sh              # Generate report
./check.sh --stubs en   # Generate English stub entries
./check.sh --help       # Show help
```

**Runtime Detection (Dev Mode):**
When running in development mode (`make dev`), the i18n system automatically:
- Displays missing keys as `[MISSING: :key-name]` in the UI
- Logs warnings for missing keys with locale information
- Validates translation consistency on startup
- Only reports each missing key once to avoid log spam

## Important: Use Specialized Agents

### Proto-Class Explorer Agent
**ALWAYS use the `proto-class-explorer` agent when you need information about:**
- Protobuf message definitions
- Java class representations of proto messages
- buf.validate constraints on proto fields
- Proto field types and structures
- Mapping between proto messages and their Java implementations

**How to use:**
Provide full context when invoking:
```
Task: proto-class-explorer agent
Prompt: "Find information about [message/class name] in /home/jare/git/potatoclient/tools/proto-explorer"
```

The agent provides:
- Comprehensive proto message details
- Java class mappings (e.g., `cmd.Root` → `cmd.JonSharedCmd$Root`)
- Pronto EDN representations for Clojure integration
- Field information with buf.validate constraints
- Automatic handling of the 2-step proto-explorer workflow using Makefile commands

**Technical Details:**
- Tool location: `/home/jare/git/potatoclient/tools/proto-explorer`
- Uses Makefile commands: `make proto-search`, `make proto-list`, `make proto-info`
- Descriptor files: `output/json-descriptors/`
- Automatically compiles proto classes when needed

### Test Runner Analyzer Agent
**ALWAYS use the `test-runner-analyzer` agent for:**
- Running tests in the codebase
- Getting test execution reports with failure details
- Validating code changes through testing

**How to use:**
Instead of directly running test commands, use:
```
Task: test-runner-analyzer agent
Prompt: "Run tests in [working directory] using command: [test command]"
```
Example: "Run tests in /home/jare/git/potatoclient using command: lein test"

### I18n Checker Agent
**ALWAYS use the `i18n-checker` agent for:**
- Checking translation completeness
- Identifying missing or unused i18n keys
- Verifying consistency across locales
- Generating translation stubs

**How to use:**
```
Task: i18n-checker agent
Prompt: "Check translation completeness"
```
Or for specific tasks:
```
Task: i18n-checker agent
Prompt: "Generate stub entries for missing Ukrainian translations"
```

The agent provides:
- Complete analysis of i18n key usage
- Missing keys that will cause runtime errors
- Unused keys that can be cleaned up
- Consistency issues between locales
- Stub generation for missing translations

**Technical Details:**
- Tool location: `/home/jare/git/potatoclient/tools/i18n-checker`
- Uses rewrite-clj for accurate AST parsing
- Analyzes all .clj and .cljc files in src/
- Supports English (en) and Ukrainian (uk) locales

**IMPORTANT**: Always use this agent proactively when:
- Adding new UI components with text
- Modifying existing UI text or labels  
- Adding new features that display messages
- Before committing UI-related changes
- When working with any i18n/tr calls


## Shared Module

The `shared` module (`./shared/README.md`) is the foundational library providing:
- **Protocol Buffer definitions** for all system communication (commands and state messages)
- **Malli specifications** that mirror protobuf constraints with runtime validation
- **Command construction** with automatic field population and validation
- **Serialization utilities** for EDN ↔ Protobuf binary conversion
- **Inter-Process Communication (IPC)** using Unix Domain Sockets with Transit serialization
- **Command queue system** for async command dispatch with automatic ping keepalive
- **Multi-layer validation** (Malli, buf.validate, roundtrip testing)
- **Single source of truth** for data structures, enums, and protocol compliance

### Key Capabilities:
- **700+ test cases** ensuring robustness and reliability
- **Organized test suites** for focused testing (cmd, ipc, malli, oneof, serialization)
- **Generative testing** with malli.instrument/check for automatic edge case discovery
- **High-performance IPC** with framed messaging and zero-copy ByteBuffer operations
- **Complete command API** for all subsystems (optical, navigation, sensors, system control)

This module ensures type safety and protocol consistency across all PotatoClient subsystems. All other modules depend on it for message definitions, validation, and inter-process communication.

## Project Principles

### Core Development Philosophy
**No Legacy, No Backward Compatibility, No Versioning**
- We are in pre-alpha stage
- Focus on building a robust, clean foundation
- Make breaking changes when needed for better architecture
- No deprecated code or compatibility layers

### Code Quality Standards
**ALL Functions MUST Have Malli Metadata:**
1. **Strict Malli Schemas** - MANDATORY for every single function
   - Every `defn` and `defn-` MUST have `:malli/schema` metadata
   - Use and expand specs from our shared registry (`potatoclient.malli.registry`)
   - Reuse common specs: `:cmd/root`, `:state/root`, `:nat-int`, `:angle-degrees`, etc.
   - Create new registry entries for repeated patterns - don't duplicate specs
   - Use Malli schemas (`:int`, `:double`, `:map`) never predicates (`int?`, `double?`)
   
   Example:
   ```clojure
   (defn process-command
     "Process a command and return result"
     {:malli/schema [:=> [:cat :cmd/root] [:map [:status :keyword] [:result any?]]]}
     [cmd]
     ...)
   ```

2. **Comprehensive Tests** - Every function must be tested
3. **Documentation** - Clear docstrings explaining purpose and usage

### Generative Testing with mi/check
With Malli schemas:
- Functions with `:malli/schema` metadata can be tested automatically
- Use `(mi/collect! {:ns ['namespace.name]})` to gather schemas
- Use `(mi/check)` for automatic generative testing of all collected functions
- This finds edge cases and constraint violations automatically
- No need to write manual `defspec` tests - schemas drive the testing

**Malli Map Specs Must Be Closed:**
- All map specs must use `{:closed true}` to reject extra keys
- This ensures strict validation and prevents unintended data from passing through
- Use `malli.util/closed-schema` when working with existing schemas

### Testing Philosophy
**NO Test Deletion or Disabling - WE MAKE THEM PASS**
- Never comment out failing tests
- Never delete tests that are inconvenient
- Fix the code, not the test
- If a test is failing, it's highlighting a real issue that needs resolution

## Development Tools

### Clj-kondo Type Config Generation
The project can automatically generate clj-kondo type configurations from Malli function schemas:

**Make Targets:**
- `make kondo-configs` - Generate type configs for entire project (root + shared modules)
- `make kondo-configs-shared` - Generate type configs for shared module only
- `cd shared && make kondo-configs` - Alternative for shared module

**How it works:**
1. Auto-discovers all Clojure namespaces in src directories
2. Loads Malli registry and UI specs
3. Collects function schemas from `:malli/schema` metadata
4. Generates clj-kondo type information for static analysis
5. Writes to `.clj-kondo/metosin/malli-types-clj/config.edn`

**Benefits:**
- Arity checking based on your function specs
- Type checking for function arguments and return values
- Better IDE integration with accurate type hints
- Catches type mismatches at edit time

Run `make kondo-configs` after adding new Malli schemas to keep type checking up to date.

## User Feedback via Status Bar

### CRITICAL: All User Actions Must Be Reflected in Status Bar
**Every user action, operation result, and error MUST be communicated through the status bar**

The status bar is the primary communication channel with users. It provides immediate feedback for all operations, ensuring users understand what the application is doing and whether operations succeed or fail.

### Status Bar Architecture
The status bar is organized into modular namespaces:
- **`potatoclient.ui.status-bar.core`** - UI component creation
- **`potatoclient.ui.status-bar.messages`** - Status message functions
- **`potatoclient.ui.status-bar.validation`** - Validation with automatic status bar reporting
- **`potatoclient.ui.status-bar.helpers`** - Utility functions

### Required Functions to Use

**Basic Status Updates:**
```clojure
(require '[potatoclient.ui.status-bar.messages :as status-bar])

;; Information messages (green icon)
(status-bar/set-info! "Operation completed successfully")

;; Warning messages (orange icon)  
(status-bar/set-warning! "Connection unstable")

;; Error messages (red icon, clickable for details)
(status-bar/set-error! "Failed to save configuration")

;; Clear status
(status-bar/clear!)

;; Ready state
(status-bar/set-ready!)
```

**Action-Specific Helpers:**
```clojure
;; Configuration operations
(status-bar/set-config-saved!)
(status-bar/set-logs-exported! "/path/to/logs.txt")

;; Theme and language changes
(status-bar/set-theme-changed! :sol-dark)
(status-bar/set-language-changed! :ukrainian)

;; Connection status
(status-bar/set-connecting! "server.example.com")
(status-bar/set-connected! "server.example.com")
(status-bar/set-disconnected! "server.example.com")
(status-bar/set-connection-failed! "Connection timeout")

;; Stream operations
(status-bar/set-stream-started! :heat)  ; or :day
(status-bar/set-stream-stopped! :heat)
```

**Error Handling with Status Bar:**
```clojure
;; Wrap operations that might fail
(status-bar/with-error-handler
  (fn []
    (risky-operation)))

;; Use with-status macro for operations with progress
(status-bar/with-status "Processing data..."
  (process-large-dataset)
  (save-results))
;; Automatically shows "Ready" when complete
```

**Validation with Automatic Reporting:**
```clojure
(require '[potatoclient.ui.status-bar.validation :as v])

;; Validates and reports errors to status bar + logs
(v/validate :int user-input)  ; Returns boolean

;; Get detailed validation results
(v/validate-with-details schema data)  ; Returns {:valid? bool :errors ...}

;; Silent validation (no status bar update)
(v/valid? schema data)
```

### Guidelines for Status Bar Usage

1. **ALWAYS provide feedback for user actions:**
   - Button clicks
   - Menu selections
   - Configuration changes
   - File operations
   - Network operations

2. **Use appropriate status types:**
   - `:info` - Successful operations, general information
   - `:warning` - Non-critical issues, degraded functionality
   - `:error` - Failures requiring user attention

3. **Be specific in messages:**
   - ❌ "Error occurred"
   - ✅ "Failed to connect: Connection timeout after 30s"

4. **Show progress for long operations:**
   ```clojure
   (status-bar/set-info! "Exporting logs...")
   ;; ... perform operation ...
   (status-bar/set-logs-exported! output-path)
   ```

5. **Always restore ready state after operations:**
   - Use `with-status` macro for automatic cleanup
   - Or manually call `(status-bar/set-ready!)` after operations

6. **Log errors for debugging:**
   - All validation failures are automatically logged
   - Use `with-error-handler` to capture and display exceptions
   - Error details are stored and viewable by clicking error status

### Example: Complete User Action Flow
```clojure
(defn save-user-configuration [config]
  (status-bar/set-info! "Saving configuration...")
  (try
    ;; Validate first
    (when-not (v/validate :config/schema config)
      (throw (ex-info "Invalid configuration" {:config config})))
    
    ;; Perform save
    (config/save! config)
    
    ;; Report success
    (status-bar/set-config-saved!)
    
    (catch Exception e
      ;; Report failure with details
      (status-bar/set-error! 
        (str "Failed to save configuration: " (.getMessage e)))
      (throw e))))
```

## Development Guidelines

### When Working with Protobuf
1. Use `proto-class-explorer` agent for discovery and information
2. Ensure all proto changes are reflected in Java classes
3. Validate constraints are properly defined in proto files
4. Test proto serialization/deserialization thoroughly

### When Running Tests
1. Always use `test-runner-analyzer` agent
2. Run tests after every significant change
3. Ensure all tests pass before considering work complete
4. Add new tests for new functionality

### Code Organization
- Keep code modular and composable
- Prefer pure functions where possible
- Use meaningful names that reflect intent
- Document complex logic inline when necessary

## IPC Protocol Architecture

### Unix Domain Socket IPC (Shared Module)
The shared module provides a complete IPC infrastructure using Unix Domain Sockets with Transit serialization:

**IPC Server Features:**
- **Unix Domain Sockets** for fast, reliable local communication
- **Transit msgpack** for efficient binary serialization
- **Framed messages** with length-prefixed packets for reliable delivery
- **Async processing** with separate reader/processor threads
- **Message queue** with buffered handling (1000 message capacity)
- **Server pool management** for multiple stream servers

**Usage Example:**
```clojure
(require '[potatoclient.ipc.core :as ipc])

;; Create IPC server
(def server (ipc/create-server :heat
              :on-message handle-message))

;; Send/receive messages
(ipc/send-message server {:type :event :data {...}})
(ipc/receive-message server :timeout-ms 1000)
```

### Transit Keywords - The Foundation
**CRITICAL: Always use Transit Keywords for IPC communication**
- **Kotlin/Java side**: Use `com.cognitect.transit.Keyword` via `TransitFactory.keyword("name")`
- **Clojure side**: Automatically converts Transit Keywords to Clojure keywords
- **Never use strings** for message types, actions, or enum values - always use Transit Keywords
- This eliminates type conversion issues and ensures protocol consistency

### Implementation Architecture

**Java Infrastructure (`UnixSocketCommunicator.java`):**
- Bidirectional communication with framed messages
- Direct ByteBuffer usage for zero-copy operations
- Thread-safe with ReentrantLock for writes
- Automatic reconnection handling
- 10MB max message size with 4-byte length headers

**Clojure IPC (`potatoclient.ipc.core`):**
- High-level API wrapping Java infrastructure
- Automatic Transit serialization/deserialization
- Message type helpers (event, command, log, metric)
- Server lifecycle management with cleanup

**Kotlin Side (`TransitKeys.kt`):**
- All message keys are pre-created Transit Keywords
- Use `TransitKeys.MSG_TYPE`, `TransitKeys.EVENT`, etc.
- Never use string literals for keys or enum values

**Key Benefits:**
1. Type safety - Transit Keywords are strongly typed
2. No string conversion bugs
3. High performance with zero-copy operations
4. Reliable framed messaging protocol
5. IDE autocomplete support
