(ns potatoclient.ui.startup-dialog
  "Startup dialog for server connection with theme and localization support."
  (:require [seesaw.core :as seesaw]
            [seesaw.action :as action]
            [seesaw.mig :as mig]
            [seesaw.border :as border]
            [malli.core :as m]
            [potatoclient.i18n :as i18n]
            [potatoclient.theme :as theme]
            [potatoclient.config :as config]
            [potatoclient.state :as state]
            [potatoclient.specs :as specs]
            [potatoclient.logging :as logging])
  (:import [javax.swing JFrame]
           [java.net URI URL]))

(defn- extract-domain
  "Extract domain/IP from various URL formats.
  Handles: domain.com, http://domain.com, wss://domain.com:8080/path?query, IP addresses, etc."
  [input]
  (let [cleaned (clojure.string/trim input)]
    (try
      ;; If it's already just a domain/IP, return as-is
      (if (and (not (clojure.string/includes? cleaned "://"))
               (not (clojure.string/includes? cleaned "/")))
        cleaned
        ;; Otherwise, try to parse as URL
        (let [;; Add protocol if missing
              url-str (if (clojure.string/includes? cleaned "://")
                        cleaned
                        (str "http://" cleaned))
              url (URL. url-str)
              host (.getHost url)]
          (if (clojure.string/blank? host)
            cleaned  ; Fallback to original if parsing fails
            host)))
      (catch Exception _
        ;; If all parsing fails, return the cleaned input
        cleaned))))

(defn- validate-domain
  "Validate if the input is a valid domain name or IP address using Malli spec."
  [domain]
  (m/validate specs/domain domain))

(defn- reload-dialog!
  "Reload the dialog with new theme/locale."
  [dialog callback]
  (seesaw/dispose! dialog)
  ;; Call the callback with :reload result
  (callback :reload))

(defn- create-language-action
  "Create a language selection action."
  [lang-key display-name dialog callback]
  (let [flag-icon (case lang-key
                    :english (seesaw/icon (clojure.java.io/resource "flags/en.png"))
                    :ukrainian (seesaw/icon (clojure.java.io/resource "flags/ua.png"))
                    nil)]
    (action/action
     :name (str display-name "    ")
     :icon flag-icon
     :handler (fn [e]
                (when-not (= (state/get-locale) lang-key)
                  (state/set-locale! lang-key)
                  (config/update-config! :locale lang-key)
                  (reload-dialog! dialog callback))))))

(defn- create-theme-action
  "Create a theme selection action."
  [theme-key dialog callback]
  (let [theme-i18n-key (theme/get-theme-i18n-key theme-key)
        theme-name (i18n/tr theme-i18n-key)]
    (action/action
     :name (str theme-name "    ")
     :icon (theme/key->icon theme-key)
     :handler (fn [e]
                (when-not (= (theme/get-current-theme) theme-key)
                  (when (theme/set-theme! theme-key)
                    (config/save-theme! theme-key)
                    (reload-dialog! dialog callback)))))))

(defn- create-menu-bar
  "Create menu bar for startup dialog."
  [dialog callback]
  (seesaw/menubar
   :items [(seesaw/menu
            :text (i18n/tr :menu-view-theme)
            :icon (theme/key->icon :actions-group-theme)
            :items (map #(create-theme-action % dialog callback)
                        (theme/get-available-themes)))
           (seesaw/menu
            :text (i18n/tr :menu-view-language)
            :icon (theme/key->icon :icon-languages)
            :items [(create-language-action :english "English" dialog callback)
                    (create-language-action :ukrainian "Українська" dialog callback)])]))

(defn- create-content-panel
  "Create the main content panel for the startup dialog."
  [saved-url]
  (let [url-label (seesaw/label :text (i18n/tr :startup-server-url)
                                :font {:size 14})
        url-field (seesaw/text :text saved-url
                               :columns 30
                               :font {:size 14}
                               :border (border/compound-border
                                        (border/line-border :thickness 1 :color :gray)
                                        (border/empty-border :thickness 5)))]
    
    (mig/mig-panel
     :constraints ["wrap 1, insets 20, gap 10"
                   "[grow, fill]"
                   "[]"]
     :items [[url-label ""]
             [url-field "growx, h 40!"]
             [(seesaw/separator) "growx, gaptop 10, gapbottom 10"]])))


(defn show-startup-dialog
  "Show the startup dialog and call the callback with :connect, :cancel, or :reload."
  [parent callback]
  (let [saved-url (config/get-url)
        domain (config/get-domain)  ;; This extracts domain from URL
        ;; Create buttons first to get reference to connect button
        connect-button (seesaw/button :text (i18n/tr :startup-button-connect)
                                      :font {:size 14}
                                      :preferred-size [120 :by 35])
        cancel-button (seesaw/button :text (i18n/tr :startup-button-cancel)
                                     :font {:size 14}
                                     :preferred-size [120 :by 35])
        content (create-content-panel saved-url)
        url-field (first (seesaw/select content [:JTextField]))
        buttons-panel (seesaw/flow-panel
                       :align :center
                       :hgap 15
                       :items [connect-button cancel-button])
        main-panel (seesaw/border-panel
                    :center content
                    :south buttons-panel
                    :border 10)
        ;; Create the dialog frame
        dialog (seesaw/frame
                :title (i18n/tr :startup-title)
                :icon (clojure.java.io/resource "main.png")
                :resizable? false
                :content main-panel
                :on-close :nothing)]
    
    ;; Event handlers
    (seesaw/listen connect-button :action
                   (fn [e]
                     (let [text (seesaw/text url-field)
                           extracted (extract-domain text)]
                       (if (validate-domain extracted)
                         (do
                           ;; Save the original URL as entered
                           (config/save-url! text)
                           ;; Update state with extracted domain
                           (state/set-domain! extracted)
                           (seesaw/dispose! dialog)
                           (callback :connect))
                         ;; Show error alert
                         (seesaw/alert dialog
                                       (if (clojure.string/blank? extracted)
                                         "Please enter a valid domain or IP address"
                                         (str "Invalid domain/IP: " extracted "\n\n"
                                              "Valid formats:\n"
                                              "• domain.com\n"
                                              "• sub.domain.com\n"
                                              "• 192.168.1.100\n"
                                              "• wss://domain.com:8080/path"))
                                       :title "Invalid URL"
                                       :type :error)))))
    
    (seesaw/listen cancel-button :action
                   (fn [e]
                     (seesaw/dispose! dialog)
                     (callback :cancel)))
    
    ;; Set up menu bar - must be done before showing dialog
    (.setJMenuBar ^JFrame dialog (create-menu-bar dialog callback))
    
    ;; Make Connect button the default when Enter is pressed
    (.setDefaultButton (.getRootPane dialog) connect-button)
    
    ;; Handle window closing
    (seesaw/listen dialog :window-closing
                   (fn [e]
                     (seesaw/dispose! dialog)
                     (callback :cancel)))
    
    ;; Pack and center the dialog
    (seesaw/pack! dialog)
    (.setLocationRelativeTo dialog parent)
    
    ;; Log dialog creation
    (logging/log-info {:id ::show-dialog
                       :data {:domain domain}
                       :msg (str "Showing startup dialog for domain: " domain)})
    
    ;; Show dialog
    (seesaw/show! dialog)))