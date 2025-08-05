(ns potatoclient.proto.cmd.system
  "Generated protobuf functions."
  (:require [potatoclient.proto.ser :as types])
  (:import cmd.System.JonSharedCmdSystem$Root
           cmd.System.JonSharedCmdSystem$StartALl
           cmd.System.JonSharedCmdSystem$StopALl
           cmd.System.JonSharedCmdSystem$Reboot
           cmd.System.JonSharedCmdSystem$PowerOff
           cmd.System.JonSharedCmdSystem$ResetConfigs
           cmd.System.JonSharedCmdSystem$StartRec
           cmd.System.JonSharedCmdSystem$StopRec
           cmd.System.JonSharedCmdSystem$MarkRecImportant
           cmd.System.JonSharedCmdSystem$UnmarkRecImportant
           cmd.System.JonSharedCmdSystem$EnterTransport
           cmd.System.JonSharedCmdSystem$EnableGeodesicMode
           cmd.System.JonSharedCmdSystem$DisableGeodesicMode
           cmd.System.JonSharedCmdSystem$SetLocalization))

;; =============================================================================
;; Enums
;; =============================================================================

;; No enums

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-start-a-ll)
(declare build-stop-a-ll)
(declare build-reboot)
(declare build-power-off)
(declare build-reset-configs)
(declare build-start-rec)
(declare build-stop-rec)
(declare build-mark-rec-important)
(declare build-unmark-rec-important)
(declare build-enter-transport)
(declare build-enable-geodesic-mode)
(declare build-disable-geodesic-mode)
(declare build-set-localization)
(declare parse-root)
(declare parse-start-a-ll)
(declare parse-stop-a-ll)
(declare parse-reboot)
(declare parse-power-off)
(declare parse-reset-configs)
(declare parse-start-rec)
(declare parse-stop-rec)
(declare parse-mark-rec-important)
(declare parse-unmark-rec-important)
(declare parse-enter-transport)
(declare parse-enable-geodesic-mode)
(declare parse-disable-geodesic-mode)
(declare parse-set-localization)
(declare build-root-payload)
(declare parse-root-payload)

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first
                           (filter (fn [[k v]]
                                     (#{:start-all :stop-all :reboot :power-off
                                        :localization :reset-configs :start-rec
                                        :stop-rec :mark-rec-important
                                        :unmark-rec-important :enter-transport
                                        :geodesic-mode-enable
                                        :geodesic-mode-disable}
                                      k))
                             m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-start-a-ll
  "Build a StartALl protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StartALl/newBuilder)]
    (.build builder)))

(defn build-stop-a-ll
  "Build a StopALl protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StopALl/newBuilder)]
    (.build builder)))

(defn build-reboot
  "Build a Reboot protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$Reboot/newBuilder)]
    (.build builder)))

(defn build-power-off
  "Build a PowerOff protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$PowerOff/newBuilder)]
    (.build builder)))

(defn build-reset-configs
  "Build a ResetConfigs protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$ResetConfigs/newBuilder)]
    (.build builder)))

(defn build-start-rec
  "Build a StartRec protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StartRec/newBuilder)]
    (.build builder)))

(defn build-stop-rec
  "Build a StopRec protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$StopRec/newBuilder)]
    (.build builder)))

(defn build-mark-rec-important
  "Build a MarkRecImportant protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$MarkRecImportant/newBuilder)]
    (.build builder)))

(defn build-unmark-rec-important
  "Build a UnmarkRecImportant protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$UnmarkRecImportant/newBuilder)]
    (.build builder)))

(defn build-enter-transport
  "Build a EnterTransport protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$EnterTransport/newBuilder)]
    (.build builder)))

(defn build-enable-geodesic-mode
  "Build a EnableGeodesicMode protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$EnableGeodesicMode/newBuilder)]
    (.build builder)))

(defn build-disable-geodesic-mode
  "Build a DisableGeodesicMode protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$DisableGeodesicMode/newBuilder)]
    (.build builder)))

(defn build-set-localization
  "Build a SetLocalization protobuf message from a map."
  [m]
  (let [builder (cmd.System.JonSharedCmdSystem$SetLocalization/newBuilder)]
    ;; Set regular fields
    (when (contains? m :loc)
      (.setLoc builder
               (get types/jon-gui-data-system-localizations-values
                    (get m :loc))))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-start-a-ll
  "Parse a StartALl protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StartALl proto]
  {})

(defn parse-stop-a-ll
  "Parse a StopALl protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StopALl proto]
  {})

(defn parse-reboot
  "Parse a Reboot protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$Reboot proto]
  {})

(defn parse-power-off
  "Parse a PowerOff protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$PowerOff proto]
  {})

(defn parse-reset-configs
  "Parse a ResetConfigs protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$ResetConfigs proto]
  {})

(defn parse-start-rec
  "Parse a StartRec protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StartRec proto]
  {})

(defn parse-stop-rec
  "Parse a StopRec protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$StopRec proto]
  {})

(defn parse-mark-rec-important
  "Parse a MarkRecImportant protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$MarkRecImportant proto]
  {})

(defn parse-unmark-rec-important
  "Parse a UnmarkRecImportant protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$UnmarkRecImportant proto]
  {})

(defn parse-enter-transport
  "Parse a EnterTransport protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$EnterTransport proto]
  {})

(defn parse-enable-geodesic-mode
  "Parse a EnableGeodesicMode protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$EnableGeodesicMode proto]
  {})

(defn parse-disable-geodesic-mode
  "Parse a DisableGeodesicMode protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$DisableGeodesicMode proto]
  {})

(defn parse-set-localization
  "Parse a SetLocalization protobuf message to a map."
  [^cmd.System.JonSharedCmdSystem$SetLocalization proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :loc
           (get types/jon-gui-data-system-localizations-keywords
                (.getLoc proto)))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start-all (.setStartAll builder (build-start-a-ll value))
    :stop-all (.setStopAll builder (build-stop-a-ll value))
    :reboot (.setReboot builder (build-reboot value))
    :power-off (.setPowerOff builder (build-power-off value))
    :localization (.setLocalization builder (build-set-localization value))
    :reset-configs (.setResetConfigs builder (build-reset-configs value))
    :start-rec (.setStartRec builder (build-start-rec value))
    :stop-rec (.setStopRec builder (build-stop-rec value))
    :mark-rec-important (.setMarkRecImportant builder
                                              (build-mark-rec-important value))
    :unmark-rec-important
      (.setUnmarkRecImportant builder (build-unmark-rec-important value))
    :enter-transport (.setEnterTransport builder (build-enter-transport value))
    :geodesic-mode-enable
      (.setGeodesicModeEnable builder (build-enable-geodesic-mode value))
    :geodesic-mode-disable
      (.setGeodesicModeDisable builder (build-disable-geodesic-mode value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.System.JonSharedCmdSystem$Root proto]
  (cond (.hasStartAll proto) {:start-all (parse-start-a-ll (.getStartAll
                                                             proto))}
        (.hasStopAll proto) {:stop-all (parse-stop-a-ll (.getStopAll proto))}
        (.hasReboot proto) {:reboot (parse-reboot (.getReboot proto))}
        (.hasPowerOff proto) {:power-off (parse-power-off (.getPowerOff proto))}
        (.hasLocalization proto) {:localization (parse-set-localization
                                                  (.getLocalization proto))}
        (.hasResetConfigs proto) {:reset-configs (parse-reset-configs
                                                   (.getResetConfigs proto))}
        (.hasStartRec proto) {:start-rec (parse-start-rec (.getStartRec proto))}
        (.hasStopRec proto) {:stop-rec (parse-stop-rec (.getStopRec proto))}
        (.hasMarkRecImportant proto) {:mark-rec-important
                                        (parse-mark-rec-important
                                          (.getMarkRecImportant proto))}
        (.hasUnmarkRecImportant proto) {:unmark-rec-important
                                          (parse-unmark-rec-important
                                            (.getUnmarkRecImportant proto))}
        (.hasEnterTransport proto)
          {:enter-transport (parse-enter-transport (.getEnterTransport proto))}
        (.hasGeodesicModeEnable proto) {:geodesic-mode-enable
                                          (parse-enable-geodesic-mode
                                            (.getGeodesicModeEnable proto))}
        (.hasGeodesicModeDisable proto) {:geodesic-mode-disable
                                           (parse-disable-geodesic-mode
                                             (.getGeodesicModeDisable proto))}))