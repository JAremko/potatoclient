(ns real-file-test
  (:require [meta-to-arrow.final-transform :as transform]
            [clojure.java.io :as io]))

;; Test with actual file content
(def test-content (slurp "../../src/potatoclient/ui/status_bar/messages.clj"))

(println "Testing with real file: messages.clj")
(println "First 500 chars of input:")
(println (subs test-content 0 (min 500 (count test-content))))
(println)

(let [result (transform/transform-file test-content {:require-alias "m"})]
  (println "Transformation result:")
  (println "Count:" (:count result))
  (println)
  (when (pos? (:count result))
    (println "First 1000 chars of transformed:")
    (println (subs (:transformed result) 0 (min 1000 (count (:transformed result)))))))