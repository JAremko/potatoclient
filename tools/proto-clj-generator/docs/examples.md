# Proto-CLJ Generator Examples

This document provides practical examples of using the generated protobuf bindings.

## Basic Command Building

### Simple Command

```clojure
(require '[potatoclient.proto.cmd :as cmd])

;; Build a ping command
(def ping-bytes
  (-> {:payload {:ping {}}}
      cmd/build-root
      .toByteArray))

;; Parse it back
(def parsed
  (-> ping-bytes
      (cmd.JonSharedCmd$Root/parseFrom)
      cmd/parse-root))
;; => {:protocol-version 0, :session-id 0, :important false, 
;;     :from-cv-subsystem false, :client-type :unknown, 
;;     :payload {:ping {}}}
```

### Command with Metadata

```clojure
;; Build command with all metadata fields
(def full-command
  {:protocol-version 1
   :session-id 12345
   :important true
   :from-cv-subsystem false
   :client-type :smartphone
   :payload {:noop {}}})

(def proto (cmd/build-root full-command))
```

## Complex Commands

### Rotary Platform Control

```clojure
(require '[potatoclient.proto.cmd :as cmd])

;; Move to normalized device coordinates
(def goto-ndc-cmd
  {:payload {:rotary {:goto-ndc {:x 0.5 :y -0.25 :channel :heat}}}})

;; Halt all movement
(def halt-cmd
  {:payload {:rotary {:halt {}}}})

;; Set platform azimuth
(def set-azimuth-cmd
  {:payload {:rotary {:set-platform-azimuth {:angle 45.0}}}})

;; Build and send
(-> goto-ndc-cmd cmd/build-root .toByteArray send-to-device!)
```

### Camera Control

```clojure
;; Day camera zoom
(def zoom-in-cmd
  {:payload {:day-camera {:zoom-in {}}}})

;; Heat camera calibration
(def calibrate-cmd
  {:payload {:heat-camera {:calibrate {}}}})

;; Set focus mode
(def focus-cmd
  {:payload {:day-camera {:set-focus-mode {:mode :auto}}}})
```

## Working with State Messages

```clojure
(require '[potatoclient.proto.ser :as ser])

;; Parse incoming state
(defn handle-state-update [^bytes state-bytes]
  (let [state (-> state-bytes
                  (ser.JonSharedData$JonGUIState/parseFrom)
                  ser/parse-jon-gui-state)]
    (println "Platform azimuth:" (-> state :rotary-platform :azimuth))
    (println "GPS latitude:" (-> state :gps :latitude))
    (println "Heat camera zoom:" (-> state :heat-camera :zoom-percentage))))
```

## Validation Examples

### Pre-validation

```clojure
(require '[malli.core :as m]
         '[malli.error :as me])

(defn validate-and-build [command-data]
  (if (m/validate cmd/root-spec command-data)
    (cmd/build-root command-data)
    (let [explanation (m/explain cmd/root-spec command-data)]
      (throw (ex-info "Invalid command"
                      {:errors (me/humanize explanation)
                       :data command-data})))))

;; Usage
(validate-and-build {:payload {:invalid "data"}})
;; => ExceptionInfo: Invalid command
;;    {:errors {:payload ["invalid type"]}}
```

### Custom Validation

```clojure
;; Business rule: important commands need session ID
(def important-command-spec
  [:and
   cmd/root-spec
   [:fn {:error/message "Important commands require session-id"}
    (fn [{:keys [important session-id]}]
      (or (not important)
          (and (some? session-id) (pos? session-id))))]])

(m/validate important-command-spec
            {:important true
             :payload {:ping {}}})
;; => false (missing session-id)
```

## Error Handling

### Graceful Parsing

```clojure
(defn safe-parse-command [^bytes data]
  (try
    (let [proto (cmd.JonSharedCmd$Root/parseFrom data)
          parsed (cmd/parse-root proto)]
      {:success true :command parsed})
    (catch com.google.protobuf.InvalidProtocolBufferException e
      {:success false :error :invalid-protobuf :message (.getMessage e)})
    (catch Exception e
      {:success false :error :unknown :message (.getMessage e)})))
```

### Validation Errors

```clojure
(defn command-with-error-details [data]
  (try
    (cmd/build-root data)
    (catch Exception e
      (if-let [data (ex-data e)]
        (do
          (println "Validation failed:")
          (println "  Schema:" (:schema data))
          (println "  Value:" (:value data))
          (println "  Errors:" (:errors data)))
        (throw e)))))
```

## Testing with Generated Code

### Property-Based Testing

```clojure
(require '[clojure.test.check.generators :as gen]
         '[clojure.test.check.properties :as prop]
         '[malli.generator :as mg])

(def prop-roundtrip
  (prop/for-all [cmd (mg/generator cmd/root-spec)]
    (= cmd (-> cmd
               cmd/build-root
               .toByteArray
               cmd.JonSharedCmd$Root/parseFrom
               cmd/parse-root))))

(tc/quick-check 100 prop-roundtrip)
```

### Unit Testing

```clojure
(deftest camera-commands-test
  (testing "Day camera zoom commands"
    (let [zoom-in {:payload {:day-camera {:zoom-in {}}}}
          proto (cmd/build-root zoom-in)]
      (is (instance? cmd.JonSharedCmd$Root proto))
      (is (.hasDayCamera proto))
      (is (.hasZoomIn (.getDayCamera proto)))))
  
  (testing "Heat camera calibration"
    (let [calibrate {:payload {:heat-camera {:calibrate {}}}}
          roundtrip (-> calibrate
                       cmd/build-root
                       cmd/parse-root)]
      (is (= calibrate (select-keys roundtrip [:payload]))))))
```

## Performance Tips

### Reuse Builders

```clojure
;; For high-frequency commands, consider builder reuse
(defn make-goto-ndc-builder []
  (let [root-builder (cmd.JonSharedCmd$Root/newBuilder)
        rotary-builder (cmd.RotaryPlatform.Root/newBuilder)
        goto-builder (cmd.RotaryPlatform.GotoNDC/newBuilder)]
    (fn [{:keys [x y channel]}]
      (-> goto-builder
          (.setX x)
          (.setY y)
          (.setChannel (get cmd.rotaryplatform/channel-values channel))
          (.build)
          (->> (.setGotoNdc rotary-builder))
          (.build)
          (->> (.setRotary root-builder))
          (.build)))))

(def fast-goto-ndc (make-goto-ndc-builder))
```

### Batch Operations

```clojure
;; Process multiple commands efficiently
(defn build-command-batch [commands]
  (mapv (fn [cmd-data]
          (try
            {:success true
             :bytes (-> cmd-data cmd/build-root .toByteArray)}
            (catch Exception e
              {:success false
               :error (.getMessage e)
               :data cmd-data})))
        commands))
```

## Integration Patterns

### With core.async

```clojure
(require '[clojure.core.async :as async])

(defn command-processor [in-chan out-chan]
  (async/go-loop []
    (when-let [cmd-data (async/<! in-chan)]
      (try
        (let [proto-bytes (-> cmd-data cmd/build-root .toByteArray)]
          (async/>! out-chan {:type :command :bytes proto-bytes}))
        (catch Exception e
          (async/>! out-chan {:type :error :message (.getMessage e)})))
      (recur))))
```

### With WebSocket

```clojure
(require '[hato.websocket :as ws])

(defn send-command [ws-conn command-data]
  (let [bytes (-> command-data cmd/build-root .toByteArray)]
    (ws/send! ws-conn bytes)))

(defn handle-state [data]
  (let [state (-> data ser.JonSharedData$JonGUIState/parseFrom ser/parse-jon-gui-state)]
    (update-ui! state)))
```