(import '[cmd JonSharedCmd$Root JonSharedCmd$Ping])
(import '[ser JonSharedDataTypes$JonGuiDataClientType])

(println "Creating ping command with client type...")

(let [builder (JonSharedCmd$Root/newBuilder)]
  (println "Setting protocol version...")
  (.setProtocolVersion builder 1)
  
  (println "Setting session ID...")
  (.setSessionId builder 1000)
  
  (println "Setting client type to LOCAL_NETWORK...")
  (println "  LOCAL_NETWORK enum:" JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
  (.setClientType builder JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK)
  
  (println "Setting ping...")
  (.setPing builder (JonSharedCmd$Ping/newBuilder))
  
  (println "Building...")
  (let [msg (.build builder)]
    (println "Result:")
    (println "  Protocol version:" (.getProtocolVersion msg))
    (println "  Session ID:" (.getSessionId msg)) 
    (println "  Client type:" (.getClientType msg))
    (println "  Client type value:" (.getNumber (.getClientType msg)))
    (println "  Has ping?" (.hasPing msg))))