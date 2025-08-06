(ns test.roundtrip.cmd.lira
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import cmd.Lira.JonSharedCmdLira$Root
           cmd.Lira.JonSharedCmdLira$Refine_target
           cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget))

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
    [:oneof {:refine-target [:map [:refine-target :cmd.lira/refine-target]]}]]])

(def refine-target-spec
  "Malli spec for refine-target message"
  [:map [:target [:maybe :cmd.lira/jon-gui-data-lira-target]]])

(def jon-gui-data-lira-target-spec
  "Malli spec for jon-gui-data-lira-target message"
  [:map [:timestamp [:maybe :int]] [:target-longitude [:maybe :double]]
   [:target-latitude [:maybe :double]] [:target-altitude [:maybe :double]]
   [:target-azimuth [:maybe :double]] [:target-elevation [:maybe :double]]
   [:distance [:maybe :double]] [:uuid-part-1 [:maybe :int]]
   [:uuid-part-2 [:maybe :int]] [:uuid-part-3 [:maybe :int]]
   [:uuid-part-4 [:maybe :int]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-refine-target)
(declare build-jon-gui-data-lira-target)
(declare parse-root)
(declare parse-refine-target)
(declare parse-jon-gui-data-lira-target)
(declare build-root-payload)
(declare parse-root-payload)

(>defn build-root
       "Build a Root protobuf message from a map."
       [m]
       [root-spec => #(instance? cmd.Lira.JonSharedCmdLira$Root %)]
       (let [builder (cmd.Lira.JonSharedCmdLira$Root/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first (filter (fn [[k v]] (#{:refine-target} k))
                                       m))]
           (build-root-payload builder cmd-field))
         (.build builder)))

(>defn
  build-refine-target
  "Build a Refine_target protobuf message from a map."
  [m]
  [refine-target-spec => #(instance? cmd.Lira.JonSharedCmdLira$Refine_target %)]
  (let [builder (cmd.Lira.JonSharedCmdLira$Refine_target/newBuilder)]
    ;; Set regular fields
    (when (contains? m :target)
      (.setTarget builder (build-jon-gui-data-lira-target (get m :target))))
    (.build builder)))

(>defn
  build-jon-gui-data-lira-target
  "Build a JonGuiDataLiraTarget protobuf message from a map."
  [m]
  [jon-gui-data-lira-target-spec =>
   #(instance? cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget %)]
  (let [builder (cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget/newBuilder)]
    ;; Set regular fields
    (when (contains? m :timestamp) (.setTimestamp builder (get m :timestamp)))
    (when (contains? m :target-longitude)
      (.setTargetLongitude builder (get m :target-longitude)))
    (when (contains? m :target-latitude)
      (.setTargetLatitude builder (get m :target-latitude)))
    (when (contains? m :target-altitude)
      (.setTargetAltitude builder (get m :target-altitude)))
    (when (contains? m :target-azimuth)
      (.setTargetAzimuth builder (get m :target-azimuth)))
    (when (contains? m :target-elevation)
      (.setTargetElevation builder (get m :target-elevation)))
    (when (contains? m :distance) (.setDistance builder (get m :distance)))
    (when (contains? m :uuid-part-1)
      (.setUuidPart1 builder (get m :uuid-part-1)))
    (when (contains? m :uuid-part-2)
      (.setUuidPart2 builder (get m :uuid-part-2)))
    (when (contains? m :uuid-part-3)
      (.setUuidPart3 builder (get m :uuid-part-3)))
    (when (contains? m :uuid-part-4)
      (.setUuidPart4 builder (get m :uuid-part-4)))
    (.build builder)))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.Lira.JonSharedCmdLira$Root proto]
       [#(instance? cmd.Lira.JonSharedCmdLira$Root %) => root-spec]
       (cond-> {}
         ;; Oneof payload
         true (merge (parse-root-payload proto))))

(>defn parse-refine-target
       "Parse a Refine_target protobuf message to a map."
       [^cmd.Lira.JonSharedCmdLira$Refine_target proto]
       [#(instance? cmd.Lira.JonSharedCmdLira$Refine_target %) =>
        refine-target-spec]
       (cond-> {}
         ;; Regular fields
         (.hasTarget proto)
           (assoc :target (parse-jon-gui-data-lira-target (.getTarget proto)))))

(>defn parse-jon-gui-data-lira-target
       "Parse a JonGuiDataLiraTarget protobuf message to a map."
       [^cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget proto]
       [#(instance? cmd.Lira.JonSharedCmdLira$JonGuiDataLiraTarget %) =>
        jon-gui-data-lira-target-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :timestamp (.getTimestamp proto))
         true (assoc :target-longitude (.getTargetLongitude proto))
         true (assoc :target-latitude (.getTargetLatitude proto))
         true (assoc :target-altitude (.getTargetAltitude proto))
         true (assoc :target-azimuth (.getTargetAzimuth proto))
         true (assoc :target-elevation (.getTargetElevation proto))
         true (assoc :distance (.getDistance proto))
         true (assoc :uuid-part-1 (.getUuidPart1 proto))
         true (assoc :uuid-part-2 (.getUuidPart2 proto))
         true (assoc :uuid-part-3 (.getUuidPart3 proto))
         true (assoc :uuid-part-4 (.getUuidPart4 proto))))

(>defn-
  build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  [#(instance? cmd.Lira.JonSharedCmdLira$Root$Builder %) [:tuple keyword? any?]
   => #(instance? cmd.Lira.JonSharedCmdLira$Root$Builder %)]
  (case field-key
    :refine-target (.setRefineTarget builder (build-refine-target value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(>defn- parse-root-payload
        "Parse the oneof payload from Root."
        [^cmd.Lira.JonSharedCmdLira$Root proto]
        [#(instance? cmd.Lira.JonSharedCmdLira$Root %) => (? map?)]
        (cond (.hasRefineTarget proto) {:refine-target (parse-refine-target
                                                         (.getRefineTarget
                                                           proto))}))