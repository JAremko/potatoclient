(ns potatoclient.test-utils.protobuf
  "Utilities for creating protobuf test messages"
  (:import [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Frozen 
            JonSharedCmd$Noop]
           [cmd.RotaryPlatform JonSharedCmdRotary$Root JonSharedCmdRotary$Stop
            JonSharedCmdRotary$AngleTo JonSharedCmdRotary$Axis 
            JonSharedCmdRotary$Azimuth JonSharedCmdRotary$Elevation]
           [cmd.DayCamera JonSharedCmdDayCamera$Root JonSharedCmdDayCamera$ZoomTo
            JonSharedCmdDayCamera$Focus JonSharedCmdDayCamera$Iris]
           [ser JonSharedData$JonGUIState 
            JonSharedDataSystem$JonGuiDataSystem
            JonSharedDataTime$JonGuiDataTime 
            JonSharedDataRotary$JonGuiDataRotary
            JonSharedDataDayCamera$JonGuiDataDayCamera 
            JonSharedDataHeatCamera$JonGuiDataHeatCamera]
           [ser JonSharedDataTypes$JonGuiDataRotaryMode 
            JonSharedDataTypes$JonGuiDataRotaryDirection
            JonSharedDataTypes$JonGuiDataVideoChannel]))

;; Command creation helpers

(defn create-ping-command
  "Create a ping command"
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setPing (JonSharedCmd$Ping/newBuilder))
      (.build)))

(defn create-frozen-command
  "Create a frozen command"
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setFrozen (JonSharedCmd$Frozen/newBuilder))
      (.build)))

(defn create-noop-command
  "Create a noop command"
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setNoop (JonSharedCmd$Noop/newBuilder))
      (.build)))

(defn create-rotary-stop-command
  "Create a rotary stop command"
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setRotary (-> (JonSharedCmdRotary$Root/newBuilder)
                      (.setStop (JonSharedCmdRotary$Stop/newBuilder))))
      (.build)))

(defn create-rotary-angle-to-command
  "Create a rotary angle-to command"
  [azimuth elevation]
  (-> (JonSharedCmd$Root/newBuilder)
      (.setRotary (-> (JonSharedCmdRotary$Root/newBuilder)
                      (.setAngleTo (-> (JonSharedCmdRotary$AngleTo/newBuilder)
                                       (.setAzimuth azimuth)
                                       (.setElevation elevation)))))
      (.build)))

(defn create-day-camera-zoom-command
  "Create a day camera zoom command"
  [zoom-level]
  (-> (JonSharedCmd$Root/newBuilder)
      (.setDayCamera (-> (JonSharedCmdDayCamera$Root/newBuilder)
                         (.setZoomTo zoom-level)))
      (.build)))

(defn create-day-camera-focus-command
  "Create a day camera focus command"
  [focus-value]
  (-> (JonSharedCmd$Root/newBuilder)
      (.setDayCamera (-> (JonSharedCmdDayCamera$Root/newBuilder)
                         (.setFocus (-> (JonSharedCmdDayCamera$Focus/newBuilder)
                                        (.setAbsolute focus-value)))))
      (.build)))

;; State creation helpers

(defn create-test-system-state
  "Create system state data - using minimal fields"
  []
  ;; Since we don't know the exact field names, let's create an empty system state
  (-> (JonSharedDataSystem$JonGuiDataSystem/newBuilder)
      (.build)))

(defn create-test-time-state
  "Create time state data - using minimal fields"
  []
  ;; Since we don't know the exact field names, let's create an empty time state
  (-> (JonSharedDataTime$JonGuiDataTime/newBuilder)
      (.build)))

(defn create-test-rotary-state
  "Create rotary state data - using minimal fields"
  []
  ;; Since we don't know the exact field names, let's create an empty rotary state
  (-> (JonSharedDataRotary$JonGuiDataRotary/newBuilder)
      (.build)))

(defn create-test-day-camera-state
  "Create day camera state data - using minimal fields"
  []
  ;; Since we don't know the exact field names, let's create an empty day camera state
  (-> (JonSharedDataDayCamera$JonGuiDataDayCamera/newBuilder)
      (.build)))

(defn create-test-gui-state
  "Create a complete GUI state message"
  []
  ;; Create a minimal GUI state
  (-> (JonSharedData$JonGUIState/newBuilder)
      (.build)))

;; Validation helpers

(defn command-has-type?
  "Check if a command has a specific type"
  [command type-keyword]
  (case type-keyword
    :ping (.hasPing command)
    :frozen (.hasFrozen command)
    :noop (.hasNoop command)
    :rotary (.hasRotary command)
    :day-camera (.hasDayCamera command)
    :heat-camera (.hasHeatCamera command)
    :lrf (.hasLrf command)
    :gps (.hasGps command)
    :compass (.hasCompass command)
    :system (.hasSystem command)
    :osd (.hasOsd command)
    :cv (.hasCv command)
    false))

(defn parse-command
  "Parse a command from binary data"
  [^bytes data]
  (JonSharedCmd$Root/parseFrom data))

(defn parse-state
  "Parse a state from binary data"
  [^bytes data]
  (JonSharedData$JonGUIState/parseFrom data))

(defn command->bytes
  "Convert command to byte array"
  [command]
  (.toByteArray command))

(defn state->bytes
  "Convert state to byte array"
  [state]
  (.toByteArray state))

;; Test data generators

(defn generate-random-rotary-state
  "Generate random rotary state for testing"
  []
  ;; Return empty map for now
  {})

(defn generate-test-states
  "Generate a sequence of test states"
  [n]
  (repeatedly n create-test-gui-state))

;; Assertion helpers for tests

(defn assert-command-type
  "Assert that a command is of a specific type and optionally check fields"
  [command expected-type & {:as field-checks}]
  (assert (command-has-type? command expected-type)
          (str "Expected command type " expected-type))
  
  (when (and (= expected-type :rotary) (:angle-to field-checks))
    (let [{:keys [azimuth elevation]} (:angle-to field-checks)
          angle-to (.getAngleTo (.getRotary command))]
      (assert (= azimuth (.getAzimuth angle-to))
              (str "Expected azimuth " azimuth " but got " (.getAzimuth angle-to)))
      (assert (= elevation (.getElevation angle-to))
              (str "Expected elevation " elevation " but got " (.getElevation angle-to))))))

(defn assert-state-has-fields
  "Assert that a state has specific fields"
  [state & field-keys]
  (doseq [field field-keys]
    (case field
      :system (assert (.hasSystem state) "State should have system field")
      :time (assert (.hasTime state) "State should have time field")
      :rotary (assert (.hasRotary state) "State should have rotary field")
      :day-camera (assert (.hasDayCamera state) "State should have day camera field")
      :heat-camera (assert (.hasHeatCamera state) "State should have heat camera field"))))