(ns generator.proto-registry-test
  (:require [clojure.test :refer :all]
            [generator.proto-registry :as registry]
            [generator.naming-config :as naming]
            [clojure.java.io :as io]))

(deftest test-package-to-namespace
  (testing "Package to namespace conversion with default config"
    (registry/clear-registry)
    (is (= "potatoclient.proto.cmd.cv" 
           (registry/package->namespace "cmd.CV")))
    (is (= "potatoclient.proto.ser.types" 
           (registry/package->namespace "ser")))
    (is (= "potatoclient.proto.ser.data" 
           (registry/package->namespace "ser.Data")))
    (is (= "potatoclient.proto.custom-package" 
           (registry/package->namespace "custom_package")))))

(deftest test-custom-naming-config
  (testing "Custom naming configuration"
    (registry/clear-registry)
    (registry/set-naming-config
     {:namespace-prefix "com.example.proto"
      :package-rules
      [{:name "Test rule"
        :pattern #"^test\.(.+)$"
        :transform (fn [match] (str "testing." (second match)))}
       {:name "Default"
        :pattern #"(.+)"
        :transform (fn [match] (second match))}]})
    
    (is (= "com.example.proto.testing.foo"
           (registry/package->namespace "test.foo")))
    (is (= "com.example.proto.other"
           (registry/package->namespace "other")))))

(deftest test-filename-to-alias
  (testing "Filename to alias generation"
    (let [config naming/default-config]
      (is (= :cv (naming/filename->alias config "jon_shared_cmd_cv.proto")))
      (is (= :types (naming/filename->alias config "jon_shared_data_types.proto")))
      (is (= :rotary (naming/filename->alias config "jon_shared_cmd_rotary.proto")))
      (is (= :my-file (naming/filename->alias config "my_file.proto"))))))

(deftest test-type-registry
  (testing "Type registry and lookup"
    (registry/clear-registry)
    ;; Simulate loading a descriptor
    (swap! @#'registry/registry assoc-in 
           [:files "jon_shared_cmd_cv.proto"]
           {:filename "jon_shared_cmd_cv.proto"
            :package "cmd.CV"
            :dependencies ["jon_shared_data_types.proto"]
            :message-types ["Root" "StartTrackNDC"]
            :enum-types ["TrackingMode"]})
    
    (swap! @#'registry/registry assoc-in
           [:types ".cmd.CV.TrackingMode"]
           {:type :enum
            :name "TrackingMode"
            :qualified-name ".cmd.CV.TrackingMode"
            :filename "jon_shared_cmd_cv.proto"
            :package "cmd.CV"})
    
    (let [type-info (registry/get-type-info ".cmd.CV.TrackingMode")]
      (is (= :enum (:type type-info)))
      (is (= "TrackingMode" (:name type-info)))
      (is (= "jon_shared_cmd_cv.proto" (:filename type-info))))))

(deftest test-enum-resolution
  (testing "Enum resolution with metadata"
    (registry/clear-registry)
    ;; Set up test data
    (swap! @#'registry/registry assoc-in 
           [:files "jon_shared_cmd_cv.proto"]
           {:filename "jon_shared_cmd_cv.proto"
            :package "cmd.CV"
            :dependencies ["jon_shared_data_types.proto"]})
    
    (swap! @#'registry/registry assoc-in 
           [:files "jon_shared_data_types.proto"]
           {:filename "jon_shared_data_types.proto"
            :package "ser"
            :dependencies []})
    
    (swap! @#'registry/registry assoc-in
           [:types ".ser.Channel"]
           {:type :enum
            :name "Channel"
            :qualified-name ".ser.Channel"
            :filename "jon_shared_data_types.proto"
            :package "ser"})
    
    ;; Test enum resolution from different file
    (let [result (registry/resolve-enum-with-metadata ".ser.Channel" "jon_shared_cmd_cv.proto")]
      (is (= "channel-values" (:name result)))
      (is (= true (:qualified? result)))
      (is (= :types (:ns-alias result))))
    
    ;; Test enum resolution from same file
    (swap! @#'registry/registry assoc-in
           [:types ".cmd.CV.TrackingMode"]
           {:type :enum
            :name "TrackingMode"
            :qualified-name ".cmd.CV.TrackingMode"
            :filename "jon_shared_cmd_cv.proto"
            :package "cmd.CV"})
    
    (let [result (registry/resolve-enum-with-metadata ".cmd.CV.TrackingMode" "jon_shared_cmd_cv.proto")]
      (is (= "tracking-mode-values" (:name result)))
      (is (= false (:qualified? result))))))

(deftest test-ns-alias-map
  (testing "Namespace alias map generation"
    (registry/clear-registry)
    ;; Set up dependencies
    (swap! @#'registry/registry assoc-in 
           [:files "jon_shared_cmd_cv.proto"]
           {:filename "jon_shared_cmd_cv.proto"
            :package "cmd.CV"
            :dependencies ["jon_shared_data_types.proto" "buf/validate/validate.proto"]})
    
    (swap! @#'registry/registry assoc-in 
           [:files "jon_shared_data_types.proto"]
           {:filename "jon_shared_data_types.proto"
            :package "ser"
            :dependencies []})
    
    (let [alias-map (registry/build-ns-alias-map "jon_shared_cmd_cv.proto")]
      (is (= :types (get alias-map "potatoclient.proto.ser.types")))
      ;; buf/validate should be filtered out as it's not in registry
      (is (= 1 (count alias-map))))))

(deftest test-naming-config-validation
  (testing "Naming configuration validation"
    (is (true? (naming/validate-config naming/default-config)))
    (is (false? (naming/validate-config {})))
    (is (false? (naming/validate-config {:namespace-prefix "test"})))
    (is (false? (naming/validate-config 
                 {:namespace-prefix "test"
                  :package-rules [{:pattern "not-a-pattern" :transform inc}]})))))