(ns proto-explorer.spec-generator
  "Generate Malli specs from EDN-converted protobuf descriptors.
  
  This namespace implements a multimethod-based system for converting
  protobuf type information to Malli schemas, including support for:
  - Primitive types
  - Message references
  - Repeated fields
  - Optional fields
  - Oneof constraints
  - buf.validate annotations"
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [clojure.pprint :as pp]
            [proto-explorer.json-to-edn :as json-edn]
            [proto-explorer.buf-validate-extractor :as validate]
            [proto-explorer.constraints.metadata-enricher :as enricher]
            [proto-explorer.constraints.compiler :as constraint-compiler]))

;; =============================================================================
;; Type Mapping
;; =============================================================================

(def proto-type->malli
  "Map protobuf types to Malli schemas."
  {:type-double   :double
   :type-float    :double
   :type-int64    :int
   :type-uint64   :int
   :type-int32    :int
   :type-fixed64  :int
   :type-fixed32  :int
   :type-bool     :boolean
   :type-string   :string
   :type-bytes    :bytes
   :type-uint32   :int
   :type-sfixed32 :int
   :type-sfixed64 :int
   :type-sint32   :int
   :type-sint64   :int})

(defn resolve-type-name
  "Convert a protobuf type name to a Malli schema reference.
  
  Examples:
  - '.cmd.Root' → :cmd/root
  - '.ser.JonGuiData' → :ser/jon-gui-data"
  [type-name]
  (when type-name
    (let [parts (str/split type-name #"\.")
          ns-part (some #(when (not (str/blank? %)) %) (butlast parts))
          name-part (last parts)]
      (when (and ns-part name-part)
        (keyword ns-part (json-edn/camel->kebab name-part))))))

;; =============================================================================
;; Field Processing Multimethod
;; =============================================================================

(defmulti process-field
  "Process a protobuf field into a Malli schema.
  Dispatches on field type."
  (fn [field _context]
    (:type field)))

;; Primitive types
(doseq [[proto-type malli-type] proto-type->malli]
  (defmethod process-field proto-type
    [field context]
    malli-type))

;; Message type references
(defmethod process-field :type-message
  [field context]
  (or (resolve-type-name (:type-name field))
      :map)) ; Fallback if resolution fails

;; Enum types
(defmethod process-field :type-enum
  [field context]
  ;; TODO: Look up enum values from context
  (or (resolve-type-name (:type-name field))
      :keyword)) ; Enums typically map to keywords

;; Default fallback
(defmethod process-field :default
  [field context]
  (println "Warning: Unknown field type" (:type field) "for field" (:name field))
  :any)

;; =============================================================================
;; Field Modifiers
;; =============================================================================

(defn apply-field-label
  "Apply field label (repeated, optional) to the base schema.
  
  Note: Does not apply [:maybe ...] to oneof fields since oneof fields
  are alternatives where exactly one option must be chosen. The optionality
  is at the oneof level, not the individual field level."
  [schema field]
  (case (:label field)
    :label-repeated [:vector schema]
    :label-optional (if (:oneof-index field)
                      schema  ; Don't wrap oneof fields with [:maybe ...]
                      [:maybe schema])
    schema))

(defn apply-field-constraints
  "Apply buf.validate constraints to the schema using metadata."
  [schema field]
  ;; Check if field has constraint metadata
  (if-let [constraints (enricher/get-constraints field)]
    ;; Use the constraint compiler to apply constraints
    ;; We need to pass the field with its type and constraints
    (constraint-compiler/apply-constraints schema {:type (:type field)
                                                   :constraints constraints})
    ;; No constraints, return original schema
    schema))

(defn process-field-schema
  "Process a complete field into a Malli schema with all modifiers."
  [field context]
  (-> field
      (process-field context)
      (apply-field-label field)
      (apply-field-constraints field)))

;; =============================================================================
;; Message Processing
;; =============================================================================

(defn process-oneof
  "Process a oneof declaration into a Malli :oneof schema."
  [oneof-decl oneof-index fields context]
  (let [oneof-name (:name oneof-decl)
        oneof-fields (filter #(= (:oneof-index %) oneof-index) fields)]
    [:oneof
     (into {}
           (map (fn [field]
                  [(keyword (json-edn/snake->kebab (:name field)))
                   [:map [(keyword (json-edn/snake->kebab (:name field)))
                          (process-field-schema field context)]]])
                oneof-fields))]))

(defn process-message-fields
  "Process all fields in a message, handling oneofs specially."
  [message context]
  (let [fields (:field message)
        oneofs (:oneof-decl message)
        ;; Group fields by oneof index
        oneof-groups (group-by :oneof-index fields)
        regular-fields (get oneof-groups nil [])
        
        ;; Process regular fields
        regular-specs (map (fn [field]
                            [(keyword (json-edn/snake->kebab (:name field)))
                             (process-field-schema field context)])
                          regular-fields)
        
        ;; Process oneofs
        oneof-specs (map-indexed
                     (fn [idx oneof-decl]
                       [(keyword (json-edn/snake->kebab (:name oneof-decl)))
                        (process-oneof oneof-decl idx (get oneof-groups idx []) context)])
                     oneofs)]
    
    (into [:map] (concat regular-specs oneof-specs))))

(defn process-message
  "Process a protobuf message into a Malli schema."
  [message context]
  (let [message-name (keyword (json-edn/camel->kebab (:name message)))
        schema (process-message-fields message (assoc context :current-message message))]
    {message-name schema}))

;; =============================================================================
;; Enum Processing
;; =============================================================================

(defn process-enum
  "Process a protobuf enum into a Malli schema."
  [enum-type context]
  (let [enum-name (keyword (json-edn/camel->kebab (:name enum-type)))
        values (map (fn [v]
                     (keyword (json-edn/snake->kebab (json-edn/camel->kebab (:name v)))))
                   (:value enum-type))]
    {enum-name (into [:enum] values)}))

;; =============================================================================
;; File Processing
;; =============================================================================

(defn process-file-descriptor
  "Process a single proto file descriptor into Malli schemas."
  [file-desc context]
  (let [package (:package file-desc)
        messages (:message-type file-desc)
        enums (:enum-type file-desc)
        
        ;; Create namespace context
        ns-context (assoc context :current-package package)
        
        ;; Process all messages
        message-schemas (map #(process-message % ns-context) messages)
        
        ;; Process all enums
        enum-schemas (map #(process-enum % ns-context) enums)]
    
    {:package package
     :schemas (apply merge (concat message-schemas enum-schemas))}))

;; =============================================================================
;; Main API
;; =============================================================================

(defn generate-specs
  "Generate Malli specs from EDN descriptor data.
  
  Returns a map of namespace → schemas."
  [edn-data]
  (let [files (:file edn-data)
        context {:descriptor-set edn-data}]
    (->> files
         (map #(process-file-descriptor % context))
         (group-by :package)
         (map (fn [[pkg file-results]]
                [pkg (apply merge (map :schemas file-results))]))
         (into {}))))

;; =============================================================================
;; Spec File Generation
;; =============================================================================

(defn generate-namespace-declaration
  "Generate a namespace declaration for a spec file."
  [package]
  (let [ns-name (str "potatoclient.specs." (json-edn/snake->kebab package))]
    (list 'ns (symbol ns-name)
          "Generated Malli specs from protobuf descriptors"
          '(:require [malli.core :as m]
                     [malli.generator :as mg]))))

(defn serialize-schema
  "Convert a schema to a serializable form, handling function objects."
  [schema]
  (walk/postwalk
    (fn [x]
      (cond
        ;; Handle :fn schemas by converting to a quoted form
        (and (vector? x)
             (= :fn (first x))
             (map? (second x))
             (fn? (nth x 2 nil)))
        ;; Replace the function with a symbol placeholder
        [:fn (second x) 'FUNCTION-PLACEHOLDER]
        
        ;; Handle regex patterns - convert to string representation
        (instance? java.util.regex.Pattern x)
        (list 're-pattern (str x))
        
        :else x))
    schema))

(defn format-schema
  "Format a schema for pretty printing in a spec file."
  [schema-name schema]
  `(def ~(symbol (name schema-name))
     ~(str "Schema for " (name schema-name))
     ~(serialize-schema schema)))

(defn generate-spec-file
  "Generate a complete spec file for a package."
  [package schemas & {:keys [width] :or {width 80}}]
  (let [ns-decl (generate-namespace-declaration package)
        schema-defs (map (fn [[k v]] (format-schema k v)) schemas)]
    (str (with-out-str
           (binding [pp/*print-right-margin* width
                     pp/*print-miser-width* width]
             (pp/pprint ns-decl)))
         "\n"
         ";; Note: FUNCTION-PLACEHOLDER markers indicate where runtime functions are needed\n"
         ";; These will be replaced with actual implementations when loaded\n\n"
         (str/join "\n\n" 
                   (map (fn [schema-def]
                          (with-out-str
                            (binding [pp/*print-right-margin* width
                                      pp/*print-miser-width* width]
                              (pp/pprint schema-def))))
                        schema-defs)))))

;; =============================================================================
;; Example Usage
;; =============================================================================

(comment
  ;; Load JSON descriptor with metadata enrichment (default)
  (def edn-data (json-edn/json->edn "/tmp/json-output/descriptor-set.json"))
  
  ;; Generate specs (now with constraint support)
  (def specs (generate-specs edn-data))
  
  ;; Look at cmd specs
  (get specs "cmd")
  
  ;; Check a message with constraints
  (get-in specs ["cmd.RotaryPlatform" :set-azimuth-value])
  ;; Should show [:map [:value [:and [:maybe :float] [:>= 0] [:< 360]]]]
  
  ;; Generate spec file for cmd package
  (println (generate-spec-file "cmd" (get specs "cmd")))
  
  ;; Test type resolution
  (resolve-type-name ".cmd.Root")           ; => :cmd/root
  (resolve-type-name ".ser.JonGuiData")     ; => :ser/jon-gui-data
  
  ;; Test field processing with constraints
  (def test-field (with-meta {:type :type-float :name "value"}
                            {:constraints {:buf.validate {:float {:gte 0 :lt 360}}}}))
  (process-field-schema test-field {})     ; => [:and [:maybe :float] [:>= 0] [:< 360]]
  )