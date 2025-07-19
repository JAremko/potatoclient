(ns potatoclient.ui.startup-dialog
  "Startup dialog for server connection with theme and localization support."
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- => ?]]
            [seesaw.core :as seesaw]
            [seesaw.action :as action]
            [seesaw.mig :as mig]
            [seesaw.border :as border]
            [malli.core :as m]
            [potatoclient.i18n :as i18n]
            [potatoclient.theme :as theme]
            [potatoclient.config :as config]
            [potatoclient.state :as state]
            [potatoclient.specs :as specs]
            [potatoclient.logging :as logging]))

(>defn- extract-domain
  "Extract domain/IP from various URL formats.
  Handles: domain.com, http://domain.com, wss://domain.com:8080/path?query, IP addresses, etc."
  [input]
  [string? => string?]
  (let [cleaned (clojure.string/trim input)]
    ;; If it's already just a domain/IP (no protocol, no path), return as-is
    (if (and (not (clojure.string/includes? cleaned "://"))
             (not (re-find #"[/?#&:]" cleaned)))
      cleaned
      ;; Otherwise extract the domain/IP part
      (let [;; Remove protocol if present
            after-protocol (if-let [idx (clojure.string/index-of cleaned "://")]
                             (subs cleaned (+ idx 3))
                             cleaned)
            ;; Take everything up to the first separator (excluding port)
            domain (if-let [sep-idx (some #(clojure.string/index-of after-protocol %)
                                          ["/" "?" "#" "&"])]
                     (subs after-protocol 0 sep-idx)
                     after-protocol)
            ;; Remove port if present
            domain (if-let [port-idx (clojure.string/index-of domain ":")]
                     (subs domain 0 port-idx)
                     domain)]
        ;; Return the extracted domain or original if extraction failed
        (if (clojure.string/blank? domain)
          cleaned
          domain)))))

(>defn- validate-domain
  "Validate if the input is a valid domain name or IP address using Malli spec."
  [domain]
  [string? => boolean?]
  (m/validate specs/domain domain))

(>defn- reload-dialog!
  "Reload the dialog with new theme/locale."
  [dialog callback]
  [[:fn {:error/message "must be a JFrame"}
    #(instance? javax.swing.JFrame %)]
   ifn? => nil?]
  (seesaw/dispose! dialog)
  ;; Call the callback with :reload result
  (callback :reload))

(>defn- create-language-action
  "Create a language selection action."
  [lang-key display-name dialog callback]
  [:potatoclient.specs/locale string?
   [:fn {:error/message "must be a JFrame"}
    #(instance? javax.swing.JFrame %)]
   ifn? => any?]
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

(>defn- create-theme-action
  "Create a theme selection action."
  [theme-key dialog callback]
  [:potatoclient.specs/theme-key
   [:fn {:error/message "must be a JFrame"}
    #(instance? javax.swing.JFrame %)]
   ifn? => any?]
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

(>defn- create-menu-bar
  "Create menu bar for startup dialog."
  [dialog callback]
  [[:fn {:error/message "must be a JFrame"}
    #(instance? javax.swing.JFrame %)]
   ifn? => any?]
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

(>defn- create-content-panel
  "Create the main content panel for the startup dialog."
  [saved-url]
  [[:maybe string?] => [:fn {:error/message "must be a Swing panel"}
                        #(instance? javax.swing.JPanel %)]]
  (let [url-label (seesaw/label :text (i18n/tr :startup-server-url)
                                :font {:size 14})
        ;; Get URL history for combobox
        recent-urls (config/get-recent-urls)
        ;; Create items for combobox - saved URL first, then history
        combo-items (vec (distinct (concat (when saved-url [saved-url]) recent-urls)))
        ;; Create editable combobox
        url-combobox (seesaw/combobox :id :url-input
                                      :model combo-items
                                      :editable? true
                                      :font {:size 14}
                                      :border (border/compound-border
                                                (border/line-border :thickness 1 :color :gray)
                                                (border/empty-border :thickness 5)))]
    ;; Set the saved URL as the initial text
    (when saved-url
      (seesaw/value! url-combobox saved-url))

    (mig/mig-panel
      :constraints ["wrap 1, insets 20, gap 10"
                    "[grow, fill]"
                    "[]"]
      :items [[url-label ""]
              [url-combobox "growx, h 40!"]
              [(seesaw/separator) "growx, gaptop 10, gapbottom 10"]])))

(>defn show-startup-dialog
  "Show the startup dialog and call the callback with :connect, :cancel, or :reload."
  [parent callback]
  [[:maybe [:fn {:error/message "must be a JFrame"}
            #(instance? javax.swing.JFrame %)]]
   ifn? => nil?]
  (let [saved-url (config/get-most-recent-url)
        domain (config/get-domain) ;; This extracts domain from URL
        content (create-content-panel saved-url)
        url-combobox (seesaw/select content [:#url-input])
        ;; Create the dialog frame first
        dialog (atom nil)
        ;; Create button actions that can reference the dialog
        connect-action (action/action
                         :name (i18n/tr :startup-button-connect)
                         :handler (fn [e]
                                    (let [text (str (seesaw/value url-combobox))
                                          extracted (extract-domain text)]
                                      (if (validate-domain extracted)
                                        (do
                                          (config/add-url-to-history text)
                                          (state/set-domain! extracted)
                                          (seesaw/dispose! @dialog)
                                          (callback :connect))
                                        (seesaw/alert @dialog
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
        cancel-action (action/action
                        :name (i18n/tr :startup-button-cancel)
                        :handler (fn [e]
                                   (seesaw/dispose! @dialog)
                                   (callback :cancel)))
        ;; Create buttons with actions
        connect-button (seesaw/button :action connect-action
                                      :font {:size 14}
                                      :preferred-size [120 :by 35])
        cancel-button (seesaw/button :action cancel-action
                                     :font {:size 14}
                                     :preferred-size [120 :by 35])
        buttons-panel (seesaw/flow-panel
                        :align :center
                        :hgap 15
                        :items [connect-button cancel-button])
        main-panel (seesaw/border-panel
                     :center content
                     :south buttons-panel
                     :border 10)]

    ;; Create the actual dialog
    (reset! dialog
            (seesaw/frame
              :title (i18n/tr :startup-title)
              :icon (clojure.java.io/resource "main.png")
              :resizable? false
              :content main-panel
              :on-close :nothing))

    ;; Configure dialog properties
    (seesaw/config! @dialog :menubar (create-menu-bar @dialog callback))

    ;; Make Connect button the default when Enter is pressed
    (let [root-pane (seesaw/to-root @dialog)]
      (when-let [root (.getRootPane root-pane)]
        (.setDefaultButton root connect-button)))

    ;; Handle window closing
    (seesaw/listen @dialog :window-closing
                   (fn [e]
                     (seesaw/dispose! @dialog)
                     (callback :cancel)))

    ;; Pack and center the dialog
    (doto @dialog
      seesaw/pack!
      (.setLocationRelativeTo parent))

    ;; Log dialog creation
    (logging/log-info {:id ::show-dialog
                       :data {:url saved-url
                              :domain domain}
                       :msg (str "Showing startup dialog with URL: " saved-url)})

    ;; Show dialog
    (seesaw/show! @dialog)
    nil))