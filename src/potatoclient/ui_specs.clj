(ns potatoclient.ui-specs
  "Essential UI and video stream specs for PotatoClient.
   Uses the shared malli registry for global spec management."
  (:require [clojure.string :as str]
            [potatoclient.malli.registry :as registry]
            [potatoclient.url-parser :as url-parser])
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

(def ifn
  "Schema for invokable function values (functions, keywords, maps, etc)"
  [:fn {:error/message "must be invokable (function, keyword, map, etc)"}
   ifn?])

(def domain
  "Domain name or IP address - validates hosts that can be used for WebSocket connections.
   Uses Instaparse grammar as the single source of truth for validation."
  [:and
   string?
   [:fn {:error/message "must be a valid domain name or IP address"}
    url-parser/valid-domain-or-ip?]])

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

(def status-type
  "Status message type"
  [:enum :info :warning :error])

(def status-message
  "Status bar message"
  [:map
   [:message string?]
   [:type status-type]])

(def window-state
  "Window state information"
  [:map
   [:bounds {:optional true} window-bounds]
   [:extended-state {:optional true} int?]
   [:divider-locations {:optional true} [:sequential int?]]])

;; -----------------------------------------------------------------------------
;; Registry Registration
;; -----------------------------------------------------------------------------

;; Register all UI specs to the shared global registry
(defn register-ui-specs!
  "Register all UI specs to the global malli registry"
  []
  ;; Core domain schemas
  (registry/register-spec! ::theme-key theme-key)
  (registry/register-spec! ::theme theme-key)  ; Also register as ::theme for backward compatibility
  (registry/register-spec! ::locale locale)
  (registry/register-spec! ::locale-code locale-code)
  (registry/register-spec! ::domain domain)
  (registry/register-spec! ::stream-key stream-key)
  (registry/register-spec! ::stream-type stream-type)
  (registry/register-spec! :ifn ifn)  ; Register :ifn for function schemas

  ;; Configuration schemas
  (registry/register-spec! ::url url)
  (registry/register-spec! ::config-key config-key)
  (registry/register-spec! ::url-history-entry url-history-entry)
  (registry/register-spec! ::url-history url-history)
  (registry/register-spec! ::config config)

  ;; UI Component schemas
  (registry/register-spec! ::file file)
  (registry/register-spec! ::jframe jframe)
  (registry/register-spec! ::jpanel jpanel)
  (registry/register-spec! ::jtextfield jtextfield)
  (registry/register-spec! ::jmenu jmenu)
  (registry/register-spec! ::jmenubar jmenubar)
  (registry/register-spec! ::action action)
  (registry/register-spec! ::icon icon)
  (registry/register-spec! ::rectangle rectangle)
  (registry/register-spec! ::color color)

  ;; I18n schemas
  (registry/register-spec! ::translation-key translation-key)
  (registry/register-spec! ::translation-args translation-args)
  (registry/register-spec! ::translations-map translations-map)

  ;; Window and state schemas
  (registry/register-spec! ::window-bounds window-bounds)
  (registry/register-spec! ::future-instance future-instance)
  (registry/register-spec! ::window-state window-state)
  
  ;; Status bar schemas
  (registry/register-spec! ::status-type status-type)
  (registry/register-spec! ::status-message status-message))

;; Initialize the global registry and register UI specs on namespace load
(defonce ^:private registry-initialized
  (do
    ;; Setup the global registry if not already done
    (registry/setup-global-registry!)
    ;; Register all UI specs
    (register-ui-specs!)
    true))
