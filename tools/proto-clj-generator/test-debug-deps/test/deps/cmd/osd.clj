(ns test.deps.cmd.osd
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m])
  (:import cmd.OSD.JonSharedCmdOsd$Root
           cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen
           cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen
           cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen
           cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen
           cmd.OSD.JonSharedCmdOsd$EnableHeatOSD
           cmd.OSD.JonSharedCmdOsd$DisableHeatOSD
           cmd.OSD.JonSharedCmdOsd$EnableDayOSD
           cmd.OSD.JonSharedCmdOsd$DisableDayOSD))

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
     {:show-default-screen
        [:map [:show-default-screen :cmd.osd/show-default-screen]],
      :show-lrf-measure-screen
        [:map [:show-lrf-measure-screen :cmd.osd/show-lrf-measure-screen]],
      :show-lrf-result-screen
        [:map [:show-lrf-result-screen :cmd.osd/show-lrf-result-screen]],
      :show-lrf-result-simplified-screen
        [:map
         [:show-lrf-result-simplified-screen
          :cmd.osd/show-lrf-result-simplified-screen]],
      :enable-heat-osd [:map [:enable-heat-osd :cmd.osd/enable-heat-osd]],
      :disable-heat-osd [:map [:disable-heat-osd :cmd.osd/disable-heat-osd]],
      :enable-day-osd [:map [:enable-day-osd :cmd.osd/enable-day-osd]],
      :disable-day-osd [:map [:disable-day-osd :cmd.osd/disable-day-osd]]}]]])

(def show-default-screen-spec
  "Malli spec for show-default-screen message"
  [:map])

(def show-lrf-measure-screen-spec
  "Malli spec for show-lrf-measure-screen message"
  [:map])

(def show-lrf-result-screen-spec
  "Malli spec for show-lrf-result-screen message"
  [:map])

(def show-lrf-result-simplified-screen-spec
  "Malli spec for show-lrf-result-simplified-screen message"
  [:map])

(def enable-heat-osd-spec "Malli spec for enable-heat-osd message" [:map])

(def disable-heat-osd-spec "Malli spec for disable-heat-osd message" [:map])

(def enable-day-osd-spec "Malli spec for enable-day-osd message" [:map])

(def disable-day-osd-spec "Malli spec for disable-day-osd message" [:map])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-show-default-screen)
(declare build-show-lrf-measure-screen)
(declare build-show-lrf-result-screen)
(declare build-show-lrf-result-simplified-screen)
(declare build-enable-heat-osd)
(declare build-disable-heat-osd)
(declare build-enable-day-osd)
(declare build-disable-day-osd)
(declare parse-root)
(declare parse-show-default-screen)
(declare parse-show-lrf-measure-screen)
(declare parse-show-lrf-result-screen)
(declare parse-show-lrf-result-simplified-screen)
(declare parse-enable-heat-osd)
(declare parse-disable-heat-osd)
(declare parse-enable-day-osd)
(declare parse-disable-day-osd)
(declare build-root-payload)
(declare parse-root-payload)

(>defn build-root
       "Build a Root protobuf message from a map."
       [m]
       [root-spec => #(instance? cmd.OSD.JonSharedCmdOsd$Root %)]
       (let [builder (cmd.OSD.JonSharedCmdOsd$Root/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first (filter
                                       (fn [[k v]]
                                         (#{:show-default-screen
                                            :show-lrf-measure-screen
                                            :show-lrf-result-screen
                                            :show-lrf-result-simplified-screen
                                            :enable-heat-osd :disable-heat-osd
                                            :enable-day-osd :disable-day-osd}
                                          k))
                                       m))]
           (build-root-payload builder cmd-field))
         (.build builder)))

(>defn build-show-default-screen
       "Build a ShowDefaultScreen protobuf message from a map."
       [m]
       [show-default-screen-spec =>
        #(instance? cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen %)]
       (let [builder (cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen/newBuilder)]
         (.build builder)))

(>defn build-show-lrf-measure-screen
       "Build a ShowLRFMeasureScreen protobuf message from a map."
       [m]
       [show-lrf-measure-screen-spec =>
        #(instance? cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen %)]
       (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen/newBuilder)]
         (.build builder)))

(>defn build-show-lrf-result-screen
       "Build a ShowLRFResultScreen protobuf message from a map."
       [m]
       [show-lrf-result-screen-spec =>
        #(instance? cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen %)]
       (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen/newBuilder)]
         (.build builder)))

(>defn build-show-lrf-result-simplified-screen
       "Build a ShowLRFResultSimplifiedScreen protobuf message from a map."
       [m]
       [show-lrf-result-simplified-screen-spec =>
        #(instance? cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen %)]
       (let
         [builder
            (cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen/newBuilder)]
         (.build builder)))

(>defn build-enable-heat-osd
       "Build a EnableHeatOSD protobuf message from a map."
       [m]
       [enable-heat-osd-spec =>
        #(instance? cmd.OSD.JonSharedCmdOsd$EnableHeatOSD %)]
       (let [builder (cmd.OSD.JonSharedCmdOsd$EnableHeatOSD/newBuilder)]
         (.build builder)))

(>defn build-disable-heat-osd
       "Build a DisableHeatOSD protobuf message from a map."
       [m]
       [disable-heat-osd-spec =>
        #(instance? cmd.OSD.JonSharedCmdOsd$DisableHeatOSD %)]
       (let [builder (cmd.OSD.JonSharedCmdOsd$DisableHeatOSD/newBuilder)]
         (.build builder)))

(>defn build-enable-day-osd
       "Build a EnableDayOSD protobuf message from a map."
       [m]
       [enable-day-osd-spec =>
        #(instance? cmd.OSD.JonSharedCmdOsd$EnableDayOSD %)]
       (let [builder (cmd.OSD.JonSharedCmdOsd$EnableDayOSD/newBuilder)]
         (.build builder)))

(>defn build-disable-day-osd
       "Build a DisableDayOSD protobuf message from a map."
       [m]
       [disable-day-osd-spec =>
        #(instance? cmd.OSD.JonSharedCmdOsd$DisableDayOSD %)]
       (let [builder (cmd.OSD.JonSharedCmdOsd$DisableDayOSD/newBuilder)]
         (.build builder)))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.OSD.JonSharedCmdOsd$Root proto]
       [#(instance? cmd.OSD.JonSharedCmdOsd$Root %) => root-spec]
       (cond-> {}
         ;; Oneof payload
         true (merge (parse-root-payload proto))))

(>defn parse-show-default-screen
       "Parse a ShowDefaultScreen protobuf message to a map."
       [^cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen proto]
       [any? => show-default-screen-spec]
       {})

(>defn parse-show-lrf-measure-screen
       "Parse a ShowLRFMeasureScreen protobuf message to a map."
       [^cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen proto]
       [any? => show-lrf-measure-screen-spec]
       {})

(>defn parse-show-lrf-result-screen
       "Parse a ShowLRFResultScreen protobuf message to a map."
       [^cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen proto]
       [any? => show-lrf-result-screen-spec]
       {})

(>defn parse-show-lrf-result-simplified-screen
       "Parse a ShowLRFResultSimplifiedScreen protobuf message to a map."
       [^cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen proto]
       [any? => show-lrf-result-simplified-screen-spec]
       {})

(>defn parse-enable-heat-osd
       "Parse a EnableHeatOSD protobuf message to a map."
       [^cmd.OSD.JonSharedCmdOsd$EnableHeatOSD proto]
       [any? => enable-heat-osd-spec]
       {})

(>defn parse-disable-heat-osd
       "Parse a DisableHeatOSD protobuf message to a map."
       [^cmd.OSD.JonSharedCmdOsd$DisableHeatOSD proto]
       [any? => disable-heat-osd-spec]
       {})

(>defn parse-enable-day-osd
       "Parse a EnableDayOSD protobuf message to a map."
       [^cmd.OSD.JonSharedCmdOsd$EnableDayOSD proto]
       [any? => enable-day-osd-spec]
       {})

(>defn parse-disable-day-osd
       "Parse a DisableDayOSD protobuf message to a map."
       [^cmd.OSD.JonSharedCmdOsd$DisableDayOSD proto]
       [any? => disable-day-osd-spec]
       {})

(>defn-
  build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  [#(instance? cmd.OSD.JonSharedCmdOsd$Root$Builder %) [:tuple keyword? any?] =>
   #(instance? cmd.OSD.JonSharedCmdOsd$Root$Builder %)]
  (case field-key
    :show-default-screen
      (.setShowDefaultScreen builder (build-show-default-screen value))
    :show-lrf-measure-screen
      (.setShowLrfMeasureScreen builder (build-show-lrf-measure-screen value))
    :show-lrf-result-screen
      (.setShowLrfResultScreen builder (build-show-lrf-result-screen value))
    :show-lrf-result-simplified-screen
      (.setShowLrfResultSimplifiedScreen
        builder
        (build-show-lrf-result-simplified-screen value))
    :enable-heat-osd (.setEnableHeatOsd builder (build-enable-heat-osd value))
    :disable-heat-osd (.setDisableHeatOsd builder
                                          (build-disable-heat-osd value))
    :enable-day-osd (.setEnableDayOsd builder (build-enable-day-osd value))
    :disable-day-osd (.setDisableDayOsd builder (build-disable-day-osd value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(>defn-
  parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.OSD.JonSharedCmdOsd$Root proto]
  [#(instance? cmd.OSD.JonSharedCmdOsd$Root %) => (? map?)]
  (cond (.hasShowDefaultScreen proto) {:show-default-screen
                                         (parse-show-default-screen
                                           (.getShowDefaultScreen proto))}
        (.hasShowLrfMeasureScreen proto) {:show-lrf-measure-screen
                                            (parse-show-lrf-measure-screen
                                              (.getShowLrfMeasureScreen proto))}
        (.hasShowLrfResultScreen proto) {:show-lrf-result-screen
                                           (parse-show-lrf-result-screen
                                             (.getShowLrfResultScreen proto))}
        (.hasShowLrfResultSimplifiedScreen proto)
          {:show-lrf-result-simplified-screen
             (parse-show-lrf-result-simplified-screen
               (.getShowLrfResultSimplifiedScreen proto))}
        (.hasEnableHeatOsd proto)
          {:enable-heat-osd (parse-enable-heat-osd (.getEnableHeatOsd proto))}
        (.hasDisableHeatOsd proto) {:disable-heat-osd (parse-disable-heat-osd
                                                        (.getDisableHeatOsd
                                                          proto))}
        (.hasEnableDayOsd proto) {:enable-day-osd (parse-enable-day-osd
                                                    (.getEnableDayOsd proto))}
        (.hasDisableDayOsd proto)
          {:disable-day-osd (parse-disable-day-osd (.getDisableDayOsd proto))}))