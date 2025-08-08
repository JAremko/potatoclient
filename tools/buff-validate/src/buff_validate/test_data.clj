(ns buff-validate.test-data
  "Test data generation for buff-validate tests.
   Creates valid and invalid protobuf messages for testing."
  (:import
   [ser JonSharedData$JonGUIState JonSharedData$JonGUIState$Builder]
   [ser JonSharedDataTime$JonGuiDataTime JonSharedDataTime$JonGuiDataTime$Builder]
   [ser JonSharedDataSystem$JonGuiDataSystem JonSharedDataSystem$JonGuiDataSystem$Builder]
   [ser JonSharedDataGps$JonGuiDataGps JonSharedDataGps$JonGuiDataGps$Builder]
   [cmd JonSharedCmd$Root JonSharedCmd$Root$Builder]
   [cmd.System JonSharedCmdSystem$JonProtoCommandSystem JonSharedCmdSystem$JonProtoCommandSystem$Builder]
   [cmd.System JonSharedCmdSystem$JonProtoCommandSystem$Reboot JonSharedCmdSystem$JonProtoCommandSystem$Reboot$Builder]
   [com.google.protobuf ByteString]
   [java.io ByteArrayOutputStream]))

(defn create-valid-state-message
  "Create a valid state message with all required fields."
  []
  (-> (JonSharedData$JonGUIState/newBuilder)
      (.setTime (-> (JonSharedDataTime$JonGuiDataTime/newBuilder)
                    (.setMsSinceBoot 5000)
                    (.setClockOffsetObtained true)
                    (.setClockOffsetNs 123456)
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
                  (.setSpeed 0.0)
                  (.setCourse 0.0)
                  (.setSatellitesVisible 8)
                  (.setFixQuality 2)
                  (.build)))
      (.build)))

(defn create-invalid-state-message
  "Create a state message that will fail validation.
   This would need actual validation constraints in the proto files."
  []
  (-> (JonSharedData$JonGUIState/newBuilder)
      (.setTime (-> (JonSharedDataTime$JonGuiDataTime/newBuilder)
                    (.setMsSinceBoot -1000) ; Negative time - might be invalid
                    (.setClockOffsetObtained false)
                    (.build)))
      (.build)))

(defn create-valid-cmd-message
  "Create a valid command message."
  []
  (-> (JonSharedCmd$Frozen/newBuilder)
      (.setSystem (-> (JonSharedCmdSystem$JonProtoCommandSystem/newBuilder)
                     (.setReboot (-> (JonSharedCmdSystem$JonProtoCommandSystem$Reboot/newBuilder)
                                    (.setDelayMs 5000)
                                    (.build)))
                     (.build)))
      (.build)))

(defn create-invalid-cmd-message
  "Create a command message that should fail validation if constraints are defined."
  []
  (-> (JonSharedCmd$Frozen/newBuilder)
      (.setSystem (-> (JonSharedCmdSystem$JonProtoCommandSystem/newBuilder)
                     (.setReboot (-> (JonSharedCmdSystem$JonProtoCommandSystem$Reboot/newBuilder)
                                    (.setDelayMs -100) ; Negative delay - likely invalid
                                    (.build)))
                     (.build)))
      (.build)))

(defn message-to-bytes
  "Convert a protobuf message to byte array."
  [message]
  (.toByteArray message))

(defn create-corrupted-binary
  "Create corrupted binary data by modifying a valid message."
  [valid-message & {:keys [corruption-type] :or {corruption-type :random}}]
  (let [valid-bytes (.toByteArray valid-message)
        corrupted (byte-array valid-bytes)]
    (case corruption-type
      :random
      (do
        ;; Corrupt random bytes
        (dotimes [_ 5]
          (let [index (rand-int (count corrupted))]
            (aset-byte corrupted index (unchecked-byte (rand-int 256)))))
        corrupted)
      
      :truncate
      ;; Return truncated message
      (byte-array (take (/ (count valid-bytes) 2) valid-bytes))
      
      :header
      ;; Corrupt the protobuf header
      (do
        (aset-byte corrupted 0 (unchecked-byte 0xFF))
        (aset-byte corrupted 1 (unchecked-byte 0xFF))
        corrupted)
      
      :empty
      (byte-array 0)
      
      :garbage
      (byte-array (repeatedly 100 #(unchecked-byte (rand-int 256)))))))

(defn create-partial-message
  "Create a partial/incomplete protobuf message."
  []
  (let [full-message (create-valid-state-message)
        full-bytes (.toByteArray full-message)
        ;; Take only first 10 bytes - definitely incomplete
        partial-size (min 10 (count full-bytes))]
    (byte-array (take partial-size full-bytes))))

(defn create-empty-message
  "Create an empty protobuf message (minimal valid structure)."
  []
  (.toByteArray (JonSharedData$JonGUIState/newBuilder)
                (.build)))

(defn create-large-message
  "Create a large valid message for stress testing."
  []
  (let [builder (JonSharedData$JonGUIState/newBuilder)]
    ;; Add basic required fields
    (.setTime builder (-> (JonSharedDataTime$JonGuiDataTime/newBuilder)
                          (.setMsSinceBoot 5000)
                          (.build)))
    ;; Could add more repeated fields if the proto supports them
    (.build builder)))