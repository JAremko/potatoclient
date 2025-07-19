(ns potatoclient.specs
  "Shared Malli schemas for the PotatoClient application.
   This namespace contains all the common schemas used across multiple namespaces."
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [malli.util :as mu]
            [clojure.core.async.impl.channels]
            [clojure.data.json]))

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
  [:fn #(instance? java.lang.Process %)])

(def process-state
  "Process lifecycle state"
  [:enum :starting :running :stopping :stopped :failed])

(def stream-process-map
  "Complete stream process structure"
  [:map
   [:process process]
   [:writer [:fn #(instance? java.io.BufferedWriter %)]]
   [:stdout-reader [:fn #(instance? java.io.BufferedReader %)]]
   [:stderr-reader [:fn #(instance? java.io.BufferedReader %)]]
   [:output-chan [:fn {:error/message "must be a core.async channel"}
                  #(instance? clojure.core.async.impl.channels.ManyToManyChannel %)]]
   [:stream-id string?]
   [:state [:fn {:error/message "must be an atom containing process state"}
            #(and (instance? clojure.lang.Atom %)
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
  [:fn #(instance? java.io.File %)])

(def color
  "Java AWT Color instance"
  [:fn #(instance? java.awt.Color %)])

(def rectangle
  "Java AWT Rectangle instance"
  [:fn #(instance? java.awt.Rectangle %)])

(def icon
  "Swing Icon instance"
  [:maybe [:fn #(instance? javax.swing.Icon %)]])

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
  [:fn #(instance? javax.swing.JFrame %)])

(def jpanel
  "Swing JPanel instance"
  [:fn #(instance? javax.swing.JPanel %)])

(def jbutton
  "Swing button instance"
  [:fn #(instance? javax.swing.AbstractButton %)])

(def jtext-field
  "Swing JTextField instance"
  [:fn #(instance? javax.swing.JTextField %)])

(def jscroll-pane
  "Swing JScrollPane instance"
  [:fn #(instance? javax.swing.JScrollPane %)])

(def jmenu-bar
  "Swing JMenuBar instance"
  [:fn #(instance? javax.swing.JMenuBar %)])

(def jmenu
  "Swing JMenu instance"
  [:fn #(instance? javax.swing.JMenu %)])

(def action
  "Swing Action instance"
  [:fn #(instance? javax.swing.Action %)])

(def jtoggle-button
  "Swing JToggleButton instance"
  [:fn #(instance? javax.swing.JToggleButton %)])

(def table-cell-renderer
  "Swing table cell renderer"
  [:fn #(instance? javax.swing.table.DefaultTableCellRenderer %)])

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
     ::translations-map translations-map}))

;; Set as default registry for this namespace
(mr/set-default-registry! registry)