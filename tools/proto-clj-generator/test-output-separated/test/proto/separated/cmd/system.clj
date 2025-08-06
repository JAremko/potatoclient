(ns test.proto.separated.cmd.system
  "Generated protobuf functions."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn- ?]]
            [malli.core :as m]
            [test.proto.separated.ser.data :as types])
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
;; Malli Specs
;; =============================================================================

(def root-spec
  "Malli spec for root message"
  [:map
   [:cmd
    [:oneof
     {:geodesic-mode-disable
        [:map [:geodesic-mode-disable :cmd.system/disable-geodesic-mode]],
      :start-all [:map [:start-all :cmd.system/start-a-ll]],
      :geodesic-mode-enable
        [:map [:geodesic-mode-enable :cmd.system/enable-geodesic-mode]],
      :localization [:map [:localization :cmd.system/set-localization]],
      :unmark-rec-important
        [:map [:unmark-rec-important :cmd.system/unmark-rec-important]],
      :stop-rec [:map [:stop-rec :cmd.system/stop-rec]],
      :reboot [:map [:reboot :cmd.system/reboot]],
      :start-rec [:map [:start-rec :cmd.system/start-rec]],
      :power-off [:map [:power-off :cmd.system/power-off]],
      :reset-configs [:map [:reset-configs :cmd.system/reset-configs]],
      :stop-all [:map [:stop-all :cmd.system/stop-a-ll]],
      :enter-transport [:map [:enter-transport :cmd.system/enter-transport]],
      :mark-rec-important
        [:map [:mark-rec-important :cmd.system/mark-rec-important]]}]]])

(def start-a-ll-spec "Malli spec for start-a-ll message" [:map])

(def stop-a-ll-spec "Malli spec for stop-a-ll message" [:map])

(def reboot-spec "Malli spec for reboot message" [:map])

(def power-off-spec "Malli spec for power-off message" [:map])

(def reset-configs-spec "Malli spec for reset-configs message" [:map])

(def start-rec-spec "Malli spec for start-rec message" [:map])

(def stop-rec-spec "Malli spec for stop-rec message" [:map])

(def mark-rec-important-spec "Malli spec for mark-rec-important message" [:map])

(def unmark-rec-important-spec
  "Malli spec for unmark-rec-important message"
  [:map])

(def enter-transport-spec "Malli spec for enter-transport message" [:map])

(def enable-geodesic-mode-spec
  "Malli spec for enable-geodesic-mode message"
  [:map])

(def disable-geodesic-mode-spec
  "Malli spec for disable-geodesic-mode message"
  [:map])

(def set-localization-spec
  "Malli spec for set-localization message"
  [:map [:loc [:maybe :ser/jon-gui-data-system-localizations]]])

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

(>defn build-root
       "Build a Root protobuf message from a map."
       [m]
       [root-spec => #(instance? cmd.System.JonSharedCmdSystem$Root %)]
       (let [builder (cmd.System.JonSharedCmdSystem$Root/newBuilder)]
         ;; Handle oneof: cmd
         (when-let [cmd-field (first (filter (fn [[k v]]
                                               (#{:start-all :stop-all :reboot
                                                  :power-off :localization
                                                  :reset-configs :start-rec
                                                  :stop-rec :mark-rec-important
                                                  :unmark-rec-important
                                                  :enter-transport
                                                  :geodesic-mode-enable
                                                  :geodesic-mode-disable}
                                                k))
                                       m))]
           (build-root-payload builder cmd-field))
         (.build builder)))

(>defn build-start-a-ll
       "Build a StartALl protobuf message from a map."
       [m]
       [start-a-ll-spec =>
        #(instance? cmd.System.JonSharedCmdSystem$StartALl %)]
       (let [builder (cmd.System.JonSharedCmdSystem$StartALl/newBuilder)]
         (.build builder)))

(>defn build-stop-a-ll
       "Build a StopALl protobuf message from a map."
       [m]
       [stop-a-ll-spec => #(instance? cmd.System.JonSharedCmdSystem$StopALl %)]
       (let [builder (cmd.System.JonSharedCmdSystem$StopALl/newBuilder)]
         (.build builder)))

(>defn build-reboot
       "Build a Reboot protobuf message from a map."
       [m]
       [reboot-spec => #(instance? cmd.System.JonSharedCmdSystem$Reboot %)]
       (let [builder (cmd.System.JonSharedCmdSystem$Reboot/newBuilder)]
         (.build builder)))

(>defn build-power-off
       "Build a PowerOff protobuf message from a map."
       [m]
       [power-off-spec => #(instance? cmd.System.JonSharedCmdSystem$PowerOff %)]
       (let [builder (cmd.System.JonSharedCmdSystem$PowerOff/newBuilder)]
         (.build builder)))

(>defn build-reset-configs
       "Build a ResetConfigs protobuf message from a map."
       [m]
       [reset-configs-spec =>
        #(instance? cmd.System.JonSharedCmdSystem$ResetConfigs %)]
       (let [builder (cmd.System.JonSharedCmdSystem$ResetConfigs/newBuilder)]
         (.build builder)))

(>defn build-start-rec
       "Build a StartRec protobuf message from a map."
       [m]
       [start-rec-spec => #(instance? cmd.System.JonSharedCmdSystem$StartRec %)]
       (let [builder (cmd.System.JonSharedCmdSystem$StartRec/newBuilder)]
         (.build builder)))

(>defn build-stop-rec
       "Build a StopRec protobuf message from a map."
       [m]
       [stop-rec-spec => #(instance? cmd.System.JonSharedCmdSystem$StopRec %)]
       (let [builder (cmd.System.JonSharedCmdSystem$StopRec/newBuilder)]
         (.build builder)))

(>defn build-mark-rec-important
       "Build a MarkRecImportant protobuf message from a map."
       [m]
       [mark-rec-important-spec =>
        #(instance? cmd.System.JonSharedCmdSystem$MarkRecImportant %)]
       (let [builder
               (cmd.System.JonSharedCmdSystem$MarkRecImportant/newBuilder)]
         (.build builder)))

(>defn build-unmark-rec-important
       "Build a UnmarkRecImportant protobuf message from a map."
       [m]
       [unmark-rec-important-spec =>
        #(instance? cmd.System.JonSharedCmdSystem$UnmarkRecImportant %)]
       (let [builder
               (cmd.System.JonSharedCmdSystem$UnmarkRecImportant/newBuilder)]
         (.build builder)))

(>defn build-enter-transport
       "Build a EnterTransport protobuf message from a map."
       [m]
       [enter-transport-spec =>
        #(instance? cmd.System.JonSharedCmdSystem$EnterTransport %)]
       (let [builder (cmd.System.JonSharedCmdSystem$EnterTransport/newBuilder)]
         (.build builder)))

(>defn build-enable-geodesic-mode
       "Build a EnableGeodesicMode protobuf message from a map."
       [m]
       [enable-geodesic-mode-spec =>
        #(instance? cmd.System.JonSharedCmdSystem$EnableGeodesicMode %)]
       (let [builder
               (cmd.System.JonSharedCmdSystem$EnableGeodesicMode/newBuilder)]
         (.build builder)))

(>defn build-disable-geodesic-mode
       "Build a DisableGeodesicMode protobuf message from a map."
       [m]
       [disable-geodesic-mode-spec =>
        #(instance? cmd.System.JonSharedCmdSystem$DisableGeodesicMode %)]
       (let [builder
               (cmd.System.JonSharedCmdSystem$DisableGeodesicMode/newBuilder)]
         (.build builder)))

(>defn build-set-localization
       "Build a SetLocalization protobuf message from a map."
       [m]
       [set-localization-spec =>
        #(instance? cmd.System.JonSharedCmdSystem$SetLocalization %)]
       (let [builder (cmd.System.JonSharedCmdSystem$SetLocalization/newBuilder)]
         ;; Set regular fields
         (when (contains? m :loc)
           (.setLoc builder
                    (get types/jon-gui-data-system-localizations-values
                         (get m :loc))))
         (.build builder)))

(>defn parse-root
       "Parse a Root protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$Root proto]
       [#(instance? cmd.System.JonSharedCmdSystem$Root %) => root-spec]
       (cond-> {}
         ;; Oneof payload
         true (merge (parse-root-payload proto))))

(>defn parse-start-a-ll
       "Parse a StartALl protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$StartALl proto]
       [any? => start-a-ll-spec]
       {})

(>defn parse-stop-a-ll
       "Parse a StopALl protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$StopALl proto]
       [any? => stop-a-ll-spec]
       {})

(>defn parse-reboot
       "Parse a Reboot protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$Reboot proto]
       [any? => reboot-spec]
       {})

(>defn parse-power-off
       "Parse a PowerOff protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$PowerOff proto]
       [any? => power-off-spec]
       {})

(>defn parse-reset-configs
       "Parse a ResetConfigs protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$ResetConfigs proto]
       [any? => reset-configs-spec]
       {})

(>defn parse-start-rec
       "Parse a StartRec protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$StartRec proto]
       [any? => start-rec-spec]
       {})

(>defn parse-stop-rec
       "Parse a StopRec protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$StopRec proto]
       [any? => stop-rec-spec]
       {})

(>defn parse-mark-rec-important
       "Parse a MarkRecImportant protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$MarkRecImportant proto]
       [any? => mark-rec-important-spec]
       {})

(>defn parse-unmark-rec-important
       "Parse a UnmarkRecImportant protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$UnmarkRecImportant proto]
       [any? => unmark-rec-important-spec]
       {})

(>defn parse-enter-transport
       "Parse a EnterTransport protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$EnterTransport proto]
       [any? => enter-transport-spec]
       {})

(>defn parse-enable-geodesic-mode
       "Parse a EnableGeodesicMode protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$EnableGeodesicMode proto]
       [any? => enable-geodesic-mode-spec]
       {})

(>defn parse-disable-geodesic-mode
       "Parse a DisableGeodesicMode protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$DisableGeodesicMode proto]
       [any? => disable-geodesic-mode-spec]
       {})

(>defn parse-set-localization
       "Parse a SetLocalization protobuf message to a map."
       [^cmd.System.JonSharedCmdSystem$SetLocalization proto]
       [#(instance? cmd.System.JonSharedCmdSystem$SetLocalization %) =>
        set-localization-spec]
       (cond-> {}
         ;; Regular fields
         true (assoc :loc
                (get types/jon-gui-data-system-localizations-keywords
                     (.getLoc proto)))))

(>defn-
  build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  [#(instance? cmd.System.JonSharedCmdSystem$Root$Builder %)
   [:tuple keyword? any?] =>
   #(instance? cmd.System.JonSharedCmdSystem$Root$Builder %)]
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

(>defn-
  parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.System.JonSharedCmdSystem$Root proto]
  [#(instance? cmd.System.JonSharedCmdSystem$Root %) => (? map?)]
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