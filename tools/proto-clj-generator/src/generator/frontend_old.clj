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

;; =============================================================================
;; AST Navigation and Manipulation
;; =============================================================================

(defn find-and-replace
  "Find a symbol and replace it with a new value, preserving location context."
  [zloc sym-name replacement]
  (loop [loc zloc]
    (cond
      (z/end? loc) zloc
      
      (and (= :token (z/tag loc))
           (symbol? (z/sexpr loc))
           (= (str (z/sexpr loc)) (str sym-name)))
      (-> loc
          (z/replace replacement)
          z/up
          (or zloc))
      
      :else (recur (z/next loc)))))

(defn find-and-replace-string
  "Find a string node and replace its content."
  [zloc string-content replacement]
  (loop [loc zloc]
    (cond
      (z/end? loc) zloc
      
      (and (string? (z/sexpr loc))
           (str/includes? (z/sexpr loc) string-content))
      (let [current-string (z/sexpr loc)
            new-string (str/replace current-string string-content replacement)]
        (-> loc
            (z/replace (n/string-node new-string))
            z/up
            (or zloc)))
      
      :else (recur (z/next loc)))))

(defn find-and-replace-all
  "Replace all occurrences of a pattern."
  [zloc pattern replacement-fn]
  (loop [loc zloc
         changed? false]
    (cond
      (z/end? loc) (if changed? (z/of-node (z/root loc)) zloc)
      
      (pattern loc)
      (let [new-loc (z/replace loc (replacement-fn loc))]
        (recur (z/next new-loc) true))
      
      :else (recur (z/next loc) changed?))))

;; =============================================================================
;; Code Generation Helpers
;; =============================================================================

(defn resolve-type-name
  "Resolve a type reference to a builder/parser name."
  [type-ref type-lookup prefix]
  (when type-ref
    (let [canonical-ref (str/replace type-ref #"^\." "")
          type-def (get type-lookup canonical-ref)]
      (when type-def
        (str prefix (name (:name type-def)))))))

;; =============================================================================
;; Field Generation
;; =============================================================================

(defn generate-field-setter-node
  "Generate AST node for setting a single field."
  [field]
  (n/list-node
   [(n/token-node 'when)
    (n/whitespace-node " ")
    (n/list-node
     [(n/token-node 'contains?)
      (n/whitespace-node " ")
      (n/token-node 'm)
      (n/whitespace-node " ")
      (n/keyword-node (:name field))])
    (n/newline-node "\n")
    (n/whitespace-node "      ")
    (n/list-node
     [(n/token-node (symbol (str ".set" (csk/->PascalCase (:proto-name field)))))
      (n/whitespace-node " ")
      (n/token-node 'builder)
      (n/whitespace-node " ")
      (n/list-node
       [(n/token-node 'get)
        (n/whitespace-node " ")
        (n/token-node 'm)
        (n/whitespace-node " ")
        (n/keyword-node (:name field))])])]))

(defn generate-field-getter-node
  "Generate AST node for getting a single field value."
  [field]
  (n/list-node
   [(n/token-node (symbol (str ".has" (csk/->PascalCase (:proto-name field)))))
    (n/whitespace-node " ")
    (n/token-node 'proto)
    (n/whitespace-node " ")
    (n/list-node
     [(n/token-node 'assoc)
      (n/whitespace-node " ")
      (n/keyword-node (:name field))
      (n/whitespace-node " ")
      (n/list-node
       [(n/token-node (symbol (str ".get" (csk/->PascalCase (:proto-name field)))))
        (n/whitespace-node " ")
        (n/token-node 'proto)])]]))

;; =============================================================================
;; Enum Generation
;; =============================================================================

(defn generate-enum-value-entry
  "Generate a single enum value entry node."
  [value enum]
  [(n/keyword-node (:name value))
   (n/whitespace-node " ")
   (n/token-node (symbol (str (:java-class enum) "/" 
                             (csk/->SCREAMING_SNAKE_CASE (:proto-name value)))))])

(defn generate-enum-reverse-entry
  "Generate a single reverse enum entry node."
  [value enum]
  [(n/token-node (symbol (str (:java-class enum) "/" 
                             (csk/->SCREAMING_SNAKE_CASE (:proto-name value)))))
   (n/whitespace-node " ")
   (n/keyword-node (:name value))])

(defn generate-enum-map-node
  "Generate enum map node."
  [enum entry-fn]
  (n/map-node
   (into []
         (comp (map #(entry-fn % enum))
               (interpose [(n/newline-node "\n") (n/whitespace-node "   ")])
               cat)
         (:values enum))))

;; =============================================================================
;; Template Manipulation Functions
;; =============================================================================

(defn process-builder-template
  "Process builder template with actual values."
  [template message type-lookup]
  (let [regular-fields (remove :oneof-index (:fields message))
        oneof-fields (filter :oneof-index (:fields message))
        fn-name (str "build-" (name (:name message)))]
    (-> template
        ;; Replace function name
        (find-and-replace 'BUILD-FN-NAME (symbol fn-name))
        ;; Replace docstring
        (find-and-replace-string "PROTO-NAME" (:proto-name message))
        ;; Replace Java class
        (find-and-replace 'JAVA-CLASS (symbol (:java-class message)))
        ;; Replace regular fields
        (find-and-replace 
         'REGULAR-FIELDS
         (if (seq regular-fields)
           (n/forms-node
            (cons (n/comment-node ";; Set regular fields")
                  (interpose (n/newline-node "\n    ")
                           (map generate-field-setter-node regular-fields))))
           (n/comment-node ";; No regular fields")))
        ;; Replace oneof payload
        (find-and-replace
         'ONEOF-PAYLOAD
         (if (seq oneof-fields)
           (n/forms-node
            [(n/newline-node "\n")
             (n/whitespace-node "    ")
             (n/comment-node ";; Set oneof payload")
             (n/newline-node "\n")
             (n/whitespace-node "    ")
             (n/list-node
              [(n/token-node 'when-let)
               (n/whitespace-node " ")
               (n/vector-node
                [(n/token-node 'payload)
                 (n/whitespace-node " ")
                 (n/list-node
                  [(n/token-node 'first)
                   (n/whitespace-node " ")
                   (n/list-node
                    [(n/token-node 'filter)
                     (n/whitespace-node " ")
                     (n/list-node
                      [(n/token-node 'fn)
                       (n/whitespace-node " ")
                       (n/vector-node
                        [(n/vector-node
                          [(n/token-node 'k)
                           (n/whitespace-node " ")
                           (n/token-node 'v)])])
                       (n/whitespace-node " ")
                       (n/list-node
                        [(n/set-node
                          (interpose (n/whitespace-node " ")
                                   (map #(n/keyword-node (:name %)) oneof-fields)))
                         (n/whitespace-node " ")
                         (n/token-node 'k)])])
                     (n/whitespace-node " ")
                     (n/token-node 'm)])])])
               (n/newline-node "\n")
               (n/whitespace-node "      ")
               (n/list-node
                [(n/token-node (symbol (str "build-" (csk/->kebab-case (:proto-name message)) "-payload")))
                 (n/whitespace-node " ")
                 (n/token-node 'builder)
                 (n/whitespace-node " ")
                 (n/token-node 'payload)])])])
           (n/comment-node ""))))))

(defn process-parser-template
  "Process parser template with actual values."
  [template message type-lookup]
  (let [regular-fields (remove :oneof-index (:fields message))
        fn-name (str "parse-" (name (:name message)))]
    (-> template
        ;; Replace function name
        (find-and-replace 'PARSE-FN-NAME (symbol fn-name))
        ;; Replace docstring
        (find-and-replace-string "PROTO-NAME" (:proto-name message))
        ;; Replace Java class
        (find-and-replace 'JAVA-CLASS (symbol (:java-class message)))
        ;; Replace regular fields
        (find-and-replace
         'REGULAR-FIELDS
         (if (seq regular-fields)
           (n/forms-node
            (cons (n/comment-node ";; Regular fields")
                  (interpose (n/newline-node "\n    ")
                           (map generate-field-getter-node regular-fields))))
           (n/comment-node ";; No fields")))
        ;; Replace oneof payload
        (find-and-replace
         'ONEOF-PAYLOAD
         (if (seq (:oneofs message))
           (n/forms-node
            [(n/newline-node "\n")
             (n/whitespace-node "    ")
             (n/comment-node ";; Oneof payload")
             (n/newline-node "\n")
             (n/whitespace-node "    ")
             (n/token-node 'true)
             (n/whitespace-node " ")
             (n/list-node
              [(n/token-node 'merge)
               (n/whitespace-node " ")
               (n/list-node
                [(n/token-node (symbol (str "parse-" (csk/->kebab-case (:proto-name message)) "-payload")))
                 (n/whitespace-node " ")
                 (n/token-node 'proto)])])])
           (n/comment-node ""))))))

(defn process-enum-template
  "Generate enum definitions."
  [enum]
  (let [values-name (str (csk/->kebab-case (:proto-name enum)) "-values")
        keywords-name (str (csk/->kebab-case (:proto-name enum)) "-keywords")]
    (n/forms-node
     [(n/comment-node (str ";; Enum: " (:proto-name enum)))
      (n/newline-node "\n")
      (n/list-node
       [(n/token-node 'def)
        (n/whitespace-node " ")
        (n/token-node (symbol values-name))
        (n/newline-node "\n")
        (n/whitespace-node "  ")
        (n/string-node (str "Keyword to Java enum mapping for " (:proto-name enum) "."))
        (n/newline-node "\n")
        (n/whitespace-node "  ")
        (generate-enum-map-node enum generate-enum-value-entry)])
      (n/newline-node "\n")
      (n/newline-node "\n")
      (n/list-node
       [(n/token-node 'def)
        (n/whitespace-node " ")
        (n/token-node (symbol keywords-name))
        (n/newline-node "\n")
        (n/whitespace-node "  ")
        (n/string-node (str "Java enum to keyword mapping for " (:proto-name enum) "."))
        (n/newline-node "\n")
        (n/whitespace-node "  ")
        (generate-enum-map-node enum generate-enum-reverse-entry)])])))

;; =============================================================================
;; Namespace Generation
;; =============================================================================

(defn process-namespace-template
  "Process namespace template with actual values."
  [template ns-name imports enums messages type-lookup]
  (-> template
      ;; Replace namespace
      (find-and-replace 'NAMESPACE-PLACEHOLDER (symbol ns-name))
      ;; Replace imports
      (find-and-replace
       'IMPORTS-PLACEHOLDER
       (if (seq imports)
         (n/vector-node
          (interpose (n/newline-node "\n   ")
                    (map #(n/token-node (symbol %)) imports)))
         (n/vector-node [])))
      ;; Replace enums
      (find-and-replace
       'ENUMS-PLACEHOLDER
       (if (seq enums)
         (n/forms-node
          (interpose (n/newline-node "\n\n")
                    (map process-enum-template enums)))
         (n/comment-node ";; No enums")))
      ;; Replace builders and parsers
      (find-and-replace
       'BUILDERS-AND-PARSERS-PLACEHOLDER
       (if (seq messages)
         (n/forms-node
          (interpose (n/newline-node "\n\n")
                    (mapcat (fn [msg]
                             (filter some?
                                    [(-> (load-template "builder.clj")
                                         (process-builder-template msg type-lookup)
                                         z/root)
                                     (when (seq (:oneofs msg))
                                       ;; TODO: Process oneof builder template
                                       nil)
                                     (-> (load-template "parser.clj")
                                         (process-parser-template msg type-lookup)
                                         z/root)
                                     (when (seq (:oneofs msg))
                                       ;; TODO: Process oneof parser template
                                       nil)]))
                           messages)))
         (n/comment-node ";; No messages")))))

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
  "Generate complete Clojure code from EDN data using rewrite-clj."
  [{:keys [ns-name edn-data type-lookup]}]
  (let [imports (collect-imports edn-data)
        messages (sp/select [:files sp/ALL :messages sp/ALL] edn-data)
        enums (sp/select [:files sp/ALL :enums sp/ALL] edn-data)
        template (load-template "namespace.clj")]
    (-> template
        (process-namespace-template ns-name imports enums messages type-lookup)
        z/root
        n/string)))

(defn generate-from-backend
  "Generate Clojure code from backend EDN output."
  [{:keys [command state type-lookup] :as backend-output} ns-prefix]
  {:command (generate-code {:ns-name (str ns-prefix ".command")
                           :edn-data command
                           :type-lookup type-lookup})
   :state (generate-code {:ns-name (str ns-prefix ".state")
                         :edn-data state
                         :type-lookup type-lookup})})
