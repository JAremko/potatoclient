# Subprocess Communication Implementation Plan

## Current State Analysis

### Video Streams (JSON Protocol)
```kotlin
// Current: VideoStreamManager sends JSON messages
messageProtocol.sendLog("INFO", "WebSocket connected")
messageProtocol.sendException("Error", exception)
messageProtocol.sendNavigationEvent(x, y, frameTimestamp)
```

```clojure
;; Parent receives and parses JSON
(defn- parse-json-message [line stream-id]
  (json/read-str line :key-fn keyword))
```

### Transit Subprocesses
```kotlin
// Current: Direct Transit communication
transitComm.sendMessage(mapOf("msg-type" to "response", ...))
```

```clojure
;; Parent uses Transit reader/writer
(transit-core/read-message reader)
(transit-core/write-message! writer message)
```

## Unified Architecture

### 1. Common Message Interface (Kotlin)

```kotlin
// SubprocessCommunicator.kt
interface SubprocessCommunicator {
    fun sendMessage(message: Map<String, Any>)
    fun sendLog(level: LogLevel, message: String, data: Map<String, Any> = emptyMap())
    fun sendError(context: String, error: Throwable, data: Map<String, Any> = emptyMap())
    fun sendMetric(metric: Metric)
    fun sendEvent(event: Event)
    fun sendStatus(status: ProcessStatus, details: Map<String, Any> = emptyMap())
    fun sendHealth(health: HealthReport)
}

// Implementations
class TransitCommunicator : SubprocessCommunicator { ... }
class JsonCommunicator : SubprocessCommunicator { ... }

// Unified message structure
data class SubprocessMessage(
    val msgType: String,
    val msgId: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val processId: String,
    val processType: String,
    val payload: Map<String, Any>
)
```

### 2. Parent Process Handler (Clojure)

```clojure
;; subprocess_manager.clj
(ns potatoclient.subprocess-manager
  (:require [potatoclient.transit.core :as transit]
            [clojure.data.json :as json]))

(defprotocol SubprocessProtocol
  (read-message [this])
  (write-message [this message]))

(defrecord TransitSubprocess [input output]
  SubprocessProtocol
  (read-message [_]
    (transit/read-message input))
  (write-message [_ message]
    (transit/write-message! output message)))

(defrecord JsonSubprocess [input output]
  SubprocessProtocol
  (read-message [_]
    (json/read-str (.readLine input) :key-fn keyword))
  (write-message [_ message]
    (json/write-str message output)))

;; Unified message dispatcher
(defmulti dispatch-subprocess-message
  (fn [subprocess-id message] (:msg-type message)))

(defmethod dispatch-subprocess-message "log"
  [subprocess-id {:keys [payload]}]
  (let [{:keys [level message data]} payload]
    (case level
      "ERROR" (logging/log-error {:subprocess subprocess-id
                                  :msg message
                                  :data data})
      "WARN"  (logging/log-warn {:subprocess subprocess-id
                                 :msg message
                                 :data data})
      "DEBUG" (when-not (runtime/release-build?)
                (logging/log-debug {:subprocess subprocess-id
                                    :msg message
                                    :data data}))
      (logging/log-info {:subprocess subprocess-id
                         :msg message
                         :data data}))))

(defmethod dispatch-subprocess-message "error"
  [subprocess-id {:keys [payload]}]
  (let [{:keys [context error stackTrace]} payload]
    (logging/log-error {:subprocess subprocess-id
                        :context context
                        :error error
                        :stackTrace stackTrace})
    ;; Notify UI about critical errors
    (when (critical-error? context)
      (ui/show-error-notification subprocess-id context error))))

(defmethod dispatch-subprocess-message "metric"
  [subprocess-id {:keys [payload]}]
  (metrics/record-subprocess-metric subprocess-id payload))

(defmethod dispatch-subprocess-message "health"
  [subprocess-id {:keys [payload]}]
  (health-monitor/update-health subprocess-id payload))
```

### 3. Health Monitoring System

```clojure
;; health_monitor.clj
(ns potatoclient.subprocess.health-monitor
  (:require [potatoclient.state :as state]))

(def health-check-interval-ms 30000)
(def unhealthy-threshold-ms 60000)

(defn start-health-monitoring! []
  (future
    (loop []
      (Thread/sleep health-check-interval-ms)
      (doseq [[subprocess-id subprocess] (state/all-subprocesses)]
        (let [last-health (state/get-subprocess-health subprocess-id)
              time-since-last (- (System/currentTimeMillis) 
                                (:timestamp last-health 0))]
          (when (> time-since-last unhealthy-threshold-ms)
            (state/set-subprocess-health! subprocess-id :unhealthy)
            (logging/log-warn {:msg "Subprocess unhealthy"
                               :subprocess subprocess-id
                               :last-seen-ms time-since-last}))))
      (recur))))

(defn update-health [subprocess-id health-data]
  (let [health-status (evaluate-health health-data)]
    (state/set-subprocess-health! subprocess-id health-status health-data)))

(defn evaluate-health [{:keys [cpu memory errors]}]
  (cond
    (> errors 10)     :error
    (> cpu 80)        :warning
    (> memory 500)    :warning  ; 500MB
    :else             :healthy))
```

### 4. Migration Strategy

#### Step 1: Add Unified Protocol to Transit Subprocesses
```kotlin
// Update CommandSubprocess.kt
class CommandSubprocess(wsUrl: String) {
    private val communicator = TransitCommunicator(System.`in`, System.out)
    private val logger = SubprocessLogger("command", communicator)
    
    fun run() {
        logger.info("Starting command subprocess")
        logger.metric(Metric("startup_time", System.currentTimeMillis()))
        // ... existing logic
    }
}
```

#### Step 2: Create Adapter for Video Streams
```kotlin
// VideoStreamAdapter.kt
class VideoStreamAdapter(private val jsonProtocol: MessageProtocol) : SubprocessCommunicator {
    override fun sendLog(level: LogLevel, message: String, data: Map<String, Any>) {
        when (level) {
            LogLevel.ERROR -> jsonProtocol.sendLog("ERROR", message)
            LogLevel.WARN -> jsonProtocol.sendLog("WARN", message)
            LogLevel.INFO -> jsonProtocol.sendLog("INFO", message)
            LogLevel.DEBUG -> if (!isReleaseBuild) jsonProtocol.sendLog("DEBUG", message)
        }
    }
    
    // Adapt other methods...
}
```

#### Step 3: Update Parent Process
```clojure
;; Update process.clj to handle both protocols
(defn create-subprocess [type command]
  (let [process (start-process command)
        protocol (if (#{:command :state} type)
                   (->TransitSubprocess (:input process) (:output process))
                   (->JsonSubprocess (:input process) (:output process)))]
    {:process process
     :protocol protocol
     :type type}))
```

### 5. UI Integration

```clojure
;; subprocess_ui.clj
(ns potatoclient.ui.subprocess-panel
  (:require [seesaw.core :as s]))

(defn create-subprocess-indicator [subprocess-id]
  (let [status-label (s/label :id (keyword (str subprocess-id "-status"))
                              :text "●"
                              :font {:size 16})]
    (update-subprocess-status! subprocess-id status-label)
    status-label))

(defn update-subprocess-status! [subprocess-id label]
  (add-watch (state/subprocess-health-atom subprocess-id) 
             ::ui-update
             (fn [_ _ _ health]
               (s/invoke-later
                 (s/config! label 
                            :foreground (case (:status health)
                                          :healthy :green
                                          :warning :yellow
                                          :error :red
                                          :offline :gray))))))
```

## Benefits of Unified Architecture

1. **Single Source of Truth**: All subprocess communication flows through one system
2. **Better Debugging**: Unified logging and metrics across all subprocesses
3. **Proactive Monitoring**: Health checks prevent silent failures
4. **Gradual Migration**: Can migrate one subprocess at a time
5. **Type Safety**: Transit provides better type preservation than JSON
6. **Performance**: Unified metrics enable optimization
7. **User Experience**: Consistent error handling and status reporting