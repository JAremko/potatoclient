(ns potatoclient.proto
  "Protocol buffer serialization for communication with the server.
  
  Provides functions to serialize commands and deserialize state messages
  using the pronto library for Clojure protobuf support."
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [com.fulcrologic.guardrails.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [potatoclient.specs :as specs])
  (:import (cmd JonSharedCmd$Root)
           (data JonSharedData$JonGUIState)))

;; Client type constants for better readability
(def client-types
  {:internal-cv "JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV"
   :local "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
   :certificate "JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED"
   :lira "JON_GUI_DATA_CLIENT_TYPE_LIRA"})

;; Client type values schema
(def client-type-values
  [:enum "JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV"
   "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
   "JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED"
   "JON_GUI_DATA_CLIENT_TYPE_LIRA"])

;; Command schema with proper client type
(def command-schema
  [:map
   [:protocol-version ::specs/protocol-version]
   [:session-id ::specs/session-id]
   [:important ::specs/important]
   [:from-cv-subsystem ::specs/from-cv-subsystem]
   [:client-type client-type-values]
   [:payload ::specs/payload]])

;; Utility functions for case conversion
(>defn- camel->kebab
  "Convert camelCase or PascalCase to kebab-case."
  [s]
  [string? => string?]
  (-> s
      (str/replace #"([a-z])([A-Z])" "$1-$2")
      (str/replace #"([A-Z]+)([A-Z][a-z])" "$1-$2")
      str/lower-case))

(>defn- kebab->camel
  "Convert kebab-case to camelCase."
  [s]
  [string? => string?]
  (let [parts (str/split s #"-")]
    (str (first parts)
         (apply str (map str/capitalize (rest parts))))))

;; Protobuf message conversion functions
(>defn- proto-map->clj-map
  "Convert a protobuf message to a Clojure map with kebab-case keys.
  Recursively processes nested messages."
  [proto-msg]
  [any? => map?]
  (let [descriptor (.getDescriptorForType proto-msg)
        fields (.getFields descriptor)]
    (reduce
     (fn [acc field]
       (let [field-name (.getName field)
             kebab-name (camel->kebab field-name)
             value (.getField proto-msg field)]
         (cond
           ;; Skip unset fields
           (and (not (.isRepeated field))
                (= value (.getDefaultValue field)))
           acc
           
           ;; Handle repeated fields
           (.isRepeated field)
           (if (seq value)
             (assoc acc (keyword kebab-name)
                    (mapv #(if (instance? com.google.protobuf.Message %)
                             (proto-map->clj-map %)
                             %)
                          value))
             acc)
           
           ;; Handle message types
           (instance? com.google.protobuf.Message value)
           (if (.hasField proto-msg field)
             (assoc acc (keyword kebab-name) (proto-map->clj-map value))
             acc)
           
           ;; Handle enum types
           (= (.getJavaType field) com.google.protobuf.Descriptors$FieldDescriptor$JavaType/ENUM)
           (assoc acc (keyword kebab-name) (.getName value))
           
           ;; Handle primitive types
           :else
           (assoc acc (keyword kebab-name) value))))
     {}
     fields)))

(>defn- clj-map->proto-builder
  "Convert a Clojure map to a protobuf builder.
  Handles nested messages and repeated fields."
  [builder clj-map]
  [any? map? => any?]
  (let [descriptor (.getDescriptorForType builder)]
    (doseq [[k v] clj-map]
      (let [field-name (kebab->camel (name k))
            field (.findFieldByName descriptor field-name)]
        (when field
          (cond
            ;; Handle repeated fields
            (.isRepeated field)
            (doseq [item v]
              (if (= (.getJavaType field) com.google.protobuf.Descriptors$FieldDescriptor$JavaType/MESSAGE)
                (let [item-builder (.newBuilderForField builder field)]
                  (clj-map->proto-builder item-builder item)
                  (.addRepeatedField builder field (.build item-builder)))
                (.addRepeatedField builder field item)))
            
            ;; Handle message types
            (= (.getJavaType field) com.google.protobuf.Descriptors$FieldDescriptor$JavaType/MESSAGE)
            (let [nested-builder (.newBuilderForField builder field)]
              (clj-map->proto-builder nested-builder v)
              (.setField builder field (.build nested-builder)))
            
            ;; Handle enum types
            (= (.getJavaType field) com.google.protobuf.Descriptors$FieldDescriptor$JavaType/ENUM)
            (let [enum-type (.getEnumType field)
                  enum-value (.findValueByName enum-type v)]
              (when enum-value
                (.setField builder field enum-value)))
            
            ;; Handle primitive types
            :else
            (.setField builder field v)))))
    builder))

;; Serialization functions
(>defn serialize-cmd
  "Serialize a Clojure command map to protobuf bytes.
  
  The command should have the structure:
  {:protocol-version 1
   :session-id 12345  
   :important true
   :from-cv-subsystem false
   :client-type \"JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK\"
   :payload {:ping {}}}
   
  Returns the serialized bytes or throws an exception with details."
  [cmd-map]
  [:potatoclient.specs/command => bytes?]
  (try
    (let [builder (JonSharedCmd$Root/newBuilder)]
      (clj-map->proto-builder builder cmd-map)
      (.toByteArray (.build builder)))
    (catch Exception e
      (throw (ex-info "Failed to serialize command"
                      {:error (.getMessage e)
                       :command cmd-map
                       :cause e})))))

(>defn deserialize-state
  "Deserialize protobuf bytes to a Clojure state map.
  
  Returns a map with kebab-case keys representing the current state.
  Throws an exception if deserialization fails."
  [proto-bytes]
  [bytes? => map?]
  (try
    (let [proto-msg (JonSharedData$JonGUIState/parseFrom proto-bytes)]
      (proto-map->clj-map proto-msg))
    (catch Exception e
      (throw (ex-info "Failed to deserialize state"
                      {:error (.getMessage e)
                       :bytes-length (count proto-bytes)
                       :cause e})))))

;; Command factory functions
(>defn- create-command
  "Create a base command structure."
  [session-id client-type-key & {:keys [important? from-cv?]
                                 :or {important? false
                                      from-cv? false}}]
  [::specs/session-id ::specs/client-type-key (? (s/* any?)) => map?]
  {:protocol-version 1
   :session-id session-id
   :important important?
   :from-cv-subsystem from-cv?
   :client-type (get client-types client-type-key)})

(>defn cmd-ping
  "Create a ping command for heartbeat/keepalive."
  [session-id client-type-key]
  [pos-int? [:enum :internal-cv :local :certificate :lira] => :potatoclient.specs/command]
  (assoc (create-command session-id client-type-key)
         :payload {:ping {}}))

(>defn cmd-noop
  "Create a no-operation command."
  [session-id client-type-key]
  [pos-int? [:enum :internal-cv :local :certificate :lira] => :potatoclient.specs/command]
  (assoc (create-command session-id client-type-key)
         :payload {:noop {}}))

(>defn cmd-frozen
  "Create a frozen command (marks important state)."
  [session-id client-type-key]
  [pos-int? [:enum :internal-cv :local :certificate :lira] => :potatoclient.specs/command]
  (assoc (create-command session-id client-type-key :important? true)
         :payload {:frozen {}}))

;; State accessor functions with nil-safety
(>defn get-system-info
  "Extract system information from state."
  [state]
  [map? => (? map?)]
  (get state :system))

(>defn get-camera-day
  "Extract day camera information from state."
  [state]
  [map? => (? map?)]
  (get state :camera-day))

(>defn get-camera-heat
  "Extract heat camera information from state."
  [state]
  [map? => (? map?)]
  (get state :camera-heat))

(>defn get-gps-info
  "Extract GPS information from state."
  [state]
  [map? => (? map?)]
  (get state :gps))

(>defn get-compass-info
  "Extract compass information from state."
  [state]
  [map? => (? map?)]
  (get state :compass))

(>defn get-lrf-info
  "Extract laser range finder information from state."
  [state]
  [map? => (? map?)]
  (get state :lrf))

(>defn get-time-info
  "Extract time information from state."
  [state]
  [map? => (? map?)]
  (get state :time))

;; Higher-level state queries
(>defn get-location
  "Extract location data (GPS + compass) from state."
  [state]
  [map? => (? [:map
               [:latitude number?]
               [:longitude number?]
               [:altitude number?]
               [:heading {:optional true} number?]
               [:timestamp {:optional true} [:or string? inst?]]])]
  (when-let [gps (get-gps-info state)]
    {:latitude (:latitude gps)
     :longitude (:longitude gps)
     :altitude (:altitude gps)
     :heading (get-in state [:compass :heading])
     :timestamp (get-in state [:time :timestamp])}))

(>defn cameras-available?
  "Check if camera data is available in state."
  [state]
  [map? => boolean?]
  (or (some? (get-camera-day state))
      (some? (get-camera-heat state))))

;; Validation functions
(>defn valid-command?
  "Check if a command map is valid for serialization."
  [cmd-map]
  [map? => boolean?]
  (m/validate command-schema cmd-map))

(>defn explain-invalid-command
  "Get explanation for why a command is invalid."
  [cmd-map]
  [map? => string?]
  (str (m/explain command-schema cmd-map)))