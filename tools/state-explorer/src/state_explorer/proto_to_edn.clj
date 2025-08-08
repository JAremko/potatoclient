(ns state-explorer.proto-to-edn
  "Convert protobuf messages to EDN using reflection"
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:import [com.google.protobuf Descriptors$FieldDescriptor]
           [ser JonSharedData$JonGUIState]))

(declare proto-message->map)

(defn field-name->keyword
  "Convert protobuf field name to Clojure keyword"
  [field-name]
  (-> field-name
      (str/replace #"_" "-")
      (str/lower-case)
      keyword))

(defn get-field-value
  "Extract value from a protobuf field"
  [message field]
  (let [value (.getField message field)]
    (cond
      ;; Handle nested messages
      (instance? com.google.protobuf.Message value)
      (proto-message->map value)
      
      ;; Handle enums
      (instance? com.google.protobuf.Descriptors$EnumValueDescriptor value)
      (-> (.getName value)
          (str/lower-case)
          (str/replace #"_" "-")
          keyword)
      
      ;; Handle repeated fields (lists)
      (instance? java.util.List value)
      (mapv #(if (instance? com.google.protobuf.Message %)
               (proto-message->map %)
               %)
            value)
      
      ;; Handle byte strings
      (instance? com.google.protobuf.ByteString value)
      (.toByteArray value)
      
      ;; Return primitive values as-is
      :else value)))

(defn proto-message->map
  "Convert a protobuf message to a Clojure map using reflection"
  [message]
  (try
    (let [descriptor (.getDescriptorForType message)
          fields (.getFields descriptor)
          all-fields (.getAllFields message)]
      
      ;; Build map from all set fields
      (reduce (fn [m field]
                (if (.containsKey all-fields field)
                  (let [field-name (field-name->keyword (.getName field))
                        field-value (get-field-value message field)]
                    (assoc m field-name field-value))
                  m))
              {}
              fields))
    (catch Exception e
      (log/error e "Failed to convert protobuf to map")
      {:error "conversion-failed"
       :message (.getMessage e)})))

(defn parse-and-convert
  "Parse binary protobuf and convert to EDN"
  [binary-data]
  (try
    (let [message (JonSharedData$JonGUIState/parseFrom binary-data)]
      (proto-message->map message))
    (catch Exception e
      (log/error e "Failed to parse protobuf")
      {:error "parse-failed"
       :message (.getMessage e)})))