(ns proto-explorer.pronto-integration
  "Integration with Pronto for generating EDN representations of protobuf messages"
  (:require [pronto.core :as pronto]
            [pronto.schema :as pronto-schema]
            [clojure.pprint :as pprint]
            [proto-explorer.java-class-info :as java-info]
            [clojure.string :as str]))

(defn class-name->proto-map
  "Convert a protobuf class to a pronto proto-map with default values"
  [class-name]
  (try
    (let [clazz (Class/forName class-name)
          ;; Create a dynamic mapper for this class
          mapper-sym (gensym "mapper")
          _ (eval `(pronto/defmapper ~mapper-sym [~(symbol (.getName clazz))]))
          mapper (eval mapper-sym)
          ;; Create proto-map
          proto-map (pronto/proto-map mapper clazz)]
      {:success true
       :proto-map proto-map
       :mapper mapper})
    (catch Exception e
      {:success false
       :error (.getMessage e)
       :class-name class-name})))

(defn proto-map->edn
  "Convert a pronto proto-map to regular EDN"
  [proto-map]
  (pronto/proto-map->clj-map proto-map))

(defn get-message-edn
  "Get the EDN representation of a protobuf message class"
  [class-name]
  (let [result (class-name->proto-map class-name)]
    (if (:success result)
      {:success true
       :class-name class-name
       :edn (proto-map->edn (:proto-map result))}
      result)))

(defn find-and-get-edn
  "Find a message class by name and return its EDN representation"
  [message-name]
  (let [class-info (java-info/find-message-class message-name)]
    (if (:error class-info)
      {:error (:message class-info)
       :message-name message-name}
      (get-message-edn (get-in class-info [:class :name])))))

(defn format-edn-output
  "Format EDN data for display"
  [edn-data]
  (with-out-str
    (pprint/pprint edn-data)))

(defn get-pronto-info
  "Get comprehensive Pronto information about a message"
  [message-name]
  (let [edn-result (find-and-get-edn message-name)]
    (if (:error edn-result)
      edn-result
      (let [edn-data (:edn edn-result)]
        {:success true
         :message-name message-name
         :class-name (:class-name edn-result)
         :edn-structure edn-data
         :field-count (count (filter (fn [[k v]] (keyword? k)) edn-data))
         :fields (keys edn-data)
         :default-values (into {} (map (fn [[k v]] [k (if (nil? v) :nil v)]) edn-data))}))))

(defn get-nested-message-edn
  "Get EDN for nested messages within a parent message"
  [parent-class-name]
  (try
    (let [parent-class (Class/forName parent-class-name)
          nested-classes (.getDeclaredClasses parent-class)
          message-classes (filter (fn [c]
                                   (and (.isAssignableFrom com.google.protobuf.Message c)
                                        (not (.isInterface c))
                                        (not (str/includes? (.getSimpleName c) "Builder"))
                                        (not (str/includes? (.getSimpleName c) "OrBuilder"))))
                                 nested-classes)]
      {:success true
       :parent parent-class-name
       :nested-messages (mapv (fn [c]
                               (let [class-name (.getName c)]
                                 {:name (.getSimpleName c)
                                  :full-name class-name
                                  :edn (get-message-edn class-name)}))
                             message-classes)})
    (catch Exception e
      {:success false
       :error (.getMessage e)
       :parent parent-class-name})))

(defn get-pronto-schema
  "Get the pronto schema for a protobuf message class"
  [class-name]
  (try
    (let [clazz (Class/forName class-name)
          schema (pronto-schema/schema clazz)]
      {:success true
       :class-name class-name
       :schema schema})
    (catch Exception e
      {:success false
       :error (.getMessage e)
       :class-name class-name})))

(defn get-pronto-schema-for-field
  "Get the pronto schema for a specific field of a protobuf message"
  [class-name field-key]
  (try
    (let [clazz (Class/forName class-name)
          schema (pronto-schema/schema clazz field-key)]
      {:success true
       :class-name class-name
       :field field-key
       :schema schema})
    (catch Exception e
      {:success false
       :error (.getMessage e)
       :class-name class-name
       :field field-key})))

(defn find-and-get-schema
  "Find a message class by name and return its pronto schema"
  [message-name]
  (let [class-info (java-info/find-message-class message-name)]
    (if (:error class-info)
      {:error (:message class-info)
       :message-name message-name}
      (get-pronto-schema (get-in class-info [:class :name])))))

(defn get-comprehensive-pronto-info
  "Get both EDN representation and pronto schema for a message"
  [message-name]
  (let [edn-result (find-and-get-edn message-name)
        schema-result (find-and-get-schema message-name)]
    (if (or (:error edn-result) (:error schema-result))
      {:error (or (:error edn-result) (:error schema-result))
       :message-name message-name}
      {:success true
       :message-name message-name
       :class-name (:class-name edn-result)
       :edn-structure (:edn edn-result)
       :pronto-schema (:schema schema-result)
       :field-count (count (filter (fn [[k v]] (keyword? k)) (:edn edn-result)))
       :fields (keys (:edn edn-result))})))

(defn get-pronto-info-by-class
  "Get comprehensive Pronto information about a message using full class name"
  [class-name]
  (let [edn-result (get-message-edn class-name)]
    (if (:error edn-result)
      edn-result
      (let [edn-data (:edn edn-result)]
        {:success true
         :class-name class-name
         :edn-structure edn-data
         :field-count (count (filter (fn [[k v]] (keyword? k)) edn-data))
         :fields (keys edn-data)
         :default-values (into {} (map (fn [[k v]] [k (if (nil? v) :nil v)]) edn-data))}))))

(defn get-schema-by-class
  "Get the pronto schema for a protobuf message using full class name"
  [class-name]
  (get-pronto-schema class-name))

(comment
  ;; Test with cmd.JonSharedCmd$Root
  (find-and-get-edn "Root")
  (get-pronto-info "Root")
  (get-nested-message-edn "cmd.JonSharedCmd")
  
  ;; Test with ser.JonSharedData$JonGUIState
  (find-and-get-edn "JonGUIState")
  (get-message-edn "ser.JonSharedData$JonGUIState")
  )