# WebSocket Refactoring Plan: Removing Kotlin Subprocesses

## Overview
This document outlines the plan to remove Kotlin subprocesses (Command and State) and reimplement their functionality using:
- **[Hato](https://github.com/gnarroway/hato)** - Modern HTTP/WebSocket client for Clojure
- **[core.async](https://github.com/clojure/core.async)** - Clojure's async programming library  
- **[Transit](https://github.com/cognitect/transit-clj)** - Only for video stream subprocess communication
- **Custom Proto Generator** - Using [rewrite-clj](https://github.com/clj-commons/rewrite-clj) for code generation

## Key Principles
- **NO BACKWARD COMPATIBILITY NEEDED** - We're building a new system from scratch
- **Clean Architecture** - Build solid bedrock with no technical debt
- **Metadata-Driven** - All configuration from proto descriptors, no hardcoding
- **Simplicity First** - Choose clean solutions over complex compatibility layers

## Current Architecture Analysis

### 1. Kotlin Subprocess Architecture

#### CommandSubprocess.kt
- **Location**: `src/potatoclient/kotlin/transit/CommandSubprocess.kt`
- **Purpose**: Converts Transit commands from Clojure to protobuf and sends to server
- **WebSocket Endpoint**: `wss://{domain}/ws/ws_cmd`
- **Protocol**: Sends binary protobuf command messages
- **Main Class**: `potatoclient.kotlin.transit.CommandSubprocessKt`
- **Reconnection**: No automatic reconnection - relies on process restart
- **Launch Args**: `[java-exe -cp classpath jvm-args main-class url domain]`
- **Related Files**:
  - `TransitMessageProtocol.kt` - Transit message envelope handling
  - `WebSocketClientBuiltIn.kt` - WebSocket implementation

#### StateSubprocess.kt
- **Location**: `src/potatoclient/kotlin/transit/StateSubprocess.kt`
- **Purpose**: Receives protobuf state updates from server and converts to Transit for Clojure
- **WebSocket Endpoint**: `wss://{domain}/ws/ws_state`
- **Protocol**: Receives binary protobuf state messages
- **Main Class**: `potatoclient.kotlin.transit.StateSubprocessKt`
- **Reconnection**: Automatic with 5-second delay (hardcoded)
- **Rate Limiting**: Token bucket (default 30Hz, configurable via control message)
- **Important**: Each state update is a full snapshot - no batching or queueing
- **Test Mode**: `StateSubprocessTestMode.kt` for stdin-based testing

#### MetadataCommandSubprocess.kt
- **Purpose**: Alternative command processor using metadata-based routing
- **Similar to CommandSubprocess but with metadata handling

### 2. WebSocket Connection Details

#### Connection Configuration
```kotlin
// URL patterns
val ws-url = "wss://${domain}/ws/ws_cmd"
val state-url = "wss://${domain}/ws/ws_state"

// Timeouts (hardcoded)
connectTimeout = Duration.ofSeconds(10)
reconnectDelay = 5000ms (StateSubprocess)
reconnectDelay = 1000ms (WebSocketClientBuiltIn)
```

#### SSL Configuration
- Uses trust-all `X509TrustManager` (accepts all certificates)
- No certificate validation
- Not configurable
- **Decision**: Keep this approach for simplicity

#### Reconnection Logic

**StateSubprocess**:
```kotlin
suspend fun connect(onMessage: suspend (ByteArray) -> Unit) = coroutineScope {
    while (isRunning.get() && isActive) {
        try {
            // Connection attempt
            val listener = StateWebSocketListener(onMessage, this)
            // ... connection logic ...
            listener.awaitDisconnection()
            connected.set(false)
        } catch (e: Exception) {
            logError("[StateWebSocket] Connection failed", e)
            delay(5000) // Wait before reconnect
        }
    }
}
```

**CommandSubprocess**: No automatic reconnection

### 3. Protobuf Message Formats

**Proto Files Location**: `tools/proto-explorer/proto/`

#### Command Messages (jon_shared_cmd.proto)
```protobuf
// Root message
message Root {
    oneof payload {
        DayCamera day_camera = 1;
        HeatCamera heat_camera = 2;
        GPS gps = 3;
        Compass compass = 4;
        LRF lrf = 5;
        RotaryPlatform rotary = 6;
        OSD osd = 7;
        System system = 8;
        CV cv = 9;
        Ping ping = 10;
        Noop noop = 11;
        Frozen frozen = 12;
    }
}
```

#### State Messages (jon_shared_data.proto)
```protobuf
// Root message
message JonGUIState {
    System system = 1;
    GPS gps = 2;
    Compass compass = 3;
    Rotary rotary = 4;
    // ... other state fields
}
```

### 4. Control Messages
All subprocesses support:
- `shutdown` - Graceful shutdown with 100ms grace period
- `set-rate-limit` - State subprocess only

### 5. Test Mode Support
- CommandSubprocess: `--test-mode` flag uses `TestModeWebSocketStub`
- StateSubprocess: `StateSubprocessTestMode.kt` reads protobuf from stdin

## Mock-Video-Stream Reference Implementation

**Location**: `tools/mock-video-stream/`
**Documentation**: `tools/mock-video-stream/README.md`

### Transit Communication Pattern
```clojure
;; Standard message envelope
{:msg-type :command      ; or :event, :log, :response, :control
 :msg-id "uuid-string"
 :timestamp 1234567890
 :payload {...}}

;; Message handling with multimethod dispatch
(defmulti handle-message 
  (fn [msg _ _] (:msg-type msg)))

;; Bidirectional Transit communication
(def reader (transit/reader (PushbackInputStream. System/in) :msgpack))
(def writer (transit/writer (BufferedOutputStream. System/out) :msgpack))
```

### Key Patterns to Adopt
1. Multimethod-based message routing
2. Atom-based state management
3. Structured error handling with Transit logging
4. Clean separation of concerns (process/core/handlers)

## Refactoring Steps - UPDATED PROGRESS

### Phase 1: Research and Planning ✅ COMPLETED
- [x] Research current Kotlin subprocess implementation
- [x] Analyze mock-video-stream reference implementation
- [x] Research protobuf version compatibility issues
- [x] Research Hato WebSocket capabilities
- [x] Create detailed implementation plan

### Phase 1.5: Proto Serialization Generator 🚧 IN PROGRESS
- [x] Create new tool: `tools/proto-clj-generator`
  - [x] Set up project structure with deps.edn
  - [x] Add dependencies: rewrite-clj, core.match, malli, cheshire
  - [ ] Include custom oneof schema from `shared/specs/custom/potatoclient/specs/malli_oneof.clj`
    - [ ] Add shared/specs to classpath
    - [ ] Register custom :oneof schema type in Malli registry
    - [ ] Use same oneof handling as proto-explorer for consistency
- [x] **NEW: Create metadata-driven dependency resolution system**
  - [x] Create `proto_registry.clj` - Centralized proto file metadata registry
  - [x] Create `naming_config.clj` - Configurable naming conventions
  - [x] Implement dynamic namespace resolution (no hardcoding)
  - [x] Build comprehensive type lookup from JSON descriptors
  - [x] Create dynamic alias generation system
  - [x] Add comprehensive tests for new system
- [ ] Parse JSON descriptors from proto-explorer
  - [x] Extract file/package/namespace mappings dynamically
  - [ ] Parse message types, fields, oneofs, enums
  - [ ] Build internal representation for code generation
- [ ] Design rewrite-clj templates for different field types
  - [ ] Template for simple fields (string, int, float, etc.)
  - [ ] Template for message fields (nested objects)
  - [ ] Template for repeated fields (lists)
  - [ ] Template for oneof fields using custom :oneof schema
  - [ ] Template for enum conversions
- [ ] Implement code generation using templates
  - [ ] Generate builder functions (Clojure → Protobuf)
  - [ ] Generate parser functions (Protobuf → Clojure)
  - [ ] Generate macros for common patterns
  - [ ] Add proper imports and namespace declarations
  - [ ] Generate Malli specs using custom :oneof for validation
- [ ] Generate bidirectional converters for commands
  - [ ] Handle all command types from `jon_shared_cmd.proto`
  - [ ] Support oneof payload selection using custom schema
- [ ] Generate bidirectional converters for state
  - [ ] Handle full state message from `jon_shared_data.proto`
  - [ ] Support all nested state components
- [ ] Add roundtrip tests
  - [ ] Test every message type can roundtrip
  - [ ] Validate against existing Malli specs
  - [ ] Property-based testing with generators (including oneof generator)
- [ ] Add buf.validate compliance testing
  - [ ] Ensure generated protobuf respects constraints
  - [ ] Test boundary conditions

### Phase 2: Infrastructure Setup
- [ ] Set up Hato WebSocket client helpers
  - [ ] Create `potatoclient.websocket.core` namespace
  - [ ] Implement trust-all SSL context function
  - [ ] Create reconnecting WebSocket wrapper
  - [ ] Add connection lifecycle logging
- [ ] Integrate generated proto conversion code
  - [ ] Add generated code to classpath
  - [ ] Create facade functions for easy usage
- [ ] Set up core.async channels for internal communication
  - [ ] Design channel topology (command-in, state-out, etc.)
  - [ ] Create `potatoclient.websocket.channels` namespace
  - [ ] Implement channel creation and management
- [ ] Create WebSocket connection management
  - [ ] Implement exponential backoff (1s → 2s → 4s → ... → 30s)
  - [ ] Handle connection state tracking
  - [ ] Add health check/ping support
- [ ] Implement rate limiting for state updates
  - [ ] Create token bucket rate limiter (30Hz default)
  - [ ] Drop messages when rate exceeded
  - [ ] Add metrics for dropped messages

### Phase 3: Command Subprocess Replacement
- [ ] Implement Hato WebSocket client for ws_cmd endpoint
  - [ ] Create `potatoclient.websocket.command` namespace
  - [ ] Connect to `wss://{domain}/ws/ws_cmd`
  - [ ] Handle binary protobuf messages
  - [ ] Implement error handling and logging
- [ ] Use generated Clojure→Protobuf conversion functions
  - [ ] Import generated `potatoclient.proto.command` namespace
  - [ ] Convert commands using `build-root` function
  - [ ] Handle all command types (rotary, camera, system, etc.)
- [ ] Set up core.async command routing
  - [ ] Read from command-in channel
  - [ ] Validate with Malli specs
  - [ ] Convert to protobuf
  - [ ] Send via WebSocket
- [ ] Add comprehensive test suite
  - [ ] Unit tests for each command type
  - [ ] Integration tests with mock WebSocket
  - [ ] Error handling tests
- [ ] Add spec validation for all commands
  - [ ] Use specs from `potatoclient.specs.command`
  - [ ] Validate before conversion
  - [ ] Log validation errors
- [ ] Performance testing vs Kotlin implementation
  - [ ] Measure conversion latency
  - [ ] Test throughput (commands/second)
  - [ ] Memory usage comparison

### Phase 4: State Subprocess Replacement
- [ ] Implement Hato WebSocket client for ws_state endpoint
  - [ ] Create `potatoclient.websocket.state` namespace
  - [ ] Connect to `wss://{domain}/ws/ws_state`
  - [ ] Handle incoming binary protobuf messages
  - [ ] Implement automatic reconnection
- [ ] Use generated Protobuf→Clojure conversion functions
  - [ ] Import generated `potatoclient.proto.state` namespace
  - [ ] Parse state using `parse-state` function
  - [ ] Handle all state components
- [ ] Implement rate limiting (30Hz default - drop messages when exceeded)
  - [ ] Create token bucket with 30 tokens/second
  - [ ] Drop messages when bucket empty
  - [ ] Log dropped message count
  - [ ] Make rate configurable
- [ ] Set up core.async state distribution
  - [ ] Write to state-out pub/sub channel
  - [ ] Allow multiple subscribers
  - [ ] Handle slow consumers (drop policy)
- [ ] Add comprehensive test suite
  - [ ] Unit tests for state parsing
  - [ ] Rate limiting tests
  - [ ] Integration tests with mock data
- [ ] Add spec validation for all state updates
  - [ ] Use specs from `potatoclient.specs.state`
  - [ ] Validate after parsing
  - [ ] Log validation errors
- [ ] Performance testing vs Kotlin implementation
  - [ ] Measure parsing latency
  - [ ] Test with 30Hz update rate
  - [ ] Memory usage under load

### Phase 5: Integration and Migration
- [ ] Update main app to use new Clojure implementations
  - [ ] Replace subprocess launcher calls in `core.clj`
  - [ ] Update app-db to remove subprocess state
  - [ ] Connect UI to new WebSocket channels
  - [ ] Update command sending functions
- [ ] Remove subprocess launching code
  - [ ] Remove `subprocess_launcher.clj`
  - [ ] Remove subprocess monitoring code
  - [ ] Clean up process management utilities
- [ ] Update configuration system
  - [ ] Remove subprocess-specific config
  - [ ] Add WebSocket client config options
  - [ ] Update environment variables
- [ ] Integration testing with real server
  - [ ] Test all command types
  - [ ] Verify state updates work correctly
  - [ ] Test reconnection scenarios
  - [ ] Load testing with real data rates
- [ ] Update documentation
  - [ ] Update CLAUDE.md with new architecture
  - [ ] Update developer guides in `docs/`
  - [ ] Add migration guide
  - [ ] Update README.md
- [ ] Remove Kotlin subprocess code
  - [ ] Delete `src/potatoclient/kotlin/transit/`
  - [ ] Remove Kotlin test files
  - [ ] Update build.clj to remove Kotlin compilation

### Phase 6: Cleanup
- [x] Remove tools/transit-test-generator (already removed)
- [ ] Update build scripts
  - [ ] Remove Kotlin compilation from Makefile
  - [ ] Update deps.edn to remove Kotlin-related deps
  - [ ] Clean up build.clj
  - [ ] Update CI/CD pipelines
- [ ] Update deployment process
  - [ ] Remove subprocess JAR packaging
  - [ ] Update AppImage configuration
  - [ ] Simplify deployment scripts
  - [ ] Update release process
- [ ] Final testing in all environments
  - [ ] Test on Linux (primary platform)
  - [ ] Test on macOS
  - [ ] Test on Windows
  - [ ] Test AppImage distribution
  - [ ] Verify all features work correctly

## Technical Considerations

### Real-time Requirements
- **No batching**: Commands sent immediately as they arrive
- **No queueing**: State updates processed as they come
- **Full snapshots**: Each state update is complete - can safely drop intermediate ones
- **Rate limiting**: When exceeded, drop messages rather than queue
- **Low latency**: Direct protobuf conversion without intermediate formats

### Protobuf Version Compatibility
- **Current project**: Uses protobuf-java 4.31.1
- **No version downgrade needed**: Custom generator works with current version
- **Direct protobuf usage**: No intermediate libraries required

### Protogen Tool Updates 
- **No updates needed**: Protogen remains unchanged
- **Use as-is**: Continue generating bindings for other languages
- **JSON descriptors**: Use existing output for our custom generator
- **Spec generation**: Already handled by proto-explorer

### Hato WebSocket Capabilities

**Documentation**: [Hato GitHub](https://github.com/gnarroway/hato) | [WebSocket API](https://github.com/gnarroway/hato#websockets)

Hato is a modern HTTP client for Clojure built on JDK 11's HttpClient, providing comprehensive WebSocket support:

#### Key Features
- ✅ **Binary message support**: Native ByteBuffer handling for protobuf
- ❌ **No automatic reconnection**: Must implement manually with exponential backoff
- ✅ **Full lifecycle management**: Open, message, close, error, ping/pong callbacks
- ✅ **SSL/TLS configuration**: Trust-all for simplicity
- ✅ **Timeout configuration**: Connection and read timeouts
- ✅ **Async by default**: Returns CompletableFuture, integrates with manifold/core.async

#### WebSocket Implementation Example
```clojure
(require '[hato.websocket :as ws])

(defn create-command-websocket [domain]
  (ws/websocket (str "wss://" domain "/ws/ws_cmd")
    {:connect-timeout 10000  ; 10 seconds like current Kotlin impl
     :headers {"User-Agent" "PotatoClient/1.0"}
     :http-client {:ssl-context (trust-all-ssl-context)}  ; Don't verify certs
     :on-open (fn [ws] 
                (log/info "Command WebSocket connected"))
     :on-message (fn [ws msg last?]
                   (log/warn "Unexpected message on command channel"))
     :on-close (fn [ws status reason]
                 (log/info "Command WebSocket closed" status reason))
     :on-error (fn [ws error]
                 (log/error "Command WebSocket error" error))}))
```

#### Reconnection Strategy Implementation
```clojure
(defn create-reconnecting-websocket [url options]
  (let [reconnect-delay (atom 1000)
        max-delay 30000
        should-reconnect (atom true)]
    
    (letfn [(connect []
              (when @should-reconnect
                (try
                  @(ws/websocket url 
                     (assoc options
                       :on-close (fn [ws status reason]
                                   (when (and @should-reconnect 
                                            (not= status 1000)) ; Not normal closure
                                     (schedule-reconnect)))
                       :on-error (fn [ws error]
                                   (when @should-reconnect
                                     (schedule-reconnect)))))
                  (reset! reconnect-delay 1000) ; Reset on success
                  (catch Exception e
                    (schedule-reconnect)))))
            
            (schedule-reconnect []
              (future
                (Thread/sleep @reconnect-delay)
                (swap! reconnect-delay #(min (* % 2) max-delay))
                (connect)))]
      
      (connect))))
```

#### Comparison with Current Implementation

| Feature | Current Kotlin | Hato |
|---------|----------------|------|
| Binary Messages | ✅ ByteBuffer | ✅ ByteBuffer |
| SSL/TLS | ✅ Trust-all | ✅ Trust-all (simplified) |
| Reconnection | ✅ Built-in (State only) | ❌ Manual needed |
| Timeout Config | ❌ Hardcoded 10s | ✅ Configurable |
| Process | ❌ Separate JVM | ✅ Same process |

### Custom Proto Serialization Generator

We'll generate our own serialization code similar to the existing Kotlin generator pattern, giving us full control over the conversion process.

#### Key Design Decisions
1. **Use existing proto-explorer JSON descriptors** - Already have complete protobuf metadata
2. **Generate Clojure code** - Direct map↔protobuf conversion functions
3. **Keep current protobuf-java 4.x** - No version downgrade needed
4. **Template-based generation** - Use rewrite-clj + core.match for clean code generation
5. **Metadata-driven resolution** - No hardcoded namespaces or aliases

#### Code Generation Architecture

**Tools**: 
- [rewrite-clj](https://cljdoc.org/d/rewrite-clj/rewrite-clj/1.2.50/doc/readme) - AST manipulation
- [core.match](https://github.com/clojure/core.match) - Pattern matching for template selection
- [Malli](https://github.com/metosin/malli) - AST validation before code generation

**Template Example with Validation**:
```clojure
(require '[rewrite-clj.zip :as z]
         '[clojure.core.match :refer [match]]
         '[malli.core :as m])

;; Schema for generated function AST
(def FunctionAST
  [:map
   [:name keyword?]
   [:args [:vector keyword?]]
   [:body [:sequential any?]]])

;; Template for protobuf builder function
(defn builder-template [class-name fields]
  (let [ast (z/of-string
              "(defn ~builder-name [m]
                 (-> (~class-name/newBuilder)
                     ~@field-setters
                     (.build)))")
        fn-map {:name builder-name
                :args '[m]
                :body field-setters}]
    ;; Validate AST structure before returning
    (when-not (m/validate FunctionAST fn-map)
      (throw (ex-info "Invalid function AST" 
                      {:error (m/explain FunctionAST fn-map)})))
    ast))

;; Template for field setter with validation
(defn field-setter-template [field]
  (let [setter-ast
        (match [(:type field) (:repeated field)]
          ["TYPE_MESSAGE" false] `(.~setter (build-~type-fn ~getter))
          ["TYPE_ENUM" false]    `(.~setter (enum->proto ~type-ref ~getter))
          ["TYPE_STRING" false]  `(.~setter ~getter)
          [_ true]              `(.~add-all ~getter))]
    ;; Validate setter matches expected protobuf pattern
    (validate-setter-ast setter-ast field)
    setter-ast))

;; Validate generated code matches our specs
(defn validate-generated-ns [ns-ast proto-spec]
  (let [ns-data (ast->data ns-ast)]
    (when-not (m/validate proto-spec ns-data)
      (throw (ex-info "Generated namespace doesn't match proto spec"
                      {:error (m/explain proto-spec ns-data)})))))
```

#### Generated Code Structure
```clojure
;; Generated namespace: potatoclient.proto.command
(ns potatoclient.proto.command
  (:import [jon.shared.protobuf.cmd Root DayCamera HeatCamera]))

;; Map → Protobuf
(defn build-root [m]
  (match (first m)  ; oneof handling
    [:day-camera v] (-> (Root/newBuilder)
                        (.setDayCamera (build-day-camera v))
                        (.build))
    [:heat-camera v] (-> (Root/newBuilder)
                         (.setHeatCamera (build-heat-camera v))
                         (.build))))

;; Protobuf → Map  
(defn parse-root [^Root proto]
  (case (.getPayloadCase proto)
    DAY_CAMERA {:day-camera (parse-day-camera (.getDayCamera proto))}
    HEAT_CAMERA {:heat-camera (parse-heat-camera (.getHeatCamera proto))}))
```

#### Advantages of Custom Generator
1. **No version conflicts** - Use current protobuf-java 4.x
2. **Full control** - Custom handling for our specific patterns
3. **Better performance** - Direct method calls, no reflection
4. **Maintainable** - Templates are readable and modifiable
5. **Consistent** - Matches existing Kotlin generator pattern
6. **Validation** - Malli validation on both AST and runtime data
7. **No backward compatibility** - Clean implementation from scratch

#### Integration with Existing System

**Input**: JSON descriptors from proto-explorer

**Location**: `tools/proto-explorer/output/json-descriptors/`
**Files**: 
- `jon_shared_cmd.json` - Command definitions
- `jon_shared_data.json` - State definitions
- Individual component files (e.g., `jon_shared_cmd_cv.json`)

**Example Structure**:
```json
{
  "messageType": [{
    "name": "Root",
    "field": [{
      "name": "day_camera",
      "number": 1,
      "type": "TYPE_MESSAGE",
      "typeName": ".jon.shared.protobuf.cmd.DayCamera",
      "oneofIndex": 0
    }],
    "oneofDecl": [{"name": "payload"}]
  }]
}
```

**Output**: Generated Clojure namespace with macros and validation:
```clojure
(ns potatoclient.proto.command
  (:require [malli.core :as m]
            [potatoclient.specs.command :as cmd-spec])
  (:import [jon.shared.protobuf.cmd Root DayCamera]))

;; Generated macros for common patterns
(defmacro defbuilder
  "Define a protobuf builder function with validation"
  [name proto-class spec & field-mappings]
  `(defn ~name [m#]
     {:pre [(m/validate ~spec m#)]}
     (-> (~(symbol (str proto-class "/newBuilder")))
         ~@(map (fn [[k field setter]]
                  `(~setter (~(symbol (str "build-" field)) (~k m#))))
                field-mappings)
         (.build))))

;; Macro for oneof handling
(defmacro match-oneof
  "Match on oneof field and build appropriate protobuf"
  [proto-class m & cases]
  `(match (first ~m)
     ~@(mapcat (fn [[k builder-fn]]
                 `[~k (-> (~(symbol (str proto-class "/newBuilder")))
                          (~(symbol (str ".set" (name k))) (~builder-fn (second ~m)))
                          (.build))])
               (partition 2 cases))
     :else (throw (ex-info "Unknown oneof variant" {:data ~m}))))

;; Macro for bidirectional message conversion
(defmacro defconverter
  "Define bidirectional protobuf converter with validation"
  [name proto-class spec & field-mappings]
  `(do
     ;; Clojure → Protobuf
     (defn ~(symbol (str "build-" name)) [m#]
       {:pre [(m/validate ~spec m#)]}
       (-> (~(symbol (str proto-class "/newBuilder")))
           ~@(map (fn [[k field setter]]
                    `(~setter (~(symbol (str "build-" field)) (~k m#))))
                  field-mappings)
           (.build)))
     
     ;; Protobuf → Clojure
     (defn ~(symbol (str "parse-" name)) [proto#]
       (let [result# (hash-map
                      ~@(mapcat (fn [[k field getter]]
                                  `[~k (~(symbol (str "parse-" field)) 
                                        (~getter proto#))])
                                field-mappings))]
         {:post [(m/validate ~spec result#)]}
         result#))))

;; Generated converters - bidirectional
(defconverter goto-ndc GotoNdc ::cmd-spec/goto-ndc
  [:x float .setX .getX]
  [:y float .setY .getY]
  [:channel channel .setChannel .getChannel])

;; Macro for oneof bidirectional handling
(defmacro defconverter-oneof
  "Define bidirectional oneof converter"
  [name proto-class & cases]
  `(do
     ;; Clojure → Protobuf
     (defn ~(symbol (str "build-" name)) [m#]
       (match (first m#)
         ~@(mapcat (fn [[k builder-fn setter]]
                     `[~k (-> (~(symbol (str proto-class "/newBuilder")))
                              (~setter (~builder-fn (second m#)))
                              (.build))])
                   (partition 3 cases))
         :else (throw (ex-info "Unknown oneof variant" {:data m#}))))
     
     ;; Protobuf → Clojure
     (defn ~(symbol (str "parse-" name)) [proto#]
       (case (.getPayloadCase proto#)
         ~@(mapcat (fn [[k _ getter enum-val]]
                     `[~enum-val {~k (~(symbol (str "parse-" (name k))) 
                                      (~getter proto#))}])
                   (partition 4 cases))
         (throw (ex-info "Unknown payload case" 
                         {:case (.getPayloadCase proto#)}))))))

;; Usage for Root command
(defconverter-oneof root Root
  :day-camera build-day-camera .setDayCamera .getDayCamera DAY_CAMERA
  :heat-camera build-heat-camera .setHeatCamera .getHeatCamera HEAT_CAMERA
  :rotary build-rotary .setRotary .getRotary ROTARY
  :system build-system .setSystem .getSystem SYSTEM)

;; Roundtrip test helpers
(defn roundtrip-command [cmd]
  (-> cmd
      build-root
      .toByteArray
      Root/parseFrom
      parse-root))

(defn roundtrip-state [state]
  (-> state
      build-state
      .toByteArray
      JonGUIState/parseFrom
      parse-state))

;; Validation with both Malli and buf.validate
(defn validate-roundtrip [data spec build-fn parse-fn]
  (let [original data
        roundtripped (-> data build-fn .toByteArray parse-fn)]
    (and (m/validate spec original)
         (m/validate spec roundtripped)
         (= original roundtripped))))
```

### Spec Validation Strategy

**Spec Locations**:
- Generated specs: `src/potatoclient/specs/` (created by proto-explorer)
- Shared specs: `shared/specs/` (common specifications)
- UI specs: `src/potatoclient/ui_specs.cljc`

**Validation Approach**:
- Use generated specs from `shared/specs/protobuf/` directory
- Validate all commands before sending (pre-condition)
- Validate all state updates after receiving (post-condition)
- Performance mode: disable validation in release builds
- Use `malli.instrument` for development-time checking
- SKIP ALL VALIDATION IN RELEASE

## Testing Strategy

### Proto Conversion Tests
- **Roundtrip tests**: Every message type must survive Clojure→Proto→Clojure
- **Malli validation**: Pre/post conditions on all conversions
- **buf.validate compliance**: Generated proto must pass buf validation
- **Property-based testing**: Generate random valid data using Malli generators
- **Edge cases**: null values, empty collections, boundary values

### Unit Tests
- Proto conversion functions
- Message validation
- Rate limiting logic
- Reconnection logic

### Integration Tests
- Mock server testing
- Real server testing
- Performance benchmarks
- Error scenarios

### Generated Test Suite
```clojure
(ns potatoclient.proto.command-test
  (:require [clojure.test :refer :all]
            [malli.generator :as mg]
            [potatoclient.proto.command :as proto]
            [potatoclient.specs.command :as spec]))

;; Generative roundtrip testing
(deftest all-commands-roundtrip
  (doseq [cmd-type [:day-camera :heat-camera :rotary :system]]
    (testing (str "Roundtrip for " cmd-type)
      (checking (str cmd-type " commands") 100
        [cmd (mg/generator (spec/command-spec cmd-type))]
        (is (= cmd (proto/roundtrip-command cmd)))))))

;; buf.validate compliance
(deftest buf-validate-compliance
  (testing "Generated protobuf passes buf.validate"
    (let [cmd {:rotary {:goto-ndc {:x 0.5 :y -0.5 :channel :heat}}}
          proto-bytes (proto/command->bytes cmd)]
      ;; This would call buf CLI or Java validator
      (is (valid-according-to-buf? proto-bytes)))))
```

## Benefits of Refactoring

1. **Simplified Architecture**: No separate JVM processes
2. **Better Integration**: Direct access to Clojure state
3. **Improved Testing**: Easier to test without subprocess complexity
4. **Better Error Handling**: Unified error handling in Clojure
5. **Performance**: Reduced IPC overhead
6. **Maintainability**: All code in one language/process
7. **Debugging**: Easier debugging without cross-process boundaries
8. **No Technical Debt**: Clean implementation without backward compatibility

## Recent Progress Summary

### Completed Metadata-Driven Resolution System
1. **Proto Registry** (`proto_registry.clj`)
   - Centralized registry for all proto file metadata
   - Dynamic namespace resolution based on JSON descriptors
   - No more hardcoded patterns

2. **Naming Configuration** (`naming_config.clj`)
   - Configurable naming conventions with pattern rules
   - Flexible alias generation
   - Extensible without code changes

3. **Updated Type Resolution**
   - Uses registry for all lookups
   - Dynamic enum resolution
   - Metadata-driven approach throughout

4. **Fixed Dependency Graph**
   - Dynamic alias generation
   - No hardcoded "types" aliases
   - Uses registry metadata

5. **Comprehensive Tests**
   - Full test coverage for new systems
   - Validates all resolution paths
   - Tests custom configurations

## Next Steps

1. **Complete Proto Generator Setup**
   - Add custom oneof schema support
   - Parse remaining JSON descriptor fields
   - Design and implement code templates

2. **Implement Code Generation**
   - Build template system with rewrite-clj
   - Generate bidirectional converters
   - Add macro generation for patterns

3. **WebSocket Infrastructure**
   - Implement Hato WebSocket wrappers
   - Create core.async channel topology
   - Build reconnection logic

4. **Replace Subprocesses**
   - Command subprocess first
   - Then state subprocess
   - Full integration testing

5. **Cleanup and Documentation**
   - Remove all Kotlin code
   - Update build systems
   - Complete documentation

## Additional Resources

### External Documentation
- [Hato WebSocket Guide](https://github.com/gnarroway/hato#websockets)
- [rewrite-clj Tutorial](https://cljdoc.org/d/rewrite-clj/rewrite-clj/1.2.50/doc/readme)
- [Malli Guide](https://github.com/metosin/malli#readme)
- [core.async Walkthrough](https://github.com/clojure/core.async/wiki/walkthrough)
- [Protocol Buffers Java Tutorial](https://protobuf.dev/getting-started/javatutorial/)

### Internal Documentation with the old architecture
- Architecture Overview: `docs/architecture/system-overview.md`
- Transit Protocol: `docs/architecture/transit-protocol.md`
- Command System: `docs/architecture/command-system.md`
- Proto Explorer: `tools/proto-explorer/README.md`
- Mock Video Stream: `tools/mock-video-stream/README.md`

## Implementation Notes

### Proto-Clj-Generator Implementation

**Directory Structure**:
```
tools/proto-clj-generator/
├── src/
│   ├── generator/
│   │   ├── core.clj              # Main generator logic
│   │   ├── templates.clj         # rewrite-clj templates
│   │   ├── parser.clj            # JSON descriptor parsing
│   │   ├── validation.clj        # Malli AST validation
│   │   ├── proto_registry.clj    # ✅ DONE - Metadata registry
│   │   ├── naming_config.clj     # ✅ DONE - Naming conventions
│   │   └── type_resolution.clj   # ✅ UPDATED - Uses registry
│   └── main.clj                  # CLI entry point
├── test/
│   └── generator/
│       ├── proto_registry_test.clj # ✅ DONE - Registry tests
│       ├── roundtrip_test.clj      # Test generated code
│       └── templates_test.clj      # Test template generation
└── deps.edn                        # Dependencies
```

**Key Dependencies**:
```clojure
{:deps {rewrite-clj/rewrite-clj {:mvn/version "1.2.50"}
        org.clojure/core.match {:mvn/version "1.1.0"}
        metosin/malli {:mvn/version "0.16.4"}
        cheshire/cheshire {:mvn/version "5.13.0"}}}
```

**Generation Process**:
1. Load JSON descriptors into registry ✅
2. Parse into internal representation
3. Generate AST using rewrite-clj templates
4. Validate AST with Malli schemas
5. Write formatted Clojure code to output directory
6. Run tests on generated code

**Macro Generation Strategy**:
```clojure
;; Template for generating defbuilder macros
(defn generate-builder-macro [message-type]
  (let [fields (get-fields message-type)
        field-mappings (map field->mapping fields)]
    (z/of-string
      (str "(defbuilder build-" (->kebab (:name message-type))
           " " (:java-class message-type)
           " ::spec/" (->kebab (:name message-type))
           "\n"
           (str/join "\n" (map format-field-mapping field-mappings))
           ")"))))

;; Template for oneof case generation
(defn generate-oneof-matcher [oneof-name cases]
  (z/of-string
    (str "(match-oneof " (->pascal oneof-name) " m\n"
         (str/join "\n" 
           (map (fn [{:keys [field-name builder]}]
                  (str "  " field-name " " builder))
                cases))
         ")")))
```

### Tools to Keep vs Remove
- **Keep**: protogen (for non-Clojure languages)
- **Keep**: proto-explorer (for JSON descriptors and specs)
- **Keep**: mock-video-stream (still uses Transit for video subprocesses)
- **Create**: tools/proto-clj-generator (new)
- **Removed**: ~~tools/transit-test-generator~~ (already removed - was for Kotlin subprocess testing)
- **Remove**: Kotlin subprocesses (after migration)
- **Remove**: tools/proto-explorer/generate-kotlin-handlers.clj (no longer needed)

## Guiding Principles

1. **Clean Slate**: No backward compatibility needed - we're building new
2. **Metadata-Driven**: All configuration from data, not code
3. **Simple Over Complex**: Choose straightforward solutions
4. **Performance**: Direct conversions without intermediate formats
5. **Testability**: Everything must be testable in isolation
6. **No Technical Debt**: Build it right the first time