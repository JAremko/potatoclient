(ns potatoclient.ui-specs
  "Essential UI and video stream specs for PotatoClient.
   This replaces the legacy specs.clj with only the schemas actually in use."
  (:require [clojure.string :as str]
            [malli.core :as m]
            [malli.util :as mu]
            [malli.registry :as mr]
            [potatoclient.malli.oneof :as oneof])
  (:import (java.util.concurrent Future)
           (javax.swing JFrame JPanel JTextField JMenu JMenuBar Action Icon)
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

(def config
  "Application configuration"
  [:map
   [:theme {:optional true} theme-key]
   [:domain {:optional true} domain]
   [:locale {:optional true} locale]
   [:url-history {:optional true} url-history]])

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

(def window-bounds
  "Window bounds"
  [:map
   [:x int?]
   [:y int?]
   [:width pos-int?]
   [:height pos-int?]])

(def future-instance
  "Java Future instance"
  [:fn {:error/message "must be a Future"}
   #(instance? Future %)])

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
          ::future-instance future-instance
          ::window-state window-state
          ::config config
          ::app-state app-state
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
          ::url url
          ::url-history url-history
          ::config-key config-key
          ::translation-key translation-key
          ::translation-args translation-args
          ::translations-map translations-map}))

;; Set as default registry so qualified keywords work
(mr/set-default-registry! registry)
