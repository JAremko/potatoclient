#!/usr/bin/env clojure

(require '[clojure.test :as test])

;; Add paths
(System/setProperty "clojure.compile.path" "target/classes")
(.mkdirs (java.io.File. "target/classes"))

;; Load test namespaces
(require 'mock-video-stream.core-test)
(require 'mock-video-stream.gesture-sim-test)  
(require 'mock-video-stream.scenarios-test)

;; Run tests
(let [results (test/run-tests 'mock-video-stream.core-test
                              'mock-video-stream.gesture-sim-test
                              'mock-video-stream.scenarios-test)]
  (println "\n============ Test Summary ============")
  (println "Tests run:" (:test results))
  (println "Assertions:" (:pass results))
  (println "Failures:" (:fail results))
  (println "Errors:" (:error results))
  (System/exit (if (zero? (+ (:fail results) (:error results))) 0 1)))