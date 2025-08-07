(ns test.roundtrip.cmd.lrf
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof]
            [test.roundtrip.ser :as types])
  (:import cmd.Lrf.JonSharedCmdLrf$Root
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
;; Malli Specs
;; =============================================================================

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:oneof
     {:target-designator-off
        [:map [:target-designator-off :cmd.lrf/target-designator-off]],
      :target-designator-on-mode-b [:map
                                    [:target-designator-on-mode-b
                                     :cmd.lrf/target-designator-on-mode-b]],
      :disable-fog-mode [:map [:disable-fog-mode :cmd.lrf/disable-fog-mode]],
      :set-scan-mode [:map [:set-scan-mode :cmd.lrf/set-scan-mode]],
      :refine-off [:map [:refine-off :cmd.lrf/refine-off]],
      :scan-off [:map [:scan-off :cmd.lrf/scan-off]],
      :refine-on [:map [:refine-on :cmd.lrf/refine-on]],
      :start [:map [:start :cmd.lrf/start]],
      :measure [:map [:measure :cmd.lrf/measure]],
      :scan-on [:map [:scan-on :cmd.lrf/scan-on]],
      :stop [:map [:stop :cmd.lrf/stop]],
      :new-session [:map [:new-session :cmd.lrf/new-session]],
      :get-meteo [:map [:get-meteo :cmd.lrf/get-meteo]],
      :enable-fog-mode [:map [:enable-fog-mode :cmd.lrf/enable-fog-mode]],
      :error/message "This oneof field is required",
      :target-designator-on-mode-a [:map
                                    [:target-designator-on-mode-a
                                     :cmd.lrf/target-designator-on-mode-a]]}]]])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def measure-spec "Malli spec for measure message" [:map])

(def scan-on-spec "Malli spec for scan-on message" [:map])

(def scan-off-spec "Malli spec for scan-off message" [:map])

(def refine-off-spec "Malli spec for refine-off message" [:map])

(def refine-on-spec "Malli spec for refine-on message" [:map])

(def target-designator-off-spec
  "Malli spec for target-designator-off message"
  [:map])

(def target-designator-on-mode-a-spec
  "Malli spec for target-designator-on-mode-a message"
  [:map])

(def target-designator-on-mode-b-spec
  "Malli spec for target-designator-on-mode-b message"
  [:map])

(def enable-fog-mode-spec "Malli spec for enable-fog-mode message" [:map])

(def disable-fog-mode-spec "Malli spec for disable-fog-mode message" [:map])

(def set-scan-mode-spec
  "Malli spec for set-scan-mode message"
  [:map
   [:mode {:optional true} [test.roundtrip.ser :as types]
    /jon-gui-data-lrf-scan-modes-spec]])

(def new-session-spec "Malli spec for new-session message" [:map])

;; =============================================================================
;; Registry Setup
;; =============================================================================

;; Registry for enum and message specs in this namespace
(def registry
  {:cmd.Lrf/root root-spec,
   :cmd.Lrf/get-meteo get-meteo-spec,
   :cmd.Lrf/start start-spec,
   :cmd.Lrf/stop stop-spec,
   :cmd.Lrf/measure measure-spec,
   :cmd.Lrf/scan-on scan-on-spec,
   :cmd.Lrf/scan-off scan-off-spec,
   :cmd.Lrf/refine-off refine-off-spec,
   :cmd.Lrf/refine-on refine-on-spec,
   :cmd.Lrf/target-designator-off target-designator-off-spec,
   :cmd.Lrf/target-designator-on-mode-a target-designator-on-mode-a-spec,
   :cmd.Lrf/target-designator-on-mode-b target-designator-on-mode-b-spec,
   :cmd.Lrf/enable-fog-mode enable-fog-mode-spec,
   :cmd.Lrf/disable-fog-mode disable-fog-mode-spec,
   :cmd.Lrf/set-scan-mode set-scan-mode-spec,
   :cmd.Lrf/new-session new-session-spec})

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

(>defn build-root
       "Build a Root protobuf message from a map."
       [m]
       [root-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$Root/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first (filter
                                       (fn [[k v]]
                                         (#{:measure :scan-on :scan-off :start
                                            :stop :target-designator-off
                                            :target-designator-on-mode-a
                                            :target-designator-on-mode-b
                                            :enable-fog-mode :disable-fog-mode
                                            :set-scan-mode :new-session
                                            :get-meteo :refine-on :refine-off}
                                          k))
                                       (:cmd m)))]
           (build-root-payload builder cmd-field))
         (.build builder)))

(>defn build-get-meteo
       "Build a GetMeteo protobuf message from a map."
       [m]
       [get-meteo-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$GetMeteo/newBuilder)]
         (.build builder)))

(>defn build-start
       "Build a Start protobuf message from a map."
       [m]
       [start-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$Start/newBuilder)]
         (.build builder)))

(>defn build-stop
       "Build a Stop protobuf message from a map."
       [m]
       [stop-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$Stop/newBuilder)]
         (.build builder)))

(>defn build-measure
       "Build a Measure protobuf message from a map."
       [m]
       [measure-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$Measure/newBuilder)]
         (.build builder)))

(>defn build-scan-on
       "Build a ScanOn protobuf message from a map."
       [m]
       [scan-on-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$ScanOn/newBuilder)]
         (.build builder)))

(>defn build-scan-off
       "Build a ScanOff protobuf message from a map."
       [m]
       [scan-off-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$ScanOff/newBuilder)]
         (.build builder)))

(>defn build-refine-off
       "Build a RefineOff protobuf message from a map."
       [m]
       [refine-off-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$RefineOff/newBuilder)]
         (.build builder)))

(>defn build-refine-on
       "Build a RefineOn protobuf message from a map."
       [m]
       [refine-on-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$RefineOn/newBuilder)]
         (.build builder)))

(>defn build-target-designator-off
       "Build a TargetDesignatorOff protobuf message from a map."
       [m]
       [target-designator-off-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff/newBuilder)]
         (.build builder)))

(>defn build-target-designator-on-mode-a
       "Build a TargetDesignatorOnModeA protobuf message from a map."
       [m]
       [target-designator-on-mode-a-spec => any?]
       (let [builder
               (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA/newBuilder)]
         (.build builder)))

(>defn build-target-designator-on-mode-b
       "Build a TargetDesignatorOnModeB protobuf message from a map."
       [m]
       [target-designator-on-mode-b-spec => any?]
       (let [builder
               (cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB/newBuilder)]
         (.build builder)))

(>defn build-enable-fog-mode
       "Build a EnableFogMode protobuf message from a map."
       [m]
       [enable-fog-mode-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$EnableFogMode/newBuilder)]
         (.build builder)))

(>defn build-disable-fog-mode
       "Build a DisableFogMode protobuf message from a map."
       [m]
       [disable-fog-mode-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$DisableFogMode/newBuilder)]
         (.build builder)))

(>defn build-set-scan-mode
       "Build a SetScanMode protobuf message from a map."
       [m]
       [set-scan-mode-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$SetScanMode/newBuilder)]
         ;; Set regular fields
         (when (contains? m :mode)
           (.setMode builder
                     (when-let [v (get m :mode)]
                       (get types/jon-gui-data-lrf-scan-modes-values v))))
         (.build builder)))

(>defn build-new-session
       "Build a NewSession protobuf message from a map."
       [m]
       [new-session-spec => any?]
       (let [builder (cmd.Lrf.JonSharedCmdLrf$NewSession/newBuilder)]
         (.build builder)))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$Root proto]
       [any? => root-spec]
       (cond-> {}
         ;; Oneof: cmd
         (parse-root-payload proto) (assoc :cmd (parse-root-payload proto))))

(>defn parse-get-meteo
       "Parse a GetMeteo protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$GetMeteo proto]
       [any? => get-meteo-spec]
       {})

(>defn parse-start
       "Parse a Start protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$Start proto]
       [any? => start-spec]
       {})

(>defn parse-stop
       "Parse a Stop protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$Stop proto]
       [any? => stop-spec]
       {})

(>defn parse-measure
       "Parse a Measure protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$Measure proto]
       [any? => measure-spec]
       {})

(>defn parse-scan-on
       "Parse a ScanOn protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$ScanOn proto]
       [any? => scan-on-spec]
       {})

(>defn parse-scan-off
       "Parse a ScanOff protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$ScanOff proto]
       [any? => scan-off-spec]
       {})

(>defn parse-refine-off
       "Parse a RefineOff protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$RefineOff proto]
       [any? => refine-off-spec]
       {})

(>defn parse-refine-on
       "Parse a RefineOn protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$RefineOn proto]
       [any? => refine-on-spec]
       {})

(>defn parse-target-designator-off
       "Parse a TargetDesignatorOff protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOff proto]
       [any? => target-designator-off-spec]
       {})

(>defn parse-target-designator-on-mode-a
       "Parse a TargetDesignatorOnModeA protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeA proto]
       [any? => target-designator-on-mode-a-spec]
       {})

(>defn parse-target-designator-on-mode-b
       "Parse a TargetDesignatorOnModeB protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$TargetDesignatorOnModeB proto]
       [any? => target-designator-on-mode-b-spec]
       {})

(>defn parse-enable-fog-mode
       "Parse a EnableFogMode protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$EnableFogMode proto]
       [any? => enable-fog-mode-spec]
       {})

(>defn parse-disable-fog-mode
       "Parse a DisableFogMode protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$DisableFogMode proto]
       [any? => disable-fog-mode-spec]
       {})

(>defn parse-set-scan-mode
       "Parse a SetScanMode protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$SetScanMode proto]
       [any? => set-scan-mode-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :mode
                (get types/jon-gui-data-lrf-scan-modes-keywords
                     (.getMode proto)))))

(>defn parse-new-session
       "Parse a NewSession protobuf message to a map."
       [^cmd.Lrf.JonSharedCmdLrf$NewSession proto]
       [any? => new-session-spec]
       {})

(>defn-
  build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  [any? [:tuple keyword? any?] => any?]
  (case field-key
    :measure (.setMeasure builder (build-measure value))
    :scan-on (.setScanOn builder (build-scan-on value))
    :scan-off (.setScanOff builder (build-scan-off value))
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :target-designator-off
      (.setTargetDesignatorOff builder (build-target-designator-off value))
    :target-designator-on-mode-a (.setTargetDesignatorOnModeA
                                   builder
                                   (build-target-designator-on-mode-a value))
    :target-designator-on-mode-b (.setTargetDesignatorOnModeB
                                   builder
                                   (build-target-designator-on-mode-b value))
    :enable-fog-mode (.setEnableFogMode builder (build-enable-fog-mode value))
    :disable-fog-mode (.setDisableFogMode builder
                                          (build-disable-fog-mode value))
    :set-scan-mode (.setSetScanMode builder (build-set-scan-mode value))
    :new-session (.setNewSession builder (build-new-session value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    :refine-on (.setRefineOn builder (build-refine-on value))
    :refine-off (.setRefineOff builder (build-refine-off value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(>defn-
  parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Lrf.JonSharedCmdLrf$Root proto]
  [any? => (? map?)]
  (cond (.hasMeasure proto) {:measure (parse-measure (.getMeasure proto))}
        (.hasScanOn proto) {:scan-on (parse-scan-on (.getScanOn proto))}
        (.hasScanOff proto) {:scan-off (parse-scan-off (.getScanOff proto))}
        (.hasStart proto) {:start (parse-start (.getStart proto))}
        (.hasStop proto) {:stop (parse-stop (.getStop proto))}
        (.hasTargetDesignatorOff proto) {:target-designator-off
                                           (parse-target-designator-off
                                             (.getTargetDesignatorOff proto))}
        (.hasTargetDesignatorOnModeA proto)
          {:target-designator-on-mode-a (parse-target-designator-on-mode-a
                                          (.getTargetDesignatorOnModeA proto))}
        (.hasTargetDesignatorOnModeB proto)
          {:target-designator-on-mode-b (parse-target-designator-on-mode-b
                                          (.getTargetDesignatorOnModeB proto))}
        (.hasEnableFogMode proto)
          {:enable-fog-mode (parse-enable-fog-mode (.getEnableFogMode proto))}
        (.hasDisableFogMode proto) {:disable-fog-mode (parse-disable-fog-mode
                                                        (.getDisableFogMode
                                                          proto))}
        (.hasSetScanMode proto) {:set-scan-mode (parse-set-scan-mode
                                                  (.getSetScanMode proto))}
        (.hasNewSession proto) {:new-session (parse-new-session (.getNewSession
                                                                  proto))}
        (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo proto))}
        (.hasRefineOn proto) {:refine-on (parse-refine-on (.getRefineOn proto))}
        (.hasRefineOff proto) {:refine-off (parse-refine-off (.getRefineOff
                                                               proto))}))
;; =============================================================================
;; Validation Helper Functions
;; =============================================================================

;; Validation helpers for SetScanMode
;; Warning: Could not extract spec for field mode