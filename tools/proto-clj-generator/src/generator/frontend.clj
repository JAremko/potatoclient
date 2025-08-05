(ns generator.frontend
  "Frontend that generates Clojure code from EDN intermediate representation."
  (:require [clojure.string :as str]
            [camel-snake-kebab.core :as csk]))

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
    (let [;; Try multiple lookup strategies
          type-key (csk/->kebab-case-keyword 
                    (last (str/split type-ref #"\.")))
          ;; Also try without leading dot
          clean-ref (str/replace type-ref #"^\." "")
          ;; Try the full reference
          full-key (csk/->kebab-case-keyword clean-ref)]
      (if-let [type-def (or (get type-lookup type-key)
                           (get type-lookup full-key))]
        (str "build-" (name (:name type-def)))
        ;; Fallback to deriving from type reference
        (str "build-" (csk/->kebab-case 
                      (last (str/split type-ref #"\."))))))))

(defn resolve-parser-name
  "Resolve the parser function name for a type reference."
  [type-ref type-lookup]
  (when type-ref
    (let [;; Try multiple lookup strategies
          type-key (csk/->kebab-case-keyword 
                    (last (str/split type-ref #"\.")))
          ;; Also try without leading dot
          clean-ref (str/replace type-ref #"^\." "")
          ;; Try the full reference
          full-key (csk/->kebab-case-keyword clean-ref)]
      (if-let [type-def (or (get type-lookup type-key)
                           (get type-lookup full-key))]
        (str "parse-" (name (:name type-def)))
        ;; Fallback to deriving from type reference
        (str "parse-" (csk/->kebab-case 
                      (last (str/split type-ref #"\."))))))))

;; =============================================================================
;; Builder Generation
;; =============================================================================

(defn generate-field-setter
  "Generate code for setting a single field."
  [field indent]
  (let [spaces (apply str (repeat indent " "))]
    (str spaces "(when (contains? m " (:name field) ")\n"
         spaces "  (." (field-java-setter field) " builder (get m " (:name field) ")))")))

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

(defn generate-builder
  "Generate a builder function for a message."
  [message type-lookup]
  (let [regular-fields (vec (:fields message))
        has-oneof? (seq (:oneofs message))
        oneof-fields (when has-oneof?
                      (mapcat :fields (:oneofs message)))]
    (str "(defn build-" (name (:name message)) "\n"
         "  \"Build a " (:proto-name message) " protobuf message from a map.\"\n"
         "  [m]\n"
         "  (let [builder (" (:java-class message) "/newBuilder)]\n"
         
         ;; Regular fields
         (when (seq regular-fields)
           (str "    ;; Set regular fields\n"
                (str/join "\n"
                         (map #(generate-field-setter % 4) regular-fields))
                "\n"))
         
         ;; Oneof fields
         (when has-oneof?
           (str "    ;; Set oneof payload\n"
                "    (when-let [payload (first (filter (fn [[k v]] (#{" 
                (str/join " " (map :name oneof-fields))
                "} k)) m))]\n"
                "      (build-" (csk/->kebab-case (:proto-name message)) 
                "-payload builder payload))\n"))
         
         "    (.build builder)))\n")))

(defn generate-oneof-builder
  "Generate the oneof payload builder for a message."
  [message type-lookup]
  (when (seq (:oneofs message))
    (let [oneof-fields (mapcat :fields (:oneofs message))]
      (str "\n(defn build-" (csk/->kebab-case (:proto-name message)) "-payload\n"
           "  \"Set the oneof payload field.\"\n"
           "  [builder [field-key field-value]]\n"
           "  (case field-key\n"
           (str/join "\n"
                    (map #(generate-oneof-case % type-lookup) oneof-fields))
           "\n    (throw (ex-info \"Unknown payload field\" {:field field-key}))))\n"))))

;; =============================================================================
;; Parser Generation
;; =============================================================================

(defn generate-field-getter
  "Generate code for getting a single field value."
  [field]
  (str (:name field) " (." (field-java-getter field) " proto)"))

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

(defn generate-parser
  "Generate a parser function for a message."
  [message type-lookup]
  (let [regular-fields (vec (:fields message))
        has-oneof? (seq (:oneofs message))]
    (str "(defn parse-" (name (:name message)) "\n"
         "  \"Parse a " (:proto-name message) " protobuf message to a map.\"\n"
         "  [^" (:java-class message) " proto]\n"
         "  (merge\n"
         "    {" (when (seq regular-fields)
               (str/join "\n     "
                        (map generate-field-getter regular-fields))) 
         "}\n"
         (when has-oneof?
           (str "    (parse-" (csk/->kebab-case (:proto-name message)) 
                "-payload proto)"))
         "))\n")))

(defn generate-oneof-parser
  "Generate the oneof payload parser for a message."
  [message type-lookup]
  (when (seq (:oneofs message))
    (let [oneof-fields (mapcat :fields (:oneofs message))]
      (str "\n(defn parse-" (csk/->kebab-case (:proto-name message)) "-payload\n"
           "  \"Parse the oneof payload.\"\n"
           "  [proto]\n"
           "  (case (.getPayloadCase proto)\n"
           (str/join "\n"
                    (map #(generate-oneof-parser-case % type-lookup) 
                         oneof-fields))
           "\n    {}))\n"))))

;; =============================================================================
;; Import Generation
;; =============================================================================

(defn collect-imports
  "Collect all Java imports needed from the EDN data."
  [edn-data]
  (let [all-types (if (= (:type edn-data) :file)
                    (concat (:messages edn-data) (:enums edn-data))
                    ;; For descriptor sets
                    (mapcat (fn [file]
                             (concat (:messages file) (:enums file)))
                           (:files edn-data)))
        
        ;; Group by package
        by-package (group-by (fn [type-def]
                              (let [java-class (:java-class type-def)
                                    last-dot (.lastIndexOf java-class ".")]
                                (if (pos? last-dot)
                                  (subs java-class 0 last-dot)
                                  "")))
                            all-types)]
    
    ;; Convert to import format
    (vec (for [[pkg types] by-package
               :when (not= pkg "")]
          (into [pkg] 
                (map (fn [type-def]
                      (let [java-class (:java-class type-def)
                            last-dot (.lastIndexOf java-class ".")]
                        (subs java-class (inc last-dot))))
                    types))))))

;; =============================================================================
;; Full Code Generation
;; =============================================================================

(defn generate-declarations
  "Generate forward declarations for all functions."
  [messages]
  (when (seq messages)
    (let [all-fns (mapcat (fn [msg]
                           (concat
                            [(str "build-" (name (:name msg)))
                             (str "parse-" (name (:name msg)))]
                            (when (seq (:oneofs msg))
                              [(str "build-" (csk/->kebab-case (:proto-name msg)) "-payload")
                               (str "parse-" (csk/->kebab-case (:proto-name msg)) "-payload")])))
                         messages)]
      (str "(declare " (str/join " " all-fns) ")\n\n"))))

(defn generate-code
  "Generate complete Clojure code from EDN representation."
  [{:keys [ns-name edn-data type-lookup]}]
  (let [messages (if (= (:type edn-data) :file)
                   (:messages edn-data)
                   ;; For descriptor sets, get messages from all files
                   (mapcat :messages (:files edn-data)))
        imports (collect-imports edn-data)]
    
    (str "(ns " ns-name "\n"
         "  \"Generated protobuf conversion functions.\"\n"
         "  (:require [clojure.string :as str])\n"
         (when (seq imports)
           (str "  (:import\n"
                (str/join "\n"
                         (map (fn [import-group]
                               (str "    [" (str/join " " import-group) "]"))
                             imports))
                ")"))
         ")\n\n"
         
         ";; Forward declarations\n"
         (generate-declarations messages)
         
         ";; Message Converters\n"
         (str/join "\n" 
                  (mapcat (fn [msg]
                           (concat
                            [(generate-builder msg type-lookup)]
                            (when (seq (:oneofs msg))
                              [(generate-oneof-builder msg type-lookup)])
                            [(generate-parser msg type-lookup)]
                            (when (seq (:oneofs msg))
                              [(generate-oneof-parser msg type-lookup)])))
                         messages)))))

(defn generate-from-backend
  "Generate Clojure code from backend EDN output."
  [{:keys [command state type-lookup] :as backend-output} ns-prefix]
  {:command (generate-code {:ns-name (str ns-prefix ".command")
                           :edn-data command
                           :type-lookup type-lookup})
   :state (generate-code {:ns-name (str ns-prefix ".state")
                         :edn-data state
                         :type-lookup type-lookup})})