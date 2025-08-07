(ns test.roundtrip.cmd.lrf-calib
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import cmd.Lrf_calib.JonSharedCmdLrfAlign$Root
           cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets
           cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets
           cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy
           cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets
           cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets))

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
   [:channel
    [:oneof
     {:day [:map [:day :cmd.lrf-calib/offsets]],
      :heat [:map [:heat :cmd.lrf-calib/offsets]]}]]])

(def offsets-spec
  "Malli spec for offsets message"
  [:map
   [:cmd
    [:oneof
     {:set [:map [:set :cmd.lrf-calib/set-offsets]],
      :save [:map [:save :cmd.lrf-calib/save-offsets]],
      :reset [:map [:reset :cmd.lrf-calib/reset-offsets]],
      :shift [:map [:shift :cmd.lrf-calib/shift-offsets-by]],
      :error/message "This oneof field is required"}]]])

(def set-offsets-spec
  "Malli spec for set-offsets message"
  [:map [:x [:maybe :int]] [:y [:maybe :int]]])

(def shift-offsets-by-spec
  "Malli spec for shift-offsets-by message"
  [:map [:x [:maybe :int]] [:y [:maybe :int]]])

(def reset-offsets-spec "Malli spec for reset-offsets message" [:map])

(def save-offsets-spec "Malli spec for save-offsets message" [:map])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-offsets)
(declare build-set-offsets)
(declare build-shift-offsets-by)
(declare build-reset-offsets)
(declare build-save-offsets)
(declare parse-root)
(declare parse-offsets)
(declare parse-set-offsets)
(declare parse-shift-offsets-by)
(declare parse-reset-offsets)
(declare parse-save-offsets)
(declare build-root-payload)
(declare build-offsets-payload)
(declare parse-root-payload)
(declare parse-offsets-payload)

(>defn build-root
       "Build a Root protobuf message from a map."
       [m]
       [root-spec => any?]
       (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$Root/newBuilder)]
         ;; Handle oneof: channel
         (when-let [channel-field (first (filter (fn [[k v]] (#{:day :heat} k))
                                           (:channel m)))]
           (build-root-channel builder channel-field))
         (.build builder)))

(>defn build-offsets
       "Build a Offsets protobuf message from a map."
       [m]
       [offsets-spec => any?]
       (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first (filter (fn [[k v]]
                                               (#{:set :save :reset :shift} k))
                                       (:cmd m)))]
           (build-offsets-cmd builder cmd-field))
         (.build builder)))

(>defn build-set-offsets
       "Build a SetOffsets protobuf message from a map."
       [m]
       [set-offsets-spec => any?]
       (let [builder (cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets/newBuilder)]
         ;; Set regular fields
         (when (contains? m :x) (.setX builder (get m :x)))
         (when (contains? m :y) (.setY builder (get m :y)))
         (.build builder)))

(>defn build-shift-offsets-by
       "Build a ShiftOffsetsBy protobuf message from a map."
       [m]
       [shift-offsets-by-spec => any?]
       (let [builder
               (cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy/newBuilder)]
         ;; Set regular fields
         (when (contains? m :x) (.setX builder (get m :x)))
         (when (contains? m :y) (.setY builder (get m :y)))
         (.build builder)))

(>defn build-reset-offsets
       "Build a ResetOffsets protobuf message from a map."
       [m]
       [reset-offsets-spec => any?]
       (let [builder
               (cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets/newBuilder)]
         (.build builder)))

(>defn build-save-offsets
       "Build a SaveOffsets protobuf message from a map."
       [m]
       [save-offsets-spec => any?]
       (let [builder
               (cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets/newBuilder)]
         (.build builder)))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Root proto]
       [any? => root-spec]
       (cond-> {}
         ;; Oneof payload
         true (merge (parse-root-payload proto))))

(>defn parse-offsets
       "Parse a Offsets protobuf message to a map."
       [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets proto]
       [any? => offsets-spec]
       (cond-> {}
         ;; Oneof payload
         true (merge (parse-offsets-payload proto))))

(>defn parse-set-offsets
       "Parse a SetOffsets protobuf message to a map."
       [^cmd.Lrf_calib.JonSharedCmdLrfAlign$SetOffsets proto]
       [any? => set-offsets-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :x (.getX proto))
         true (assoc :y (.getY proto))))

(>defn parse-shift-offsets-by
       "Parse a ShiftOffsetsBy protobuf message to a map."
       [^cmd.Lrf_calib.JonSharedCmdLrfAlign$ShiftOffsetsBy proto]
       [any? => shift-offsets-by-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :x (.getX proto))
         true (assoc :y (.getY proto))))

(>defn parse-reset-offsets
       "Parse a ResetOffsets protobuf message to a map."
       [^cmd.Lrf_calib.JonSharedCmdLrfAlign$ResetOffsets proto]
       [any? => reset-offsets-spec]
       {})

(>defn parse-save-offsets
       "Parse a SaveOffsets protobuf message to a map."
       [^cmd.Lrf_calib.JonSharedCmdLrfAlign$SaveOffsets proto]
       [any? => save-offsets-spec]
       {})

(>defn- build-root-payload
        "Build the oneof payload for Root."
        [builder [field-key value]]
        [any? [:tuple keyword? any?] => any?]
        (case field-key
          :day (.setDay builder (build-offsets value))
          :heat (.setHeat builder (build-offsets value))
          (throw (ex-info "Unknown oneof field"
                          {:field field-key, :oneof ":channel"}))))

(>defn- build-offsets-payload
        "Build the oneof payload for Offsets."
        [builder [field-key value]]
        [any? [:tuple keyword? any?] => any?]
        (case field-key
          :set (.setSet builder (build-set-offsets value))
          :save (.setSave builder (build-save-offsets value))
          :reset (.setReset builder (build-reset-offsets value))
          :shift (.setShift builder (build-shift-offsets-by value))
          (throw (ex-info "Unknown oneof field"
                          {:field field-key, :oneof ":cmd"}))))

(>defn- parse-root-payload
        "Parse the oneof payload from Root."
        [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Root proto]
        [any? => (? map?)]
        (cond (.hasDay proto) {:day (parse-offsets (.getDay proto))}
              (.hasHeat proto) {:heat (parse-offsets (.getHeat proto))}))

(>defn- parse-offsets-payload
        "Parse the oneof payload from Offsets."
        [^cmd.Lrf_calib.JonSharedCmdLrfAlign$Offsets proto]
        [any? => (? map?)]
        (cond (.hasSet proto) {:set (parse-set-offsets (.getSet proto))}
              (.hasSave proto) {:save (parse-save-offsets (.getSave proto))}
              (.hasReset proto) {:reset (parse-reset-offsets (.getReset proto))}
              (.hasShift proto) {:shift (parse-shift-offsets-by (.getShift
                                                                  proto))}))
;; =============================================================================
;; Validation Helper Functions
;; =============================================================================

;; Validation helpers for SetOffsets
;; Warning: Could not extract spec for field x

;; Warning: Could not extract spec for field y

;; Validation helpers for ShiftOffsetsBy
;; Warning: Could not extract spec for field x

;; Warning: Could not extract spec for field y