(ns generator.frontend
  "Frontend for the proto-clj-generator.
  Generates Clojure code from the EDN intermediate representation."
  (:require [clojure.string :as str]
            [camel-snake-kebab.core :as csk]))

;; =============================================================================
;; Helper Functions
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
  "Resolve the builder function name for a type reference.
  Throws if the type cannot be found."
  [type-ref type-lookup]
  (when type-ref
    (let [;; Normalize the type reference to be our canonical key
          canonical-ref (str/replace type-ref #"^\." "")
          type-def (get type-lookup canonical-ref)]
      (if type-def
        (str "build-" (name (:name type-def)))
        (throw (ex-info "Cannot resolve type reference for builder"
                        {:type-ref type-ref
                         :canonical-ref canonical-ref
                         :available-types (keys type-lookup)}))))))

(defn resolve-parser-name
  "Resolve the parser function name for a type reference.
  Throws if the type cannot be found."
  [type-ref type-lookup]
  (when type-ref
    (let [;; Normalize the type reference to be our canonical key
          canonical-ref (str/replace type-ref #"^\." "")
          type-def (get type-lookup canonical-ref)]
      (if type-def
        (str "parse-" (name (:name type-def)))
        (throw (ex-info "Cannot resolve type reference for parser"
                        {:type-ref type-ref
                         :canonical-ref canonical-ref
                         :available-types (keys type-lookup)}))))))

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
  (let [regular-fields (remove :oneof-index (:fields message))
        oneof-fields (filter :oneof-index (:fields message))
        has-oneof? (seq oneof-fields)]
    (str "(defn build-" (csk/->kebab-case (:proto-name message)) "\n"
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
  (let [regular-fields (remove :oneof-index (:fields message))
        oneof-fields (filter :oneof-index (:fields message))
        has-oneof? (seq oneof-fields)]
    (str "(defn parse-" (csk/->kebab-case (:proto-name message)) "\n"
         "  \"Parse a " (:proto-name message) " protobuf message to a map.\"\n"
         "  [^" (:java-class message) " proto]\n"
         "  (cond-> {}\n"
         
         ;; Regular fields
         (when (seq regular-fields)
           (str "    ;; Regular fields\n"
                (str/join "\n"
                         (map (fn [field]
                               (str "    (." (str "has" (csk/->PascalCase (:proto-name field)))
                                    " proto) (assoc " (generate-field-getter field) ")"))
                             regular-fields))
                "\n"))
         
         ;; Oneof fields
         (when has-oneof?
           (let [oneof (first (:oneofs message))]
             (str "    ;; Oneof payload\n"
                  "    true (merge (parse-" (csk/->kebab-case (:proto-name message))
                  "-payload proto))))\n")))
         
         (when-not has-oneof?
           "))\n"))))

(defn generate-oneof-parser
  "Generate the oneof payload parser for a message."
  [message type-lookup]
  (when (seq (:oneofs message))
    (let [oneof (first (:oneofs message))
          oneof-fields (:fields oneof)]
      (str "\n(defn parse-" (csk/->kebab-case (:proto-name message)) "-payload\n"
           "  \"Parse the oneof payload from a " (:proto-name message) ".\"\n"
           "  [^" (:java-class message) " proto]\n"
           "  (case (.get" (csk/->PascalCase (:proto-name oneof)) "Case proto)\n"
           (str/join "\n"
                    (map #(generate-oneof-parser-case % type-lookup) oneof-fields))
           "\n    ;; Default case - no payload set\n"
           "    {}))\n"))))

;; =============================================================================
;; Enum Generation
;; =============================================================================

(defn generate-enum-value-map
  "Generate the enum value mapping."
  [enum]
  (str "{" (str/join "\n   "
                    (map (fn [value]
                          (str (:name value) " " (:java-class enum) "/" 
                               (:proto-name value)))
                        (:values enum)))
       "}"))

(defn generate-enum-reverse-map
  "Generate the reverse enum mapping (Java -> keyword)."
  [enum]
  (str "{" (str/join "\n   "
                    (map (fn [value]
                          (str (:java-class enum) "/" (:proto-name value)
                               " " (:name value)))
                        (:values enum)))
       "}"))

(defn generate-enum
  "Generate enum mappings."
  [enum]
  (str ";; Enum: " (:proto-name enum) "\n"
       "(def " (csk/->kebab-case (:proto-name enum)) "-values\n"
       "  \"Keyword to Java enum mapping for " (:proto-name enum) ".\"\n"
       "  " (generate-enum-value-map enum) ")\n\n"
       "(def " (csk/->kebab-case (:proto-name enum)) "-keywords\n"
       "  \"Java enum to keyword mapping for " (:proto-name enum) ".\"\n"
       "  " (generate-enum-reverse-map enum) ")\n"))

;; =============================================================================
;; Import Generation
;; =============================================================================

(defn collect-imports
  "Collect all Java imports needed for the generated code."
  [edn-data]
  (let [all-types (concat
                   (mapcat (fn [file]
                            (concat (:messages file) (:enums file)))
                          (:files edn-data)))]
    (->> all-types
         (map :java-class)
         (filter identity)
         ;; Filter out internal protobuf packages
         (remove #(or (str/starts-with? % "com.google.protobuf.")
                     (str/starts-with? % "build.buf.validate.")))
         distinct
         sort)))

;; =============================================================================
;; Main Code Generation
;; =============================================================================

(defn generate-code
  "Generate complete Clojure code from EDN data."
  [{:keys [ns-name edn-data type-lookup]}]
  (let [imports (collect-imports edn-data)
        messages (mapcat :messages (:files edn-data))
        enums (mapcat :enums (:files edn-data))]
    (str "(ns " ns-name "\n"
         "  \"Generated protobuf functions.\"\n"
         "  (:import\n"
         (str/join "\n"
                  (map #(str "   [" % "]") imports))
         "))\n\n"
         
         ";; =============================================================================\n"
         ";; Enums\n"
         ";; =============================================================================\n\n"
         (str/join "\n" (map generate-enum enums))
         
         "\n;; =============================================================================\n"
         ";; Builders and Parsers\n"
         ";; =============================================================================\n\n"
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