(ns potatoclient.specs
  "Shared Malli schemas for the PotatoClient application.
   This namespace contains all the common schemas used across multiple namespaces."
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [malli.util :as mu]
            [clojure.core.async.impl.channels]
            [clojure.data.json])
  (:import (clojure.core.async.impl.channels ManyToManyChannel)
           (clojure.lang Atom)
           (java.awt Color Rectangle)
           (java.io BufferedReader BufferedWriter File)
           (javax.swing AbstractButton Action Icon JFrame JMenu JMenuBar JPanel JScrollPane JTextField JToggleButton)
           (javax.swing.table DefaultTableCellRenderer)))

;; -----------------------------------------------------------------------------
;; Core Domain Schemas
;; -----------------------------------------------------------------------------

(def theme-key
  "Valid theme identifiers"
  [:enum :sol-light :sol-dark :dark :hi-dark])

(def domain
  "Domain name or IP address - validates hosts that can be used for WebSocket connections"
  [:and
   string?
   [:fn {:error/message "must be a valid domain name or IP address"}
    (fn [s]
      (and (not (clojure.string/blank? s))
           (let [;; IPv4 pattern
                 ip-pattern #"^(\d{1,3}\.){3}\d{1,3}$"
                 ;; Domain pattern - allows subdomains, hyphens, ports
                 ;; More permissive than standard to allow .local domains, etc.
                 domain-pattern #"^[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?)*$"]
             (or (re-matches ip-pattern s)
                 (re-matches domain-pattern s)))))]])

(def locale
  "Application locale"
  [:enum :english :ukrainian])

(def locale-code
  "Language code for i18n"
  [:enum :en :uk])

(def stream-key
  "Stream identifier"
  [:enum :heat :day])

;; -----------------------------------------------------------------------------
;; Configuration Schemas
;; -----------------------------------------------------------------------------

(def config-key
  "Valid configuration keys"
  [:enum :theme :domain :locale :url-history])

(def url-history-entry
  "URL history entry with timestamp"
  [:map
   [:url string?]
   [:timestamp inst?]])

(def url-history
  "Set of previously used URLs with timestamps (max 10)"
  [:set url-history-entry])

(def config
  "Application configuration - open schema to allow future extensions"
  [:map {:closed false}
   [:theme theme-key]
   [:domain {:optional true} domain] ;; Keep for backward compatibility only
   [:url-history {:optional true} url-history] ;; History of URLs
   [:locale locale]])

;; -----------------------------------------------------------------------------
;; Process Schemas
;; -----------------------------------------------------------------------------

(def process
  "Java Process instance"
  [:fn #(instance? Process %)])

(def process-state
  "Process lifecycle state"
  [:enum :starting :running :stopping :stopped :failed])

(def stream-process-map
  "Complete stream process structure"
  [:map
   [:process process]
   [:writer [:fn #(instance? BufferedWriter %)]]
   [:stdout-reader [:fn #(instance? BufferedReader %)]]
   [:stderr-reader [:fn #(instance? BufferedReader %)]]
   [:output-chan [:fn {:error/message "must be a core.async channel"}
                  #(instance? ManyToManyChannel %)]]
   [:stream-id string?]
   [:state [:fn {:error/message "must be an atom containing process state"}
            #(and (instance? Atom %)
                  (contains? #{:starting :running :stopping :stopped :failed} @%))]]])

;; -----------------------------------------------------------------------------
;; State Schemas
;; -----------------------------------------------------------------------------

(def stream-process
  "Stream process information"
  [:maybe stream-process-map])

(def process-command
  "Command to send to process - JSON serializable map with action"
  [:and
   [:map {:closed false}
    [:action [:enum "show" "shutdown" "ping" "noop"]]]
   [:fn {:error/message "values must be JSON serializable"}
    (fn [m]
      (try
        (clojure.data.json/write-str m)
        true
        (catch Exception _ false)))]])

;; -----------------------------------------------------------------------------
;; IPC/Message Schemas
;; -----------------------------------------------------------------------------

(def message-type
  "IPC message types"
  [:enum :response :log :navigation :window])

(def message
  "IPC message structure"
  [:map {:closed false}
   [:type message-type]])

;; -----------------------------------------------------------------------------
;; Protocol Schemas
;; -----------------------------------------------------------------------------

(def protocol-version
  "Protocol version number"
  pos-int?)

(def session-id
  "Session identifier"
  pos-int?)

(def important
  "Important flag"
  boolean?)

(def from-cv-subsystem
  "CV subsystem flag"
  boolean?)

(def client-type
  "Client type values"
  [:enum :java :rust :go :clojure])

(def payload-type
  "Command payload types"
  [:enum :ping :pong :noop :frozen :gimbal-angle-to
   :lrf-request :lrf-single-pulse])

(def payload
  "Command payload - must match protobuf payload types"
  [:map-of
   [:enum :ping :pong :noop :frozen :gimbal-angle-to :lrf-request :lrf-single-pulse]
   [:map]])

(def command
  "Protocol command structure"
  [:map
   [:protocol-version protocol-version]
   [:session-id session-id]
   [:important important]
   [:from-cv-subsystem from-cv-subsystem]
   [:client-type client-type]
   [:payload-type payload-type]
   [:payload payload]])

;; -----------------------------------------------------------------------------
;; UI Event Schemas
;; -----------------------------------------------------------------------------

(def event-type
  "UI event type"
  [:enum
   ;; Window events
   :resized :moved :maximized :unmaximized :minimized
   :restored :focused :unfocused :opened :closing
   ;; Mouse events
   :mouse-click :mouse-press :mouse-release :mouse-move
   :mouse-drag-start :mouse-drag :mouse-drag-end
   :mouse-enter :mouse-exit :mouse-wheel])

(def x-coord
  "X coordinate in pixels"
  int?)

(def y-coord
  "Y coordinate in pixels"
  int?)

(def ndc-x
  "Normalized device coordinate X (-1.0 to 1.0)"
  [:and number? [:>= -1.0] [:<= 1.0]])

(def ndc-y
  "Normalized device coordinate Y (-1.0 to 1.0)"
  [:and number? [:>= -1.0] [:<= 1.0]])

(def mouse-button
  "Mouse button number"
  [:enum 1 2 3])

(def click-count
  "Click count"
  pos-int?)

(def wheel-rotation
  "Mouse wheel rotation - negative for up, positive for down"
  int?)

(def window-event
  "Window event structure"
  [:map
   [:type event-type]
   [:x x-coord]
   [:y y-coord]
   [:width {:optional true} pos-int?]
   [:height {:optional true} pos-int?]
   [:button {:optional true} mouse-button]
   [:clickCount {:optional true} click-count]
   [:wheelRotation {:optional true} wheel-rotation]])

(def navigation-event
  "Navigation event structure"
  [:map
   [:type event-type]
   [:ndcX ndc-x]
   [:ndcY ndc-y]
   [:button {:optional true} mouse-button]
   [:clickCount {:optional true} click-count]
   [:wheelRotation {:optional true} wheel-rotation]])

;; -----------------------------------------------------------------------------
;; Java Interop Schemas
;; -----------------------------------------------------------------------------

(def file
  "Java File instance"
  [:fn #(instance? File %)])

(def color
  "Java AWT Color instance"
  [:fn #(instance? Color %)])

(def rectangle
  "Java AWT Rectangle instance"
  [:fn #(instance? Rectangle %)])

(def icon
  "Swing Icon instance"
  [:maybe [:fn #(instance? Icon %)]])

(def exception
  "Java Exception instance"
  [:fn #(instance? Exception %)])

;; -----------------------------------------------------------------------------
;; Concurrency Schemas
;; -----------------------------------------------------------------------------

(def future-instance
  "Java Future instance"
  [:fn {:error/message "must be a Future"} future?])

;; -----------------------------------------------------------------------------
;; UI Component Schemas
;; -----------------------------------------------------------------------------

(def jframe
  "Swing JFrame instance"
  [:fn #(instance? JFrame %)])

(def jpanel
  "Swing JPanel instance"
  [:fn #(instance? JPanel %)])

(def jbutton
  "Swing button instance"
  [:fn #(instance? AbstractButton %)])

(def jtext-field
  "Swing JTextField instance"
  [:fn #(instance? JTextField %)])

(def jscroll-pane
  "Swing JScrollPane instance"
  [:fn #(instance? JScrollPane %)])

(def jmenu-bar
  "Swing JMenuBar instance"
  [:fn #(instance? JMenuBar %)])

(def jmenu
  "Swing JMenu instance"
  [:fn #(instance? JMenu %)])

(def action
  "Swing Action instance"
  [:fn #(instance? Action %)])

(def jtoggle-button
  "Swing JToggleButton instance"
  [:fn #(instance? JToggleButton %)])

(def table-cell-renderer
  "Swing table cell renderer"
  [:fn #(instance? DefaultTableCellRenderer %)])

;; -----------------------------------------------------------------------------
;; UI State Schemas
;; -----------------------------------------------------------------------------

(def extended-state
  "JFrame extended state"
  int?)

(def window-state
  "Window state information"
  [:map
   [:bounds rectangle]
   [:extended-state extended-state]])

(def frame-params
  "Parameters for creating main frame"
  [:map
   [:version string?]
   [:build-type string?]
   [:title {:optional true} string?]])

;; -----------------------------------------------------------------------------
;; I18n Schemas
;; -----------------------------------------------------------------------------

(def translation-key
  "Translation key"
  keyword?)

(def translation-args
  "Translation arguments - typically strings, numbers, or keywords"
  [:vector [:or string? number? keyword?]])

(def translations-map
  "Translation definitions"
  [:map-of keyword? [:map-of keyword? string?]])

;; -----------------------------------------------------------------------------
;; Command Domain Schemas
;; -----------------------------------------------------------------------------

(def azimuth-degrees
  "Azimuth angle in degrees [0, 360)"
  [:double {:min 0.0 :max 360.0}])

(def elevation-degrees
  "Elevation angle in degrees [-90, 90]"
  [:double {:min -90.0 :max 90.0}])

(def rotation-speed
  "Rotation speed normalized [0, 1]"
  [:double {:min 0.0 :max 1.0}])

(def gps-latitude
  "GPS latitude in degrees [-90, 90]"
  [:double {:min -90.0 :max 90.0}])

(def gps-longitude
  "GPS longitude in degrees [-180, 180)"
  [:double {:min -180.0 :max 180.0}])

(def gps-altitude
  "GPS altitude in meters (Dead Sea to Everest)"
  [:double {:min -433.0 :max 8848.86}])

(def zoom-level
  "Zoom level normalized [0, 1]"
  [:double {:min 0.0 :max 1.0}])

(def zoom-table-index
  "Zoom table index value"
  [:int {:min 0}])

(def digital-zoom-level
  "Digital zoom level [1.0, ∞)"
  [:double {:min 1.0}])

(def focus-value
  "Focus value normalized [0, 1]"
  [:double {:min 0.0 :max 1.0}])

(def iris-value
  "Iris value normalized [0, 1]"
  [:double {:min 0.0 :max 1.0}])

(def clahe-level
  "CLAHE level normalized [0, 1]"
  [:double {:min 0.0 :max 1.0}])

(def clahe-shift
  "CLAHE shift value [-1, 1]"
  [:double {:min -1.0 :max 1.0}])

(def relative-angle
  "Relative angle in degrees (-360, 360)"
  [:double {:min -360.0 :max 360.0}])

(def bank-angle
  "Bank angle in degrees [-180, 180)"
  [:double {:min -180.0 :max 180.0}])

(def scan-linger-time
  "Scan linger time in seconds"
  [:double {:min 0.0}])

(def scan-node-index
  "Scan node index"
  [:int {:min 0}])

;; -----------------------------------------------------------------------------
;; Protobuf Types
;; -----------------------------------------------------------------------------

(def protobuf-message
  "Google Protobuf Message instance"
  [:fn {:error/message "must be a protobuf message"}
   #(instance? com.google.protobuf.Message %)])

(def protobuf-builder
  "Google Protobuf Message Builder instance"
  [:fn {:error/message "must be a protobuf message builder"}
   #(instance? com.google.protobuf.Message$Builder %)])

;; -----------------------------------------------------------------------------
;; Command Enum Types
;; -----------------------------------------------------------------------------

(def rotary-direction
  "Rotary platform direction enum"
  [:fn {:error/message "must be a JonGuiDataRotaryDirection enum"}
   #(instance? data.JonSharedDataTypes$JonGuiDataRotaryDirection %)])

(def rotary-mode
  "Rotary platform mode enum"
  [:fn {:error/message "must be a JonGuiDataRotaryMode enum"}
   #(instance? data.JonSharedDataTypes$JonGuiDataRotaryMode %)])

(def video-channel
  "Video channel enum"
  [:fn {:error/message "must be a JonGuiDataVideoChannel enum"}
   #(instance? data.JonSharedDataTypes$JonGuiDataVideoChannel %)])

;; TODO: These enums don't exist in the current protobuf definitions
;; (def day-camera-palette
;;   "Day camera palette enum"
;;   [:fn {:error/message "must be a JonGuiDataDayCameraPalette enum"}
;;    #(instance? data.JonSharedDataTypes$JonGuiDataDayCameraPalette %)])

;; Temporary definition until protobuf is fixed
(def day-camera-palette
  "Day camera palette enum"
  any?)

;; (def day-camera-agc-mode
;;   "Day camera AGC mode enum"
;;   [:fn {:error/message "must be a JonGuiDataDayCameraAgcMode enum"}
;;    #(instance? data.JonSharedDataTypes$JonGuiDataDayCameraAgcMode %)])

(def day-camera-agc-mode
  "Day camera AGC mode enum"
  any?)

;; (def day-camera-exposure-mode
;;   "Day camera exposure mode enum"
;;   [:fn {:error/message "must be a JonGuiDataDayCameraExposureMode enum"}
;;    #(instance? data.JonSharedDataTypes$JonGuiDataDayCameraExposureMode %)])

(def day-camera-exposure-mode
  "Day camera exposure mode enum"
  any?)

;; (def day-camera-wdr-mode
;;   "Day camera WDR mode enum"
;;   [:fn {:error/message "must be a JonGuiDataDayCameraWdrMode enum"}
;;    #(instance? data.JonSharedDataTypes$JonGuiDataDayCameraWdrMode %)])

(def day-camera-wdr-mode
  "Day camera WDR mode enum"
  any?)

;; (def day-camera-defog-status
;;   "Day camera defog status enum"
;;   [:fn {:error/message "must be a JonGuiDataDayCameraDefogStatus enum"}
;;    #(instance? data.JonSharedDataTypes$JonGuiDataDayCameraDefogStatus %)])

(def day-camera-defog-status
  "Day camera defog status enum"
  any?)

;; (def boolean-enum
;;   "Boolean value enum"
;;   [:fn {:error/message "must be a JonGuiDataBoolean enum"}
;;    #(instance? data.JonSharedDataTypes$JonGuiDataBoolean %)])

(def boolean-enum
  "Boolean value enum"
  any?)

;; -----------------------------------------------------------------------------
;; Command Structures
;; -----------------------------------------------------------------------------

(def cmd-root-builder
  "JonSharedCmd Root builder"
  [:fn {:error/message "must be a JonSharedCmd$Root$Builder"}
   #(instance? cmd.JonSharedCmd$Root$Builder %)])

(def rotary-axis-builder
  "JonSharedCmdRotary Axis builder"
  [:fn {:error/message "must be a JonSharedCmdRotary$Axis$Builder"}
   #(instance? cmd.RotaryPlatform.JonSharedCmdRotary$Axis$Builder %)])

(def rotary-axis-command-map
  "Map for rotary axis commands"
  [:map
   [:azimuth {:optional true} [:fn #(instance? cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth$Builder %)]]
   [:elevation {:optional true} [:fn #(instance? cmd.RotaryPlatform.JonSharedCmdRotary$Elevation$Builder %)]]])

(def command-payload-map
  "Command payload structure"
  [:map
   [:pld bytes?]
   [:should-buffer boolean?]])

;; -----------------------------------------------------------------------------
;; Channel Types
;; -----------------------------------------------------------------------------

(def core-async-channel
  "core.async channel"
  [:fn {:error/message "must be a core.async channel"}
   #(instance? clojure.core.async.impl.channels.ManyToManyChannel %)])

;; -----------------------------------------------------------------------------
;; Registry Setup
;; -----------------------------------------------------------------------------

(def registry
  "Malli registry with all PotatoClient schemas"
  (merge
    (m/default-schemas)
    (mu/schemas)
    {;; Core domain
     ::theme-key theme-key
     ::domain domain
     ::locale locale
     ::locale-code locale-code
     ::stream-key stream-key

    ;; Configuration
     ::config-key config-key
     ::config config
     ::url-history-entry url-history-entry
     ::url-history url-history

    ;; State
     ::stream-process stream-process

    ;; Process
     ::process process
     ::process-state process-state
     ::stream-process-map stream-process-map
     ::process-command process-command

;; IPC
     ::message-type message-type
     ::message message

    ;; Protocol
     ::protocol-version protocol-version
     ::session-id session-id
     ::important important
     ::from-cv-subsystem from-cv-subsystem
     ::client-type client-type
     ::payload-type payload-type
     ::payload payload
     ::command command

    ;; UI Events
     ::event-type event-type
     ::x-coord x-coord
     ::y-coord y-coord
     ::ndc-x ndc-x
     ::ndc-y ndc-y
     ::mouse-button mouse-button
     ::click-count click-count
     ::wheel-rotation wheel-rotation
     ::window-event window-event
     ::navigation-event navigation-event

    ;; Java interop
     ::file file
     ::color color
     ::rectangle rectangle
     ::icon icon
     ::exception exception
     ::future-instance future-instance

    ;; UI Components
     ::jframe jframe
     ::jpanel jpanel
     ::jbutton jbutton
     ::jtext-field jtext-field
     ::jscroll-pane jscroll-pane
     ::jmenu-bar jmenu-bar
     ::jmenu jmenu
     ::action action
     ::jtoggle-button jtoggle-button
     ::table-cell-renderer table-cell-renderer

    ;; UI State
     ::extended-state extended-state
     ::window-state window-state
     ::frame-params frame-params

    ;; I18n
     ::translation-key translation-key
     ::translation-args translation-args
     ::translations-map translations-map
     
     ;; Command Domain
     ::azimuth-degrees azimuth-degrees
     ::elevation-degrees elevation-degrees
     ::rotation-speed rotation-speed
     ::gps-latitude gps-latitude
     ::gps-longitude gps-longitude
     ::gps-altitude gps-altitude
     ::zoom-level zoom-level
     ::zoom-table-index zoom-table-index
     ::digital-zoom-level digital-zoom-level
     ::focus-value focus-value
     ::iris-value iris-value
     ::clahe-level clahe-level
     ::clahe-shift clahe-shift
     ::relative-angle relative-angle
     ::bank-angle bank-angle
     ::scan-linger-time scan-linger-time
     ::scan-node-index scan-node-index
     
     ;; Protobuf Types
     ::protobuf-message protobuf-message
     ::protobuf-builder protobuf-builder
     
     ;; Command Enums
     ::rotary-direction rotary-direction
     ::rotary-mode rotary-mode
     ::video-channel video-channel
     ::day-camera-palette day-camera-palette
     ::day-camera-agc-mode day-camera-agc-mode
     ::day-camera-exposure-mode day-camera-exposure-mode
     ::day-camera-wdr-mode day-camera-wdr-mode
     ::day-camera-defog-status day-camera-defog-status
     ::boolean-enum boolean-enum
     
     ;; Command Structures
     ::cmd-root-builder cmd-root-builder
     ::rotary-axis-builder rotary-axis-builder
     ::rotary-axis-command-map rotary-axis-command-map
     ::command-payload-map command-payload-map
     
     ;; Channel Types
     ::core-async-channel core-async-channel}))

;; Set as default registry for this namespace
(mr/set-default-registry! registry)