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
                                     :namespace-split? true
                                     :debug? false})]
      (is (:success result))
      
      (testing "Index files are generated"
        (let [ns-path (str output-dir "/test/proto/ns")]
          (is (.exists (io/file ns-path "command.clj")))
          (is (.exists (io/file ns-path "state.clj")))))
      
      (testing "Package-specific files are generated"
        (let [ns-path (str output-dir "/test/proto/ns")]
          ;; Check for some expected package files
          (is (.exists (io/file ns-path "cmd/lira.clj")))
          (is (.exists (io/file ns-path "cmd/heatcamera.clj")))
          (is (.exists (io/file ns-path "cmd/system.clj")))
          (is (.exists (io/file ns-path "ser/heat.clj")))
          (is (.exists (io/file ns-path "ser/gps.clj")))))
      
      (testing "Index files contain proper re-exports"
        (let [cmd-content (slurp (io/file output-dir "test/proto/ns/command.clj"))]
          ;; Check for prefixed re-exports
          (is (str/includes? cmd-content "lira-build-root"))
          (is (str/includes? cmd-content "heatcamera-build-root"))
          (is (str/includes? cmd-content "system-build-root"))
          ;; Check for proper requires
          (is (str/includes? cmd-content "[test.proto.ns.cmd.lira :as lira]"))
          (is (str/includes? cmd-content "[test.proto.ns.cmd.heatcamera :as heatcamera]")))))))

(deftest namespace-split-vs-single-mode
  (testing "Both modes generate working code"
    (let [;; Generate in single mode
          single-result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                            :output-dir "test-output-single-mode"
                                            :namespace-prefix "test.proto.single"
                                            :namespace-mode :single
                                            :debug? false})
          ;; Generate in namespace-split mode
          ns-result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                        :output-dir "test-output-ns-mode"
                                        :namespace-prefix "test.proto.split"
                                        :namespace-split? true
                                        :debug? false})]
      
      (is (:success single-result))
      (is (:success ns-result))
      
      (testing "Single mode has monolithic files"
        (let [single-path "test-output-single-mode/test/proto/single"]
          (is (.exists (io/file single-path "command.clj")))
          (is (.exists (io/file single-path "state.clj")))
          ;; Should NOT have sub-packages
          (is (not (.exists (io/file single-path "cmd"))))))
      
      (testing "Namespace-split mode has distributed files"
        (let [ns-path "test-output-ns-mode/test/proto/split"]
          (is (.exists (io/file ns-path "command.clj")))
          (is (.exists (io/file ns-path "state.clj")))
          ;; Should have sub-packages
          (is (.exists (io/file ns-path "cmd")))
          (is (.exists (io/file ns-path "ser"))))))))