(ns generator.namespace-split-test
  "Test the namespace-split generation mode"
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [generator.core :as core]))

(deftest namespace-split-generates-correct-structure
  (testing "Namespace-split mode generates expected file structure"
    (let [output-dir "test-output-namespace-split"
          result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                     :output-dir output-dir
                                     :namespace-prefix "test.proto.ns"
                                     :debug? false})]
      (is (:success result))
      
      (testing "Index files are generated"
        (let [ns-path (str output-dir "/test/proto/ns")]
          (is (.exists (io/file ns-path "cmd.clj")))
          (is (.exists (io/file ns-path "ser.clj")))))
      
      (testing "Package-specific files are generated"
        (let [ns-path (str output-dir "/test/proto/ns")]
          ;; Check for some expected package files
          (is (.exists (io/file ns-path "cmd/lira.clj")))
          (is (.exists (io/file ns-path "cmd/heatcamera.clj")))
          (is (.exists (io/file ns-path "cmd/system.clj")))
          (is (.exists (io/file ns-path "ser/heat.clj")))
          (is (.exists (io/file ns-path "ser/gps.clj")))))
      
      (testing "Index files contain proper content"
        (let [cmd-content (slurp (io/file output-dir "test/proto/ns/cmd.clj"))]
          ;; Check for namespace declaration
          (is (str/includes? cmd-content "(ns test.proto.ns.cmd"))
          ;; Check for proper requires
          (is (str/includes? cmd-content "[test.proto.ns.cmd.lira :as lira]"))
          (is (str/includes? cmd-content "[test.proto.ns.cmd.heatcamera :as heatcamera]")))))))

(deftest namespace-split-generates-expected-files
  (testing "Separated namespace mode generates expected files"
    (let [;; Generate in separated namespace mode
          result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                    :output-dir "test-output-separated"
                                    :namespace-prefix "test.proto.separated"
                                    :debug? false})]
      
      (is (:success result))
      
      (testing "Separated mode has package-specific files"
        (let [ns-path "test-output-separated/test/proto/separated"]
          ;; Should have index files
          (is (.exists (io/file ns-path "cmd.clj")))
          (is (.exists (io/file ns-path "ser.clj")))
          ;; Should have sub-packages
          (is (.exists (io/file ns-path "cmd")))
          (is (.exists (io/file ns-path "ser")))
          ;; Check some specific package files
          (is (.exists (io/file ns-path "cmd/lira.clj")))
          (is (.exists (io/file ns-path "cmd/system.clj")))
          (is (.exists (io/file ns-path "ser/gps.clj"))))))))