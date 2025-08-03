(ns potatoclient.ui-specs
  "Essential UI and video stream specs for PotatoClient.
   This replaces the legacy specs.clj with only the schemas actually in use."
  (:require [malli.core :as m]
            [clojure.string :as str])
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
  "WebSocket URL for video streams"
  [:and
   string?
   [:fn {:error/message "must start with ws:// or wss://"}
    #(re-matches #"wss?://.*" %)]])

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

(def gesture-event
  "Gesture event from video stream"
  [:map
   [:gesture-type gesture-type]
   [:stream-type stream-type]
   [:canvas-width {:optional true} pos-int?]
   [:canvas-height {:optional true} pos-int?]
   [:aspect-ratio {:optional true} aspect-ratio]
   [:ndc-x {:optional true} number?]
   [:ndc-y {:optional true} number?]
   [:ndc-delta-x {:optional true} number?]
   [:ndc-delta-y {:optional true} number?]
   [:direction {:optional true} swipe-direction]
   [:frame-timestamp {:optional true} int?]])

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