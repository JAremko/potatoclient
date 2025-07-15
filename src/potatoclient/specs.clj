(ns potatoclient.specs
  "Shared Malli schemas for the PotatoClient application.
   This namespace contains all the common schemas used across multiple namespaces."
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [malli.util :as mu]))

;; -----------------------------------------------------------------------------
;; Core Domain Schemas
;; -----------------------------------------------------------------------------

(def theme-key
  "Valid theme identifiers"
  [:enum :sol-light :sol-dark :dark :hi-dark])

(def domain
  "Domain name string"
  :string)

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
  [:enum :theme :domain :locale])

(def config
  "Application configuration"
  [:map
   [:theme theme-key]
   [:domain domain]
   [:locale locale]])

;; -----------------------------------------------------------------------------
;; State Schemas
;; -----------------------------------------------------------------------------

(def stream-process
  "Stream process information"
  [:maybe :map])

;; -----------------------------------------------------------------------------
;; Logging Schemas
;; -----------------------------------------------------------------------------

(def log-time
  "Log timestamp"
  :pos-int)

(def log-stream
  "Log stream name"
  :string)

(def log-type
  "Log type/level"
  :string)

(def log-message
  "Log message content"
  :string)

(def log-entry
  "Complete log entry"
  [:map
   [:time log-time]
   [:stream log-stream]
   [:type log-type]
   [:message log-message]
   [:event-type {:optional true} [:maybe :string]]
   [:nav-type {:optional true} [:maybe :string]]
   [:raw-data {:optional true} :any]])

;; -----------------------------------------------------------------------------
;; IPC/Message Schemas
;; -----------------------------------------------------------------------------

(def message-type
  "IPC message types"
  [:enum :response :log :navigation :window])

(def message
  "IPC message structure"
  [:map
   [:type message-type]])

;; -----------------------------------------------------------------------------
;; Protocol Schemas
;; -----------------------------------------------------------------------------

(def protocol-version
  "Protocol version number"
  :pos-int)

(def session-id
  "Session identifier"
  :pos-int)

(def important
  "Important flag"
  :boolean)

(def from-cv-subsystem
  "CV subsystem flag"
  :boolean)

(def client-type
  "Client type values"
  [:enum :java :rust :go :clojure])

(def payload-type
  "Command payload types"
  [:enum :ping :pong :noop :frozen :gimbal-angle-to
         :lrf-request :lrf-single-pulse])

(def payload
  "Command payload"
  [:map-of :keyword :any])

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
  :keyword)

(def x-coord
  "X coordinate"
  :number)

(def y-coord
  "Y coordinate"
  :number)

(def ndc-x
  "Normalized device coordinate X"
  :number)

(def ndc-y
  "Normalized device coordinate Y"
  :number)

(def mouse-button
  "Mouse button number"
  [:enum 1 2 3])

(def click-count
  "Click count"
  :pos-int)

(def wheel-rotation
  "Mouse wheel rotation"
  :number)

(def window-event
  "Window event structure"
  [:map
   [:type event-type]
   [:x x-coord]
   [:y y-coord]
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

(def buffered-writer
  "Java BufferedWriter instance"
  [:fn #(instance? java.io.BufferedWriter %)])

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
  :int)

(def window-state
  "Window state information"
  [:map
   [:bounds rectangle]
   [:extended-state extended-state]])

(def frame-params
  "Parameters for creating main frame"
  [:map
   [:version :string]
   [:build-type :string]
   [:title {:optional true} :string]])

;; -----------------------------------------------------------------------------
;; I18n Schemas
;; -----------------------------------------------------------------------------

(def translation-key
  "Translation key"
  :keyword)

(def translation-args
  "Translation arguments"
  [:vector :any])

(def translations-map
  "Translation definitions"
  [:map-of :keyword [:map-of :keyword :string]])

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
    
    ;; State
    ::stream-process stream-process
    
    ;; Logging
    ::log-time log-time
    ::log-stream log-stream
    ::log-type log-type
    ::log-message log-message
    ::log-entry log-entry
    
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
    ::buffered-writer buffered-writer
    ::file file
    ::color color
    ::rectangle rectangle
    ::icon icon
    ::exception exception
    
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