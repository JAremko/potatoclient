(require '[buff-validate.pronto-test-data :as ptd])
(require '[buff-validate.validator :as v])

(println "Testing idiomatic Pronto message creation and validation...")

(println "\n1. Testing valid ping command:")
(let [cmd (ptd/create-ping-cmd)
      bytes (ptd/proto-map->bytes cmd)]
  (println "  Proto-map type:" (type cmd))
  (println "  Bytes:" (count bytes))
  (let [result (v/validate-binary bytes :type :cmd)]
    (println "  Valid?" (:valid? result))
    (when-not (:valid? result)
      (println "  Violations:" (:violations result)))))

(println "\n2. Testing valid state message:")
(let [state (ptd/create-minimal-valid-state)
      bytes (ptd/proto-map->bytes state)]
  (println "  Proto-map type:" (type state))
  (println "  Bytes:" (count bytes))
  (let [result (v/validate-binary bytes :type :state)]
    (println "  Valid?" (:valid? result))
    (when-not (:valid? result)
      (println "  First 3 violations:" (take 3 (:violations result))))))

(println "\n3. Testing invalid client type (should fail):")
(let [cmd (ptd/create-invalid-client-type-cmd)
      bytes (ptd/proto-map->bytes cmd)]
  (let [result (v/validate-binary bytes :type :cmd)]
    (println "  Valid?" (:valid? result))
    (println "  Expected violation:" (:violations result))))

(println "\n4. Testing performant updates:")
(let [original-cmd (ptd/create-ping-cmd)
      updated-cmd (ptd/update-cmd-session-id original-cmd 9999)]
  (println "  Original session ID:" (:session-id original-cmd))
  (println "  Updated session ID:" (:session-id updated-cmd)))

(println "\n5. Testing auto-detection:")
(let [cmd-bytes (ptd/get-ping-cmd-bytes)
      state-bytes (ptd/get-valid-state-bytes)]
  (println "  CMD detected as:" (:message-type (v/validate-binary cmd-bytes)))
  (println "  STATE detected as:" (:message-type (v/validate-binary state-bytes))))

(println "\nDone!")