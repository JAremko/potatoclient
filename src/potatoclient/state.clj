(ns potatoclient.state
  "Application state management for the Potato Client.
  Provides centralized state atoms and state manipulation functions.")

;; Core application state
(defonce app-state 
  ;; Main application state atom containing stream processes
  (atom {:heat nil 
         :day nil}))

;; Log data
(defonce log-entries
  ;; Collection of all log entries
  (atom []))

(defonce log-buffer
  ;; Temporary buffer for batching log updates
  (atom []))

(defonce update-scheduled
  ;; Flag to track if a log update is already scheduled
  (atom false))

;; Configuration
(defonce config
  ;; Application configuration
  (atom {:domain "sych.local"}))

;; UI component references
(defonce ui-elements
  ;; References to UI components that need updates
  (atom {}))

;; State accessors
(defn get-stream 
  "Get a stream process by key (:heat or :day)"
  [stream-key]
  (get @app-state stream-key))

(defn set-stream! 
  "Set a stream process"
  [stream-key stream]
  (swap! app-state assoc stream-key stream))

(defn clear-stream! 
  "Clear a stream process"
  [stream-key]
  (swap! app-state assoc stream-key nil))

(defn get-domain 
  "Get the current domain configuration"
  []
  (:domain @config))

(defn set-domain! 
  "Update the domain configuration"
  [domain]
  (swap! config assoc :domain domain))

(defn add-log-entry!
  "Add a log entry to the buffer"
  [entry]
  (swap! log-buffer conj entry)
  ;; Keep buffer size limited
  (when (> (count @log-buffer) 200)
    (swap! log-buffer (fn [v] (vec (drop 100 v))))))

(defn flush-log-buffer!
  "Flush log buffer to main log entries"
  []
  (let [entries @log-buffer]
    (when (seq entries)
      (reset! log-buffer [])
      (swap! log-entries (fn [current]
                          (vec (take 100 (concat entries current))))))))

(defn clear-logs!
  "Clear all log entries"
  []
  (reset! log-entries []))

(defn get-ui-element
  "Get a UI element reference by key"
  [element-key]
  (get @ui-elements element-key))

(defn register-ui-element!
  "Register a UI element for later updates"
  [element-key element]
  (swap! ui-elements assoc element-key element))