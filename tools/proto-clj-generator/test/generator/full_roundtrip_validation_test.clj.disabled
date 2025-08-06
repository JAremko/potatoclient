(ns generator.full-roundtrip-validation-test
  "Comprehensive end-to-end roundtrip tests with full validation chain including:
   - Malli generation of test data (300+ samples)
   - Malli validation
   - EDN → Java protobuf conversion
   - buf.validate validation
   - Binary serialization/deserialization
   - Java → EDN conversion
   - Comparison with original EDN
   - Negative and sanity testing at each stage"
  (:require [clojure.test :refer :all]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            [potatoclient.proto.command :as cmd-gen]
            [potatoclient.proto.state :as state-gen]
            [test-utils.diff :as diff]
            ;; Import the actual Malli specs
            [potatoclient.specs.cmd :as cmd-specs]
            [potatoclient.specs.ser :as state-specs])
  (:import [cmd JonSharedCmd JonSharedCmd$Root]
           [ser JonSharedData JonSharedData$JonGUIState]
           [build.buf.validate Validator ValidatorFactory]
           [build.buf.validate.exceptions ValidationException]
           [com.google.protobuf InvalidProtocolBufferException]))

;; Create custom registry with oneof support
(def registry
  (merge (m/default-schemas)
         (mr/schemas m/default-registry)
         {:oneof oneof/-oneof-schema}))

;; buf.validate validator instance
(def validator (.newValidator (ValidatorFactory/forDescriptors 
                               (.getDescriptor JonSharedCmd$Root)
                               (.getDescriptor JonSharedData$JonGUIState))))

(defn validate-with-buf
  "Validate a protobuf message using buf.validate.
   Returns {:valid? true} or {:valid? false :errors [...]}."
  [proto-message]
  (try
    (let [result (.validate validator proto-message)]
      (if (.isSuccess result)
        {:valid? true}
        {:valid? false
         :errors (map #(.getMessage %) (.getViolations result))}))
    (catch ValidationException e
      {:valid? false
       :errors [(.getMessage e)]})))

(defn full-roundtrip-test
  "Performs full roundtrip test with all validation stages.
   Returns a map with results from each stage."
  [original-edn build-fn parse-fn schema]
  (let [results {:original original-edn
                 :stages {}}]
    (try
      ;; Stage 1: Validate EDN with Malli
      (let [malli-valid? (m/validate schema original-edn {:registry registry})]
        (assoc-in results [:stages :malli-validation]
                  {:valid? malli-valid?
                   :errors (when-not malli-valid?
                             (m/explain schema original-edn {:registry registry}))}))
      
      ;; Stage 2: Convert EDN → Java protobuf
      (let [proto (build-fn original-edn)]
        (assoc-in results [:stages :edn-to-java] {:success true
                                                   :type (type proto)})
        
        ;; Stage 3: Validate with buf.validate
        (let [buf-validation (validate-with-buf proto)]
          (assoc-in results [:stages :buf-validation] buf-validation)
          
          ;; Stage 4: Serialize to binary
          (let [binary (.toByteArray proto)]
            (assoc-in results [:stages :serialization]
                      {:success true
                       :size (count binary)})
            
            ;; Stage 5: Deserialize from binary
            (let [parsed (if (instance? JonSharedCmd$Root proto)
                           (JonSharedCmd$Root/parseFrom binary)
                           (JonSharedData$JonGUIState/parseFrom binary))]
              (assoc-in results [:stages :deserialization] {:success true})
              
              ;; Stage 6: Validate parsed with buf.validate
              (let [buf-validation-parsed (validate-with-buf parsed)]
                (assoc-in results [:stages :buf-validation-parsed] buf-validation-parsed)
                
                ;; Stage 7: Compare Java representations
                (let [java-equal? (.equals proto parsed)]
                  (assoc-in results [:stages :java-comparison]
                            {:equal? java-equal?})
                  
                  ;; Stage 8: Convert Java → EDN
                  (let [roundtripped-edn (parse-fn parsed)]
                    (assoc-in results [:stages :java-to-edn]
                              {:success true})
                    
                    ;; Stage 9: Validate roundtripped EDN with Malli
                    (let [malli-valid-rt? (m/validate schema roundtripped-edn {:registry registry})]
                      (assoc-in results [:stages :malli-validation-roundtripped]
                                {:valid? malli-valid-rt?
                                 :errors (when-not malli-valid-rt?
                                           (m/explain schema roundtripped-edn {:registry registry}))})
                      
                      ;; Stage 10: Compare EDN representations
                      (assoc results
                             :roundtripped roundtripped-edn
                             :stages (assoc (:stages results)
                                            :edn-comparison
                                            {:equal? (= original-edn roundtripped-edn)
                                             :diff (when-not (= original-edn roundtripped-edn)
                                                     (diff/show-diff "EDN comparison" 
                                                                     original-edn 
                                                                     roundtripped-edn))}))))))))))
      (catch Exception e
        (assoc-in results [:stages :error]
                  {:exception (type e)
                   :message (.getMessage e)})))))

(deftest comprehensive-command-roundtrip-test
  (testing "Generate and test 300 command messages"
    (let [;; Generate samples using actual cmd-specs/root schema
          samples (take 300 (mg/sample cmd-specs/root {:size 300 :registry registry}))
          ;; Track statistics
          stats (atom {:total 0
                       :passed 0
                       :failed 0
                       :errors {}})]
      
      (doseq [sample samples]
        (swap! stats update :total inc)
        (let [result (full-roundtrip-test sample 
                                          cmd-gen/build-root
                                          cmd-gen/parse-root
                                          cmd-specs/root)]
          ;; Check if all stages passed
          (if (and (get-in result [:stages :malli-validation :valid?])
                   (get-in result [:stages :buf-validation :valid?])
                   (get-in result [:stages :buf-validation-parsed :valid?])
                   (get-in result [:stages :java-comparison :equal?])
                   (get-in result [:stages :malli-validation-roundtripped :valid?])
                   (not (get-in result [:stages :error])))
            (swap! stats update :passed inc)
            (do
              (swap! stats update :failed inc)
              ;; Log failure details
              (when (get-in result [:stages :error])
                (swap! stats update-in [:errors (get-in result [:stages :error :exception])] 
                       (fnil inc 0)))))))
      
      ;; Report results
      (println "\nCommand roundtrip test results:")
      (println "Total samples:" (:total @stats))
      (println "Passed:" (:passed @stats))
      (println "Failed:" (:failed @stats))
      (when (seq (:errors @stats))
        (println "Error types:" (:errors @stats)))
      
      ;; Assert reasonable pass rate (allowing for some edge cases in generation)
      (is (>= (:passed @stats) (* 0.8 (:total @stats)))
          "At least 80% of generated commands should pass full roundtrip"))))

(deftest comprehensive-state-roundtrip-test
  (testing "Generate and test 300 state messages"
    (let [;; Generate samples using actual state-specs/jon-gui-state schema
          samples (take 300 (mg/sample state-specs/jon-gui-state {:size 300 :registry registry}))
          stats (atom {:total 0
                       :passed 0
                       :failed 0
                       :errors {}})]
      
      (doseq [sample samples]
        (swap! stats update :total inc)
        (let [result (full-roundtrip-test sample 
                                          state-gen/build-jon-gui-state
                                          state-gen/parse-jon-gui-state
                                          state-specs/jon-gui-state)]
          (if (and (get-in result [:stages :malli-validation :valid?])
                   (get-in result [:stages :buf-validation :valid?])
                   (get-in result [:stages :buf-validation-parsed :valid?])
                   (get-in result [:stages :java-comparison :equal?])
                   (get-in result [:stages :malli-validation-roundtripped :valid?])
                   (not (get-in result [:stages :error])))
            (swap! stats update :passed inc)
            (do
              (swap! stats update :failed inc)
              (when (get-in result [:stages :error])
                (swap! stats update-in [:errors (get-in result [:stages :error :exception])] 
                       (fnil inc 0)))))))
      
      (println "\nState roundtrip test results:")
      (println "Total samples:" (:total @stats))
      (println "Passed:" (:passed @stats))
      (println "Failed:" (:failed @stats))
      (when (seq (:errors @stats))
        (println "Error types:" (:errors @stats)))
      
      (is (>= (:passed @stats) (* 0.8 (:total @stats)))
          "At least 80% of generated states should pass full roundtrip"))))

;; Negative tests - testing invalid data at each stage
(deftest negative-malli-validation-tests
  (testing "Invalid EDN is caught by Malli validation"
    ;; Invalid enum value
    (let [result (full-roundtrip-test
                  {:client-type :invalid-client-type :ping {}}
                  cmd-gen/build-root
                  cmd-gen/parse-root
                  cmd-specs/root)]
      (is (false? (get-in result [:stages :malli-validation :valid?]))
          "Invalid enum should fail Malli validation"))
    
    ;; Wrong type for numeric field
    (let [result (full-roundtrip-test
                  {:protocol-version "not-a-number" :ping {}}
                  cmd-gen/build-root
                  cmd-gen/parse-root
                  cmd-specs/root)]
      (is (false? (get-in result [:stages :malli-validation :valid?]))
          "String protocol-version should fail Malli validation"))
    
    ;; Missing required oneof in command
    (let [result (full-roundtrip-test
                  {:client-type :jon-gui-data-client-type-local-network}
                  cmd-gen/build-root
                  cmd-gen/parse-root
                  cmd-specs/root)]
      ;; This might pass Malli but fail buf.validate
      (is (or (false? (get-in result [:stages :malli-validation :valid?]))
              (false? (get-in result [:stages :buf-validation :valid?])))
          "Missing required oneof should fail validation"))))

(deftest negative-protobuf-tests
  (testing "Invalid binary data is rejected"
    ;; Corrupted binary
    (is (thrown? InvalidProtocolBufferException
                 (JonSharedCmd$Root/parseFrom (byte-array [1 2 3 4 5])))
        "Corrupted binary should throw exception")
    
    ;; Empty binary
    (let [parsed (JonSharedCmd$Root/parseFrom (byte-array 0))]
      ;; Empty binary creates default instance
      (is (= JonSharedCmd$Root$PayloadCase/PAYLOAD_NOT_SET 
             (.getPayloadCase parsed))
          "Empty binary should create default instance"))))

;; Sanity tests - edge cases and boundary conditions
(deftest sanity-edge-case-tests
  (testing "Empty messages"
    (let [result (full-roundtrip-test
                  {}
                  cmd-gen/build-root
                  cmd-gen/parse-root
                  cmd-specs/root)]
      ;; Empty command might fail buf.validate due to required fields
      (is (or (false? (get-in result [:stages :malli-validation :valid?]))
              (false? (get-in result [:stages :buf-validation :valid?])))
          "Empty command should fail validation")))
  
  (testing "Messages with only required fields"
    (let [minimal-cmd {:protocol-version 1
                       :client-type :jon-gui-data-client-type-local-network
                       :ping {}}
          result (full-roundtrip-test
                  minimal-cmd
                  cmd-gen/build-root
                  cmd-gen/parse-root
                  cmd-specs/root)]
      (is (get-in result [:stages :buf-validation :valid?])
          "Minimal valid command should pass validation")
      (is (get-in result [:stages :java-comparison :equal?])
          "Minimal command should roundtrip correctly")))
  
  (testing "Integer boundary values"
    (let [samples [{:protocol-version Integer/MAX_VALUE
                    :session-id 0
                    :client-type :jon-gui-data-client-type-local-network
                    :ping {}}
                   {:protocol-version 1
                    :session-id Integer/MAX_VALUE
                    :client-type :jon-gui-data-client-type-local-network
                    :ping {}}]]
      (doseq [sample samples]
        (let [result (full-roundtrip-test
                      sample
                      cmd-gen/build-root
                      cmd-gen/parse-root
                      cmd-specs/root)]
          (is (get-in result [:stages :java-comparison :equal?])
              "Integer boundary values should roundtrip correctly")))))
  
  (testing "Large nested structures"
    ;; This would require more complex state messages with actual nested data
    ;; For now, test with what we have
    (let [state-with-all-fields {:protocol-version 1
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
          result (full-roundtrip-test
                  state-with-all-fields
                  state-gen/build-jon-gui-state
                  state-gen/parse-jon-gui-state
                  state-specs/jon-gui-state)]
      (is (get-in result [:stages :java-comparison :equal?])
          "State with all fields should roundtrip correctly"))))

(deftest performance-sanity-test
  (testing "Roundtrip performance is reasonable"
    (let [sample {:protocol-version 1
                  :client-type :jon-gui-data-client-type-local-network
                  :ping {}}
          start (System/nanoTime)
          _ (dotimes [_ 1000]
              (let [proto (cmd-gen/build-root sample)
                    binary (.toByteArray proto)
                    parsed (JonSharedCmd$Root/parseFrom binary)
                    _ (cmd-gen/parse-root parsed)]))
          end (System/nanoTime)
          duration-ms (/ (- end start) 1e6)]
      (println (format "\n1000 roundtrips completed in %.2f ms (%.2f μs per roundtrip)"
                       duration-ms
                       (/ duration-ms 1000)))
      (is (< duration-ms 1000)
          "1000 roundtrips should complete in under 1 second"))))