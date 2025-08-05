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
       (csk/->SCREAMING_SNAKE_CASE (:proto-name value))))

(defn generate-enum-reverse-entry
  "Generate a single reverse enum entry."
  [value enum]
  (str (:java-class enum) "/" 
       (csk/->SCREAMING_SNAKE_CASE (:proto-name value))
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
        oneof-fields (filter :oneof-index (:fields message))
        fn-name (str "build-" (name (:name message)))
        
        regular-field-setters (when (seq regular-fields)
                               (str ";; Set regular fields\n    "
                                    (str/join "\n    " 
                                             (map generate-field-setter regular-fields))))
        
        oneof-payload (when (seq oneof-fields)
                       (str "\n    ;; Set oneof payload\n"
                            "    (when-let [payload (first (filter (fn [[k v]] (#{" 
                            (str/join " " (map #(pr-str (:name %)) oneof-fields))
                            "} k)) m))]\n"
                            "      (build-" (csk/->kebab-case (:proto-name message)) 
                            "-payload builder payload))"))
        
        replacements {"BUILD-FN-NAME" fn-name
                     "PROTO-NAME" (:proto-name message)
                     "JAVA-CLASS" (:java-class message)
                     "REGULAR-FIELDS" (or regular-field-setters "")
                     "ONEOF-PAYLOAD" (or oneof-payload "")}]
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
;; Namespace Generation
;; =============================================================================

(defn generate-imports
  "Generate import statement."
  [imports]
  (if (seq imports)
    (str "[" (str/join "\n   " imports) "]")
    "[]"))

(defn generate-namespace
  "Generate complete namespace."
  [ns-name imports enums messages type-lookup]
  (let [template (load-template-string "namespace.clj")
        
        enums-code (when (seq enums)
                    (str/join "\n\n" (map generate-enum-def enums)))
        
        messages-code (when (seq messages)
                       (str/join "\n\n" 
                                (mapcat (fn [msg]
                                         (filter some?
                                                [(generate-builder msg type-lookup)
                                                 (when (seq (:oneofs msg))
                                                   ;; TODO: Generate oneof builder
                                                   nil)
                                                 (generate-parser msg type-lookup)
                                                 (when (seq (:oneofs msg))
                                                   ;; TODO: Generate oneof parser
                                                   nil)]))
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
                              edn-data)]
    (->> all-classes
         (map #(str/replace % #"\$.*" ""))  ; Remove inner class part
         (remove #(or (empty? %)
                     (str/starts-with? % "com.google.protobuf.")
                     (str/starts-with? % "build.buf.validate.")))
         distinct
         sort)))

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