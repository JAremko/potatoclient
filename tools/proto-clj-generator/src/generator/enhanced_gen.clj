(ns generator.enhanced-gen
  "Enhanced code generation with support for all field types."
  (:require [clojure.string :as str]
            [taoensso.timbre :as log]))

;; =============================================================================
;; Helper Functions
;; =============================================================================

(defn pascal-case
  [s]
  (->> (str/split (name s) #"-")
       (map str/capitalize)
       (str/join)))

(defn camel-case
  [s]
  (let [parts (str/split (name s) #"-")]
    (str (first parts)
         (str/join (map str/capitalize (rest parts))))))

(defn proto-enum-name
  "Convert kebab-case to PROTO_ENUM format."
  [k]
  (-> (name k)
      str/upper-case
      (str/replace #"-" "_")))

(defn type-name->builder-fn
  "Extract builder function name from type name."
  [type-name]
  (when type-name
    (str "build-" 
         (-> type-name
             (str/split #"\.")
             last
             (str/replace #"([a-z])([A-Z])" "$1-$2")
             str/lower-case))))

(defn type-name->parser-fn
  "Extract parser function name from type name."
  [type-name]
  (when type-name
    (str "parse-" 
         (-> type-name
             (str/split #"\.")
             last
             (str/replace #"([a-z])([A-Z])" "$1-$2")
             str/lower-case))))

;; =============================================================================
;; Field Setters
;; =============================================================================

(defn generate-field-setter
  [{:keys [kebab-name type java-setter type-name repeated?] :as field}]
  (let [getter (str "(get m " kebab-name ")")]
    (cond
      ;; Repeated fields
      repeated?
      (let [add-all-method (str ".addAll" (pascal-case (:name field)))]
        (case type
          :message (str "      (->> " getter "\n"
                       "           (map " (type-name->builder-fn type-name) ")\n"
                       "           (" add-all-method "))")
          :enum (str "      (->> " getter "\n"
                    "           (map #(" (last (str/split type-name #"\.")) "/valueOf (proto-enum-name %)))\n"
                    "           (" add-all-method "))")
          (str "      (" add-all-method " " getter ")")))
      
      ;; Oneof fields belong to payload
      (and (:oneof-index field) (= (:oneof-index field) 0))
      (str "      ;; Oneof field - handled separately")
      
      ;; Regular fields
      :else
      (case type
        :message (str "      (cond-> (contains? m " kebab-name ")\n"
                     "        (." java-setter " (" (type-name->builder-fn type-name) " " getter ")))")
        :enum (str "      (cond-> (contains? m " kebab-name ")\n"
                  "        (." java-setter " (" (last (str/split type-name #"\.")) "/valueOf (proto-enum-name " getter "))))")
        :boolean (str "      (." java-setter " (boolean " getter "))")
        (str "      (." java-setter " " getter ")")))))

;; =============================================================================
;; Field Getters
;; =============================================================================

(defn generate-field-getter
  [{:keys [kebab-name type java-getter java-has type-name repeated? optional?] :as field}]
  (let [proto-getter (str "(." java-getter " proto)")]
    (cond
      ;; Repeated fields
      repeated?
      (case type
        :message (str "    " kebab-name " (mapv " (type-name->parser-fn type-name) " (.get" (pascal-case (:name field)) "List proto))")
        :enum (str "    " kebab-name " (mapv #(-> (.name %) str/lower-case (str/replace #\"_\" \"-\") keyword) (.get" (pascal-case (:name field)) "List proto))")
        (str "    " kebab-name " (vec (.get" (pascal-case (:name field)) "List proto))"))
      
      ;; Oneof fields
      (and (:oneof-index field) (= (:oneof-index field) 0))
      nil  ;; Handled separately in oneof parsing
      
      ;; Optional fields
      optional?
      (case type
        :message (str "    (when (." java-has " proto)\n"
                     "      (assoc " kebab-name " (" (type-name->parser-fn type-name) " " proto-getter ")))")
        :enum (str "    (when (." java-has " proto)\n"
                  "      (assoc " kebab-name " (-> " proto-getter " .name str/lower-case (str/replace #\"_\" \"-\") keyword)))")
        (str "    (when (." java-has " proto)\n"
            "      (assoc " kebab-name " " proto-getter "))"))
      
      ;; Required fields
      :else
      (case type
        :message (str "    " kebab-name " (" (type-name->parser-fn type-name) " " proto-getter ")")
        :enum (str "    " kebab-name " (-> " proto-getter " .name str/lower-case (str/replace #\"_\" \"-\") keyword)")
        (str "    " kebab-name " " proto-getter)))))

;; =============================================================================
;; Oneof Handling
;; =============================================================================

(defn generate-oneof-builder
  [{:keys [name java-class oneofs]}]
  (when (seq oneofs)
    (let [oneof (first oneofs)  ;; Assuming one oneof per message
          oneof-fields (:fields oneof)]
      (str "\n(defn build-" (str/lower-case name) "-payload\n"
           "  \"Build the oneof payload for " name ".\"\n"
           "  [builder [field-key field-value]]\n"
           "  (case field-key\n"
           (str/join "\n"
                    (map (fn [{:keys [kebab-name name type type-name]}]
                           (str "    " kebab-name " "
                                (if (= type :message)
                                  (str "(.set" (pascal-case name) " builder (" (type-name->builder-fn type-name) " field-value))")
                                  (str "(.set" (pascal-case name) " builder field-value)"))))
                         oneof-fields))
           "\n    (throw (ex-info \"Unknown payload field\" {:field field-key}))))\n"))))

(defn generate-oneof-parser
  [{:keys [name java-class oneofs]}]
  (when (seq oneofs)
    (let [oneof (first oneofs)
          oneof-fields (:fields oneof)]
      (str "\n(defn parse-" (str/lower-case name) "-payload\n"
           "  \"Parse the oneof payload from " name ".\"\n"
           "  [proto]\n"
           "  (case (.getPayloadCase proto)\n"
           (str/join "\n"
                    (map (fn [{:keys [kebab-name name type type-name]}]
                           (let [enum-val (proto-enum-name name)
                                 getter (str ".get" (pascal-case name))]
                             (str "    " enum-val " {" kebab-name " "
                                  (if (= type :message)
                                    (str "(" (type-name->parser-fn type-name) " (" getter " proto))")
                                    (str "(" getter " proto)"))
                                  "}"))
                         oneof-fields))
           "\n    nil))\n"))))

;; =============================================================================
;; Message Generation
;; =============================================================================

(defn generate-builder
  [{:keys [name kebab-name java-class fields oneofs]}]
  (let [non-oneof-fields (remove #(= (:oneof-index %) 0) fields)
        has-oneof? (seq oneofs)]
    (str "(defn build-" (clojure.core/name kebab-name) "\n"
         "  \"Build a " name " protobuf message from a map.\"\n"
         "  [m]\n"
         "  (cond-> (" java-class "/newBuilder)\n"
         (str/join "\n" (map generate-field-setter non-oneof-fields))
         (when has-oneof?
           (str "\n    ;; Handle oneof payload\n"
                "    (some? (first m))\n"
                "    (build-" (str/lower-case name) "-payload (first m))"))
         "\n    true (.build)))\n")))

(defn generate-parser
  [{:keys [name kebab-name java-class fields oneofs]}]
  (let [non-oneof-fields (remove #(= (:oneof-index %) 0) fields)
        required-fields (remove :optional? non-oneof-fields)
        optional-fields (filter :optional? non-oneof-fields)
        has-oneof? (seq oneofs)]
    (str "(defn parse-" (clojure.core/name kebab-name) "\n"
         "  \"Parse a " name " protobuf message to a map.\"\n"
         "  [^" java-class " proto]\n"
         "  (merge\n"
         ;; Required fields
         (when (seq required-fields)
           (str "    {" (str/join "\n     " (keep generate-field-getter required-fields)) "}"))
         ;; Optional fields
         (when (seq optional-fields)
           (str "\n    (cond-> {}\n"
                (str/join "\n" (keep generate-field-getter optional-fields)) ")"))
         ;; Oneof payload
         (when has-oneof?
           (str "\n    (parse-" (str/lower-case name) "-payload proto)"))
         "))\n")))

;; =============================================================================
;; Enum Generation
;; =============================================================================

(defn generate-enum-converters
  [{:keys [name kebab-name java-class values]}]
  (str ";; Enum converters for " name "\n"
       "(defn " (clojure.core/name kebab-name) "->proto\n"
       "  \"Convert " name " keyword to protobuf enum.\"\n"
       "  [k]\n"
       "  (case k\n"
       (str/join "\n"
                (map (fn [{:keys [kebab-name name]}]
                       (str "    " kebab-name " " java-class "/" name))
                     values))
       "\n    (throw (ex-info \"Unknown " name " value\" {:value k}))))\n\n"
       
       "(defn proto->" (clojure.core/name kebab-name) "\n"
       "  \"Convert protobuf " name " to keyword.\"\n"
       "  [e]\n"
       "  (-> (.name e)\n"
       "      str/lower-case\n"
       "      (str/replace #\"_\" \"-\")\n"
       "      keyword))\n"))

;; =============================================================================
;; Main Code Generation
;; =============================================================================

(defn generate-code
  [{:keys [ns-name messages enums imports]}]
  (str "(ns " ns-name "\n"
       "  \"Generated protobuf conversion functions.\"\n"
       "  (:require [clojure.string :as str]\n"
       "            [clojure.core.match :refer [match]]\n"
       "            [malli.core :as m]\n"
       "            [potatoclient.specs.malli-oneof :as oneof])\n"
       (when (seq imports)
         (str "  (:import\n"
              (str/join "\n"
                       (map (fn [import-group]
                              (str "    [" (str/join " " import-group) "]"))
                            imports))
              ")"))
       ")\n\n"
       
       ";; Forward declarations\n"
       (when (seq messages)
         (let [all-fns (mapcat (fn [msg]
                                (concat
                                  [(str "build-" (clojure.core/name (:kebab-name msg)))
                                   (str "parse-" (clojure.core/name (:kebab-name msg)))]
                                  (when (seq (:oneofs msg))
                                    [(str "build-" (str/lower-case (:name msg)) "-payload")
                                     (str "parse-" (str/lower-case (:name msg)) "-payload")])))
                              messages)]
           (str "(declare " (str/join " " all-fns) ")\n\n")))
       
       ";; Helper function\n"
       "(defn proto-enum-name\n"
       "  \"Convert kebab-case keyword to PROTO_ENUM format.\"\n"
       "  [k]\n"
       "  (-> (name k)\n"
       "      str/upper-case\n"
       "      (str/replace #\"-\" \"_\")))\n\n"
       
       (when (seq enums)
         (str ";; Enum Converters\n"
              (str/join "\n" (map generate-enum-converters enums))
              "\n"))
       
       ";; Message Converters\n"
       (str/join "\n" 
                (mapcat (fn [msg]
                         (concat
                           [(generate-builder msg)]
                           (when (seq (:oneofs msg))
                             [(generate-oneof-builder msg)])
                           [(generate-parser msg)]
                           (when (seq (:oneofs msg))
                             [(generate-oneof-parser msg)])))
                       messages))))