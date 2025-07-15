(ns potatoclient.ui.log-table
  "Log table UI component for displaying stream events"
  (:require [clojure.data.json :as json]
            [potatoclient.state :as state]
            [potatoclient.i18n :as i18n]
            [malli.core :as m]
            [potatoclient.specs :as specs])
  (:use [seesaw core table])
  (:import [javax.swing.table DefaultTableCellRenderer]
           [java.awt Color]))

(defn ^:private get-type-color
  "Get the background color for a log entry based on its type"
  [type event-type nav-type]
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


(defn ^:private create-type-renderer
  "Create a custom cell renderer for the type column"
  [table-model]
  (proxy [DefaultTableCellRenderer] []
    (getTableCellRendererComponent [table value isSelected hasFocus row column]
      (let [component (proxy-super getTableCellRendererComponent table value isSelected hasFocus row column)
            row-data (value-at table-model row)]
        (when (and row-data (not isSelected))
          (let [type (:type row-data)
                event-type (:event-type row-data)
                nav-type (:nav-type row-data)]
            (.setBackground component (get-type-color type event-type nav-type))))
        component))))


(defn ^:private show-raw-data
  "Show the raw JSON data for a log entry"
  [row-data]
  (when-let [raw-data (:raw-data row-data)]
    (alert (str "JSON Data for " (:type row-data) " event:\n\n"
               (json/write-str raw-data :indent true)))))


(defn create
  "Create the log table UI component"
  []
  (let [columns [{:key :time :text (i18n/tr :log-column-time) 
                  :class java.lang.Long
                  :read (fn [m] (.format (java.text.SimpleDateFormat. "HH:mm:ss.SSS") 
                                        (java.util.Date. (:time m))))
                  :write (fn [m v] m)}
                 {:key :stream :text (i18n/tr :log-column-source)}
                 {:key :type :text (i18n/tr :log-column-type)}
                 {:key :message :text (i18n/tr :log-column-message)}]
        table-model (table-model :columns columns)
        log-table (table :model table-model
                        :show-grid? true)]
    
    ;; Update table when log-entries changes
    (add-watch state/log-entries :table-updater
      (fn [_ _ old-val new-val]
        (when (not= old-val new-val)
          (invoke-later
            (clear! table-model)
            (doseq [row new-val]
              (insert-at! table-model (row-count table-model) row))))))
    
    ;; Set custom renderer for type column
    (let [type-renderer (create-type-renderer table-model)]
      (.setCellRenderer (.getColumn (.getColumnModel log-table) 2) type-renderer))
    
    ;; Add double-click handler for JSON display
    (listen log-table :mouse-clicked
      (fn [e]
        (when (= 2 (.getClickCount e))  ; Double click
          (let [row (.rowAtPoint log-table (.getPoint e))]
            (when (>= row 0)
              (when-let [row-data (value-at table-model row)]
                (show-raw-data row-data)))))))
    
    (scrollable log-table)))

