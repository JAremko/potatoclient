(ns potatoclient.ui.help.menu
  "Help menu creation and management."
  (:require
            [malli.core :as m] [clojure.java.io :as io]
            [potatoclient.i18n :as i18n]
            [potatoclient.theme :as theme]
            [potatoclient.ui.help.about :as about]
            [potatoclient.ui.log-viewer :as log-viewer]
            [seesaw.action :as action]
            [seesaw.core :as seesaw])
  (:import (java.awt Image)
           (javax.swing ImageIcon JFrame JMenu JMenuItem JSeparator)))

(defn- get-main-app-icon
  "Get the main app icon resized to menu size."
  []
  (when-let [icon-resource (io/resource "main.png")]
    (let [icon (ImageIcon. icon-resource)
          img (.getImage icon)
          ;; Scale to 16x16 to match other menu icons
          scaled-img (.getScaledInstance img 16 16 Image/SCALE_SMOOTH)]
      (ImageIcon. scaled-img)))) 
 (m/=> get-main-app-icon [:=> [:cat] [:maybe [:fn (partial instance? ImageIcon)]]])

(defn- create-menu-item
  "Create a styled menu item with icon and handler."
  [text-key icon-key-or-icon handler]
  (let [icon (if (keyword? icon-key-or-icon)
               (theme/key->icon icon-key-or-icon)
               icon-key-or-icon)
        item (seesaw/menu-item :text (i18n/tr text-key)
                               :icon icon
                               :listen [:action handler])]
    ;; Add padding for better visual appearance
    (.setBorder item (javax.swing.BorderFactory/createEmptyBorder 5 10 5 10))
    item)) 
 (m/=> create-menu-item [:=> [:cat :keyword [:or :keyword :nil] :ifn] [:fn (partial instance? JMenuItem)]])

(defn- add-separator
  "Add a separator to the menu."
  [menu]
  (.add menu (JSeparator.))) 
 (m/=> add-separator [:=> [:cat [:fn (partial instance? JMenu)]] :nil])

(defn create-help-menu
  "Create the Help menu with improved formatting and organization.
   
   Options:
   - :parent - Parent frame for dialogs
   - :include-logs? - Whether to include log viewer (default: true)
   - :include-shortcuts? - Whether to include keyboard shortcuts (default: false)
   - :include-docs? - Whether to include documentation links (default: false)"
  [{:keys [parent include-logs? include-shortcuts? include-docs?]
    :or {include-logs? true
         include-shortcuts? false
         include-docs? false}}]

  (let [menu (seesaw/menu :text (i18n/tr :menu-help)
                          :icon (theme/key->icon :actions-group-menu))]

    ;; Set menu properties for better appearance
    (.setFont menu (java.awt.Font. "SansSerif" java.awt.Font/PLAIN 14))

    ;; Documentation section (if enabled)
    (when include-docs?
      (.add menu (create-menu-item :menu-help-documentation
                                   :file-open
                                   (fn [_] (println "Open documentation"))))
      (.add menu (create-menu-item :menu-help-online-help
                                   :web-icon
                                   (fn [_] (println "Open online help"))))
      (add-separator menu))

    ;; Keyboard shortcuts (if enabled)
    (when include-shortcuts?
      (.add menu (create-menu-item :menu-help-shortcuts
                                   :keyboard-icon
                                   (fn [_] (println "Show keyboard shortcuts"))))
      (add-separator menu))

    ;; Log viewer (if enabled)
    (when include-logs?
      (.add menu (create-menu-item :menu-help-view-logs
                                   :file-open
                                   (fn [_] (log-viewer/show-log-viewer))))
      (add-separator menu))

    ;; About dialog (always included)
    (.add menu (create-menu-item :menu-help-about
                                 (get-main-app-icon)
                                 (fn [_] (when parent
                                           (about/show-about-dialog parent)))))

    menu)) 
 (m/=> create-help-menu [:=> [:cat :map] [:fn (partial instance? JMenu)]])

(defn create-help-action
  "Create a help action for toolbars or other contexts."
  [action-type parent]
  (case action-type
    :about (action/action
             :name (i18n/tr :menu-help-about)
             :icon (get-main-app-icon)
             :tip (i18n/tr :menu-help-about-tip)
             :handler (fn [_] (about/show-about-dialog parent)))

    :logs (action/action
            :name (i18n/tr :menu-help-view-logs)
            :icon (theme/key->icon :file-open)
            :tip (i18n/tr :menu-help-logs-tip)
            :handler (fn [_] (log-viewer/show-log-viewer)))

    :help (action/action
            :name (i18n/tr :menu-help)
            :icon (theme/key->icon :actions-group-menu)
            :tip (i18n/tr :menu-help-tip)
            :handler (fn [_] (about/show-about-dialog parent)))

    nil)) 
 (m/=> create-help-action [:=> [:cat :keyword [:fn (partial instance? JFrame)]] :any])