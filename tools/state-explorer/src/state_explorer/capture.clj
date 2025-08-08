(ns state-explorer.capture
  "Capture and save WebSocket payloads"
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [zprint.core :as zp])
  (:import [java.io FileOutputStream]
           [java.nio.file Files Paths StandardOpenOption]))

(defn ensure-output-dir
  "Ensure output directory exists"
  [output-dir]
  (let [dir (io/file output-dir)]
    (when-not (.exists dir)
      (.mkdirs dir)
      (log/info "Created output directory:" output-dir))
    output-dir))

(defn generate-filename
  "Generate timestamp-based filename"
  [output-dir extension]
  (let [timestamp (System/currentTimeMillis)
        filename (str timestamp extension)]
    (io/file output-dir filename)))

(defn save-binary
  "Save binary payload to file"
  [output-dir payload]
  (let [file (generate-filename output-dir ".bin")]
    (try
      (with-open [fos (FileOutputStream. file)]
        (.write fos payload))
      (log/debug "Saved binary payload to" (.getName file))
      file
      (catch Exception e
        (log/error e "Failed to save binary payload")
        nil))))

(defn save-edn
  "Save EDN data to file with pretty-printing"
  [output-dir data timestamp]
  (let [filename (str timestamp ".edn")
        file (io/file output-dir filename)]
    (try
      ;; Use zprint for pretty-printing with nice formatting
      (spit file (zp/zprint-str data {:map {:comma? false
                                             :force-nl? true}
                                       :width 120
                                       :style :community}))
      (log/debug "Saved EDN to" (.getName file))
      file
      (catch Exception e
        (log/error e "Failed to save EDN")
        nil))))

(defn create-capture-handler
  "Create a message handler that captures payloads.
  
  Options:
    :output-dir - Directory to save files (default: ./output)
    :max-count - Maximum number of payloads to capture
    :on-payload - Optional callback for each payload (receives {:binary bytes :timestamp ms})
    :on-complete - Optional callback when capture limit reached"
  [{:keys [output-dir max-count on-payload on-complete]
    :or {output-dir "./output"
         max-count 1}}]
  (ensure-output-dir output-dir)
  (let [counter (atom 0)
        captures (atom [])]
    
    (fn [payload]
      (let [current-count (swap! counter inc)
            timestamp (System/currentTimeMillis)]
        
        (when (<= current-count max-count)
          (log/info (format "Captured payload %d/%d" current-count max-count))
          
          ;; Save binary file
          (let [bin-file (save-binary output-dir payload)]
            (when bin-file
              (let [capture-info {:binary-file bin-file
                                 :timestamp timestamp
                                 :size (count payload)
                                 :sequence current-count}]
                (swap! captures conj capture-info)
                
                ;; Callback with payload info
                (when on-payload
                  (on-payload {:binary payload
                              :timestamp timestamp
                              :file bin-file}))))
            
            ;; Check if we've reached the limit
            (when (>= current-count max-count)
              (log/info "Capture limit reached")
              (when on-complete
                (on-complete @captures)))))))))

(defn get-capture-stats
  "Get statistics from captures"
  [captures]
  {:total-count (count captures)
   :total-size (reduce + 0 (map :size captures))
   :avg-size (if (empty? captures)
               0
               (/ (reduce + 0 (map :size captures))
                  (count captures)))
   :duration-ms (when (>= (count captures) 2)
                 (- (:timestamp (last captures))
                    (:timestamp (first captures))))})