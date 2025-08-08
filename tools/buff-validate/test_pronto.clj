(require '[buff-validate.pronto-test-data :as ptd])
(require '[buff-validate.validator :as v])

(println "Testing Pronto-based message creation...")

(println "\n1. Testing valid ping command:")
(let [bytes (ptd/get-ping-cmd-bytes)]
  (println "  Bytes:" (count bytes))
  (let [result (v/validate-binary bytes :type :cmd)]
    (println "  Valid?" (:valid? result))
    (println "  Violations:" (:violations result))))

(println "\n2. Testing valid state message:")
(let [bytes (ptd/get-valid-state-bytes)]
  (println "  Bytes:" (count bytes))
  (let [result (v/validate-binary bytes :type :state)]
    (println "  Valid?" (:valid? result))
    (if-not (:valid? result)
      (println "  Violations:" (take 3 (:violations result)) "..."))))

(println "\n3. Testing invalid client type (should fail):")
(let [bytes (ptd/get-invalid-client-type-cmd-bytes)]
  (println "  Bytes:" (count bytes))
  (let [result (v/validate-binary bytes :type :cmd)]
    (println "  Valid?" (:valid? result))
    (println "  Violations:" (:violations result))))

(println "\n4. Testing auto-detection with valid ping:")
(let [bytes (ptd/get-ping-cmd-bytes)]
  (let [result (v/validate-binary bytes)]  ; No :type specified
    (println "  Detected type:" (:message-type result))
    (println "  Valid?" (:valid? result))))