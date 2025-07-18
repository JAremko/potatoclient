(ns potatoclient.proto
  "Protocol buffer serialization for communication with the server.
  
  Provides functions to serialize commands and deserialize state messages
  using the pronto library for Clojure protobuf support."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn ?]]
            [malli.core :as m]
            [potatoclient.specs :as specs]
            [pronto.core :as p]
            [pronto.utils :as u])
  (:import (cmd JonSharedCmd$Root)
           (ser JonSharedData$JonGUIState)))

;; Client type constants for better readability
(def client-types
  {:internal-cv "JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV"
   :local "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"
   :certificate "JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED"
   :lira "JON_GUI_DATA_CLIENT_TYPE_LIRA"})

;; Command payload types
(def command-types #{:ping :noop :frozen})

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

;; Define the mapper for state and command messages
(p/defmapper proto-mapper [JonSharedData$JonGUIState JonSharedCmd$Root]
  :key-name-fn u/->kebab-case)

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
         (-> (p/clj-map->proto-map proto-mapper JonSharedCmd$Root cmd-map)
             (p/proto-map->bytes))
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