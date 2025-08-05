(ns generator.parser
  "Parse JSON descriptors from proto-explorer output into internal representation."
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [taoensso.timbre :as log]))

;; =============================================================================
;; Case Conversion (borrowed from proto-explorer)
;; =============================================================================

(defn snake->kebab
  "Convert snake_case to kebab-case."
  [s]
  (-> s
      (str/replace #"_+" "-")
      (str/replace #"^-+" "")
      (str/replace #"-+$" "")))

(defn camel->kebab
  "Convert camelCase or PascalCase to kebab-case."
  [s]
  (-> s
      (str/replace #"([a-z\d])([A-Z])" "$1-$2")
      (str/replace #"([A-Z]+)([A-Z][a-z])" "$1-$2")
      str/lower-case))

(defn normalize-key
  "Convert a string key to a normalized kebab-case keyword."
  [k]
  (when (string? k)
    (-> k
        camel->kebab
        snake->kebab
        keyword)))

;; =============================================================================
;; Protobuf Type Mapping
;; =============================================================================

(def proto-type->clj-type
  "Map protobuf types to Clojure types for code generation."
  {:type-double   :double
   :type-float    :float
   :type-int64    :long
   :type-uint64   :long
   :type-int32    :int
   :type-fixed64  :long
   :type-fixed32  :int
   :type-bool     :boolean
   :type-string   :string
   :type-bytes    :bytes
   :type-uint32   :int
   :type-enum     :enum
   :type-sfixed32 :int
   :type-sfixed64 :long
   :type-sint32   :int
   :type-sint64   :long
   :type-message  :message})

(def proto-type->java-setter
  "Map protobuf types to Java setter method patterns."
  {:type-double   ".set~{field-name}"
   :type-float    ".set~{field-name}"
   :type-int64    ".set~{field-name}"
   :type-uint64   ".set~{field-name}"
   :type-int32    ".set~{field-name}"
   :type-fixed64  ".set~{field-name}"
   :type-fixed32  ".set~{field-name}"
   :type-bool     ".set~{field-name}"
   :type-string   ".set~{field-name}"
   :type-bytes    ".set~{field-name}"
   :type-uint32   ".set~{field-name}"
   :type-enum     ".set~{field-name}"
   :type-sfixed32 ".set~{field-name}"
   :type-sfixed64 ".set~{field-name}"
   :type-sint32   ".set~{field-name}"
   :type-sint64   ".set~{field-name}"
   :type-message  ".set~{field-name}"})

;; =============================================================================
;; JSON Descriptor Parsing
;; =============================================================================

(defn keywordize-keys
  "Custom key function for Cheshire that converts keys to kebab-case keywords."
  [k]
  (normalize-key k))

(defn proto-constant?
  "Check if a string looks like a protobuf constant."
  [s]
  (and (string? s)
       (or (re-matches #"^TYPE_[A-Z0-9_]+$" s)
           (re-matches #"^LABEL_[A-Z_]+$" s))))

(defn convert-value
  "Convert protobuf constants to keywords."
  [v]
  (if (proto-constant? v)
    (normalize-key v)
    v))

(defn process-values
  "Walk the data structure and convert proto constants to keywords."
  [data]
  (walk/prewalk
    (fn [x]
      (cond
        (map? x)
        (reduce-kv
          (fn [m k v]
            (assoc m k (convert-value v)))
          {}
          x)
        :else x))
    data))

(defn load-json-descriptor
  "Load and parse a JSON descriptor file."
  [path]
  (with-open [reader (io/reader path)]
    (let [json-string (slurp reader)
          data (json/parse-string json-string keywordize-keys)]
      (process-values data))))

;; =============================================================================
;; Message Analysis
;; =============================================================================

(defn extract-package-name
  "Extract the package name from a file descriptor."
  [file-desc]
  (:package file-desc))

(defn extract-java-package
  "Extract the Java package from file options."
  [file-desc]
  (get-in file-desc [:options :java-package]))

(defn extract-messages
  "Extract all message types from a file descriptor."
  [file-desc]
  (:message-type file-desc))

(defn extract-enums
  "Extract all enum types from a file descriptor."
  [file-desc]
  (:enum-type file-desc))

(defn find-message-by-name
  "Find a specific message by name in the descriptor."
  [file-desc message-name]
  (first (filter #(= (:name %) message-name) 
                 (extract-messages file-desc))))

(defn field-type
  "Get the Clojure type for a field."
  [field]
  (let [proto-type (:type field)]
    (get proto-type->clj-type proto-type :unknown)))

(defn field-is-repeated?
  "Check if a field is repeated (a list)."
  [field]
  (= (:label field) :label-repeated))

(defn field-is-optional?
  "Check if a field is optional."
  [field]
  (= (:label field) :label-optional))

(defn field-belongs-to-oneof?
  "Check if a field belongs to a oneof group."
  [field]
  (some? (:oneof-index field)))

(defn extract-oneof-fields
  "Extract fields that belong to a specific oneof by index."
  [message oneof-index]
  (filter #(= (:oneof-index %) oneof-index)
          (:field message)))

(defn pascal-case
  "Convert kebab-case or snake_case to PascalCase for Java class names."
  [s]
  (->> (str/split (name s) #"[-_]")  ;; Split on both - and _
       (map str/capitalize)
       (str/join)))

(defn field->java-setter
  "Generate the Java setter method name for a field."
  [field]
  (str "set" (pascal-case (:name field))))

(defn field->java-getter
  "Generate the Java getter method name for a field."
  [field]
  (str "get" (pascal-case (:name field))))

(defn field->java-has
  "Generate the Java 'has' method name for optional fields."
  [field]
  (str "has" (pascal-case (:name field))))

(defn resolve-type-name
  "Resolve a fully qualified type name to its components."
  [type-name]
  (when type-name
    (let [parts (str/split type-name #"\.")
          ;; Remove leading empty string from absolute path
          parts (if (= (first parts) "") (rest parts) parts)]
      {:full-name type-name
       :package (str/join "." (butlast parts))
       :simple-name (last parts)})))

;; =============================================================================
;; Descriptor Analysis for Code Generation
;; =============================================================================

(defn analyze-message
  "Analyze a message for code generation, returning all needed info."
  [message package java-package java-outer-classname]
  {:name (:name message)
   :kebab-name (normalize-key (:name message))
   :java-class (cond
                ;; Special handling for google.protobuf messages
                (= package "google.protobuf")
                (str "com.google.protobuf." (:name message))
                
                ;; Special handling for buf.validate messages  
                (= package "buf.validate")
                (str "build.buf.validate." (:name message))
                
                ;; Regular messages with outer classname
                java-outer-classname
                (str (or java-package package) "." java-outer-classname "$" (:name message))
                
                ;; Regular messages without outer classname
                :else
                (str (or java-package package) "." (:name message)))
   :fields (map (fn [field]
                  {:name (:name field)
                   :kebab-name (normalize-key (:name field))
                   :number (:number field)
                   :type (field-type field)
                   :proto-type (:type field)
                   :type-name (:type-name field)
                   :repeated? (field-is-repeated? field)
                   :optional? (field-is-optional? field)
                   :oneof-index (:oneof-index field)
                   :java-setter (field->java-setter field)
                   :java-getter (field->java-getter field)
                   :java-has (when (field-is-optional? field)
                              (field->java-has field))})
                (:field message))
   :oneofs (map-indexed
             (fn [idx oneof]
               {:index idx
                :name (:name oneof)
                :kebab-name (normalize-key (:name oneof))
                :fields (map (fn [field]
                              {:name (:name field)
                               :kebab-name (normalize-key (:name field))
                               :number (:number field)
                               :type (field-type field)
                               :proto-type (:type field)
                               :type-name (:type-name field)
                               :java-setter (field->java-setter field)
                               :java-getter (field->java-getter field)})
                            (extract-oneof-fields message idx))})
             (:oneof-decl message))})

(defn analyze-enum
  "Analyze an enum for code generation."
  [enum-type package java-package java-outer-classname]
  {:name (:name enum-type)
   :kebab-name (normalize-key (:name enum-type))
   :java-class (cond
                ;; Special handling for google.protobuf enums
                (= package "google.protobuf")
                (str "com.google.protobuf." (:name enum-type))
                
                ;; Special handling for buf.validate enums
                (= package "buf.validate")
                (str "build.buf.validate." (:name enum-type))
                
                ;; Regular enums with outer classname
                java-outer-classname
                (str (or java-package package) "." java-outer-classname "$" (:name enum-type))
                
                ;; Regular enums without outer classname
                :else
                (str (or java-package package) "." (:name enum-type)))
   :values (map (fn [value]
                  {:name (:name value)
                   :kebab-name (normalize-key (:name value))
                   :number (:number value)})
                (:value enum-type))})

(defn analyze-file
  "Analyze an entire file descriptor for code generation."
  [file-desc]
  (let [package (extract-package-name file-desc)
        java-package (extract-java-package file-desc)
        java-outer-classname (get-in file-desc [:options :java-outer-classname])
        ;; If no outer classname is set, derive it from the filename
        default-outer-classname (when-not java-outer-classname
                                  (let [base-name (-> (:name file-desc)
                                                     (str/split #"/")
                                                     last
                                                     (str/replace #"\.proto$" ""))]
                                    ;; Convert snake_case to PascalCase
                                    (->> (str/split base-name #"_")
                                         (map str/capitalize)
                                         str/join)))
        effective-outer-classname (or java-outer-classname default-outer-classname)]
    {:package package
     :java-package java-package
     :java-outer-classname effective-outer-classname
     :messages (map #(analyze-message % package java-package effective-outer-classname)
                   (extract-messages file-desc))
     :enums (map #(analyze-enum % package java-package effective-outer-classname)
                (extract-enums file-desc))}))

;; =============================================================================
;; Main API
;; =============================================================================

(defn parse-descriptor-set
  "Parse a descriptor set JSON file containing multiple proto files."
  [path]
  (let [desc-set (load-json-descriptor path)
        files (:file desc-set)]
    (log/info "Loaded descriptor set with" (count files) "files")
    (map analyze-file files)))

(defn parse-single-descriptor
  "Parse a single proto file descriptor."
  [path]
  (let [desc (load-json-descriptor path)
        filename (-> path io/file .getName (str/replace #"\.json$" ".proto"))]
    (if (:file desc)
      ;; It's a descriptor set, find the matching file
      (if-let [matching-file (first (filter #(= (:name %) filename) (:file desc)))]
        (analyze-file matching-file)
        ;; If no match, take the last file (usually the main one)
        (analyze-file (last (:file desc))))
      ;; It's already a single file descriptor
      (analyze-file desc))))

(defn find-proto-files
  "Find all relevant proto files for code generation.
  Returns a map of {:commands <file> :state <file>}"
  [descriptor-dir]
  (let [cmd-file (io/file descriptor-dir "jon_shared_cmd.json")
        state-file (io/file descriptor-dir "jon_shared_data.json")]
    (when-not (.exists cmd-file)
      (throw (ex-info "Command descriptor file not found"
                      {:path (.getPath cmd-file)})))
    (when-not (.exists state-file)
      (throw (ex-info "State descriptor file not found"
                      {:path (.getPath state-file)})))
    {:commands (.getPath cmd-file)
     :state (.getPath state-file)}))