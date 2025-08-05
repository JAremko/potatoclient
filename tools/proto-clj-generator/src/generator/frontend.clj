(ns generator.frontend
  "Frontend using rewrite-clj for AST-based code generation.
  Uses templates as base and manipulates them via rewrite-clj."
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
  "Load a template file from resources as a zipper."
  [template-name]
  (if-let [resource (io/resource (str "templates/" template-name))]
    (z/of-string (slurp resource))
    (throw (ex-info "Template not found" {:template template-name}))))

(defn load-template-string
  "Load a template file from resources as a string."
  [template-name]
  (if-let [resource (io/resource (str "templates/" template-name))]
    (slurp resource)
    (throw (ex-info "Template not found" {:template template-name}))))

;; =============================================================================
;; Template Processing
;; =============================================================================

(defn replace-in-template
  "Replace placeholders in a template string."
  [template replacements]
  (reduce (fn [t [k v]]
            (str/replace t (str k) (str v)))
          template
          replacements))

;; =============================================================================
;; Field Generation
;; =============================================================================

(defn generate-field-setter
  "Generate setter code for a single field."
  [field]
  (let [template (if (:repeated? field)
                   (load-template-string "field-setter-repeated.clj")
                   (load-template-string "field-setter.clj"))
        field-name-pascal (csk/->PascalCase (:proto-name field))
        replacements {"FIELD-KEY" (str (:name field))
                     "FIELD-NAME" field-name-pascal
                     "METHOD-NAME" (if (:repeated? field)
                                    (str ".addAll" field-name-pascal)
                                    (str ".set" field-name-pascal))}]
    (replace-in-template template replacements)))

(defn generate-field-getter
  "Generate getter code for a single field."
  [field]
  (let [template (if (:repeated? field)
                   (load-template-string "field-getter-repeated.clj")
                   (load-template-string "field-getter.clj"))
        field-name-pascal (csk/->PascalCase (:proto-name field))
        replacements {"FIELD-KEY" (str (:name field))
                     "FIELD-NAME" field-name-pascal
                     "METHOD-NAME" (if (:repeated? field)
                                    "true" 
                                    (str ".has" field-name-pascal))
                     "GETTER-NAME" (str ".get" field-name-pascal)}]
    (replace-in-template template replacements)))

;; =============================================================================
;; Enum Generation
;; =============================================================================

(defn generate-enum-value-entry
  "Generate a single enum value entry."
  [value enum]
  (str (pr-str (:name value)) " " 
       (:java-class enum) "/" 
       (:proto-name value)))

(defn generate-enum-reverse-entry
  "Generate a single reverse enum entry."
  [value enum]
  (str (:java-class enum) "/" 
       (:proto-name value)
       " " (pr-str (:name value))))

(defn generate-enum-def
  "Generate enum definition."
  [enum]
  (let [values-name (str (csk/->kebab-case (:proto-name enum)) "-values")
        keywords-name (str (csk/->kebab-case (:proto-name enum)) "-keywords")
        value-entries (str/join "\n   " (map #(generate-enum-value-entry % enum) (:values enum)))
        keyword-entries (str/join "\n   " (map #(generate-enum-reverse-entry % enum) (:values enum)))]
    (str ";; Enum: " (:proto-name enum) "\n"
         "(def " values-name "\n"
         "  \"Keyword to Java enum mapping for " (:proto-name enum) ".\"\n"
         "  {" value-entries "})\n\n"
         "(def " keywords-name "\n"
         "  \"Java enum to keyword mapping for " (:proto-name enum) ".\"\n"
         "  {" keyword-entries "})")))

;; =============================================================================
;; Message Generation
;; =============================================================================

(defn generate-builder
  "Generate builder function for a message."
  [message type-lookup]
  (let [template (load-template-string "builder.clj")
        regular-fields (remove :oneof-index (:fields message))
        fn-name (str "build-" (name (:name message)))
        
        regular-field-setters (when (seq regular-fields)
                               (str ";; Set regular fields\n    "
                                    (str/join "\n    " 
                                             (map generate-field-setter regular-fields))))
        
        ;; Generate oneof handling for each oneof
        oneof-payloads (when (seq (:oneofs message))
                        (str/join "\n" 
                                 (map (fn [oneof]
                                       (let [oneof-field-names (map :name (:fields oneof))
                                             var-name (str (name (:name oneof)) "-field")]
                                         (str "\n    ;; Handle oneof: " (:proto-name oneof) "\n"
                                              "    (when-let [" var-name " (first (filter (fn [[k v]] (#{"
                                              (str/join " " (map pr-str oneof-field-names))
                                              "} k)) m))]\n"
                                              "      (build-" (csk/->kebab-case (:proto-name message))
                                              "-payload builder " var-name "))")))
                                      (:oneofs message))))
        
        replacements {"BUILD-FN-NAME" fn-name
                     "PROTO-NAME" (:proto-name message)
                     "JAVA-CLASS" (:java-class message)
                     "REGULAR-FIELDS" (or regular-field-setters "")
                     "ONEOF-PAYLOAD" (or oneof-payloads "")}]
    (replace-in-template template replacements)))

(defn generate-parser
  "Generate parser function for a message."
  [message type-lookup]
  (let [template (load-template-string "parser.clj")
        regular-fields (remove :oneof-index (:fields message))
        fn-name (str "parse-" (name (:name message)))
        
        field-getters (when (seq regular-fields)
                       (str ";; Regular fields\n    "
                            (str/join "\n    "
                                     (map generate-field-getter regular-fields))))
        
        oneof-payload (when (seq (:oneofs message))
                       (str "\n    ;; Oneof payload\n"
                            "    true (merge (parse-" 
                            (csk/->kebab-case (:proto-name message)) 
                            "-payload proto))"))
        
        replacements {"PARSE-FN-NAME" fn-name
                     "PROTO-NAME" (:proto-name message)
                     "JAVA-CLASS" (:java-class message)
                     "REGULAR-FIELDS" (or field-getters "")
                     "ONEOF-PAYLOAD" (or oneof-payload "")}]
    (replace-in-template template replacements)))

;; =============================================================================
;; Oneof Generation
;; =============================================================================

(defn generate-oneof-builder-case
  "Generate a single case for oneof builder."
  [field message type-lookup]
  (let [field-type (:type field)
        type-ref (or (get-in field-type [:message :type-ref])
                     (get-in field-type [:enum :type-ref]))
        builder-fn (when (and type-ref (get-in field-type [:message]))
                    (let [canonical-ref (str/replace type-ref #"^\." "")
                          type-def (get type-lookup canonical-ref)]
                      (when (and type-def (= :message (:type type-def)))
                        (str "build-" (name (:name type-def))))))]
    (str (pr-str (:name field)) " "
         "(." (str "set" (csk/->PascalCase (:proto-name field))) " builder "
         (if builder-fn
           (str "(" builder-fn " value)")
           "value") ")")))

(defn generate-oneof-parser-case
  "Generate a single case for oneof parser."
  [field message type-lookup]
  (let [field-type (:type field)
        type-ref (or (get-in field-type [:message :type-ref])
                     (get-in field-type [:enum :type-ref]))
        parser-fn (when (and type-ref (get-in field-type [:message]))
                   (let [canonical-ref (str/replace type-ref #"^\." "")
                         type-def (get type-lookup canonical-ref)]
                     (when (and type-def (= :message (:type type-def)))
                       (str "parse-" (name (:name type-def))))))]
    (str "(." (str "has" (csk/->PascalCase (:proto-name field))) " proto) "
         "{" (pr-str (:name field)) " "
         (if parser-fn
           (str "(" parser-fn " (." (str "get" (csk/->PascalCase (:proto-name field))) " proto))")
           (str "(." (str "get" (csk/->PascalCase (:proto-name field))) " proto)"))
         "}")))

(defn generate-oneof-builder
  "Generate oneof builder function."
  [message oneof type-lookup]
  (let [template (load-template-string "oneof-builder.clj")
        fn-name (str "build-" (csk/->kebab-case (:proto-name message)) "-payload")
        ;; Use fields from the oneof structure itself
        oneof-fields (:fields oneof)
        
        cases (str/join "\n    " 
                       (map #(generate-oneof-builder-case % message type-lookup) 
                            oneof-fields))
        
        replacements {"ONEOF-BUILD-FN-NAME" fn-name
                     "PROTO-NAME" (:proto-name message)
                     "ONEOF-NAME" (pr-str (:name oneof))
                     "ONEOF-CASES" cases}]
    (replace-in-template template replacements)))

(defn generate-oneof-parser
  "Generate oneof parser function."
  [message oneof type-lookup]
  (let [template (load-template-string "oneof-parser.clj")
        fn-name (str "parse-" (csk/->kebab-case (:proto-name message)) "-payload")
        ;; Use fields from the oneof structure itself
        oneof-fields (:fields oneof)
        
        cases (str/join "\n    " 
                       (map #(generate-oneof-parser-case % message type-lookup) 
                            oneof-fields))
        
        replacements {"ONEOF-PARSE-FN-NAME" fn-name
                     "PROTO-NAME" (:proto-name message)
                     "JAVA-CLASS" (:java-class message)
                     "ONEOF-CASES" cases}]
    (replace-in-template template replacements)))

;; =============================================================================
;; Namespace Generation
;; =============================================================================

(defn generate-imports
  "Generate import statement."
  [imports]
  (if (seq imports)
    ;; Each import needs to be on its own line without brackets
    (str/join "\n   " imports)
    ""))

(defn generate-namespace
  "Generate complete namespace."
  [ns-name imports enums messages type-lookup]
  (let [template (load-template-string "namespace.clj")
        
        enums-code (when (seq enums)
                    (str/join "\n\n" (map generate-enum-def enums)))
        
        messages-code (when (seq messages)
                       (str/join "\n\n" 
                                (mapcat (fn [msg]
                                         (concat 
                                          ;; Generate oneofs first (they're called by builders/parsers)
                                          (for [oneof (:oneofs msg)]
                                            (generate-oneof-builder msg oneof type-lookup))
                                          (for [oneof (:oneofs msg)]
                                            (generate-oneof-parser msg oneof type-lookup))
                                          ;; Then generate the message builder and parser
                                          [(generate-builder msg type-lookup)
                                           (generate-parser msg type-lookup)]))
                                       messages)))
        
        replacements {"NAMESPACE-PLACEHOLDER" ns-name
                     "IMPORTS-PLACEHOLDER" (generate-imports imports)
                     "ENUMS-PLACEHOLDER" (or enums-code ";; No enums")
                     "BUILDERS-AND-PARSERS-PLACEHOLDER" (or messages-code ";; No messages")}]
    (replace-in-template template replacements)))

;; =============================================================================
;; Main Code Generation
;; =============================================================================

(defn collect-imports
  "Collect all Java imports needed."
  [edn-data]
  (let [all-classes (sp/select [:files sp/ALL 
                               (sp/multi-path :messages :enums) 
                               sp/ALL :java-class]
                              edn-data)
        ;; Extract just the outer class for imports
        outer-classes (->> all-classes
                          (map (fn [class-name]
                                (if (str/includes? class-name "$")
                                  ;; Get the outer class before the $
                                  (subs class-name 0 (str/index-of class-name "$"))
                                  class-name)))
                          ;; Remove special imports that we'll add manually
                          (remove #(or (empty? %)
                                      (str/starts-with? % "google.protobuf.")
                                      (str/starts-with? % "buf.validate.")))
                          distinct
                          sort)]
    ;; Add special imports that are always needed
    (->> (concat outer-classes)
         distinct
         vec)))

(defn generate-code
  "Generate complete Clojure code from EDN data."
  [{:keys [ns-name edn-data type-lookup]}]
  (let [imports (collect-imports edn-data)
        messages (sp/select [:files sp/ALL :messages sp/ALL] edn-data)
        enums (sp/select [:files sp/ALL :enums sp/ALL] edn-data)]
    (generate-namespace ns-name imports enums messages type-lookup)))

(defn generate-from-backend
  "Generate Clojure code from backend EDN output."
  [{:keys [command state type-lookup] :as backend-output} ns-prefix]
  {:command (generate-code {:ns-name (str ns-prefix ".command")
                           :edn-data command
                           :type-lookup type-lookup})
   :state (generate-code {:ns-name (str ns-prefix ".state")
                         :edn-data state
                         :type-lookup type-lookup})})