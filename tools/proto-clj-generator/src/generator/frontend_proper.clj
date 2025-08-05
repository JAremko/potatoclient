(ns generator.frontend-proper
  "Frontend using rewrite-clj properly for AST-based code generation.
  No string manipulation - pure AST transformations."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [camel-snake-kebab.core :as csk]
            [com.rpl.specter :as sp]
            [rewrite-clj.zip :as z]
            [rewrite-clj.parser :as p]
            [rewrite-clj.node :as n]))

;; =============================================================================
;; AST Building Functions
;; =============================================================================

(defn sym-node
  "Create a symbol node."
  [s]
  (n/token-node (symbol s)))

(defn kw-node
  "Create a keyword node."
  [k]
  (if (keyword? k)
    (n/keyword-node k)
    (n/keyword-node (keyword k))))

(defn str-node
  "Create a string node."
  [s]
  (n/string-node s))

(defn ws
  "Create whitespace node."
  ([] (n/whitespace-node " "))
  ([n] (n/whitespace-node (apply str (repeat n " ")))))

(defn nl
  "Create newline node."
  []
  (n/newline-node "\n"))

(defn comment-node
  "Create a comment node."
  [text]
  (n/comment-node (str ";; " text)))

;; =============================================================================
;; Complex Node Builders
;; =============================================================================

(defn import-node
  "Create an import form for a Java class."
  [class-name]
  (sym-node class-name))

(defn defn-node
  "Create a defn form."
  [name docstring params & body]
  (n/list-node
   (concat
    [(sym-node 'defn) (ws) (sym-node name)]
    (when docstring
      [(nl) (ws 2) (str-node docstring)])
    [(nl) (ws 2) (n/vector-node params)]
    (map (fn [form] (n/list-node [(nl) (ws 2) form])) body))))

(defn def-node
  "Create a def form."
  [name docstring value]
  (n/list-node
   (concat
    [(sym-node 'def) (ws) (sym-node name)]
    (when docstring
      [(nl) (ws 2) (str-node docstring)])
    [(nl) (ws 2) value])))

(defn let-node
  "Create a let form."
  [bindings & body]
  (n/list-node
   (concat
    [(sym-node 'let) (ws) (n/vector-node bindings)]
    (map (fn [form] (n/list-node [(nl) (ws 4) form])) body))))

(defn when-node
  "Create a when form."
  [test & body]
  (n/list-node
   (concat
    [(sym-node 'when) (ws) test]
    (map (fn [form] (n/list-node [(nl) (ws 4) form])) body))))

(defn cond-node
  "Create a cond form."
  [& clauses]
  (n/list-node
   (concat
    [(sym-node 'cond)]
    (mapcat (fn [[test expr]]
             [(nl) (ws 4) test (ws) expr])
           (partition 2 clauses)))))

(defn case-node
  "Create a case form."
  [test & clauses]
  (n/list-node
   (concat
    [(sym-node 'case) (ws) test]
    (mapcat (fn [[k v]]
             [(nl) (ws 4) k (ws) v])
           (partition 2 clauses)))))

(defn map-node
  "Create a map node from alternating keys and values."
  [& kvs]
  (n/map-node kvs))

(defn vec-node
  "Create a vector node."
  [& items]
  (n/vector-node items))

(defn method-call-node
  "Create a Java method call (.method obj args...)."
  [method obj & args]
  (n/list-node
   (concat
    [(sym-node (str "." method)) (ws) obj]
    (when (seq args)
      (mapcat (fn [arg] [(ws) arg]) args)))))

(defn static-field-node
  "Create a Java static field access (Class/FIELD)."
  [class-name field-name]
  (sym-node (str class-name "/" field-name)))

;; =============================================================================
;; Field Generation
;; =============================================================================

(defn field-java-setter
  "Generate Java setter method name for a field."
  [field]
  (str "set" (csk/->PascalCase (:proto-name field))))

(defn field-java-getter
  "Generate Java getter method name for a field."
  [field]
  (str "get" (csk/->PascalCase (:proto-name field))))

(defn field-java-has
  "Generate Java has method name for a field."
  [field]
  (str "has" (csk/->PascalCase (:proto-name field))))

(defn generate-field-setter
  "Generate AST for setting a single field."
  [field]
  (when-node
   (n/list-node [(sym-node 'contains?) (ws) (sym-node 'm) (ws) (kw-node (:name field))])
   (method-call-node
    (field-java-setter field)
    (sym-node 'builder)
    (n/list-node [(sym-node 'get) (ws) (sym-node 'm) (ws) (kw-node (:name field))]))))

(defn generate-field-getter
  "Generate AST for getting a single field."
  [field]
  (n/list-node
   [(method-call-node (field-java-has field) (sym-node 'proto))
    (ws)
    (n/list-node
     [(sym-node 'assoc)
      (ws)
      (kw-node (:name field))
      (ws)
      (method-call-node (field-java-getter field) (sym-node 'proto))])]))

;; =============================================================================
;; Builder Generation
;; =============================================================================

(defn generate-builder-function
  "Generate a complete builder function AST."
  [message type-lookup]
  (let [regular-fields (remove :oneof-index (:fields message))
        oneof-fields (filter :oneof-index (:fields message))
        fn-name (str "build-" (name (:name message)))
        builder-class (str (:java-class message) "/newBuilder")]
    (defn-node
     fn-name
     (str "Build a " (:proto-name message) " protobuf message from a map.")
     [(sym-node 'm)]
     (let-node
      [(sym-node 'builder) (n/list-node [(sym-node builder-class)])]
      ;; Set regular fields
      (when (seq regular-fields)
        (n/list-node
         (concat
          [(comment-node "Set regular fields")]
          (map generate-field-setter regular-fields))))
      ;; Set oneof payload
      (when (seq oneof-fields)
        (when-node
         (n/list-node
          [(sym-node 'let)
           (ws)
           (n/vector-node
            [(sym-node 'payload)
             (n/list-node
              [(sym-node 'first)
               (ws)
               (n/list-node
                [(sym-node 'filter)
                 (ws)
                 (n/list-node
                  [(sym-node 'fn)
                   (ws)
                   (n/vector-node [(n/vector-node [(sym-node 'k) (ws) (sym-node 'v)])])
                   (nl)
                   (ws 21)
                   (n/list-node
                    [(n/set-node (map kw-node (map :name oneof-fields)))
                     (ws)
                     (sym-node 'k)])])
                 (ws)
                 (sym-node 'm)])])])])
         (n/list-node
          [(sym-node (str "build-" (csk/->kebab-case (:proto-name message)) "-payload"))
           (ws)
           (sym-node 'builder)
           (ws)
           (sym-node 'payload)])))
      ;; Build and return
      (method-call-node "build" (sym-node 'builder))))))

;; =============================================================================
;; Parser Generation
;; =============================================================================

(defn generate-parser-function
  "Generate a complete parser function AST."
  [message type-lookup]
  (let [regular-fields (remove :oneof-index (:fields message))
        fn-name (str "parse-" (name (:name message)))]
    (defn-node
     fn-name
     (str "Parse a " (:proto-name message) " protobuf message to a map.")
     [(sym-node 'proto)]
     (cond-node
      ;; Regular fields
      (when (seq regular-fields)
        [(sym-node 'true)
         (n/list-node
          (concat
           [(sym-node 'cond->)
            (ws)
            (map-node)]
           (mapcat (fn [field]
                    [(nl)
                     (ws 4)
                     (generate-field-getter field)])
                  regular-fields)))])
      ;; Oneof payload
      (when (seq (:oneofs message))
        [(sym-node 'true)
         (n/list-node
          [(sym-node 'merge)
           (ws)
           (n/list-node
            [(sym-node (str "parse-" (csk/->kebab-case (:proto-name message)) "-payload"))
             (ws)
             (sym-node 'proto)])])])))))

;; =============================================================================
;; Enum Generation
;; =============================================================================

(defn generate-enum-defs
  "Generate enum value and keyword mappings."
  [enum]
  (let [values-name (str (csk/->kebab-case (:proto-name enum)) "-values")
        keywords-name (str (csk/->kebab-case (:proto-name enum)) "-keywords")
        value-map (n/map-node
                  (mapcat (fn [v]
                           [(kw-node (:name v))
                            (ws)
                            (static-field-node
                             (:java-class enum)
                             (csk/->SCREAMING_SNAKE_CASE (:proto-name v)))])
                         (:values enum)))
        keyword-map (n/map-node
                    (mapcat (fn [v]
                             [(static-field-node
                               (:java-class enum)
                               (csk/->SCREAMING_SNAKE_CASE (:proto-name v)))
                              (ws)
                              (kw-node (:name v))])
                           (:values enum)))]
    (n/list-node
     [(comment-node (str "Enum: " (:proto-name enum)))
      (nl)
      (def-node values-name
                (str "Keyword to Java enum mapping for " (:proto-name enum) ".")
                value-map)
      (nl) (nl)
      (def-node keywords-name
                (str "Java enum to keyword mapping for " (:proto-name enum) ".")
                keyword-map)])))

;; =============================================================================
;; Namespace Generation
;; =============================================================================

(defn generate-ns-form
  "Generate a namespace declaration."
  [ns-name imports]
  (n/list-node
   (concat
    [(sym-node 'ns) (ws) (sym-node ns-name)]
    (when (seq imports)
      [(nl) (ws 2)
       (n/list-node
        (concat
         [(kw-node :import)]
         (map (fn [imp] (n/list-node [(nl) (ws 3) (import-node imp)])) imports)))]))))

;; =============================================================================
;; Main Code Generation
;; =============================================================================

(defn generate-code
  "Generate complete Clojure code from EDN data as AST."
  [{:keys [ns-name edn-data type-lookup]}]
  (let [imports (sp/select [:files sp/ALL 
                           (sp/multi-path :messages :enums) 
                           sp/ALL :java-class]
                          edn-data)
        imports (->> imports
                    (map #(str/replace % #"\$.*" ""))
                    (remove #(or (empty? %)
                               (str/starts-with? % "com.google.protobuf.")
                               (str/starts-with? % "build.buf.validate.")))
                    distinct
                    sort)
        messages (sp/select [:files sp/ALL :messages sp/ALL] edn-data)
        enums (sp/select [:files sp/ALL :enums sp/ALL] edn-data)]
    
    ;; Build complete AST
    (n/forms-node
     (concat
      ;; Namespace declaration
      [(generate-ns-form ns-name imports)]
      ;; Enums
      (when (seq enums)
        (concat [(nl) (nl)] (interpose (nl) (map generate-enum-defs enums))))
      ;; Builders and parsers
      (when (seq messages)
        (concat [(nl) (nl)]
               (interpose (nl)
                         (mapcat (fn [msg]
                                  [(generate-builder-function msg type-lookup)
                                   (when (seq (:oneofs msg))
                                     ;; TODO: Generate oneof builder
                                     nil)
                                   (generate-parser-function msg type-lookup)
                                   (when (seq (:oneofs msg))
                                     ;; TODO: Generate oneof parser
                                     nil)])
                                messages))))))))

(defn generate-from-backend
  "Generate Clojure code from backend EDN output."
  [{:keys [command state type-lookup] :as backend-output} ns-prefix]
  {:command (n/string (generate-code {:ns-name (str ns-prefix ".command")
                                     :edn-data command
                                     :type-lookup type-lookup}))
   :state (n/string (generate-code {:ns-name (str ns-prefix ".state")
                                   :edn-data state
                                   :type-lookup type-lookup}))})