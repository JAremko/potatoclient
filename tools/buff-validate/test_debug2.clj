(require '[buff-validate.validator :as v])
(require '[buff-validate.simple-test-data :as std])

(println "Testing parsed message fields...")

(let [ping (std/create-ping-cmd)
      bytes (std/message-to-bytes ping)]
  
  ;; Parse as state and check fields
  (println "\nParsed as state:")
  (try
    (let [result (v/parse-state-message bytes)]
      (println "  Protocol version:" (.getProtocolVersion result))
      (println "  Has time?" (.hasTime result))
      (println "  Has system?" (.hasSystem result))
      (println "  Has GPS?" (.hasGps result)))
    (catch Exception e
      (println "  Failed:" (.getMessage e))))
  
  ;; Parse as cmd and check fields  
  (println "\nParsed as cmd:")
  (try
    (let [result (v/parse-cmd-message bytes)]
      (println "  Protocol version:" (.getProtocolVersion result))
      (println "  Session ID:" (.getSessionId result))
      (println "  Has ping?" (.hasPing result))
      (println "  Has noop?" (.hasNoop result))
      (println "  Client type:" (.getClientType result)))
    (catch Exception e
      (println "  Failed:" (.getMessage e)))))