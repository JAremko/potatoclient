(ns validate.simple-test-data
  "Simple test data generation for validate tests using minimal messages."
  (:import
   ;; State message imports
   [ser JonSharedData$JonGUIState JonSharedData$JonGUIState$Builder]
   ;; Command message imports  
   [cmd JonSharedCmd$Root JonSharedCmd$Root$Builder]
   [cmd JonSharedCmd$Ping JonSharedCmd$Ping$Builder]
   [cmd JonSharedCmd$Noop JonSharedCmd$Noop$Builder]
   [cmd JonSharedCmd$Frozen JonSharedCmd$Frozen$Builder]
))

(defn create-ping-cmd
  "Create a simple Ping command in a Root message."
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion 1)
      (.setSessionId 1000)
      (.setPing (-> (JonSharedCmd$Ping/newBuilder)
                    (.build)))
      (.build)))

(defn create-noop-cmd
  "Create a simple Noop command in a Root message."
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion 1)
      (.setSessionId 2000)
      (.setNoop (-> (JonSharedCmd$Noop/newBuilder)
                    (.build)))
      (.build)))

(defn create-frozen-cmd
  "Create a simple Frozen command in a Root message."
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion 1)
      (.setSessionId 3000)
      (.setFrozen (-> (JonSharedCmd$Frozen/newBuilder)
                      (.build)))
      (.build)))

; Glass heater commands removed - class structure needs investigation

(defn create-minimal-state
  "Create a minimal valid state message with only protocol version."
  []
  (-> (JonSharedData$JonGUIState/newBuilder)
      (.setProtocolVersion 1)
      (.build)))

(defn create-invalid-protocol-cmd
  "Create command with invalid protocol version (0)."
  []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion 0) ; Should fail validation: must be > 0
      (.setPing (-> (JonSharedCmd$Ping/newBuilder)
                    (.build)))
      (.build)))

(defn create-invalid-protocol-state
  "Create state with invalid protocol version (0)."
  []
  (-> (JonSharedData$JonGUIState/newBuilder)
      (.setProtocolVersion 0) ; Should fail validation: must be > 0
      (.build)))

; Day camera commands removed - class structure needs investigation

(defn message-to-bytes
  "Convert a protobuf message to byte array."
  [message]
  (.toByteArray message))