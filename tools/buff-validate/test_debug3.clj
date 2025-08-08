(require '[buff-validate.simple-test-data :as std])

(println "Checking ping command creation...")

(let [ping (std/create-ping-cmd)]
  (println "Protocol version:" (.getProtocolVersion ping))
  (println "Session ID:" (.getSessionId ping))
  (println "Client type:" (.getClientType ping))
  (println "Client type value:" (.getNumber (.getClientType ping)))
  (println "Has ping?" (.hasPing ping)))