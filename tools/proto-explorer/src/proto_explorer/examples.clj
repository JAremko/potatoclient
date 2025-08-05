(ns proto-explorer.examples
  "Example usage of Proto Explorer"
  (:require [proto-explorer.core :as pe]
            [clojure.pprint :as pp]))

(defn example-basic-usage
  "Basic usage examples"
  []
  (println "\n=== Proto Explorer Examples ===\n")
  
  ;; Initialize (normally would point to actual proto files)
  (println "1. Initializing Proto Explorer:")
  (pe/init! {:proto-dir "../../examples/protogen/proto"
             :class-path "../../target/classes"})
  
  ;; Search for messages
  (println "\n2. Fuzzy search for 'camera':")
  (pp/pprint (take 3 (pe/fuzzy-find "camera")))
  
  ;; Explore a message (if classes are available)
  (println "\n3. Exploring a message structure:")
  (let [result (pe/explore "java.lang.String")] ; Using String as example
    (if (:error result)
      (println "  Note: Protobuf classes not in classpath")
      (pp/pprint result)))
  
  ;; Show command path
  (println "\n4. Getting command path:")
  (pp/pprint (pe/command-path "rotary-goto-ndc"))
  
  ;; Generate builder code
  (println "\n5. Generating Kotlin builder:")
  (pe/generate-builder "rotary-set-velocity")
  
  ;; Validate command
  (println "\n6. Validating command parameters:")
  (pp/pprint (pe/validate-command "rotary-goto-ndc" 
                                  {:channel "heat" :x 0.5 :y -0.5})))

(defn example-validation
  "Validation examples"
  []
  (println "\n=== Validation Examples ===\n")
  
  ;; Parse ActionRegistry (if available)
  (println "1. Comparing with ActionRegistry:")
  (let [registry-file "../../src/potatoclient/transit/ActionRegistry.java"]
    (if (.exists (clojure.java.io/file registry-file))
      (pp/pprint (pe/compare-with-registry registry-file))
      (println "  ActionRegistry.java not found")))
  
  ;; Find mismatches
  (println "\n2. Finding protobuf mismatches:")
  (println "  (Would scan project for inconsistencies)")
  
  ;; Generate corrected registry
  (println "\n3. Generating corrected registry:")
  (println "  registry.register(\"rotary-goto-ndc\", RotaryCommandBuilder::buildGotoNdc);")
  (println "  registry.register(\"heat-camera-set-agc\", HeatCameraCommandBuilder::buildSetAgc);"))

(defn example-code-generation
  "Code generation examples"
  []
  (println "\n=== Code Generation Examples ===\n")
  
  ;; Generate builder for specific command
  (println "1. Command builder generation:")
  (pe/generate-builder "heat-camera-set-agc")
  
  ;; Analyze command structure
  (println "\n2. Command structure analysis:")
  (pp/pprint (pe/analyze-command "rotary-goto-ndc"))
  
  ;; Generate registry entries
  (println "\n3. Registry entry generation:")
  (pe/generate-registry-entry "day-camera-set-zoom"))

(defn example-search
  "Search functionality examples"
  []
  (println "\n=== Search Examples ===\n")
  
  ;; Fuzzy search with typo
  (println "1. Fuzzy search with typo:")
  (pp/pprint (take 3 (pe/fuzzy-find "rotery"))) ; typo: rotery instead of rotary
  
  ;; Find all commands
  (println "\n2. All available commands:")
  (pp/pprint (take 5 (pe/all-commands)))
  
  ;; Commands by domain
  (println "\n3. Commands by domain:")
  (pp/pprint (pe/commands-by-domain "rotary"))
  
  ;; Find field type
  (println "\n4. Finding field types:")
  (println "  SetVelocity.azimuth type:" (pe/field-type "SetVelocity" "azimuth")))

(defn example-proto-parsing
  "Proto file parsing examples"
  []
  (println "\n=== Proto Parsing Examples ===\n")
  
  ;; Parse simple proto
  (println "1. Parsing simple proto:")
  (let [proto-result {:type :message
                      :name "Ping"
                      :fields [{:name "sequence" :type "int32" :number 1}]}]
    (pp/pprint proto-result))
  
  ;; Extract validation rules
  (println "\n2. Validation rules:")
  (pp/pprint (pe/validated-fields))
  
  ;; Message tree
  (println "\n3. Message hierarchy:")
  (println "  (Would show nested message structure)"))

(defn run-all-examples
  "Run all examples"
  []
  (example-basic-usage)
  (example-validation)
  (example-code-generation)
  (example-search)
  (example-proto-parsing)
  (println "\n=== Examples Complete ==="))

;; Main entry point for examples
(defn -main
  [& args]
  (run-all-examples))