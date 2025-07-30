# Transit-based Architecture Rework (Version 3)

## Executive Summary

This document describes a comprehensive rework of PotatoClient's command and state communication system using Transit with MessagePack format. The new architecture introduces two dedicated Kotlin subprocesses that completely isolate protobuf handling from Clojure code, following the successful pattern established by the video stream processors but with Transit/MessagePack for superior performance and type safety.

## Motivation and Goals

### Current Pain Points
1. **State Fragmentation**: 13+ separate atoms requiring complex synchronization
2. **Protobuf Coupling**: Direct dependency on Java protobuf classes in Clojure
3. **Build Complexity**: Protobuf generation affects compilation and classpath
4. **Type Safety**: Manual case conversion and validation
5. **Testing Difficulty**: Cannot test without full protobuf setup
6. **Coverage Generation**: Doesn't work with runtime generation of proto bindings

### Design Goals
1. **Single Source of Truth**: One atom following re-frame's proven app-db pattern
2. **Complete Isolation**: Clojure never touches protobuf classes
3. **Streaming Performance**: Efficient binary protocol with minimal overhead
4. **Type Safety**: Transit's built-in type preservation
5. **Testability**: Mock-friendly architecture
6. **Smart Rate Limiting**: Client-controlled state update rates with debouncing

## Architecture Overview

### Enhanced Core Design with Bidirectional Communication

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           Clojure Main Process                           │
│                                                                          │
│  ┌─────────────────┐                                                    │
│  │   Single State   │     Transit/MessagePack Bidirectional             │
│  │      Atom        │     ════════════════════════════════              │
│  │                  │                                                    │
│  │  {:server-state  │     State Updates (stdout) ◄────┐                 │
│  │   :app-state     │     Control/Config (stdin) ────►│                 │
│  │   :validation    │                                 │                 │
│  │   :rate-limits}  │                                 ▼                 │
│  └─────────────────┘     ┌──────────────────────────────────────┐      │
│                          │      State Subprocess (Kotlin)        │      │
│                          │                                       │      │
│                          │  • WebSocket Client (State Stream)   │      │
│                          │  • Protobuf → Transit Converter      │      │
│                          │  • Debouncing (proto.equals())       │  ◄── Rate limiting happens HERE
│                          │  • Rate Limiting (configurable)      │  ◄── NOT in Clojure!
│                          │  • buf.validate Validation           │  ◄── Runs in ALL modes except release
│                          │  • Reconnection Logic                │      │
│                          └──────────────────────────────────────┘      │
│                                           │                             │
│                                           │ WebSocket                   │
│                                           ▼ (State)                     │
│                          ┌──────────────────────────────────────┐      │
│                          │    Server (State & Commands)          │      │
│  Transit/MP Bidirectional│                                       │      │
│  ════════════════════    └──────────────────────────────────────┘      │
│                                           ▲                             │
│  Commands (stdin) ────────┐               │ WebSocket                   │
│  Responses (stdout) ◄─────┘               │ (Commands)                  │
│                          ▼                │                             │
│                          ┌──────────────────────────────────────┐      │
│                          │     Cmd Subprocess (Kotlin)           │      │
│                          │                                       │      │
│                          │  • Transit → Protobuf Builder        │      │
│                          │  • WebSocket Client (Cmd Stream)     │      │
│                          │  • buf.validate Validation           │  ◄── Runs in ALL modes except release
│                          │  • Command Acknowledgments           │      │
│                          │  • Reconnection Logic                │      │
│                          └──────────────────────────────────────┘      │
│                                                                          │
│         JSON (existing)                                                  │
│         ═══════════════                                                  │
│              ▲                                                           │
│              │                                                           │
│  ┌───────────┴──────────────┬──────────────────────┐                   │
│  │   Heat Video Process     │   Day Video Process   │                   │
│  │      (Kotlin)            │      (Kotlin)         │                   │
│  └──────────────────────────┴──────────────────────┘                   │
└─────────────────────────────────────────────────────────────────────────┘
```

### Single Atom Structure with Validation and Rate Control

```clojure
(def app-db  ;; Following re-frame naming convention
  (atom {:server-state {:system {:battery-level 85
                                 :localization "en"
                                 :recording false}
                        :lrf {:distance 142.5
                              :scan-mode "single"}
                        :gps {:latitude 51.5074
                              :longitude -0.1278
                              :altitude 35.0
                              :fix-type "3D"
                              :use-manual false}
                        :compass {:heading 275.5
                                  :unit "degrees"}
                        :rotary {:azimuth 45.0
                                 :elevation 30.0
                                 :moving false}
                        ;; ... all other subsystems
                        }
         :app-state {:connection {:url "wss://sych.local"
                                  :connected? true
                                  :latency-ms 23}
                     :ui {:theme :sol-dark
                          :locale :en
                          :read-only-mode? false}
                     :processes {:state-proc {:pid 12345
                                              :status :running}
                                 :cmd-proc {:pid 12346
                                            :status :running}
                                 :heat-video {:pid 12347
                                              :status :running}
                                 :day-video {:pid 12348
                                             :status :running}}}
         :validation {:enabled? true  ;; false only in release mode (both Malli and buf.validate)
                      :errors []      ;; validation errors from both Kotlin and Clojure
                      :stats {:total-validations 0
                              :failed-validations 0}}
         :rate-limits {:max-rate-hz 30     ;; max 30 updates per second
                       :current-rate 0      ;; actual rate
                       :dropped-updates 0}}))  ;; stats
```

## Guardrails Requirements

### Mandatory Function Specifications

**CRITICAL**: ALL Clojure functions in this architecture MUST use Guardrails (`>defn` or `>defn-`). This is non-negotiable and applies to:

1. **Public Functions**: Always use `>defn` with precise domain specs
2. **Private Functions**: Always use `>defn-` (never `defn-` or `defn ^:private`)
3. **Helper Functions**: Even small utilities must be spec'd
4. **Multimethod Implementations**: Wrap in spec'd functions

### Example Transit Implementation with Guardrails

```clojure
(ns potatoclient.transit.core
  (:require [cognitect.transit :as transit]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]])
  (:import [java.io OutputStream InputStream]
           [com.cognitect.transit Writer Reader]))

;; ALL functions must have specs - no exceptions
(>defn make-writer
  "Create a Transit MessagePack writer with custom handlers"
  [out]
  [[:fn {:error/message "must be an OutputStream"}
    #(instance? OutputStream %)] 
   => [:fn {:error/message "must be a Transit Writer"}
       #(instance? Writer %)]]
  (transit/writer out :msgpack {:handlers write-handlers}))

(>defn make-reader
  "Create a Transit MessagePack reader with custom handlers"
  [in]
  [[:fn {:error/message "must be an InputStream"}
    #(instance? InputStream %)] 
   => [:fn {:error/message "must be a Transit Reader"}
       #(instance? Reader %)]]
  (transit/reader in :msgpack {:handlers read-handlers}))

(>defn write-message!
  "Write a message to Transit writer and flush the stream"
  [writer msg]
  [[:fn {:error/message "must be a Transit Writer"}
    #(instance? Writer %)] 
   any? => nil?]
  (transit/write writer msg)
  (.flush ^OutputStream (.getOutputStream writer))
  nil)

(>defn read-message
  "Read a single message from Transit reader"
  [reader]
  [[:fn {:error/message "must be a Transit Reader"}
    #(instance? Reader %)] 
   => any?]
  (transit/read reader))

;; Even simple functions need specs
(>defn- validate-message-envelope
  "Validate message has required envelope fields"
  [msg]
  [map? => boolean?]
  (and (contains? msg :msg-type)
       (contains? msg :msg-id)
       (contains? msg :timestamp)
       (contains? msg :payload)))
```

## Enhanced Message Envelope Design

```clojure
;; Bidirectional message types
{:msg-type :state        ;; State update from Kotlin
 :msg-id (uuid)          ;; For tracking/debugging
 :timestamp (System/currentTimeMillis)
 :payload {...}}

;; Control messages (Main → Subprocess)
{:msg-type :control
 :msg-id (uuid)
 :timestamp 1706234567892
 :payload {:action :set-rate-limit
           :max-hz 60}}

{:msg-type :control
 :msg-id (uuid)
 :timestamp 1706234567893
 :payload {:action :get-logs
           :lines 100}}

;; Response messages (Subprocess → Main)
{:msg-type :response
 :msg-id (uuid)
 :timestamp 1706234567894
 :payload {:status :ok
           :data [...logs...]}}

;; Validation error messages (protobuf details translated to simple data)
{:msg-type :validation-error
 :msg-id (uuid)
 :timestamp 1706234567895
 :payload {:source :buf-validate  ;; or :malli
           :subsystem :gps        ;; Kotlin translates proto field to keyword
           :errors [{:field "latitude"
                     :constraint "gte: -90.0, lte: 90.0"
                     :value -91.5}]}}  ;; No protobuf types exposed
```

## Debouncing and Rate Limiting Implementation

**IMPORTANT**: Rate limiting and debouncing are implemented ONLY in the Kotlin state subprocess, NOT in the Clojure main process. This design prevents overwhelming the main application with state updates while ensuring commands are sent immediately without delay.

### Where Rate Limiting Happens:
- **State Subprocess (Kotlin)**: Implements both debouncing and rate limiting before sending state updates to Clojure
- **Command Subprocess (Kotlin)**: NO rate limiting - commands are sent immediately
- **Clojure Main Process**: Only receives already rate-limited state updates, no additional limiting needed

### Why Rate Limit in Kotlin?
1. **CPU Efficiency**: Prevents unnecessary protobuf parsing and Transit encoding for duplicate states
2. **Memory Efficiency**: Reduces IPC message traffic between processes
3. **Simplicity**: Clojure receives clean, rate-limited updates without additional complexity
4. **Performance**: Leverages protobuf's native equals() method for efficient comparison

### Kotlin State Subprocess with Smart Debouncing

```kotlin
class StateSubprocess(
    private val wsUrl: String,
    private val transitComm: TransitCommunicator
) {
    private val wsClient = WebSocketClient(wsUrl)
    private val stateConverter = ProtobufToTransitConverter()
    private val validator = ProtoValidator.newValidator()
    
    // Rate limiting configuration
    @Volatile
    private var maxRateHz = 30
    private val rateLimiter = RateLimiter(maxRateHz)
    
    // Debouncing with proto equals
    private var lastSentProto: JonGUIState? = null
    private var lastSentHash: Int? = null
    
    suspend fun run() = coroutineScope {
        // WebSocket → Transit pipeline with debouncing
        launch {
            wsClient.stateFlow
                .filter { protoBytes ->
                    val stateProto = JonGUIState.parseFrom(protoBytes)
                    shouldSendUpdate(stateProto)
                }
                .collect { protoBytes ->
                    val stateProto = JonGUIState.parseFrom(protoBytes)
                    
                    // Validate in all modes except release
                    if (!isReleaseBuild()) {
                        validateAndReport(stateProto)
                    }
                    
                    val transitMap = stateConverter.convert(stateProto)
                    transitComm.sendMessage(mapOf(
                        "msg-type" to "state",
                        "msg-id" to UUID.randomUUID().toString(),
                        "timestamp" to System.currentTimeMillis(),
                        "payload" to transitMap
                    ))
                    
                    // Update last sent for debouncing
                    lastSentProto = stateProto
                    lastSentHash = stateProto.hashCode()
                }
        }
        
        // Handle control messages
        launch {
            while (isActive) {
                val msg = transitComm.readMessage() as? Map<*, *>
                when (msg?.get("msg-type")) {
                    "control" -> handleControl(msg)
                    "query" -> handleQuery(msg)
                }
            }
        }
    }
    
    private fun shouldSendUpdate(newProto: JonGUIState): Boolean {
        // First update always goes through
        if (lastSentProto == null) return true
        
        // Use protobuf's built-in equals() for deep comparison
        if (newProto == lastSentProto) return false
        
        // Additional hash check for performance
        if (newProto.hashCode() == lastSentHash) {
            // Hash collision, do full equals check
            if (newProto.equals(lastSentProto)) return false
        }
        
        // Apply rate limiting
        return rateLimiter.tryAcquire()
    }
    
    private suspend fun validateAndReport(proto: JonGUIState) {
        try {
            val result = validator.validate(proto)
            if (!result.isSuccess) {
                val errors = result.violations.map { violation ->
                    mapOf(
                        "field" to violation.fieldPath,
                        "constraint" to violation.constraintId,
                        "message" to violation.message
                    )
                }
                
                transitComm.sendMessage(mapOf(
                    "msg-type" to "validation-error",
                    "msg-id" to UUID.randomUUID().toString(),
                    "timestamp" to System.currentTimeMillis(),
                    "payload" to mapOf(
                        "source" to "buf-validate",
                        "errors" to errors
                    )
                ))
            }
        } catch (e: Exception) {
            // Log validation framework errors
        }
    }
    
    private fun handleControl(msg: Map<*, *>) {
        val payload = msg["payload"] as Map<*, *>
        when (payload["action"]) {
            "set-rate-limit" -> {
                maxRateHz = (payload["max-hz"] as Number).toInt()
                rateLimiter.updateRate(maxRateHz)
            }
            "get-logs" -> sendLogs(payload["lines"] as Int)
            "shutdown" -> gracefulShutdown()
        }
    }
}

// Rate limiter with configurable rate
class RateLimiter(initialRateHz: Int) {
    private val semaphore = Semaphore(initialRateHz)
    private val refillJob = CoroutineScope(Dispatchers.Default).launch {
        while (isActive) {
            delay(1000 / initialRateHz)
            semaphore.tryAcquire()
            semaphore.release()
        }
    }
    
    fun tryAcquire(): Boolean = semaphore.tryAcquire()
    
    fun updateRate(newRateHz: Int) {
        refillJob.cancel()
        // Restart with new rate
    }
}
```

### Command Subprocess (No Rate Limiting)

```kotlin
class CommandSubprocess(
    private val wsUrl: String,
    private val transitComm: TransitCommunicator
) {
    private val wsClient = WebSocketClient(wsUrl)
    private val cmdBuilder = TransitToProtobufBuilder()
    private val validator = ProtoValidator.newValidator()
    
    suspend fun run() = coroutineScope {
        // Transit → WebSocket pipeline (no rate limiting)
        launch {
            while (isActive) {
                val msg = transitComm.readMessage() as? Map<*, *>
                if (msg?.get("msg-type") == "command") {
                    val payload = msg["payload"] as Map<*, *>
                    
                    try {
                        val protoCmd = cmdBuilder.buildCommand(payload)
                        
                        // Validate in all modes except release
                        if (!isReleaseBuild()) {
                            val result = validator.validate(protoCmd)
                            if (!result.isSuccess) {
                                sendValidationError(result, payload)
                                continue
                            }
                        }
                        
                        // Send immediately - no rate limiting for commands
                        wsClient.sendCommand(protoCmd.toByteArray())
                        
                        // Send acknowledgment
                        transitComm.sendMessage(mapOf(
                            "msg-type" to "response",
                            "msg-id" to msg["msg-id"],
                            "timestamp" to System.currentTimeMillis(),
                            "payload" to mapOf(
                                "status" to "sent",
                                "command" to payload["action"]
                            )
                        ))
                    } catch (e: Exception) {
                        sendError(msg["msg-id"] as String, e)
                    }
                }
            }
        }
    }
}
```

## Comprehensive Validation Strategy

### Malli Schemas for Complete State

```clojure
(ns potatoclient.specs.app-db
  (:require [malli.core :as m]
            [potatoclient.specs :as specs]))

;; Complete app-db schema
(def app-db-schema
  [:map
   [:server-state
    [:map
     [:system {:optional true} specs/system-schema]
     [:lrf {:optional true} specs/lrf-schema]
     [:gps {:optional true} specs/gps-schema]
     [:compass {:optional true} specs/compass-schema]
     [:rotary {:optional true} specs/rotary-schema]
     [:camera-day {:optional true} specs/camera-day-schema]
     [:camera-heat {:optional true} specs/camera-heat-schema]
     ;; ... all subsystems
     ]]
   [:app-state
    [:map
     [:connection
      [:map
       [:url :string]
       [:connected? :boolean]
       [:latency-ms [:maybe :int]]]]
     [:ui
      [:map
       [:theme specs/theme]
       [:locale specs/locale]
       [:read-only-mode? :boolean]]]
     [:processes
      [:map-of :keyword
       [:map
        [:pid :int]
        [:status [:enum :running :stopped :error]]]]]]]
   [:validation
    [:map
     [:enabled? :boolean]
     [:errors [:vector specs/validation-error]]
     [:stats
      [:map
       [:total-validations :int]
       [:failed-validations :int]]]]]
   [:rate-limits
    [:map
     [:max-rate-hz [:int {:min 1 :max 120}]]
     [:current-rate :double]
     [:dropped-updates :int]]]])

;; Validation enabled in all modes except release (same as Kotlin)
(def validation-enabled?
  (not (runtime/release-build?)))

;; Guardrails function with comprehensive validation
(>defn update-app-db!
  "Update app-db with validation"
  [update-fn & args]
  [fn? [:* any?] => [:fn {:error/fn #(m/validate app-db-schema %)}
                     #(m/validate app-db-schema %)]]
  (let [new-state (apply swap! app-db update-fn args)]
    ;; Malli validation runs in all modes except release
    (when validation-enabled?
      (when-not (m/validate app-db-schema new-state)
        (log/error "Invalid app-db state after update"
                   {:errors (m/explain app-db-schema new-state)})))
    new-state))
```

### Bidirectional Control Interface

```clojure
(ns potatoclient.subprocess.control
  (:require [potatoclient.transit.core :as transit]
            [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]]))

;; ALL functions must use Guardrails - no exceptions
(>defn set-state-rate-limit!
  "Set maximum state update rate in Hz"
  [hz]
  [[:int {:min 1 :max 120}] => nil?]
  (send-control-message! :state-proc
                         {:action :set-rate-limit
                          :max-hz hz}))

(>defn get-subprocess-logs
  "Get recent logs from subprocess"
  [process-key lines]
  [[:enum :state-proc :cmd-proc :heat-video :day-video] 
   [:int {:min 1 :max 10000}] 
   => ::specs/future-instance]
  (send-query-message! process-key
                       {:action :get-logs
                        :lines lines}))

(>defn enable-validation!
  "Enable/disable validation in subprocess"
  [process-key enabled?]
  [[:enum :state-proc :cmd-proc] boolean? => nil?]
  (send-control-message! process-key
                         {:action :set-validation
                          :enabled enabled?}))

;; Handle responses from subprocesses - even multimethods need specs
(defmulti handle-subprocess-response :msg-type)

(>defn- handle-validation-error
  "Process validation error from subprocess"
  [{:keys [payload]}]
  [[:map [:payload map?]] => nil?]
  (swap! app-db update-in [:validation :errors] conj payload)
  (swap! app-db update-in [:validation :stats :failed-validations] inc))

(defmethod handle-subprocess-response :validation-error
  [msg]
  (handle-validation-error msg))

(>defn- handle-response
  "Process general response from subprocess"
  [{:keys [payload msg-id]}]
  [[:map [:payload any?] [:msg-id string?]] => boolean?]
  (deliver (get @pending-responses msg-id) payload))

(defmethod handle-subprocess-response :response
  [msg]
  (handle-response msg))
```

## WebSocket Reconnection Logic (Kotlin Side)

```kotlin
class ReconnectingWebSocketClient(
    private val url: String,
    private val reconnectDelayMs: Long = 1000,
    private val maxReconnectDelayMs: Long = 30000
) {
    private var currentDelay = reconnectDelayMs
    private var wsClient: WebSocketClient? = null
    
    suspend fun connect() = coroutineScope {
        while (isActive) {
            try {
                wsClient = WebSocketClient.newBuilder()
                    .uri(URI.create(url))
                    .connectTimeout(Duration.ofSeconds(10))
                    .build()
                
                val listener = StateWebSocketListener()
                wsClient.connectAsync(listener).await()
                
                // Reset delay on successful connection
                currentDelay = reconnectDelayMs
                
                // Wait for disconnection
                listener.awaitDisconnection()
                
            } catch (e: Exception) {
                log.error("WebSocket connection failed", e)
                
                // Exponential backoff
                delay(currentDelay)
                currentDelay = min(currentDelay * 2, maxReconnectDelayMs)
            }
        }
    }
}
```

## Performance Metrics and Monitoring

```clojure
;; ALL functions must use Guardrails with precise specs
(>defn rate-limit-metrics
  "Track rate limiting effectiveness"
  []
  [=> [:map
       [:configured-rate [:int {:min 1 :max 120}]]
       [:actual-rate :double]
       [:efficiency [:double {:min 0.0 :max 1.0}]]
       [:dropped-pct [:double {:min 0.0 :max 1.0}]]]]
  (let [state @app-db
        limits (get state :rate-limits)]
    {:configured-rate (:max-rate-hz limits)
     :actual-rate (:current-rate limits)
     :efficiency (/ (:current-rate limits) (:max-rate-hz limits))
     :dropped-pct (/ (:dropped-updates limits)
                     (+ (:current-rate limits) (:dropped-updates limits)))}))

;; Monitor validation performance with Guardrails
(>defn validation-metrics
  "Get current validation performance metrics"
  []
  [=> [:map
       [:failure-rate [:double {:min 0.0 :max 1.0}]]
       [:total-validations :int]
       [:current-errors :int]]]
  (let [validation (get-in @app-db [:validation :stats])]
    {:failure-rate (/ (:failed-validations validation)
                      (max 1 (:total-validations validation)))
     :total-validations (:total-validations validation)
     :current-errors (count (get-in @app-db [:validation :errors]))}))
```

## Testing Strategy with Validation

### Unit Tests with Comprehensive Validation

```clojure
(deftest app-db-schema-test
  (testing "Valid complete state"
    (is (m/validate app-db-schema valid-complete-state)))
  
  (testing "Invalid state detection"
    (let [invalid-state (assoc-in valid-state [:server-state :gps :latitude] 200)]
      (is (not (m/validate app-db-schema invalid-state)))
      (is (= 1 (count (m/explain app-db-schema invalid-state)))))))

(deftest rate-limiting-test
  (testing "Rate limiter respects configured limit"
    (let [limiter (make-rate-limiter 10)] ;; 10 Hz
      ;; Should allow 10 in first second
      (is (every? true? (repeatedly 10 #(.tryAcquire limiter))))
      ;; 11th should fail
      (is (not (.tryAcquire limiter))))))
```

## Complete Protobuf Isolation

**CRITICAL**: The Clojure main process has ZERO knowledge of protobuf specifics:
- No protobuf classes in classpath
- No protobuf dependencies
- No protobuf field names or types
- Only sees clean Transit/EDN data structures

All protobuf handling is completely isolated in the Kotlin subprocesses:
- State subprocess: Converts protobuf state → Transit maps
- Command subprocess: Converts Transit maps → protobuf commands
- Validation errors: Translated to simple Transit messages

## Benefits Summary

1. **Simplicity**: One atom, one truth - following proven re-frame pattern
2. **Complete Isolation**: Clojure never touches protobuf classes
3. **Smart Updates**: Debouncing via protobuf equals() prevents redundant updates
4. **Flexible Rate Control**: Client-controlled update rates for optimal performance
5. **Comprehensive Validation**: 
   - Kotlin: buf.validate for protobuf structures (all modes except release)
   - Clojure: Malli for app-db and Transit messages (all modes except release)
6. **Bidirectional Control**: Full subprocess management like video streams
7. **Zero Overhead Commands**: Commands sent immediately without rate limiting
8. **Built-in Reconnection**: Robust WebSocket handling with exponential backoff

## Implementation Timeline

### Week 1: Core Infrastructure
- Transit communication setup
- Subprocess management framework
- Basic message routing

### Week 2: Smart Features
- Debouncing with protobuf equals()
- Rate limiting implementation
- Bidirectional control messages

### Week 3: Validation Layer
- buf.validate integration
- Malli schema for app-db
- Validation error reporting

### Week 4: Production Readiness
- Reconnection logic
- Performance monitoring
- Comprehensive testing

## Key Development Requirements

### Guardrails Enforcement

1. **100% Function Coverage**: Every single function must use `>defn` or `>defn-`
2. **Precise Specs**: Use domain-specific specs, not generic types like `any?`
3. **Private Functions**: Use `>defn-` instead of `defn-` or `^:private`
4. **No Exceptions**: Even trivial helper functions must be spec'd

### Validation Strategy

1. **Kotlin Subprocesses (State & Command)**: 
   - buf.validate runs in ALL modes except release
   - Validates both incoming state from server and outgoing commands
   - Sends validation errors to Clojure via Transit messages
   
2. **Clojure Main Process**:
   - Malli validation runs in ALL modes except release
   - Validates app-db structure and Transit message envelopes
   
3. **Release Mode**: All validation disabled for performance
4. **Error Reporting**: All validation errors flow through Transit messages

### Example Function Pattern

```clojure
;; WRONG - Never use plain defn
(defn process-state [state]
  (update state :counter inc))

;; CORRECT - Always use Guardrails
(>defn process-state
  "Process incoming state update"
  [state]
  [::specs/app-state => ::specs/app-state]
  (update state :counter inc))
```

## Implementation Approach

**No backward compatibility or migration layers** - Clean rewrite approach:

1. **Phase 1**: Implement Transit core and app-db ✅
2. **Phase 2**: Rewrite state management to use app-db directly ✅  
3. **Phase 3**: Create Kotlin subprocesses with full functionality ✅
4. **Phase 4**: Replace WebSocket code to use Transit subprocesses
5. **Phase 5**: Remove all protobuf dependencies from Clojure

### Key Decision: Clean Break
- No migration layers or compatibility shims
- Direct rewrite of state management
- All old state atoms removed
- Clear separation between old and new code

### Packaging Strategy

**Single JAR Architecture**: Following the proven pattern of the video stream subprocesses:
- All Kotlin Transit classes compile into the main application JAR
- Subprocesses launched using the same classpath with different main classes
- No separate JAR files or complex deployment
- Reuses existing `potatoclient.process` infrastructure
- Simplifies distribution and AppImage packaging

This matches how `VideoStreamManager` currently works:
```clojure
;; Existing video stream launch
[java-exe "-cp" classpath "potatoclient.kotlin.VideoStreamManager" ...]

;; New Transit subprocess launch
[java-exe "-cp" classpath "potatoclient.kotlin.transit.CommandSubprocess" ...]
[java-exe "-cp" classpath "potatoclient.kotlin.transit.StateSubprocess" ...]
```

## Conclusion

This architecture provides a robust, high-performance solution that completely isolates protobuf handling while adding intelligent features like debouncing, rate limiting, and comprehensive validation. The bidirectional communication pattern matches our proven video stream architecture, ensuring consistency across the application.

The mandatory use of Guardrails across all functions ensures type safety, better documentation, and catches errors during development while having zero runtime overhead in production builds.