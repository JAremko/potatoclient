(ns potatoclient.ui.log-viewer
  "Log viewer window for browsing and viewing log files.
  
  Uses idiomatic Seesaw patterns including:
  - border-panel for layout management
  - listen for event handling
  - config! for dynamic component updates
  - Built-in Seesaw functions instead of Java interop where possible"
  (:require [seesaw.core :as seesaw]
            [seesaw.table :as table]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [potatoclient.theme :as theme]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.config :as config]
            [potatoclient.runtime :as runtime])
  (:import [java.awt.datatransfer StringSelection]
           [java.text SimpleDateFormat]))

(defn- get-log-directory
  []
  (logging/get-logs-directory))

(defn- parse-log-filename
  [filename]
  (when-let [match (re-matches #"potatoclient-(.+?)-(\d{8}-\d{6})\.log" filename)]
    {:version (second match)
     :timestamp (nth match 2)
     :filename filename}))

(defn- format-timestamp
  [timestamp-str]
  (try
    (let [input-format (SimpleDateFormat. "yyyyMMdd-HHmmss")
          output-format (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss")
          date (.parse input-format timestamp-str)]
      (.format output-format date))
    (catch Exception _
      timestamp-str)))

(defn- get-log-files
  []
  (let [log-dir (get-log-directory)]
    (if (.exists log-dir)
      (->> (.listFiles log-dir)
           (filter #(and (.isFile %)
                        (str/ends-with? (.getName %) ".log")))
           (map (fn [file]
                  (let [parsed (parse-log-filename (.getName file))]
                    (when parsed
                      (assoc parsed
                             :file file
                             :size (.length file)
                             :last-modified (.lastModified file))))))
           (filter some?)
           (sort-by :last-modified >))
      [])))

(defn- format-file-size
  [size]
  (cond
    (< size 1024) (str size " B")
    (< size (* 1024 1024)) (format "%.1f KB" (/ size 1024.0))
    :else (format "%.1f MB" (/ size (* 1024.0 1024.0)))))

(defn- copy-to-clipboard
  "Copy text to system clipboard."
  [text]
  (let [selection (StringSelection. text)
        clipboard (.getSystemClipboard (java.awt.Toolkit/getDefaultToolkit))]
    (.setContents clipboard selection selection)))

(defn- format-log-content
  "Add visual separators between log entries"
  [content]
  (let [lines (str/split-lines content)
        separator (str/join (repeat 80 "-"))]
    (->> lines
         (map (fn [line]
                (if (re-matches #"^\d{4}-\d{2}-\d{2}T.*" line)
                  (str separator "\n" line)
                  line)))
         (str/join "\n"))))

(defn- create-file-viewer
  "Create a viewer window for displaying log file contents."
  [file parent-frame]
  (let [raw-content (slurp file)
        content (format-log-content raw-content)
        text-area (seesaw/text 
                   :id :log-content
                   :multi-line? true
                   :text content
                   :editable? false
                   :wrap-lines? true
                   :font {:name "Monospaced" :size 12}
                   :caret-position 0)
        frame (seesaw/frame 
               :title (str "Log: " (.getName file))
               :icon (clojure.java.io/resource "main.png")
               :size [800 :by 600]
               :on-close :dispose)
        copy-action (seesaw/action
                     :name "Copy to Clipboard"
                     :icon (theme/key->icon :file-save)
                     :handler (fn [_] 
                               (copy-to-clipboard raw-content)
                               (seesaw/alert parent-frame "Copied to clipboard!")))
        close-action (seesaw/action
                      :name "Close"
                      :icon (theme/key->icon :file-excel)
                      :handler (fn [_] (seesaw/dispose! frame)))]
    ;; Build content using idiomatic border-panel
    (seesaw/config! frame :content
                    (seesaw/border-panel
                     :border 10
                     :hgap 10
                     :vgap 10
                     :center (seesaw/scrollable text-area)
                     :south (seesaw/horizontal-panel 
                            :items [copy-action close-action])))
    ;; Position relative to parent
    (seesaw/pack! frame)
    (.setLocationRelativeTo frame parent-frame)
    (seesaw/show! frame)
    frame))

(defn- create-log-table
  "Create the main log file listing table."
  []
  (seesaw/table 
   :id :log-table
   :model (table/table-model 
           :columns [{:key :filename :text "Filename"}
                     {:key :version :text "Version"}
                     {:key :formatted-timestamp :text "Created"}
                     {:key :formatted-size :text "Size"}])
   :show-grid? true))

(defn- update-log-list!
  "Update the log table with current log files."
  [table]
  (let [log-files (get-log-files)
        formatted-files (map (fn [log]
                               (assoc log
                                      :formatted-timestamp (format-timestamp (:timestamp log))
                                      :formatted-size (format-file-size (:size log))))
                             log-files)]
    (table/clear! table)
    (doseq [log formatted-files]
      (table/insert-at! table (table/row-count table) log))))

(defn create-log-viewer-frame
  "Create the main log viewer window."
  []
  (let [table (create-log-table)
        frame (seesaw/frame 
               :title "Log Viewer"
               :icon (clojure.java.io/resource "main.png")
               :size [700 :by 400]
               :on-close :dispose)
        
        ;; Define actions
        refresh-action (seesaw/action
                        :name "Refresh"
                        :icon (theme/key->icon :file-import)
                        :handler (fn [_] (update-log-list! table)))
        
        open-action (seesaw/action
                     :name "Open"
                     :icon (theme/key->icon :file-open)
                     :enabled? false
                     :handler (fn [_]
                               (when-let [selected-row (seesaw/selection table)]
                                 (let [row-data (table/value-at table selected-row)]
                                   (when-let [file (:file row-data)]
                                     (create-file-viewer file frame))))))
        
        close-action (seesaw/action
                      :name "Close"
                      :icon (theme/key->icon :file-excel)
                      :handler (fn [_] (seesaw/dispose! frame)))]
    
    ;; Wire up event handlers using idiomatic listen
    (seesaw/listen table
                   ;; Handle selection changes
                   :selection (fn [_]
                               (seesaw/config! open-action 
                                              :enabled? (some? (seesaw/selection table))))
                   
                   ;; Handle double-clicks
                   :mouse-clicked (fn [e]
                                   (when (= 2 (.getClickCount e))
                                     (.actionPerformed open-action nil)))
                   
                   ;; Handle Enter key
                   :key-pressed (fn [e]
                                 (when (= (.getKeyCode e) java.awt.event.KeyEvent/VK_ENTER)
                                   (.actionPerformed open-action nil))))
    
    ;; Build UI using border-panel
    (seesaw/config! frame :content
                    (seesaw/border-panel
                     :border 10
                     :hgap 10
                     :vgap 10
                     :center (seesaw/scrollable table 
                                               :border 0)
                     :south (seesaw/flow-panel
                            :align :center
                            :hgap 5
                            :items [refresh-action open-action close-action])))
    
    ;; Initialize table data
    (update-log-list! table)
    
    frame))

(defn show-log-viewer
  "Show the log viewer window, ensuring it runs on the EDT."
  []
  (if (javax.swing.SwingUtilities/isEventDispatchThread)
    ;; Already on EDT, execute immediately
    (let [log-files (get-log-files)]
      (if (empty? log-files)
        (seesaw/alert "No log files found in the logs directory.")
        (let [frame (create-log-viewer-frame)]
          (seesaw/pack! frame)
          (.setLocationRelativeTo frame nil)
          (seesaw/show! frame))))
    ;; Not on EDT, schedule execution
    (seesaw/invoke-later
     (fn []
       (let [log-files (get-log-files)]
         (if (empty? log-files)
           (seesaw/alert "No log files found in the logs directory.")
           (let [frame (create-log-viewer-frame)]
             (seesaw/pack! frame)
             (.setLocationRelativeTo frame nil)
             (seesaw/show! frame))))))))