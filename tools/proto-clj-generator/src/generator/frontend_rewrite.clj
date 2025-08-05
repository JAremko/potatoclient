(ns generator.frontend-rewrite
  "Frontend using rewrite-clj for proper Clojure code generation."
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
  "Load a template file from resources and parse it with rewrite-clj."
  [template-name]
  (if-let [resource (io/resource (str "templates/" template-name))]
    (-> resource slurp z/of-string)
    (throw (ex-info "Template not found" {:template template-name}))))

;; =============================================================================
;; Code Replacement Utilities
;; =============================================================================

(defn find-and-replace-all
  "Find all occurrences of a symbol and replace them."
  [zloc sym-to-find replacement]
  (loop [z zloc]
    (if-let [found (z/find-value z z/next sym-to-find)]
      (recur (z/replace found replacement))
      z)))

(defn replace-in-string-node
  "Replace text within string nodes (for docstrings)."
  [zloc old-text new-text]
  (loop [z zloc]
    (if-let [next-z (z/find z z/next #(and (string? (z/sexpr %))
                                           (str/includes? (z/sexpr %) old-text)))]
      (let [current-string (z/sexpr next-z)
            new-string (str/replace current-string old-text new-text)]
        (recur (z/replace next-z (n/string-node new-string))))
      z)))

(defn replace-java-class-constructor
  "Replace JAVA-CLASS/newBuilder pattern."
  [zloc old-class new-class]
  (loop [z zloc]
    (if-let [found (z/find z z/next #(and (z/list? %)
                                          (= 2 (count (z/sexpr %)))
                                          (= (symbol (str old-class "/newBuilder"))
                                             (first (z/sexpr %)))))]
      (let [new-node (n/list-node [(n/token-node (symbol (str new-class "/newBuilder")))])]
        (recur (z/replace found new-node)))
      z)))

(defn replace-method-call
  "Replace method calls like .GET-ONEOF-CASE."
  [zloc old-method new-method]
  (loop [z zloc]
    (if-let [found (z/find z z/next #(and (symbol? (z/sexpr %))
                                          (= (z/sexpr %) old-method)))]
      (recur (z/replace found (symbol new-method)))
      z)))

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

(defn generate-field-setter
  "Generate setter code for a single field."
  [field]
  (let [setter (field-java-setter field)
        field-key (:name field)]
    (n/list-node
     [(n/token-node 'when)
      (n/list-node [(n/token-node 'contains?) (n/token-node 'm) (n/keyword-node field-key)])
      (n/list-node [(n/token-node (symbol setter)) 
                    (n/token-node 'builder) 
                    (n/list-node [(n/token-node 'get) (n/token-node 'm) (n/keyword-node field-key)])])])))

(defn generate-field-parser
  "Generate parser code for a single field."
  [field]
  (let [has-method (field-java-has field)
        getter (field-java-getter field)
        field-key (:name field)]
    [(n/token-node (symbol has-method))
     (n/token-node 'proto)
     (n/list-node [(n/token-node 'assoc) 
                   (n/keyword-node field-key) 
                   (n/list-node [(n/token-node (symbol getter)) (n/token-node 'proto)])])]))

(defn generate-enum-value-node
  "Generate a single enum value mapping node."
  [enum-value enum-class]
  [(n/keyword-node (csk/->kebab-case-keyword (:proto-name enum-value)))
   (n/token-node (symbol (str enum-class "/" (:proto-name enum-value))))])

(defn generate-enum-reverse-node
  "Generate a single reverse enum mapping node."
  [enum-value enum-class]
  [(n/token-node (symbol (str enum-class "/" (:proto-name enum-value))))
   (n/keyword-node (csk/->kebab-case-keyword (:proto-name enum-value)))])

;; =============================================================================
;; Import Collection
;; =============================================================================

(defn collect-imports
  "Collect all Java imports needed for the generated code."
  [edn-data]
  (->> (sp/select [:files sp/ALL (sp/multi-path :messages :enums) sp/ALL :java-class] edn-data)
       (map #(str/replace % #"\$.*" ""))
       (remove #(or (str/starts-with? % "com.google.protobuf.")
                   (str/starts-with? % "build.buf.validate.")))
       distinct
       sort))

;; =============================================================================
;; Main Code Generation Functions
;; =============================================================================

(defn generate-builder
  "Generate a builder function using rewrite-clj."
  [message type-lookup]
  (let [zloc (load-template "builder.clj")
        fn-name (symbol (str "build-" (name (:name message))))
        regular-fields (:fields message)
        field-nodes (if (seq regular-fields)
                     (n/spaces-node (map generate-field-setter regular-fields))
                     (n/comment-node " No regular fields"))
        oneof-payload (if (seq (:oneofs message))
                       (n/list-node
                        [(n/token-node 'when-let)
                         (n/vector-node [(n/token-node 'payload) 
                                        (n/list-node [(n/token-node 'seq) 
                                                     (n/list-node [(n/token-node 'select-keys) 
                                                                  (n/token-node 'm) 
                                                                  (n/vector-node (map #(n/keyword-node (:name %))
                                                                                    (mapcat :fields (:oneofs message))))])])])
                         (n/list-node [(n/token-node (symbol (str "build-" (name (:name message)) "-payload")))
                                      (n/token-node 'builder)
                                      (n/list-node [(n/token-node 'first) (n/token-node 'payload)])])])
                       (n/whitespace-node " "))]
    
    (-> zloc
        (find-and-replace-all 'BUILD-FN-NAME fn-name)
        (replace-in-string-node "PROTO-NAME" (:proto-name message))
        (replace-java-class-constructor 'JAVA-CLASS (:java-class message))
        (find-and-replace-all 'REGULAR-FIELDS field-nodes)
        (find-and-replace-all 'ONEOF-PAYLOAD oneof-payload)
        z/root-string)))

(defn generate-parser
  "Generate a parser function using rewrite-clj."
  [message type-lookup]
  (let [zloc (load-template "parser.clj")
        fn-name (symbol (str "parse-" (name (:name message))))
        regular-fields (:fields message)
        field-nodes (if (seq regular-fields)
                     (apply n/newline-node 
                           (map generate-field-parser regular-fields))
                     (n/comment-node " No fields"))
        oneof-payload (if (seq (:oneofs message))
                       (n/spaces-node
                        [(n/token-node 'true)
                         (n/list-node [(n/token-node 'merge)
                                      (n/list-node [(n/token-node (symbol (str "parse-" (name (:name message)) "-payload")))
                                                   (n/token-node 'proto)])])])
                       (n/whitespace-node " "))]
    
    (-> zloc
        (find-and-replace-all 'PARSE-FN-NAME fn-name)
        (replace-in-string-node "PROTO-NAME" (:proto-name message))
        (find-and-replace-all 'JAVA-CLASS (symbol (:java-class message)))
        (find-and-replace-all 'REGULAR-FIELDS field-nodes)
        (find-and-replace-all 'ONEOF-PAYLOAD oneof-payload)
        z/root-string)))

(defn generate-enum
  "Generate enum mappings using nodes."
  [enum]
  (let [enum-name (:name enum)
        enum-class (:java-class enum)
        values-def (str "(def " (name enum-name) "-values\n"
                       "  \"Keyword to Java enum mapping for " (:proto-name enum) ".\"\n"
                       "  " (pr-str (into {} (map #(generate-enum-value-node % enum-class) (:values enum)))) ")")
        
        keywords-def (str "\n\n(def " (name enum-name) "-keywords\n"
                         "  \"Java enum to keyword mapping for " (:proto-name enum) ".\"\n"
                         "  " (pr-str (into {} (map #(generate-enum-reverse-node % enum-class) (:values enum)))) ")")]
    
    (str ";; Enum: " (:proto-name enum) "\n"
         values-def
         keywords-def)))

(defn generate-namespace
  "Generate a complete namespace using rewrite-clj."
  [{:keys [ns-name edn-data type-lookup]}]
  (let [zloc (load-template "namespace.clj")
        imports (collect-imports edn-data)
        messages (sp/select [:files sp/ALL :messages sp/ALL] edn-data)
        enums (sp/select [:files sp/ALL :enums sp/ALL] edn-data)
        
        ;; Generate import nodes
        import-nodes (if (seq imports)
                      (apply n/newline-node
                            (map #(n/token-node (symbol %)) imports))
                      (n/comment-node " No imports needed"))
        
        ;; Generate enum code
        enum-code (if (seq enums)
                   (n/string-node (str/join "\n\n" (map generate-enum enums)))
                   (n/comment-node " No enums"))
        
        ;; Generate builders and parsers
        builder-parser-code (if (seq messages)
                             (n/string-node 
                              (str/join "\n\n" 
                                       (mapcat (fn [msg]
                                                (concat
                                                 [(generate-builder msg type-lookup)]
                                                 (when (seq (:oneofs msg))
                                                   ["oneof builder TBD"])
                                                 [(generate-parser msg type-lookup)]
                                                 (when (seq (:oneofs msg))
                                                   ["oneof parser TBD"])))
                                              messages)))
                             (n/comment-node " No messages"))]
    
    (-> zloc
        (find-and-replace-all 'NAMESPACE-PLACEHOLDER (symbol ns-name))
        (find-and-replace-all 'IMPORTS-PLACEHOLDER import-nodes)
        (find-and-replace-all 'ENUMS-PLACEHOLDER enum-code)
        (find-and-replace-all 'BUILDERS-AND-PARSERS-PLACEHOLDER builder-parser-code)
        z/root-string)))

(defn generate-from-backend
  "Generate Clojure code from backend EDN output."
  [{:keys [command state type-lookup] :as backend-output} ns-prefix]
  {:command (generate-namespace {:ns-name (str ns-prefix ".command")
                                :edn-data command
                                :type-lookup type-lookup})
   :state (generate-namespace {:ns-name (str ns-prefix ".state")
                              :edn-data state
                              :type-lookup type-lookup})})