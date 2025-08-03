(ns potatoclient.transit.metadata-command-test
  "Tests for the metadata-based command architecture"
  (:require [clojure.test :refer [deftest testing is]]
            [potatoclient.transit.command-sender :as sender]
            [potatoclient.transit.core :as transit]
            [clojure.walk :as walk]))

;; =============================================================================
;; Test Helpers
;; =============================================================================

(defn extract-metadata
  "Extract metadata from a value if it has any"
  [v]
  (meta v))

(defn has-proto-metadata?
  "Check if a value has protobuf metadata"
  [v]
  (let [m (meta v)]
    (and (contains? m :proto-type)
         (contains? m :proto-path))))

;; =============================================================================
;; Basic Metadata Tests
;; =============================================================================

(deftest test-metadata-attachment
  (testing "Commands have protobuf metadata attached"
    (let [msg (sender/send-command {:ping {}} [:root :ping])
          payload (:payload msg)]
      (is (has-proto-metadata? payload))
      (is (= "cmd.JonSharedCmd$Root" (:proto-type (meta payload))))
      (is (= [:root :ping] (:proto-path (meta payload)))))))

(deftest test-different-contexts-same-names
  (testing "Same field names in different contexts get different metadata"
    ;; Both have 'start' but different proto types
    (let [rotary-msg (sender/send-command {:start {}} [:rotary-platform :start])
          rotary-meta (meta (:payload rotary-msg))]
      (is (= "cmd.JonSharedCmdRotaryPlatform$Root" (:proto-type rotary-meta)))
      (is (= [:rotary-platform :start] (:proto-path rotary-meta))))
    
    ;; If we had video recording commands
    ;; (let [video-msg (sender/send-command {:start {}} [:video-recording :start])
    ;;       video-meta (meta (:payload video-msg))]
    ;;   (is (= "cmd.JonSharedCmdVideoRecording$Root" (:proto-type video-meta)))
    ;;   (is (= [:video-recording :start] (:proto-path video-meta))))
    ))

(deftest test-nested-command-structure
  (testing "Nested commands preserve structure with metadata"
    (let [msg (sender/send-command 
               {:goto {:azimuth 123.45 :elevation -15.0}}
               [:rotary-platform :goto])
          payload (:payload msg)]
      (is (= {:goto {:azimuth 123.45 :elevation -15.0}} payload))
      (is (has-proto-metadata? payload)))))

(deftest test-deeply-nested-paths
  (testing "Deeply nested paths work correctly"
    (let [msg (sender/send-command 
               {:day {:set {:x-offset 10 :y-offset -5}}}
               [:lrf-calib :day :set])
          meta-data (meta (:payload msg))]
      (is (= [:lrf-calib :day :set] (:proto-path meta-data)))
      (is (string? (:proto-type meta-data))))))

;; =============================================================================
;; Transit Serialization Tests
;; =============================================================================

(deftest test-metadata-survives-transit
  (testing "Metadata is preserved through Transit serialization"
    ;; Note: Standard Transit doesn't preserve Clojure metadata
    ;; We need custom handlers or a different approach
    ;; This test documents the limitation
    
    (let [original {:goto {:azimuth 180.0 :elevation 45.0}}
          with-meta (with-meta original 
                      {:proto-type "cmd.JonSharedCmdRotaryPlatform$Root"
                       :proto-path [:rotary-platform :goto]})
          ;; Standard Transit round-trip
          encoded (transit/encode with-meta)
          decoded (transit/decode encoded)]
      
      ;; Standard Transit loses metadata
      (is (nil? (meta decoded)))
      
      ;; This is why we need to embed metadata in the message
      ;; or use custom Transit handlers
      )))

(deftest test-embedded-metadata-approach
  (testing "Embedding metadata in the message structure"
    ;; Alternative approach: embed metadata as data
    (let [command {:goto {:azimuth 180.0 :elevation 45.0}}
          with-embedded-meta {:^meta {:proto-type "cmd.JonSharedCmdRotaryPlatform$Root"
                                      :proto-path [:rotary-platform :goto]}
                              :data command}
          encoded (transit/encode with-embedded-meta)
          decoded (transit/decode encoded)]
      
      ;; Metadata survives as data
      (is (= {:proto-type "cmd.JonSharedCmdRotaryPlatform$Root"
              :proto-path [:rotary-platform :goto]}
             (:^meta decoded)))
      (is (= command (:data decoded))))))

;; =============================================================================
;; Validation Tests
;; =============================================================================

(deftest test-unknown-type-path
  (testing "Unknown type paths throw errors"
    (is (thrown? Exception
                 (sender/send-command {:foo {}} [:unknown :path])))))

(deftest test-validation-with-metadata
  (testing "Commands are validated before metadata attachment"
    ;; Invalid azimuth (assuming > 360 is invalid)
    (is (thrown-with-msg? Exception #"validation failed"
                          (sender/send-command 
                           {:goto {:azimuth 400.0 :elevation 45.0}}
                           [:rotary-platform :goto])))))

;; =============================================================================
;; Convenience Function Tests
;; =============================================================================

(deftest test-convenience-functions
  (testing "Convenience functions produce correct metadata"
    (let [msg (sender/rotary-goto 180.0 45.0)
          payload (:payload msg)
          meta-data (meta payload)]
      (is (= {:goto {:azimuth 180.0 :elevation 45.0}} payload))
      (is (= [:rotary-platform :goto] (:proto-path meta-data))))))

;; =============================================================================
;; Type Registry Tests
;; =============================================================================

(deftest test-type-registry-coverage
  (testing "All registered paths have valid proto types"
    (doseq [[path proto-class] sender/proto-type-registry]
      (is (vector? path))
      (is (string? proto-class))
      (is (re-matches #"^cmd\.[A-Za-z$]+.*" proto-class)
          (str "Invalid proto class format: " proto-class)))))

;; =============================================================================
;; Integration Simulation
;; =============================================================================

(deftest test-command-flow-simulation
  (testing "Simulate full command flow"
    (let [;; 1. Create command with metadata
          cmd-msg (sender/send-command 
                   {:goto {:azimuth 90.0 :elevation 30.0}}
                   [:rotary-platform :goto])
          
          ;; 2. Extract payload with metadata
          payload (:payload cmd-msg)
          meta-data (meta payload)
          
          ;; 3. Simulate what Kotlin handler would do
          proto-type (:proto-type meta-data)
          proto-path (:proto-path meta-data)]
      
      ;; Verify structure
      (is (= "command" (:msg-type cmd-msg)))
      (is (= {:goto {:azimuth 90.0 :elevation 30.0}} payload))
      
      ;; Verify metadata
      (is (= "cmd.JonSharedCmdRotaryPlatform$Root" proto-type))
      (is (= [:rotary-platform :goto] proto-path))
      
      ;; In real implementation, Kotlin would:
      ;; 1. Read the metadata
      ;; 2. Use reflection to instantiate cmd.JonSharedCmdRotaryPlatform$Root
      ;; 3. Populate the goto field with the data
      ;; 4. Send the protobuf to the server
      )))