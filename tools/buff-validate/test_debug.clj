(require '[buff-validate.validator :as v])
(require '[buff-validate.simple-test-data :as std])

(println "Testing auto-detection...")

(let [ping (std/create-ping-cmd)
      bytes (std/message-to-bytes ping)]
  (println "Ping bytes:" (vec (take 10 bytes)))
  (println "Ping size:" (count bytes))
  
  ;; Try parsing as state
  (println "\nTrying to parse as state:")
  (try
    (let [result (v/parse-state-message bytes)]
      (println "  Success! Parsed as state:" (type result)))
    (catch Exception e
      (println "  Failed:" (.getMessage e))))
  
  ;; Try parsing as cmd  
  (println "\nTrying to parse as cmd:")
  (try
    (let [result (v/parse-cmd-message bytes)]
      (println "  Success! Parsed as cmd:" (type result)))
    (catch Exception e
      (println "  Failed:" (.getMessage e))))
  
  ;; Auto-detect
  (println "\nAuto-detect result:" (v/auto-detect-message-type bytes)))