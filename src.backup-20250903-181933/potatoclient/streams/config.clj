(ns potatoclient.streams.config
  "Stream configuration and endpoints."
  (:require
    [potatoclient.config :as config]))

;; ============================================================================
;; Stream Types
;; ============================================================================

(def stream-types
  "Valid stream types"
  #{:heat :day})

(defn valid-stream-type?
  "Check if stream type is valid"
  {:malli/schema [:=> [:cat :keyword] :boolean]}
  [stream-type]
  (contains? stream-types stream-type))

;; ============================================================================
;; Stream Endpoints
;; ============================================================================

(def stream-endpoints
  "WebSocket endpoints for each stream type"
  {:heat "/ws/ws_rec_video_heat"
   :day "/ws/ws_rec_video_day"})

(defn get-stream-endpoint
  "Get WebSocket endpoint for stream type"
  {:malli/schema [:=> [:cat :keyword] [:maybe :string]]}
  [stream-type]
  (get stream-endpoints stream-type))

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
  {:malli/schema [:=> [:cat :keyword] [:maybe :map]]}
  [stream-type]
  (get stream-config stream-type))

(defn get-process-key
  "Get process key for stream type (used in app state)"
  {:malli/schema [:=> [:cat :keyword] [:maybe :keyword]]}
  [stream-type]
  (get-in stream-config [stream-type :process-key]))

;; ============================================================================
;; Host Configuration
;; ============================================================================

(defn get-stream-host
  "Get host for stream connections"
  {:malli/schema [:=> [:cat] :string]}
  []
  (or (config/get-domain) "localhost"))

(defn build-stream-url
  "Build complete WebSocket URL for stream"
  {:malli/schema [:=> [:cat :keyword] :string]}
  [stream-type]
  (let [host (get-stream-host)
        endpoint (get-stream-endpoint stream-type)]
    (str "wss://" host endpoint)))

;; ============================================================================
;; Process Configuration
;; ============================================================================

(defn get-java-command
  "Get Java command for running streams"
  {:malli/schema [:=> [:cat] :string]}
  []
  "java")

(defn get-classpath
  "Get classpath for VideoStreamManager using clojure -Spath"
  {:malli/schema [:=> [:cat] :string]}
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

(defn get-main-class
  "Get main class for VideoStreamManager"
  {:malli/schema [:=> [:cat] :string]}
  []
  "potatoclient.kotlin.VideoStreamManager")

(defn get-debug-flag
  "Check if debug mode is enabled"
  {:malli/schema [:=> [:cat] :boolean]}
  []
  (not (potatoclient.runtime/release-build?)))