(ns state-explorer.pronto-handler
  "Handle protobuf to EDN conversion using Pronto directly"
  (:require [clojure.tools.logging :as log]
            [pronto.core :as p]
            [state-explorer.capture :as capture])
  (:import [ser JonSharedData$JonGUIState]))

;; Define mapper for state messages
(p/defmapper state-mapper [ser.JonSharedData$JonGUIState])

(defn parse-state-message
  "Parse binary payload as JonGUIState protobuf message using Pronto"
  [binary-data]
  (try
    (p/bytes->proto-map state-mapper ser.JonSharedData$JonGUIState binary-data)
    (catch Exception e
      (log/error e "Failed to parse protobuf message")
      nil)))

(defn proto->edn
  "Convert protobuf to EDN using Pronto's proto-map->clj-map.
   This gives us the canonical snake_case representation."
  [proto-map]
  (try
    (when proto-map
      (p/proto-map->clj-map proto-map))
    (catch Exception e
      (log/error e "Failed to convert protobuf to EDN")
      nil)))

(defn create-converting-handler
  "Create a capture handler that saves binary and Pronto EDN"
  [{:keys [output-dir max-count on-complete]
    :or {output-dir "./output" max-count 1}}]
  (let [counter (atom 0)
        captures (atom [])]
    (fn [payload]
      (let [current (swap! counter inc)
            timestamp (System/currentTimeMillis)]
        (when (<= current max-count)
          (log/info (format "Processing payload %d/%d" current max-count))
          
          ;; Save binary
          (when-let [bin-file (capture/save-binary output-dir payload)]
            ;; Parse with Pronto and save EDN
            (let [proto-map (parse-state-message payload)
                  edn-data (proto->edn proto-map)
                  edn-file (when edn-data
                            (capture/save-edn output-dir edn-data timestamp))]
              
              (swap! captures conj {:binary-file bin-file
                                   :edn-file edn-file
                                   :timestamp timestamp
                                   :size (count payload)})
              
              (if edn-file
                (log/info "Saved" (.getName bin-file) "and" (.getName edn-file))
                (log/warn "Saved" (.getName bin-file) "only"))))
          
          (when (>= current max-count)
            (log/info "Capture complete")
            (when on-complete
              (on-complete @captures))))))))