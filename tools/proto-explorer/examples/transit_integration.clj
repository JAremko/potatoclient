(ns transit-integration
  "Example of integrating generated specs with Transit command system"
  (:require [potatoclient.specs.cmd :as cmd-specs]
            [potatoclient.specs.generators :as gen]
            [malli.core :as m]
            [malli.error :as me]))

;; Example function that could be added to potatoclient.transit.commands

(defn validate-command-params
  "Validate command parameters against generated Malli spec"
  [command-name params]
  (let [cmd-keyword (keyword "cmd" command-name)
        spec [:ref cmd-keyword]]
    (if (contains? cmd-specs/registry cmd-keyword)
      (if (m/validate spec params cmd-specs/schema)
        {:valid? true}
        {:valid? false
         :errors (me/humanize (m/explain spec params cmd-specs/schema))})
      {:valid? false
       :errors {:message (str "Unknown command: " command-name)}})))

;; Example usage in command builder
(defn build-validated-command
  "Build command with validation"
  [command-type params]
  (let [validation (validate-command-params command-type params)]
    (if (:valid? validation)
      {:command-type command-type
       :params params
       :validated true}
      (throw (ex-info "Invalid command parameters"
                      {:command command-type
                       :params params
                       :errors (:errors validation)})))))

;; Example instrumentation for existing functions
(comment
  ;; In potatoclient.instrumentation
  
  ;; Spec for rotary velocity command
  (m/=> rotary-set-velocity
        [:=> [:cat :double :double] [:ref :cmd/RotaryPlatform/SetVelocity]])
  
  ;; Spec for command that returns Transit message  
  (m/=> create-goto-ndc-command
        [:=> [:cat :string :double :double] 
         [:map
          [:msg-type :keyword]
          [:payload [:ref :cmd/RotaryPlatform/RotateToNDC]]]]))

;; Example test using generated data
(defn test-command-handling []
  (println "Testing command handling with generated data:")
  
  ;; Generate valid test commands
  (doseq [i (range 5)]
    (let [cmd (gen/generate-command :cmd/RotaryPlatform/SetVelocity :seed i)
          ;; Simulate processing
          result (try
                   (build-validated-command "RotaryPlatform/SetVelocity" cmd)
                   :success
                   (catch Exception e
                     :failed))]
      (println (str "Test " i ": " result)))))

;; Example: Generate test data for specific scenarios
(defn generate-edge-case-commands []
  (println "\nGenerating edge case commands:")
  
  ;; Generate commands with specific characteristics
  (let [factory (gen/create-test-factory)]
    
    ;; Minimum values
    (println "Min velocity:"
             ((:generate-command factory) :cmd/RotaryPlatform/SetVelocity
              :overrides {:azimuth -1.0 :elevation -1.0}))
    
    ;; Maximum values  
    (println "Max velocity:"
             ((:generate-command factory) :cmd/RotaryPlatform/SetVelocity
              :overrides {:azimuth 1.0 :elevation 1.0}))
    
    ;; Zero values
    (println "Zero velocity:"
             ((:generate-command factory) :cmd/RotaryPlatform/SetVelocity
              :overrides {:azimuth 0.0 :elevation 0.0}))))

;; Example: Validate existing Transit messages
(defn validate-transit-message [transit-msg]
  (let [{:keys [msg-type payload]} transit-msg]
    (case msg-type
      :command (m/validate [:ref :cmd/Root] payload cmd-specs/schema)
      ;; Add other message types as needed
      false)))

(defn -main []
  (test-command-handling)
  (generate-edge-case-commands)
  
  ;; Example validation
  (println "\nValidation examples:")
  (let [valid-params {:azimuth 0.5 :elevation -0.3}
        invalid-params {:azimuth 2.0 :elevation -0.3}] ; Out of range
    
    (println "Valid params:" (validate-command-params "RotaryPlatform/SetVelocity" valid-params))
    (println "Invalid params:" (validate-command-params "RotaryPlatform/SetVelocity" invalid-params))))

(when (= *ns* (find-ns 'transit-integration))
  (-main))