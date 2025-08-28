(ns proto-explorer.json-parsing-test
  (:require [clojure.test :refer [deftest testing is]]
            [proto-explorer.json-to-edn :as json-edn]))

(deftest json-descriptor-loading-test
  (testing "Loading JSON descriptor files"
    (let [descriptor-set (json-edn/load-json-descriptor "output/json-descriptors/jon_shared_cmd.json")
          cmd-descriptor (first (:file descriptor-set))]
      
      (testing "File structure"
        (is (not (nil? descriptor-set)) "Descriptor set should load")
        (is (not (nil? cmd-descriptor)) "Should have at least one file descriptor")
        (is (= "google/protobuf/descriptor.proto" (:name cmd-descriptor))
            "Should have expected file name")
        (is (= "google.protobuf" (:package cmd-descriptor))
            "Should have expected package")
        (is (nil? (:dependency cmd-descriptor))
            "Should have no dependencies")
        (is (= 23 (count (:messageType cmd-descriptor)))
            "Should have expected number of message types"))
      
      (testing "First message structure"
        (let [first-msg (first (:messageType cmd-descriptor))]
          (is (= "FileDescriptorSet" (:name first-msg))
              "First message should be FileDescriptorSet")
          (is (= 1 (count (:field first-msg)))
              "Should have one field")
          (is (= 0 (count (:oneofDecl first-msg)))
              "Should have no oneof declarations"))))))