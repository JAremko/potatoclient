(ns generator.frontend
  "Frontend using rewrite-clj for AST-based code generation.
  Generates Clojure code from the EDN intermediate representation."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [camel-snake-kebab.core :as csk]
            [com.rpl.specter :as sp]
            [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]))

;; =============================================================================
;; Template Loading and Parsing
;; =============================================================================

(defn load-template
  "Load a template file from resources as a string."
  [template-name]
  (if-let [resource (io/resource (str "templates/" template-name))]
    (slurp resource)
    (throw (ex-info "Template not found" {:template template-name}))))

;; =============================================================================
;; AST Node Creation Helpers
;; =============================================================================

(defn create-when-contains-node
  "Create (when (contains? m :key) ...) node."
  [map-sym key-kw body-node]
  (n/list-node
   [(n/token-node 'when)
    (n/whitespace-node " ")
    (n/list-node
     [(n/token-node 'contains?)
      (n/whitespace-node " ")
      (n/token-node map-sym)
      (n/whitespace-node " ")
      (n/keyword-node key-kw)])
    (n/newline-node "\n")
    (n/whitespace-node "      ")
    body-node]))

(defn create-method-call-node
  "Create (.methodName obj args...) node."
  [method-name obj & args]
  (n/list-node
   (concat
    [(n/token-node (symbol method-name))
     (n/whitespace-node " ")
     (n/token-node obj)]
    (when (seq args)
      (mapcat (fn [arg]
               [(n/whitespace-node " ")
                arg])
             args)))))

(defn create-get-node
  "Create (get m :key) node."
  [map-sym key-kw]
  (n/list-node
   [(n/token-node 'get)
    (n/whitespace-node " ")
    (n/token-node map-sym)
    (n/whitespace-node " ")
    (n/keyword-node key-kw)]))

(defn create-assoc-node
  "Create (assoc :key value) node."
  [key-kw value-node]
  (n/list-node
   [(n/token-node 'assoc)
    (n/whitespace-node " ")
    (n/keyword-node key-kw)
    (n/whitespace-node " ")
    value-node]))

;; =============================================================================
;; Code Generation Helpers
;; =============================================================================

(defn field-java-setter
  "Generate Java setter method name for a field."
  [field]
  (str ".set" (csk/->PascalCase (:proto-name field))))

(defn field-java-getter
  "Generate Java getter method name for a field."
  [field]
  (str ".get" (csk/->PascalCase (:proto-name field))))

(defn field-java-has
  "Generate Java has method name for a field."
  [field]
  (str ".has" (csk/->PascalCase (:proto-name field))))

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

(defn generate-field-setter-node
  "Generate AST node for setting a single field."
  [field]
  (create-when-contains-node
   'm
   (:name field)
   (create-method-call-node
    (field-java-setter field)
    'builder
    (create-get-node 'm (:name field)))))

(defn generate-field-getter-node
  "Generate AST node for getting a single field value."
  [field]
  (n/list-node
   [(create-method-call-node (field-java-has field) 'proto)
    (n/whitespace-node " ")
    (create-assoc-node
     (:name field)
     (create-method-call-node (field-java-getter field) 'proto))]))

(defn generate-regular-fields-setter-code
  "Generate setter code for regular fields."
  [fields]
  (if (seq fields)
    (str ";; Set regular fields\n    "
         (str/join "\n    " 
                  (map #(n/string (generate-field-setter-node %)) fields)))
    ""))

(defn generate-regular-fields-getter-code
  "Generate getter code for regular fields."
  [fields]
  (if (seq fields)
    (str ";; Regular fields\n    "
         (str/join "\n    " 
                  (map #(n/string (generate-field-getter-node %)) fields)))
    ""))

;; =============================================================================
;; Oneof Generation
;; =============================================================================

(defn generate-oneof-case-node
  "Generate a case clause node for a oneof field."
  [field type-lookup]
  (let [field-type (get-in field [:type :message :type-ref])
        builder-name (when field-type
                      (resolve-builder-name field-type type-lookup))]
    (str "    " (:name field) " "
         (n/string
          (create-method-call-node
           (field-java-setter field)
           'builder
           (if (and (get-in field [:type :message])
                    builder-name)
             ;; Message type - need to build it
             (n/list-node
              [(n/token-node (symbol builder-name))
               (n/whitespace-node " ")
               (n/token-node 'field-value)])
             ;; Scalar type - set directly
             (n/token-node 'field-value)))))))

(defn generate-oneof-parser-case-node
  "Generate a case clause node for parsing a oneof field."
  [field type-lookup]
  (let [field-type (get-in field [:type :message :type-ref])
        parser-name (when field-type
                     (resolve-parser-name field-type type-lookup))]
    (str "    " (csk/->SCREAMING_SNAKE_CASE (:proto-name field))
         " {" (:name field) " "
         (if (and (get-in field [:type :message])
                  parser-name)
           ;; Message type - need to parse it
           (str "(" parser-name " " 
                (n/string (create-method-call-node (field-java-getter field) 'proto))
                ")")
           ;; Scalar type - get directly
           (n/string (create-method-call-node (field-java-getter field) 'proto)))
         "}")))

(defn generate-oneof-payload-setter-code
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

(defn generate-oneof-payload-getter-code
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
  "Generate a builder function for a message."
  [message type-lookup]
  (let [regular-fields (remove :oneof-index (:fields message))
        oneof-fields (filter :oneof-index (:fields message))
        fn-name (str "build-" (name (:name message)))
        template (load-template "builder.clj")]
    (-> template
        (str/replace "BUILD-FN-NAME" fn-name)
        (str/replace "PROTO-NAME" (:proto-name message))
        (str/replace "JAVA-CLASS" (:java-class message))
        (str/replace "REGULAR-FIELDS" (generate-regular-fields-setter-code regular-fields))
        (str/replace "ONEOF-PAYLOAD" (generate-oneof-payload-setter-code message oneof-fields)))))

(defn generate-oneof-builder
  "Generate the oneof payload builder for a message."
  [message type-lookup]
  (when (seq (:oneofs message))
    (let [oneof-fields (mapcat :fields (:oneofs message))
          case-clauses (str/join "\n" 
                               (map #(generate-oneof-case-node % type-lookup) 
                                   oneof-fields))
          fn-name (str "build-" (name (:name message)))
          template (load-template "oneof-builder.clj")]
      (str "\n" (-> template
                    (str/replace "BUILD-FN-NAME-PAYLOAD" (str fn-name "-payload"))
                    (str/replace "CASE-CLAUSES" case-clauses))))))

(defn generate-parser
  "Generate a parser function for a message."
  [message type-lookup]
  (let [regular-fields (remove :oneof-index (:fields message))
        fn-name (str "parse-" (name (:name message)))
        template (load-template "parser.clj")]
    (-> template
        (str/replace "PARSE-FN-NAME" fn-name)
        (str/replace "PROTO-NAME" (:proto-name message))
        (str/replace "JAVA-CLASS" (:java-class message))
        (str/replace "REGULAR-FIELDS" (generate-regular-fields-getter-code regular-fields))
        (str/replace "ONEOF-PAYLOAD" (generate-oneof-payload-getter-code message)))))

(defn generate-oneof-parser
  "Generate the oneof payload parser for a message."
  [message type-lookup]
  (when (seq (:oneofs message))
    (let [oneof (first (:oneofs message))
          oneof-fields (:fields oneof)
          case-clauses (str/join "\n"
                               (map #(generate-oneof-parser-case-node % type-lookup) 
                                   oneof-fields))
          fn-name (str "parse-" (name (:name message)))
          oneof-getter (str ".get" (csk/->PascalCase (:proto-name oneof)) "Case")
          template (load-template "oneof-parser.clj")]
      (str "\n" (-> template
                     (str/replace "PARSE-FN-NAME-PAYLOAD" (str fn-name "-payload"))
                     (str/replace "PROTO-NAME" (:proto-name message))
                     (str/replace "JAVA-CLASS" (:java-class message))
                     (str/replace "GET-ONEOF-CASE" oneof-getter)
                     (str/replace "CASE-CLAUSES" case-clauses))))))

;; =============================================================================
;; Enum Generation
;; =============================================================================

(defn generate-enum-value-entry
  "Generate a single enum value entry."
  [value enum]
  (str (:name value) " " (:java-class enum) "/" 
       (csk/->SCREAMING_SNAKE_CASE (:proto-name value))))

(defn generate-enum-reverse-entry
  "Generate a single reverse enum entry."
  [value enum]
  (str (:java-class enum) "/" 
       (csk/->SCREAMING_SNAKE_CASE (:proto-name value))
       " " (:name value)))

(defn generate-enum
  "Generate enum mappings."
  [enum]
  (when (:proto-name enum)
    (let [values-entries (map #(generate-enum-value-entry % enum) (:values enum))
          reverse-entries (map #(generate-enum-reverse-entry % enum) (:values enum))]
      (str ";; Enum: " (:proto-name enum) "\n"
           "(def " (csk/->kebab-case (:proto-name enum)) "-values\n"
           "  \"Keyword to Java enum mapping for " (:proto-name enum) ".\"\n"
           "  {" (str/join "\n   " values-entries) "})\n\n"
           "(def " (csk/->kebab-case (:proto-name enum)) "-keywords\n"
           "  \"Java enum to keyword mapping for " (:proto-name enum) ".\"\n"
           "  {" (str/join "\n   " reverse-entries) "})\n"))))

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
  "Generate complete Clojure code from EDN data."
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
        
        template (load-template "namespace.clj")]
    
    (-> template
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