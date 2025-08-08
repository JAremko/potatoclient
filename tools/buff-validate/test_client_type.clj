(ns test-client-type
  (:import
   [cmd JonSharedCmd$Root JonSharedCmd$Ping]
   [ser JonSharedDataTypes$JonGuiDataClientType]))

(defn create-ping-cmd []
  (-> (JonSharedCmd$Root/newBuilder)
      (.setProtocolVersion 1)
      (.setSessionId 1000)
      (.setClientType JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
      (.setPing (JonSharedCmd$Ping/newBuilder))
      (.build)))

(let [ping (create-ping-cmd)]
  (println "Client type:" (.getClientType ping))
  (println "Client type value:" (.getNumber (.getClientType ping)))
  (println "Expected:" (.getNumber JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)))