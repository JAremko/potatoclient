(ns generator.templates
  "rewrite-clj templates for generating protobuf conversion code."
  (:require [rewrite-clj.zip :as z]
            [rewrite-clj.node :as n]
            [clojure.string :as str]
            [clojure.core.match :refer [match]]
            [taoensso.timbre :as log]))

;; =============================================================================
;; Helper Functions
;; =============================================================================

(defn pascal-case
  "Convert kebab-case to PascalCase for Java method names."
  [s]
  (->> (str/split (name s) #"-")
       (map str/capitalize)
       (str/join)))

(defn camel-case
  "Convert kebab-case to camelCase."
  [s]
  (let [parts (str/split (name s) #"-")]
    (str (first parts)
         (str/join (map str/capitalize (rest parts))))))

(defn proto-enum-value
  "Convert kebab-case keyword to PROTO_ENUM_VALUE."
  [k]
  (-> (name k)
      (str/upper-case)
      (str/replace #"-" "_")))

;; =============================================================================
;; AST Node Creation Helpers
;; =============================================================================

(defn sym-node
  "Create a symbol node."
  [s]
  (n/token-node (symbol s)))

(defn kw-node
  "Create a keyword node."
  [k]
  (n/token-node k))

(defn str-node
  "Create a string node."
  [s]
  (n/string-node s))

(defn num-node
  "Create a number node."
  [n]
  (n/token-node n))

(defn import-node
  "Create an import form node."
  [class-names]
  (n/list-node
    [(sym-node 'import)
     (n/vector-node
       (map (fn [cn]
              (if (vector? cn)
                (n/vector-node (map sym-node cn))
                (sym-node cn)))
            class-names))]))

;; =============================================================================
;; Field Setter Templates
;; =============================================================================

(defn simple-field-setter
  "Generate setter for simple fields (string, int, etc)."
  [{:keys [java-setter kebab-name]}]
  (n/list-node
    [(sym-node (str "." java-setter))
     (n/list-node [(sym-node 'get) (sym-node 'm) (kw-node kebab-name)])]))

(defn enum-field-setter
  "Generate setter for enum fields."
  [{:keys [java-setter kebab-name type-name]}]
  (let [enum-class (last (str/split type-name #"\."))]
    (n/list-node
      [(sym-node (str "." java-setter))
       (n/list-node
         [(sym-node 'enum->proto)
          (sym-node enum-class)
          (n/list-node [(sym-node 'get) (sym-node 'm) (kw-node kebab-name)])])])))

(defn message-field-setter
  "Generate setter for message fields."
  [{:keys [java-setter kebab-name type-name]}]
  (let [builder-fn (str "build-" (-> type-name
                                     (str/split #"\.")
                                     last
                                     (str/replace #"([a-z])([A-Z])" "$1-$2")
                                     str/lower-case))]
    (n/list-node
      [(sym-node (str "." java-setter))
       (n/list-node
         [(sym-node builder-fn)
          (n/list-node [(sym-node 'get) (sym-node 'm) (kw-node kebab-name)])])])))

(defn repeated-field-setter
  "Generate setter for repeated fields."
  [{:keys [java-setter kebab-name type proto-type type-name] :as field}]
  (let [add-all-method (str ".addAll" (pascal-case (:name field)))]
    (cond
      ;; Repeated message type
      (= type :message)
      (let [builder-fn (str "build-" (-> type-name
                                        (str/split #"\.")
                                        last
                                        (str/replace #"([a-z])([A-Z])" "$1-$2")
                                        str/lower-case))]
        (n/list-node
          [(sym-node add-all-method)
           (n/list-node
             [(sym-node 'map)
              (sym-node builder-fn)
              (n/list-node [(sym-node 'get) (sym-node 'm) (kw-node kebab-name)])])]))
      
      ;; Repeated enum type
      (= type :enum)
      (let [enum-class (last (str/split type-name #"\."))]
        (n/list-node
          [(sym-node add-all-method)
           (n/list-node
             [(sym-node 'map)
              (n/list-node
                [(sym-node 'partial)
                 (sym-node 'enum->proto)
                 (sym-node enum-class)])
              (n/list-node [(sym-node 'get) (sym-node 'm) (kw-node kebab-name)])])]))
      
      ;; Repeated simple type
      :else
      (n/list-node
        [(sym-node add-all-method)
         (n/list-node [(sym-node 'get) (sym-node 'm) (kw-node kebab-name)])]))))

(defn field->setter
  "Generate the appropriate setter based on field type."
  [field]
  (cond
    (:repeated? field) (repeated-field-setter field)
    (= (:type field) :enum) (enum-field-setter field)
    (= (:type field) :message) (message-field-setter field)
    :else (simple-field-setter field)))

;; =============================================================================
;; Field Getter Templates
;; =============================================================================

(defn simple-field-getter
  "Generate getter for simple fields."
  [{:keys [java-getter kebab-name]}]
  (n/vector-node
    [(kw-node kebab-name)
     (n/list-node [(sym-node (str "." java-getter)) (sym-node 'proto)])]))

(defn enum-field-getter
  "Generate getter for enum fields."
  [{:keys [java-getter kebab-name]}]
  (n/vector-node
    [(kw-node kebab-name)
     (n/list-node
       [(sym-node 'proto->enum)
        (n/list-node [(sym-node (str "." java-getter)) (sym-node 'proto)])])]))

(defn message-field-getter
  "Generate getter for message fields."
  [{:keys [java-getter kebab-name type-name]}]
  (let [parser-fn (str "parse-" (-> type-name
                                   (str/split #"\.")
                                   last
                                   (str/replace #"([a-z])([A-Z])" "$1-$2")
                                   str/lower-case))]
    (n/vector-node
      [(kw-node kebab-name)
       (n/list-node
         [(sym-node parser-fn)
          (n/list-node [(sym-node (str "." java-getter)) (sym-node 'proto)])])])))

(defn repeated-field-getter
  "Generate getter for repeated fields."
  [{:keys [java-getter kebab-name type type-name] :as field}]
  (let [list-method (str ".get" (pascal-case (:name field)) "List")]
    (cond
      ;; Repeated message type
      (= type :message)
      (let [parser-fn (str "parse-" (-> type-name
                                       (str/split #"\.")
                                       last
                                       (str/replace #"([a-z])([A-Z])" "$1-$2")
                                       str/lower-case))]
        (n/vector-node
          [(kw-node kebab-name)
           (n/list-node
             [(sym-node 'mapv)
              (sym-node parser-fn)
              (n/list-node [(sym-node list-method) (sym-node 'proto)])])]))
      
      ;; Repeated enum type
      (= type :enum)
      (n/vector-node
        [(kw-node kebab-name)
         (n/list-node
           [(sym-node 'mapv)
            (sym-node 'proto->enum)
            (n/list-node [(sym-node list-method) (sym-node 'proto)])])])
      
      ;; Repeated simple type
      :else
      (n/vector-node
        [(kw-node kebab-name)
         (n/list-node
           [(sym-node 'vec)
            (n/list-node [(sym-node list-method) (sym-node 'proto)])])]))))

(defn field->getter
  "Generate the appropriate getter based on field type."
  [field]
  (cond
    (:repeated? field) (repeated-field-getter field)
    (= (:type field) :enum) (enum-field-getter field)
    (= (:type field) :message) (message-field-getter field)
    :else (simple-field-getter field)))

;; =============================================================================
;; Message Builder/Parser Templates
;; =============================================================================

(defn message-builder-template
  "Generate a builder function for a protobuf message."
  [{:keys [name java-class fields kebab-name]}]
  (let [builder-name (str "build-" (clojure.core/name kebab-name))
        non-oneof-fields (remove :oneof-index fields)]
    (n/list-node
      [(sym-node 'defn)
       (sym-node builder-name)
       (str-node (str "Build a " name " protobuf message from a map."))
       (n/vector-node [(sym-node 'm)])
       (n/list-node
         (concat
           [(sym-node '->)
            (n/list-node
              [(sym-node (str java-class "/newBuilder"))])]
           (map (fn [field]
                  (n/list-node
                    [(sym-node 'cond->)
                     (n/list-node
                       [(sym-node 'contains?)
                        (sym-node 'm)
                        (kw-node (:kebab-name field))])
                     (field->setter field)]))
                non-oneof-fields)
           [(n/list-node
              [(sym-node '.build)])]))])))

(defn message-parser-template
  "Generate a parser function for a protobuf message."
  [{:keys [name java-class fields kebab-name]}]
  (let [parser-name (str "parse-" (clojure.core/name kebab-name))
        type-hint (symbol (str "^" java-class))
        non-oneof-fields (remove :oneof-index fields)]
    (n/list-node
      [(sym-node 'defn)
       (sym-node parser-name)
       (str-node (str "Parse a " name " protobuf message to a map."))
       (n/vector-node [(n/meta-node
                         (sym-node 'proto)
                         (n/map-node [(kw-node :tag) (sym-node java-class)]))])
       (n/list-node
         (concat
           [(sym-node 'cond->)
            (n/map-node [])]
           (interpose
             (n/newline-node "\n")
             (mapcat (fn [field]
                       (if (:optional? field)
                         [(n/list-node
                            [(sym-node (str "." (:java-has field)))
                             (sym-node 'proto)])
                          (field->getter field)]
                         [(sym-node 'true)
                          (field->getter field)]))
                     non-oneof-fields))))]))

;; =============================================================================
;; Oneof Templates
;; =============================================================================

(defn oneof-builder-case
  "Generate a case for building a oneof field."
  [field parent-class]
  (let [field-key (:kebab-name field)
        setter-method (str ".set" (pascal-case (:name field)))]
    [(kw-node field-key)
     (n/list-node
       [(sym-node '->)
        (n/list-node [(sym-node (str parent-class "/newBuilder"))])
        (if (= (:type field) :message)
          (let [builder-fn (str "build-" (-> (:type-name field)
                                           (str/split #"\.")
                                           last
                                           (str/replace #"([a-z])([A-Z])" "$1-$2")
                                           str/lower-case))]
            (n/list-node
              [(sym-node setter-method)
               (n/list-node
                 [(sym-node builder-fn)
                  (n/list-node [(sym-node 'second) (sym-node 'm)])])]))
          (n/list-node
            [(sym-node setter-method)
             (n/list-node [(sym-node 'second) (sym-node 'm)])]))
        (n/list-node [(sym-node '.build)])])]))

(defn oneof-parser-case
  "Generate a case for parsing a oneof field."
  [field enum-value]
  (let [field-key (:kebab-name field)
        getter-method (str ".get" (pascal-case (:name field)))]
    [(sym-node enum-value)
     (n/map-node
       [(kw-node field-key)
        (if (= (:type field) :message)
          (let [parser-fn (str "parse-" (-> (:type-name field)
                                          (str/split #"\.")
                                          last
                                          (str/replace #"([a-z])([A-Z])" "$1-$2")
                                          str/lower-case))]
            (n/list-node
              [(sym-node parser-fn)
               (n/list-node [(sym-node getter-method) (sym-node 'proto)])]))
          (n/list-node [(sym-node getter-method) (sym-node 'proto)]))])]))

(defn oneof-builder-template
  "Generate builder for a message with oneof fields."
  [{:keys [name java-class oneofs kebab-name]}]
  (when (seq oneofs)
    (let [builder-name (str "build-" (clojure.core/name kebab-name) "-oneof")
          oneof (first oneofs) ; Assuming one oneof per message for now
          oneof-fields (:fields oneof)]
      (n/list-node
        [(sym-node 'defn)
         (sym-node builder-name)
         (str-node (str "Build " name " with oneof field."))
         (n/vector-node [(sym-node 'm)])
         (n/list-node
           (concat
             [(sym-node 'match)
              (n/list-node [(sym-node 'first) (sym-node 'm)])]
             (interleave
               (map #(oneof-builder-case % java-class) oneof-fields)
               (repeat (n/newline-node "\n")))
             [(kw-node :else)
              (n/list-node
                [(sym-node 'throw)
                 (n/list-node
                   [(sym-node 'ex-info)
                    (str-node "Unknown oneof variant")
                    (n/map-node [(kw-node :data) (sym-node 'm)])])])]))]))))

;; =============================================================================
;; Enum Templates
;; =============================================================================

(defn enum-converter-template
  "Generate enum conversion functions."
  [{:keys [name java-class values kebab-name]}]
  (let [enum->proto-name (str (clojure.core/name kebab-name) "->proto")
        proto->enum-name (str "proto->" (clojure.core/name kebab-name))]
    [(n/list-node
       [(sym-node 'defn)
        (sym-node enum->proto-name)
        (str-node (str "Convert " name " keyword to protobuf enum."))
        (n/vector-node [(sym-node 'k)])
        (n/list-node
          (concat
            [(sym-node 'case)
             (sym-node 'k)]
            (interleave
              (map (fn [v]
                     (kw-node (:kebab-name v)))
                   values)
              (map (fn [v]
                     (n/list-node
                       [(sym-node (str java-class "/valueOf"))
                        (str-node (:name v))]))
                   values))
            [(kw-node :else)
             (n/list-node
               [(sym-node 'throw)
                (n/list-node
                  [(sym-node 'ex-info)
                   (str-node (str "Unknown " name " value"))
                   (n/map-node [(kw-node :value) (sym-node 'k)])])])]))])
     (n/list-node
       [(sym-node 'defn)
        (sym-node proto->enum-name)
        (str-node (str "Convert protobuf " name " to keyword."))
        (n/vector-node [(sym-node 'e)])
        (n/list-node
          (concat
            [(sym-node 'condp)
             (sym-node '=)
             (n/list-node [(sym-node '.name) (sym-node 'e)])]
            (interleave
              (map (fn [v] (str-node (:name v))) values)
              (map (fn [v] (kw-node (:kebab-name v))) values))
            [(kw-node :else)
             (kw-node :unknown)]))])]))

;; =============================================================================
;; Namespace Template
;; =============================================================================

(defn namespace-template
  "Generate the namespace declaration with imports."
  [ns-name imports]
  (n/list-node
    [(sym-node 'ns)
     (sym-node ns-name)
     (str-node "Generated protobuf conversion functions.")
     (n/list-node
       [(kw-node :require)
        (n/vector-node [(sym-node 'clojure.core.match) (kw-node :refer) (n/vector-node [(sym-node 'match)])])
        (n/vector-node [(sym-node 'malli.core) (kw-node :as) (sym-node 'm)])
        (n/vector-node [(sym-node 'malli.generator) (kw-node :as) (sym-node 'mg)])
        (n/vector-node [(sym-node 'potatoclient.specs.malli-oneof) (kw-node :as) (sym-node 'oneof)])])
     (when (seq imports)
       (n/list-node
         (concat
           [(kw-node :import)]
           (map (fn [import-group]
                  (n/vector-node
                    (map sym-node import-group)))
                imports))))]))

;; =============================================================================
;; Main Template Generation
;; =============================================================================

(defn generate-code
  "Generate complete Clojure namespace with all conversion functions."
  [{:keys [ns-name messages enums imports]}]
  (let [nodes (concat
                 ;; Namespace declaration
                 [(namespace-template ns-name imports)
                  (n/newline-node "\n")
                  (n/newline-node "\n")]
                 
                 ;; Enum converters
                 (when (seq enums)
                   (concat
                     [(n/comment-node " Enum Converters")
                      (n/newline-node "\n")]
                     (interleave
                       (mapcat enum-converter-template enums)
                       (repeat (n/newline-node "\n")))))
                 
                 ;; Forward declarations for circular dependencies
                 (when (seq messages)
                   [(n/newline-node "\n")
                    (n/comment-node " Forward declarations")
                    (n/newline-node "\n")
                    (n/list-node
                      (concat
                        [(sym-node 'declare)]
                        (interleave
                          (mapcat (fn [msg]
                                    [(sym-node (str "build-" (clojure.core/name (:kebab-name msg))))
                                     (sym-node (str "parse-" (clojure.core/name (:kebab-name msg))))])
                                  messages)
                          (repeat (n/whitespace-node " ")))))
                    (n/newline-node "\n")
                    (n/newline-node "\n")])
                 
                 ;; Message builders and parsers
                 (when (seq messages)
                   (concat
                     [(n/comment-node " Message Converters")
                      (n/newline-node "\n")]
                     (interleave
                       (mapcat (fn [msg]
                                 (concat
                                   [(message-builder-template msg)
                                    (n/newline-node "\n")
                                    (message-parser-template msg)]
                                   (when (seq (:oneofs msg))
                                     [(n/newline-node "\n")
                                      (oneof-builder-template msg)])))
                               messages)
                       (repeat (n/newline-node "\n"))))))]
    ;; Convert nodes to string
    (-> (n/forms-node nodes)
        (n/string)))))