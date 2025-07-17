(ns potatoclient.ui.log-viewer
  (:require [seesaw.core :as seesaw]
            [seesaw.table :as table]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [potatoclient.theme :as theme]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.config :as config]
            [potatoclient.runtime :as runtime])
  (:import [java.awt Dimension BorderLayout Toolkit]
           [java.awt.datatransfer StringSelection]
           [java.awt.event KeyEvent]
           [java.io File]
           [java.text SimpleDateFormat]
           [java.util Date]))

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
  [text]
  (let [selection (StringSelection. text)
        clipboard (.getSystemClipboard (Toolkit/getDefaultToolkit))]
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
  [file parent-frame]
  (let [raw-content (slurp file)
        content (format-log-content raw-content)
        text-area (seesaw/text :multi-line? true
                               :text content
                               :editable? false
                               :wrap-lines? true
                               :font {:name "Monospaced" :size 12})
        copy-button (seesaw/button :text "Copy to Clipboard"
                                   :icon (theme/key->icon :file-save)
                                   :listen [:action (fn [_] 
                                                      (copy-to-clipboard raw-content)
                                                      (seesaw/alert "Copied to clipboard!"))])
        frame (seesaw/frame :title (str "Log: " (.getName file))
                            :icon (clojure.java.io/resource "main.png")
                            :size [800 :by 600]
                            :on-close :dispose)
        close-button (seesaw/button :text "Close"
                                    :icon (theme/key->icon :file-excel)
                                    :listen [:action (fn [_] 
                                                       (.dispose frame))])
        button-panel (seesaw/horizontal-panel :items [copy-button close-button])]
    (seesaw/config! frame :content
                    (seesaw/border-panel
                     :center (seesaw/scrollable text-area)
                     :south button-panel
                     :vgap 10
                     :hgap 10
                     :border 10))
    (.setLocationRelativeTo frame parent-frame)
    ;; Set caret to beginning so view starts at top
    (.setCaretPosition text-area 0)
    (seesaw/show! frame)
    frame))

(defn- create-log-table
  []
  (let [columns [{:key :filename :text "Filename"}
                 {:key :version :text "Version"}
                 {:key :formatted-timestamp :text "Created"}
                 {:key :formatted-size :text "Size"}]
        model (table/table-model :columns columns)]
    (seesaw/table :model model
                  :show-grid? true)))

(defn- update-log-list
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
  []
  (let [table (create-log-table)
        refresh-button (seesaw/button :text "Refresh"
                                      :icon (theme/key->icon :file-import)
                                      :listen [:action (fn [_] (update-log-list table))])
        open-button (seesaw/button :text "Open"
                                   :icon (theme/key->icon :file-open)
                                   :enabled? false)
        close-button (seesaw/button :text "Close"
                                    :icon (theme/key->icon :file-excel))
        button-panel (seesaw/horizontal-panel :items [refresh-button open-button close-button])
        frame (seesaw/frame :title "Log Viewer"
                            :icon (clojure.java.io/resource "main.png")
                            :size [700 :by 400]
                            :on-close :dispose)]
    
    ;; Selection listener
    (seesaw/listen table :selection
                   (fn [_]
                     (let [selected (seesaw/selection table)]
                       (seesaw/config! open-button :enabled? (some? selected)))))
    
    ;; Function to open selected file
    (let [open-selected-file (fn []
                               (let [selected-row (seesaw/selection table)]
                                 (when selected-row
                                   (let [row-data (table/value-at table selected-row)]
                                     (when-let [file (:file row-data)]
                                       (create-file-viewer file frame))))))]
      
      ;; Double-click listener
      (seesaw/listen table :mouse-clicked
                     (fn [e]
                       (when (= 2 (.getClickCount e))
                         (open-selected-file))))
      
      ;; Enter key listener
      (seesaw/listen table :key-pressed
                     (fn [e]
                       (when (= (.getKeyCode e) java.awt.event.KeyEvent/VK_ENTER)
                         (open-selected-file))))
    
      ;; Open button listener
      (seesaw/listen open-button :action (fn [_] (open-selected-file))))
    
    ;; Close button listener
    (seesaw/listen close-button :action (fn [_] (.dispose frame)))
    
    ;; Set up frame content
    (seesaw/config! frame :content
                    (seesaw/border-panel
                     :center (seesaw/scrollable table)
                     :south button-panel
                     :vgap 10
                     :hgap 10
                     :border 10))
    
    ;; Populate table
    (update-log-list table)
    
    frame))

(defn show-log-viewer
  []
  (if (javax.swing.SwingUtilities/isEventDispatchThread)
    (let [log-files (get-log-files)]
      (if (empty? log-files)
        (seesaw/alert "No log files found in the logs directory.")
        (let [frame (create-log-viewer-frame)]
          (.setLocationRelativeTo frame nil)
          (seesaw/show! frame))))
    (seesaw/invoke-later
     (fn []
       (let [log-files (get-log-files)]
         (if (empty? log-files)
           (seesaw/alert "No log files found in the logs directory.")
           (let [frame (create-log-viewer-frame)]
             (.setLocationRelativeTo frame nil)
             (seesaw/show! frame))))))))