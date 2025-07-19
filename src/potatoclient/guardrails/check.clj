(ns potatoclient.guardrails.check
  "Utility namespace for detecting functions that use raw defn instead of Guardrails' >defn.
  
  This namespace provides tools to analyze source code and find functions that are not
  using Guardrails, helping maintain consistent validation across the codebase."
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => ?]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set])
  (:import [java.io File]))

(>defn file->lines
  "Read a file and return its lines."
  [file]
  [[:fn {:error/message "must be a File"}
    #(instance? File %)] => [:sequential string?]]
  (when (.exists file)
    (str/split-lines (slurp file))))

(>defn find-function-definitions
  "Find all function definitions in a sequence of lines.
  Returns a map of line numbers to function names."
  [lines]
  [[:sequential string?] => [:map-of pos-int? string?]]
  (into {}
        (for [[idx line] (map-indexed vector lines)
              :let [trimmed (str/trim line)]
              :when (or (str/starts-with? trimmed "(defn ")
                        (str/starts-with? trimmed "(defn-")
                        (str/starts-with? trimmed "(>defn ")
                        (str/starts-with? trimmed "(>defn-"))
              :let [func-name (second (str/split trimmed #"\s+"))
                    func-name (str/replace func-name #"[\(\)]" "")]]
          [(inc idx) func-name])))

(>defn find-raw-defn-functions
  "Find functions using raw defn/defn- instead of >defn/>defn-."
  [lines]
  [[:sequential string?] => [:sequential string?]]
  (for [[idx line] (map-indexed vector lines)
        :let [trimmed (str/trim line)]
        :when (and (or (str/starts-with? trimmed "(defn ")
                       (str/starts-with? trimmed "(defn-"))
                   (not (str/starts-with? trimmed "(>defn"))
                   (not (str/starts-with? trimmed "(>defn-")))
        :let [func-name (second (str/split trimmed #"\s+"))
              func-name (str/replace func-name #"[\(\)]" "")]]
    func-name))

(>defn analyze-file
  "Analyze a Clojure file for functions not using Guardrails."
  [file]
  [[:fn {:error/message "must be a File"}
    #(instance? File %)] => [:map
                             [:file-path string?]
                             [:total-functions nat-int?]
                             [:raw-defn-functions [:sequential string?]]
                             [:guardrails-functions nat-int?]]]
  (let [lines (file->lines file)
        all-functions (find-function-definitions lines)
        raw-functions (find-raw-defn-functions lines)
        total-count (count all-functions)
        raw-count (count raw-functions)
        guardrails-count (- total-count raw-count)]
    {:file-path (.getPath file)
     :total-functions total-count
     :raw-defn-functions raw-functions
     :guardrails-functions guardrails-count}))

(>defn find-clojure-files
  "Find all Clojure source files in a directory."
  [dir]
  [[:fn {:error/message "must be a File"}
    #(instance? File %)] => [:sequential [:fn {:error/message "must be a File"}
                                          #(instance? File %)]]]
  (let [clj-extensions #{".clj" ".cljs" ".cljc"}]
    (->> (file-seq dir)
         (filter #(and (.isFile %)
                       (some (fn [ext] (str/ends-with? (.getName %) ext))
                             clj-extensions)))
         vec)))

(>defn find-unspecced-functions
  "Find all functions in potatoclient namespaces that use raw defn/defn-
   instead of Guardrails' >defn/>defn-."
  []
  [=> [:map-of symbol? [:sequential symbol?]]]
  (let [src-dir (io/file "src/potatoclient")
        files (find-clojure-files src-dir)
        results (map analyze-file files)]
    (reduce (fn [acc {:keys [file-path raw-defn-functions]}]
              (if (seq raw-defn-functions)
                (let [ns-path (-> file-path
                                  (str/replace #"^src/" "")
                                  (str/replace #"\.clj[cs]?$" "")
                                  (str/replace #"/" ".")
                                  (str/replace #"_" "-")
                                  symbol)]
                  (assoc acc ns-path (mapv symbol raw-defn-functions)))
                acc))
            {}
            results)))

(>defn generate-report
  "Generate a report of functions not using Guardrails."
  []
  [=> string?]
  (let [unspecced (find-unspecced-functions)
        total-unspecced (reduce + (map count (vals unspecced)))]
    (str "# Functions Not Using Guardrails\n\n"
         "Total functions without Guardrails: " total-unspecced "\n\n"
         (str/join "\n"
                   (for [[ns-sym funcs] (sort-by key unspecced)]
                     (str "## " ns-sym " (" (count funcs) " functions)\n"
                          (str/join "\n" (map #(str "- " %) (sort funcs)))
                          "\n"))))))

(>defn check-and-report!
  "Check for functions not using Guardrails and print a report."
  []
  [=> nil?]
  (println (generate-report)))