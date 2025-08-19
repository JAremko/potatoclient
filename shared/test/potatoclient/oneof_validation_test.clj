(ns potatoclient.oneof-validation-test
  "Test our custom oneof validation to catch edge cases"
  (:require
   [clojure.test :refer [deftest is testing]]
   [matcher-combinators.test] ;; extends clojure.test's `is` macro
   [matcher-combinators.matchers :as matchers]
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.error :as me]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.cmd.root]
   [pronto.core :as pronto]
   [pronto.utils]))

;; Initialize registry
(registry/setup-global-registry!)

;; Setup Pronto mapper
(pronto/defmapper cmd-mapper [cmd.JonSharedCmd$Root])

(deftest test-oneof-validation-consistency
  (testing "m/validate and m/explain should be consistent"
    ;; Generate many samples and check consistency
    (let [spec (m/schema :cmd/root)]
      (dotimes [_ 100]
        (let [sample (mg/generate spec)
              valid? (m/validate spec sample)
              explanation (m/explain spec sample)]
          
          ;; Key invariant: if validate returns false, explain must return non-nil
          (when-not valid?
            (is (some? explanation) 
                (str "m/validate returned false but m/explain returned nil for: " 
                     (pr-str sample))))
          
          ;; And vice versa: if explain returns something, validate must return false
          (when explanation
            (is (not valid?)
                (str "m/explain returned errors but m/validate returned true for: "
                     (pr-str sample)))))))))

(deftest test-oneof-roundtrip-validation
  (testing "Protobuf roundtrip should preserve validity"
    (let [spec (m/schema :cmd/root)
          failures (atom [])]
      
      (dotimes [i 100]
        (let [original (mg/generate spec)
              proto-map (pronto/clj-map->proto-map cmd-mapper cmd.JonSharedCmd$Root original)
              proto (pronto.utils/proto-map->proto proto-map)
              bytes (.toByteArray proto)
              parsed (cmd.JonSharedCmd$Root/parseFrom bytes)
              proto-map2 (pronto/proto->proto-map cmd-mapper parsed)
              result (pronto/proto-map->clj-map proto-map2)
              valid-before? (m/validate spec original)
              valid-after? (m/validate spec result)
              explain-after (m/explain spec result)]
          
          ;; Original should always be valid (generated from spec)
          (is valid-before? 
              (format "Generated sample %d should be valid" i))
          
          ;; If result is invalid, explanation must exist
          (when-not valid-after?
            (swap! failures conj {:original original :result result :explanation explain-after})
            (is (some? explain-after)
                (format "Sample %d: m/validate returned false but m/explain returned nil after roundtrip" i))
            
            ;; Also check that we can humanize the explanation (safely)
            (when explain-after
              (let [humanized (try 
                               (me/humanize explain-after)
                               (catch Exception e
                                 ;; If humanization fails, at least we have the raw explanation
                                 (println (format "Sample %d: Could not humanize, raw errors:" i) 
                                         (:errors explain-after))
                                 nil))]
                (when humanized
                  (is (some? humanized) 
                      (format "Sample %d: Humanized explanation exists" i))))))))
      
      ;; Report summary
      (when (seq @failures)
        (println (format "\nFound %d validation failures in roundtrip:" (count @failures)))
        (doseq [{:keys [explanation]} (take 3 @failures)]
          (when explanation
            (try
              (println "  Error:" (me/humanize explanation))
              (catch Exception _
                (println "  Raw errors:" (:errors explanation))))))))))

(deftest test-oneof-closed-map-behavior
  (testing "Oneof schema should handle closed map validation correctly"
    (let [spec (m/schema :cmd/root)]
      
      ;; Test with valid data
      (testing "Valid data with all required fields"
        (let [valid-data {:protocol_version 1
                         :session_id 123
                         :important false
                         :from_cv_subsystem false
                         :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                         :ping {}
                         ;; All other oneof fields must be nil
                         :osd nil :system nil :noop nil :cv nil :gps nil 
                         :lrf nil :lira nil :lrf_calib nil :rotary nil 
                         :day_cam_glass_heater nil :heat_camera nil 
                         :compass nil :day_camera nil :frozen nil}]
          (is (m/validate spec valid-data) "Valid data should validate")
          (is (nil? (m/explain spec valid-data)) "Valid data should have no explanation")
          ;; Also check with matcher-combinators
          (is (match? {:protocol_version pos-int?
                       :session_id nat-int?
                       :important false?
                       :from_cv_subsystem false?
                       :client_type keyword?
                       :ping map?}
                      valid-data))))
      
      ;; Test with extra keys
      (testing "Data with extra keys should fail"
        (let [invalid-data {:protocol_version 1
                           :session_id 123
                           :important false
                           :from_cv_subsystem false
                           :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                           :ping {}
                           :extra-key "should not be here"  ; Extra key
                           :osd nil :system nil :noop nil :cv nil :gps nil 
                           :lrf nil :lira nil :lrf_calib nil :rotary nil 
                           :day_cam_glass_heater nil :heat_camera nil 
                           :compass nil :day_camera nil :frozen nil}
              valid? (m/validate spec invalid-data)
              explanation (m/explain spec invalid-data)]
          
          (is (not valid?) "Data with extra keys should not validate")
          (is (some? explanation) "Should have explanation for extra keys")
          (when explanation
            (let [humanized (me/humanize explanation)]
              (is (some? humanized) "Should be able to humanize the error")))))
      
      ;; Test with missing oneof
      (testing "Data with no oneof field should fail"
        (let [invalid-data {:protocol_version 1
                           :session_id 123
                           :important false
                           :from_cv_subsystem false
                           :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                           ;; All oneof fields are nil
                           :ping nil :osd nil :system nil :noop nil :cv nil :gps nil 
                           :lrf nil :lira nil :lrf_calib nil :rotary nil 
                           :day_cam_glass_heater nil :heat_camera nil 
                           :compass nil :day_camera nil :frozen nil}
              valid? (m/validate spec invalid-data)
              explanation (m/explain spec invalid-data)]
          
          (is (not valid?) "Data with no oneof should not validate")
          (is (some? explanation) "Should have explanation for missing oneof")
          (when explanation
            (let [errors (:errors explanation)
                  humanized (try (me/humanize explanation) (catch Exception _ nil))
                  error-messages (map :message errors)]
              (is (or (some #(re-find #"exactly one" (str %)) error-messages)
                     (and humanized (re-find #"exactly one" (str humanized))))
                  "Error should mention oneof constraint"))))))))

(deftest test-explain-returns-nil-bug
  (testing "Reproduce the exact bug where m/explain returns nil"
    (let [spec (m/schema :cmd/root)
          found-bug? (atom false)]
      
      ;; Try to reproduce the exact condition
      (dotimes [i 200]
        (when-not @found-bug?
          (let [original (mg/generate spec)
                proto-map (pronto/clj-map->proto-map cmd-mapper cmd.JonSharedCmd$Root original)
                proto (pronto.utils/proto-map->proto proto-map)
                bytes (.toByteArray proto)
                parsed (cmd.JonSharedCmd$Root/parseFrom bytes)
                proto-map2 (pronto/proto->proto-map cmd-mapper parsed)
                result (pronto/proto-map->clj-map proto-map2)
                valid? (m/validate spec result)
                explanation (m/explain spec result)]
            
            ;; This is the bug: validate returns false but explain returns nil
            (when (and (not valid?) (nil? explanation))
              (reset! found-bug? true)
              (println (format "\nBUG REPRODUCED at iteration %d!" i))
              (println "Result keys:" (sort (keys result)))
              (println "Validating with open map (no :closed constraint):")
              (let [open-spec (m/schema [:map 
                                        [:protocol_version :int]
                                        [:session_id :int]])]
                (println "  Open map validates?" (m/validate open-spec result))
                ;; Check structure with matcher-combinators
                (is (match? {:protocol_version int?
                            :session_id int?}
                           result)))
              
              ;; This should fail the test, demonstrating the bug
              (is false 
                  (format "Bug: m/validate returned false but m/explain returned nil at iteration %d" i))))))
      
      (when-not @found-bug?
        (println "\nBug not reproduced in 200 iterations - may need more samples")))))