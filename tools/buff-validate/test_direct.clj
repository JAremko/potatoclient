(require '[buff-validate.validator :as v])
(require '[buff-validate.simple-test-data :as std])

(println "Testing simple ping command...")
(let [ping (std/create-ping-cmd)
      bytes (std/message-to-bytes ping)]
  (println "Ping message bytes:" (count bytes))
  (println "Validation result:" (v/validate-binary bytes :type :cmd)))

(println "\nTesting minimal state...")
(let [state (std/create-minimal-state)
      bytes (std/message-to-bytes state)]
  (println "State message bytes:" (count bytes))
  (println "Validation result:" (v/validate-binary bytes :type :state)))

(println "\nTesting auto-detection...")
(let [ping (std/create-ping-cmd)
      bytes (std/message-to-bytes ping)]
  (println "Auto-detect result:" (v/validate-binary bytes)))