(ns potatoclient.ui.log-viewer
  "Log viewer window for browsing and viewing log files.
  
  Uses idiomatic Seesaw patterns including:
  - border-panel for layout management
  - listen for event handling
  - config! for dynamic component updates
  - Built-in Seesaw functions instead of Java interop where possible"
  (:require [seesaw.core :as seesaw]
            [seesaw.table :as table]
            [seesaw.clipboard :as clipboard]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [potatoclient.theme :as theme]
            [potatoclient.i18n :as i18n]
            [potatoclient.logging :as logging]
            [potatoclient.config :as config]
            [potatoclient.runtime :as runtime])
  (:import [java.text SimpleDateFormat]
           [java.awt Dimension]
           [java.awt Frame]
           [java.awt.event KeyEvent]))


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
  (clipboard/contents! text))

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
               :title (str (i18n/tr :log-viewer-file-title) (.getName file))
               :icon (clojure.java.io/resource "main.png")
               :on-close :dispose)
        copy-action (seesaw/action
                     :name (i18n/tr :log-viewer-copy)
                     :icon (theme/key->icon :file-save)
                     :handler (fn [_] 
                               (copy-to-clipboard raw-content)
                               (seesaw/alert parent-frame (i18n/tr :log-viewer-copied))))
        close-action (seesaw/action
                      :name (i18n/tr :log-viewer-close)
                      :icon (theme/key->icon :file-excel)
                      :handler (fn [_] (seesaw/dispose! frame)))]
    ;; Build content using idiomatic border-panel
    (seesaw/config! frame :content
                    (seesaw/border-panel
                     :border 10
                     :hgap 10
                     :vgap 10
                     :center (seesaw/scrollable text-area
                                               :vscroll :always
                                               :hscroll :as-needed)
                     :south (seesaw/horizontal-panel 
                            :items [copy-action close-action])))
    ;; Set maximized state before showing to avoid visual glitch
    (.setExtendedState frame Frame/MAXIMIZED_BOTH)
    (.setLocationRelativeTo frame parent-frame)
    (seesaw/show! frame)
    frame))

(defn- pack-table-columns!
  "Auto-size table columns based on content using Seesaw's table utilities."
  [table]
  ;; Use Seesaw's table utilities where possible
  (let [col-model (.getColumnModel table)
        row-count (table/row-count table)]
    (doseq [col-idx (range (.getColumnCount col-model))]
      (let [column (.getColumn col-model col-idx)
            header-renderer (.getHeaderRenderer column)
            header-value (.getHeaderValue column)
            header-width (if header-renderer
                          (-> header-renderer
                              (.getTableCellRendererComponent table header-value false false 0 col-idx)
                              .getPreferredSize
                              .width)
                          75)
            max-width (atom header-width)]
        ;; Check each row's content width
        (doseq [row (range (min 20 row-count))]
          (let [renderer (.getCellRenderer table row col-idx)
                value (.getValueAt table row col-idx)
                comp (.getTableCellRendererComponent renderer table value false false row col-idx)
                width (-> comp .getPreferredSize .width)]
            (swap! max-width max width)))
        ;; Set preferred width with some padding
        (.setPreferredWidth column (+ @max-width 20))))))

(defn- create-log-table
  "Create the main log file listing table."
  []
  (seesaw/table 
   :id :log-table
   :model (table/table-model 
           :columns [{:key :filename :text (i18n/tr :log-viewer-column-filename)}
                     {:key :version :text (i18n/tr :log-viewer-column-version)}
                     {:key :formatted-timestamp :text (i18n/tr :log-viewer-column-created)}
                     {:key :formatted-size :text (i18n/tr :log-viewer-column-size)}])
   :show-grid? true
   :auto-resize :off))

(defn- update-log-list!
  "Update the log table with current log files."
  [table]
  (let [log-files (get-log-files)
        ;; Take only the most recent 20 files
        recent-files (take 20 log-files)
        formatted-files (map (fn [log]
                               (assoc log
                                      :formatted-timestamp (format-timestamp (:timestamp log))
                                      :formatted-size (format-file-size (:size log))))
                             recent-files)]
    (table/clear! table)
    (doseq [log formatted-files]
      (table/insert-at! table (table/row-count table) log))
    ;; Auto-size columns after adding data
    (pack-table-columns! table)))

(defn create-log-viewer-frame
  "Create the main log viewer window."
  []
  (let [table (create-log-table)
        frame (seesaw/frame 
               :title (i18n/tr :log-viewer-title)
               :icon (clojure.java.io/resource "main.png")
               :on-close :dispose)
        
        ;; Define actions
        refresh-action (seesaw/action
                        :name (i18n/tr :log-viewer-refresh)
                        :icon (theme/key->icon :file-import)
                        :handler (fn [_] (update-log-list! table)))
        
        open-action (seesaw/action
                     :name (i18n/tr :log-viewer-open)
                     :icon (theme/key->icon :file-open)
                     :enabled? false
                     :handler (fn [_]
                               (when-let [selected-row (seesaw/selection table)]
                                 (let [row-data (table/value-at table selected-row)]
                                   (when-let [file (:file row-data)]
                                     (create-file-viewer file frame))))))
        
        close-action (seesaw/action
                      :name (i18n/tr :log-viewer-close)
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
                                 (when (= (.getKeyCode e) KeyEvent/VK_ENTER)
                                   ((:handler (meta open-action)) nil))))
    
    ;; Build UI using border-panel
    (seesaw/config! frame :content
                    (seesaw/border-panel
                     :border 10
                     :hgap 10
                     :vgap 10
                     :center (let [scroll-pane (seesaw/scrollable table 
                                                            :border 0
                                                            :vscroll :always
                                                            :hscroll :as-needed)]  ;; Add horizontal scroll as needed
                               ;; Set preferred viewport size for ~20 rows
                               (doto (.getViewport scroll-pane)
                                 (.setPreferredSize (Dimension. 0 (* 22 (.getRowHeight table)))))
                               scroll-pane)
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
  (seesaw/invoke-now
   (let [log-files (get-log-files)]
     (if (empty? log-files)
       (seesaw/alert (i18n/tr :log-viewer-no-files))
       (let [frame (create-log-viewer-frame)]
         (.setExtendedState frame Frame/MAXIMIZED_BOTH)
         (.setLocationRelativeTo frame nil)
         (seesaw/show! frame))))))