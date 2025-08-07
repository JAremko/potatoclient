#!/usr/bin/env clojure
;; Runner script for roundtrip test

(require '[clojure.java.io :as io])

;; First ensure test-roundtrip-output is on classpath
(let [test-output-dir (io/file "test-roundtrip-output")]
  (when (.exists test-output-dir)
    (println "Adding test-roundtrip-output to classpath...")
    (.add (.getURLs (ClassLoader/getSystemClassLoader))
          (.toURL (.toURI test-output-dir)))))

;; Load the test namespace and run it
(println "Loading test namespace...")
(require 'generator.full-roundtrip-validation-basic-test)

;; Run tests
(println "\nRunning tests...")
(require 'clojure.test)
((resolve 'clojure.test/run-tests) 'generator.full-roundtrip-validation-basic-test)