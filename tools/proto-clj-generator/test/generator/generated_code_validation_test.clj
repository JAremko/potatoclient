(ns generator.generated-code-validation-test
  "Tests that validate generated code quality using cljfmt and clj-kondo"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [cljfmt.core :as cljfmt]
            [generator.core :as core]
            [generator.backend :as backend]
            [taoensso.timbre :as log]))

;; =============================================================================
;; Test Helpers
;; =============================================================================

(defn find-clojure-files
  "Find all .clj files in a directory"
  [dir]
  (->> (file-seq (io/file dir))
       (filter #(.endsWith (.getName %) ".clj"))
       (map #(.getPath %))))

(defn validate-with-cljfmt
  "Validate that a file can be parsed by cljfmt"
  [file-path]
  (try
    (let [content (slurp file-path)
          ;; Try to parse with cljfmt
          _ (cljfmt/reformat-string content {})]
      {:valid? true :file file-path})
    (catch Exception e
      {:valid? false 
       :file file-path 
       :error (.getMessage e)
       :content-preview (subs (slurp file-path) 0 
                             (min 500 (count (slurp file-path))))})))

(defn run-clj-kondo
  "Run clj-kondo on a directory and return results"
  [dir]
  (let [result (shell/sh "clj-kondo" "--lint" dir)]
    {:exit (:exit result)
     :out (:out result)
     :err (:err result)}))

;; =============================================================================
;; Tests
;; =============================================================================

(deftest generated-code-parseable
  (testing "All generated code should be parseable by cljfmt"
    (let [output-dir "test-roundtrip-output"
          ;; Check if generated code exists
          _ (when-not (.exists (io/file output-dir))
              (log/warn "No generated code found. Run 'make generate-test-code' first"))
          clj-files (find-clojure-files output-dir)
          validation-results (map validate-with-cljfmt clj-files)]
      
      ;; All files should be valid
      (doseq [result validation-results]
        (when-not (:valid? result)
          (println "\nFailed to parse:" (:file result))
          (println "Error:" (:error result))
          (println "Content preview:")
          (println (:content-preview result)))
        (is (:valid? result) 
            (str "File should be parseable: " (:file result)))))))

(deftest generated-code-lintable
  (testing "Generated code should pass basic clj-kondo checks"
    (let [output-dir "test-roundtrip-output"]
      (when (.exists (io/file output-dir))
        (let [result (run-clj-kondo output-dir)]
          ;; Should exit successfully
          (is (zero? (:exit result)) 
              (str "clj-kondo should pass. Output:\n" (:out result) "\n" (:err result)))
          
          ;; Log any warnings for inspection
          (when-not (empty? (:out result))
            (println "\nclj-kondo output:")
            (println (:out result))))))))

(deftest generated-code-formatting
  (testing "Generated code should be properly formatted"
    (let [output-dir "test-roundtrip-output"
          clj-files (find-clojure-files output-dir)]
      (doseq [file clj-files]
        (let [content (slurp file)]
          ;; Check basic formatting rules
          (testing (str "File: " file)
            ;; No lines should be too long (except imports)
            (let [lines (clojure.string/split-lines content)
                  non-import-lines (remove #(re-find #"^\s*\(:import" %) lines)
                  long-lines (filter #(> (count %) 120) non-import-lines)]
              (is (empty? long-lines)
                  (str "No lines should exceed 120 chars (found " (count long-lines) ")")))
            
            ;; Should not have trailing whitespace
            (is (not (re-find #" +\n" content))
                "Should not have trailing whitespace")
            
            ;; Should end with newline
            (is (.endsWith content "\n")
                "File should end with newline")))))))

(deftest validate-imports
  (testing "Generated imports should be valid"
    (let [output-dir "test-roundtrip-output"
          clj-files (find-clojure-files output-dir)]
      (doseq [file clj-files]
        (let [content (slurp file)]
          ;; Extract imports
          (when-let [imports (re-find #"\(:import[^)]+\)" content)]
            (testing (str "Imports in " file)
              ;; Should not have empty import lists
              (is (not (re-find #"\(:import\s*\)" imports))
                  "Should not have empty import list")
              
              ;; All imports should be properly formatted
              (is (or (re-find #"\(:import\s+[A-Za-z]" imports)
                      (re-find #"\(:import\)" imports))
                  "Imports should be properly formatted"))))))))

(deftest namespace-declarations-valid
  (testing "All generated files should have valid namespace declarations"
    (let [output-dir "test-roundtrip-output"
          clj-files (find-clojure-files output-dir)]
      (doseq [file clj-files]
        (let [content (slurp file)
              ;; Extract namespace declaration
              ns-match (re-find #"\(ns\s+([^\s]+)" content)]
          (is ns-match (str "File should have namespace declaration: " file))
          (when ns-match
            (let [ns-name (second ns-match)
                  ;; File path should match namespace
                  expected-path (str output-dir "/" 
                                   (clojure.string/replace ns-name #"\." "/")
                                   ".clj")]
              (is (= expected-path file)
                  (str "Namespace " ns-name " should match file path")))))))))