(ns potatoclient.ui.dialogs.connection-lost
  "Modal dialog for handling lost connection to state server while in main frame."
  (:require
    [clojure.java.io :as io]
    [malli.core :as m]
    [potatoclient.i18n :as i18n]
    [potatoclient.logging :as logging]
    [potatoclient.state.server.core :as state-server]
    [seesaw.action :as action]
    [seesaw.core :as seesaw]
    [seesaw.mig :as mig])
  (:import (javax.swing JDialog JFrame JLabel JPanel SwingConstants)))

(defn- create-content-panel
  "Create the content panel for the connection lost dialog."
  []
  (let [icon-label (seesaw/label
                     :icon (io/resource "icons/warning.png")
                     :halign :center)
        title-label (seesaw/label
                      :text (i18n/tr :connection-lost-title)
                      :font {:size 18 :style :bold}
                      :halign :center)
        message-label (seesaw/label
                        :text (str "<html><center>"
                                   (i18n/tr :connection-lost-message) "<br><br>"
                                   "<span style='color:gray;font-size:11px;'>"
                                   (i18n/tr :connection-lost-description)
                                   "</span></center></html>")
                        :font {:size 14}
                        :halign :center)
        status-label (seesaw/label
                       :id :status-label
                       :text (i18n/tr :connection-lost-reconnecting)
                       :font {:size 12}
                       :halign :center
                       :foreground :orange)]
    (mig/mig-panel
      :constraints ["wrap 1, insets 30, gap 15, align center" "[grow, fill, align center]" "[]"]
      :items [[icon-label ""]
              [title-label "gaptop 10"]
              [message-label "gaptop 20"]
              [status-label "gaptop 15"]])))
(m/=> create-content-panel [:=> [:cat] [:fn {:error/message "must be a Swing panel"} (partial instance? JPanel)]])

(defn- monitor-reconnection!
  "Monitor reconnection attempts and update UI."
  [dialog status-label monitoring?]
  (future
    (try
      (while @monitoring?
        (when (state-server/get-manager)
          (let [stats (state-server/get-connection-stats)
                status (:status stats :disconnected)
                attempts (:consecutive-failures stats 0)]

            (seesaw/invoke-later
              (seesaw/config! status-label
                              :text (case status
                                      :connected (i18n/tr :connection-lost-reconnected)
                                      :connecting (i18n/tr :connection-lost-reconnecting-attempts [attempts])
                                      :disconnected (i18n/tr :connection-lost-disconnected)
                                      (i18n/tr :connection-lost-reconnecting))
                              :foreground (case status
                                            :connected :green
                                            :connecting :orange
                                            :red)))

            ;; If reconnected, close the dialog
            (when (= status :connected)
              (reset! monitoring? false)
              (seesaw/invoke-later
                (Thread/sleep 500) ; Brief pause to show success
                (when (.isDisplayable dialog)
                  (seesaw/dispose! dialog))))))

        (Thread/sleep 500))
      (catch InterruptedException _
        (logging/log-debug {:msg "Reconnection monitoring interrupted"}))
      (catch Exception e
        (logging/log-error {:msg "Error monitoring reconnection" :error e})))))

(m/=> monitor-reconnection! [:=> [:cat any? any? any?] :nil])

(defn show-connection-lost-dialog
  "Show modal dialog for lost connection with option to cancel and return to initial menu.
   
   Parameters:
   - parent: Parent frame (usually the main frame)
   - on-cancel: Callback function to execute when user cancels (typically returns to initial menu)
   
   Returns nil."
  [parent on-cancel]
  (let [content (create-content-panel)
        status-label (seesaw/select content [:#status-label])
        monitoring? (atom true)

        dialog (seesaw/custom-dialog
                 :parent parent
                 :title (i18n/tr :connection-lost-title)
                 :icon (io/resource "main.png")
                 :modal? true
                 :resizable? false
                 :on-close :nothing
                 :content content)

        cancel-action (action/action
                        :name (i18n/tr :connection-lost-cancel)
                        :handler (fn [_]
                                   (reset! monitoring? false)
                                   (seesaw/dispose! dialog)
                                   (when on-cancel
                                     (on-cancel))))

        cancel-button (seesaw/button
                        :action cancel-action
                        :font {:size 14}
                        :preferred-size [120 :by 40])

        buttons-panel (seesaw/flow-panel
                        :align :center
                        :items [cancel-button])]

    ;; Add buttons to dialog
    (seesaw/config! dialog
                    :content (seesaw/border-panel
                               :center content
                               :south buttons-panel
                               :border 20))

    ;; Handle window closing
    (seesaw/listen dialog :window-closing
                   (fn [_]
                     (reset! monitoring? false)
                     (seesaw/dispose! dialog)
                     (when on-cancel
                       (on-cancel))))

    ;; Start monitoring reconnection
    (monitor-reconnection! dialog status-label monitoring?)

    ;; Show dialog
    (doto dialog
      seesaw/pack!
      (.setLocationRelativeTo parent))

    (logging/log-info {:msg "Showing connection lost dialog"})
    (seesaw/show! dialog)
    nil))
(m/=> show-connection-lost-dialog [:=> [:cat [:maybe [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]] [:maybe :ifn]] :nil])