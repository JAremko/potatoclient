(ns generator.frontend
  "Frontend using rewrite-clj for AST-based code generation.
  Uses templates as base and manipulates them via rewrite-clj."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [camel-snake-kebab.core :as csk]
            [com.rpl.specter :as sp]
            [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]
            [generator.type-resolution :as type-res]
            [generator.spec-gen :as spec-gen]
            [generator.validation-helpers :as validation]))

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
  [field current-package type-lookup]
  (let [is-message? (get-in field [:type :message])
        is-enum? (and (not (:repeated? field))
                     (get-in field [:type :enum]))
        field-name-pascal (csk/->PascalCase (:proto-name field))
        field-key (str (:name field))
        
        ;; For message types, we need to build them; for enums, convert from keywords
        value-expr (cond
                    is-message?
                    (let [type-ref (get-in field [:type :message :type-ref])
                          ;; Extract the message name from the type reference
                          message-name (when type-ref
                                        (-> type-ref
                                            (str/replace #"^\." "")
                                            (str/split #"\.")
                                            last
                                            csk/->kebab-case))]
                      (str "(build-" message-name " (get m " field-key "))"))
                    
                    is-enum?
                    (let [enum-type (get-in field [:type :enum :type-ref])
                          enum-ref (type-res/resolve-enum-reference 
                                   enum-type current-package type-lookup)
                          qualified-ref (type-res/qualified-enum-ref enum-ref)]
                      (str "(when-let [v (get m " field-key ")] (get " qualified-ref " v))"))
                    
                    :else
                    (str "(get m " field-key ")"))
        
        template (if (:repeated? field)
                   (load-template-string "field-setter-repeated.clj")
                   ;; For non-repeated fields, use inline template
                   (str "(when (contains? m " field-key ")\n"
                        "  (." (if (:repeated? field)
                               (str "addAll" field-name-pascal)
                               (str "set" field-name-pascal))
                        " builder " value-expr "))"))]
    
    (if (:repeated? field)
      (replace-in-template template 
                          {"FIELD-KEY" field-key
                           "FIELD-NAME" field-name-pascal
                           "METHOD-NAME" (str ".addAll" field-name-pascal)})
      template)))

(defn generate-field-getter
  "Generate getter code for a single field."
  [field current-package type-lookup]
  (let [;; Check if this is a message type (which might have has methods)
        is-message? (and (not (:repeated? field))
                        (get-in field [:type :message]))
        ;; Check if this is an enum type
        is-enum? (and (not (:repeated? field))
                     (get-in field [:type :enum]))
        ;; For proto3, scalar fields don't have has methods
        ;; We'll use a simple heuristic: if it's not a message type, just get the value
        use-has-method? is-message?
        
        field-name-pascal (csk/->PascalCase (:proto-name field))
        getter-expr (str ".get" field-name-pascal)
        
        ;; For messages, we need to parse them; for enums, convert to keywords
        value-expr (cond
                    is-message?
                    (let [type-ref (get-in field [:type :message :type-ref])
                          ;; Extract the message name from the type reference
                          message-name (when type-ref
                                        (-> type-ref
                                            (str/replace #"^\." "")
                                            (str/split #"\.")
                                            last
                                            csk/->kebab-case))]
                      (str "(parse-" message-name " (" getter-expr " proto))"))
                    
                    is-enum?
                    (let [enum-type (get-in field [:type :enum :type-ref])
                          enum-ref (type-res/resolve-enum-keyword-map 
                                   enum-type current-package type-lookup)
                          qualified-ref (type-res/qualified-enum-ref enum-ref)]
                      (str "(get " qualified-ref " (" getter-expr " proto))"))
                    
                    :else
                    (str "(" getter-expr " proto)"))
        
        template (if (:repeated? field)
                   (load-template-string "field-getter-repeated.clj")
                   (if use-has-method?
                     (load-template-string "field-getter.clj")
                     ;; For scalar fields, use a simpler template with true condition
                     (str "true (assoc FIELD-KEY " value-expr ")")))
        
        replacements {"FIELD-KEY" (str (:name field))
                     "FIELD-NAME" field-name-pascal
                     "METHOD-NAME" (if (:repeated? field)
                                    "true" 
                                    (str ".has" field-name-pascal))
                     "GETTER-NAME" value-expr}]
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
  [message type-lookup current-package guardrails?]
  (let [template (load-template-string (if guardrails? "builder-guardrails.clj" "builder.clj"))
        regular-fields (remove :oneof-index (:fields message))
        fn-name (str "build-" (name (:name message)))
        
        regular-field-setters (when (seq regular-fields)
                               (str ";; Set regular fields\n    "
                                    (str/join "\n    " 
                                             (map #(generate-field-setter % current-package type-lookup) 
                                                  regular-fields))))
        
        ;; Generate oneof handling for each oneof
        oneof-payloads (when (seq (:oneofs message))
                        (str/join "\n" 
                                 (map (fn [oneof]
                                       (let [oneof-field-names (map :name (:fields oneof))
                                             var-name (str (name (:name oneof)) "-field")]
                                         (str "\n    ;; Handle oneof: " (:proto-name oneof) "\n"
                                              "    (when-let [" var-name "\n"
                                              "                 (first (filter (fn [[k v]]\n"
                                              "                                  (#{"
                                              (str/join " " (map pr-str oneof-field-names))
                                              "}\n"
                                              "                                   k))\n"
                                              "                          (:" (name (:name oneof)) " m)))]\n"
                                              "      (build-" (csk/->kebab-case (:proto-name message))
                                              "-payload builder " var-name "))")))
                                      (:oneofs message))))
        
        spec-name (spec-gen/message->spec-name message)
        replacements {"BUILD-FN-NAME" fn-name
                     "PROTO-NAME" (:proto-name message)
                     "JAVA-CLASS" (:java-class message)
                     "SPEC-NAME" spec-name
                     "REGULAR-FIELDS" (or regular-field-setters "")
                     "ONEOF-PAYLOAD" (or oneof-payloads "")}]
    (replace-in-template template replacements)))

(defn generate-parser
  "Generate parser function for a message."
  [message type-lookup current-package guardrails?]
  (let [regular-fields (remove :oneof-index (:fields message))
        fn-name (str "parse-" (name (:name message)))
        spec-name (spec-gen/message->spec-name message)
        has-fields? (or (seq regular-fields) (seq (:oneofs message)))
        
        ;; For empty messages, use a simpler template
        template (if has-fields?
                   (load-template-string (if guardrails? "parser-guardrails.clj" "parser.clj"))
                   (str "(>defn " fn-name "\n"
                        "  \"Parse a " (:proto-name message) " protobuf message to a map.\"\n"
                        "  [^" (:java-class message) " proto]\n"
                        "  [any? => " spec-name "]\n"
                        "  {})"))
        
        field-getters (when (seq regular-fields)
                       (str ";; Regular fields\n    "
                            (str/join "\n    "
                                     (map #(generate-field-getter % current-package type-lookup) 
                                          regular-fields))))
        
        oneof-payload (when (seq (:oneofs message))
                       (str "\n    ;; Oneof payload\n"
                            "    true (merge (parse-" 
                            (csk/->kebab-case (:proto-name message)) 
                            "-payload proto))"))
        
        replacements {"PARSE-FN-NAME" fn-name
                     "PROTO-NAME" (:proto-name message)
                     "JAVA-CLASS" (:java-class message)
                     "SPEC-NAME" spec-name
                     "REGULAR-FIELDS" (or field-getters "")
                     "ONEOF-PAYLOAD" (or oneof-payload "")}]
    (if has-fields?
      (replace-in-template template replacements)
      template)))

;; =============================================================================
;; Oneof Generation
;; =============================================================================

(defn generate-oneof-builder-case
  "Generate a single case for oneof builder."
  [field message type-lookup current-package]
  (let [field-type (:type field)
        type-ref (or (get-in field-type [:message :type-ref])
                     (get-in field-type [:enum :type-ref]))
        is-enum? (get-in field-type [:enum])
        builder-fn (when (and type-ref (get-in field-type [:message]))
                    (let [canonical-ref (str/replace type-ref #"^\." "")
                          type-def (get type-lookup canonical-ref)
                          target-package (:package type-def)
                          ;; Check if we need namespace qualification (case-insensitive)
                          needs-ns? (and target-package 
                                        (not= (str/lower-case target-package) 
                                              (str/lower-case current-package)))
                          ;; Debug print
                          _ (println "DEBUG oneof builder:"
                                    "field" (:name field)
                                    "type-ref" type-ref
                                    "canonical-ref" canonical-ref
                                    "type-def?" (boolean type-def)
                                    "target-package" target-package 
                                    "current-package" current-package
                                    "needs-ns?" needs-ns?)]
                      (when (and type-def (= :message (:type type-def)))
                        (if needs-ns?
                          ;; Qualify with namespace alias
                          (let [ns-alias (-> (last (str/split target-package #"\."))
                                             str/lower-case
                                             (str/replace "_" "-"))]
                            (str ns-alias "/build-" (name (:name type-def))))
                          ;; Same namespace, no qualification needed
                          (str "build-" (name (:name type-def)))))))
        value-expr (cond
                    builder-fn (str "(" builder-fn " value)")
                    is-enum? (let [enum-ref (type-res/resolve-enum-reference 
                                           type-ref current-package type-lookup)
                                  qualified-ref (type-res/qualified-enum-ref enum-ref)]
                              (str "(get " qualified-ref " value)"))
                    :else "value")]
    (str (pr-str (:name field)) " "
         "(." (str "set" (csk/->PascalCase (:proto-name field))) " builder "
         value-expr ")")))

(defn generate-oneof-parser-case
  "Generate a single case for oneof parser."
  [field message type-lookup current-package]
  (let [field-type (:type field)
        type-ref (or (get-in field-type [:message :type-ref])
                     (get-in field-type [:enum :type-ref]))
        is-enum? (get-in field-type [:enum])
        parser-fn (when (and type-ref (get-in field-type [:message]))
                   (let [canonical-ref (str/replace type-ref #"^\." "")
                         type-def (get type-lookup canonical-ref)
                         target-package (:package type-def)
                         ;; Check if we need namespace qualification (case-insensitive)
                         needs-ns? (and target-package 
                                       (not= (str/lower-case target-package)
                                             (str/lower-case current-package)))]
                     (when (and type-def (= :message (:type type-def)))
                       (if needs-ns?
                         ;; Qualify with namespace alias
                         (let [ns-alias (-> (last (str/split target-package #"\."))
                                            str/lower-case
                                            (str/replace "_" "-"))]
                           (str ns-alias "/parse-" (name (:name type-def))))
                         ;; Same namespace, no qualification needed
                         (str "parse-" (name (:name type-def)))))))
        getter-expr (str "(." (str "get" (csk/->PascalCase (:proto-name field))) " proto)")
        value-expr (cond
                    parser-fn (str "(" parser-fn " " getter-expr ")")
                    is-enum? (let [enum-ref (type-res/resolve-enum-keyword-map 
                                           type-ref current-package type-lookup)
                                  qualified-ref (type-res/qualified-enum-ref enum-ref)]
                              (str "(get " qualified-ref " " getter-expr ")"))
                    :else getter-expr)]
    (str "(." (str "has" (csk/->PascalCase (:proto-name field))) " proto) "
         "{" (pr-str (:name field)) " "
         value-expr
         "}")))

(defn generate-oneof-builder
  "Generate oneof builder function."
  [message oneof type-lookup current-package guardrails?]
  (let [template (load-template-string (if guardrails? "oneof-builder-guardrails.clj" "oneof-builder.clj"))
        fn-name (str "build-" (csk/->kebab-case (:proto-name message)) "-payload")
        ;; Use fields from the oneof structure itself
        oneof-fields (:fields oneof)
        
        cases (str/join "\n    " 
                       (map #(generate-oneof-builder-case % message type-lookup current-package) 
                            oneof-fields))
        
        replacements {"ONEOF-BUILD-FN-NAME" fn-name
                     "PROTO-NAME" (:proto-name message)
                     "ONEOF-NAME" (pr-str (:name oneof))
                     "ONEOF-CASES" cases}]
    (replace-in-template template replacements)))

(defn generate-oneof-parser
  "Generate oneof parser function."
  [message oneof type-lookup current-package guardrails?]
  (let [template (load-template-string (if guardrails? "oneof-parser-guardrails.clj" "oneof-parser.clj"))
        fn-name (str "parse-" (csk/->kebab-case (:proto-name message)) "-payload")
        ;; Use fields from the oneof structure itself
        oneof-fields (:fields oneof)
        
        cases (str/join "\n    " 
                       (map #(generate-oneof-parser-case % message type-lookup current-package) 
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
  ([ns-name imports enums messages type-lookup]
   (generate-namespace ns-name imports enums messages type-lookup [] false true nil))
  ([ns-name imports enums messages type-lookup require-specs]
   (generate-namespace ns-name imports enums messages type-lookup require-specs false true nil))
  ([ns-name imports enums messages type-lookup require-specs guardrails?]
   (generate-namespace ns-name imports enums messages type-lookup require-specs guardrails? true nil))
  ([ns-name imports enums messages type-lookup require-specs guardrails? generate-specs?]
   (generate-namespace ns-name imports enums messages type-lookup require-specs guardrails? generate-specs? nil))
  ([ns-name imports enums messages type-lookup require-specs guardrails? generate-specs? proto-package]
   (let [template (cond
                    ;; Specs + guardrails + requires
                    (and generate-specs? guardrails? (seq require-specs))
                    (load-template-string "namespace-with-specs-guardrails.clj")
                    
                    ;; Specs + guardrails (no requires) 
                    (and generate-specs? guardrails?)
                    (load-template-string "namespace-with-specs-guardrails.clj")
                    
                    ;; Specs + requires (no guardrails)
                    (and generate-specs? (seq require-specs))
                    (load-template-string "namespace-with-specs.clj")
                    
                    ;; Specs only
                    generate-specs?
                    (load-template-string "namespace-with-specs.clj")
                    
                    ;; Original templates without specs
                    (and guardrails? (seq require-specs))
                    (load-template-string "namespace-with-requires-guardrails.clj")
                    
                    guardrails?
                    (load-template-string "namespace-guardrails.clj")
                    
                    :else
                    (load-template-string "namespace-guardrails.clj"))
         
         ;; Use provided proto-package or extract from namespace name
         current-package (or proto-package
                            (when (str/includes? ns-name ".")
                              (let [parts (str/split ns-name #"\.")
                                    ;; Skip the namespace prefix parts
                                    package-parts (drop-while #(not (#{"cmd" "ser"} %)) parts)]
                                (when (seq package-parts)
                                  ;; Convert last part to PascalCase for proto package
                                  (if (= 1 (count package-parts))
                                    (first package-parts)
                                    (str (first package-parts) "." 
                                         (csk/->PascalCase (last package-parts))))))))
         
         enums-code (when (seq enums)
                      (str/join "\n\n" (map generate-enum-def enums)))
         
         ;; Sort messages so nested messages come before their parents
         sorted-messages (sort-by 
                         (fn [msg]
                           ;; Count the depth (number of $ in java-class)
                           (count (filter #(= \$ %) (:java-class msg))))
                         #(compare %2 %1) ;; Reverse sort - deepest first
                         messages)
         
         ;; Generate forward declarations for all builder and parser functions
         forward-decls (when (seq sorted-messages)
                       (str ";; Forward declarations\n"
                            (str/join "\n" 
                                     (concat
                                      (for [msg sorted-messages]
                                        (str "(declare build-" (name (:name msg)) ")"))
                                      (for [msg sorted-messages]
                                        (str "(declare parse-" (name (:name msg)) ")"))
                                      (for [msg sorted-messages
                                            :when (seq (:oneofs msg))]
                                        (str "(declare build-" (str/replace (str/lower-case (:proto-name msg)) #"_" "-") "-payload)"))
                                      (for [msg sorted-messages
                                            :when (seq (:oneofs msg))]
                                        (str "(declare parse-" (str/replace (str/lower-case (:proto-name msg)) #"_" "-") "-payload)"))))
                            "\n"))
         
         ;; Generate all message builders/parsers first, then all oneofs
         ;; This avoids forward reference issues
         all-builders (for [msg sorted-messages]
                       (generate-builder msg type-lookup current-package guardrails?))
         all-parsers (for [msg sorted-messages]
                      (generate-parser msg type-lookup current-package guardrails?))
         all-oneof-builders (for [msg sorted-messages
                                 oneof (:oneofs msg)]
                             (generate-oneof-builder msg oneof type-lookup current-package guardrails?))
         all-oneof-parsers (for [msg sorted-messages
                                oneof (:oneofs msg)]
                            (generate-oneof-parser msg oneof type-lookup current-package guardrails?))
         
         messages-code (when (seq sorted-messages)
                       (str/join "\n\n" 
                                (concat all-builders 
                                        all-parsers
                                        all-oneof-builders
                                        all-oneof-parsers)))
         
         ;; Generate require clause if needed
         require-clause (when (seq require-specs)
                        (str/join "\n            " 
                                 (map pr-str require-specs)))
         
         ;; Generate specs if requested
         specs-code (when generate-specs?
                     (let [{:keys [enum-specs message-specs]} 
                           (spec-gen/generate-specs-for-namespace 
                            {:messages messages :enums enums})]
                       (str (when (seq enum-specs)
                             (str enum-specs "\n\n"))
                            (when (seq message-specs)
                             message-specs))))
         
         ;; Generate validation helpers if we have specs and guardrails
         validation-code (when (and generate-specs? guardrails?)
                          (validation/generate-namespace-validation-helpers messages))
         
         replacements {"NAMESPACE-PLACEHOLDER" ns-name
                     "REQUIRE-PLACEHOLDER" (or require-clause "")
                     "IMPORTS-PLACEHOLDER" (generate-imports imports)
                     "ENUMS-PLACEHOLDER" (or enums-code ";; No enums")
                     "SPECS-PLACEHOLDER" (or specs-code ";; No specs")
                     "BUILDERS-AND-PARSERS-PLACEHOLDER" (str forward-decls "\n" (or messages-code ";; No messages") validation-code)}]
    (replace-in-template template replacements))))

;; =============================================================================
;; Main Code Generation
;; =============================================================================

(defn collect-imports
  "Collect all Java imports needed."
  [edn-data]
  (let [;; Helper to collect all messages recursively
        collect-all-messages (fn collect [msg]
                              (cons msg 
                                    (when (:nested-types msg)
                                      (mapcat collect (:nested-types msg)))))
        ;; Helper to collect all enums from messages recursively
        collect-enums-from-message (fn collect-enums [msg]
                                     (concat (:enums msg [])
                                            (when (:nested-types msg)
                                              (mapcat collect-enums (:nested-types msg)))))
        ;; Collect all messages
        all-messages (->> edn-data
                         :files
                         (mapcat :messages)
                         (mapcat collect-all-messages))
        ;; Collect all Java classes from messages and their enums
        message-classes (map :java-class all-messages)
        top-level-enum-classes (sp/select [:files sp/ALL :enums sp/ALL :java-class] edn-data)
        nested-enum-classes (->> all-messages
                                (mapcat collect-enums-from-message)
                                (map :java-class))
        all-classes (concat message-classes top-level-enum-classes nested-enum-classes)
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
  [{:keys [ns-name edn-data type-lookup guardrails? proto-package]}]
  (let [imports (collect-imports edn-data)
        ;; Helper to collect all messages recursively
        collect-all-messages (fn collect [msg]
                              (cons msg 
                                    (when (:nested-types msg)
                                      (mapcat collect (:nested-types msg)))))
        ;; Collect all messages including nested ones
        all-messages (->> edn-data
                         :files
                         (mapcat :messages)
                         (mapcat collect-all-messages))
        _ (println "Found" (count all-messages) "total messages")
        ;; Helper to collect all enums from messages recursively
        collect-enums-from-message (fn collect-enums [msg]
                                     (concat (:enums msg [])
                                            (when (:nested-types msg)
                                              (mapcat collect-enums (:nested-types msg)))))
        ;; Collect all enums (top-level and nested in messages)
        all-enums (concat 
                   (sp/select [:files sp/ALL :enums sp/ALL] edn-data)
                   (->> all-messages
                        (mapcat collect-enums-from-message)))]
    (generate-namespace ns-name imports all-enums all-messages type-lookup [] guardrails? true proto-package)))

(defn generate-from-backend
  "Generate Clojure code from backend EDN output."
  [{:keys [command state type-lookup] :as backend-output} ns-prefix guardrails?]
  {:command (generate-code {:ns-name (str ns-prefix ".command")
                           :edn-data command
                           :type-lookup type-lookup
                           :guardrails? guardrails?
                           :proto-package "cmd"})
   :state (generate-code {:ns-name (str ns-prefix ".state")
                         :edn-data state
                         :type-lookup type-lookup
                         :guardrails? guardrails?
                         :proto-package "ser"})})