(ns test-suites.cmd-suite
  (:require [clojure.test :refer [run-tests]]
            ;; Core command tests
            [potatoclient.cmd.root-test]
            [potatoclient.cmd.negative-test]
            [potatoclient.cmd.validation-test]
            [potatoclient.cmd.builder-test]
            [potatoclient.cmd.generative-test]
            [potatoclient.cmd.integration-test]
            ;; Specific command tests
            [potatoclient.cmd.cam-day-glass-heater-test]
            [potatoclient.cmd.compass-debug-test]
            [potatoclient.cmd.compass-generative-test]
            [potatoclient.cmd.compass-micheck-test]
            [potatoclient.cmd.compass-test]
            [potatoclient.cmd.cv-test]
            [potatoclient.cmd.day-camera-test]
            [potatoclient.cmd.gps-test]
            [potatoclient.cmd.heat-camera-test]
            [potatoclient.cmd.lrf-alignment-test]
            [potatoclient.cmd.lrf-test]
            [potatoclient.cmd.osd-test]
            [potatoclient.cmd.rotary-test]
            ;; Spec tests
            [potatoclient.specs.cmd.root-test]
            [potatoclient.specs.cmd.root-validate-test]))

(defn run-cmd-tests []
  (println "\n========================================")
  (println "Running Command Tests")
  (println "========================================\n")
  (let [results (run-tests ;; Core command tests
                          'potatoclient.cmd.root-test
                          'potatoclient.cmd.negative-test
                          'potatoclient.cmd.validation-test
                          'potatoclient.cmd.builder-test
                          'potatoclient.cmd.generative-test
                          'potatoclient.cmd.integration-test
                          ;; Specific command tests
                          'potatoclient.cmd.cam-day-glass-heater-test
                          'potatoclient.cmd.compass-debug-test
                          'potatoclient.cmd.compass-generative-test
                          'potatoclient.cmd.compass-micheck-test
                          'potatoclient.cmd.compass-test
                          'potatoclient.cmd.cv-test
                          'potatoclient.cmd.day-camera-test
                          'potatoclient.cmd.gps-test
                          'potatoclient.cmd.heat-camera-test
                          'potatoclient.cmd.lrf-alignment-test
                          'potatoclient.cmd.lrf-test
                          'potatoclient.cmd.osd-test
                          'potatoclient.cmd.rotary-test
                          ;; Spec tests
                          'potatoclient.specs.cmd.root-test
                          'potatoclient.specs.cmd.root-validate-test)]
    (println "\n========================================")
    (println "Command Tests Complete")
    (println "========================================\n")
    results))

(defn -main [& _args]
  (let [{:keys [fail error]} (run-cmd-tests)]
    (System/exit (if (zero? (+ fail error)) 0 1))))