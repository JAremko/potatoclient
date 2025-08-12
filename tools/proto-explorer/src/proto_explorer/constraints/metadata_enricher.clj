(ns proto-explorer.constraints.metadata-enricher
  "Enriches EDN data structures with constraint metadata.
  
  This namespace is responsible for extracting constraints from the
  options field and attaching them as metadata to the relevant nodes."
  (:require [clojure.walk :as walk]))

(defn extract-buf-validate-constraints
  "Extract buf.validate constraints from a node's options."
  [options]
  (when-let [buf-validate (get options (keyword "[buf.validate.field]"))]
    {:buf.validate buf-validate}))

(defn extract-oneof-constraints
  "Extract buf.validate constraints for oneof declarations."
  [options]
  (when-let [buf-validate (get options (keyword "[buf.validate.oneof]"))]
    {:buf.validate buf-validate}))

(defn extract-comments
  "Extract documentation from source code info if available."
  [node path source-code-info]
  ;; TODO: Implement source code info extraction
  nil)

(defn enrich-field
  "Enrich a field with constraint metadata."
  [field]
  (if-let [constraints (extract-buf-validate-constraints (:options field))]
    (-> field
        (dissoc :options)  ; Remove options from structure
        (with-meta {:constraints constraints
                    :original-options (:options field)
                    :field-type :field}))
    field))

(defn enrich-oneof
  "Enrich a oneof declaration with constraint metadata."
  [oneof]
  (if-let [constraints (extract-oneof-constraints (:options oneof))]
    (-> oneof
        (dissoc :options)
        (with-meta {:constraints constraints
                    :original-options (:options oneof)
                    :field-type :oneof}))
    oneof))

(defn enrich-message
  "Enrich a message with metadata, processing its fields and oneofs."
  [message]
  (cond-> message
    ;; Process fields
    (:field message)
    (update :field (fn [fields]
                    (mapv enrich-field fields)))
    
    ;; Process oneofs
    (:oneof-decl message)
    (update :oneof-decl (fn [oneofs]
                         (mapv enrich-oneof oneofs)))
    
    ;; Process nested messages
    (:nested-type message)
    (update :nested-type (fn [nested]
                          (mapv enrich-message nested)))))

(defn enrich-enum
  "Enrich an enum with metadata."
  [enum-type]
  ;; For now, just preserve the enum
  ;; TODO: Add enum value constraints if needed
  enum-type)

(defn enrich-file-descriptor
  "Enrich a single file descriptor with constraint metadata."
  [file-desc]
  (cond-> file-desc
    ;; Process message types
    (:messageType file-desc)
    (update :messageType (fn [messages]
                           (mapv enrich-message messages)))
    
    ;; Process enums
    (:enumType file-desc)
    (update :enumType (fn [enums]
                        (mapv enrich-enum enums)))))

(defn enrich-descriptor
  "Main entry point to enrich a complete descriptor with constraint metadata.
  
  Takes a descriptor (as returned by json-to-edn) and returns the same
  structure with constraint information attached as metadata to relevant nodes."
  [descriptor]
  (if (contains? descriptor :file)
    (update descriptor :file 
            (fn [files]
              (mapv enrich-file-descriptor files)))
    descriptor))

;; =============================================================================
;; Metadata Access Helpers
;; =============================================================================

(defn get-constraints
  "Get constraints from a node's metadata."
  [node]
  (-> node meta :constraints))

(defn get-field-type
  "Get the field type from metadata."
  [node]
  (-> node meta :field-type))

(defn has-constraints?
  "Check if a node has constraints attached."
  [node]
  (boolean (get-constraints node)))

(defn get-original-options
  "Get the original options map that was removed from the structure."
  [node]
  (-> node meta :original-options))

;; =============================================================================
;; Debug Helpers
;; =============================================================================

(defn print-with-metadata
  "Print a data structure showing its metadata."
  [data]
  (walk/prewalk
   (fn [node]
     (if (and (map? node) (meta node))
       (assoc node ::metadata (meta node))
       node))
   data))

(defn strip-metadata
  "Remove all metadata from a data structure."
  [data]
  (walk/prewalk
   (fn [node]
     (if (meta node)
       (with-meta node nil)
       node))
   data))