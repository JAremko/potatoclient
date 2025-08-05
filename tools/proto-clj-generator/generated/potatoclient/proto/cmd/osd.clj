(ns potatoclient.proto.cmd.osd
  "Generated protobuf functions."
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

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]]
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

(defn build-show-default-screen
  "Build a ShowDefaultScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen/newBuilder)]
    (.build builder)))

(defn build-show-lrf-measure-screen
  "Build a ShowLRFMeasureScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen/newBuilder)]
    (.build builder)))

(defn build-show-lrf-result-screen
  "Build a ShowLRFResultScreen protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen/newBuilder)]
    (.build builder)))

(defn build-show-lrf-result-simplified-screen
  "Build a ShowLRFResultSimplifiedScreen protobuf message from a map."
  [m]
  (let [builder
          (cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen/newBuilder)]
    (.build builder)))

(defn build-enable-heat-osd
  "Build a EnableHeatOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$EnableHeatOSD/newBuilder)]
    (.build builder)))

(defn build-disable-heat-osd
  "Build a DisableHeatOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$DisableHeatOSD/newBuilder)]
    (.build builder)))

(defn build-enable-day-osd
  "Build a EnableDayOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$EnableDayOSD/newBuilder)]
    (.build builder)))

(defn build-disable-day-osd
  "Build a DisableDayOSD protobuf message from a map."
  [m]
  (let [builder (cmd.OSD.JonSharedCmdOsd$DisableDayOSD/newBuilder)]
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-show-default-screen
  "Parse a ShowDefaultScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowDefaultScreen proto]
  {})

(defn parse-show-lrf-measure-screen
  "Parse a ShowLRFMeasureScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFMeasureScreen proto]
  {})

(defn parse-show-lrf-result-screen
  "Parse a ShowLRFResultScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFResultScreen proto]
  {})

(defn parse-show-lrf-result-simplified-screen
  "Parse a ShowLRFResultSimplifiedScreen protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$ShowLRFResultSimplifiedScreen proto]
  {})

(defn parse-enable-heat-osd
  "Parse a EnableHeatOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$EnableHeatOSD proto]
  {})

(defn parse-disable-heat-osd
  "Parse a DisableHeatOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$DisableHeatOSD proto]
  {})

(defn parse-enable-day-osd
  "Parse a EnableDayOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$EnableDayOSD proto]
  {})

(defn parse-disable-day-osd
  "Parse a DisableDayOSD protobuf message to a map."
  [^cmd.OSD.JonSharedCmdOsd$DisableDayOSD proto]
  {})

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
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

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.OSD.JonSharedCmdOsd$Root proto]
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