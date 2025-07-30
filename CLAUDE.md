# Developer Guide

PotatoClient is a high-performance multi-process live video streaming client with dual H.264 WebSocket streams. The architecture uses Transit-based IPC to completely isolate protobuf handling in Kotlin subprocesses, while the main Clojure process handles UI and coordination.

## Architecture Overview

- **Main Process (Clojure)**: UI, state management, subprocess coordination
- **Command Subprocess (Kotlin)**: Receives Transit commands, converts to protobuf, sends via WebSocket
- **State Subprocess (Kotlin)**: Receives protobuf state from WebSocket, converts to Transit, sends to main process
- **Video Subprocesses (Kotlin)**: Handle H.264 video streams with hardware acceleration

**Key architectural principle**: Protobuf is completely isolated in Kotlin subprocesses. The Clojure process never touches protobuf directly, using Transit/MessagePack for all IPC communication.

**Documentation:**
- **Transit Architecture**: [.claude/transit-architecture.md](.claude/transit-architecture.md) - Complete Transit implementation details
- **Kotlin Subprocesses**: [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md) - Video streaming and subprocess details
- **Protobuf Commands**: [.claude/protobuf-command-system.md](.claude/protobuf-command-system.md) - Command system implementation

## Important: Function Validation with Guardrails

**ALWAYS** use Guardrails' `>defn` and `>defn-` instead of regular `defn` and `defn-` for all functions. This provides runtime validation in development and is automatically removed in release builds.

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

```clojure
(require '[potatoclient.guardrails.check :as check])

;; Print report
(check/print-report)

;; Get data structure
(check/report-unspecced-functions)
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
- Lint report generators with false positive filtering capabilities

Key development commands:
- `make dev` - Primary development command with full validation
- `make nrepl` - REPL development on port 7888
- `make lint` - Run all code quality checks
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
- Linting tools for Clojure (clj-kondo) and Kotlin (ktlint, detekt)
- Running linters and generating reports
- False positive filtering (removes ~56% of false positives)
- Integration with development workflow
- Periodic build verification to ensure code hasn't been broken
- Best practices for maintaining code quality

## Testing Infrastructure

### Test Execution and Logging

The test suite now includes comprehensive logging and analysis capabilities:

**Key Commands**:
- `make test` - Run all tests with automatic logging to timestamped directories
- `make test-summary` - View the latest test run summary
- `make coverage` - Generate test coverage report with jacoco
- `make report-unspecced` - Find functions without Malli specs

**Test Logging System**:
- All test runs are automatically logged to `logs/test-runs/YYYYMMDD_HHMMSS/`
- Each run generates:
  - `test-full.log` - Complete test output
  - `test-full-summary.txt` - Compact analysis of results
  - `test-full-failures.txt` - Extracted failures for quick review
- Logs are automatically compacted and analyzed by `scripts/compact-test-logs.sh`

**Coverage Reports**:
- Coverage analysis uses jacoco via the `cloverage` tool
- Reports generated in `target/coverage/` include:
  - HTML reports for browser viewing
  - XML reports for CI integration
  - Console summary of coverage percentages
- Run `make coverage` then open `target/coverage/index.html`

### WebSocket Test Infrastructure

Tests now use a sophisticated stubbing approach instead of real WebSocket servers:

**Stubbing Benefits**:
- No port conflicts or network delays
- Deterministic test behavior
- Faster test execution (no server startup/shutdown)
- Better isolation between tests

**Key Components**:
- `test/potatoclient/test_utils.clj` - Core stubbing infrastructure
- Mock WebSocket managers with command capture
- State simulation capabilities
- Async command verification

**Example Usage**:
```clojure
(use-fixtures :each h/websocket-fixture)

(deftest test-command-sending
  (h/send-test-command! (cmd/ping))
  (is (= 1 (count (h/get-captured-commands))))
  (is (= :ping (h/get-command-type (first (h/get-captured-commands))))))
```

### Test Organization

**Unit Tests**:
- Command generation and validation
- State management and transformations
- Protobuf serialization/deserialization
- Malli schema validation

**Integration Tests**:
- WebSocket communication (stubbed)
- Event system integration
- Frame timing with CV events
- Command dispatch and handling

**Property-Based Tests**:
- Generator-based testing with Malli
- Comprehensive command coverage
- State schema validation
- Edge case discovery

## Architecture

### Transit-Based IPC Architecture

The system uses Transit/MessagePack for all inter-process communication:

**Transit Namespaces**
- `potatoclient.transit.core` - Transit reader/writer creation and message envelope handling
- `potatoclient.transit.app-db` - Single source of truth atom following re-frame pattern
- `potatoclient.transit.commands` - Command API that creates Transit messages
- `potatoclient.transit.subprocess-launcher` - Process lifecycle management for Transit subprocesses
- `potatoclient.transit.handlers` - Message handlers for incoming Transit messages

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

**Kotlin (Subprocess Components)**
- `CommandSubprocess` - Receives Transit commands, converts to protobuf, sends via WebSocket
- `StateSubprocess` - Receives protobuf state, converts to Transit maps, implements debouncing and rate limiting
- `TransitCommunicator` - Handles Transit message framing and serialization over stdin/stdout
- `SimpleCommandBuilder` - Creates protobuf commands from Transit message data
- `SimpleStateConverter` - Converts protobuf state to Transit-compatible maps
- Video streaming subprocesses with hardware acceleration
- For detailed implementation, see [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md)

### Transit Message Flow

1. **Commands (Clojure → Server)**:
   ```
   Clojure UI → Transit command → CommandSubprocess → Protobuf → WebSocket → Server
   ```

2. **State Updates (Server → Clojure)**:
   ```
   Server → WebSocket → Protobuf → StateSubprocess → Transit map → Clojure app-db
   ```

3. **Key Features**:
   - Complete protobuf isolation in Kotlin
   - Debouncing to prevent duplicate state updates
   - Token bucket rate limiting (configurable via Transit messages)
   - Automatic reconnection with exponential backoff
   - Clean subprocess lifecycle management

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

**Help Menu Features**
- **About Dialog**: Shows application version, build type (DEVELOPMENT/RELEASE)
- **View Logs**: Opens log directory in system file explorer
  - Development: Shows `./logs/` directory
  - Release: Shows platform-specific user directory (see Logging System)

**Add Event Type**
1. Define in `potatoclient.events.stream`
2. Handle in Kotlin subprocess (see [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md#event-system-integration))
3. Add to `ipc/message-handlers` dispatch table

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

### Kotlin/Java Integration
Kotlin subprocesses integrate with the main Clojure logging system via IPC. For details on Kotlin logging integration, see [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md#logging-integration).

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

## Best Practices

### Development Workflow

1. **Fix Guardrails errors immediately** - They indicate real bugs in your code
2. **Write precise specs** - Use domain-specific types from `potatoclient.specs`
3. **Check reflection warnings** - They indicate performance issues

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

