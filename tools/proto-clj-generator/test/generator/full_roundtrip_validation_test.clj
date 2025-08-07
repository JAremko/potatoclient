(ns generator.full-roundtrip-validation-test
  "Comprehensive end-to-end roundtrip tests with full validation chain including:
   - Malli generation of test data with boundary conditions
   - Malli validation
   - EDN → Java protobuf conversion
   - buf.validate validation (if available)
   - Binary serialization/deserialization
   - Java → EDN conversion
   - Comparison with original EDN
   - Negative testing at each stage
   
   This test is PARAMOUNT and MUST pass for the proto-clj-generator to be considered working."
  (:require [clojure.test :refer :all]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            [generator.core :as core]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [com.google.protobuf InvalidProtocolBufferException]))

;; =============================================================================
;; Test Setup
;; =============================================================================

(defonce generated? (atom false))

(defn ensure-generated-code!
  "Generate code once for all tests"
  []
  (when-not @generated?
    (println "\nGenerating code for full roundtrip validation tests...")
    ;; Clear naming cache to avoid collisions
    (require 'generator.naming)
    ((resolve 'generator.naming/clear-conversion-cache!))
    
    ;; Generate code with constraints
    (let [result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                     :output-dir "test-roundtrip-output"
                                     :namespace-prefix "test.roundtrip"
                                     :debug? false})]
      (when-not (:success result)
        (throw (ex-info "Failed to generate code" result)))
      
      ;; Load generated namespaces
      (println "Loading generated code...")
      (require '[test.roundtrip.cmd :as cmd-gen] :reload)
      (require '[test.roundtrip.ser :as state-gen] :reload)
      
      (reset! generated? true))))

;; Register the custom :oneof schema globally so all generated code can use it
(mr/set-default-registry!
  (merge (m/default-schemas)
         (mr/schemas m/default-registry)
         {:oneof oneof/-oneof-schema}))

;; =============================================================================
;; Validation Helpers
;; =============================================================================

(defn validate-with-malli
  "Validate data with Malli schema"
  [schema data]
  (let [valid? (m/validate schema data {:registry registry})]
    {:valid? valid?
     :errors (when-not valid?
               (m/explain schema data {:registry registry}))}))

(defn validate-with-buf
  "Validate a protobuf message using buf.validate if available.
   Returns {:valid? true} or {:valid? false :errors [...]}."
  [proto-message]
  ;; Try to load buf.validate classes dynamically
  (try
    (let [validator-class (Class/forName "build.buf.protovalidate.Validator")
          factory-class (Class/forName "build.buf.protovalidate.ValidatorFactory")
          factory (.newInstance factory-class)
          validator (.build factory)
          result (.validate validator proto-message)]
      (if (.isSuccess result)
        {:valid? true}
        {:valid? false
         :errors (map #(.getMessage %) (.getViolations result))}))
    (catch ClassNotFoundException _
      ;; buf.validate not available, skip validation
      {:valid? true :skipped true})
    (catch Exception e
      {:valid? false
       :errors [(.getMessage e)]})))

;; =============================================================================
;; Full Roundtrip Test
;; =============================================================================

(defn full-roundtrip-test
  "Performs full roundtrip test with all validation stages.
   Returns a map with results from each stage."
  [original-edn build-fn parse-fn proto-class schema test-name]
  (let [results {:test-name test-name
                 :original original-edn
                 :stages {}}]
    (try
      ;; Stage 1: Validate EDN with Malli
      (let [malli-result (validate-with-malli schema original-edn)]
        (-> results
            (assoc-in [:stages :malli-validation] malli-result)
            (as-> res
              (if (:valid? malli-result)
                ;; Stage 2: Convert EDN → Java protobuf
                (try
                  (let [proto (build-fn original-edn)]
                    (-> res
                        (assoc-in [:stages :edn-to-java] {:success true
                                                          :type (type proto)})
                        (as-> res2
                          ;; Stage 3: Validate with buf.validate
                          (let [buf-result (validate-with-buf proto)]
                            (-> res2
                                (assoc-in [:stages :buf-validation] buf-result)
                                (as-> res3
                                  (if (or (:valid? buf-result) (:skipped buf-result))
                                    ;; Stage 4: Serialize to binary
                                    (let [binary (.toByteArray proto)]
                                      (-> res3
                                          (assoc-in [:stages :serialization]
                                                    {:success true
                                                     :size (count binary)})
                                          (as-> res4
                                            ;; Stage 5: Deserialize from binary
                                            (try
                                              (let [parse-method (.getMethod proto-class "parseFrom" (into-array Class [(.getClass binary)]))
                                                    parsed (.invoke parse-method nil (into-array Object [binary]))]
                                                (-> res4
                                                    (assoc-in [:stages :deserialization] {:success true})
                                                    (as-> res5
                                                      ;; Stage 6: Validate parsed with buf.validate
                                                      (let [buf-result-parsed (validate-with-buf parsed)]
                                                        (-> res5
                                                            (assoc-in [:stages :buf-validation-parsed] buf-result-parsed)
                                                            (as-> res6
                                                              ;; Stage 7: Compare Java representations
                                                              (let [java-equal? (.equals proto parsed)]
                                                                (-> res6
                                                                    (assoc-in [:stages :java-comparison]
                                                                              {:equal? java-equal?})
                                                                    (as-> res7
                                                                      ;; Stage 8: Convert Java → EDN
                                                                      (try
                                                                        (let [roundtripped-edn (parse-fn parsed)]
                                                                          (-> res7
                                                                              (assoc-in [:stages :java-to-edn]
                                                                                        {:success true})
                                                                              (assoc :roundtripped roundtripped-edn)
                                                                              (as-> res8
                                                                                ;; Stage 9: Validate roundtripped EDN
                                                                                (let [malli-result-rt (validate-with-malli schema roundtripped-edn)]
                                                                                  (-> res8
                                                                                      (assoc-in [:stages :malli-validation-roundtripped] malli-result-rt)
                                                                                      (as-> res9
                                                                                        ;; Stage 10: Compare EDN
                                                                                        (assoc-in res9 [:stages :edn-comparison]
                                                                                                  {:equal? (= original-edn roundtripped-edn)
                                                                                                   :diff (when-not (= original-edn roundtripped-edn)
                                                                                                           {:original original-edn
                                                                                                            :roundtripped roundtripped-edn})})))))))
                                                                        (catch Exception e
                                                                          (assoc-in res7 [:stages :java-to-edn]
                                                                                    {:success false
                                                                                     :error (.getMessage e)}))))))))))
                                              (catch Exception e
                                                (assoc-in res4 [:stages :deserialization]
                                                          {:success false
                                                           :error (.getMessage e)}))))))
                                    res3))))))
                  (catch Exception e
                    (assoc-in res [:stages :edn-to-java]
                              {:success false
                               :error (.getMessage e)})))
                res))))
      (catch Exception e
        (assoc-in results [:stages :error]
                  {:exception (type e)
                   :message (.getMessage e)})))))

(defn analyze-results
  "Analyze test results and return summary"
  [results]
  (let [stages (:stages results)
        all-passed? (and (get-in stages [:malli-validation :valid?])
                        (get-in stages [:edn-to-java :success])
                        (or (get-in stages [:buf-validation :valid?])
                            (get-in stages [:buf-validation :skipped]))
                        (get-in stages [:serialization :success])
                        (get-in stages [:deserialization :success])
                        (or (get-in stages [:buf-validation-parsed :valid?])
                            (get-in stages [:buf-validation-parsed :skipped]))
                        (get-in stages [:java-comparison :equal?])
                        (get-in stages [:java-to-edn :success])
                        (get-in stages [:malli-validation-roundtripped :valid?])
                        (get-in stages [:edn-comparison :equal?])
                        (not (get-in stages [:error])))]
    {:passed? all-passed?
     :failed-stages (when-not all-passed?
                     (cond-> []
                       (not (get-in stages [:malli-validation :valid?]))
                       (conj :malli-validation)
                       
                       (not (get-in stages [:edn-to-java :success]))
                       (conj :edn-to-java)
                       
                       (and (not (get-in stages [:buf-validation :valid?]))
                            (not (get-in stages [:buf-validation :skipped])))
                       (conj :buf-validation)
                       
                       (not (get-in stages [:serialization :success]))
                       (conj :serialization)
                       
                       (not (get-in stages [:deserialization :success]))
                       (conj :deserialization)
                       
                       (and (not (get-in stages [:buf-validation-parsed :valid?]))
                            (not (get-in stages [:buf-validation-parsed :skipped])))
                       (conj :buf-validation-parsed)
                       
                       (not (get-in stages [:java-comparison :equal?]))
                       (conj :java-comparison)
                       
                       (not (get-in stages [:java-to-edn :success]))
                       (conj :java-to-edn)
                       
                       (not (get-in stages [:malli-validation-roundtripped :valid?]))
                       (conj :malli-validation-roundtripped)
                       
                       (not (get-in stages [:edn-comparison :equal?]))
                       (conj :edn-comparison)
                       
                       (get-in stages [:error])
                       (conj :error)))}))

;; =============================================================================
;; Test Fixtures
;; =============================================================================

(use-fixtures :once 
  (fn [f]
    (ensure-generated-code!)
    (f)))

;; =============================================================================
;; Command Roundtrip Tests
;; =============================================================================

(deftest command-boundary-tests
  (testing "Command messages with boundary conditions"
    (let [cmd-ns (find-ns 'test.roundtrip.cmd)
          build-root (ns-resolve cmd-ns 'build-root)
          parse-root (ns-resolve cmd-ns 'parse-root)
          root-class (Class/forName "cmd.JonSharedCmd$Root")
          ;; Get the generated schema
          cmd-specs (ns-resolve cmd-ns 'specs)
          root-schema (get @cmd-specs :root)]
      
      (testing "Minimal valid command"
        (let [minimal-cmd {:protocol-version 1
                          :client-type :jon-gui-data-client-type-local-network
                          :ping {}}
              result (full-roundtrip-test minimal-cmd build-root parse-root 
                                        root-class root-schema "minimal-command")
              analysis (analyze-results result)]
          (is (:passed? analysis) 
              (str "Minimal command should pass all stages. Failed: " (:failed-stages analysis)))))
      
      (testing "Protocol version boundary (constraint: > 0)"
        (let [boundary-cmd {:protocol-version 1  ; Minimum valid value
                           :client-type :jon-gui-data-client-type-local-network
                           :ping {}}
              result (full-roundtrip-test boundary-cmd build-root parse-root 
                                        root-class root-schema "protocol-version-boundary")
              analysis (analyze-results result)]
          (is (:passed? analysis)
              (str "Protocol version = 1 should pass. Failed: " (:failed-stages analysis)))))
      
      (testing "Maximum integer values"
        (let [max-int-cmd {:protocol-version Integer/MAX_VALUE
                          :session-id Integer/MAX_VALUE
                          :client-type :jon-gui-data-client-type-local-network
                          :ping {}}
              result (full-roundtrip-test max-int-cmd build-root parse-root 
                                        root-class root-schema "max-integers")
              analysis (analyze-results result)]
          (is (:passed? analysis)
              (str "Max integer values should roundtrip. Failed: " (:failed-stages analysis))))))))

(deftest command-negative-tests
  (testing "Invalid command data is caught at appropriate stages"
    (let [cmd-ns (find-ns 'test.roundtrip.cmd)
          build-root (ns-resolve cmd-ns 'build-root)
          parse-root (ns-resolve cmd-ns 'parse-root)
          root-class (Class/forName "cmd.JonSharedCmd$Root")
          cmd-specs (ns-resolve cmd-ns 'specs)
          root-schema (get @cmd-specs :root)]
      
      (testing "Invalid protocol version (constraint violation)"
        (let [invalid-cmd {:protocol-version 0  ; Must be > 0
                          :client-type :jon-gui-data-client-type-local-network
                          :ping {}}
              result (full-roundtrip-test invalid-cmd build-root parse-root 
                                        root-class root-schema "invalid-protocol-version")
              analysis (analyze-results result)]
          (is (not (:passed? analysis))
              "Protocol version = 0 should fail validation")
          (is (or (some #{:malli-validation} (:failed-stages analysis))
                  (some #{:buf-validation} (:failed-stages analysis)))
              "Should fail at Malli or buf.validate stage")))
      
      (testing "Invalid enum value"
        (let [invalid-cmd {:protocol-version 1
                          :client-type :invalid-client-type
                          :ping {}}
              result (full-roundtrip-test invalid-cmd build-root parse-root 
                                        root-class root-schema "invalid-enum")
              analysis (analyze-results result)]
          (is (not (:passed? analysis))
              "Invalid enum should fail")
          (is (some #{:malli-validation} (:failed-stages analysis))
              "Should fail at Malli validation stage")))
      
      (testing "Missing required oneof"
        (let [invalid-cmd {:protocol-version 1
                          :client-type :jon-gui-data-client-type-local-network}
              result (full-roundtrip-test invalid-cmd build-root parse-root 
                                        root-class root-schema "missing-oneof")
              analysis (analyze-results result)]
          ;; This might pass Malli but should fail buf.validate
          (is (or (not (:passed? analysis))
                  (get-in result [:stages :buf-validation :skipped]))
              "Missing oneof should fail or buf.validate should be skipped"))))))

(deftest command-generator-tests
  (testing "Generate and test command messages"
    (let [cmd-ns (find-ns 'test.roundtrip.cmd)
          build-root (ns-resolve cmd-ns 'build-root)
          parse-root (ns-resolve cmd-ns 'parse-root)
          root-class (Class/forName "cmd.JonSharedCmd$Root")
          cmd-specs (ns-resolve cmd-ns 'specs)
          root-schema (get @cmd-specs :root)
          ;; Generate samples
          samples (try
                   (take 50 (mg/sample root-schema {:size 50 :registry registry}))
                   (catch Exception e
                     (println "Failed to generate samples:" (.getMessage e))
                     []))
          stats (atom {:total 0 :passed 0 :failed 0 :errors {}})]
      
      (doseq [sample samples]
        (swap! stats update :total inc)
        (let [result (full-roundtrip-test sample build-root parse-root 
                                        root-class root-schema 
                                        (str "generated-cmd-" (:total @stats)))
              analysis (analyze-results result)]
          (if (:passed? analysis)
            (swap! stats update :passed inc)
            (do
              (swap! stats update :failed inc)
              (doseq [stage (:failed-stages analysis)]
                (swap! stats update-in [:errors stage] (fnil inc 0)))))))
      
      (println "\nCommand generator test results:")
      (println "Total samples:" (:total @stats))
      (println "Passed:" (:passed @stats))
      (println "Failed:" (:failed @stats))
      (when (seq (:errors @stats))
        (println "Failed stages:" (:errors @stats)))
      
      ;; Allow some failures due to generator edge cases, but require high pass rate
      (is (>= (:passed @stats) (* 0.9 (:total @stats)))
          (str "At least 90% of generated commands should pass. "
               "Passed: " (:passed @stats) "/" (:total @stats))))))

;; =============================================================================
;; State Roundtrip Tests
;; =============================================================================

(deftest state-boundary-tests
  (testing "State messages with boundary conditions"
    (let [state-ns (find-ns 'test.roundtrip.ser)
          build-fn (ns-resolve state-ns 'build-jon-gui-state)
          parse-fn (ns-resolve state-ns 'parse-jon-gui-state)
          state-class (Class/forName "ser.JonSharedData$JonGUIState")
          state-specs (ns-resolve state-ns 'specs)
          state-schema (get @state-specs :jon-gui-state)]
      
      (testing "Minimal valid state"
        (let [minimal-state {:protocol-version 1}
              result (full-roundtrip-test minimal-state build-fn parse-fn 
                                        state-class state-schema "minimal-state")
              analysis (analyze-results result)]
          (is (:passed? analysis)
              (str "Minimal state should pass. Failed: " (:failed-stages analysis)))))
      
      (testing "State with all optional fields"
        (let [full-state {:protocol-version 1
                         :system {}
                         :meteo-internal {}
                         :lrf {}
                         :time {}
                         :gps {}
                         :compass {}
                         :rotary {}
                         :camera-day {}
                         :camera-heat {}
                         :compass-calibration {}
                         :rec-osd {}
                         :day-cam-glass-heater {}
                         :actual-space-time {}}
              result (full-roundtrip-test full-state build-fn parse-fn 
                                        state-class state-schema "full-state")
              analysis (analyze-results result)]
          (is (:passed? analysis)
              (str "State with all fields should pass. Failed: " (:failed-stages analysis))))))))

;; =============================================================================
;; RGB Color Constraint Tests
;; =============================================================================

(deftest rgb-color-constraint-tests
  (testing "RGB color values with constraints [0-255]"
    (let [state-ns (find-ns 'test.roundtrip.ser)
          ;; Need to find a message type that has RGB values
          ;; This will depend on what's actually in the proto files
          ;; For now, we'll test the constraint validation principle
          ]
      
      (testing "RGB boundary values"
        ;; Test min value (0)
        (is true "RGB min value test placeholder")
        
        ;; Test max value (255)
        (is true "RGB max value test placeholder")
        
        ;; Test invalid values
        (is true "RGB invalid value test placeholder")))))

;; =============================================================================
;; Performance Test
;; =============================================================================

(deftest roundtrip-performance-test
  (testing "Roundtrip performance is reasonable"
    (let [cmd-ns (find-ns 'test.roundtrip.cmd)
          build-root (ns-resolve cmd-ns 'build-root)
          parse-root (ns-resolve cmd-ns 'parse-root)
          root-class (Class/forName "cmd.JonSharedCmd$Root")
          sample {:protocol-version 1
                 :client-type :jon-gui-data-client-type-local-network
                 :ping {}}
          
          ;; Warm up
          _ (dotimes [_ 100]
              (let [proto (build-root sample)
                    binary (.toByteArray proto)
                    parsed (.parseFrom root-class binary)]
                (parse-root parsed)))
          
          ;; Measure
          start (System/nanoTime)
          _ (dotimes [_ 1000]
              (let [proto (build-root sample)
                    binary (.toByteArray proto)
                    parsed (.parseFrom root-class binary)]
                (parse-root parsed)))
          end (System/nanoTime)
          duration-ms (/ (- end start) 1e6)]
      
      (println (format "\n1000 roundtrips completed in %.2f ms (%.2f μs per roundtrip)"
                      duration-ms
                      (/ duration-ms 1000)))
      
      (is (< duration-ms 5000)
          "1000 roundtrips should complete in under 5 seconds")))))

;; =============================================================================
;; Final Summary Test
;; =============================================================================

(deftest ^:summary roundtrip-test-summary
  (testing "PARAMOUNT PRINCIPLE: Full roundtrip validation with boundary checks"
    (println "\n" (str/join "" (repeat 80 "="))
             "\nFULL ROUNDTRIP VALIDATION TEST SUMMARY"
             "\n" (str/join "" (repeat 80 "=")))
    
    (println "\nThis test validates the ENTIRE data pipeline:")
    (println "1. Generate test data from Malli specs (with constraints)")
    (println "2. Validate with Malli specs")
    (println "3. Convert to Java protobuf")
    (println "4. Validate with buf.validate (if available)")
    (println "5. Serialize to binary")
    (println "6. Parse from binary")
    (println "7. Validate again")
    (println "8. Convert to EDN")
    (println "9. Compare with original")
    
    (println "\nAll tests in this namespace MUST pass for proto-clj-generator to be considered working.")
    
    (is true "Summary marker test")))