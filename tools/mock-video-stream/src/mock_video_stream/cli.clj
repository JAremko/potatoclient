(ns mock-video-stream.cli
  "Command-line interface for mock video stream tool.
  Provides various modes: subprocess, scenario execution, validation, etc."
  (:require [mock-video-stream.core :as core]
            [mock-video-stream.process :as process]
            [mock-video-stream.scenarios :as scenarios]
            [mock-video-stream.gesture-sim :as gesture-sim]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [taoensso.telemere :as log]))

;; ============================================================================
;; CLI Commands
;; ============================================================================

(defn validate-scenarios
  "Validate all test scenarios against specs"
  []
  (println "\nValidating test scenarios...")
  (let [results (scenarios/validate-all-scenarios)]
    (println (format "\nTotal scenarios: %d" (:total results)))
    (println (format "Valid: %d" (:valid results)))
    (println (format "Invalid: %d" (count (:invalid results))))
    (when (seq (:invalid results))
      (println "\nInvalid scenarios:")
      (doseq [{:keys [name errors]} (:invalid results)]
        (println (format "\n  %s:" name))
        (pp/pprint errors)))
    (System/exit (if (empty? (:invalid results)) 0 1))))

(defn list-scenarios
  "List all available test scenarios"
  []
  (println "\nAvailable test scenarios:")
  (println)
  (doseq [[name scenario] scenarios/scenarios]
    (println (format "  %-20s %s" (name name) (:description scenario))))
  (println))

(defn run-example
  "Run a specific scenario and show the results"
  [scenario-name]
  (if-let [scenario (scenarios/get-scenario (keyword scenario-name))]
    (do
      (println (format "\nRunning scenario: %s" scenario-name))
      (println (format "Description: %s\n" (:description scenario)))
      
      ;; Set up state
      (reset! core/state
              {:stream-type (or (:stream-type scenario) :heat)
               :canvas (:canvas scenario)
               :zoom-level (or (:zoom-level scenario) 0)
               :frame-data (or (:frame-data scenario) {:timestamp 0 :duration 33})})
      
      ;; Process events
      (let [results (gesture-sim/process-event-sequence (:events scenario) core/state)]
        (println "Generated commands:")
        (doseq [cmd (:commands results)]
          (pp/pprint cmd))
        (println)
        
        (println "Generated gesture events:")
        (doseq [evt (:gesture-events results)]
          (pp/pprint evt))
        (println)
        
        ;; Compare with expected
        (when (:expected-commands scenario)
          (println "Expected commands:")
          (doseq [cmd (:expected-commands scenario)]
            (pp/pprint cmd))
          (println)
          
          (if (= (:commands results) (:expected-commands scenario))
            (println "✓ Commands match expected output")
            (println "✗ Commands DO NOT match expected output")))))
    (do
      (println (format "\nError: Unknown scenario '%s'" scenario-name))
      (println "\nUse 'make scenarios' to list available scenarios")
      (System/exit 1))))

(defn export-scenarios
  "Export all scenarios to JSON files"
  [output-dir]
  (println (format "\nExporting scenarios to %s..." output-dir))
  (scenarios/export-all-scenarios output-dir)
  (println "✓ Export complete"))

;; ============================================================================
;; Main Entry Point
;; ============================================================================

(defn print-usage
  "Print usage information"
  []
  (println "Mock Video Stream Tool")
  (println)
  (println "Usage:")
  (println "  clojure -M:run <command> [options]")
  (println)
  (println "Commands:")
  (println "  validate                    Validate all test scenarios")
  (println "  list-scenarios              List available test scenarios")
  (println "  example --scenario NAME     Run a specific scenario")
  (println "  export --output-dir DIR     Export scenarios to JSON files")
  (println)
  (println "Process mode:")
  (println "  clojure -M:process --stream-type TYPE")
  (println "    Run as subprocess (TYPE: heat or day)"))

(defn -main
  "Main CLI entry point"
  [& args]
  (let [[cmd & opts] args]
    (case cmd
      "validate" (validate-scenarios)
      
      "list-scenarios" (list-scenarios)
      
      "example" 
      (let [scenario (second (drop-while #(not= "--scenario" %) opts))]
        (if scenario
          (run-example scenario)
          (do
            (println "Error: --scenario option required")
            (print-usage)
            (System/exit 1))))
      
      "export"
      (let [output-dir (second (drop-while #(not= "--output-dir" %) opts))]
        (if output-dir
          (export-scenarios output-dir)
          (do
            (println "Error: --output-dir option required")
            (print-usage)
            (System/exit 1))))
      
      ;; Default
      (do
        (print-usage)
        (System/exit 0)))))