# Transit Quick Reference

## Common Tasks

### Sending a Command

```clojure
(require '[potatoclient.transit.commands :as cmd]
         '[potatoclient.transit.subprocess-launcher :as launcher])

;; Create command (returns a map)
(def command (cmd/rotary-goto {:azimuth 45.0 :elevation 30.0}))

;; Send to subprocess (if you have direct access)
(launcher/send-message! cmd-subprocess command)

;; Or use higher-level API (recommended)
(require '[potatoclient.api :as api])
(api/send-command! (cmd/ping))
```

### Reading State

```clojure
(require '[potatoclient.transit.app-db :as app-db])

;; Get complete state
(app-db/get-server-state)

;; Get specific subsystem
(app-db/get-subsystem-state :gps)
(app-db/get-subsystem-state :rotary)

;; Check connection
(app-db/connected?)
(app-db/get-connection-url)
```

### Starting/Stopping Subprocesses

```clojure
(require '[potatoclient.transit.subprocess-launcher :as launcher])

;; Start subprocesses
(def cmd-proc (launcher/start-command-subprocess "wss://server/cmd"))
(def state-proc (launcher/start-state-subprocess "wss://server/state"))

;; Send command
(launcher/send-message! cmd-proc (cmd/ping))

;; Stop subprocesses
(launcher/stop-subprocess! cmd-proc)
(launcher/stop-subprocess! state-proc)
```

### Working with App-DB

```clojure
(require '[potatoclient.transit.app-db :as app-db])

;; Update theme
(app-db/set-theme! :sol-dark)

;; Update locale
(app-db/set-locale! :ukrainian)

;; Check process status
(app-db/process-running? :cmd-proc)
(app-db/get-process-state :state-proc)

;; Add validation error
(app-db/add-validation-error! :malli :gps 
  [{:field "latitude" :error "out of range"}])

;; Update rate metrics
(app-db/update-rate-metrics! 29.5 false)  ; 29.5 Hz, not dropped
```

## Message Formats

### Command Message
```clojure
{:action "rotary-goto"
 :params {:azimuth 45.0
          :elevation 30.0}}
```

### State Message (from Kotlin)
```clojure
{:msg-type :state
 :msg-id "uuid-string"
 :timestamp 1234567890
 :payload {:system {:battery-level 85
                    :temperature 23.5}
           :gps {:latitude 40.7128
                 :longitude -74.0060}}}
```

### Control Message
```clojure
{:msg-type :control
 :msg-id "uuid-string"
 :timestamp 1234567890
 :payload {:action "set-rate-limit"
           :max-rate-hz 60}}
```

## Transit Utilities

### Creating Messages
```clojure
(require '[potatoclient.transit.core :as transit])

;; Create message with envelope
(transit/create-message :command {:action "ping"})
;; => {:msg-type :command
;;     :msg-id "uuid..."
;;     :timestamp 1234567890
;;     :payload {:action "ping"}}

;; Validate message
(transit/validate-message-envelope my-message)
;; => true/false
```

### Direct Transit Operations
```clojure
;; Create writer/reader
(def out (ByteArrayOutputStream.))
(def writer (transit/make-writer out))

;; Write message
(transit/write-message! writer {:test "data"} out)

;; Read message
(def in (ByteArrayInputStream. (.toByteArray out)))
(def reader (transit/make-reader in))
(transit/read-message reader)
```

## Debugging

### Enable Transit Debug Logging
```bash
export TRANSIT_DEBUG=true
```

### Check Message Flow
```clojure
;; Add app-db watcher
(app-db/add-watch-handler ::debug
  (fn [key ref old new]
    (println "State changed:" 
             (clojure.data/diff old new))))

;; Remove when done
(app-db/remove-watch-handler ::debug)
```

### Inspect Subprocess State
```clojure
;; Check if subprocess is alive
(launcher/subprocess-alive? cmd-proc)

;; Read message with timeout
(launcher/read-message state-proc 1000)  ; 1 second timeout
```

## Common Patterns

### Debounced Updates
StateSubprocess automatically debounces identical states. No action needed in Clojure.

### Rate Limited Commands
```clojure
;; Set rate limit via control message
(launcher/send-message! state-proc
  {:msg-type :control
   :msg-id (str (java.util.UUID/randomUUID))
   :timestamp (System/currentTimeMillis)
   :payload {:action "set-rate-limit"
             :max-rate-hz 30}})
```

### Error Recovery
Subprocesses automatically reconnect on WebSocket failure. Monitor via:
```clojure
(app-db/get-connection-state)
;; => {:connected? true :url "wss://..." :reconnect-count 2}
```

## Testing

### Mock Subprocess
```clojure
(defn mock-subprocess []
  {:subprocess-type :mock
   :output-chan (async/chan)
   :write-fn (fn [msg] (println "Mock received:" msg))
   :state (atom :running)})
```

### Test Commands
```clojure
(require '[clojure.test :refer [deftest is]])

(deftest test-command-creation
  (let [cmd (cmd/rotary-goto {:azimuth 90.0 :elevation 45.0})]
    (is (= "rotary-goto" (:action cmd)))
    (is (= 90.0 (get-in cmd [:params :azimuth])))))
```