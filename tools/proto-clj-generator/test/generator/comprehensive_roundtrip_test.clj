(ns generator.comprehensive-roundtrip-test
  "Comprehensive roundtrip tests with full validation chain"
  (:require [clojure.test :refer :all]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]))

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
  [:oneof {:error/message "Exactly one rotary command must be set"}
   [:goto-ndc rotary-goto-ndc-schema]
   [:halt rotary-halt-schema]])

(def system-start-rec-schema
  [:map {:closed true}])

(def system-stop-rec-schema
  [:map {:closed true}])

(def system-command-schema
  [:oneof {:error/message "Exactly one system command must be set"}
   [:start-rec system-start-rec-schema]
   [:stop-rec system-stop-rec-schema]])

(def command-root-schema
  [:map {:closed true}
   [:protocol-version {:optional true} :int]
   [:session-id {:optional true} :string]
   [:important {:optional true} :boolean]
   [:from-cv-subsystem {:optional true} :boolean]
   [:client-type {:optional true} [:enum :gui :cv]]
   ;; The oneof payload
   [:oneof {:error/message "Exactly one command payload must be set"}
    [:ping ping-schema]
    [:rotary rotary-command-schema]
    [:system system-command-schema]
    [:noop [:map {:closed true}]]
    [:frozen [:map {:closed true}]]]])

;; State schemas
(def state-schema
  [:map {:closed true}
   [:protocol-version {:optional true} :int]
   [:system {:optional true} :any] ;; Simplified for testing
   [:meteo-internal {:optional true} :any]
   [:lrf {:optional true} :any]
   [:time {:optional true} :any]
   [:gps {:optional true} :any]
   [:compass {:optional true} :any]
   [:rotary {:optional true} :any]
   [:camera-day {:optional true} :any]
   [:camera-heat {:optional true} :any]
   [:compass-calibration {:optional true} :any]
   [:rec-osd {:optional true} :any]
   [:day-cam-glass-heater {:optional true} :any]
   [:actual-space-time {:optional true} :any]])

;; Helper to check if we can load the generated namespaces
(defn can-load-generated-code? []
  (try
    (require '[potatoclient.proto.command :as cmd-gen])
    (require '[potatoclient.proto.state :as state-gen])
    true
    (catch Exception e
      false)))

(deftest generated-code-availability-test
  (testing "Generated code can be loaded"
    (is (can-load-generated-code?)
        "Generated protobuf converters must be available. Run 'clojure -M:gen' first.")))

;; Tests that require generated code
(when (can-load-generated-code?)
  (require '[potatoclient.proto.command :as cmd-gen])
  (require '[potatoclient.proto.state :as state-gen])
  
  ;; Import the Java protobuf classes
  (import '[cmd JonSharedCmd$Root]
          '[ser JonSharedData$JonGUIState])
  
  (deftest command-ping-roundtrip-test
    (testing "Ping command full roundtrip with validation"
      (let [;; 1. Generate test data with Malli
            original {:ping {}}
            
            ;; 2. Validate with Malli
            valid? (m/validate command-root-schema original {:registry registry})]
        
        (is valid? "Original data should be valid according to Malli schema")
        
        ;; 3. Convert to Java protobuf
        (let [proto (cmd-gen/build-root original)]
          (is (instance? cmd.JonSharedCmd$Root proto) "Should create Root protobuf instance")
          (is (= cmd.JonSharedCmd$Root$PayloadCase/PING (.getPayloadCase proto)) "Should have PING payload")
          
          ;; 4. Built-in protobuf validation happens during build
          ;; If the data was invalid, build would have thrown
          
          ;; 5. Serialize to binary
          (let [binary (.toByteArray proto)]
            (is (pos? (count binary)) "Should produce non-empty binary")
            
            ;; 6. Parse back to Java object
            (let [parsed (cmd.JonSharedCmd$Root/parseFrom binary)]
              (is (instance? cmd.JonSharedCmd$Root parsed) "Should parse back to Root")
              
              ;; 7. Check equality at Java level
              (is (.equals proto parsed) "Original and parsed protos should be equal")
              
              ;; 8. Convert back to Clojure map
              (let [roundtripped (cmd-gen/parse-root parsed)]
                
                ;; 9. Validate roundtripped data with Malli
                (is (m/validate command-root-schema roundtripped {:registry registry})
                    "Roundtripped data should be valid")
                
                ;; 10. Check Clojure-level equality
                (is (= original roundtripped) "Original and roundtripped should be equal"))))))))
  
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
              parsed (Root/parseFrom binary)
              roundtripped (cmd-gen/parse-root parsed)]
          
          (is (.equals proto parsed) "Protos should be equal")
          (is (= original roundtripped) "Data should roundtrip correctly")))))
  
  (deftest command-with-metadata-roundtrip-test
    (testing "Command with metadata fields"
      (let [original {:protocol-version 1
                      :session-id "test-123"
                      :important true
                      :from-cv-subsystem false
                      :client-type :gui
                      :system {:start-rec {}}}
            
            valid? (m/validate command-root-schema original {:registry registry})]
        
        (is valid? "Original data should be valid")
        
        (let [proto (cmd-gen/build-root original)
              binary (.toByteArray proto)
              parsed (Root/parseFrom binary)
              roundtripped (cmd-gen/parse-root parsed)]
          
          (is (= 1 (.getProtocolVersion proto)) "Protocol version should be set")
          (is (= "test-123" (.getSessionId proto)) "Session ID should be set")
          (is (.getImportant proto) "Important flag should be true")
          (is (not (.getFromCvSubsystem proto)) "From CV flag should be false")
          
          (is (.equals proto parsed) "Protos should be equal")
          (is (= original roundtripped) "Data should roundtrip correctly")))))
  
  (deftest state-roundtrip-test
    (testing "State message full roundtrip"
      (let [original {:protocol-version 2
                      :system "system-data"
                      :meteo-internal "meteo-data"}
            
            valid? (m/validate state-schema original {:registry registry})]
        
        (is valid? "Original state should be valid")
        
        (let [proto (state-gen/build-jon-gui-state original)
              binary (.toByteArray proto)
              parsed (ser.JonSharedData$JonGUIState/parseFrom binary)
              roundtripped (state-gen/parse-jon-gui-state parsed)]
          
          (is (instance? ser.JonSharedData$JonGUIState proto) "Should create JonGUIState instance")
          (is (.equals proto parsed) "State protos should be equal")
          (is (= (:protocol-version original) (:protocol-version roundtripped)))
          (is (= (:system original) (:system roundtripped)))
          (is (= (:meteo-internal original) (:meteo-internal roundtripped)))))))
  
  ;; Property-based testing with generators
  (deftest command-property-test
    (testing "Generated commands roundtrip correctly"
      (let [generator (mg/generator command-root-schema {:registry registry})
            samples (mg/sample generator {:size 20})]
        
        (doseq [sample samples]
          (testing (str "Sample: " (pr-str sample))
            (is (m/validate command-root-schema sample {:registry registry})
                "Generated sample should be valid")
            
            (let [proto (cmd-gen/build-root sample)
                  binary (.toByteArray proto)
                  parsed (Root/parseFrom binary)
                  roundtripped (cmd-gen/parse-root parsed)]
              
              (is (.equals proto parsed) "Protos should be equal")
              (is (= sample roundtripped) "Sample should roundtrip correctly")))))))
  
  ;; Negative tests
  (deftest negative-validation-tests
    (testing "Invalid data is rejected"
      
      (testing "Multiple oneof fields set"
        (let [invalid {:ping {} :rotary {:halt {}}}]
          (is (not (m/validate command-root-schema invalid {:registry registry}))
              "Should reject multiple payload fields")))
      
      (testing "No oneof field set"
        (let [invalid {:protocol-version 1}]
          (is (not (m/validate command-root-schema invalid {:registry registry}))
              "Should reject missing payload")))
      
      (testing "Invalid enum value"
        (let [invalid {:rotary {:goto-ndc {:channel :invalid :x 0.0 :y 0.0}}}]
          (is (not (m/validate command-root-schema invalid {:registry registry}))
              "Should reject invalid enum value")))
      
      (testing "Missing required field"
        (let [invalid {:rotary {:goto-ndc {:channel :heat}}}] ; missing x and y
          (is (not (m/validate command-root-schema invalid {:registry registry}))
              "Should reject missing required fields")))
      
      (testing "Wrong type"
        (let [invalid {:rotary {:goto-ndc {:channel :heat :x "not-a-number" :y 0.0}}}]
          (is (not (m/validate command-root-schema invalid {:registry registry}))
              "Should reject wrong types")))
      
      (testing "Extra fields"
        (let [invalid {:ping {} :extra-field "should-not-be-here"}]
          (is (not (m/validate command-root-schema invalid {:registry registry}))
              "Should reject extra fields with closed map"))))))

;; Test runner feedback
(deftest ^:always test-summary
  (testing "Test environment"
    (println "\n=== Proto-CLJ Generator Test Summary ===")
    (println "Generated code available:" (can-load-generated-code?))
    (when (can-load-generated-code?)
      (println "✓ Command converters loaded")
      (println "✓ State converters loaded")
      (println "✓ Java protobuf classes available"))
    (println "======================================\n")))