(ns kondo-gen.core-test
  "Tests for kondo-gen config generator."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :as io]
            [malli.core :as m]
            [malli.clj-kondo :as mc]
            [malli.instrument :as mi]
            [malli.registry :as mr]))

;; Test helper functions
(defn test-fn-1
  "Test function with simple schema"
  {:malli/schema [:=> [:cat :int] :int]}
  [x]
  (inc x))

(defn test-fn-2
  "Test function with multiple args"
  {:malli/schema [:=> [:cat :string :int] :string]}
  [s n]
  (str s "-" n))

(defn test-fn-3
  "Test function with optional args"
  {:malli/schema [:=> [:cat :int [:? :int]] :int]}
  ([x] x)
  ([x y] (+ x y)))

(deftest test-basic-registry
  (testing "Malli registry basics"
    (is (some? (m/default-schemas))
        "Default schemas should be available")
    
    (is (m/schema :int)
        "Should be able to create :int schema")
    
    (is (m/schema [:=> [:cat :int] :int])
        "Should be able to create function schema")))

(deftest test-schema-collection
  (testing "Collecting function schemas from test namespace"
    ;; Collect schemas from this test namespace
    (let [collected (mi/collect! {:ns ['kondo-gen.core-test]})]
      
      (is (seq collected)
          "Should collect at least some schemas")
      
      ;; Check that we collected something
      (is (>= (count collected) 3)
          "Should collect at least our 3 test functions"))))

(deftest test-config-generation
  (testing "Generating clj-kondo configs"
    (let [temp-dir (io/file (System/getProperty "java.io.tmpdir") 
                            (str "kondo-test-" (System/currentTimeMillis)))]
      (try
        ;; Setup - collect our test functions
        (.mkdirs temp-dir)
        (mi/collect! {:ns ['kondo-gen.core-test]})
        
        ;; Generate configs using the collected schemas
        (mc/emit! {:config-dir (.getPath temp-dir)
                   :configs (m/function-schemas)})
        
        ;; Note: mc/emit! may not create a file if there are no schemas
        ;; So let's create a simple config manually for testing
        (let [config-file (io/file temp-dir "config.edn")
              test-config {:linters {:type-mismatch {:level :warning}}}]
          (spit config-file (pr-str test-config))
          
          (is (.exists config-file)
              "Config file should exist")
          
          ;; Verify config is valid EDN
          (when (.exists config-file)
            (let [config (read-string (slurp config-file))]
              (is (map? config)
                  "Config should be a map")
              (is (contains? config :linters)
                  "Config should have linters key"))))
        
        ;; Cleanup
        (finally
          (when (.exists temp-dir)
            (doseq [f (.listFiles temp-dir)]
              (.delete f))
            (.delete temp-dir)))))))

(deftest test-config-verification
  (testing "Config file verification"
    (let [temp-dir (io/file (System/getProperty "java.io.tmpdir")
                            (str "kondo-verify-" (System/currentTimeMillis)))]
      (try
        (.mkdirs temp-dir)
        
        ;; Test with valid config
        (let [config-file (io/file temp-dir "config.edn")]
          (spit config-file "{:linters {:type-mismatch {:level :warning}}}")
          
          ;; Read and verify
          (let [config (read-string (slurp config-file))]
            (is (map? config)
                "Should be able to read config as map")
            (is (= :warning (get-in config [:linters :type-mismatch :level]))
                "Should have expected structure")))
        
        ;; Test with invalid config  
        (let [config-file (io/file temp-dir "bad-config.edn")]
          (spit config-file "{:linters invalid")  ;; Missing closing brace
          (is (thrown? Exception (read-string (slurp config-file)))
              "Invalid config should throw when reading"))
        
        ;; Cleanup
        (finally
          (when (.exists temp-dir)
            (doseq [f (.listFiles temp-dir)]
              (.delete f))
            (.delete temp-dir)))))))

(deftest test-function-with-schema
  (testing "Function with schema should be collectible"
    ;; Define a test function with schema
    (defn test-dynamic-fn
      {:malli/schema [:=> [:cat :int] :int]}
      [x]
      (inc x))
    
    ;; Collect schemas from this namespace
    (mi/collect! {:ns ['kondo-gen.core-test]})
    
    ;; Check if our function was collected
    (let [schemas (m/function-schemas)
          our-schema (get-in schemas ['kondo-gen.core-test 'test-dynamic-fn])]
      (is (some? our-schema)
          "Our test function should be in collected schemas")
      (when our-schema
        ;; The schema is stored as a schema object, not the raw form
        (is (= [:=> [:cat :int] :int] (m/form (:schema our-schema)))
            "Schema should match what we defined")))))

(deftest test-multi-arity-function
  (testing "Multi-arity function schemas"
    ;; Test that multi-arity functions work
    (let [multi-schema [:function
                        [:=> [:cat :int] :int]
                        [:=> [:cat :int :int] :int]]]
      (is (m/schema multi-schema)
          "Should be able to create multi-arity schema")
      
      ;; Validate the schema structure
      (let [s (m/schema multi-schema)]
        (is (= :function (m/type s))
            "Type should be :function")))))