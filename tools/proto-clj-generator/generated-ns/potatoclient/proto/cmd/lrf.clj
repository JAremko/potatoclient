(ns potatoclient.proto.cmd.lrf
  "Generated protobuf functions."
  (:import
   cmd.Lrf.JonSharedCmdLrf$Root
   cmd.Lrf.JonSharedCmdLrf$GetMeteo
   cmd.Lrf.JonSharedCmdLrf$Start
   cmd.Lrf.JonSharedCmdLrf$Stop
   cmd.Lrf.JonSharedCmdLrf$Measure
   cmd.Lrf.JonSharedCmdLrf$ScanOn
   cmd.Lrf.JonSharedCmdLrf$ScanOff
   cmd.Lrf.JonSharedCmdLrf$RefineOff
   cmd.Lrf.JonSharedCmdLrf$RefineOn
   cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff
   cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA
   cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB
   cmd.Lrf.JonSharedCmdLrf$EnableFogMode
   cmd.Lrf.JonSharedCmdLrf$DisableFogMode
   cmd.Lrf.JonSharedCmdLrf$SetScanMode
   cmd.Lrf.JonSharedCmdLrf$NewSession))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-get-meteo)
(declare build-start)
(declare build-stop)
(declare build-measure)
(declare build-scan-on)
(declare build-scan-off)
(declare build-refine-off)
(declare build-refine-on)
(declare build-target-designator-off)
(declare build-target-designator-on-mode-a)
(declare build-target-designator-on-mode-b)
(declare build-enable-fog-mode)
(declare build-disable-fog-mode)
(declare build-set-scan-mode)
(declare build-new-session)
(declare parse-root)
(declare parse-get-meteo)
(declare parse-start)
(declare parse-stop)
(declare parse-measure)
(declare parse-scan-on)
(declare parse-scan-off)
(declare parse-refine-off)
(declare parse-refine-on)
(declare parse-target-designator-off)
(declare parse-target-designator-on-mode-a)
(declare parse-target-designator-on-mode-b)
(declare parse-enable-fog-mode)
(declare parse-disable-fog-mode)
(declare parse-set-scan-mode)
(declare parse-new-session)
(declare build-root-payload)
(declare parse-root-payload)

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Root/newBuilder)]

;; Handle oneof: cmd
    (when-let [cmd-field (first (filter (fn [[k v]] (#{:measure :scan-on :scan-off :start :stop :target-designator-off :target-designator-on-mode-a :target-designator-on-mode-b :enable-fog-mode :disable-fog-mode :set-scan-mode :new-session :get-meteo :refine-on :refine-off} k)) m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$GetMeteo/newBuilder)]

    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Start/newBuilder)]

    (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Stop/newBuilder)]

    (.build builder)))

(defn build-measure
  "Build a Measure protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$Measure/newBuilder)]

    (.build builder)))

(defn build-scan-on
  "Build a ScanOn protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$ScanOn/newBuilder)]

    (.build builder)))

(defn build-scan-off
  "Build a ScanOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$ScanOff/newBuilder)]

    (.build builder)))

(defn build-refine-off
  "Build a RefineOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$RefineOff/newBuilder)]

    (.build builder)))

(defn build-refine-on
  "Build a RefineOn protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$RefineOn/newBuilder)]

    (.build builder)))

(defn build-target-designator-off
  "Build a TargetDesignatorOff protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff/newBuilder)]

    (.build builder)))

(defn build-target-designator-on-mode-a
  "Build a TargetDesignatorOnModeA protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA/newBuilder)]

    (.build builder)))

(defn build-target-designator-on-mode-b
  "Build a TargetDesignatorOnModeB protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB/newBuilder)]

    (.build builder)))

(defn build-enable-fog-mode
  "Build a EnableFogMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$EnableFogMode/newBuilder)]

    (.build builder)))

(defn build-disable-fog-mode
  "Build a DisableFogMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$DisableFogMode/newBuilder)]

    (.build builder)))

(defn build-set-scan-mode
  "Build a SetScanMode protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$SetScanMode/newBuilder)]
    ;; Set regular fields
    (when (contains? m :mode)
      (.setMode builder (get jon-gui-data-lrf-scan-modes-values (get m :mode))))

    (.build builder)))

(defn build-new-session
  "Build a NewSession protobuf message from a map."
  [m]
  (let [builder (cmd.Lrf.JonSharedCmdLrf$NewSession/newBuilder)]

    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Root proto]
  (cond-> {}

;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$GetMeteo proto]
  {})

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Start proto]
  {})

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Stop proto]
  {})

(defn parse-measure
  "Parse a Measure protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$Measure proto]
  {})

(defn parse-scan-on
  "Parse a ScanOn protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$ScanOn proto]
  {})

(defn parse-scan-off
  "Parse a ScanOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$ScanOff proto]
  {})

(defn parse-refine-off
  "Parse a RefineOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$RefineOff proto]
  {})

(defn parse-refine-on
  "Parse a RefineOn protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$RefineOn proto]
  {})

(defn parse-target-designator-off
  "Parse a TargetDesignatorOff protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff proto]
  {})

(defn parse-target-designator-on-mode-a
  "Parse a TargetDesignatorOnModeA protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA proto]
  {})

(defn parse-target-designator-on-mode-b
  "Parse a TargetDesignatorOnModeB protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB proto]
  {})

(defn parse-enable-fog-mode
  "Parse a EnableFogMode protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$EnableFogMode proto]
  {})

(defn parse-disable-fog-mode
  "Parse a DisableFogMode protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$DisableFogMode proto]
  {})

(defn parse-set-scan-mode
  "Parse a SetScanMode protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$SetScanMode proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :mode (get jon-gui-data-lrf-scan-modes-keywords (.getMode proto)))))

(defn parse-new-session
  "Parse a NewSession protobuf message to a map."
  [^cmd.Lrf.JonSharedCmdLrf$NewSession proto]
  {})

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :measure (.setMeasure builder (build-measure value))
    :scan-on (.setScanOn builder (build-scan-on value))
    :scan-off (.setScanOff builder (build-scan-off value))
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :target-designator-off (.setTargetDesignatorOff builder (build-target-designator-off value))
    :target-designator-on-mode-a (.setTargetDesignatorOnModeA builder (build-target-designator-on-mode-a value))
    :target-designator-on-mode-b (.setTargetDesignatorOnModeB builder (build-target-designator-on-mode-b value))
    :enable-fog-mode (.setEnableFogMode builder (build-enable-fog-mode value))
    :disable-fog-mode (.setDisableFogMode builder (build-disable-fog-mode value))
    :set-scan-mode (.setSetScanMode builder (build-set-scan-mode value))
    :new-session (.setNewSession builder (build-new-session value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    :refine-on (.setRefineOn builder (build-refine-on value))
    :refine-off (.setRefineOff builder (build-refine-off value))
    (throw (ex-info "Unknown oneof field" {:field field-key :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Lrf.JonSharedCmdLrf$Root proto]
  (cond
    (.hasMeasure proto) {:measure (parse-measure (.getMeasure proto))}
    (.hasScanOn proto) {:scan-on (parse-scan-on (.getScanOn proto))}
    (.hasScanOff proto) {:scan-off (parse-scan-off (.getScanOff proto))}
    (.hasStart proto) {:start (parse-start (.getStart proto))}
    (.hasStop proto) {:stop (parse-stop (.getStop proto))}
    (.hasTargetDesignatorOff proto) {:target-designator-off (parse-target-designator-off (.getTargetDesignatorOff proto))}
    (.hasTargetDesignatorOnModeA proto) {:target-designator-on-mode-a (parse-target-designator-on-mode-a (.getTargetDesignatorOnModeA proto))}
    (.hasTargetDesignatorOnModeB proto) {:target-designator-on-mode-b (parse-target-designator-on-mode-b (.getTargetDesignatorOnModeB proto))}
    (.hasEnableFogMode proto) {:enable-fog-mode (parse-enable-fog-mode (.getEnableFogMode proto))}
    (.hasDisableFogMode proto) {:disable-fog-mode (parse-disable-fog-mode (.getDisableFogMode proto))}
    (.hasSetScanMode proto) {:set-scan-mode (parse-set-scan-mode (.getSetScanMode proto))}
    (.hasNewSession proto) {:new-session (parse-new-session (.getNewSession proto))}
    (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
    (.hasRefineOn proto) {:refine-on (parse-refine-on (.getRefineOn proto))}
    (.hasRefineOff proto) {:refine-off (parse-refine-off (.getRefineOff proto))}))