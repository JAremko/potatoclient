(ns using-generated-specs
  "Example of using the generated Malli specs in the main application"
  (:require [potatoclient.specs.cmd :as cmd-specs]
            [potatoclient.specs.state :as state-specs]
            [potatoclient.specs.generators :as gen]
            [malli.core :as m]
            [malli.error :as me]
            [clojure.pprint :as pp]))

;; Example 1: Validate a command structure
(defn validate-rotary-command []
  (let [valid-cmd {:rotary {:rotate_to_ndc {:channel "heat"
                                            :x 0.5
                                            :y -0.3}}}
        invalid-cmd {:rotary {:rotate_to_ndc {:x 2.0  ; Out of NDC range
                                              :y -0.3}}}]
    
    (println "Valid command validation:")
    (println (m/validate [:ref :cmd/Root] valid-cmd cmd-specs/schema))
    
    (println "\nInvalid command validation:")
    (println (m/validate [:ref :cmd/Root] invalid-cmd cmd-specs/schema))
    
    ;; Get detailed errors
    (when-let [errors (m/explain [:ref :cmd/Root] invalid-cmd cmd-specs/schema)]
      (println "\nValidation errors:")
      (pp/pprint (me/humanize errors)))))

;; Example 2: Generate test data
(defn generate-test-commands []
  (println "\nGenerating test commands:")
  
  ;; Generate a ping command
  (let [ping-cmd (gen/generate-command :cmd/Ping)]
    (println "Generated Ping:" ping-cmd))
  
  ;; Generate a rotary command with seed
  (let [rotary-cmd (gen/generate-command :cmd/RotaryPlatform/RotateToNDC :seed 42)]
    (println "Generated RotateToNDC:" rotary-cmd))
  
  ;; Generate root command (will have one random command)
  (let [root-cmd (gen/generate-cmd-root :seed 100)]
    (println "Generated Root command:" (keys root-cmd))))

;; Example 3: Use in Guardrails specs
(defn example-guardrails-usage []
  ;; This would go in your instrumentation namespace
  (comment
    (require '[com.fulcrologic.guardrails.malli.core :as gr])
    
    (gr/=> process-command 
           [:=> [:cat [:ref :cmd/Root]] [:or :keyword ::error]])
    
    (gr/=> handle-state-update
           [:=> [:cat [:ref :ser/JonGUIState]] nil?])))

;; Example 4: Property-based testing with generated data
(defn property-test-example []
  (println "\nProperty test example:")
  
  ;; Test that all generated commands are valid
  (let [results (for [i (range 10)]
                  (let [cmd (gen/generate-cmd-root :seed i)]
                    (m/validate [:ref :cmd/Root] cmd cmd-specs/schema)))]
    (println "All generated commands valid?" (every? true? results))))

;; Example 5: Create custom generators with constraints
(defn custom-generator-example []
  (println "\nCustom generator example:")
  
  ;; Use the test factory to generate with overrides
  (let [factory (gen/create-test-factory)
        custom-cmd ((:generate-command factory) 
                    :cmd/RotaryPlatform/SetVelocity
                    :overrides {:azimuth 0.5 :elevation -0.5})]
    (println "Custom velocity command:" custom-cmd)))

;; Example 6: Extract spec for documentation
(defn document-command-structure []
  (println "\nCommand structure documentation:")
  
  ;; Get the spec for a specific command
  (let [spec (get cmd-specs/registry :cmd/RotaryPlatform/RotateToNDC)]
    (println "RotateToNDC spec:")
    (pp/pprint spec)))

;; Run examples
(defn -main []
  (validate-rotary-command)
  (generate-test-commands)
  (property-test-example)
  (custom-generator-example)
  (document-command-structure))

;; Run if executed directly
(when (= *ns* (find-ns 'using-generated-specs))
  (-main))