(ns potatoclient.ui-specs
  "Essential UI and video stream specs for PotatoClient.
   This replaces the legacy specs.clj with only the schemas actually in use."
  (:require [clojure.string :as str]
            [malli.core :as m]
            [malli.util :as mu]
            [malli.registry :as mr]
            [potatoclient.specs.malli-oneof :as oneof])
  (:import (javax.swing JFrame JPanel JTextField JMenu JMenuBar Action Icon)
           (java.io File)
           (java.awt Rectangle Color)))

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
      (and (not (str/blank? s))
           (let [ip-pattern #"^(\d{1,3}\.){3}\d{1,3}$"
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
  "Video stream identifiers"
  [:enum :heat :day])

(def stream-type
  "Alias for stream-key for clarity"
  stream-key)

;; -----------------------------------------------------------------------------
;; Configuration Schemas
;; -----------------------------------------------------------------------------

(def url
  "URL entered by user - any non-blank string"
  [:and
   string?
   [:fn {:error/message "must not be blank"}
    #(not (str/blank? %))]])

(def config-key
  "Valid configuration keys"
  [:enum :theme :domain :locale :url-history])

(def url-history-entry
  "Entry in URL history"
  [:map
   [:url url]
   [:timestamp pos-int?]])

(def url-history
  "Collection of URL history entries"
  [:sequential url-history-entry])

(def speed-config
  "Speed configuration for pan gestures"
  [:map
   [:zoom-table-index int?]
   [:max-rotation-speed number?]
   [:dead-zone-radius number?]
   [:curve-steepness number?]])

(def config
  "Application configuration"
  [:map
   [:theme {:optional true} theme-key]
   [:domain {:optional true} domain]
   [:locale {:optional true} locale]
   [:url-history {:optional true} url-history]])

;; -----------------------------------------------------------------------------
;; Process Management Schemas
;; -----------------------------------------------------------------------------

(def process-state
  "Process lifecycle states"
  [:enum :running :stopped :error])

(def subprocess-type
  "Types of subprocesses"
  [:enum :cmd :state :video])

;; -----------------------------------------------------------------------------
;; Video Stream Schemas
;; -----------------------------------------------------------------------------

(def canvas-dimensions
  "Canvas width and height"
  [:map
   [:width pos-int?]
   [:height pos-int?]])

(def aspect-ratio
  "Video aspect ratio"
  pos?)

;; -----------------------------------------------------------------------------
;; Gesture Event Schemas
;; -----------------------------------------------------------------------------

(def gesture-type
  "Types of gestures"
  [:enum :tap :doubletap :panstart :panmove :panstop :swipe])

(def swipe-direction
  "Swipe gesture directions"
  [:enum :up :down :left :right])

;; Specific gesture event types (defined fully without merge)
(def tap-gesture-event
  "Tap gesture event"
  [:map {:closed true}
   [:type [:= :gesture]]
   [:timestamp int?]
   [:canvas-width pos-int?]
   [:canvas-height pos-int?]
   [:aspect-ratio number?]
   [:stream-type stream-type]
   [:gesture-type [:= :tap]]
   [:x int?]
   [:y int?]
   [:ndc-x number?]
   [:ndc-y number?]])

(def double-tap-gesture-event
  "Double tap gesture event with optional frame timing"
  [:map {:closed true}
   [:type [:= :gesture]]
   [:timestamp int?]
   [:canvas-width pos-int?]
   [:canvas-height pos-int?]
   [:aspect-ratio number?]
   [:stream-type stream-type]
   [:gesture-type [:= :doubletap]]
   [:x int?]
   [:y int?]
   [:ndc-x number?]
   [:ndc-y number?]
   [:frame-timestamp {:optional true} int?]
   [:frame-duration {:optional true} int?]])

(def pan-start-gesture-event
  "Pan start gesture event"
  [:map {:closed true}
   [:type [:= :gesture]]
   [:timestamp int?]
   [:canvas-width pos-int?]
   [:canvas-height pos-int?]
   [:aspect-ratio number?]
   [:stream-type stream-type]
   [:gesture-type [:= :panstart]]
   [:x int?]
   [:y int?]
   [:ndc-x number?]
   [:ndc-y number?]])

(def pan-move-gesture-event
  "Pan move gesture event with delta values"
  [:map {:closed true}
   [:type [:= :gesture]]
   [:timestamp int?]
   [:canvas-width pos-int?]
   [:canvas-height pos-int?]
   [:aspect-ratio number?]
   [:stream-type stream-type]
   [:gesture-type [:= :panmove]]
   [:x int?]
   [:y int?]
   [:delta-x int?]
   [:delta-y int?]
   [:ndc-delta-x number?]
   [:ndc-delta-y number?]])

(def pan-stop-gesture-event
  "Pan stop gesture event"
  [:map {:closed true}
   [:type [:= :gesture]]
   [:timestamp int?]
   [:canvas-width pos-int?]
   [:canvas-height pos-int?]
   [:aspect-ratio number?]
   [:stream-type stream-type]
   [:gesture-type [:= :panstop]]
   [:x int?]
   [:y int?]])

(def swipe-gesture-event
  "Swipe gesture event (future use)"
  [:map {:closed true}
   [:type [:= :gesture]]
   [:timestamp int?]
   [:canvas-width pos-int?]
   [:canvas-height pos-int?]
   [:aspect-ratio number?]
   [:stream-type stream-type]
   [:gesture-type [:= :swipe]]
   [:direction swipe-direction]])

(def gesture-event
  "Gesture event from video stream - precise spec using :or"
  [:or
   tap-gesture-event
   double-tap-gesture-event
   pan-start-gesture-event
   pan-move-gesture-event
   pan-stop-gesture-event
   swipe-gesture-event])

;; -----------------------------------------------------------------------------
;; UI Component Schemas
;; -----------------------------------------------------------------------------

(def file
  "Java File instance"
  [:fn {:error/message "must be a File"}
   #(instance? File %)])

(def jframe
  "Swing JFrame"
  [:fn {:error/message "must be a JFrame"}
   #(instance? JFrame %)])

(def jpanel
  "Swing JPanel"
  [:fn {:error/message "must be a JPanel"}
   #(instance? JPanel %)])

(def jtextfield
  "Swing JTextField"
  [:fn {:error/message "must be a JTextField"}
   #(instance? JTextField %)])

(def jmenu
  "Swing JMenu"
  [:fn {:error/message "must be a JMenu"}
   #(instance? JMenu %)])

(def jmenubar
  "Swing JMenuBar"
  [:fn {:error/message "must be a JMenuBar"}
   #(instance? JMenuBar %)])

(def action
  "Swing Action"
  [:fn {:error/message "must be an Action"}
   #(instance? Action %)])

(def icon
  "Swing Icon"
  [:fn {:error/message "must be an Icon"}
   #(instance? Icon %)])

(def rectangle
  "AWT Rectangle"
  [:fn {:error/message "must be a Rectangle"}
   #(instance? Rectangle %)])

(def color
  "AWT Color"
  [:fn {:error/message "must be a Color"}
   #(instance? Color %)])

;; -----------------------------------------------------------------------------
;; Internationalization Schemas
;; -----------------------------------------------------------------------------

(def translation-key
  "Key for translation lookup"
  keyword?)

(def translation-args
  "Arguments for translation string formatting"
  [:sequential any?])

(def translations-map
  "Map of locale to translation strings"
  [:map-of locale-code [:map-of keyword? string?]])

;; -----------------------------------------------------------------------------
;; Transit Message Validation
;; -----------------------------------------------------------------------------

(def message-type
  "Transit message types"
  [:enum :command :state :event :control :log :error :response])

(def message-envelope
  "Standard Transit message envelope"
  [:map
   [:msg-type message-type]
   [:msg-id string?]
   [:timestamp pos-int?]
   [:payload map?]])

;; -----------------------------------------------------------------------------
;; Additional Transit Message Schemas
;; -----------------------------------------------------------------------------

(def command-message
  "Command message"
  [:map
   [:msg-type [:= :command]]
   [:msg-id string?]
   [:timestamp pos-int?]
   [:payload map?]])

(def response-message
  "Response message"
  [:map
   [:msg-type [:= :response]]
   [:msg-id string?]
   [:timestamp pos-int?]
   [:payload map?]])

(def request-payload
  "Request payload"
  [:map
   [:method keyword?]
   [:params {:optional true} map?]])

(def log-payload
  "Log message payload"
  [:map
   [:level keyword?]
   [:message string?]
   [:timestamp pos-int?]
   [:process {:optional true} keyword?]
   [:data {:optional true} map?]])

(def error-payload
  "Error message payload"
  [:map
   [:message string?]
   [:type {:optional true} keyword?]
   [:stack-trace {:optional true} string?]
   [:data {:optional true} map?]])

(def status-payload
  "Status message payload"
  [:map
   [:status keyword?]
   [:message {:optional true} string?]
   [:data {:optional true} map?]])

(def metric-payload
  "Metric message payload"
  [:map
   [:name keyword?]
   [:value number?]
   [:timestamp pos-int?]
   [:tags {:optional true} map?]])

(def control-message
  "Control message"
  [:map
   [:msg-type [:= :control]]
   [:msg-id string?]
   [:timestamp pos-int?]
   [:payload [:map [:action keyword?]]]])

(def state-update-message
  "State update message"
  [:map
   [:msg-type [:= :state-update]]
   [:msg-id string?]
   [:timestamp pos-int?]
   [:payload map?]])

(def navigation-event-payload
  "Navigation event payload"
  [:map
   [:x number?]
   [:y number?]
   [:button {:optional true} keyword?]
   [:action keyword?]])

(def gesture-event-payload
  "Gesture event payload - matches gesture-event structure"
  gesture-event)

(def window-event-payload
  "Window event payload"
  [:map
   [:action keyword?]
   [:width {:optional true} pos-int?]
   [:height {:optional true} pos-int?]
   [:state {:optional true} keyword?]])

(def event-message
  "Event message"
  [:map
   [:msg-type [:= :event]]
   [:msg-id string?]
   [:timestamp pos-int?]
   [:payload [:map
              [:type keyword?]
              [:data map?]]]])

(def protobuf-state-message
  "Message containing protobuf state update"
  [:map
   [:msg-type [:= :state]]
   [:msg-id string?]
   [:timestamp pos-int?]
   [:payload map?]])

(def window-bounds
  "Window bounds"
  [:map
   [:x int?]
   [:y int?]
   [:width pos-int?]
   [:height pos-int?]])

(def stream-process
  "Stream subprocess state"
  [:map
   [:pid {:optional true} pos-int?]
   [:status process-state]
   [:error {:optional true} string?]])

(def stream-process-map
  "Map representing a running stream process"
  [:map
   [:process any?]  ; Java Process object
   [:writer fn?]    ; Function to write Transit messages
   [:input-stream any?]  ; InputStream
   [:output-stream any?] ; OutputStream  
   [:stderr-reader any?] ; BufferedReader
   [:message-handler fn?] ; Message handler function
   [:stream-id string?]   ; Stream identifier
   [:state any?]])        ; Atom containing process state

(def process-command
  "Command to send to a process"
  map?)

(def future-instance
  "Java Future instance"
  [:fn {:error/message "must be a Future"}
   #(instance? java.util.concurrent.Future %)])

(def window-state
  "Window state information"
  [:map
   [:bounds {:optional true} window-bounds]
   [:extended-state {:optional true} int?]
   [:divider-locations {:optional true} [:sequential int?]]])

(def app-state
  "Full app state"
  [:map
   [:config config]
   [:connection {:optional true} [:map
                                  [:connected? boolean?]
                                  [:url {:optional true} string?]
                                  [:error {:optional true} string?]]]
   [:processes {:optional true} [:map-of subprocess-type stream-process]]
   [:streams {:optional true} [:map-of stream-type map?]]
   [:zoom-table-index {:optional true} [:map-of stream-type int?]]])

(def transit-subprocess
  "Transit subprocess"
  [:map
   [:subprocess-type subprocess-type]
   [:process any?]              ; Java Process object
   [:url string?]               ; WebSocket URL
   [:input-stream any?]         ; InputStream
   [:output-stream any?]        ; OutputStream
   [:error-stream any?]         ; ErrorStream
   [:write-fn fn?]              ; Function to write messages
   [:message-handler fn?]       ; Message handler function
   [:state any?]])

;; -----------------------------------------------------------------------------
;; Schema Registry
;; -----------------------------------------------------------------------------

(def registry
  "Registry of all UI specs for qualified keyword lookups"
  (merge (m/default-schemas)
         (mu/schemas)
         {:oneof oneof/-oneof-schema
          ::theme-key theme-key
          ::locale locale
          ::locale-code locale-code
          ::domain domain
          ::stream-key stream-key
          ::stream-type stream-type
          ::stream-process stream-process
          ::stream-process-map stream-process-map
          ::process-command process-command
          ::future-instance future-instance
          ::window-state window-state
          ::transit-subprocess transit-subprocess
          ::speed-config speed-config
          ::config config
          ::app-state app-state
          ::tap-gesture-event tap-gesture-event
          ::double-tap-gesture-event double-tap-gesture-event
          ::pan-start-gesture-event pan-start-gesture-event
          ::pan-move-gesture-event pan-move-gesture-event
          ::pan-stop-gesture-event pan-stop-gesture-event
          ::swipe-gesture-event swipe-gesture-event
          ::gesture-event gesture-event
          ::gesture-type gesture-type
          ::swipe-direction swipe-direction
          ::window-bounds window-bounds
          ::icon icon
          ::file file
          ::jframe jframe
          ::jpanel jpanel
          ::jtextfield jtextfield
          ::jmenu jmenu
          ::jmenubar jmenubar
          ::action action
          ::color color
          ::rectangle rectangle
          ::command-message command-message
          ::response-message response-message
          ::request-payload request-payload
          ::log-payload log-payload
          ::error-payload error-payload
          ::status-payload status-payload
          ::metric-payload metric-payload
          ::control-message control-message
          ::state-update-message state-update-message
          ::navigation-event-payload navigation-event-payload
          ::gesture-event-payload gesture-event-payload
          ::window-event-payload window-event-payload
          ::event-message event-message
          ::protobuf-state-message protobuf-state-message
          ::message-type message-type
          ::process-state process-state
          ::subprocess-type subprocess-type
          ::canvas-dimensions canvas-dimensions
          ::aspect-ratio aspect-ratio
          ::url url
          ::url-history url-history
          ::config-key config-key
          ::translation-key translation-key
          ::translation-args translation-args
          ::translations-map translations-map}))

;; Set as default registry so qualified keywords work
(mr/set-default-registry! registry)
