(ns buff-validate.test-data
  "Test data generation utilities for buff-validate tests"
  (:import
   [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen]
   [ser JonSharedData$JonGUIState JonSharedDataTime$JonGuiDataTime JonSharedDataTypes$JonGuiDataClientType
        JonSharedDataSystem$JonGuiDataSystem JonSharedDataGps$JonGuiDataGps
        JonSharedDataCompass$JonGuiDataCompass JonSharedDataRotaryPlatform$JonGuiDataRotaryPlatform
        JonSharedDataCameraDay$JonGuiDataCameraDay JonSharedDataCameraHeat$JonGuiDataCameraHeat
        JonSharedDataGlassHeater$JonGuiDataGlassHeater JonSharedDataMeteoInternal$JonGuiDataMeteoInternal
        JonSharedDataLRF$JonGuiDataLRF JonSharedDataCompassCalibration$JonGuiDataCompassCalibration
        JonSharedDataRecOSD$JonGuiDataRecOSD JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime]
   [com.google.protobuf ByteString]))

;; ============================================================================
;; Valid Message Creation
;; ============================================================================

(defn create-valid-state-message
  "Create a valid state message with all required fields"
  []
  (-> (JonSharedData$JonGUIState/newBuilder)
      (.setProtocolVersion 1)
      (.setTime (-> (JonSharedDataTime$JonGuiDataTime/newBuilder)
                    (.setTimestamp 5000)
                    (.setManualTimestamp 123456)
                    (.setZoneId 0)
                    (.setUseManualTime false)
                    (.build)))
      (.setSystem (-> (JonSharedDataSystem$JonGuiDataSystem/newBuilder)
                      (.setCpuTemp 65.5)
                      (.setCpuLoad 45.2)
                      (.setMemoryUsed 1024000)
                      (.setMemoryTotal 2048000)
                      (.build)))
      (.setGps (-> (JonSharedDataGps$JonGuiDataGps/newBuilder)
                   (.setLatitude 37.7749)
                   (.setLongitude -122.4194)
                   (.setAltitude 50.0)
                   (.build)))
      (.setCompass (-> (JonSharedDataCompass$JonGuiDataCompass/newBuilder)
                       (.setHeading 180.0)
                       (.setPitch 0.0)
                       (.setRoll 0.0)
                       (.build)))
      (.setRotary (-> (JonSharedDataRotaryPlatform$JonGuiDataRotaryPlatform/newBuilder)
                      (.setAzimuth 0.0)
                      (.setElevation 0.0)
                      (.build)))
      (.setCameraDay (-> (JonSharedDataCameraDay$JonGuiDataCameraDay/newBuilder)
                         (.setZoom 1.0)
                         (.setFocus 100.0)
                         (.build)))
      (.setCameraHeat (-> (JonSharedDataCameraHeat$JonGuiDataCameraHeat/newBuilder)
                          (.setZoom 1.0)
                          (.setPalette 0)
                          (.build)))
      (.setDayCamGlassHeater (-> (JonSharedDataGlassHeater$JonGuiDataGlassHeater/newBuilder)
                                 (.setPowerOn false)
                                 (.setTemperature 20.0)
                                 (.build)))
      (.setMeteoInternal (-> (JonSharedDataMeteoInternal$JonGuiDataMeteoInternal/newBuilder)
                            (.setTemperature 25.0)
                            (.setPressure 1013.25)
                            (.setHumidity 50.0)
                            (.build)))
      (.setLrf (-> (JonSharedDataLRF$JonGuiDataLRF/newBuilder)
                   (.setDistance 0.0)
                   (.setMeasuring false)
                   (.build)))
      (.setCompassCalibration (-> (JonSharedDataCompassCalibration$JonGuiDataCompassCalibration/newBuilder)
                                  (.setCalibrated false)
                                  (.setProgress 0)
                                  (.build)))
      (.setRecOsd (-> (JonSharedDataRecOSD$JonGuiDataRecOSD/newBuilder)
                      (.setRecording false)
                      (.setOsdEnabled true)
                      (.build)))
      (.setActualSpaceTime (-> (JonSharedDataActualSpaceTime$JonGuiDataActualSpaceTime/newBuilder)
                               (.setUtcTimestampMs 1234567890000)
                               (.setTimeZoneOffsetMs 0)
                               (.build)))
      (.build)))

(defn create-valid-cmd-message
  "Create a valid command message"
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion 1)
      (.setSessionId 1000)
      (.setClientType JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
      (.setPing (JonSharedCmd$Ping/newBuilder))
      (.build)))

(defn create-invalid-state-message
  "Create a state message with invalid data (protocol_version = 0)"
  []
  (-> (JonSharedData$JonGUIState/newBuilder)
      (.setProtocolVersion 0)  ; Invalid: should be > 0
      (.build)))

(defn create-invalid-cmd-message
  "Create a command message with invalid data"
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion 0)  ; Invalid: should be > 0
      (.setSessionId 1000)
      (.setClientType JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
      (.setPing (JonSharedCmd$Ping/newBuilder))
      (.build)))

(defn create-large-message
  "Create a large state message for stress testing"
  []
  (let [builder (JonSharedData$JonGUIState/newBuilder)]
    (.setProtocolVersion builder 1)
    ;; Add time data
    (.setTime builder (-> (JonSharedDataTime$JonGuiDataTime/newBuilder)
                         (.setTimestamp 99999999)
                         (.setManualTimestamp 999999999)
                         (.setZoneId 5)
                         (.setUseManualTime true)
                         (.build)))
    (.build builder)))

;; ============================================================================
;; Binary Data Utilities
;; ============================================================================

(defn message-to-bytes
  "Convert a protobuf message to byte array"
  [message]
  (.toByteArray message))

(defn create-empty-message
  "Create an empty byte array"
  []
  (byte-array 0))

(defn create-partial-message
  "Create a partial/incomplete message"
  []
  (let [full-msg (create-valid-state-message)
        full-bytes (message-to-bytes full-msg)
        half-size (/ (count full-bytes) 2)]
    (byte-array (take half-size full-bytes))))

(defn create-corrupted-binary
  "Create corrupted binary data from a message"
  [message & {:keys [corruption-type] :or {corruption-type :random}}]
  (case corruption-type
    :random
    (if message
      (let [bytes (message-to-bytes message)
            corrupted (byte-array bytes)]
        ;; Corrupt first few bytes
        (aset-byte corrupted 0 (unchecked-byte 0xFF))
        (aset-byte corrupted 1 (unchecked-byte 0xFF))
        (when (> (count corrupted) 2)
          (aset-byte corrupted 2 (unchecked-byte 0xFF)))
        corrupted)
      (byte-array [0xFF 0xFF 0xFF]))
    
    :truncate
    (if message
      (let [bytes (message-to-bytes message)
            size (count bytes)
            truncate-size (max 1 (/ size 3))]
        (byte-array (take truncate-size bytes)))
      (byte-array [0x08]))
    
    :header
    (if message
      (let [bytes (message-to-bytes message)
            corrupted (byte-array bytes)]
        ;; Corrupt the protobuf wire format header
        (aset-byte corrupted 0 (unchecked-byte 0xFE))
        corrupted)
      (byte-array [0xFE 0xED]))
    
    :garbage
    ;; Random garbage data
    (byte-array (repeatedly 50 #(unchecked-byte (rand-int 256))))))

;; ============================================================================
;; Test Utilities
;; ============================================================================

(defn bytes-equal?
  "Check if two byte arrays are equal"
  [a b]
  (and (= (count a) (count b))
       (every? true? (map = (seq a) (seq b)))))

(defn create-test-messages
  "Create a collection of test messages for batch testing"
  []
  {:valid-state (create-valid-state-message)
   :valid-cmd (create-valid-cmd-message)
   :invalid-state (create-invalid-state-message)
   :invalid-cmd (create-invalid-cmd-message)})