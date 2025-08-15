(ns potatoclient.oneof-base-fields-test
  "Test the enhanced oneof schema with base fields support"
  (:require
   [clojure.test :refer [deftest is testing]]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]))

;; Setup registry with oneof support
(registry/setup-global-registry!)

(deftest test-oneof-with-base-fields
  (testing "Oneof schema with base fields"
    
    (testing "Simple oneof with base fields"
      (let [schema [:oneof
                    ;; Base fields
                    [:id {:base true} :int]
                    [:name {:base true} :string]
                    ;; Oneof fields
                    [:foo :string]
                    [:bar :int]
                    [:baz :boolean]]
            
            ;; Valid samples
            valid-with-foo {:id 1 :name "test" :foo "hello"}
            valid-with-bar {:id 2 :name "test2" :bar 42}
            valid-with-baz {:id 3 :name "test3" :baz true}
            
            ;; Invalid samples
            invalid-no-oneof {:id 1 :name "test"}
            invalid-multiple {:id 1 :name "test" :foo "hello" :bar 42}
            invalid-missing-base {:foo "hello"}]
        
        (is (m/validate schema valid-with-foo) "Should validate with foo active")
        (is (m/validate schema valid-with-bar) "Should validate with bar active")
        (is (m/validate schema valid-with-baz) "Should validate with baz active")
        
        (is (not (m/validate schema invalid-no-oneof)) 
            "Should fail when no oneof field is present")
        (is (not (m/validate schema invalid-multiple))
            "Should fail when multiple oneof fields are present")
        (is (m/validate schema invalid-missing-base)
            "Base fields are not required by default")))
    
    (testing "Generation with base fields"
      (let [schema [:oneof
                    [:protocol_version {:base true} [:int {:min 1}]]
                    [:session_id {:base true} [:int {:min 1}]]
                    [:ping [:map]]
                    [:noop [:map]]
                    [:system [:map [:command :keyword]]]]
            
            samples (mg/sample schema {:size 10})]
        
        (println "\n=== Generated samples with base fields ===")
        (doseq [sample (take 3 samples)]
          (println "Sample:" sample)
          (is (m/validate schema sample))
          (is (contains? sample :protocol_version) "Should have protocol_version")
          (is (contains? sample :session_id) "Should have session_id")
          (is (>= (:protocol_version sample) 1) "protocol_version >= 1")
          (is (>= (:session_id sample) 1) "session_id >= 1")
          
          ;; Check exactly one oneof field is present
          (let [oneof-fields [:ping :noop :system]
                present-oneof (filter #(some? (get sample %)) oneof-fields)]
            (is (= 1 (count present-oneof)) 
                "Exactly one oneof field should be present")))))
    
    (testing "Optional base fields"
      (let [schema [:oneof
                    [:id {:base true} :int]
                    [:optional_field {:base true :optional true} :string]
                    [:active :boolean]
                    [:inactive :boolean]]
            
            with-optional {:id 1 :optional_field "present" :active true}
            without-optional {:id 2 :inactive false}]
        
        (is (m/validate schema with-optional) "Valid with optional field")
        (is (m/validate schema without-optional) "Valid without optional field")))
    
    (testing "Complex nested structures with base fields"
      (let [schema [:oneof
                    [:metadata {:base true} [:map
                                              [:version :int]
                                              [:timestamp :int]]]
                    [:config {:base true} [:map
                                           [:debug :boolean]]]
                    [:create [:map [:name :string]]]
                    [:update [:map [:id :int] [:name :string]]]
                    [:delete [:map [:id :int]]]]
            
            sample {:metadata {:version 1 :timestamp 12345}
                    :config {:debug false}
                    :create {:name "test"}}]
        
        (is (m/validate schema sample) "Complex nested structure validates")))
    
    (testing "Nil values in oneof fields"
      (let [schema [:oneof
                    [:base {:base true} :int]
                    [:a [:map]]
                    [:b [:map]]
                    [:c [:map]]]
            
            ;; With explicit nils for inactive fields (Pronto style)
            with-nils {:base 1 :a {} :b nil :c nil}
            ;; Without nils (cleaner style)
            without-nils {:base 1 :a {}}]
        
        (is (m/validate schema with-nils) "Valid with nil oneof fields")
        (is (m/validate schema without-nils) "Valid without nil oneof fields")))))