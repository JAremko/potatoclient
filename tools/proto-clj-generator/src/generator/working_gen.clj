(ns generator.working-gen
  "Working code generator with proper oneof handling."
  (:require [clojure.string :as str]))

(defn pascal-case [s]
  (->> (str/split (name s) #"-")
       (map str/capitalize)
       (str/join)))

(defn field->setter [{:keys [kebab-name java-setter]}]
  (str "      (." java-setter " (get m " kebab-name "))"))

(defn generate-builder [{:keys [name kebab-name java-class fields oneofs]}]
  (let [non-oneof-fields (remove #(= (:oneof-index %) 0) fields)
        has-oneof? (seq oneofs)]
    (str "(defn build-" (clojure.core/name kebab-name) "\n"
         "  \"Build a " name " protobuf message from a map.\"\n"
         "  [m]\n"
         "  (let [builder (" java-class "/newBuilder)]\n"
         (when (seq non-oneof-fields)
           (str "    ;; Set regular fields\n"
                (str/join "\n"
                         (map (fn [field]
                                (str "    (when (contains? m " (:kebab-name field) ")\n"
                                     "      (." (:java-setter field) " builder (get m " (:kebab-name field) ")))"))
                              non-oneof-fields))
                "\n"))
         (when has-oneof?
           (str "    ;; Set oneof payload\n"
                "    (when-let [payload (first (filter (fn [[k v]] (#{" 
                (str/join " " (map #(:kebab-name %) (:fields (first oneofs))))
                "} k)) m))]\n"
                "      (build-" (str/lower-case name) "-payload builder payload))\n"))
         "    (.build builder)))\n")))

(defn generate-oneof-builder [{:keys [name oneofs]}]
  (when (seq oneofs)
    (let [oneof-fields (:fields (first oneofs))]
      (str "\n(defn build-" (str/lower-case name) "-payload\n"
           "  \"Set the oneof payload field.\"\n"
           "  [builder [field-key field-value]]\n"
           "  (case field-key\n"
           (str/join "\n"
                    (map (fn [{:keys [kebab-name name]}]
                           (str "    " kebab-name " (." 
                                "set" (pascal-case name) " builder field-value)"))
                         oneof-fields))
           "\n    (throw (ex-info \"Unknown payload field\" {:field field-key}))))\n"))))

(defn generate-parser [{:keys [name kebab-name java-class fields oneofs]}]
  (let [non-oneof-fields (remove #(= (:oneof-index %) 0) fields)
        has-oneof? (seq oneofs)]
    (str "(defn parse-" (clojure.core/name kebab-name) "\n"
         "  \"Parse a " name " protobuf message to a map.\"\n"
         "  [^" java-class " proto]\n"
         "  (merge\n"
         "    {" (str/join "\n     "
                        (map (fn [{:keys [kebab-name java-getter]}]
                               (str kebab-name " (." java-getter " proto)"))
                             non-oneof-fields)) "}\n"
         (when has-oneof?
           (str "    (parse-" (str/lower-case name) "-payload proto)"))
         "))\n")))

(defn generate-oneof-parser [{:keys [name oneofs]}]
  (when (seq oneofs)
    (let [oneof-fields (:fields (first oneofs))]
      (str "\n(defn parse-" (str/lower-case name) "-payload\n"
           "  \"Parse the oneof payload.\"\n"
           "  [proto]\n"
           "  (case (.getPayloadCase proto)\n"
           (str/join "\n"
                    (map (fn [{:keys [kebab-name name]}]
                           (str "    " (str/upper-case name) 
                                " {" kebab-name " (.get" (pascal-case name) " proto)}"))
                         oneof-fields))
           "\n    {}))\n"))))

(defn generate-code [{:keys [ns-name messages enums imports outer-class]}]
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