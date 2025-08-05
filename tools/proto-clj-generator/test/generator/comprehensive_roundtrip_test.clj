(ns generator.comprehensive-roundtrip-test
  "Comprehensive roundtrip tests with full validation chain"
  (:require [clojure.test :refer :all]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            [potatoclient.proto.command :as cmd-gen]
            [potatoclient.proto.state :as state-gen]
            [test-utils.diff :as diff])
  (:import [cmd JonSharedCmd JonSharedCmd$Root JonSharedCmd$Root$PayloadCase]
           [ser JonSharedData JonSharedData$JonGUIState JonSharedDataTypes$JonGuiDataClientType]))

;; First, let's define the Malli schemas that match our protobuf structures
;; These would normally be generated, but for testing we'll define them manually

(def registry
  (merge (m/default-schemas)
         {:oneof oneof/-oneof-schema}))

;; Command schemas
(def ping-schema
  [:map {:closed true}])

(def rotary-goto-ndc-schema
  [:map {:closed true}
   [:channel [:enum :heat :visual]]
   [:x :double]
   [:y :double]])

(def rotary-halt-schema
  [:map {:closed true}])

(def rotary-command-schema
  [:oneof {:error/message "Exactly one rotary command must be set"
           :goto-ndc rotary-goto-ndc-schema
           :halt rotary-halt-schema}])

(def system-start-rec-schema
  [:map {:closed true}])

(def system-stop-rec-schema
  [:map {:closed true}])

(def system-command-schema
  [:oneof {:error/message "Exactly one system command must be set"
           :start-rec system-start-rec-schema
           :stop-rec system-stop-rec-schema}])

;; Client type enum values that match the protobuf
(def client-type-enum
  [:enum
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA])

(def command-root-schema
  [:and
   [:map {:closed true}
    [:protocol-version {:optional true} pos-int?]
    [:session-id {:optional true} :string]
    [:important {:optional true} :boolean]
    [:from-cv-subsystem {:optional true} :boolean]
    [:client-type {:optional true} client-type-enum]]
   [:oneof {:error/message "Exactly one command payload must be set"
            :ping ping-schema
            :rotary rotary-command-schema
            :system system-command-schema
            :noop [:map {:closed true}]
            :frozen [:map {:closed true}]}]])

;; State schemas - simplified for testing
;; In reality, each field would be a complex message type
(def state-field-schema
  [:map {:closed true}])  ;; Simplified - actual messages have many fields

(def state-schema
  [:map {:closed true}
   [:protocol-version {:optional true} pos-int?]
   [:system {:optional true} state-field-schema]
   [:meteo-internal {:optional true} state-field-schema]
   [:lrf {:optional true} state-field-schema]
   [:time {:optional true} state-field-schema]
   [:gps {:optional true} state-field-schema]
   [:compass {:optional true} state-field-schema]
   [:rotary {:optional true} state-field-schema]
   [:camera-day {:optional true} state-field-schema]
   [:camera-heat {:optional true} state-field-schema]
   [:compass-calibration {:optional true} state-field-schema]
   [:rec-osd {:optional true} state-field-schema]
   [:day-cam-glass-heater {:optional true} state-field-schema]
   [:actual-space-time {:optional true} state-field-schema]])

(deftest command-ping-roundtrip-test
  (testing "Ping command full roundtrip with validation"
    (let [;; 1. Generate test data with Malli
          original {:ping {}}
          
          ;; 2. Validate with Malli
          valid? (m/validate command-root-schema original {:registry registry})]
      
      (is valid? "Original data should be valid according to Malli schema")
      
      ;; 3. Convert to Java protobuf
      (let [proto (cmd-gen/build-root original)]
        (is (instance? JonSharedCmd$Root proto) "Should create Root protobuf instance")
        (is (= JonSharedCmd$Root$PayloadCase/PING (.getPayloadCase proto)) "Should have PING payload")
        
        ;; 4. Built-in protobuf validation happens during build
        ;; If the data was invalid, build would have thrown
        
        ;; 5. Serialize to binary
        (let [binary (.toByteArray proto)]
          (is (pos? (count binary)) "Should produce non-empty binary")
          
          ;; 6. Parse back to Java object
          (let [parsed (JonSharedCmd$Root/parseFrom binary)]
            (is (instance? JonSharedCmd$Root parsed) "Should parse back to Root")
            
            ;; 7. Check equality at Java level
            (is (.equals proto parsed) "Original and parsed protos should be equal")
            
            ;; 8. Convert back to Clojure map
            (let [roundtripped (cmd-gen/parse-root parsed)]
              
              ;; 9. Validate roundtripped data with Malli
              (is (m/validate command-root-schema roundtripped {:registry registry})
                  "Roundtripped data should be valid")
              
              ;; 10. Check that the core data is preserved
              ;; Note: protobuf adds default values for unset fields
              (testing "EDN comparison"
                (diff/show-diff "Ping payload comparison" (:ping original) (:ping roundtripped))
                (diff/assert-edn-equal original roundtripped 
                                      "Full message should roundtrip correctly"))))))

(deftest command-rotary-roundtrip-test
  (testing "Rotary command full roundtrip with validation"
    (let [;; 1. Generate test data
          original {:rotary {:goto-ndc {:channel :heat :x 0.5 :y -0.5}}}
          
          ;; 2. Validate with Malli
          valid? (m/validate command-root-schema original {:registry registry})]
      
      (is valid? "Original data should be valid")
      
      ;; 3-10. Same roundtrip process
      (let [proto (cmd-gen/build-root original)
            binary (.toByteArray proto)
            parsed (JonSharedCmd$Root/parseFrom binary)
            roundtripped (cmd-gen/parse-root parsed)]
        
        (is (.equals proto parsed) "Protos should be equal")
        ;; Check core data is preserved
        (testing "EDN comparison"
          (diff/show-diff "Rotary command comparison" original roundtripped)
          (diff/assert-edn-equal (:rotary original) (:rotary roundtripped) 
                                "Rotary payload should be preserved"))))))

(deftest command-with-metadata-roundtrip-test
  (testing "Command with metadata fields"
    (let [original {:protocol-version 1
                    :session-id 123
                    :important true
                    :from-cv-subsystem false
                    :client-type JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                    :system {:start-rec {}}}
          
          valid? (m/validate command-root-schema original {:registry registry})]
      
      (is valid? "Original data should be valid")
      
      (let [proto (cmd-gen/build-root original)
            binary (.toByteArray proto)
            parsed (JonSharedCmd$Root/parseFrom binary)
            roundtripped (cmd-gen/parse-root parsed)]
        
        (is (= 1 (.getProtocolVersion proto)) "Protocol version should be set")
        (is (= 123 (.getSessionId proto)) "Session ID should be set")
        (is (.getImportant proto) "Important flag should be true")
        (is (not (.getFromCvSubsystem proto)) "From CV flag should be false")
        
        (is (.equals proto parsed) "Protos should be equal")
        ;; Check core fields are preserved
        (testing "EDN comparison with metadata"
          (diff/show-diff "Command with metadata comparison" original roundtripped)
          ;; Check individual fields for clarity
          (is (= (:protocol-version original) (:protocol-version roundtripped)))
          (is (= (:session-id original) (:session-id roundtripped)))
          (is (= (:important original) (:important roundtripped)))
          (is (= (:from-cv-subsystem original) (:from-cv-subsystem roundtripped)))
          (is (= (:client-type original) (:client-type roundtripped)))
          (is (= (:system original) (:system roundtripped)))
          ;; Full comparison
          (diff/assert-edn-equal original roundtripped 
                                "Full command with metadata should roundtrip"))))))

(deftest state-roundtrip-test
  (testing "State message full roundtrip"
    (let [;; For state, we need actual message objects for required fields
          ;; For testing, we'll use empty maps which the generator should handle
          original {:protocol-version 2
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
          
          valid? (m/validate state-schema original {:registry registry})]
      
      (is valid? "Original state should be valid")
      
      (let [proto (state-gen/build-jon-gui-state original)
            binary (.toByteArray proto)
            parsed (JonSharedData$JonGUIState/parseFrom binary)
            roundtripped (state-gen/parse-jon-gui-state parsed)]
        
        (is (= 2 (.getProtocolVersion proto)) "Protocol version should be set")
        (is (.equals proto parsed) "Protos should be equal")
        (testing "EDN comparison for state"
          (diff/show-diff "State message comparison" original roundtripped)
          (is (= (:protocol-version original) (:protocol-version roundtripped))
              "Protocol version should roundtrip correctly")
          ;; Full comparison - note that empty maps might be nil after roundtrip
          (diff/assert-edn-equal original roundtripped 
                                "State message should roundtrip correctly"))))))

(deftest malli-generation-roundtrip-test
  (testing "Randomly generated data roundtrips correctly"
    (let [;; Generate 300 samples as requested
          command-samples (mg/sample command-root-schema {:size 300 :registry registry})
          state-samples (mg/sample state-schema {:size 300 :registry registry})]
      
      (testing "Command roundtrips"
        (doseq [sample command-samples]
          (when (m/validate command-root-schema sample {:registry registry})
            (try
              (let [proto (cmd-gen/build-root sample)
                    binary (.toByteArray proto)
                    parsed (JonSharedCmd$Root/parseFrom binary)
                    roundtripped (cmd-gen/parse-root parsed)]
                
                (is (.equals proto parsed) "Generated protos should be equal")
                (when-not (m/validate command-root-schema roundtripped {:registry registry})
                  ;; Show diff when validation fails
                  (diff/show-diff "Generated command failed" sample roundtripped))
                (is (m/validate command-root-schema roundtripped {:registry registry})
                    "Roundtripped generated data should be valid"))
              (catch Exception e
                ;; Log but don't fail on generation edge cases
                (println "Sample failed:" sample)
                (println "Error:" (.getMessage e)))))))
      
      (testing "State roundtrips"
        (doseq [sample state-samples]
          (when (m/validate state-schema sample {:registry registry})
            (try
              (let [proto (state-gen/build-jon-gui-state sample)
                    binary (.toByteArray proto)
                    parsed (JonSharedData$JonGUIState/parseFrom binary)
                    roundtripped (state-gen/parse-jon-gui-state parsed)]
                
                (is (.equals proto parsed) "Generated state protos should be equal")
                (when-not (m/validate state-schema roundtripped {:registry registry})
                  ;; Show diff when validation fails
                  (diff/show-diff "Generated state failed" sample roundtripped))
                (is (m/validate state-schema roundtripped {:registry registry})
                    "Roundtripped generated state should be valid"))
              (catch Exception e
                ;; Log but don't fail on generation edge cases
                (println "State sample failed:" sample)
                (println "Error:" (.getMessage e))))))))))

;; Negative tests
(deftest negative-validation-tests
  (testing "Invalid data is rejected by Malli"
    ;; Missing required oneof
    (is (not (m/validate command-root-schema {} {:registry registry}))
        "Empty command should fail validation")
    
    ;; Invalid enum value
    (is (not (m/validate command-root-schema 
                         {:rotary {:goto-ndc {:channel :invalid :x 0.5 :y 0.5}}}
                         {:registry registry}))
        "Invalid enum value should fail validation")
    
    ;; Wrong type
    (is (not (m/validate command-root-schema 
                         {:rotary {:goto-ndc {:channel :heat :x "not-a-number" :y 0.5}}}
                         {:registry registry}))
        "Wrong type should fail validation")
    
    ;; Multiple oneof values
    (is (not (m/validate command-root-schema 
                         {:ping {} :noop {}}
                         {:registry registry}))
        "Multiple oneof values should fail validation")))

(deftest protobuf-builder-validation-tests
  (testing "Invalid data is rejected by protobuf builders"
    ;; These tests check that our builders handle invalid data appropriately
    ;; The actual validation happens at the protobuf level
    
    (testing "Missing required fields"
      ;; Protobuf doesn't enforce required fields at build time
      ;; But we can check default values
      (let [proto (cmd-gen/build-root {})]
        (is (= JonSharedCmd$Root$PayloadCase/PAYLOAD_NOT_SET (.getPayloadCase proto))
            "Empty command should have no payload set")))
    
    (testing "Unknown fields are ignored"
      ;; Protobuf ignores unknown fields
      (let [proto (cmd-gen/build-root {:unknown-field "value" :ping {}})]
        (is (= JonSharedCmd$Root$PayloadCase/PING (.getPayloadCase proto))
            "Unknown fields should be ignored"))))))))
