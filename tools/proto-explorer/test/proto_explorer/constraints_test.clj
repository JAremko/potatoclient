(ns proto-explorer.constraints-test
  (:require [clojure.test :refer [deftest testing is]]
            [proto-explorer.json-to-edn :as json-edn]))

(deftest constraint-extraction-test
  (testing "Extracting constraints from protobuf descriptors"
    (let [descriptor (json-edn/load-json-descriptor "output/json-descriptors/jon_shared_cmd_rotary.json")
          rotary-file (first (filter #(re-find #"rotary" (:name %)) (:file descriptor)))
          messages (:messageType rotary-file)]
      
      (testing "Rotary file loading"
        (is (not (nil? descriptor)) "Descriptor should load")
        (is (not (nil? rotary-file)) "Should find rotary file")
        (is (not (empty? messages)) "Should have messages"))
      
      (testing "SetAzimuthValue message"
        (let [set-azimuth (first (filter #(= "SetAzimuthValue" (:name %)) messages))]
          (is (not (nil? set-azimuth)) "Should find SetAzimuthValue message")
          (when set-azimuth
            (is (not (empty? (:field set-azimuth))) "Should have fields")
            (doseq [field (:field set-azimuth)]
              (is (contains? field :name) "Each field should have a name")))))
      
      (testing "SetMode message"
        (let [set-mode (first (filter #(= "SetMode" (:name %)) messages))]
          (is (not (nil? set-mode)) "Should find SetMode message")
          (when set-mode
            (is (not (empty? (:field set-mode))) "Should have fields")))))))