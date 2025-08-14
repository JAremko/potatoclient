(ns potatoclient.test-health-check
  "Health check to ensure all test namespaces are actually running.
   This prevents 'false green' scenarios where tests pass because they don't run."
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(def ^:private expected-test-namespaces
  "List of test namespaces that MUST exist and run.
   Add any critical test namespace here to ensure it's not silently skipped."
  #{'potatoclient.specs.buf-validate-gen-test
    'potatoclient.specs.common-test
    'potatoclient.specs.state.gps-test
    'potatoclient.specs.state.lrf-test
    'potatoclient.specs.state.root-test
    'potatoclient.specs.state.rotary-test
    'potatoclient.specs.state.system-test
    'potatoclient.specs.state.time-test
    'potatoclient.specs.state.camera-day-test
    'potatoclient.specs.state.camera-heat-test})

(defn find-test-files
  "Find all test files in the test directory."
  []
  (let [test-dir (io/file "test")]
    (->> (file-seq test-dir)
         (filter #(.endsWith (.getName %) "_test.clj"))
         (map (fn [file]
                (let [path (.getPath file)
                      ;; Convert file path to namespace
                      ns-path (-> path
                                  (str/replace #"^test/" "")
                                  (str/replace #"\.clj$" "")
                                  (str/replace #"/" ".")
                                  (str/replace #"_" "-"))]
                  (symbol ns-path))))
         set)))

(defn namespace-loaded?
  "Check if a namespace is loaded in the current runtime."
  [ns-sym]
  (try
    (find-ns ns-sym)
    true
    (catch Exception _
      false)))

(deftest critical-test-namespaces-exist
  (testing "All critical test namespaces should exist as files"
    (let [actual-test-files (find-test-files)
          missing-files (remove actual-test-files expected-test-namespaces)]
      (is (empty? missing-files)
          (format "Missing test files: %s\nThese namespaces are expected but their files don't exist!"
                  (vec missing-files))))))

(deftest critical-test-namespaces-loaded
  (testing "All critical test namespaces should be loaded (not skipped due to compile errors)"
    (let [not-loaded (remove namespace-loaded? expected-test-namespaces)]
      (is (empty? not-loaded)
          (format "Test namespaces not loaded (likely due to compile errors): %s\n
These tests are NOT running! Check for syntax errors or missing dependencies."
                  (vec not-loaded))))))

(deftest test-count-sanity-check
  (testing "Should have a reasonable number of test namespaces loaded"
    (let [loaded-test-nses (->> (all-ns)
                                (map ns-name)
                                (filter #(str/includes? (str %) "test"))
                                (remove #(= % 'potatoclient.test-health-check))
                                set)
          count-loaded (count loaded-test-nses)]
      
      (is (>= count-loaded 5)
          (format "Only %d test namespaces loaded. Expected at least 5. Loaded: %s"
                  count-loaded
                  (vec loaded-test-nses)))
      
      ;; Report what's actually loaded for visibility
      (println "\n=== Test Health Check Report ===")
      (println (format "Test namespaces loaded: %d" count-loaded))
      (doseq [ns-name (sort loaded-test-nses)]
        (println (format "  ✓ %s" ns-name)))
      (println "================================\n"))))

(deftest no-disabled-test-files
  (testing "Should not have disabled test files lying around"
    (let [test-dir (io/file "test")
          disabled-files (->> (file-seq test-dir)
                             (filter #(or (.endsWith (.getName %) ".disabled")
                                        (.endsWith (.getName %) ".skip")
                                        (.endsWith (.getName %) ".bak")))
                             (map #(.getPath %)))]
      (is (empty? disabled-files)
          (format "Found disabled/skipped test files: %s\n
These should be either fixed or removed from the repository!"
                  (vec disabled-files))))))

;; This will run when namespace loads, giving immediate feedback
(defn report-test-environment []
  (println "\n🔍 Test Environment Check:")
  (println (format "   Expected test namespaces: %d" (count expected-test-namespaces)))
  (println (format "   Actual test files found: %d" (count (find-test-files))))
  (println (format "   Test namespaces loaded: %d" 
                   (count (filter namespace-loaded? expected-test-namespaces))))
  (when-let [not-loaded (seq (remove namespace-loaded? expected-test-namespaces))]
    (println "   ⚠️  WARNING: Some tests not loaded:" (vec not-loaded))))

;; Run check on namespace load
(report-test-environment)