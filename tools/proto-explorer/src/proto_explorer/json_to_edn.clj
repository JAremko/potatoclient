(ns proto-explorer.json-to-edn
  "Convert JSON descriptors to EDN with proper keyword conversion.
  
  This namespace handles the conversion of protoc-generated JSON descriptors
  to Clojure EDN format with:
  - String keys → keywords (preserving original case)
  - Preservation of actual string values (descriptions, etc.)
  - Constraint metadata attachment via metadata enricher"
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [clojure.pprint :as pprint]
            [proto-explorer.constraints.metadata-enricher :as enricher]))

;; =============================================================================
;; Case Conversion
;; =============================================================================

(defn snake->kebab
  "Convert snake_case to kebab-case.
  Handles edge cases like consecutive underscores and leading/trailing underscores."
  [s]
  (-> s
      (str/replace #"_+" "-")  ; Replace one or more underscores with single dash
      (str/replace #"^-+" "")  ; Remove leading dashes
      (str/replace #"-+$" ""))) ; Remove trailing dashes

(defn camel->kebab
  "Convert camelCase or PascalCase to kebab-case."
  [s]
  (-> s
      ;; Insert dash before uppercase letters that follow lowercase
      (str/replace #"([a-z\d])([A-Z])" "$1-$2")
      ;; Insert dash before uppercase letters followed by lowercase
      (str/replace #"([A-Z]+)([A-Z][a-z])" "$1-$2")
      str/lower-case))

(defn normalize-key
  "Convert a string key to a keyword, preserving the original case.
  This matches Pronto's behavior of keeping the original field names."
  [k]
  (when (string? k)
    (keyword k)))

;; =============================================================================
;; JSON Parsing with Custom Key Function
;; =============================================================================

(defn keywordize-keys
  "Custom key function for Cheshire that converts keys to keywords.
  Preserves the original case as used by Pronto."
  [k]
  (normalize-key k))

(declare json->edn)

(defn load-json-descriptor
  "Load a JSON descriptor file and convert to EDN with proper keywords.
  
  Options:
  - :convert-values? - Also convert enum-like string values to keywords (default: true)
  - :enrich-metadata? - Attach constraint metadata via enricher (default: true)"
  ([path]
   (load-json-descriptor path {:convert-values? true :enrich-metadata? true}))
  ([path options]
   (with-open [reader (io/reader path)]
     (let [json-string (slurp reader)]
       (json->edn json-string options)))))


;; =============================================================================
;; Selective Value Conversion
;; =============================================================================

(defn proto-type-string?
  "Check if a string looks like a protobuf type constant."
  [s]
  (and (string? s)
       (re-matches #"^TYPE_[A-Z0-9_]+$" s)))

(defn proto-label-string?
  "Check if a string looks like a protobuf label constant."
  [s]
  (and (string? s)
       (re-matches #"^LABEL_[A-Z_]+$" s)))

(defn should-convert-value?
  "Determine if a string value should be converted to a keyword.
  
  Convert:
  - Protobuf type constants (TYPE_STRING → :TYPE_STRING)
  - Protobuf label constants (LABEL_OPTIONAL → :LABEL_OPTIONAL)
  - Known enum-like values
  
  Preserve:
  - Descriptions and comments
  - File paths
  - Package names
  - Type names (e.g., '.cmd.Root')"
  [k v]
  (and (string? v)
       (or (proto-type-string? v)
           (proto-label-string? v)
           ;; Add more patterns as needed
           )))

(defn convert-value
  "Convert a string value to keyword if appropriate."
  [k v]
  (if (should-convert-value? k v)
    (keyword v)
    v))

(defn convert-constraint-values
  "Convert constraint values to appropriate types.
  Numeric constraints should be numbers, not strings."
  [constraints]
  (reduce-kv
    (fn [m k v]
      (cond
        ;; Numeric constraint keys that should have numeric values
        (and (#{:gte :gt :lte :lt :const} k)
             (string? v))
        (assoc m k (parse-double v))
        
        ;; For nested maps, recurse
        (map? v)
        (assoc m k (convert-constraint-values v))
        
        ;; Otherwise keep as-is
        :else
        (assoc m k v)))
    {}
    constraints))

(defn process-values
  "Walk the data structure and selectively convert string values."
  [data]
  (walk/prewalk
    (fn [x]
      (cond
        ;; Special handling for buf.validate constraints
        (and (map? x)
             (contains? x (keyword "[buf.validate.field]")))
        (update x (keyword "[buf.validate.field]") convert-constraint-values)
        
        ;; Regular map processing
        (map? x)
        (reduce-kv
          (fn [m k v]
            (assoc m k (convert-value k v)))
          {}
          x)
        
        :else x))
    data))

;; =============================================================================
;; Main API
;; =============================================================================

(defn json->edn
  "Convert JSON string to EDN with proper keyword conversion.
  
  Takes a JSON string and converts it to EDN with:
  - String keys → keywords (preserving original case)
  - Proto constants → keywords (when convert-values? is true)
  
  Options:
  - :convert-values? - Also convert enum-like string values to keywords (default: true)
  - :enrich-metadata? - Attach constraint metadata via enricher (default: true)"
  ([json-string]
   (json->edn json-string {:convert-values? true :enrich-metadata? true}))
  ([json-string {:keys [convert-values? enrich-metadata?] 
                 :or {convert-values? true enrich-metadata? true}}]
   (when-not (string? json-string)
     (throw (IllegalArgumentException. 
             "json->edn expects a JSON string. Use load-json-descriptor for files.")))
   (let [data (json/parse-string json-string keywordize-keys)
         ;; Apply value conversion if requested
         data (if convert-values?
                (process-values data)
                data)]
     ;; Apply metadata enrichment if requested
     (if enrich-metadata?
       (enricher/enrich-descriptor data)
       data))))

;; =============================================================================
;; Utility Functions
;; =============================================================================

(defn find-file-descriptor
  "Find a specific proto file descriptor by name pattern."
  [edn-data pattern]
  (->> edn-data
       :file
       (filter #(re-find pattern (:name %)))))

(defn extract-messages
  "Extract all messages from a file descriptor."
  [file-desc]
  (let [package (:package file-desc)]
    (->> (:messageType file-desc)
         (map (fn [msg]
                (assoc msg
                       :full-name (str package "." (:name msg))
                       :package package))))))

(defn find-message
  "Find a specific message by name in the descriptor set."
  [edn-data message-name]
  (for [file-desc (:file edn-data)
        msg (:messageType file-desc)
        :when (or (= message-name (:name msg))
                  (= message-name (str (:package file-desc) "." (:name msg))))]
    (assoc msg 
           :package (:package file-desc)
           :full-name (str (:package file-desc) "." (:name msg)))))

;; =============================================================================
;; Pretty Printing
;; =============================================================================

(defn save-edn
  "Save EDN data to a file with pretty printing."
  [data path]
  (with-open [writer (io/writer path)]
    (binding [*out* writer]
      (pprint/pprint data))))

;; =============================================================================
;; Example Usage
;; =============================================================================

(comment
  ;; Load a JSON descriptor with metadata enrichment
  (def desc (json->edn "/tmp/json-output/descriptor-set.json"))
  
  ;; Load without metadata enrichment
  (def desc-plain (json->edn "/tmp/json-output/descriptor-set.json" 
                             {:enrich-metadata? false}))
  
  ;; Find command proto files
  (find-file-descriptor desc #"cmd")
  
  ;; Extract messages from a specific file
  (def cmd-file (first (find-file-descriptor desc #"jon_shared_cmd\.proto")))
  (extract-messages cmd-file)
  
  ;; Find a specific message
  (find-message desc "Root")
  
  ;; Check if a field has constraints
  (def rotary-msg (first (find-message desc "SetAzimuthValue")))
  (def value-field (first (filter #(= (:name %) "value") (:field rotary-msg))))
  (enricher/has-constraints? value-field)
  (enricher/get-constraints value-field)
  
  ;; Save as EDN
  (save-edn desc "/tmp/descriptor-set.edn")
  
  ;; Test case conversions (Note: snake->kebab and camel->kebab still available but not used)
  ;; normalize-key now preserves original case:
  (normalize-key "message_type")      ; => :message_type
  (normalize-key "TYPE_STRING")       ; => :TYPE_STRING
  (normalize-key "messageType")       ; => :messageType
  (normalize-key "PascalCase")        ; => :PascalCase
  (normalize-key "snake_case_field")  ; => :snake_case_field
  )