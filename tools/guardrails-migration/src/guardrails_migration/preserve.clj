(ns guardrails-migration.preserve
  "Migration that preserves formatting using string manipulation"
  (:require [clojure.string :as str]))

;; ============================================================================
;; String-based conversion helpers
;; ============================================================================

(defn convert-spec-str
  "Convert a single spec string from Guardrails to Malli"
  [spec-str]
  (let [trimmed (str/trim spec-str)]
    (cond
      ;; (? spec) -> [:maybe spec]
      (str/starts-with? trimmed "(? ")
      (str "[:maybe " (convert-spec-str (subs trimmed 3 (- (count trimmed) 1))) "]")
      
      ;; Predicate function int? -> :int
      (and (str/ends-with? trimmed "?")
           (not (str/includes? trimmed " "))
           (not (str/starts-with? trimmed ":")))
      (let [base-name (str/replace trimmed #"\?$" "")]
        (case base-name
          "nil" ":nil"
          "any" ":any"
          "boolean" ":boolean"
          "string" ":string"
          "int" ":int"
          "double" ":double"
          "fn" ":fn"
          "ifn" ":ifn"
          "pos-int" "pos-int?"  ; Keep as predicate
          trimmed))  ; Keep unknown predicates as-is
      
      ;; Already a keyword or complex spec
      :else trimmed)))

(defn parse-gspec-line
  "Parse a gspec line like [int? string? => boolean?]"
  [gspec-line]
  (let [;; Remove brackets and split by =>
        clean (-> gspec-line
                  str/trim
                  (str/replace #"^\[" "")
                  (str/replace #"\]$" ""))
        parts (str/split clean #"\s*=>\s*" 2)]
    (when (= 2 (count parts))
      (let [args-str (first parts)
            ret-str (second parts)
            ;; Split args by whitespace, handling variadic
            args-parts (if (str/blank? args-str)
                         []
                         (str/split args-str #"\s+"))
            ;; Check for variadic
            variadic-idx (.indexOf args-parts "[:*")
            regular-args (if (>= variadic-idx 0)
                           (take variadic-idx args-parts)
                           (remove #(= "&" %) args-parts))
            variadic-spec (when (>= variadic-idx 0)
                            (str/join " " (drop variadic-idx args-parts)))]
        {:args (mapv convert-spec-str regular-args)
         :variadic variadic-spec
         :ret (convert-spec-str ret-str)}))))

(defn build-malli-schema
  "Build Malli schema string from parsed gspec"
  [{:keys [args variadic ret]}]
  (str "[:=> [:cat"
       (when (seq args)
         (str " " (str/join " " args)))
       (when variadic
         (str " " variadic))
       "] " ret "]"))

;; ============================================================================
;; File processing
;; ============================================================================

(defn remove-guardrails-require
  "Remove Guardrails require from namespace form"
  [ns-str]
  ;; Remove the entire line containing Guardrails require
  (-> ns-str
      (str/replace #".*com\.fulcrologic\.guardrails\.malli\.core.*\n?" "")
      (str/replace #".*com\.fulcrologic\.guardrails\.core.*\n?" "")))

(defn process-defn
  "Process a >defn form, converting to defn with metadata"
  [defn-str]
  (let [lines (str/split defn-str #"\n")
        first-line (first lines)]
    (cond
      ;; Single-line >defn
      (str/starts-with? (str/trim first-line) "(>defn")
      (let [;; Find the gspec line (has =>)
        gspec-line (first (filter #(str/includes? % "=>") lines))
        gspec-idx (when gspec-line (.indexOf lines gspec-line))]
    (if (and gspec-line gspec-idx)
      (let [parsed (parse-gspec-line gspec-line)
            schema (when parsed (build-malli-schema parsed))
            ;; Find where to insert metadata (after docstring if present)
            defn-line-idx 0
            name-line-idx 1
            potential-doc-idx 2
            has-docstring? (and (> (count lines) potential-doc-idx)
                                (str/starts-with? (str/trim (nth lines potential-doc-idx)) "\""))
            metadata-insert-idx (if has-docstring?
                                  (inc potential-doc-idx)
                                  potential-doc-idx)
            ;; Build new lines
            new-first-line (str/replace first-line #">defn-?" "defn")
            metadata-line (str "  {:malli/schema " schema "}")
            new-lines (concat
                       [new-first-line]
                       (subvec (vec lines) 1 metadata-insert-idx)
                       [metadata-line]
                       ;; Skip the gspec line and continue
                       (concat
                        (subvec (vec lines) metadata-insert-idx gspec-idx)
                        (subvec (vec lines) (inc gspec-idx))))]
        (str/join "\n" new-lines))
      ;; No gspec, just convert >defn to defn
      (str/replace defn-str #">defn-?" "defn")))
      
      ;; Multi-line >defn-
      (str/starts-with? (str/trim first-line) "(>defn-")
      (-> defn-str
          (str/replace #">defn-" "defn-")
          (process-defn))
      
      ;; Not a >defn form
      :else defn-str)))

(defn migrate-file
  "Migrate a file preserving formatting"
  [input-path output-path]
  (let [content (slurp input-path)
        ;; Split into forms while preserving structure
        forms (str/split content #"\n\n")
        migrated-forms (for [form forms]
                         (cond
                           ;; Namespace form
                           (str/starts-with? (str/trim form) "(ns ")
                           (remove-guardrails-require form)
                           
                           ;; >defn form
                           (or (str/starts-with? (str/trim form) "(>defn")
                               (str/starts-with? (str/trim form) "(>defn-"))
                           (process-defn form)
                           
                           ;; Keep as-is
                           :else form))
        result (str/join "\n\n" migrated-forms)]
    (spit output-path result)
    {:status :success
     :file output-path}))