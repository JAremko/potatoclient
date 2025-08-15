(ns potatoclient.proto.to-edn
  "Convert protobuf messages to EDN using reflection.
   Based on state-explorer's approach for reliable proto-to-EDN conversion."
  (:require [clojure.string :as str])
  (:import [com.google.protobuf Descriptors$FieldDescriptor]))

(declare proto-message->map)

(defn field-name->keyword
  "Convert protobuf field name to Clojure keyword.
   Keeps the field name exactly as-is from the proto definition."
  [field-name]
  (keyword field-name))

(defn get-field-value
  "Extract value from a protobuf field"
  [message field]
  (let [value (.getField message field)]
    (cond
      ;; Handle nested messages
      (instance? com.google.protobuf.Message value)
      (proto-message->map value)
      
      ;; Handle enums - keep UPPER_SNAKE_CASE format
      (instance? com.google.protobuf.Descriptors$EnumValueDescriptor value)
      (keyword (.getName value))
      
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
            fields)))