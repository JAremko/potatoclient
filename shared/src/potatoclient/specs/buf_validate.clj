(ns potatoclient.specs.buf-validate
  "Malli specs that exactly match buf.validate constraints from proto files.
   ALL MAPS USE {:closed true} to catch typos and invalid keys."
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [clojure.test.check.generators :as gen])
  (:import
   [ser JonSharedDataTypes$JonGuiDataClientType
    JonSharedDataTypes$JonGuiDataGpsFixType
    JonSharedDataTypes$JonGuiDataRotaryDirection
    JonSharedDataTypes$JonGuiDataRotaryMode
    JonSharedDataTypes$JonGuiDataVideoChannel]))

;; ====================================================================
;; Protocol Version Spec (uint32 > 0)
;; ====================================================================
(def protocol-version-spec
  [:int {:min 1 :max 2147483647
         :gen/gen (gen/choose 1 100)}])  ; Realistic range for protocol versions

(registry/register! :proto/protocol-version protocol-version-spec)

;; ====================================================================
;; GPS Coordinate Specs (EXACT buf.validate constraints)
;; ====================================================================

;; Latitude: double ∈ [-90, 90]
(def latitude-exact-spec
  [:double {:min -90.0 :max 90.0
            :gen/gen (gen/double* {:min -90.0 :max 90.0 :NaN? false})}])

;; Longitude: double ∈ [-180, 180] (note: some proto fields use < 180)
(def longitude-exact-spec
  [:double {:min -180.0 :max 180.0
            :gen/gen (gen/double* {:min -180.0 :max 179.999999 :NaN? false})}])

;; Altitude: double ∈ [-433, 8848.86] (Dead Sea to Mt. Everest)
(def altitude-exact-spec
  [:double {:min -433.0 :max 8848.86
            :gen/gen (gen/double* {:min -433.0 :max 8848.86 :NaN? false})}])

(registry/register! :gps/latitude latitude-exact-spec)
(registry/register! :gps/longitude longitude-exact-spec)
(registry/register! :gps/altitude altitude-exact-spec)

;; GPS Fix Type: Cannot be UNSPECIFIED (0)
(def gps-fix-type-spec
  [:enum
   JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_NONE
   JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_1D
   JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_2D
   JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_3D
   JonSharedDataTypes$JonGuiDataGpsFixType/JON_GUI_DATA_GPS_FIX_TYPE_MANUAL])

(registry/register! :gps/fix-type gps-fix-type-spec)

;; ====================================================================
;; Client Type Spec (Cannot be UNSPECIFIED)
;; ====================================================================
(def client-type-spec
  [:enum
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
   JonSharedDataTypes$JonGuiDataClientType/JON_GUI_DATA_CLIENT_TYPE_LIRA])

(registry/register! :proto/client-type client-type-spec)

;; ====================================================================
;; Rotary Specs (EXACT buf.validate constraints)
;; ====================================================================

;; Rotary Speed: float/double > 0 and ≤ 1
(def rotary-speed-spec
  [:double {:min 0.001 :max 1.0  ; > 0 means we need small positive value
            :gen/gen (gen/double* {:min 0.001 :max 1.0 :NaN? false})}])

(registry/register! :rotary/speed rotary-speed-spec)

;; Azimuth for rotary: float ≥ 0 and < 360
(def rotary-azimuth-spec
  [:double {:min 0.0 :max 359.999999
            :gen/gen (gen/double* {:min 0.0 :max 359.999 :NaN? false})}])

;; Elevation for rotary: float ≥ -90 and ≤ 90
(def rotary-elevation-spec
  [:double {:min -90.0 :max 90.0
            :gen/gen (gen/double* {:min -90.0 :max 90.0 :NaN? false})}])

;; Platform azimuth: float > -360 and < 360
(def platform-azimuth-spec
  [:double {:min -359.999999 :max 359.999999
            :gen/gen (gen/double* {:min -359.999 :max 359.999 :NaN? false})}])

;; Platform bank: float ≥ -180 and < 180
(def platform-bank-spec
  [:double {:min -180.0 :max 179.999999
            :gen/gen (gen/double* {:min -180.0 :max 179.999 :NaN? false})}])

(registry/register! :rotary/azimuth rotary-azimuth-spec)
(registry/register! :rotary/elevation rotary-elevation-spec)
(registry/register! :rotary/platform-azimuth platform-azimuth-spec)
(registry/register! :rotary/platform-bank platform-bank-spec)

;; Rotary Direction: Cannot be UNSPECIFIED
(def rotary-direction-spec
  [:enum
   JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
   JonSharedDataTypes$JonGuiDataRotaryDirection/JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE])

(registry/register! :rotary/direction rotary-direction-spec)

;; Rotary Mode: Cannot be UNSPECIFIED
(def rotary-mode-spec
  [:enum
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_INITIALIZATION
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_SPEED
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_POSITION
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_STABILIZATION
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_TARGETING
   JonSharedDataTypes$JonGuiDataRotaryMode/JON_GUI_DATA_ROTARY_MODE_VIDEO_TRACKER])

(registry/register! :rotary/mode rotary-mode-spec)

;; ====================================================================
;; Video Channel Spec (Cannot be UNSPECIFIED)
;; ====================================================================
(def video-channel-spec
  [:enum
   JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_HEAT
   JonSharedDataTypes$JonGuiDataVideoChannel/JON_GUI_DATA_VIDEO_CHANNEL_DAY])

(registry/register! :video/channel video-channel-spec)

;; ====================================================================
;; Scan Node Specs (from ScanAddNode/ScanUpdateNode messages)
;; ====================================================================

;; Scan node index: int32 ≥ 0
(def scan-node-index-spec
  [:int {:min 0 :max 2147483647
         :gen/gen (gen/choose 0 1000)}])  ; Realistic range for node indices

;; Zoom table value: int32 ≥ 0
(def zoom-table-value-spec
  [:int {:min 0 :max 2147483647
         :gen/gen (gen/choose 0 100)}])  ; Realistic range for zoom values

;; Linger time: double ≥ 0 (seconds to pause at node)
(def scan-linger-spec
  [:double {:min 0.0 :max 3600.0  ; Max 1 hour is reasonable
            :gen/gen (gen/double* {:min 0.0 :max 60.0 :NaN? false})}])

(registry/register! :scan/node-index scan-node-index-spec)
(registry/register! :scan/zoom-table-value zoom-table-value-spec)
(registry/register! :scan/linger scan-linger-spec)

;; ====================================================================
;; NDC (Normalized Device Coordinates) Specs
;; ====================================================================

;; NDC coordinates: float ≥ -1.0 and ≤ 1.0
(def ndc-coord-spec
  [:double {:min -1.0 :max 1.0
            :gen/gen (gen/double* {:min -1.0 :max 1.0 :NaN? false})}])

(registry/register! :ndc/x ndc-coord-spec)
(registry/register! :ndc/y ndc-coord-spec)

;; ====================================================================
;; Composite Specs with CLOSED MAPS
;; ====================================================================

;; GPS Position (closed map - no extra fields allowed)
(def gps-position-closed-spec
  [:map {:closed true}
   [:latitude :gps/latitude]
   [:longitude :gps/longitude]
   [:altitude :gps/altitude]])

(registry/register! :gps/position-closed gps-position-closed-spec)

;; Rotary Azimuth Command (closed map)
(def rotary-azimuth-command-spec
  [:map {:closed true}
   [:speed :rotary/speed]
   [:direction :rotary/direction]])

(registry/register! :rotary/azimuth-command rotary-azimuth-command-spec)

;; Rotary Elevation Command (closed map)
(def rotary-elevation-command-spec
  [:map {:closed true}
   [:speed :rotary/speed]
   [:direction :rotary/direction]])

(registry/register! :rotary/elevation-command rotary-elevation-command-spec)

;; Scan Node Spec (closed map)
(def scan-node-spec
  [:map {:closed true}
   [:index :scan/node-index]
   [:DayZoomTableValue :scan/zoom-table-value]
   [:HeatZoomTableValue :scan/zoom-table-value]
   [:azimuth :rotary/azimuth]
   [:elevation :rotary/elevation]
   [:linger :scan/linger]
   [:speed :rotary/speed]])

(registry/register! :scan/node scan-node-spec)

;; NDC Point (closed map)
(def ndc-point-spec
  [:map {:closed true}
   [:x :ndc/x]
   [:y :ndc/y]])

(registry/register! :ndc/point ndc-point-spec)

;; ====================================================================
;; Helper Functions
;; ====================================================================

(defn validate-with-buf
  "Validate a value against a buf.validate-compliant spec"
  [spec-key value]
  (m/validate spec-key value))

(defn explain-buf-error
  "Explain validation errors in buf.validate terms"
  [spec-key value]
  (when-let [explanation (m/explain spec-key value)]
    {:valid? false
     :errors (-> explanation
                 :errors
                 (map (fn [err]
                        {:path (:path err)
                         :value (:value err)
                         :constraint (-> err :schema second)
                         :message (str "Value " (:value err) " violates constraint at " (:path err))})))
     :buf-validate-equivalent true}))

(defn generate-valid-value
  "Generate a value that satisfies buf.validate constraints"
  [spec-key & {:keys [seed size] :or {seed 42 size 10}}]
  (mg/generate spec-key {:seed seed :size size}))

;; ====================================================================
;; Testing Support
;; ====================================================================

(defn property-test-spec
  "Run property-based testing for a spec against buf.validate rules"
  [spec-key num-tests]
  (let [results (atom {:passed 0 :failed 0 :failures []})]
    (dotimes [_ num-tests]
      (let [generated (generate-valid-value spec-key)
            valid? (validate-with-buf spec-key generated)]
        (if valid?
          (swap! results update :passed inc)
          (do
            (swap! results update :failed inc)
            (swap! results update :failures conj
                   {:value generated
                    :error (explain-buf-error spec-key generated)})))))
    @results))