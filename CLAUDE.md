# Developer Guide

PotatoClient is a high-performance multi-process video streaming client with dual H.264 WebSocket streams. Main process (Clojure) handles UI, subprocesses (Kotlin) handle video streams with zero-allocation streaming and hardware acceleration.

**For detailed Kotlin subprocess architecture and implementation details, see:** [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md)

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

- Use `>defn` and `>defn-` for ALL functions to ensure proper validation
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

## Quick Reference

**IMPORTANT**: Use `make dev` for all development work. This command takes 30-40 seconds to start, so set appropriate timeouts in your tools. Do not use short timeouts!

```bash
# Development
make dev              # PRIMARY DEVELOPMENT COMMAND - Full validation, all logs, warnings
make nrepl            # REPL on port 7888 for interactive development
make report-unspecced # Check which functions need Guardrails specs
make clean            # Clean all build artifacts

# Code Quality
make lint             # Run all linters (clj-kondo, ktlint, detekt)
make lint-report      # Generate comprehensive lint report
make fmt              # Format all Clojure code
make fmt-check        # Check if code is properly formatted
```

### Development Workflow

1. **Always use `make dev`** - This is your main development command
   - Takes 30-40 seconds to start (be patient!)
   - Full Guardrails validation catches bugs immediately
   - All log levels (DEBUG, INFO, WARN, ERROR)
   - Reflection warnings for performance issues
   - GStreamer debug output

2. **For REPL development** - Use `make nrepl`
   - Connect your editor to port 7888
   - Same validation features as `make dev`

3. **Other commands** (rarely needed during development):
   - `make run` - Test the JAR in production-like mode
   - `make release` - Build optimized JAR for distribution
   - `make proto` - Regenerate protobuf classes (if .proto files change)

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

PotatoClient includes comprehensive linting for both Clojure and Kotlin code to maintain code quality and consistency.

### Linting Tools

**Clojure**
- **clj-kondo** (v2025.06.05) - Fast static analyzer with custom configuration
  - Configured for Guardrails, Seesaw, and Telemere macros
  - Custom hooks for `>defn`, `>defn-`, and `>def` macros
  - Reduced false positives through precise lint-as mappings

**Kotlin**
- **ktlint** (v1.5.0) - Code style checker following IntelliJ IDEA conventions
  - Enforces consistent formatting and style
  - Auto-downloads on first use
  - Configuration in `.ktlint/editorconfig`
  
- **detekt** (v1.23.7) - Advanced static analysis tool
  - Identifies code smells, complexity issues, and potential bugs
  - Comprehensive rule set in `detekt.yml`
  - Provides technical debt estimates

### Linting Commands

```bash
# Run all linters
make lint

# Run specific linters
make lint-clj          # Clojure linting with clj-kondo
make lint-kotlin       # Kotlin style checking with ktlint
make lint-kotlin-detekt # Advanced Kotlin analysis with detekt

# Generate comprehensive lint report
make lint-report       # Full report with all issues
make lint-report-warnings # Report excluding errors
```

### Lint Report Generation

The project includes a Babashka script that aggregates all linting results into a unified Markdown report:

```bash
# Generate report with default settings
make lint-report

# Custom report options
bb scripts/lint-report.bb --no-errors    # Exclude errors
bb scripts/lint-report.bb --clj-only     # Only Clojure linting
bb scripts/lint-report.bb --kotlin-only  # Only Kotlin linting
bb scripts/lint-report.bb --output custom-report.md
```

Report includes:
- Summary of total errors and warnings
- Breakdown by linter (clj-kondo, ktlint, detekt)
- Issues organized by file with line numbers
- Specific rule violations and descriptions

### clj-kondo Configuration

Custom configuration in `.clj-kondo/config.edn`:
- **Guardrails support**: `>defn`, `>defn-`, `>def` properly recognized
- **Seesaw macros**: UI construction functions linted correctly
- **Telemere logging**: Log macros understood without false positives
- **Custom hooks**: For complex macro transformations
- **Namespace grouping**: Organized imports validation

Common issues detected:
- Unresolved symbols and namespaces
- Unused imports and bindings
- Missing docstrings
- Unsorted namespaces
- Type mismatches (with Malli integration)

### Kotlin Linting Configuration

**ktlint** (`.ktlint/editorconfig`):
- IntelliJ IDEA code style
- 120 character line limit
- 4-space indentation
- Import ordering enforcement
- Trailing comma rules

**detekt** (`detekt.yml`):
- Complexity thresholds (cognitive and cyclomatic)
- Exception handling patterns
- Naming conventions
- Performance anti-patterns
- Code smell detection

### Integration with Development Workflow

1. **During Development**: Run `make lint` regularly to catch issues early
2. **Before Commits**: Use `make lint-report-warnings` to focus on non-error issues
3. **CI/CD Integration**: All linters can be integrated into GitHub Actions
4. **IDE Integration**: IntelliJ IDEA with Cursive automatically uses clj-kondo

### Fixing Common Issues

**Clojure**:
- Missing docstrings: Add docstrings to public functions
- Unused imports: Remove or use `:refer :all` sparingly
- Unresolved symbols: Often due to macros - check lint-as configuration

**Kotlin**:
- Trailing spaces: Configure your editor to trim on save
- Import ordering: Use IntelliJ's "Optimize Imports" (Ctrl+Alt+O)
- Complexity warnings: Refactor large functions into smaller ones

## Architecture

### Key Components

**Clojure (Main Process)**
- `potatoclient.main` - Entry point with dev mode support
- `potatoclient.core` - Application initialization, menu creation
- `potatoclient.state` - Centralized state management with specs
- `potatoclient.process` - Subprocess lifecycle with type hints
- `potatoclient.proto` - Direct Protobuf serialization (custom implementation)
- `potatoclient.ipc` - Message routing and dispatch
- `potatoclient.config` - Platform-specific configuration
- `potatoclient.i18n` - Localization (English, Ukrainian)
- `potatoclient.theme` - Theme support (Sol Dark, Sol Light)
- `potatoclient.runtime` - Runtime detection utilities
- `potatoclient.specs` - Centralized Malli schemas
- `potatoclient.instrumentation` - Function schemas (dev only)
- `potatoclient.logging` - Telemere-based logging configuration
- `potatoclient.cmd.core` - Command system infrastructure
- `potatoclient.cmd.rotary` - Rotary platform commands
- `potatoclient.cmd.day-camera` - Day camera commands

**Kotlin (Stream Processes)**
- High-performance video streaming with hardware acceleration
- Zero-allocation design with lock-free buffer pools
- WebSocket and GStreamer pipeline integration
- For detailed implementation, see [.claude/kotlin-subprocess.md](.claude/kotlin-subprocess.md)

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
For detailed Protobuf implementation, command system architecture, and troubleshooting, see [.claude/protobuf-command-system.md](.claude/protobuf-command-system.md)

## Command System

PotatoClient includes a command system based on the TypeScript web frontend architecture, enabling control message sending via Protobuf.

**For complete command system documentation**, including:
- Architecture overview and package structure
- Command validation and domain specs
- Infrastructure for all platforms (core, rotary, day camera)
- Command flow and JSON output format
- Testing and debugging utilities
- Protobuf implementation details

See [.claude/protobuf-command-system.md](.claude/protobuf-command-system.md)

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
;; Use the debouncing utilities from potatoclient.ui.utils
(require '[potatoclient.ui.utils :as ui-utils])

;; Debounce rapid updates
(def save! (ui-utils/debounce save-to-disk 1000))

;; Throttle expensive operations
(def render! (ui-utils/throttle render-preview 100))

;; Batch UI updates for better performance
(ui-utils/batch-updates
  [#(update-label label1)
   #(update-label label2)
   #(refresh-table table)])

;; Lazy rendering for large lists
(seesaw/table :model model
           :renderer (proxy [DefaultTableCellRenderer] []
                      (getTableCellRendererComponent [table value selected focus row col]
                        ;; Only render visible cells
                        (when (.isShowing table)
                          (proxy-super getTableCellRendererComponent 
                                      table value selected focus row col)))))
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

**For Command and State Functions**: When adding functions that handle commands or state derived from protobuf messages, see [.claude/protobuf-command-system.md](.claude/protobuf-command-system.md#command-validation-and-specs) for detailed information on:
- How to check `.proto` files for validation constraints
- Common protobuf validation patterns
- Creating corresponding Malli specs
- Using domain-specific specs in Guardrails functions

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

```bash
# Generate a report of functions without specs
make report-unspecced

# The report will be saved to ./reports/unspecced-functions.md
```

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

1. **Always use `make dev` during development** - Wait 30-40 seconds for startup
2. **Fix Guardrails errors immediately** - They indicate real bugs in your code
3. **Write precise specs** - Use domain-specific types from `potatoclient.specs`
4. **Check reflection warnings** - They indicate performance issues
5. **Be patient with startup** - The initial compilation takes time but catches bugs early

### Function Development

1. **Always use `>defn` or `>defn-`** - Never use raw `defn`
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

