(ns state-explorer.proto-handler-simple
  "Simplified protobuf handler without Pronto"
  (:require [clojure.tools.logging :as log]
            [state-explorer.capture :as capture]
            [state-explorer.proto-to-edn :as p2e])
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
  "Convert protobuf message to EDN using reflection"
  [proto-message]
  (try
    (when proto-message
      (p2e/proto-message->map proto-message))
    (catch Exception e
      (log/error e "Failed to convert protobuf")
      ;; Fallback to basic info
      {:message-type (-> proto-message .getClass .getSimpleName)
       :byte-size (.getSerializedSize proto-message)
       :has-data true
       :error (.getMessage e)})))

(defn create-converting-handler
  "Create a capture handler that saves binary and EDN"
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
            ;; Parse and save EDN
            (let [proto-msg (parse-state-message payload)
                  edn-data (proto->edn proto-msg)
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