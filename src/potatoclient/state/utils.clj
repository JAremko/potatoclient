(ns potatoclient.state.utils
  "Utility functions for state management.
  
  Port of TypeScript utility functions from deviceStateDispatch.ts
  for formatting timestamps, durations, and working with 64-bit values."
  (:require [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn =>]]))

;; ============================================================================
;; Timestamp Formatting
;; ============================================================================

(>defn format-timestamp
  "Format a nanosecond timestamp to a human-readable format.
  
  Args:
    timestamp - The timestamp in nanoseconds (long)
    include-nanos? - Whether to include nanoseconds in output (default false)
    
  Returns:
    Formatted timestamp string as MM:SS.mmm or MM:SS.mmmnnnnn"
  ([timestamp]
   [int? => string?]
   (format-timestamp timestamp false))
  ([timestamp include-nanos?]
   [int? boolean? => string?]
   (try
     (let [total-nanoseconds timestamp
           total-milliseconds (quot total-nanoseconds 1000000)
           minutes (quot total-milliseconds 60000)
           seconds (quot (rem total-milliseconds 60000) 1000)
           milliseconds (rem total-milliseconds 1000)
           
           ;; Format basic time as MM:SS.mmm
           base-format (format "%02d:%02d.%03d" minutes seconds milliseconds)]
       
       (if include-nanos?
         ;; Get remaining nanoseconds for more precision
         (let [nanos-only (rem total-nanoseconds 1000000)
               nanos-str (format "%06d" nanos-only)]
           (str base-format nanos-str))
         base-format))
     (catch Exception _
       "Invalid timestamp"))))

(>defn format-duration
  "Format a duration from nanoseconds to human-readable format.
  
  Args:
    duration - Duration in nanoseconds (long)
    
  Returns:
    Formatted duration string (e.g., '33.3ms', '1.2s', '500ns')"
  [duration]
  [int? => string?]
  (try
    (let [nanoseconds duration]
      (cond
        ;; Less than 1 microsecond
        (< nanoseconds 1000)
        (str nanoseconds "ns")
        
        ;; Less than 1 millisecond
        (< nanoseconds 1000000)
        (format "%.1fµs" (/ nanoseconds 1000.0))
        
        ;; Less than 1 second
        (< nanoseconds 1000000000)
        (format "%.1fms" (/ nanoseconds 1000000.0))
        
        ;; 1 second or more
        :else
        (format "%.2fs" (/ nanoseconds 1000000000.0))))
    (catch Exception _
      "Invalid duration")))

;; ============================================================================
;; Time Calculations
;; ============================================================================

(>defn get-time-difference-ms
  "Calculate time difference between two timestamps in milliseconds.
  
  Args:
    timestamp1 - First timestamp in nanoseconds
    timestamp2 - Second timestamp in nanoseconds
    
  Returns:
    Absolute time difference in milliseconds"
  [timestamp1 timestamp2]
  [int? int? => int?]
  (try
    (let [diff-ns (Math/abs (- timestamp2 timestamp1))]
      (quot diff-ns 1000000))
    (catch Exception _
      0)))

(>defn within-safe-range?
  "Check if a timestamp is within the safe integer range.
  
  In Clojure/Java, we have 64-bit longs so this is less of a concern
  than in JavaScript, but we maintain the function for compatibility."
  [timestamp]
  [int? => boolean?]
  (try
    (and (<= timestamp Long/MAX_VALUE)
         (>= timestamp Long/MIN_VALUE))
    (catch Exception _
      false)))

;; ============================================================================
;; Frame Data Support
;; ============================================================================

(>defn create-frame-data
  "Create a frame data map with timestamp and duration.
  
  Args:
    timestamp - Frame timestamp in nanoseconds
    duration - Frame duration in nanoseconds
    
  Returns:
    Map with :timestamp and :duration keys"
  [timestamp duration]
  [int? int? => [:map
                 [:timestamp int?]
                 [:duration int?]]]
  {:timestamp timestamp
   :duration duration})

(>defn frame-fps
  "Calculate frames per second from frame duration.
  
  Args:
    duration - Frame duration in nanoseconds
    
  Returns:
    FPS as a double, or 0.0 if invalid"
  [duration]
  [int? => double?]
  (if (pos? duration)
    (/ 1000000000.0 duration)
    0.0))

(>defn format-fps
  "Format FPS value for display.
  
  Args:
    fps - Frames per second
    
  Returns:
    Formatted string like '30.0 fps' or '29.97 fps'"
  [fps]
  [number? => string?]
  (if (zero? fps)
    "0 fps"
    (format "%.2f fps" (double fps))))

;; ============================================================================
;; Coordinate Formatting
;; ============================================================================

(>defn format-latitude
  "Format latitude for display with hemisphere indicator.
  
  Args:
    lat - Latitude in decimal degrees (-90 to 90)
    
  Returns:
    Formatted string like '45.123° N' or '33.456° S'"
  [lat]
  [number? => string?]
  (let [abs-lat (Math/abs lat)
        hemisphere (if (>= lat 0) "N" "S")]
    (format "%.6f° %s" abs-lat hemisphere)))

(>defn format-longitude
  "Format longitude for display with hemisphere indicator.
  
  Args:
    lon - Longitude in decimal degrees (-180 to 180)
    
  Returns:
    Formatted string like '123.456° E' or '45.678° W'"
  [lon]
  [number? => string?]
  (let [abs-lon (Math/abs lon)
        hemisphere (if (>= lon 0) "E" "W")]
    (format "%.6f° %s" abs-lon hemisphere)))

(>defn format-altitude
  "Format altitude for display with units.
  
  Args:
    alt - Altitude in meters
    
  Returns:
    Formatted string like '1234.5 m'"
  [alt]
  [number? => string?]
  (format "%.1f m" (double alt)))

;; ============================================================================
;; Angle Formatting
;; ============================================================================

(>defn format-angle
  "Format angle in degrees with optional precision.
  
  Args:
    angle - Angle in degrees
    precision - Decimal places (default 1)
    
  Returns:
    Formatted string like '45.5°'"
  ([angle]
   [number? => string?]
   (format-angle angle 1))
  ([angle precision]
   [number? int? => string?]
   (let [fmt-str (str "%." precision "f°")]
     (format fmt-str (double angle)))))

(>defn format-heading
  "Format compass heading with cardinal direction.
  
  Args:
    heading - Heading in degrees (0-360)
    
  Returns:
    Formatted string like '045° (NE)'"
  [heading]
  [number? => string?]
  (let [normalized (mod heading 360)
        cardinal (cond
                   (< normalized 11.25) "N"
                   (< normalized 33.75) "NNE"
                   (< normalized 56.25) "NE"
                   (< normalized 78.75) "ENE"
                   (< normalized 101.25) "E"
                   (< normalized 123.75) "ESE"
                   (< normalized 146.25) "SE"
                   (< normalized 168.75) "SSE"
                   (< normalized 191.25) "S"
                   (< normalized 213.75) "SSW"
                   (< normalized 236.25) "SW"
                   (< normalized 258.75) "WSW"
                   (< normalized 281.25) "W"
                   (< normalized 303.75) "WNW"
                   (< normalized 326.25) "NW"
                   (< normalized 348.75) "NNW"
                   :else "N")]
    (format "%03.0f° (%s)" normalized cardinal)))

;; ============================================================================
;; Distance Formatting
;; ============================================================================

(>defn format-distance
  "Format distance with appropriate units.
  
  Args:
    distance-dm - Distance in decimeters
    
  Returns:
    Formatted string with automatic unit selection"
  [distance-dm]
  [number? => string?]
  (let [meters (/ distance-dm 10.0)]
    (cond
      (< meters 1000)
      (format "%.1f m" meters)
      
      :else
      (format "%.2f km" (/ meters 1000.0)))))

;; ============================================================================
;; Temperature Formatting
;; ============================================================================

(>defn format-temperature
  "Format temperature with unit.
  
  Args:
    temp - Temperature in Celsius
    
  Returns:
    Formatted string like '23.5°C'"
  [temp]
  [number? => string?]
  (format "%.1f°C" (double temp)))

;; ============================================================================
;; Percentage Formatting
;; ============================================================================

(>defn format-percentage
  "Format percentage value.
  
  Args:
    value - Percentage value (0-100)
    
  Returns:
    Formatted string like '75%'"
  [value]
  [number? => string?]
  (format "%.0f%%" (double value)))