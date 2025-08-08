(ns cmd-explorer.specs.shared
  "Common reusable specs for cmd-explorer"
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [cmd-explorer.registry :as registry]
   [clojure.test.check.generators :as gen]))

;; Angle specs (degrees)
(def azimuth-spec
  [:double {:min -180.0 :max 180.0
            :gen/gen (gen/double* {:min -180.0 :max 180.0 :NaN? false})}])

(def elevation-spec
  [:double {:min -90.0 :max 90.0
            :gen/gen (gen/double* {:min -90.0 :max 90.0 :NaN? false})}])

(def bank-spec
  [:double {:min -180.0 :max 180.0
            :gen/gen (gen/double* {:min -180.0 :max 180.0 :NaN? false})}])

;; Register angle specs
(registry/register! :angle/azimuth azimuth-spec)
(registry/register! :angle/elevation elevation-spec)
(registry/register! :angle/bank bank-spec)

;; Range specs (normalized 0-1)
(def normalized-range-spec
  [:double {:min 0.0 :max 1.0
            :gen/gen (gen/double* {:NaN? false :min 0.0 :max 1.0})}])

(def zoom-level-spec normalized-range-spec)
(def focus-level-spec normalized-range-spec)

;; Register range specs
(registry/register! :range/normalized normalized-range-spec)
(registry/register! :range/zoom zoom-level-spec)
(registry/register! :range/focus focus-level-spec)

;; Position specs (GPS coordinates)
(def latitude-spec
  [:double {:min -90.0 :max 90.0
            :gen/gen (gen/double* {:NaN? false :min -90.0 :max 90.0})}])

(def longitude-spec
  [:double {:min -180.0 :max 180.0
            :gen/gen (gen/double* {:NaN? false :min -180.0 :max 180.0})}])

(def altitude-spec
  [:double {:min -1000.0 :max 10000.0
            :gen/gen (gen/double* {:NaN? false :min -1000.0 :max 10000.0})}])

;; Register position specs
(registry/register! :position/latitude latitude-spec)
(registry/register! :position/longitude longitude-spec)
(registry/register! :position/altitude altitude-spec)

;; Speed specs
(def speed-kmh-spec
  [:double {:min 0.0 :max 500.0
            :gen/gen (gen/double* {:NaN? false :min 0.0 :max 500.0})}])

(def speed-ms-spec
  [:double {:min 0.0 :max 150.0
            :gen/gen (gen/double* {:NaN? false :min 0.0 :max 150.0})}])

;; Register speed specs
(registry/register! :speed/kmh speed-kmh-spec)
(registry/register! :speed/ms speed-ms-spec)

;; Temperature specs
(def temperature-celsius-spec
  [:double {:min -50.0 :max 100.0
            :gen/gen (gen/double* {:NaN? false :min -50.0 :max 100.0})}])

;; Register temperature specs
(registry/register! :temperature/celsius temperature-celsius-spec)

;; Distance specs
(def distance-meters-spec
  [:double {:min 0.0 :max 50000.0
            :gen/gen (gen/double* {:NaN? false :min 0.0 :max 50000.0})}])

;; Register distance specs
(registry/register! :distance/meters distance-meters-spec)

;; Screen coordinate specs (NDC - Normalized Device Coordinates)
(def ndc-coord-spec
  [:double {:min -1.0 :max 1.0
            :gen/gen (gen/double* {:NaN? false :min -1.0 :max 1.0})}])

;; Register NDC specs
(registry/register! :screen/ndc-x ndc-coord-spec)
(registry/register! :screen/ndc-y ndc-coord-spec)

;; Pixel coordinate specs
(def pixel-coord-spec
  [:int {:min 0 :max 4096
         :gen/gen (gen/choose 0 4096)}])

;; Register pixel specs
(registry/register! :screen/pixel-x pixel-coord-spec)
(registry/register! :screen/pixel-y pixel-coord-spec)

;; Time specs
(def unix-timestamp-spec
  [:int {:min 0 :max 2147483647
         :gen/gen (gen/choose 1000000000 2147483647)}])

(def duration-seconds-spec
  [:int {:min 0 :max 86400
         :gen/gen (gen/choose 0 86400)}])

;; Register time specs
(registry/register! :time/unix-timestamp unix-timestamp-spec)
(registry/register! :time/duration-seconds duration-seconds-spec)

;; Boolean mode specs
(def enable-disable-spec :boolean)
(def on-off-spec :boolean)

;; Register boolean specs
(registry/register! :mode/enable enable-disable-spec)
(registry/register! :mode/on-off on-off-spec)

;; ID specs
(def session-id-spec
  [:int {:min 0 :max 2147483647
         :gen/gen (gen/choose 0 2147483647)}])

(def track-id-spec
  [:int {:min 0 :max 65535
         :gen/gen (gen/choose 0 65535)}])

;; Register ID specs
(registry/register! :id/session session-id-spec)
(registry/register! :id/track track-id-spec)

;; Percentage specs
(def percentage-spec
  [:double {:min 0.0 :max 100.0
            :gen/gen (gen/double* {:NaN? false :min 0.0 :max 100.0})}])

;; Register percentage spec
(registry/register! :percentage percentage-spec)

;; Helper functions for validation
(defn validate-spec
  "Validate a value against a registered spec"
  [spec-key value]
  (m/validate spec-key value))

(defn explain-spec
  "Explain validation errors for a value against a spec"
  [spec-key value]
  (m/explain spec-key value))

(defn generate-spec
  "Generate a sample value for a registered spec"
  [spec-key]
  (mg/generate spec-key))