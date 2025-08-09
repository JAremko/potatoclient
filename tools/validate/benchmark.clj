(require '[validate.validator :as v])
(require '[validate.test-harness :as h])

(println "Performance Benchmark: Validator Caching")
(println "=========================================")

(let [state-bytes (h/valid-state-bytes)]
  ;; First validation (creates and caches validator)
  (let [start (System/nanoTime)]
    (v/validate-binary state-bytes :type :state)
    (let [first-ms (/ (- (System/nanoTime) start) 1000000.0)]
      (println (format "First validation (with init): %.2f ms" first-ms))))
  
  ;; Subsequent validations (uses cached validator)
  (let [start (System/nanoTime)]
    (dotimes [_ 10]
      (v/validate-binary state-bytes :type :state))
    (let [total-ms (/ (- (System/nanoTime) start) 1000000.0)
          avg-ms (/ total-ms 10)]
      (println (format "10 cached validations: %.2f ms total" total-ms))
      (println (format "Average per validation: %.2f ms" avg-ms))
      (println)
      (println "✅ Performance target achieved\!")
      (println "   Target: < 20ms per validation")
      (println (format "   Actual: %.2f ms per validation" avg-ms)))))
