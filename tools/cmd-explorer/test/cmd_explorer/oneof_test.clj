(ns cmd-explorer.oneof-test
  "Comprehensive tests for oneof-pronto spec"
  (:require
   [clojure.test :refer [deftest testing is]]
   [malli.core :as m]
   [malli.generator :as mg]
   [clojure.test.check.generators :as gen]
   [cmd-explorer.test-harness] ;; Auto-initializes on load
   [potatoclient.specs.oneof-pronto :as oneof]
   [potatoclient.specs.proto-generators :as pg]
   [pronto.core :as p])
  (:import
   [cmd JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen]))

;; Define mapper for tests
(p/defmapper test-mapper [JonSharedCmd$Root JonSharedCmd$Ping JonSharedCmd$Noop JonSharedCmd$Frozen])

;; Create custom specs for proto fields
(def ping-spec [:fn {:gen/gen (pg/ping-generator test-mapper JonSharedCmd$Ping)} 
                #(instance? JonSharedCmd$Ping %)])
(def noop-spec [:fn {:gen/gen (pg/noop-generator test-mapper JonSharedCmd$Noop)} 
                #(instance? JonSharedCmd$Noop %)])
(def frozen-spec [:fn {:gen/gen (pg/frozen-generator test-mapper JonSharedCmd$Frozen)} 
                  #(instance? JonSharedCmd$Frozen %)])

(deftest test-oneof-validation
  (testing "Oneof validation with proto-maps"
    
    (testing "Accepts proto-map with exactly one field set"
      (let [spec [:oneof-pronto
                  {:proto-class JonSharedCmd$Root
                   :proto-mapper test-mapper
                   :oneof-name :payload
                   :ping [:fn #(instance? JonSharedCmd$Ping %)]
                   :noop [:fn #(instance? JonSharedCmd$Noop %)]
                   :frozen [:fn #(instance? JonSharedCmd$Frozen %)]}]
            proto-map (p/proto-map test-mapper JonSharedCmd$Root
                                 :ping (p/proto-map test-mapper JonSharedCmd$Ping))]
        (is (m/validate spec proto-map))
        (is (= :ping (p/which-one-of proto-map :payload)))))
    
    (testing "Rejects proto-map with no fields set"
      (let [spec [:oneof-pronto
                  {:proto-class JonSharedCmd$Root
                   :proto-mapper test-mapper
                   :oneof-name :payload
                   :ping [:fn #(instance? JonSharedCmd$Ping %)]
                   :noop [:fn #(instance? JonSharedCmd$Noop %)]
                   :frozen [:fn #(instance? JonSharedCmd$Frozen %)]}]
            ;; Create proto-map with no payload field set
            proto-map (p/proto-map test-mapper JonSharedCmd$Root)]
        (is (not (m/validate spec proto-map)))
        (is (nil? (p/which-one-of proto-map :payload)))))
    
    ;; Removed confusing test - validation of field values is tested in other tests
    ))

(deftest test-oneof-generator
  (testing "Oneof generator produces valid proto-maps"
    (let [spec [:oneof-pronto
                {:proto-class JonSharedCmd$Root
                 :proto-mapper test-mapper
                 :oneof-name :payload
                 :ping [:any]
                 :noop [:any]
                 :frozen [:any]}]]
      
      (testing "Generator produces proto-maps"
        (let [sample (mg/generate spec)]
          (is (p/proto-map? sample))
          (is (some? (p/which-one-of sample :payload)))))
      
      (testing "Generated proto-maps have exactly one field set"
        (dotimes [_ 10]
          (let [sample (mg/generate spec)
                active-field (p/which-one-of sample :payload)]
            (is (some? active-field))
            (is (contains? #{:ping :noop :frozen} active-field)))))
      
      (testing "All generated samples are valid"
        (dotimes [_ 100]
          (let [sample (mg/generate spec)]
            (is (m/validate spec sample))))))))

(deftest test-oneof-serialization
  (testing "Generated proto-maps can serialize/deserialize"
    (let [spec [:oneof-pronto
                {:proto-class JonSharedCmd$Root
                 :proto-mapper test-mapper
                 :oneof-name :payload
                 :ping [:any]
                 :noop [:any]
                 :frozen [:any]}]]
      
      (testing "Round-trip serialization preserves data"
        (dotimes [_ 10]
          (let [original (mg/generate spec)
                bytes (p/proto-map->bytes original)
                restored (p/bytes->proto-map test-mapper JonSharedCmd$Root bytes)]
            (is (bytes? bytes))
            (is (p/proto-map? restored))
            (is (= (p/which-one-of original :payload)
                   (p/which-one-of restored :payload)))))))))

(deftest test-oneof-with-complex-fields
  (testing "Oneof with complex field validation"
    (let [spec [:oneof-pronto
                {:proto-class JonSharedCmd$Root
                 :proto-mapper test-mapper
                 :oneof-name :payload
                 :ping [:fn #(instance? JonSharedCmd$Ping %)]
                 :noop [:fn #(instance? JonSharedCmd$Noop %)]
                 :frozen [:fn #(instance? JonSharedCmd$Frozen %)]}]
          ;; Note: protocol_version and session_id are not part of the oneof
          ;; They're regular fields, so this spec might not work as expected
          proto-map (p/proto-map test-mapper JonSharedCmd$Root
                               :ping (p/proto-map test-mapper JonSharedCmd$Ping)
                               :protocol_version 1
                               :session_id 123)]
      ;; The oneof spec only validates the oneof field
      (is (m/validate spec proto-map)))))

(deftest test-oneof-edge-cases
  (testing "Oneof edge cases"
    
    (testing "Handles nil proto-map"
      (let [spec [:oneof-pronto
                  {:proto-class JonSharedCmd$Root
                   :proto-mapper test-mapper
                   :oneof-name :payload
                   :ping [:fn #(instance? JonSharedCmd$Ping %)]}]]
        (is (not (m/validate spec nil)))))
    
    (testing "Handles non-proto-map values"
      (let [spec [:oneof-pronto
                  {:proto-class JonSharedCmd$Root
                   :proto-mapper test-mapper
                   :oneof-name :payload
                   :ping [:fn #(instance? JonSharedCmd$Ping %)]}]]
        (is (not (m/validate spec {:ping "not-a-proto-map"})))))
    
    (testing "Explains validation errors"
      (let [spec [:oneof-pronto
                  {:proto-class JonSharedCmd$Root
                   :proto-mapper test-mapper
                   :oneof-name :payload
                   :ping [:fn #(instance? JonSharedCmd$Ping %)]}]
            proto-map (p/proto-map test-mapper JonSharedCmd$Root)
            explanation (m/explain spec proto-map)]
        (is (some? explanation))
        (is (seq (:errors explanation)))))))

(deftest test-oneof-performance
  (testing "Performance test with 1000+ samples"
    (let [;; Use :any for generation, but validate structure
          gen-spec [:oneof-pronto
                    {:proto-class JonSharedCmd$Root
                     :proto-mapper test-mapper
                     :oneof-name :payload
                     :ping [:any]
                     :noop [:any]
                     :frozen [:any]}]
          ;; Use instance checks for validation
          validation-spec [:oneof-pronto
                          {:proto-class JonSharedCmd$Root
                           :proto-mapper test-mapper
                           :oneof-name :payload
                           :ping [:fn #(instance? JonSharedCmd$Ping %)]
                           :noop [:fn #(instance? JonSharedCmd$Noop %)]
                           :frozen [:fn #(instance? JonSharedCmd$Frozen %)]}]
          start-time (System/currentTimeMillis)]
      
      (testing "Generate and validate 1000 samples"
        (let [samples (repeatedly 1000 #(mg/generate gen-spec))
              all-valid? (every? #(m/validate validation-spec %) samples)
              all-have-one-field? (every? #(some? (p/which-one-of % :payload)) samples)
              elapsed-time (- (System/currentTimeMillis) start-time)]
          
          (is all-valid? "All 1000 samples should be valid")
          (is all-have-one-field? "All samples should have exactly one field set")
          (is (< elapsed-time 5000) "Should complete within 5 seconds")
          
          ;; Check distribution of fields
          (let [field-counts (frequencies (map #(p/which-one-of % :payload) samples))]
            (is (> (count field-counts) 1) "Should generate different fields")
            (is (every? #(> % 100) (vals field-counts)) 
                "Each field should be generated at least 100 times")))))))

(deftest test-oneof-with-nested-proto-maps
  (testing "Oneof with nested proto-map validation"
    ;; This test would require more complex proto definitions
    ;; For now, we test that nested proto-maps work
    (let [spec [:oneof-pronto
                {:proto-class JonSharedCmd$Root
                 :proto-mapper test-mapper
                 :oneof-name :payload
                 :ping [:fn #(instance? JonSharedCmd$Ping %)]  ;; Validate the actual proto instance
                 :noop [:fn #(instance? JonSharedCmd$Noop %)]
                 :frozen [:fn #(instance? JonSharedCmd$Frozen %)]}]
          proto-map (p/proto-map test-mapper JonSharedCmd$Root
                               :frozen (p/proto-map test-mapper JonSharedCmd$Frozen))]
      (is (m/validate spec proto-map))
      (is (= :frozen (p/which-one-of proto-map :payload))))))