(ns test.enum.cmd.cv
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            [test.enum.ser :as types])
  (:import cmd.CV.JonSharedCmdCv$Root
           cmd.CV.JonSharedCmdCv$VampireModeEnable
           cmd.CV.JonSharedCmdCv$DumpStart
           cmd.CV.JonSharedCmdCv$DumpStop
           cmd.CV.JonSharedCmdCv$VampireModeDisable
           cmd.CV.JonSharedCmdCv$StabilizationModeEnable
           cmd.CV.JonSharedCmdCv$StabilizationModeDisable
           cmd.CV.JonSharedCmdCv$SetAutoFocus
           cmd.CV.JonSharedCmdCv$StartTrackNDC
           cmd.CV.JonSharedCmdCv$StopTrack))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Malli Specs
;; =============================================================================

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:oneof
     {:vampire-mode-enable [:map
                            [:vampire-mode-enable :cmd.cv/vampire-mode-enable]],
      :vampire-mode-disable
        [:map [:vampire-mode-disable :cmd.cv/vampire-mode-disable]],
      :error/message "This oneof field is required",
      :dump-stop [:map [:dump-stop :cmd.cv/dump-stop]],
      :stabilization-mode-disable
        [:map [:stabilization-mode-disable :cmd.cv/stabilization-mode-disable]],
      :set-auto-focus [:map [:set-auto-focus :cmd.cv/set-auto-focus]],
      :start-track-ndc [:map [:start-track-ndc :cmd.cv/start-track-ndc]],
      :dump-start [:map [:dump-start :cmd.cv/dump-start]],
      :stop-track [:map [:stop-track :cmd.cv/stop-track]],
      :stabilization-mode-enable [:map
                                  [:stabilization-mode-enable
                                   :cmd.cv/stabilization-mode-enable]]}]]])

(def vampire-mode-enable-spec
  "Malli spec for vampire-mode-enable message"
  [:map])

(def dump-start-spec "Malli spec for dump-start message" [:map])

(def dump-stop-spec "Malli spec for dump-stop message" [:map])

(def vampire-mode-disable-spec
  "Malli spec for vampire-mode-disable message"
  [:map])

(def stabilization-mode-enable-spec
  "Malli spec for stabilization-mode-enable message"
  [:map])

(def stabilization-mode-disable-spec
  "Malli spec for stabilization-mode-disable message"
  [:map])

(def set-auto-focus-spec
  "Malli spec for set-auto-focus message"
  [:map [:channel {:optional true} :ser/jon-gui-data-video-channel]
   [:value {:optional true} :boolean]])

(def start-track-ndc-spec
  "Malli spec for start-track-ndc message"
  [:map [:channel {:optional true} :ser/jon-gui-data-video-channel]
   [:x {:optional true} [:and :float [:>= -1] [:<= 1]]]
   [:y {:optional true} [:and :float [:>= -1] [:<= 1]]]
   [:frame-time {:optional true} :int]])

(def stop-track-spec "Malli spec for stop-track message" [:map])

;; =============================================================================
;; Registry Setup
;; =============================================================================

;; Registry for enum and message specs in this namespace
(def registry
  {:cmd.CV/root root-spec,
   :cmd.CV/vampire-mode-enable vampire-mode-enable-spec,
   :cmd.CV/dump-start dump-start-spec,
   :cmd.CV/dump-stop dump-stop-spec,
   :cmd.CV/vampire-mode-disable vampire-mode-disable-spec,
   :cmd.CV/stabilization-mode-enable stabilization-mode-enable-spec,
   :cmd.CV/stabilization-mode-disable stabilization-mode-disable-spec,
   :cmd.CV/set-auto-focus set-auto-focus-spec,
   :cmd.CV/start-track-ndc start-track-ndc-spec,
   :cmd.CV/stop-track stop-track-spec})

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-vampire-mode-enable)
(declare build-dump-start)
(declare build-dump-stop)
(declare build-vampire-mode-disable)
(declare build-stabilization-mode-enable)
(declare build-stabilization-mode-disable)
(declare build-set-auto-focus)
(declare build-start-track-ndc)
(declare build-stop-track)
(declare parse-root)
(declare parse-vampire-mode-enable)
(declare parse-dump-start)
(declare parse-dump-stop)
(declare parse-vampire-mode-disable)
(declare parse-stabilization-mode-enable)
(declare parse-stabilization-mode-disable)
(declare parse-set-auto-focus)
(declare parse-start-track-ndc)
(declare parse-stop-track)
(declare build-root-payload)
(declare parse-root-payload)

(>defn build-root
       "Build a Root protobuf message from a map."
       [m]
       [root-spec => any?]
       (let [builder (cmd.CV.JonSharedCmdCv$Root/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first (filter (fn [[k v]]
                                               (#{:set-auto-focus
                                                  :start-track-ndc :stop-track
                                                  :vampire-mode-enable
                                                  :vampire-mode-disable
                                                  :stabilization-mode-enable
                                                  :stabilization-mode-disable
                                                  :dump-start :dump-stop}
                                                k))
                                       (:cmd m)))]
           (build-root-payload builder cmd-field))
         (.build builder)))

(>defn build-vampire-mode-enable
       "Build a VampireModeEnable protobuf message from a map."
       [m]
       [vampire-mode-enable-spec => any?]
       (let [builder (cmd.CV.JonSharedCmdCv$VampireModeEnable/newBuilder)]
         (.build builder)))

(>defn build-dump-start
       "Build a DumpStart protobuf message from a map."
       [m]
       [dump-start-spec => any?]
       (let [builder (cmd.CV.JonSharedCmdCv$DumpStart/newBuilder)]
         (.build builder)))

(>defn build-dump-stop
       "Build a DumpStop protobuf message from a map."
       [m]
       [dump-stop-spec => any?]
       (let [builder (cmd.CV.JonSharedCmdCv$DumpStop/newBuilder)]
         (.build builder)))

(>defn build-vampire-mode-disable
       "Build a VampireModeDisable protobuf message from a map."
       [m]
       [vampire-mode-disable-spec => any?]
       (let [builder (cmd.CV.JonSharedCmdCv$VampireModeDisable/newBuilder)]
         (.build builder)))

(>defn build-stabilization-mode-enable
       "Build a StabilizationModeEnable protobuf message from a map."
       [m]
       [stabilization-mode-enable-spec => any?]
       (let [builder (cmd.CV.JonSharedCmdCv$StabilizationModeEnable/newBuilder)]
         (.build builder)))

(>defn build-stabilization-mode-disable
       "Build a StabilizationModeDisable protobuf message from a map."
       [m]
       [stabilization-mode-disable-spec => any?]
       (let [builder
               (cmd.CV.JonSharedCmdCv$StabilizationModeDisable/newBuilder)]
         (.build builder)))

(>defn build-set-auto-focus
       "Build a SetAutoFocus protobuf message from a map."
       [m]
       [set-auto-focus-spec => any?]
       (let [builder (cmd.CV.JonSharedCmdCv$SetAutoFocus/newBuilder)]
         ;; Set regular fields
         (when (contains? m :channel)
           (.setChannel builder
                        (when-let [v (get m :channel)]
                          (get types/jon-gui-data-video-channel-values v))))
         (when (contains? m :value) (.setValue builder (get m :value)))
         (.build builder)))

(>defn build-start-track-ndc
       "Build a StartTrackNDC protobuf message from a map."
       [m]
       [start-track-ndc-spec => any?]
       (let [builder (cmd.CV.JonSharedCmdCv$StartTrackNDC/newBuilder)]
         ;; Set regular fields
         (when (contains? m :channel)
           (.setChannel builder
                        (when-let [v (get m :channel)]
                          (get types/jon-gui-data-video-channel-values v))))
         (when (contains? m :x) (.setX builder (get m :x)))
         (when (contains? m :y) (.setY builder (get m :y)))
         (when (contains? m :frame-time)
           (.setFrameTime builder (get m :frame-time)))
         (.build builder)))

(>defn build-stop-track
       "Build a StopTrack protobuf message from a map."
       [m]
       [stop-track-spec => any?]
       (let [builder (cmd.CV.JonSharedCmdCv$StopTrack/newBuilder)]
         (.build builder)))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.CV.JonSharedCmdCv$Root proto]
       [any? => root-spec]
       (cond-> {}
         ;; Oneof: cmd
         (parse-root-payload proto) (assoc :cmd (parse-root-payload proto))))

(>defn parse-vampire-mode-enable
       "Parse a VampireModeEnable protobuf message to a map."
       [^cmd.CV.JonSharedCmdCv$VampireModeEnable proto]
       [any? => vampire-mode-enable-spec]
       {})

(>defn parse-dump-start
       "Parse a DumpStart protobuf message to a map."
       [^cmd.CV.JonSharedCmdCv$DumpStart proto]
       [any? => dump-start-spec]
       {})

(>defn parse-dump-stop
       "Parse a DumpStop protobuf message to a map."
       [^cmd.CV.JonSharedCmdCv$DumpStop proto]
       [any? => dump-stop-spec]
       {})

(>defn parse-vampire-mode-disable
       "Parse a VampireModeDisable protobuf message to a map."
       [^cmd.CV.JonSharedCmdCv$VampireModeDisable proto]
       [any? => vampire-mode-disable-spec]
       {})

(>defn parse-stabilization-mode-enable
       "Parse a StabilizationModeEnable protobuf message to a map."
       [^cmd.CV.JonSharedCmdCv$StabilizationModeEnable proto]
       [any? => stabilization-mode-enable-spec]
       {})

(>defn parse-stabilization-mode-disable
       "Parse a StabilizationModeDisable protobuf message to a map."
       [^cmd.CV.JonSharedCmdCv$StabilizationModeDisable proto]
       [any? => stabilization-mode-disable-spec]
       {})

(>defn parse-set-auto-focus
       "Parse a SetAutoFocus protobuf message to a map."
       [^cmd.CV.JonSharedCmdCv$SetAutoFocus proto]
       [any? => set-auto-focus-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :channel
                (get types/jon-gui-data-video-channel-keywords
                     (.getChannel proto)))
         true (assoc :value (.getValue proto))))

(>defn parse-start-track-ndc
       "Parse a StartTrackNDC protobuf message to a map."
       [^cmd.CV.JonSharedCmdCv$StartTrackNDC proto]
       [any? => start-track-ndc-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :channel
                (get types/jon-gui-data-video-channel-keywords
                     (.getChannel proto)))
         true (assoc :x (.getX proto))
         true (assoc :y (.getY proto))
         true (assoc :frame-time (.getFrameTime proto))))

(>defn parse-stop-track
       "Parse a StopTrack protobuf message to a map."
       [^cmd.CV.JonSharedCmdCv$StopTrack proto]
       [any? => stop-track-spec]
       {})

(>defn-
  build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  [any? [:tuple keyword? any?] => any?]
  (case field-key
    :set-auto-focus (.setSetAutoFocus builder (build-set-auto-focus value))
    :start-track-ndc (.setStartTrackNdc builder (build-start-track-ndc value))
    :stop-track (.setStopTrack builder (build-stop-track value))
    :vampire-mode-enable
      (.setVampireModeEnable builder (build-vampire-mode-enable value))
    :vampire-mode-disable
      (.setVampireModeDisable builder (build-vampire-mode-disable value))
    :stabilization-mode-enable (.setStabilizationModeEnable
                                 builder
                                 (build-stabilization-mode-enable value))
    :stabilization-mode-disable (.setStabilizationModeDisable
                                  builder
                                  (build-stabilization-mode-disable value))
    :dump-start (.setDumpStart builder (build-dump-start value))
    :dump-stop (.setDumpStop builder (build-dump-stop value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(>defn-
  parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.CV.JonSharedCmdCv$Root proto]
  [any? => (? map?)]
  (cond
    (.hasSetAutoFocus proto) {:set-auto-focus (parse-set-auto-focus
                                                (.getSetAutoFocus proto))}
    (.hasStartTrackNdc proto) {:start-track-ndc (parse-start-track-ndc
                                                  (.getStartTrackNdc proto))}
    (.hasStopTrack proto) {:stop-track (parse-stop-track (.getStopTrack proto))}
    (.hasVampireModeEnable proto) {:vampire-mode-enable
                                     (parse-vampire-mode-enable
                                       (.getVampireModeEnable proto))}
    (.hasVampireModeDisable proto) {:vampire-mode-disable
                                      (parse-vampire-mode-disable
                                        (.getVampireModeDisable proto))}
    (.hasStabilizationModeEnable proto)
      {:stabilization-mode-enable (parse-stabilization-mode-enable
                                    (.getStabilizationModeEnable proto))}
    (.hasStabilizationModeDisable proto)
      {:stabilization-mode-disable (parse-stabilization-mode-disable
                                     (.getStabilizationModeDisable proto))}
    (.hasDumpStart proto) {:dump-start (parse-dump-start (.getDumpStart proto))}
    (.hasDumpStop proto) {:dump-stop (parse-dump-stop (.getDumpStop proto))}))
;; =============================================================================
;; Validation Helper Functions
;; =============================================================================

;; Validation helpers for SetAutoFocus
;; Warning: Could not extract spec for field channel

;; Validation helpers for StartTrackNDC
;; Warning: Could not extract spec for field channel

;; Warning: Could not extract spec for field x

;; Warning: Could not extract spec for field y