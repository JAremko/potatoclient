# Developer Guide

PotatoClient is a multi-process video streaming client with dual H.264 WebSocket streams. Main process (Clojure) handles UI, subprocesses (Java) handle video streams.

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
- Functions must return `nil` for side-effect operations (not `true` or other values)
- For variadic keyword arguments `& {:keys [...]}`, use `(? (s/* any?))` in the spec
- Remember to require `[clojure.spec.alpha :as s]` when using spec-based Guardrails

### Handling Spec Failures

When Guardrails reports a spec failure, evaluate which needs adjustment:

1. **Check the function's actual behavior** - Does it naturally return a value?
   ```clojure
   ;; If clipboard/contents! returns the clipboard object for chaining:
   (>defn- copy-to-clipboard
           [text]
           [string? => any?]  ; Fix the spec, not the function
           (clipboard/contents! text))
   ```

2. **Check the function's intent** - Should it be a pure side-effect?
   ```clojure
   ;; If the function should only perform side effects:
   (>defn- log-message
           [msg]
           [string? => nil?]
           (println msg)
           nil)  ; Add explicit nil return
   ```

3. **Consider idiomatic Clojure** - Many functions return useful values for chaining or debugging
   - File operations often return the file object
   - State mutations often return the new state
   - UI operations often return the component for chaining

**Rule of thumb**: Preserve the natural behavior of functions unless there's a specific reason to enforce `nil` returns. Adjust the spec to match reality rather than forcing artificial constraints.

## Quick Reference

**IMPORTANT**: Use `make dev` for all development work. This command takes 30-40 seconds to start, so set appropriate timeouts in your tools. Do not use short timeouts!

```bash
make dev              # PRIMARY DEVELOPMENT COMMAND - Full validation, all logs, warnings
make nrepl            # REPL on port 7888 for interactive development
make report-unspecced # Check which functions need Guardrails specs
make clean            # Clean all build artifacts
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
- `WebSocketClientBuiltIn` - Java 17's built-in HttpClient for WSS connections
- Hardware decoder selection and fallback
- Auto-reconnection with 1-second delay
- Buffer pooling for zero-allocation streaming
- Optimized lock-free data pushing
- Direct pipeline without frame rate limiting
- Trust-all SSL certificates (development/testing only)

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

## Development Workflow

### Starting Development

**Always use `make dev`**:
```bash
make dev  # Wait 30-40 seconds for startup
```

⚠️ **IMPORTANT**: The startup takes 30-40 seconds because it:
- Compiles all proto files
- Compiles Java sources
- Initializes the Clojure runtime with full validation
- Loads all development features

Set your tool timeouts accordingly - do not use short timeouts!

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

**Build**: Java 17+, Protobuf 3.15.0 (bundled)
**Streams**: Heat (900x720), Day (1920x1080)
**WebSocket**: Built-in Java 17 HttpClient (no external dependencies)

**Performance Optimizations**:
- Zero-allocation streaming with dual buffer pools (WebSocket + GStreamer)
- Direct ByteBuffers for optimal native interop
- Lock-free fast path for video data
- Direct pipeline without unnecessary elements
- Hardware acceleration prioritized
- Automatic message buffer trimming to prevent memory bloat
- Comprehensive performance metrics and pool statistics

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

### WebSocket Client Migration & Hot Path Optimization
1. **Replaced Java-WebSocket with Java 17's HttpClient**: Eliminated external dependency
2. **Built-in WSS Support**: Native WebSocket Secure connections with certificate trust override
3. **Simplified Reconnection**: Fixed 1-second delay instead of exponential backoff
4. **Zero-Allocation Streaming**: 
   - Dual buffer pools (WebSocket + GStreamer layers)
   - Direct ByteBuffers for native interop
   - Eliminated unnecessary buffer copies for single-fragment messages
   - Pool statistics tracking for performance monitoring
5. **Memory Management**:
   - Automatic message buffer trimming every 60 seconds
   - Pre-allocated buffer pools to reduce startup allocations
   - Proper buffer lifecycle management with automatic returns to pool

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

### Logging Best Practices

1. **Use appropriate log levels**: DEBUG for development details, INFO for normal operations, WARN for issues, ERROR for failures
2. **Check logs during development**: Look in `./logs/` directory for timestamped log files
3. **Monitor production logs**: Only warnings and errors appear on stdout/stderr in release builds

### Common Guardrails Errors and Solutions

**"Return value should be nil"**:
- Your function spec says `=> nil?` but you're returning a value
- Fix: Add explicit `nil` at the end of the function

**"should be an int"**:
- You're passing the wrong type to a function
- Fix: Check the function's input specs and fix the caller

**"Return value should be positive"**:
- Your such-that predicate (`|`) is failing
- Fix: Ensure your function returns values that satisfy the predicate