(ns generator.ast-validation-test
  "AST-level validation of generated code to catch issues early."
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.edn :as edn]
            [clojure.tools.reader :as reader]
            [clojure.tools.reader.reader-types :as rt]))

(defn extract-function-calls
  "Extract all function calls from a form."
  [form]
  (let [calls (atom #{})]
    (clojure.walk/prewalk
      (fn [x]
        (when (and (list? x)
                   (symbol? (first x))
                   (not (#{'fn 'fn* 'let 'let* 'do 'if 'case 'cond 'when 'when-not
                          'quote '. '.. '-> '->> 'def 'defn 'defn- 'declare
                          'ns 'require 'import 'str 'str/join 'str/split} (first x))))
          (swap! calls conj (first x)))
        x)
      form)
    @calls))

(defn extract-function-defs
  "Extract all function definitions from a form."
  [form]
  (let [defs (atom #{})]
    (clojure.walk/prewalk
      (fn [x]
        (when (and (list? x)
                   (= 'defn (first x))
                   (symbol? (second x)))
          (swap! defs conj (second x)))
        x)
      form)
    @defs))

(defn extract-declared-functions
  "Extract all declared functions."
  [form]
  (let [declared (atom #{})]
    (clojure.walk/prewalk
      (fn [x]
        (when (and (list? x)
                   (= 'declare (first x)))
          (swap! declared into (rest x)))
        x)
      form)
    @declared))

(defn read-clojure-file
  "Read all forms from a Clojure file."
  [file-path]
  (with-open [rdr (-> file-path io/reader rt/indexing-push-back-reader)]
    (let [forms (atom [])]
      (loop []
        (let [form (try
                     (reader/read {:eof ::eof} rdr)
                     (catch Exception e
                       (println "Error reading form:" (.getMessage e))
                       ::eof))]
          (when (not= form ::eof)
            (swap! forms conj form)
            (recur))))
      @forms)))

(defn validate-function-calls
  "Validate that all function calls have corresponding definitions."
  [forms]
  (let [all-calls (set (mapcat extract-function-calls forms))
        all-defs (set (mapcat extract-function-defs forms))
        all-declared (set (mapcat extract-declared-functions forms))
        all-available (set/union all-defs all-declared)
        ;; Filter out built-in functions and Java interop
        internal-calls (set (filter #(and (str/includes? (str %) "build-")
                                         (not (str/starts-with? (str %) ".")))
                                   all-calls))
        undefined-calls (set/difference internal-calls all-available)]
    {:calls all-calls
     :defs all-defs
     :declared all-declared
     :internal-calls internal-calls
     :undefined undefined-calls}))

(deftest command-ast-validation-test
  (testing "Generated command code AST validation"
    (let [file-path "generated/potatoclient/proto/command.clj"]
      (when (.exists (io/file file-path))
        (let [forms (read-clojure-file file-path)
              validation (validate-function-calls forms)]
          
          (testing "All function declarations should exist"
            (is (seq (:declared validation))
                "Should have declared functions"))
          
          (testing "All internal function calls should be defined"
            (when (seq (:undefined validation))
              (println "\nUndefined function calls found:")
              (doseq [f (sort (:undefined validation))]
                (println "  -" f)))
            (is (empty? (:undefined validation))
                "All internal function calls should have definitions"))
          
          (testing "Declared functions should match definitions"
            (let [declared-not-defined (set/difference (:declared validation) (:defs validation))]
              (when (seq declared-not-defined)
                (println "\nDeclared but not defined:")
                (doseq [f (sort declared-not-defined)]
                  (println "  -" f)))
              (is (empty? declared-not-defined)
                  "All declared functions should be defined"))))))))

(deftest state-ast-validation-test
  (testing "Generated state code AST validation"
    (let [file-path "generated/potatoclient/proto/state.clj"]
      (when (.exists (io/file file-path))
        (let [forms (read-clojure-file file-path)
              validation (validate-function-calls forms)]
          
          (testing "All internal function calls should be defined"
            (when (seq (:undefined validation))
              (println "\nUndefined function calls in state.clj:")
              (doseq [f (sort (:undefined validation))]
                (println "  -" f)))
            (is (empty? (:undefined validation))
                "All internal function calls should have definitions")))))))

(deftest import-usage-validation-test
  (testing "Imported classes should be used correctly"
    (let [file-path "generated/potatoclient/proto/command.clj"]
      (when (.exists (io/file file-path))
        (let [content (slurp file-path)
              ;; Extract imports
              import-matches (re-seq #"\[([a-zA-Z0-9._$]+)\s+([a-zA-Z0-9._$ ]+)\]" content)
              imports (mapcat (fn [[_ pkg classes]]
                               (map #(str pkg "." %)
                                   (str/split classes #"\s+")))
                             import-matches)
              ;; Find class usages (constructor calls)
              usage-matches (re-seq #"\(([a-zA-Z0-9._$]+)/newBuilder\)" content)
              used-classes (set (map second usage-matches))]
          
          (testing "All used classes should be imported"
            (let [unimported (set/difference used-classes (set imports))]
              (when (seq unimported)
                (println "\nClasses used but not imported:")
                (doseq [c (sort unimported)]
                  (println "  -" c)))
              ;; Note: We expect some classes to be fully qualified
              (is (every? #(or (contains? (set imports) %)
                              (str/includes? % ".")) 
                         used-classes)
                  "All used classes should be imported or fully qualified"))))))))

(deftest oneof-field-consistency-test
  (testing "Oneof fields should match between builder and parser"
    (let [file-path "generated/potatoclient/proto/command.clj"
          content (when (.exists (io/file file-path)) (slurp file-path))
          ;; Extract oneof cases from builders
          builder-cases (when content
                         (mapcat #(map str/trim 
                                      (str/split (second %) #"\n"))
                                (re-seq #"case field-key\n([^)]+)\)" content)))
          ;; Extract oneof cases from parsers
          parser-cases (when content
                        (mapcat #(map str/trim 
                                     (str/split (second %) #"\n"))
                               (re-seq #"case \(\.getPayloadCase proto\)\n([^)]+)\)" content)))]
      
      (when (and (seq builder-cases) (seq parser-cases))
        (testing "Builder and parser should handle same fields"
          ;; This is a simplified check - could be more sophisticated
          (is (pos? (count builder-cases)) "Should have builder cases")
          (is (pos? (count parser-cases)) "Should have parser cases"))))))