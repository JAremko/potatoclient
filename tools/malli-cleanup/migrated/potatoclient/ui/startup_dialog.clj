(ns
  potatoclient.ui.startup-dialog
  "Startup dialog for server connection with theme and localization support."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [potatoclient.config :as config]
    [potatoclient.i18n :as i18n]
    [potatoclient.logging :as logging]
    [potatoclient.state :as state]
    [potatoclient.ui.menu-bar :as menu-bar]
    [potatoclient.url-parser :as url-parser]
    [seesaw.action :as action]
    [seesaw.border :as border]
    [seesaw.core :as seesaw]
    [seesaw.mig :as mig])
  (:import (javax.swing JFrame JPanel)))

(defn-
  reload-dialog!
  "Reload the dialog with new theme/locale."
  #:malli{:schema
          [:=>
           [:cat
            [:fn
             #:error{:message "must be a JFrame"}
             (partial instance? JFrame)]
            :ifn]
           :nil]}
  [dialog callback]
  (seesaw/dispose! dialog)
  (callback :reload))

(defn-
  create-content-panel
  "Create the main content panel for the startup dialog."
  #:malli{:schema
          [:=>
           [:cat [:maybe string?]]
           [:fn
            #:error{:message "must be a Swing panel"}
            (partial instance? JPanel)]]}
  [saved-url]
  (let
    [url-label
     (seesaw/label :text (i18n/tr :startup-server-url) :font {:size 14})
     recent-urls
     (config/get-recent-urls)
     combo-items
     (vec (distinct (concat (when saved-url [saved-url]) recent-urls)))
     url-combobox
     (seesaw/combobox
       :id
       :url-input
       :model
       combo-items
       :editable?
       true
       :font
       {:size 14}
       :border
       (border/compound-border
         (border/line-border :thickness 1 :color :gray)
         (border/empty-border :thickness 5)))]
    (when saved-url (seesaw/value! url-combobox saved-url))
    (mig/mig-panel
      :constraints
      ["wrap 1, insets 20, gap 10" "[grow, fill]" "[]"]
      :items
      [[url-label ""]
       [url-combobox "growx, h 40!"]
       [(seesaw/separator) "growx, gaptop 10, gapbottom 10"]])))

(defn
  show-startup-dialog
  "Show the startup dialog and call the callback with :connect, :cancel, or :reload."
  #:malli{:schema
          [:=>
           [:cat
            [:maybe
             [:fn
              #:error{:message "must be a JFrame"}
              (partial instance? JFrame)]]
            :ifn]
           :nil]}
  [parent callback]
  (let
    [saved-url
     (config/get-most-recent-url)
     domain
     (config/get-domain)
     content
     (create-content-panel saved-url)
     url-combobox
     (seesaw/select content [:#url-input])
     dialog
     (atom nil)
     connect-action
     (action/action
       :name
       (i18n/tr :startup-button-connect)
       :handler
       (fn
         [_]
         (let
           [text
            (str (seesaw/value url-combobox))
            validation
            (url-parser/validate-url-input text)]
           (if
             (:valid validation)
             (do
               (config/add-url-to-history text)
               (config/update-config! :domain (:domain validation))
               (state/set-connection-url! (str "wss://" (:domain validation)))
               (seesaw/dispose! @dialog)
               (callback :connect))
             (seesaw/alert
               @dialog
               (str
                 (i18n/tr (:error validation))
                 "\n\n"
                 (i18n/tr :startup-valid-formats)
                 ":\n"
                 "• "
                 (str/join
                   "\n• "
                   (map i18n/tr (url-parser/get-example-format-keys))))
               :title
               (i18n/tr :startup-invalid-url)
               :type
               :error)))))
     cancel-action
     (action/action
       :name
       (i18n/tr :startup-button-cancel)
       :handler
       (fn [_] (seesaw/dispose! @dialog) (callback :cancel)))
     connect-button
     (seesaw/button
       :action
       connect-action
       :font
       {:size 14}
       :preferred-size
       [120 :by 70])
     cancel-button
     (seesaw/button
       :action
       cancel-action
       :font
       {:size 14}
       :preferred-size
       [120 :by 70])
     buttons-panel
     (seesaw/flow-panel
       :align
       :center
       :hgap
       15
       :items
       [connect-button cancel-button])
     main-panel
     (seesaw/border-panel
       :center
       content
       :south
       buttons-panel
       :border
       10)]
    (reset!
      dialog
      (seesaw/frame
        :title
        (i18n/tr :startup-title)
        :icon
        (io/resource "main.png")
        :resizable?
        false
        :content
        main-panel
        :on-close
        :nothing))
    (seesaw/config!
      @dialog
      :menubar
      (menu-bar/create-menubar
        {:reload-fn (fn [_] (reload-dialog! @dialog callback)),
         :include-theme? true,
         :include-language? true,
         :include-help? false,
         :include-stream-buttons? false}))
    (let
      [root-pane (seesaw/to-root @dialog)]
      (when-let
        [root (.getRootPane root-pane)]
        (.setDefaultButton root connect-button)))
    (seesaw/listen
      @dialog
      :window-closing
      (fn [_] (seesaw/dispose! @dialog) (callback :cancel)))
    (doto @dialog seesaw/pack! (.setLocationRelativeTo parent))
    (logging/log-info
      {:id :user/show-dialog,
       :data {:url saved-url, :domain domain},
       :msg (str "Showing startup dialog with URL: " saved-url)})
    (seesaw/show! @dialog)
    nil))
