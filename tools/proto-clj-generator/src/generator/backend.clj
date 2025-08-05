(ns generator.backend
  "Backend parser that converts protobuf JSON descriptors to EDN intermediate representation."
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [camel-snake-kebab.core :as csk]
            [taoensso.timbre :as log]))

;; =============================================================================
;; JSON Descriptor Parsing
;; =============================================================================

(defn proto-constant?
  "Check if a string looks like a protobuf constant."
  [s]
  (and (string? s)
       (or (re-matches #"^TYPE_[A-Z0-9_]+$" s)
           (re-matches #"^LABEL_[A-Z_]+$" s))))

(defn normalize-proto-constant
  "Convert protobuf constant to keyword."
  [s]
  (-> s
      (str/replace #"^TYPE_" "")
      (str/replace #"^LABEL_" "")
      str/lower-case
      (str/replace #"_" "-")
      keyword))

(defn keywordize-keys
  "Custom key function for Cheshire that converts keys to kebab-case keywords."
  [k]
  (csk/->kebab-case-keyword k))

(defn convert-value
  "Convert protobuf constants to keywords."
  [v]
  (cond
    (proto-constant? v) (normalize-proto-constant v)
    (string? v) v
    :else v))

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
;; Type Resolution
;; =============================================================================

(def proto-type-mapping
  "Map protobuf types to our intermediate representation."
  {:double   {:scalar :double}
   :float    {:scalar :float}
   :int64    {:scalar :int64}
   :uint64   {:scalar :uint64}
   :int32    {:scalar :int32}
   :fixed64  {:scalar :fixed64}
   :fixed32  {:scalar :fixed32}
   :bool     {:scalar :bool}
   :string   {:scalar :string}
   :bytes    {:scalar :bytes}
   :uint32   {:scalar :uint32}
   :sfixed32 {:scalar :sfixed32}
   :sfixed64 {:scalar :sfixed64}
   :sint32   {:scalar :sint32}
   :sint64   {:scalar :sint64}})

(defn resolve-field-type
  "Resolve a field's type to our intermediate representation."
  [{:keys [type type-name] :as field}]
  (cond
    ;; Scalar types
    (contains? proto-type-mapping type)
    (proto-type-mapping type)
    
    ;; Enum type
    (= type :enum)
    {:enum {:type-ref type-name}}
    
    ;; Message type
    (= type :message)
    {:message {:type-ref type-name}}
    
    ;; Unknown
    :else
    {:unknown {:proto-type type}}))

;; =============================================================================
;; Java Class Name Generation
;; =============================================================================

(defn generate-java-class-name
  "Generate the full Java class name for a message or enum."
  [{:keys [package java-package java-outer-classname name]}]
  (cond
    ;; Special handling for google.protobuf
    (= package "google.protobuf")
    (str "com.google.protobuf." name)
    
    ;; Special handling for buf.validate
    (= package "buf.validate")
    (str "build.buf.validate." name)
    
    ;; Regular messages with outer classname
    java-outer-classname
    (str (or java-package package) "." java-outer-classname "$" name)
    
    ;; Regular messages without outer classname
    :else
    (str (or java-package package) "." name)))

;; =============================================================================
;; EDN Intermediate Representation Generation
;; =============================================================================

;; Forward declarations
(declare message->edn enum->edn)

(defn field->edn
  "Convert a protobuf field to EDN representation."
  [field]
  (let [base {:name (csk/->kebab-case-keyword (:name field))
              :proto-name (:name field)
              :number (:number field)
              :type (resolve-field-type field)}]
    (cond-> base
      (:label field)
      (assoc :label (:label field))
      
      (= (:label field) :repeated)
      (assoc :repeated? true)
      
      (= (:label field) :optional)
      (assoc :optional? true)
      
      (some? (:oneof-index field))
      (assoc :oneof-index (:oneof-index field))
      
      (:default-value field)
      (assoc :default-value (:default-value field)))))

(defn oneof->edn
  "Convert a oneof declaration to EDN representation."
  [oneof-decl oneof-index fields]
  {:name (csk/->kebab-case-keyword (:name oneof-decl))
   :proto-name (:name oneof-decl)
   :index oneof-index
   :fields (vec (filter #(= (:oneof-index %) oneof-index) fields))})

(defn message->edn
  "Convert a protobuf message to EDN representation."
  [message context]
  (let [fields (mapv field->edn (:field message))
        oneofs (when (:oneof-decl message)
                 (vec (map-indexed 
                       (fn [idx oneof-decl]
                         (oneof->edn oneof-decl idx fields))
                       (:oneof-decl message))))]
    {:type :message
     :name (csk/->kebab-case-keyword (:name message))
     :proto-name (:name message)
     :java-class (generate-java-class-name 
                  (merge context {:name (:name message)}))
     :fields (vec (remove :oneof-index fields))
     :oneofs (or oneofs [])
     :nested-types (vec (concat
                         (map #(message->edn % context) 
                              (:nested-type message []))
                         (map #(enum->edn % context) 
                              (:enum-type message []))))}))

(defn enum->edn
  "Convert a protobuf enum to EDN representation."
  [enum-type context]
  {:type :enum
   :name (csk/->kebab-case-keyword (:name enum-type))
   :proto-name (:name enum-type)
   :java-class (generate-java-class-name 
                (merge context {:name (:name enum-type)}))
   :values (vec (map (fn [v]
                      {:name (csk/->kebab-case-keyword (:name v))
                       :proto-name (:name v)
                       :number (:number v)})
                    (:value enum-type)))})

(defn file->edn
  "Convert a protobuf file descriptor to EDN representation."
  [file-desc]
  (let [package (:package file-desc)
        java-package (get-in file-desc [:options :java-package])
        java-outer-classname (get-in file-desc [:options :java-outer-classname])
        
        ;; Derive outer classname from filename if not specified
        default-outer-classname 
        (when-not java-outer-classname
          (let [base-name (-> (:name file-desc)
                             (str/split #"/")
                             last
                             (str/replace #"\.proto$" ""))]
            (csk/->PascalCase base-name)))
        
        context {:package package
                 :java-package java-package
                 :java-outer-classname (or java-outer-classname 
                                          default-outer-classname)}]
    
    {:type :file
     :name (:name file-desc)
     :package package
     :java-package java-package
     :java-outer-classname (:java-outer-classname context)
     :syntax (:syntax file-desc)
     :dependencies (vec (:dependency file-desc []))
     :messages (vec (map #(message->edn % context) 
                        (:message-type file-desc [])))
     :enums (vec (map #(enum->edn % context) 
                     (:enum-type file-desc [])))
     :services (vec (:service file-desc []))}))

;; =============================================================================
;; Message Lookup Building
;; =============================================================================

(defn extract-all-types
  "Recursively extract all types (messages and enums) from a type definition."
  [type-def]
  (let [current (if (= (:type type-def) :message)
                  [(dissoc type-def :nested-types)]
                  [type-def])
        nested (when (= (:type type-def) :message)
                 (mapcat extract-all-types (:nested-types type-def [])))]
    (concat current nested)))

(defn build-type-lookup
  "Build a lookup map of all types from the EDN representation.
  Creates multiple lookup keys for each type to handle different reference styles."
  [edn-data]
  (let [all-types (if (= (:type edn-data) :file)
                    (concat
                     (mapcat extract-all-types (:messages edn-data))
                     (:enums edn-data))
                    ;; For descriptor sets
                    (mapcat (fn [file]
                             (concat
                              (mapcat extract-all-types (:messages file))
                              (:enums file)))
                           (:files edn-data)))]
    ;; Create multiple lookup entries for each type
    (reduce (fn [lookup type-def]
             (let [kebab-name (:name type-def)
                   proto-name (:proto-name type-def)
                   java-class (:java-class type-def)]
               (cond-> lookup
                 ;; By kebab-case name
                 kebab-name
                 (assoc kebab-name type-def)
                 
                 ;; By proto name
                 proto-name
                 (assoc (csk/->kebab-case-keyword proto-name) type-def)
                 
                 ;; By simple class name (last part after $)
                 (and java-class (str/includes? java-class "$"))
                 (assoc (csk/->kebab-case-keyword 
                         (last (str/split java-class #"\$"))) 
                        type-def))))
           {}
           all-types)))

;; =============================================================================
;; Main API
;; =============================================================================

(defn parse-descriptor-set
  "Parse a descriptor set JSON file to EDN intermediate representation."
  [path]
  (let [desc-set (load-json-descriptor path)
        files (:file desc-set)]
    (log/info "Parsing descriptor set with" (count files) "files")
    {:type :descriptor-set
     :files (mapv file->edn files)}))

(defn parse-single-file
  "Parse a single proto file descriptor to EDN intermediate representation."
  [path]
  (let [desc (load-json-descriptor path)]
    (if (:file desc)
      ;; It's a descriptor set, extract the relevant file
      (let [filename (-> path io/file .getName (str/replace #"\.json$" ".proto"))
            matching-file (or (first (filter #(= (:name %) filename) (:file desc)))
                             (last (:file desc)))]
        (file->edn matching-file))
      ;; It's already a single file descriptor
      (file->edn desc))))

(defn process-descriptor-files
  "Process descriptor files and return EDN representation with lookup."
  [descriptor-dir]
  (let [cmd-file (io/file descriptor-dir "jon_shared_cmd.json")
        state-file (io/file descriptor-dir "jon_shared_data.json")]
    
    (when-not (.exists cmd-file)
      (throw (ex-info "Command descriptor file not found"
                      {:path (.getPath cmd-file)})))
    (when-not (.exists state-file)
      (throw (ex-info "State descriptor file not found"
                      {:path (.getPath state-file)})))
    
    (let [cmd-edn (parse-descriptor-set (.getPath cmd-file))
          state-edn (parse-descriptor-set (.getPath state-file))
          
          ;; Filter out google.protobuf and buf.validate from the command/state files
          filter-files (fn [descriptor-set]
                        (update descriptor-set :files
                               (fn [files]
                                 (vec (filter #(not (contains? #{"google.protobuf" "buf.validate"}
                                                              (:package %)))
                                            files)))))
          
          ;; Combine all types for lookup (including google.protobuf and buf.validate for references)
          all-files (concat (:files cmd-edn) (:files state-edn))
          type-lookup (build-type-lookup {:type :combined :files all-files})]
      
      {:command (filter-files cmd-edn)
       :state (filter-files state-edn)
       :type-lookup type-lookup})))