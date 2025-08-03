(ns potatoclient.transit.metadata-handler
  "Custom Transit handlers for preserving protobuf type metadata.
  
  Since Transit doesn't preserve Clojure metadata, we embed the metadata
  as part of the message structure using a special key."
  (:require [cognitect.transit :as transit]
            [clojure.java.io :as io])
  (:import [com.cognitect.transit WriteHandler ReadHandler]
           [java.io ByteArrayOutputStream ByteArrayInputStream]))

;; =============================================================================
;; Metadata Embedding Strategy
;; =============================================================================

(def ^:const metadata-key ::proto-meta)

(defn embed-metadata
  "Embed Clojure metadata into the data structure for Transit.
  Uses a special key that won't conflict with actual data."
  [data]
  (if-let [m (meta data)]
    (assoc data metadata-key m)
    data))

(defn extract-metadata
  "Extract embedded metadata and attach it back to the data."
  [data]
  (if-let [m (get data metadata-key)]
    (with-meta (dissoc data metadata-key) m)
    data))

;; =============================================================================
;; Custom Write Handler for Commands with Metadata
;; =============================================================================

(deftype CommandWriteHandler []
  WriteHandler
  (tag [_ _] "cmd")
  (rep [_ command]
    ;; Embed metadata into the command structure
    (embed-metadata command))
  (stringRep [_ _] nil)
  (getVerboseHandler [_] nil))

;; =============================================================================
;; Custom Read Handler for Commands with Metadata
;; =============================================================================

(deftype CommandReadHandler []
  ReadHandler
  (fromRep [_ rep]
    ;; Extract metadata and reattach it
    (extract-metadata rep)))

;; =============================================================================
;; Transit Reader/Writer Creation
;; =============================================================================

(defn write-handlers
  "Create write handlers that preserve metadata."
  []
  {;; Could add specific types here if needed
   ;; For now, we handle embedding at the application level
   })

(defn read-handlers
  "Create read handlers that restore metadata."
  []
  {"cmd" (CommandReadHandler.)})

(defn writer
  "Create a Transit writer that handles metadata."
  ([out type]
   (transit/writer out type {:handlers (write-handlers)}))
  ([out type opts]
   (transit/writer out type (update opts :handlers merge (write-handlers)))))

(defn reader
  "Create a Transit reader that handles metadata."
  ([in type]
   (transit/reader in type {:handlers (read-handlers)}))
  ([in type opts]
   (transit/reader in type (update opts :handlers merge (read-handlers)))))

;; =============================================================================
;; Encoding/Decoding Functions
;; =============================================================================

(defn encode-with-metadata
  "Encode data to Transit, preserving metadata."
  [data]
  (let [out (ByteArrayOutputStream.)
        w (writer out :msgpack)]
    (transit/write w (embed-metadata data))
    (.toByteArray out)))

(defn decode-with-metadata
  "Decode Transit data, restoring metadata."
  [bytes]
  (let [in (ByteArrayInputStream. bytes)
        r (reader in :msgpack)]
    (extract-metadata (transit/read r))))

;; =============================================================================
;; Alternative: Type-Tagged Approach
;; =============================================================================

(defn wrap-with-type
  "Wrap a command with explicit type information.
  This approach doesn't use metadata but explicit structure.
  
  Note: proto-path is optional. The protobuf type alone is sufficient
  for building the correct message. The path can be useful for debugging
  or tracing but is not required for the actual protobuf construction."
  ([command proto-type]
   (wrap-with-type command proto-type nil))
  ([command proto-type proto-path]
   {:type :protobuf-command
    :proto-type proto-type
    :proto-path proto-path
    :data command}))

(defn unwrap-typed-command
  "Extract command data and type information."
  [wrapped]
  (when (= (:type wrapped) :protobuf-command)
    {:data (:data wrapped)
     :proto-type (:proto-type wrapped)
     :proto-path (:proto-path wrapped)}))

;; =============================================================================
;; Kotlin Integration Helpers
;; =============================================================================

(defn prepare-for-kotlin
  "Prepare a command for sending to Kotlin subprocess.
  This embeds all necessary type information.
  Proto-path is optional - only proto-type is required."
  ([command proto-type]
   (prepare-for-kotlin command proto-type nil))
  ([command proto-type proto-path]
   ;; Use the explicit structure approach for clarity
   (wrap-with-type command proto-type proto-path)))

(defn create-command-message
  "Create a complete Transit message with embedded type info.
  Proto-path is optional and mainly useful for debugging."
  ([command proto-type]
   (create-command-message command proto-type nil))
  ([command proto-type proto-path]
   {:msg-type "command"
    :msg-id (str (random-uuid))
    :timestamp (System/currentTimeMillis)
    :payload (prepare-for-kotlin command proto-type proto-path)}))

;; =============================================================================
;; Usage Examples
;; =============================================================================

(comment
  ;; Original approach with metadata
  (let [command {:goto {:azimuth 180.0 :elevation 45.0}}
        with-meta (with-meta command 
                            {:proto-type "cmd.JonSharedCmdRotaryPlatform$Root"
                             :proto-path [:rotary-platform :goto]})
        encoded (encode-with-metadata with-meta)
        decoded (decode-with-metadata encoded)]
    (println "Original:" command)
    (println "With meta:" with-meta)
    (println "Decoded:" decoded)
    (println "Decoded meta:" (meta decoded)))
  
  ;; Type-tagged approach (more explicit)
  (let [command {:goto {:azimuth 180.0 :elevation 45.0}}
        wrapped (wrap-with-type command 
                               "cmd.JonSharedCmdRotaryPlatform$Root"
                               [:rotary-platform :goto])
        encoded (encode-with-metadata wrapped)
        decoded (decode-with-metadata encoded)]
    (println "Wrapped:" wrapped)
    (println "Unwrapped:" (unwrap-typed-command decoded)))
  
  ;; Full message for Kotlin
  (let [msg (create-command-message
             {:goto {:azimuth 90.0 :elevation 30.0}}
             "cmd.JonSharedCmdRotaryPlatform$Root"
             [:rotary-platform :goto])]
    (println "Full message:" msg))
  )