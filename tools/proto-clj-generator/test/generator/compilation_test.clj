(ns generator.compilation-test
  "Test that generated code compiles successfully.
  This validates our dependency graph implementation."
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [generator.core :as core]
            [taoensso.timbre :as log]))

(deftest generated-code-compiles
  (testing "All generated files have valid Clojure syntax"
    ;; First generate the code
    (let [result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                      :output-dir "test-output"
                                      :namespace-prefix "test.proto"
                                      :namespace-split? true
                                      :debug? false})]
      (is (:success result))
      
      ;; Check that each file can be read as valid Clojure
      (testing "All files have valid syntax"
        (doseq [file (:files result)]
          (when (str/ends-with? file ".clj")
            (testing (str "Checking syntax of " file)
              (is (try
                    ;; Try to read the file as Clojure forms
                    (with-open [rdr (java.io.PushbackReader. (io/reader file))]
                      (loop []
                        (let [form (read rdr false ::eof)]
                          (when-not (= form ::eof)
                            (recur))))
                      true)
                    (catch Exception e
                      (log/error e "Failed to parse" file)
                      false))
                  (str "Invalid syntax in " file)))))))))

(deftest dependency-graph-correctness
  (testing "Dependencies are correctly resolved"
    (let [result (core/generate-all {:input-dir "../proto-explorer/output/json-descriptors"
                                      :output-dir "test-output"
                                      :namespace-prefix "test.proto.deps"
                                      :namespace-split? true
                                      :debug? true})]
      
      ;; Check that files with dependencies have require statements
      (testing "Files with proto dependencies have Clojure requires"
        (let [rotary-file (slurp "test-output/test/proto/deps/cmd/rotaryplatform.clj")]
          ;; cmd.rotaryplatform depends on ser.types
          (is (re-find #":require.*test\.proto\.deps\.ser\.types" rotary-file)
              "rotaryplatform should require ser.types")))
      
      ;; Check that files without dependencies have no requires
      (testing "Files without proto dependencies have no extra requires"
        (let [gps-file (slurp "test-output/test/proto/deps/cmd/gps.clj")]
          ;; cmd.gps has no dependencies
          (is (not (re-find #":require" gps-file))
              "gps should have no require statements"))))))

;; Clean up test output after tests
(defn cleanup-test-output []
  (let [test-dir (io/file "test-output")]
    (when (.exists test-dir)
      (doseq [file (file-seq test-dir)]
        (when (.isFile file)
          (.delete file)))
      (doseq [dir (reverse (file-seq test-dir))]
        (when (.isDirectory dir)
          (.delete dir))))))

;; Run cleanup after all tests
(defn test-ns-hook []
  (try
    (generated-code-compiles)
    (dependency-graph-correctness)
    (finally
      (cleanup-test-output))))