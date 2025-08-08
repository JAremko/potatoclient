(ns state-explorer.proto-handler
  "Handle protobuf to EDN conversion using Pronto"
  (:require [clojure.tools.logging :as log]
            ;; [pronto.core :as pronto] ; Disabled for now - Pronto not available
            [state-explorer.capture :as capture])
  (:import [ser JonSharedData$JonGUIState]))

(defn parse-state-message
  "Parse binary payload as JonGUIState protobuf message"
  [binary-data]
  (try
    (JonSharedData$JonGUIState/parseFrom binary-data)
    (catch Exception e
      (log/error e "Failed to parse protobuf message")
      nil)))

(defn proto->edn
  "Convert protobuf message to EDN - simplified version without Pronto"
  [proto-message]
  (try
    (when proto-message
      ;; For now, just return a simple map with basic info
      ;; TODO: Re-enable Pronto when available
      {:message-type (-> proto-message .getClass .getSimpleName)
       :byte-size (.getSerializedSize proto-message)
       :has-data true
       :note "Pronto not available - showing basic info only"})
    (catch Exception e
      (log/error e "Failed to convert protobuf to EDN")
      nil)))

(defn process-payload
  "Process a binary payload: parse as protobuf and convert to EDN"
  [binary-data]
  (when-let [proto-msg (parse-state-message binary-data)]
    (proto->edn proto-msg)))

(defn create-converting-handler
  "Create a capture handler that also converts to EDN.
  
  This wraps the basic capture handler and adds EDN conversion."
  [{:keys [output-dir max-count on-payload on-complete]
    :or {output-dir "./output"
         max-count 1}}]
  (let [captures (atom [])
        counter (atom 0)]
    
    ;; Create base capture handler
    (fn [payload]
      (let [current-count (swap! counter inc)
            timestamp (System/currentTimeMillis)]
        
        (when (<= current-count max-count)
          (log/info (format "Processing payload %d/%d" current-count max-count))
          
          ;; Save binary file
          (let [bin-file (capture/save-binary output-dir payload)]
            (when bin-file
              ;; Convert to EDN
              (let [edn-data (process-payload payload)
                    edn-file (when edn-data
                              (capture/save-edn output-dir edn-data timestamp))]
                
                (let [capture-info {:binary-file bin-file
                                   :edn-file edn-file
                                   :timestamp timestamp
                                   :size (count payload)
                                   :sequence current-count
                                   :has-edn (boolean edn-file)}]
                  (swap! captures conj capture-info)
                  
                  ;; Callback with both binary and EDN
                  (when on-payload
                    (on-payload {:binary payload
                                :edn edn-data
                                :timestamp timestamp
                                :files {:binary bin-file
                                        :edn edn-file}}))
                  
                  ;; Log status
                  (if edn-file
                    (log/info "Saved" (.getName bin-file) "and" (.getName edn-file))
                    (log/warn "Saved" (.getName bin-file) "but EDN conversion failed")))))
            
            ;; Check if we've reached the limit
            (when (>= current-count max-count)
              (log/info "Capture limit reached")
              (when on-complete
                (on-complete @captures)))))))

(defn analyze-edn-structure
  "Analyze the structure of captured EDN data for reporting"
  [edn-data]
  (when (map? edn-data)
    {:top-level-keys (keys edn-data)
     :field-count (count edn-data)
     :has-camera-day (contains? edn-data :camera-day)
     :has-camera-heat (contains? edn-data :camera-heat)
     :has-rotary (contains? edn-data :rotary)
     :has-compass (contains? edn-data :compass)
     :has-gps (contains? edn-data :gps)}))