(ns potatoclient.proto
  "Protocol buffer serialization for communication with the server.
  
  Provides functions to serialize commands and deserialize state messages
  using the pronto library for Clojure protobuf support."
  (:require [pronto.core :as p]
            [pronto.utils :as u]
            [clojure.spec.alpha :as s]
            [orchestra.core :refer [defn-spec]]
            [orchestra.spec.test :as st])
  (:import [ser JonSharedData$JonGUIState]
           [cmd JonSharedCmd$Root]))

;; Client type constants for better readability
(def client-types
  {:internal-cv  "JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV"
   :local        "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
   :certificate  "JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED"
   :lira         "JON_GUI_DATA_CLIENT_TYPE_LIRA"})

;; Command payload types
(def command-types #{:ping :noop :frozen})

;; Specs for validation
(s/def ::protocol-version pos-int?)
(s/def ::session-id pos-int?)
(s/def ::important boolean?)
(s/def ::from-cv-subsystem boolean?)
(s/def ::client-type (set (vals client-types)))
(s/def ::payload-type command-types)
(s/def ::payload (s/map-of keyword? any?))

(s/def ::command
  (s/keys :req-un [::protocol-version ::session-id ::important 
                   ::from-cv-subsystem ::client-type ::payload]))

;; Define the mapper for state and command messages
(p/defmapper proto-mapper [JonSharedData$JonGUIState JonSharedCmd$Root]
  :key-name-fn u/->kebab-case)

;; Serialization functions
(defn-spec serialize-cmd bytes?
  "Serialize a Clojure command map to protobuf bytes.
  
  The command should have the structure:
  {:protocol-version 1
   :session-id 12345  
   :important true
   :from-cv-subsystem false
   :client-type \"JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK\"
   :payload {:ping {}}}
   
  Returns the serialized bytes or throws an exception with details."
  [cmd-map ::command]
  (try
    (-> (p/clj-map->proto-map proto-mapper JonSharedCmd$Root cmd-map)
        (p/proto-map->bytes))
    (catch Exception e
      (throw (ex-info "Failed to serialize command" 
                      {:error (.getMessage e)
                       :command cmd-map
                       :cause e})))))

(defn-spec deserialize-state map?
  "Deserialize protobuf bytes to a Clojure state map.
  
  Returns a map with kebab-case keys representing the current state.
  Throws an exception if deserialization fails."
  [proto-bytes bytes?]
  {:pre [(pos? (count proto-bytes))]}
  (try
    (p/bytes->proto-map proto-mapper JonSharedData$JonGUIState proto-bytes)
    (catch Exception e
      (throw (ex-info "Failed to deserialize state" 
                      {:error (.getMessage e)
                       :bytes-length (count proto-bytes)
                       :cause e})))))

;; Command factory functions
(defn- create-command
  "Create a base command structure."
  [session-id client-type-key & {:keys [important? from-cv?]
                                 :or {important? false
                                      from-cv? false}}]
  {:pre [(pos-int? session-id)
         (contains? client-types client-type-key)]}
  {:protocol-version 1
   :session-id session-id
   :important important?
   :from-cv-subsystem from-cv?
   :client-type (get client-types client-type-key)})

(defn-spec cmd-ping ::command
  "Create a ping command for heartbeat/keepalive."
  [session-id pos-int?
   client-type-key keyword?]
  {:pre [(contains? client-types client-type-key)]}
  (assoc (create-command session-id client-type-key)
         :payload {:ping {}}))

(defn-spec cmd-noop ::command
  "Create a no-operation command."
  [session-id pos-int?
   client-type-key keyword?]
  {:pre [(contains? client-types client-type-key)]}
  (assoc (create-command session-id client-type-key)
         :payload {:noop {}}))

(defn-spec cmd-frozen ::command
  "Create a frozen command (marks important state)."
  [session-id pos-int?
   client-type-key keyword?]
  {:pre [(contains? client-types client-type-key)]}
  (assoc (create-command session-id client-type-key :important? true)
         :payload {:frozen {}}))

;; State accessor functions with nil-safety
(defn-spec get-system-info any?
  "Extract system information from state."
  [state map?]
  (get state :system))

(defn-spec get-camera-day any?
  "Extract day camera information from state."
  [state map?]
  (get state :camera-day))

(defn-spec get-camera-heat any?
  "Extract heat camera information from state."
  [state map?]
  (get state :camera-heat))

(defn-spec get-gps-info any?
  "Extract GPS information from state."
  [state map?]
  (get state :gps))

(defn-spec get-compass-info any?
  "Extract compass information from state."
  [state map?]
  (get state :compass))

(defn-spec get-lrf-info any?
  "Extract laser range finder information from state."
  [state map?]
  (get state :lrf))

(defn-spec get-time-info any?
  "Extract time information from state."
  [state map?]
  (get state :time))

;; Higher-level state queries
(defn-spec get-location (s/nilable map?)
  "Extract location data (GPS + compass) from state."
  [state map?]
  (when-let [gps (get-gps-info state)]
    {:latitude (:latitude gps)
     :longitude (:longitude gps)
     :altitude (:altitude gps)
     :heading (get-in state [:compass :heading])
     :timestamp (get-in state [:time :timestamp])}))

(defn-spec cameras-available? boolean?
  "Check if camera data is available in state."
  [state map?]
  (or (some? (get-camera-day state))
      (some? (get-camera-heat state))))

;; Validation functions
(defn-spec valid-command? boolean?
  "Check if a command map is valid for serialization."
  [cmd-map any?]
  (s/valid? ::command cmd-map))

(defn-spec explain-invalid-command string?
  "Get explanation for why a command is invalid."
  [cmd-map any?]
  (s/explain-str ::command cmd-map))