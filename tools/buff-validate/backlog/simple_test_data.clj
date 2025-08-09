(ns buff-validate.simple-test-data
  "Simple test data generators for buff-validate tests"
  (:import
   [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen]
   [ser JonSharedData$JonGUIState JonSharedDataTypes$JonGuiDataClientType]))

;; ============================================================================
;; Simple Command Messages
;; ============================================================================

(defn create-ping-cmd
  "Create a simple Ping command"
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion 1)
      (.setSessionId 1000)
      (.setClientType JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
      (.setPing (JonSharedCmd$Ping/newBuilder))
      (.build)))

(defn create-noop-cmd
  "Create a simple Noop command"
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion 1)
      (.setSessionId 2000)
      (.setClientType JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
      (.setNoop (JonSharedCmd$Noop/newBuilder))
      (.build)))

(defn create-frozen-cmd
  "Create a simple Frozen command"
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion 1)
      (.setSessionId 3000)
      (.setClientType JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
      (.setFrozen (JonSharedCmd$Frozen/newBuilder))
      (.build)))

;; ============================================================================
;; Simple State Messages
;; ============================================================================

(defn create-minimal-state
  "Create a minimal state message"
  []
  (-> (JonSharedData$JonGUIState/newBuilder)
      (.setProtocolVersion 1)
      (.build)))

;; ============================================================================
;; Invalid Messages for Testing
;; ============================================================================

(defn create-invalid-protocol-cmd
  "Create a command with invalid protocol version (0)"
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion 0)  ; Invalid: should be > 0
      (.setSessionId 4000)
      (.setClientType JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
      (.setPing (JonSharedCmd$Ping/newBuilder))
      (.build)))

(defn create-invalid-protocol-state
  "Create a state with invalid protocol version (0)"
  []
  (-> (JonSharedData$JonGUIState/newBuilder)
      (.setProtocolVersion 0)  ; Invalid: should be > 0
      (.build)))

;; ============================================================================
;; Utility Functions
;; ============================================================================

(defn message-to-bytes
  "Convert a protobuf message to byte array"
  [message]
  (.toByteArray message))

(defn create-empty-bytes
  "Create empty byte array"
  []
  (byte-array 0))

(defn create-garbage-bytes
  "Create random garbage bytes"
  [size]
  (byte-array (repeatedly size #(unchecked-byte (rand-int 256)))))

(defn corrupt-bytes
  "Corrupt first few bytes of a byte array"
  [bytes]
  (let [corrupted (byte-array bytes)]
    (when (> (count corrupted) 0)
      (aset-byte corrupted 0 (unchecked-byte 0xFF)))
    (when (> (count corrupted) 1)
      (aset-byte corrupted 1 (unchecked-byte 0xFF)))
    (when (> (count corrupted) 2)
      (aset-byte corrupted 2 (unchecked-byte 0xFF)))
    corrupted))

(defn truncate-bytes
  "Truncate byte array to half its size"
  [bytes]
  (let [size (count bytes)
        new-size (max 1 (/ size 2))]
    (byte-array (take new-size bytes))))