(ns video-control-center
  (:require [seesaw.core :as seesaw]
            [seesaw.table :as table]
            [seesaw.chooser :as chooser]
            [clojure.data.json :as json]
            [clojure.core.async :as async]
            [clojure.java.io :as io]
            [seesaw.color :as color])
  (:import [java.lang ProcessBuilder]
           [java.io BufferedReader BufferedWriter InputStreamReader OutputStreamWriter]
           [javax.swing.table DefaultTableCellRenderer]
           [java.awt Color])
  (:gen-class))

(defonce state (atom {:heat nil :day nil}))
(defonce log-model (atom []))
(defonce domain (atom "sych.local"))
(defonce log-buffer (atom []))
(defonce update-scheduled (atom false))

(defn find-java []
  (let [java-home (System/getProperty "java.home")
        sep (System/getProperty "file.separator")
        java-bin (str java-home sep "bin" sep "java")]
    (if (.exists (io/file java-bin))
      java-bin
      (if (.exists (io/file (str java-bin ".exe")))
        (str java-bin ".exe")
        "java"))))

(defn start-stream-process [stream-id url]
  (let [java-exe (find-java)
        classpath (System/getProperty "java.class.path")
        pb (ProcessBuilder. [java-exe 
                           "--enable-native-access=ALL-UNNAMED"
                           "-cp" classpath
                           "-Djna.library.path=/usr/lib"
                           "-Dgstreamer.library.path=/usr/lib"
                           "-Dgstreamer.plugin.path=/usr/lib/gstreamer-1.0"
                           "com.sycha.VideoStreamManager"
                           stream-id url])
        process (.start pb)
        writer (BufferedWriter. (OutputStreamWriter. (.getOutputStream process)))
        stdout-reader (BufferedReader. (InputStreamReader. (.getInputStream process)))
        stderr-reader (BufferedReader. (InputStreamReader. (.getErrorStream process)))
        output-chan (async/chan 100)]
    
    ;; Stdout reader thread
    (future
      (try
        (loop []
          (when-let [line (.readLine stdout-reader)]
            (when (and (not (.isEmpty line))
                       (.startsWith line "{"))
              (try
                (let [msg (json/read-str line :key-fn keyword)]
                  (async/>!! output-chan msg))
                (catch Exception e
                  ;; Non-JSON stdout - log as INFO
                  (async/>!! output-chan
                    {:type "log"
                     :streamId stream-id
                     :level "INFO"
                     :message line
                     :timestamp (System/currentTimeMillis)}))))
            (recur)))
        (catch Exception e
          ;; Stream closed, normal during shutdown
          )))
    
    ;; Stderr reader thread - consolidate multi-line output
    (future
      (try
        (loop [buffer nil]
          (if-let [line (.readLine stderr-reader)]
            ;; Check if this line starts a new error or is continuation
            (if (or (re-find #"^\s+at\s+" line)  ; Stack trace line
                    (re-find #"^Caused by:" line) ; Caused by line
                    (re-find #"^\s+\.\.\." line)  ; More lines indicator
                    (and buffer (re-find #"^\s+" line))) ; Indented continuation
              ;; Continuation of previous error
              (recur (if buffer (str buffer "\n" line) line))
              ;; New error line or message
              (do
                ;; Send previous buffer if exists
                (when buffer
                  (async/>!! output-chan
                    {:type "log"
                     :streamId stream-id
                     :level "STDERR"
                     :message buffer
                     :timestamp (System/currentTimeMillis)}))
                ;; Start new buffer
                (recur line)))
            ;; EOF - send final buffer
            (when buffer
              (async/>!! output-chan
                {:type "log"
                 :streamId stream-id
                 :level "STDERR"
                 :message buffer
                 :timestamp (System/currentTimeMillis)}))))
        (catch Exception e
          ;; Stream closed, normal during shutdown
          )))
    
    {:process process
     :writer writer
     :stdout-reader stdout-reader
     :stderr-reader stderr-reader
     :output-chan output-chan
     :stream-id stream-id}))

(defn send-command [stream cmd]
  (when stream
    (try
      (.write (:writer stream) (json/write-str cmd))
      (.newLine (:writer stream))
      (.flush (:writer stream))
      (catch Exception e
        (println "Send error:" e)))))

(defn stop-stream [stream]
  (when stream
    (try
      (send-command stream {:action "shutdown"})
      (Thread/sleep 100)
      (catch Exception e))
    (try
      (when (.isAlive (:process stream))
        (.destroyForcibly (:process stream)))
      (catch Exception e))
    (try
      (.close (:writer stream))
      (catch Exception e))
    (try
      (.close (:stdout-reader stream))
      (catch Exception e))
    (try
      (.close (:stderr-reader stream))
      (catch Exception e))))

(defn add-log-entry! [entry]
  ;; Write to stdout/stderr for logging
  (let [fmt (java.text.SimpleDateFormat. "HH:mm:ss.SSS")
        log-msg (format "[%s] %s %s: %s"
                        (.format fmt (java.util.Date. (:time entry)))
                        (:stream entry)
                        (:type entry)
                        (:message entry))]
    (if (contains? #{"ERROR" "STDERR"} (:type entry))
      (binding [*out* *err*]
        (println log-msg))
      (println log-msg)))
  
  ;; Add to buffer instead of directly to model
  (swap! log-buffer conj entry)
  ;; Keep buffer size limited
  (when (> (count @log-buffer) 200)
    (swap! log-buffer (fn [v] (vec (drop 100 v)))))
  ;; Schedule batch update if not already scheduled
  (when (compare-and-set! update-scheduled false true)
    (seesaw/timer
      (fn [_]
        (let [entries @log-buffer]
          (when (seq entries)
            (reset! log-buffer [])
            (seesaw/invoke-later
              (swap! log-model (fn [current]
                               (vec (take 100 (concat entries current)))))))
          (reset! update-scheduled false)))
      :delay 100
      :repeats? false)))

(defn format-window-event [event]
  (let [type (:type event)
        details (dissoc event :type)]
    (case type
      "resized" (str "Resized to " (:width details) "x" (:height details))
      "moved" (str "Moved to (" (:x details) ", " (:y details) ")")
      "maximized" "Maximized"
      "unmaximized" "Restored from maximized"
      "minimized" "Minimized to taskbar"
      "restored" "Restored from taskbar"
      "focused" "Got focus"
      "unfocused" "Lost focus"
      "opened" "Window opened"
      "closing" "Window closing"
      (str type " " (json/write-str details)))))

(defn format-navigation-event [event]
  (let [type (:type event)
        x (:x event)
        y (:y event)
        ndc-x (:ndcX event)
        ndc-y (:ndcY event)
        button (:button event)
        click-count (:clickCount event)
        wheel (:wheelRotation event)
        ; Format coordinates with NDC if available
        coords (if (and ndc-x ndc-y)
                 (format "%d,%d [NDC: %.3f,%.3f]" x y ndc-x ndc-y)
                 (str x "," y))]
    (case type
      "mouse-click" (str "Click (" (case button 1 "Left" 2 "Middle" 3 "Right" button) 
                        (if (> click-count 1) (str " x" click-count) "") ") @ " coords)
      "mouse-press" (str "Press (" (case button 1 "Left" 2 "Middle" 3 "Right" button) ") @ " coords)
      "mouse-release" (str "Release (" (case button 1 "Left" 2 "Middle" 3 "Right" button) ") @ " coords)
      "mouse-move" (str "Move @ " coords)
      "mouse-drag-start" (str "Drag start @ " coords)
      "mouse-drag" (str "Dragging @ " coords)
      "mouse-drag-end" (str "Drag end @ " coords)
      "mouse-enter" (str "Mouse entered @ " coords)
      "mouse-exit" (str "Mouse exited @ " coords)
      "mouse-wheel" (str "Wheel " (if (pos? wheel) "down" "up") " " (Math/abs wheel) " @ " coords)
      (str type " @ " coords))))

(defn process-stream-output [stream-key stream ui-elements]
  (async/go-loop []
    (when-let [msg (async/<! (:output-chan stream))]
      (case (:type msg)
        "response" (do
                    (add-log-entry! {:time (System/currentTimeMillis)
                                   :stream (:streamId msg)
                                   :type "RESPONSE"
                                   :message (:status msg)
                                   :raw-data msg})
                    ;; Handle window-closed event
                    (when (= (:status msg) "window-closed")
                      (seesaw/invoke-later
                        (swap! state assoc stream-key nil)
                        (when-let [btn (get @ui-elements stream-key)]
                          (seesaw/text! btn (str (name stream-key) " Stream OFF"))
                          (seesaw/config! btn :selected? false)))))
        "log" (let [stack-trace (:stackTrace msg)
                    full-message (if stack-trace
                                   (str (:message msg) "\n" stack-trace)
                                   (:message msg))]
                (add-log-entry! {:time (System/currentTimeMillis)
                                :stream (:streamId msg)
                                :type (:level msg)
                                :message full-message
                                :raw-data msg}))
        "navigation" (add-log-entry! {:time (System/currentTimeMillis)
                                     :stream (:streamId msg)
                                     :type "NAV"
                                     :message (format-navigation-event (:event msg))
                                     :raw-data msg
                                     :nav-type (get-in msg [:event :type])})
        "window" (add-log-entry! {:time (System/currentTimeMillis)
                                 :stream (:streamId msg)
                                 :type "WINDOW"
                                 :message (format-window-event (:event msg))
                                 :raw-data msg
                                 :event-type (get-in msg [:event :type])})
        nil)
      (recur))))

(defn toggle-stream [stream-key endpoint ui-elements]
  (if-let [stream (get @state stream-key)]
    ;; Hide and stop
    (future
      (send-command stream {:action "hide"})
      (Thread/sleep 100)
      (stop-stream stream)
      (swap! state assoc stream-key nil))
    ;; Start and show
    (future
      (let [url (str "wss://" @domain endpoint)
            stream (start-stream-process (name stream-key) url)]
        (swap! state assoc stream-key stream)
        (process-stream-output stream-key stream ui-elements)
        (Thread/sleep 100)
        (send-command stream {:action "show"}))))
  (not (get @state stream-key)))

(defn save-logs-to-file []
  (when-let [file (chooser/choose-file
                    :type :save
                    :filters [["Log files" ["log" "txt"]]
                              ["All files" ["*"]]]) ]
    (try
      (with-open [w (io/writer file)]
        (let [fmt (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss.SSS")]
          (.write w "Potato Client Log Export\n")
          (.write w (str "Exported at: " (.format fmt (java.util.Date.)) "\n"))
          (.write w "=====================================\n\n")
          (doseq [entry @log-model]
            (.write w (format "[%s] %s %s: %s\n"
                            (.format fmt (java.util.Date. (:time entry)))
                            (:stream entry)
                            (:type entry)
                            (:message entry)))
            (when (contains? #{"ERROR" "STDERR"} (:type entry))
              (.write w "\n")))))
      (seesaw/alert "Logs saved successfully!")
      (catch Exception e
        (seesaw/alert (str "Error saving logs: " (.getMessage e)))))))

(defn create-control-panel []
  (let [domain-field (seesaw/text :columns 20 :text @domain)
        heat-btn (seesaw/toggle :text "Heat Stream OFF")
        day-btn (seesaw/toggle :text "Day Stream OFF")
        clear-log-btn (seesaw/button :text "Clear Log")
        save-log-btn (seesaw/button :text "Save Logs")
        ui-elements (atom {:heat heat-btn :day day-btn})]
    
    ;; Update domain atom when field changes
    (seesaw/listen domain-field :document
      (fn [e]
        (reset! domain (seesaw/text domain-field))))
    
    (seesaw/listen heat-btn :action
      (fn [e]
        (let [active (toggle-stream :heat "/ws/ws_rec_video_heat" ui-elements)]
          (seesaw/text! heat-btn (if active "Heat Stream ON" "Heat Stream OFF")))))
    
    (seesaw/listen day-btn :action
      (fn [e]
        (let [active (toggle-stream :day "/ws/ws_rec_video_day" ui-elements)]
          (seesaw/text! day-btn (if active "Day Stream ON" "Day Stream OFF")))))
    
    (seesaw/listen clear-log-btn :action
      (fn [e]
        (reset! log-model [])))
    
    (seesaw/listen save-log-btn :action
      (fn [e]
        (save-logs-to-file)))
    
    (seesaw/vertical-panel
      :border 5
      :items [(seesaw/label :text "Video Stream Control Center" :font "ARIAL-BOLD-16")
              (seesaw/horizontal-panel :items ["Domain: " domain-field])
              (seesaw/label :text "Heat: 900x720 @ 30fps | Day: 1920x1080 @ 30fps" :font "ARIAL-10")
              (seesaw/flow-panel :items [heat-btn day-btn])
              (seesaw/horizontal-panel :items [clear-log-btn save-log-btn])])))

(defn get-type-color [type event-type nav-type]
  (case type
    "RESPONSE" (Color. 100 200 100)  ; Light green
    "INFO" (Color. 100 150 255)      ; Light blue
    "WARN" (Color. 255 200 100)      ; Orange
    "ERROR" (Color. 255 100 100)     ; Light red
    "NAV" (case nav-type
            "mouse-click" (Color. 255 150 150)     ; Light red
            "mouse-press" (Color. 255 180 180)     ; Lighter red
            "mouse-release" (Color. 180 255 180)   ; Light green
            "mouse-move" (Color. 220 220 255)      ; Very light blue
            "mouse-drag-start" (Color. 255 200 150) ; Peach
            "mouse-drag" (Color. 255 220 180)      ; Light peach
            "mouse-drag-end" (Color. 200 255 200)  ; Mint
            "mouse-enter" (Color. 180 255 255)     ; Light cyan
            "mouse-exit" (Color. 255 180 255)      ; Light magenta
            "mouse-wheel" (Color. 255 255 180)     ; Light yellow
            (Color. 200 150 255))                  ; Purple default
    "WINDOW" (case event-type
               "resized" (Color. 255 220 100)    ; Yellow
               "moved" (Color. 100 220 255)      ; Cyan
               "maximized" (Color. 150 255 150)  ; Light green
               "minimized" (Color. 200 200 200)  ; Gray
               "focused" (Color. 100 255 200)    ; Mint
               "unfocused" (Color. 220 180 150)  ; Tan
               (Color. 200 200 255))              ; Light purple default
    (Color. 255 255 255)))                        ; White default

(defn create-type-renderer [table-model]
  (proxy [DefaultTableCellRenderer] []
    (getTableCellRendererComponent [table value isSelected hasFocus row column]
      (let [component (proxy-super getTableCellRendererComponent table value isSelected hasFocus row column)
            row-data (table/value-at table-model row)]
        (when (and row-data (not isSelected))
          (let [type (:type row-data)
                event-type (:event-type row-data)
                nav-type (:nav-type row-data)]
            (.setBackground component (get-type-color type event-type nav-type))))
        component))))

(defn create-log-table []
  (let [columns [{:key :time :text "Time" 
                  :class java.lang.Long
                  :read (fn [m] (.format (java.text.SimpleDateFormat. "HH:mm:ss.SSS") (java.util.Date. (:time m))))
                  :write (fn [m v] m)}
                 {:key :stream :text "Stream"}
                 {:key :type :text "Type"}
                 {:key :message :text "Message"}]
        table-model (table/table-model :columns columns)
        log-table (seesaw/table :model table-model
                               :show-grid? true)]
    
    ;; Update table when log-model changes
    (add-watch log-model :table-updater
      (fn [_ _ old-val new-val]
        (when (not= old-val new-val)
          (seesaw/invoke-later
            ;; Always update the table
            (table/clear! table-model)
            (doseq [row new-val]
              (table/insert-at! table-model (table/row-count table-model) row))))))
    
    ;; Set custom renderer for type column
    (let [type-renderer (create-type-renderer table-model)]
      (.setCellRenderer (.getColumn (.getColumnModel log-table) 2) type-renderer))
    
    ;; Add mouse listener for JSON display
    (seesaw/listen log-table :mouse-clicked
      (fn [e]
        (when (= 2 (.getClickCount e))  ; Double click
          (let [row (.rowAtPoint log-table (.getPoint e))]
            (when (>= row 0)
              (when-let [row-data (table/value-at table-model row)]
                (when-let [raw-data (:raw-data row-data)]
                  (seesaw/alert (str "JSON Data for " (:type row-data) " event:\n\n"
                                   (json/write-str raw-data :indent true))))))))))
    
    (seesaw/scrollable log-table)))

(defn create-main-window []
  (let [control-panel (create-control-panel)
        log-table (create-log-table)]
    
    (seesaw/frame
      :title "WebSocket Video Streams - Control Center"
      :icon (clojure.java.io/resource "icon.png")
      :on-close :exit
      :size [800 :by 600]
      :content (seesaw/border-panel
                 :north control-panel
                 :center log-table))))

(defn -main [& args]
  (seesaw/native!)
  
  ;; Shutdown hook - more robust
  (.addShutdownHook (Runtime/getRuntime)
    (Thread. (fn []
              (try
                (doseq [[k stream] @state]
                  (when stream
                    (try
                      (when (.isAlive (:process stream))
                        (.destroyForcibly (:process stream)))
                      (catch Exception e))))
                (catch Exception e)))))
  
  (seesaw/invoke-later
    (-> (create-main-window)
        seesaw/show!))
  
  (add-log-entry! {:time (System/currentTimeMillis)
                   :stream "SYSTEM"
                   :type "INFO"
                   :message "Control Center started"}))