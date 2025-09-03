(ns kondo-gen.discovery
  "Automatic discovery of namespaces containing Malli schemas."
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io File]))

(defn find-clojure-files
  "Recursively find all Clojure source files in a directory."
  {:malli/schema [:=> [:cat :string] [:sequential :string]]}
  [dir]
  (let [directory (io/file dir)]
    (when (.exists directory)
      (->> (file-seq directory)
           (filter #(.isFile %))
           (filter #(or (.endsWith (.getName %) ".clj")
                       (.endsWith (.getName %) ".cljc")))
           (map #(.getPath %))
           vec))))

(defn extract-namespace
  "Extract namespace declaration from a Clojure file."
  {:malli/schema [:=> [:cat :string] [:maybe :symbol]]}
  [file-path]
  (try
    (with-open [rdr (io/reader file-path)]
      (let [content (slurp rdr)
            ;; Simple regex to find namespace declaration
            ns-match (re-find #"\(ns\s+([\w\.-]+)" content)]
        (when ns-match
          (symbol (second ns-match)))))
    (catch Exception _
      nil)))

(defn has-malli-schemas?
  "Check if a file contains Malli schema definitions."
  {:malli/schema [:=> [:cat :string] :boolean]}
  [file-path]
  (try
    (let [content (slurp file-path)]
      (or 
       ;; Check for :malli/schema metadata
       (str/includes? content ":malli/schema")
       ;; Check for m/=> schema registration
       (str/includes? content "m/=>")
       ;; Check for mx/defn from malli.experimental
       (str/includes? content "mx/defn")))
    (catch Exception _
      false)))

(defn discover-namespaces-with-schemas
  "Discover all namespaces in the source directories that contain Malli schemas."
  {:malli/schema [:=> [:cat [:sequential :string]] [:sequential :symbol]]}
  [source-dirs]
  (->> source-dirs
       (mapcat find-clojure-files)
       (filter has-malli-schemas?)
       (keep extract-namespace)
       distinct
       (sort-by str)
       vec))

(defn discover-project-namespaces
  "Discover namespaces in the potatoclient project that have schemas."
  {:malli/schema [:=> [:cat] [:sequential :symbol]]}
  []
  (let [project-root (System/getProperty "user.dir")
        ;; When run from tools/kondo-gen, we need to go up to project root
        src-dir (if (str/ends-with? project-root "kondo-gen")
                  "../../src"
                  "src")]
    (discover-namespaces-with-schemas [src-dir])))

(defn namespace-exists?
  "Check if a namespace can be loaded."
  {:malli/schema [:=> [:cat :symbol] :boolean]}
  [ns-sym]
  (try
    (require ns-sym)
    true
    (catch Exception _
      false)))