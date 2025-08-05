(ns generator.backend
  "Backend for the proto-clj-generator using Specter.
  Parses protobuf JSON descriptors into EDN intermediate representation."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :as json]
            [camel-snake-kebab.core :as csk]
            [com.rpl.specter :as sp]
            [taoensso.timbre :as log]))

;; =============================================================================
;; Specter Paths
;; =============================================================================

(def ALL-FILES 
  "Path to all files in a descriptor set"
  [:file sp/ALL])

(def ALL-MESSAGES
  "Path to all messages in files"
  [ALL-FILES :message-type sp/ALL])

(def ALL-ENUMS
  "Path to all enums in files"
  [ALL-FILES :enum-type sp/ALL])

(def ALL-FIELDS
  "Path to all fields in a message"
  [:field sp/ALL])

(def NESTED-MESSAGES
  "Recursive path to all messages including nested ones"
  (sp/recursive-path [] p
    (sp/if-path seq?
      [sp/ALL (sp/multi-path
                sp/STAY
                [:nested-types p])]
      sp/STAY)))

(def ALL-MESSAGES-WITH-CONTEXT
  "Path that collects package context while traversing messages"
  [ALL-FILES 
   (sp/collect-one (sp/multi-path :package :java-package :java-outer-classname))
   :messages NESTED-MESSAGES])

(def ALL-ENUMS-WITH-CONTEXT  
  "Path that collects package context while traversing enums"
  [ALL-FILES
   (sp/collect-one (sp/multi-path :package :java-package :java-outer-classname))
   :enums sp/ALL])

;; =============================================================================
;; Value Conversion
;; =============================================================================

(defn keywordize-value
  "Convert protobuf constant values to keywords."
  [v]
  (cond
    (and (string? v) (re-matches #"[A-Z][A-Z0-9_]*" v))
    (csk/->kebab-case-keyword v)
    
    (keyword? v) v
    
    :else v))

;; =============================================================================
;; Type Resolution
;; =============================================================================

(def proto-type-mapping
  "Mapping from protobuf types to our intermediate representation."
  {;; Direct keyword mapping (for already processed data)
   :double   {:scalar :double}
   :float    {:scalar :float}
   :int32    {:scalar :int32}
   :int64    {:scalar :int64}
   :uint32   {:scalar :uint32}
   :uint64   {:scalar :uint64}
   :sint32   {:scalar :sint32}
   :sint64   {:scalar :sint64}
   :fixed32  {:scalar :fixed32}
   :fixed64  {:scalar :fixed64}
   :sfixed32 {:scalar :sfixed32}
   :sfixed64 {:scalar :sfixed64}
   :bool     {:scalar :bool}
   :string   {:scalar :string}
   :bytes    {:scalar :bytes}
   ;; String to keyword mapping (from JSON)
   :type-double   {:scalar :double}
   :type-float    {:scalar :float}
   :type-int32    {:scalar :int32}
   :type-int64    {:scalar :int64}
   :type-uint32   {:scalar :uint32}
   :type-uint64   {:scalar :uint64}
   :type-sint32   {:scalar :sint32}
   :type-sint64   {:scalar :sint64}
   :type-fixed32  {:scalar :fixed32}
   :type-fixed64  {:scalar :fixed64}
   :type-sfixed32 {:scalar :sfixed32}
   :type-sfixed64 {:scalar :sfixed64}
   :type-bool     {:scalar :bool}
   :type-string   {:scalar :string}
   :type-bytes    {:scalar :bytes}})

(defn resolve-field-type
  "Resolve a field's type to our intermediate representation."
  [{:keys [type typeName] :as field}]
  (let [type-kw (if (keyword? type) type (keywordize-value type))]
    (cond
      (contains? proto-type-mapping type-kw)
      (proto-type-mapping type-kw)
      
      (or (= type-kw :enum) (= type-kw :type-enum))
      {:enum {:type-ref typeName}}
      
      (or (= type-kw :message) (= type-kw :type-message))
      {:message {:type-ref typeName}}
      
      :else
      {:unknown {:proto-type type-kw}})))

;; =============================================================================
;; Java Class Name Generation
;; =============================================================================

(defn generate-java-class-name
  "Generate the full Java class name for a message or enum."
  [{:keys [package java-package java-outer-classname]} type-name parent-names]
  (let [pkg (or java-package package)
        outer-class (or java-outer-classname 
                       (csk/->PascalCase (last (str/split package #"\."))))
        nested-path (if (seq parent-names)
                     (str "$" (str/join "$" parent-names) "$" type-name)
                     (str "$" type-name))]
    (str pkg "." outer-class nested-path)))

;; =============================================================================
;; EDN Conversion using Specter
;; =============================================================================

(defn field->edn
  "Convert a protobuf field to EDN representation."
  [field]
  (let [base {:name (csk/->kebab-case-keyword (:name field))
              :proto-name (:name field)
              :number (:number field)
              :type (resolve-field-type field)}]
    (cond-> base
      (:label field)
      (assoc :label (keywordize-value (:label field)))
      
      (= (keywordize-value (:label field)) :label-repeated)
      (assoc :repeated? true)
      
      (= (keywordize-value (:label field)) :label-optional)
      (assoc :optional? true)
      
      (some? (:oneofIndex field))
      (assoc :oneof-index (:oneofIndex field))
      
      (:defaultValue field)
      (assoc :default-value (:defaultValue field)))))

(defn message->edn
  "Convert a protobuf message to EDN representation."
  [message context parent-names]
  (let [fields (sp/select [:field sp/ALL] message)
        oneof-decls (sp/select [:oneofDecl sp/ALL] message)
        java-class (generate-java-class-name context (:name message) parent-names)]
    
    {:type :message
     :name (csk/->kebab-case-keyword (:name message))
     :proto-name (:name message)
     :java-class java-class
     :fields (mapv field->edn 
                   (remove #(some? (:oneofIndex %)) fields))
     :oneofs (vec (map-indexed 
                   (fn [idx oneof-decl]
                     {:name (csk/->kebab-case-keyword (:name oneof-decl))
                      :proto-name (:name oneof-decl)
                      :index idx
                      :fields (mapv field->edn
                                   (filter #(= (:oneofIndex %) idx) fields))})
                   oneof-decls))
     :nested-types (vec (map #(message->edn % context (conj parent-names (:name message)))
                            (get message :nestedType [])))}))

(defn enum->edn
  "Convert a protobuf enum to EDN representation."
  [enum context parent-names]
  {:type :enum
   :name (csk/->kebab-case-keyword (:name enum))
   :proto-name (:name enum)
   :java-class (generate-java-class-name context (:name enum) parent-names)
   :values (mapv (fn [v] 
                   {:name (-> (:name v)
                             ;; Pre-process to handle special cases like 1D, 2D, 3D
                             (str/replace #"_(\d+)D" "_$1d")
                             (str/replace #"_(\d+)H" "_$1h")
                             (csk/->kebab-case-keyword))
                    :proto-name (:name v)
                    :number (:number v)})
                 (:value enum))})

(defn file->edn
  "Convert a protobuf file descriptor to EDN."
  [file]
  (let [;; If no explicit Java outer classname, derive from filename
        default-outer-class (when-let [filename (:name file)]
                             (-> filename
                                 (str/replace #"\.proto$" "")
                                 (csk/->PascalCase)))
        context {:package (:package file)
                 :java-package (sp/select-first [:options :javaPackage] file)
                 :java-outer-classname (or (sp/select-first [:options :javaOuterClassname] file)
                                          default-outer-class)}]
    {:type :file
     :name (:name file)
     :package (:package file)
     :java-package (:java-package context)
     :java-outer-classname (:java-outer-classname context)
     :messages (mapv #(message->edn % context [])
                     (get file :messageType []))
     :enums (mapv #(enum->edn % context [])
                 (get file :enumType []))}))

;; =============================================================================
;; Type Lookup Building
;; =============================================================================

(defn get-canonical-type-ref
  "Get the canonical reference for a type."
  [type-def file-context parent-names]
  (let [package (:package file-context)
        proto-name (:proto-name type-def)]
    (if (seq parent-names)
      (str package "." (str/join "." parent-names) "." proto-name)
      (str package "." proto-name))))

(defn collect-message-with-path
  "Helper to collect messages with their parent path"
  [parent-names msg]
  (cons {:message msg :parent-names parent-names}
        (mapcat #(collect-message-with-path 
                  (conj parent-names (:proto-name msg)) 
                  %)
               (:nested-types msg []))))

(defn collect-all-types
  "Collect all types from EDN data with their canonical references using Specter."
  [edn-data]
  (let [;; Collect files with context
        files-with-context (sp/select 
                           [:files sp/ALL
                            (sp/submap [:package :java-package :java-outer-classname :messages :enums])]
                           edn-data)]
    
    (mapcat 
     (fn [file]
       (let [file-context (select-keys file [:package :java-package :java-outer-classname])]
         (concat
          ;; Process all messages (including nested)
          (for [msg-info (mapcat #(collect-message-with-path [] %) (:messages file))
                :let [msg (:message msg-info)
                      parent-names (:parent-names msg-info)
                      canonical (get-canonical-type-ref msg file-context parent-names)]]
            [canonical msg])
          
          ;; Process enums
          (for [enum (:enums file)
                :let [canonical (get-canonical-type-ref enum file-context [])]]
            [canonical enum]))))
     files-with-context)))

(defn build-type-lookup
  "Build a lookup map using canonical type references."
  [edn-data]
  (into {} (vec (collect-all-types edn-data))))

;; =============================================================================
;; JSON Processing
;; =============================================================================

(defn process-json-value
  "Process JSON values, converting string keywords to actual keywords."
  [v]
  (cond
    (map? v) (into {} (map (fn [[k val]]
                            [k (if (and (string? val) 
                                       (not= k :name) ; Don't convert name fields
                                       (or (re-matches #"TYPE_[A-Z][A-Z0-9_]*" val)
                                           (re-matches #"LABEL_[A-Z][A-Z0-9_]*" val)
                                           (re-matches #"RETENTION_[A-Z][A-Z0-9_]*" val)
                                           (re-matches #"TARGET_[A-Z][A-Z0-9_]*" val)))
                                (keywordize-value val)
                                (process-json-value val))])
                          v))
    (vector? v) (mapv process-json-value v)
    :else v))

(defn load-json-descriptor
  "Load and parse a JSON descriptor file."
  [path]
  (-> path
      io/reader
      (json/parse-stream true)
      process-json-value))

;; =============================================================================
;; Main API
;; =============================================================================

(defn parse-descriptor-set
  "Parse a protobuf descriptor set JSON file into EDN representation."
  [descriptor-path]
  (log/info "Parsing descriptor set with" 
           (count (sp/select ALL-FILES (load-json-descriptor descriptor-path))) 
           "files")
  (let [descriptor (load-json-descriptor descriptor-path)
        ;; Filter out internal protobuf packages before processing
        files (->> (sp/select ALL-FILES descriptor)
                  (remove #(contains? #{"google.protobuf" "buf.validate"} 
                                     (:package %))))]
    {:type :descriptor-set
     :files (mapv file->edn files)}))

(defn parse-all-descriptors
  "Parse both command and state descriptors, building a unified type lookup."
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
          
          ;; Filter internal packages from output
          filter-internal (fn [descriptor-set]
                           (sp/setval [ALL-FILES 
                                      sp/ALL 
                                      #(contains? #{"google.protobuf" "buf.validate"} 
                                                 (:package %))]
                                     sp/NONE
                                     descriptor-set))
          
          ;; Build type lookup from all files
          all-files (concat (:files cmd-edn) (:files state-edn))
          type-lookup (build-type-lookup {:type :combined :files all-files})]
      
      {:command (filter-internal cmd-edn)
       :state (filter-internal state-edn)
       :type-lookup type-lookup})))