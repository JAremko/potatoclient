(ns potatoclient.streams.config
  "Stream configuration and endpoints."
  (:require
            [malli.core :as m]
    [potatoclient.config :as config]))

;; ============================================================================
;; Stream Types
;; ============================================================================

(def stream-types
  "Valid stream types"
  #{:heat :day})

(defn valid-stream-type?
  "Check if stream type is valid"
  [stream-type]
  (contains? stream-types stream-type)) 
 (m/=> valid-stream-type? [:=> [:cat :keyword] :boolean])

;; ============================================================================
;; Stream Endpoints
;; ============================================================================

(def stream-endpoints
  "WebSocket endpoints for each stream type"
  {:heat "/ws/ws_rec_video_heat"
   :day "/ws/ws_rec_video_day"})

(defn get-stream-endpoint
  "Get WebSocket endpoint for stream type"
  [stream-type]
  (get stream-endpoints stream-type)) 
 (m/=> get-stream-endpoint [:=> [:cat :keyword] [:maybe :string]])

;; ============================================================================
;; Stream Configuration
;; ============================================================================

(def stream-config
  "Configuration for each stream type"
  {:heat {:name "Thermal Camera"
          :width 900
          :height 720
          :process-key :heat-video}
   :day {:name "Day Camera"
         :width 1920
         :height 1080
         :process-key :day-video}})

(defn get-stream-config
  "Get configuration for stream type"
  [stream-type]
  (get stream-config stream-type)) 
 (m/=> get-stream-config [:=> [:cat :keyword] [:maybe :map]])

(defn get-process-key
  "Get process key for stream type (used in app state)"
  [stream-type]
  (get-in stream-config [stream-type :process-key])) 
 (m/=> get-process-key [:=> [:cat :keyword] [:maybe :keyword]])

;; ============================================================================
;; Host Configuration
;; ============================================================================

(defn get-stream-host
  "Get host for stream connections"
  []
  (or (config/get-domain) "localhost")) 
 (m/=> get-stream-host [:=> [:cat] :string])

(defn build-stream-url
  "Build complete WebSocket URL for stream"
  [stream-type]
  (let [host (get-stream-host)
        endpoint (get-stream-endpoint stream-type)]
    (str "wss://" host endpoint))) 
 (m/=> build-stream-url [:=> [:cat :keyword] :string])

;; ============================================================================
;; Process Configuration
;; ============================================================================

(defn get-java-command
  "Get Java command for running streams"
  []
  "java") 
 (m/=> get-java-command [:=> [:cat] :string])

(defn get-classpath
  "Get classpath for VideoStreamManager using clojure -Spath"
  []
  ;; Get the full classpath from the main project
  (let [project-root (System/getProperty "user.dir")
        pb (java.lang.ProcessBuilder. ["clojure" "-Spath"])
        _ (.directory pb (java.io.File. project-root))
        process (.start pb)]
    (.waitFor process)
    (if (zero? (.exitValue process))
      (let [classpath (slurp (.getInputStream process))]
        ;; Add target directories to classpath
        (str (clojure.string/trim classpath)
             ":" project-root "/target/classes"
             ":" project-root "/target/java-classes"
             ":" project-root "/target/kotlin/classes"))
      ;; Fallback to manual classpath
      (str project-root "/target/classes:"
           project-root "/target/java-classes:"
           project-root "/target/kotlin/classes:"
           project-root "/lib/*")))) 
 (m/=> get-classpath [:=> [:cat] :string])

(defn get-main-class
  "Get main class for VideoStreamManager"
  []
  "potatoclient.kotlin.VideoStreamManager") 
 (m/=> get-main-class [:=> [:cat] :string])

(defn get-debug-flag
  "Check if debug mode is enabled"
  []
  (not (potatoclient.runtime/release-build?))) 
 (m/=> get-debug-flag [:=> [:cat] :boolean])