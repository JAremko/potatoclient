(ns potatoclient.proto.cmd.compass
  "Generated protobuf functions."
  (:require [malli.core :as m])
  (:import cmd.Compass.JonSharedCmdCompass$Root
           cmd.Compass.JonSharedCmdCompass$Start
           cmd.Compass.JonSharedCmdCompass$Stop
           cmd.Compass.JonSharedCmdCompass$Next
           cmd.Compass.JonSharedCmdCompass$CalibrateStartLong
           cmd.Compass.JonSharedCmdCompass$CalibrateStartShort
           cmd.Compass.JonSharedCmdCompass$CalibrateNext
           cmd.Compass.JonSharedCmdCompass$CalibrateCencel
           cmd.Compass.JonSharedCmdCompass$GetMeteo
           cmd.Compass.JonSharedCmdCompass$SetMagneticDeclination
           cmd.Compass.JonSharedCmdCompass$SetOffsetAngleAzimuth
           cmd.Compass.JonSharedCmdCompass$SetOffsetAngleElevation
           cmd.Compass.JonSharedCmdCompass$SetUseRotaryPosition))

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
    [:altn
     {:calibrate-cencel [:map
                         [:calibrate-cencel :cmd.compass/calibrate-cencel]],
      :start [:map [:start :cmd.compass/start]],
      :set-offset-angle-elevation [:map
                                   [:set-offset-angle-elevation
                                    :cmd.compass/set-offset-angle-elevation]],
      :stop [:map [:stop :cmd.compass/stop]],
      :calibrate-next [:map [:calibrate-next :cmd.compass/calibrate-next]],
      :get-meteo [:map [:get-meteo :cmd.compass/get-meteo]],
      :set-use-rotary-position
        [:map [:set-use-rotary-position :cmd.compass/set-use-rotary-position]],
      :set-magnetic-declination [:map
                                 [:set-magnetic-declination
                                  :cmd.compass/set-magnetic-declination]],
      :start-calibrate-short
        [:map [:start-calibrate-short :cmd.compass/calibrate-start-short]],
      :start-calibrate-long
        [:map [:start-calibrate-long :cmd.compass/calibrate-start-long]],
      :set-offset-angle-azimuth [:map
                                 [:set-offset-angle-azimuth
                                  :cmd.compass/set-offset-angle-azimuth]]}]]])

(def start-spec "Malli spec for start message" [:map])

(def stop-spec "Malli spec for stop message" [:map])

(def next-spec "Malli spec for next message" [:map])

(def calibrate-start-long-spec
  "Malli spec for calibrate-start-long message"
  [:map])

(def calibrate-start-short-spec
  "Malli spec for calibrate-start-short message"
  [:map])

(def calibrate-next-spec "Malli spec for calibrate-next message" [:map])

(def calibrate-cencel-spec "Malli spec for calibrate-cencel message" [:map])

(def get-meteo-spec "Malli spec for get-meteo message" [:map])

(def set-magnetic-declination-spec
  "Malli spec for set-magnetic-declination message"
  [:map [:value [:maybe :float]]])

(def set-offset-angle-azimuth-spec
  "Malli spec for set-offset-angle-azimuth message"
  [:map [:value [:maybe :float]]])

(def set-offset-angle-elevation-spec
  "Malli spec for set-offset-angle-elevation message"
  [:map [:value [:maybe :float]]])

(def set-use-rotary-position-spec
  "Malli spec for set-use-rotary-position message"
  [:map [:flag [:maybe :boolean]]])

;; =============================================================================
;; Builders and Parsers
;; =============================================================================

;; Forward declarations
(declare build-root)
(declare build-start)
(declare build-stop)
(declare build-next)
(declare build-calibrate-start-long)
(declare build-calibrate-start-short)
(declare build-calibrate-next)
(declare build-calibrate-cencel)
(declare build-get-meteo)
(declare build-set-magnetic-declination)
(declare build-set-offset-angle-azimuth)
(declare build-set-offset-angle-elevation)
(declare build-set-use-rotary-position)
(declare parse-root)
(declare parse-start)
(declare parse-stop)
(declare parse-next)
(declare parse-calibrate-start-long)
(declare parse-calibrate-start-short)
(declare parse-calibrate-next)
(declare parse-calibrate-cencel)
(declare parse-get-meteo)
(declare parse-set-magnetic-declination)
(declare parse-set-offset-angle-azimuth)
(declare parse-set-offset-angle-elevation)
(declare parse-set-use-rotary-position)
(declare build-root-payload)
(declare parse-root-payload)

(defn build-root
  "Build a Root protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Root/newBuilder)]
    ;; Handle oneof: cmd
    (when-let [cmd-field (first
                           (filter (fn [[k v]]
                                     (#{:start :stop :set-magnetic-declination
                                        :set-offset-angle-azimuth
                                        :set-offset-angle-elevation
                                        :set-use-rotary-position
                                        :start-calibrate-long
                                        :start-calibrate-short :calibrate-next
                                        :calibrate-cencel :get-meteo}
                                      k))
                             m))]
      (build-root-payload builder cmd-field))
    (.build builder)))

(defn build-start
  "Build a Start protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Start/newBuilder)]
    (.build builder)))

(defn build-stop
  "Build a Stop protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Stop/newBuilder)]
    (.build builder)))

(defn build-next
  "Build a Next protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$Next/newBuilder)]
    (.build builder)))

(defn build-calibrate-start-long
  "Build a CalibrateStartLong protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateStartLong/newBuilder)]
    (.build builder)))

(defn build-calibrate-start-short
  "Build a CalibrateStartShort protobuf message from a map."
  [m]
  (let [builder
          (cmd.Compass.JonSharedCmdCompass$CalibrateStartShort/newBuilder)]
    (.build builder)))

(defn build-calibrate-next
  "Build a CalibrateNext protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateNext/newBuilder)]
    (.build builder)))

(defn build-calibrate-cencel
  "Build a CalibrateCencel protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$CalibrateCencel/newBuilder)]
    (.build builder)))

(defn build-get-meteo
  "Build a GetMeteo protobuf message from a map."
  [m]
  (let [builder (cmd.Compass.JonSharedCmdCompass$GetMeteo/newBuilder)]
    (.build builder)))

(defn build-set-magnetic-declination
  "Build a SetMagneticDeclination protobuf message from a map."
  [m]
  (let [builder
          (cmd.Compass.JonSharedCmdCompass$SetMagneticDeclination/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-offset-angle-azimuth
  "Build a SetOffsetAngleAzimuth protobuf message from a map."
  [m]
  (let [builder
          (cmd.Compass.JonSharedCmdCompass$SetOffsetAngleAzimuth/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-offset-angle-elevation
  "Build a SetOffsetAngleElevation protobuf message from a map."
  [m]
  (let [builder
          (cmd.Compass.JonSharedCmdCompass$SetOffsetAngleElevation/newBuilder)]
    ;; Set regular fields
    (when (contains? m :value) (.setValue builder (get m :value)))
    (.build builder)))

(defn build-set-use-rotary-position
  "Build a SetUseRotaryPosition protobuf message from a map."
  [m]
  (let [builder
          (cmd.Compass.JonSharedCmdCompass$SetUseRotaryPosition/newBuilder)]
    ;; Set regular fields
    (when (contains? m :flag) (.setFlag builder (get m :flag)))
    (.build builder)))

(defn parse-root
  "Parse a Root protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Root proto]
  (cond-> {}
    ;; Oneof payload
    true (merge (parse-root-payload proto))))

(defn parse-start
  "Parse a Start protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Start proto]
  {})

(defn parse-stop
  "Parse a Stop protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Stop proto]
  {})

(defn parse-next
  "Parse a Next protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$Next proto]
  {})

(defn parse-calibrate-start-long
  "Parse a CalibrateStartLong protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateStartLong proto]
  {})

(defn parse-calibrate-start-short
  "Parse a CalibrateStartShort protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateStartShort proto]
  {})

(defn parse-calibrate-next
  "Parse a CalibrateNext protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateNext proto]
  {})

(defn parse-calibrate-cencel
  "Parse a CalibrateCencel protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$CalibrateCencel proto]
  {})

(defn parse-get-meteo
  "Parse a GetMeteo protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$GetMeteo proto]
  {})

(defn parse-set-magnetic-declination
  "Parse a SetMagneticDeclination protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetMagneticDeclination proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-offset-angle-azimuth
  "Parse a SetOffsetAngleAzimuth protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetOffsetAngleAzimuth proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-offset-angle-elevation
  "Parse a SetOffsetAngleElevation protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetOffsetAngleElevation proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :value (.getValue proto))))

(defn parse-set-use-rotary-position
  "Parse a SetUseRotaryPosition protobuf message to a map."
  [^cmd.Compass.JonSharedCmdCompass$SetUseRotaryPosition proto]
  (cond-> {}
    ;; Regular fields
    true (assoc :flag (.getFlag proto))))

(defn build-root-payload
  "Build the oneof payload for Root."
  [builder [field-key value]]
  (case field-key
    :start (.setStart builder (build-start value))
    :stop (.setStop builder (build-stop value))
    :set-magnetic-declination (.setSetMagneticDeclination
                                builder
                                (build-set-magnetic-declination value))
    :set-offset-angle-azimuth
      (.setSetOffsetAngleAzimuth builder (build-set-offset-angle-azimuth value))
    :set-offset-angle-elevation (.setSetOffsetAngleElevation
                                  builder
                                  (build-set-offset-angle-elevation value))
    :set-use-rotary-position
      (.setSetUseRotaryPosition builder (build-set-use-rotary-position value))
    :start-calibrate-long
      (.setStartCalibrateLong builder (build-calibrate-start-long value))
    :start-calibrate-short
      (.setStartCalibrateShort builder (build-calibrate-start-short value))
    :calibrate-next (.setCalibrateNext builder (build-calibrate-next value))
    :calibrate-cencel (.setCalibrateCencel builder
                                           (build-calibrate-cencel value))
    :get-meteo (.setGetMeteo builder (build-get-meteo value))
    (throw (ex-info "Unknown oneof field" {:field field-key, :oneof ":cmd"}))))

(defn parse-root-payload
  "Parse the oneof payload from Root."
  [^cmd.Compass.JonSharedCmdCompass$Root proto]
  (cond (.hasStart proto) {:start (parse-start (.getStart proto))}
        (.hasStop proto) {:stop (parse-stop (.getStop proto))}
        (.hasSetMagneticDeclination proto)
          {:set-magnetic-declination (parse-set-magnetic-declination
                                       (.getSetMagneticDeclination proto))}
        (.hasSetOffsetAngleAzimuth proto)
          {:set-offset-angle-azimuth (parse-set-offset-angle-azimuth
                                       (.getSetOffsetAngleAzimuth proto))}
        (.hasSetOffsetAngleElevation proto)
          {:set-offset-angle-elevation (parse-set-offset-angle-elevation
                                         (.getSetOffsetAngleElevation proto))}
        (.hasSetUseRotaryPosition proto) {:set-use-rotary-position
                                            (parse-set-use-rotary-position
                                              (.getSetUseRotaryPosition proto))}
        (.hasStartCalibrateLong proto) {:start-calibrate-long
                                          (parse-calibrate-start-long
                                            (.getStartCalibrateLong proto))}
        (.hasStartCalibrateShort proto) {:start-calibrate-short
                                           (parse-calibrate-start-short
                                             (.getStartCalibrateShort proto))}
        (.hasCalibrateNext proto) {:calibrate-next (parse-calibrate-next
                                                     (.getCalibrateNext proto))}
        (.hasCalibrateCencel proto) {:calibrate-cencel (parse-calibrate-cencel
                                                         (.getCalibrateCencel
                                                           proto))}
        (.hasGetMeteo proto) {:get-meteo (parse-get-meteo (.getGetMeteo
                                                            proto))}))