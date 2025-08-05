(ns generator.frontend
  "Frontend for the proto-clj-generator using rewrite-clj templates.
  Generates Clojure code from the EDN intermediate representation."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [camel-snake-kebab.core :as csk]
            [com.rpl.specter :as sp]
            [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]))

;; =============================================================================
;; Template Loading
;; =============================================================================

(defn load-template
  "Load a template file from resources."
  [template-name]
  (if-let [resource (io/resource (str "templates/" template-name))]
    (slurp resource)
    (throw (ex-info "Template not found" {:template template-name}))))

(defn templates
  "Load templates on demand."
  []
  {:namespace (load-template "namespace.clj")
   :builder (load-template "builder.clj")
   :oneof-builder (load-template "oneof-builder.clj")
   :parser (load-template "parser.clj")
   :oneof-parser (load-template "oneof-parser.clj")})

;; =============================================================================
;; Template Helpers
;; =============================================================================

;; =============================================================================
;; Code Generation Helpers
;; =============================================================================

(defn field-java-setter
  "Generate Java setter method name for a field."
  [field]
  (str "set" (csk/->PascalCase (:proto-name field))))

(defn field-java-getter
  "Generate Java getter method name for a field."
  [field]
  (str "get" (csk/->PascalCase (:proto-name field))))

(defn resolve-builder-name
  "Resolve the builder function name for a type reference."
  [type-ref type-lookup]
  (when type-ref
    (let [canonical-ref (str/replace type-ref #"^\." "")
          type-def (get type-lookup canonical-ref)]
      (if type-def
        (str "build-" (name (:name type-def)))
        (throw (ex-info "Cannot resolve type reference for builder"
                        {:type-ref type-ref
                         :canonical-ref canonical-ref
                         :available-types (keys type-lookup)}))))))

(defn resolve-parser-name
  "Resolve the parser function name for a type reference."
  [type-ref type-lookup]
  (when type-ref
    (let [canonical-ref (str/replace type-ref #"^\." "")
          type-def (get type-lookup canonical-ref)]
      (if type-def
        (str "parse-" (name (:name type-def)))
        (throw (ex-info "Cannot resolve type reference for parser"
                        {:type-ref type-ref
                         :canonical-ref canonical-ref
                         :available-types (keys type-lookup)}))))))

;; =============================================================================
;; Field Generation
;; =============================================================================

(defn generate-field-setter
  "Generate code for setting a single field."
  [field indent]
  (let [spaces (apply str (repeat indent " "))]
    (str spaces "(when (contains? m " (:name field) ")\n"
         spaces "  (." (field-java-setter field) " builder (get m " (:name field) ")))")))

(defn generate-field-getter
  "Generate code for getting a single field value."
  [field]
  (str "(." (str "has" (csk/->PascalCase (:proto-name field))) " proto) "
       "(assoc " (:name field) " (." (field-java-getter field) " proto))"))

(defn generate-regular-fields-setter
  "Generate setters for regular fields."
  [fields]
  (if (seq fields)
    (str ";; Set regular fields\n    "
         (str/join "\n    " (map #(generate-field-setter % 0) fields)))
    ""))

(defn generate-regular-fields-getter
  "Generate getters for regular fields."
  [fields]
  (if (seq fields)
    (str ";; Regular fields\n    "
         (str/join "\n    " (map generate-field-getter fields)))
    ""))

;; =============================================================================
;; Oneof Generation
;; =============================================================================

(defn generate-oneof-case
  "Generate a case clause for a oneof field."
  [field type-lookup]
  (let [field-type (get-in field [:type :message :type-ref])
        builder-name (when field-type
                      (resolve-builder-name field-type type-lookup))]
    (if (and (get-in field [:type :message])
             builder-name)
      ;; Message type - need to build it
      (str "    " (:name field) " (." 
           (field-java-setter field) " builder (" 
           builder-name " field-value))")
      ;; Scalar type - set directly
      (str "    " (:name field) " (." 
           (field-java-setter field) " builder field-value)"))))

(defn generate-oneof-parser-case
  "Generate a case clause for parsing a oneof field."
  [field type-lookup]
  (let [field-type (get-in field [:type :message :type-ref])
        parser-name (when field-type
                     (resolve-parser-name field-type type-lookup))]
    (if (and (get-in field [:type :message])
             parser-name)
      ;; Message type - need to parse it
      (str "    " (csk/->SCREAMING_SNAKE_CASE (:proto-name field))
           " {" (:name field) " (" parser-name " (." 
           (field-java-getter field) " proto))}")
      ;; Scalar type - get directly
      (str "    " (csk/->SCREAMING_SNAKE_CASE (:proto-name field))
           " {" (:name field) " (." 
           (field-java-getter field) " proto)}"))))

(defn generate-oneof-payload-setter
  "Generate the oneof payload setter code."
  [message oneof-fields]
  (if (seq oneof-fields)
    (str "\n    ;; Set oneof payload\n"
         "    (when-let [payload (first (filter (fn [[k v]] (#{" 
         (str/join " " (map :name oneof-fields))
         "} k)) m))]\n"
         "      (build-" (csk/->kebab-case (:proto-name message)) 
         "-payload builder payload))")
    ""))

(defn generate-oneof-payload-getter
  "Generate the oneof payload getter code."
  [message]
  (if (seq (:oneofs message))
    (str "\n    ;; Oneof payload\n"
         "    true (merge (parse-" (csk/->kebab-case (:proto-name message))
         "-payload proto))")
    ""))

;; =============================================================================
;; Builder and Parser Generation
;; =============================================================================

(defn generate-builder
  "Generate a builder function for a message using template."
  [message type-lookup]
  (let [regular-fields (remove :oneof-index (:fields message))
        oneof-fields (filter :oneof-index (:fields message))
        fn-name (str "build-" (name (:name message)))
        builder-template (:builder (templates))]
    (-> builder-template
        (str/replace "BUILD-FN-NAME" fn-name)
        (str/replace "PROTO-NAME" (:proto-name message))
        (str/replace "JAVA-CLASS" (:java-class message))
        (str/replace "REGULAR-FIELDS" (generate-regular-fields-setter regular-fields))
        (str/replace "ONEOF-PAYLOAD" (generate-oneof-payload-setter message oneof-fields)))))

(defn generate-oneof-builder
  "Generate the oneof payload builder for a message using template."
  [message type-lookup]
  (when (seq (:oneofs message))
    (let [oneof-fields (mapcat :fields (:oneofs message))
          case-clauses (str/join "\n" 
                               (map #(generate-oneof-case % type-lookup) 
                                   oneof-fields))
          fn-name (str "build-" (name (:name message)))
          template (:oneof-builder (templates))]
      (str "\n" (-> template
                    (str/replace "BUILD-FN-NAME-PAYLOAD" (str fn-name "-payload"))
                    (str/replace "CASE-CLAUSES" case-clauses))))))

(defn generate-parser
  "Generate a parser function for a message using template."
  [message type-lookup]
  (let [regular-fields (remove :oneof-index (:fields message))
        fn-name (str "parse-" (name (:name message)))
        parser-template (:parser (templates))]
    (-> parser-template
        (str/replace "PARSE-FN-NAME" fn-name)
        (str/replace "PROTO-NAME" (:proto-name message))
        (str/replace "JAVA-CLASS" (:java-class message))
        (str/replace "REGULAR-FIELDS" (generate-regular-fields-getter regular-fields))
        (str/replace "ONEOF-PAYLOAD" (generate-oneof-payload-getter message)))))

(defn generate-oneof-parser
  "Generate the oneof payload parser for a message using template."
  [message type-lookup]
  (when (seq (:oneofs message))
    (let [oneof (first (:oneofs message))
          oneof-fields (:fields oneof)
          case-clauses (str/join "\n"
                               (map #(generate-oneof-parser-case % type-lookup) 
                                   oneof-fields))
          fn-name (str "parse-" (name (:name message)))
          oneof-getter (str "get" (csk/->PascalCase (:proto-name oneof)) "Case")
          template (:oneof-parser (templates))]
      (str "\n" (-> template
                     (str/replace "PARSE-FN-NAME-PAYLOAD" (str fn-name "-payload"))
                     (str/replace "PROTO-NAME" (:proto-name message))
                     (str/replace "JAVA-CLASS" (:java-class message))
                     (str/replace "GET-ONEOF-CASE" oneof-getter)
                     (str/replace "CASE-CLAUSES" case-clauses))))))

;; =============================================================================
;; Enum Generation
;; =============================================================================

(defn generate-enum-value-map
  "Generate the enum value mapping."
  [enum]
  (str "{" (str/join "\n   "
                    (map (fn [value]
                          (str (:name value) " " (:java-class enum) "/" 
                               (csk/->SCREAMING_SNAKE_CASE (:proto-name value))))
                        (:values enum)))
       "}"))

(defn generate-enum-reverse-map
  "Generate the reverse enum mapping (Java -> keyword)."
  [enum]
  (str "{" (str/join "\n   "
                    (map (fn [value]
                          (str (:java-class enum) "/" 
                               (csk/->SCREAMING_SNAKE_CASE (:proto-name value))
                               " " (:name value)))
                        (:values enum)))
       "}"))

(defn generate-enum
  "Generate enum mappings."
  [enum]
  (when (:proto-name enum)
    (str ";; Enum: " (:proto-name enum) "\n"
         "(def " (csk/->kebab-case (:proto-name enum)) "-values\n"
         "  \"Keyword to Java enum mapping for " (:proto-name enum) ".\"\n"
         "  " (generate-enum-value-map enum) ")\n\n"
         "(def " (csk/->kebab-case (:proto-name enum)) "-keywords\n"
         "  \"Java enum to keyword mapping for " (:proto-name enum) ".\"\n"
         "  " (generate-enum-reverse-map enum) ")\n")))

;; =============================================================================
;; Import Generation
;; =============================================================================

(defn collect-imports
  "Collect all Java imports needed using Specter."
  [edn-data]
  (let [all-classes (sp/select [:files sp/ALL 
                               (sp/multi-path :messages :enums) 
                               sp/ALL :java-class]
                              edn-data)]
    (println "Found java classes:" (take 5 all-classes))
    (->> all-classes
         (map #(str/replace % #"\$.*" ""))  ; Remove inner class part
         (remove #(or (empty? %)
                     (str/starts-with? % "com.google.protobuf.")
                     (str/starts-with? % "build.buf.validate.")))
         distinct
         sort)))

(defn format-imports
  "Format imports for the namespace declaration."
  [imports]
  (str/join "\n" (map #(str "   " %) imports)))

;; =============================================================================
;; Main Code Generation
;; =============================================================================

(defn generate-code
  "Generate complete Clojure code from EDN data using templates."
  [{:keys [ns-name edn-data type-lookup]}]
  (let [imports (collect-imports edn-data)
        messages (sp/select [:files sp/ALL :messages sp/ALL] edn-data)
        enums (sp/select [:files sp/ALL :enums sp/ALL] edn-data)
        
        ;; Generate all enums
        enum-code (str/join "\n" (map generate-enum enums))
        
        ;; Generate all builders and parsers
        builder-parser-code (str/join "\n" 
                                    (mapcat (fn [msg]
                                             (concat
                                              [(generate-builder msg type-lookup)]
                                              (when (seq (:oneofs msg))
                                                [(generate-oneof-builder msg type-lookup)])
                                              [(generate-parser msg type-lookup)]
                                              (when (seq (:oneofs msg))
                                                [(generate-oneof-parser msg type-lookup)])))
                                           messages))
        
        ns-file (:namespace (templates))]
    
    (-> ns-file
        (str/replace "NAMESPACE-PLACEHOLDER" ns-name)
        (str/replace "IMPORTS-PLACEHOLDER" (format-imports imports))
        (str/replace "ENUMS-PLACEHOLDER" enum-code)
        (str/replace "BUILDERS-AND-PARSERS-PLACEHOLDER" builder-parser-code))))

(defn generate-from-backend
  "Generate Clojure code from backend EDN output."
  [{:keys [command state type-lookup] :as backend-output} ns-prefix]
  {:command (generate-code {:ns-name (str ns-prefix ".command")
                           :edn-data command
                           :type-lookup type-lookup})
   :state (generate-code {:ns-name (str ns-prefix ".state")
                         :edn-data state
                         :type-lookup type-lookup})})