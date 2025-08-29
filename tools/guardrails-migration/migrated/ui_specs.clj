(ns potatoclient.ui-specs "Essential UI and video stream specs for PotatoClient.\n   Uses the shared malli registry for global spec management." (:require [clojure.string :as str] [potatoclient.malli.registry :as registry] [potatoclient.url-parser :as url-parser]) (:import (java.util.concurrent Future) (javax.swing JFrame JPanel JTextField JMenu JMenuBar Action Icon) (java.io File) (java.awt Rectangle Color)))

(def theme-key "Valid theme identifiers" [:enum :sol-light :sol-dark :dark :hi-dark])

(def domain "Domain name or IP address - validates hosts that can be used for WebSocket connections.\n   Uses Instaparse grammar as the single source of truth for validation." [:and string? [:fn #:error{:message "must be a valid domain name or IP address"} url-parser/valid-domain-or-ip?]])

(def locale "Application locale" [:enum :english :ukrainian])

(def locale-code "Language code for i18n" [:enum :en :uk])

(def stream-key "Video stream identifiers" [:enum :heat :day])

(def stream-type "Alias for stream-key for clarity" stream-key)

(def url "URL entered by user - any non-blank string" [:and string? [:fn #:error{:message "must not be blank"} (fn* [p1__278#] (not (str/blank? p1__278#)))]])

(def config-key "Valid configuration keys" [:enum :theme :domain :locale :url-history])

(def url-history-entry "Entry in URL history" [:map [:url url] [:timestamp pos-int?]])

(def url-history "Collection of URL history entries" [:sequential url-history-entry])

(def config "Application configuration" [:map [:theme {:optional true} theme-key] [:domain {:optional true} domain] [:locale {:optional true} locale] [:url-history {:optional true} url-history]])

(def file "Java File instance" [:fn #:error{:message "must be a File"} (fn* [p1__279#] (instance? File p1__279#))])

(def jframe "Swing JFrame" [:fn #:error{:message "must be a JFrame"} (fn* [p1__280#] (instance? JFrame p1__280#))])

(def jpanel "Swing JPanel" [:fn #:error{:message "must be a JPanel"} (fn* [p1__281#] (instance? JPanel p1__281#))])

(def jtextfield "Swing JTextField" [:fn #:error{:message "must be a JTextField"} (fn* [p1__282#] (instance? JTextField p1__282#))])

(def jmenu "Swing JMenu" [:fn #:error{:message "must be a JMenu"} (fn* [p1__283#] (instance? JMenu p1__283#))])

(def jmenubar "Swing JMenuBar" [:fn #:error{:message "must be a JMenuBar"} (fn* [p1__284#] (instance? JMenuBar p1__284#))])

(def action "Swing Action" [:fn #:error{:message "must be an Action"} (fn* [p1__285#] (instance? Action p1__285#))])

(def icon "Swing Icon" [:fn #:error{:message "must be an Icon"} (fn* [p1__286#] (instance? Icon p1__286#))])

(def rectangle "AWT Rectangle" [:fn #:error{:message "must be a Rectangle"} (fn* [p1__287#] (instance? Rectangle p1__287#))])

(def color "AWT Color" [:fn #:error{:message "must be a Color"} (fn* [p1__288#] (instance? Color p1__288#))])

(def translation-key "Key for translation lookup" keyword?)

(def translation-args "Arguments for translation string formatting" [:sequential any?])

(def translations-map "Map of locale to translation strings" [:map-of locale-code [:map-of keyword? string?]])

(def window-bounds "Window bounds" [:map [:x int?] [:y int?] [:width pos-int?] [:height pos-int?]])

(def future-instance "Java Future instance" [:fn #:error{:message "must be a Future"} (fn* [p1__289#] (instance? Future p1__289#))])

(def window-state "Window state information" [:map [:bounds {:optional true} window-bounds] [:extended-state {:optional true} int?] [:divider-locations {:optional true} [:sequential int?]]])

(defn register-ui-specs! "Register all UI specs to the global malli registry" [] (registry/register-spec! :user/theme-key theme-key) (registry/register-spec! :user/locale locale) (registry/register-spec! :user/locale-code locale-code) (registry/register-spec! :user/domain domain) (registry/register-spec! :user/stream-key stream-key) (registry/register-spec! :user/stream-type stream-type) (registry/register-spec! :user/url url) (registry/register-spec! :user/config-key config-key) (registry/register-spec! :user/url-history-entry url-history-entry) (registry/register-spec! :user/url-history url-history) (registry/register-spec! :user/config config) (registry/register-spec! :user/file file) (registry/register-spec! :user/jframe jframe) (registry/register-spec! :user/jpanel jpanel) (registry/register-spec! :user/jtextfield jtextfield) (registry/register-spec! :user/jmenu jmenu) (registry/register-spec! :user/jmenubar jmenubar) (registry/register-spec! :user/action action) (registry/register-spec! :user/icon icon) (registry/register-spec! :user/rectangle rectangle) (registry/register-spec! :user/color color) (registry/register-spec! :user/translation-key translation-key) (registry/register-spec! :user/translation-args translation-args) (registry/register-spec! :user/translations-map translations-map) (registry/register-spec! :user/window-bounds window-bounds) (registry/register-spec! :user/future-instance future-instance) (registry/register-spec! :user/window-state window-state))

(defonce registry-initialized (do (registry/setup-global-registry!) (register-ui-specs!) true))