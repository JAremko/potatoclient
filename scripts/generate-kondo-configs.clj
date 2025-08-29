#!/usr/bin/env clojure
;; Script to generate clj-kondo type configs from Malli/Guardrails specs

(ns generate-kondo-configs
  (:require
   [malli.instrument :as mi]
   [malli.core :as m]
   [malli.dev :as dev]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.edn :as edn]
   [clojure.pprint :as pp]))

(defn read-ns-from-file
  "Read the namespace form from a Clojure file"
  [file]
  (try
    (with-open [rdr (java.io.PushbackReader. (io/reader file))]
      (loop []
        (let [form (edn/read {:eof ::eof} rdr)]
          (cond
            (= form ::eof) nil
            (and (seq? form) (= 'ns (first form))) (second form)
            :else (recur)))))
    (catch Exception e
      nil)))

(defn find-clj-files
  "Find all .clj files in the given directories"
  [dirs]
  (for [dir dirs
        :let [dir-file (io/file dir)]
        :when (.exists dir-file)
        file (file-seq dir-file)
        :when (and (.isFile file)
                   (.endsWith (.getName file) ".clj")
                   ;; Exclude test files
                   (not (str/includes? (.getPath file) "/test/"))
                   ;; Exclude build files
                   (not (str/includes? (.getPath file) "/build/")))]
    file))

(defn discover-namespaces
  "Discover all namespaces from source files"
  [dirs]
  (let [files (find-clj-files dirs)
        namespaces (keep read-ns-from-file files)]
    (vec (distinct namespaces))))

(defn safe-require
  "Safely require a namespace, ignoring errors"
  [ns-sym]
  (try
    (require ns-sym)
    true
    (catch Exception e
      false)))

(defn setup-registries!
  "Setup the malli registry and ui-specs"
  []
  ;; First setup the global registry from shared
  (require 'potatoclient.malli.registry)
  ((resolve 'potatoclient.malli.registry/setup-global-registry!))
  
  ;; Then load ui-specs which registers its own specs
  (try
    (require 'potatoclient.ui-specs)
    true
    (catch Exception e
      (println "Note: Could not load ui-specs (expected for shared module):" (.getMessage e))
      false)))

(defn generate-configs
  "Generate clj-kondo configs using malli.dev"
  [dirs output-file]
  (let [namespaces (discover-namespaces dirs)
        _ (println "Found" (count namespaces) "namespaces")
        loaded-count (atom 0)]
    
    ;; Setup registries first
    (println "Setting up Malli registries...")
    (setup-registries!)
    
    ;; Try to load namespaces
    (println "Loading namespaces...")
    (doseq [ns-sym namespaces]
      (when (safe-require ns-sym)
        (swap! loaded-count inc)))
    
    (println "Successfully loaded" @loaded-count "namespaces")
    
    ;; Collect schemas from each namespace individually (to avoid failing on one bad namespace)
    (println "Collecting function schemas...")
    (let [collected (atom 0)]
      (doseq [ns-sym namespaces]
        (try 
          (mi/collect! {:ns [ns-sym]})
          (swap! collected inc)
          (catch Exception e
            ;; Skip namespaces with problematic schemas
            nil)))
      (println "Collected schemas from" @collected "namespaces"))
    
    ;; Get collected function schemas and write them
    (println "Writing function schemas to config file...")
    (io/make-parents output-file)
    
    (let [schemas (m/function-schemas)]
      (if (seq schemas)
        (let [config {:linters 
                     {:type-mismatch 
                      {:namespaces schemas}}}]
          (spit output-file (with-out-str (pp/pprint config)))
          (println (str "\n✓ Generated clj-kondo configs at: " output-file
                       "\n  Found " (count schemas) " function schemas"
                       "\n  File size: " (.length (io/file output-file)) " bytes")))
        (println "No function schemas found to write")))))

;; Main execution
(let [args *command-line-args*
      mode (first args)]
  ;; Disable Guardrails throwing during collection
  (System/setProperty "guardrails.enabled" "false")
  
  (case mode
    "shared"
    ;; Generate configs for shared module
    (generate-configs ["shared/src"] 
                     "shared/.clj-kondo/metosin/malli-types-clj/config.edn")
    
    "root"
    ;; Generate configs for root module (includes shared)
    (generate-configs ["src" "shared/src"]
                     ".clj-kondo/metosin/malli-types-clj/config.edn")
    
    ;; Default/help
    (do
      (println "Usage: clojure scripts/generate-kondo-configs.clj [shared|root]")
      (println "  shared - Generate configs for shared module only")
      (println "  root   - Generate configs for root module (includes shared)")
      (System/exit 1))))