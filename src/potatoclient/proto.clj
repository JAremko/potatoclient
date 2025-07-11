(ns potatoclient.proto
  (:require [pronto.core :as p]
            [pronto.utils :as u])
  (:import [ser JonSharedData$JonGUIState]
           [cmd JonSharedCmd$Root]))

;; Define the mapper for state and command messages
(p/defmapper proto-mapper [JonSharedData$JonGUIState JonSharedCmd$Root]
  :key-name-fn u/->kebab-case)

(defn serialize-cmd
  "Serialize a Clojure command map to protobuf bytes.
  The command should have the structure:
  {:protocol-version 1
   :session-id 12345  
   :important true
   :from-cv-subsystem false
   :client-type \"JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK\"
   :payload {:ping {}}}  ; or other payload types"
  [cmd-map]
  (try
    (-> (p/clj-map->proto-map proto-mapper JonSharedCmd$Root cmd-map)
        (p/proto-map->bytes))
    (catch Exception e
      (throw (ex-info "Failed to serialize command" 
                      {:error (.getMessage e)
                       :command cmd-map})))))

(defn deserialize-state
  "Deserialize protobuf bytes to a Clojure state map.
  Returns a map with kebab-case keys."
  [proto-bytes]
  (try
    (p/bytes->proto-map proto-mapper JonSharedData$JonGUIState proto-bytes)
    (catch Exception e
      (throw (ex-info "Failed to deserialize state" 
                      {:error (.getMessage e)
                       :bytes-length (count proto-bytes)})))))

(defn cmd-ping
  "Create a ping command.
  client-type should be one of: \"JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV\", 
  \"JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK\", \"JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED\", 
  \"JON_GUI_DATA_CLIENT_TYPE_LIRA\""
  [session-id client-type]
  {:protocol-version 1
   :session-id session-id
   :important false
   :from-cv-subsystem false
   :client-type client-type
   :payload {:ping {}}})

(defn cmd-noop
  "Create a no-op command.
  client-type should be one of the JON_GUI_DATA_CLIENT_TYPE_* enum strings"
  [session-id client-type]
  {:protocol-version 1
   :session-id session-id
   :important false
   :from-cv-subsystem false
   :client-type client-type
   :payload {:noop {}}})

(defn cmd-frozen
  "Create a frozen command.
  client-type should be one of the JON_GUI_DATA_CLIENT_TYPE_* enum strings"
  [session-id client-type]
  {:protocol-version 1
   :session-id session-id
   :important true
   :from-cv-subsystem false
   :client-type client-type
   :payload {:frozen {}}})

;; Utility functions for working with state data
(defn get-system-info [state]
  (get state :system))

(defn get-camera-day [state]
  (get state :camera-day))

(defn get-camera-heat [state]
  (get state :camera-heat))

(defn get-gps-info [state]
  (get state :gps))

(defn get-compass-info [state]
  (get state :compass))

(defn get-lrf-info [state]
  (get state :lrf))

(defn get-time-info [state]
  (get state :time))

;; Example usage:
(comment
  ;; Deserialize incoming state
  (def state (deserialize-state some-proto-bytes))
  
  ;; Access state data - proto-maps work like regular maps
  (get-system-info state)
  (:camera-day state)
  (get-in state [:gps :latitude])
  
  ;; Create and serialize commands
  (def ping-cmd (cmd-ping 12345 "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK"))
  (def cmd-bytes (serialize-cmd ping-cmd))
  
  ;; Send cmd-bytes over WebSocket...
  
  ;; You can also work with proto-maps directly
  (def cmd-proto-map (p/proto-map proto-mapper JonSharedCmd$Root))
  (-> cmd-proto-map
      (assoc :protocol-version 1
             :session-id 12345
             :client-type "JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK")
      (assoc-in [:payload :ping] {})))