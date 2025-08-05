(ns guardrails-check.core
  "Core functionality for detecting functions that use raw defn instead of Guardrails' >defn.
  
  This namespace provides tools to analyze source code and find functions that are not
  using Guardrails, helping maintain consistent validation across the codebase."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn file->lines
  "Read a file and return its lines."
  [file]
  (when (.exists file)
    (str/split-lines (slurp file))))

(defn find-function-definitions
  "Find all function definitions in a sequence of lines.
  Returns a map of line numbers to function names."
  [lines]
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

(defn find-raw-defn-functions
  "Find functions using raw defn/defn- instead of >defn/>defn-."
  [lines]
  (for [[_ line] (map-indexed vector lines)
        :let [trimmed (str/trim line)]
        :when (and (or (str/starts-with? trimmed "(defn ")
                       (str/starts-with? trimmed "(defn-"))
                   (not (str/starts-with? trimmed "(>defn"))
                   (not (str/starts-with? trimmed "(>defn-")))
        :let [func-name (second (str/split trimmed #"\s+"))
              func-name (str/replace func-name #"[\(\)]" "")]]
    func-name))

(defn analyze-file
  "Analyze a Clojure file for functions not using Guardrails."
  [file]
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

(defn find-clojure-files
  "Find all Clojure source files in a directory."
  [dir]
  (let [clj-extensions #{".clj" ".cljs" ".cljc"}]
    (->> (file-seq dir)
         (filter #(and (.isFile %)
                       (some (fn [ext] (str/ends-with? (.getName %) ext))
                             clj-extensions)))
         vec)))

(defn find-unspecced-functions
  "Find all functions in the given source directory that use raw defn/defn-
   instead of Guardrails' >defn/>defn-."
  [src-dir]
  (let [dir (io/file src-dir)
        files (find-clojure-files dir)
        results (map analyze-file files)]
    (reduce (fn [acc {:keys [file-path raw-defn-functions]}]
              (if (seq raw-defn-functions)
                (let [ns-path (-> file-path
                                  (str/replace (re-pattern (str "^" src-dir "/")) "")
                                  (str/replace #"\.clj[cs]?$" "")
                                  (str/replace #"/" ".")
                                  (str/replace #"_" "-")
                                  symbol)]
                  (assoc acc ns-path (mapv symbol raw-defn-functions)))
                acc))
            {}
            results)))

(defn generate-report
  "Generate a comprehensive report about functions without Guardrails."
  [src-dir]
  (let [unspecced (find-unspecced-functions src-dir)
        total-unspecced (reduce + (map count (vals unspecced)))
        total-namespaces (count unspecced)]
    {:summary {:total-unspecced-functions total-unspecced
               :total-namespaces-with-issues total-namespaces
               :namespaces (keys unspecced)}
     :details unspecced}))

(defn format-report
  "Format the report for display."
  [{:keys [summary details]}]
  (let [lines [(str "# Guardrails Check Report")
               ""
               "## Summary"
               (format "- Total unspecced functions: %d" (:total-unspecced-functions summary))
               (format "- Namespaces with issues: %d" (:total-namespaces-with-issues summary))
               ""]]
    (if (zero? (:total-unspecced-functions summary))
      (conj lines "✅ All functions are using Guardrails!")
      (concat lines
              ["## Functions Without Guardrails" ""]
              (for [[ns funcs] (sort-by key details)]
                (str "### " ns "\n"
                     (str/join "\n" (map #(str "- " %) (sort funcs)))
                     "\n"))))))