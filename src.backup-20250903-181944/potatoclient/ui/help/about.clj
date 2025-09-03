(ns potatoclient.ui.help.about
  "About dialog for the application."
  (:require
            [malli.core :as m] [clojure.java.io :as io]
            [clojure.string :as str]
            [potatoclient.i18n :as i18n]
            [potatoclient.runtime :as runtime]
            [potatoclient.theme :as theme]
            [seesaw.core :as seesaw])
  (:import (java.awt BorderLayout Dimension Font)
           (javax.swing BorderFactory Box BoxLayout JDialog JFrame JLabel JPanel SwingConstants)))

(defn- create-styled-label
  "Create a styled label with specified properties."
  ([text] (create-styled-label text {}))
  ([text {:keys [font-size font-style alignment border]
          :or {font-size 14 font-style Font/PLAIN alignment :center}}]
   (let [label (seesaw/label :text text
                             :halign alignment)]
     (.setFont label (Font. "SansSerif" font-style font-size))
     (when border
       (.setBorder label border))
     label))) 
 (m/=> create-styled-label [:=> [:cat :string [:? :map]] [:fn (partial instance? JLabel)]])

(defn- create-info-panel
  "Create the information panel with app details."
  []
  (let [version (try
                  (str/trim (slurp (io/resource "VERSION")))
                  (catch Exception _ "dev"))
        build-type (if (runtime/release-build?) "RELEASE" "DEVELOPMENT")
        panel (JPanel.)
        layout (BoxLayout. panel BoxLayout/Y_AXIS)]

    (.setLayout panel layout)
    (.setBorder panel (BorderFactory/createEmptyBorder 20 30 20 30))

    ;; App icon
    (when-let [icon-resource (io/resource "main.png")]
      (let [icon-label (JLabel. (seesaw/icon icon-resource))]
        (.setAlignmentX icon-label JLabel/CENTER_ALIGNMENT)
        (.add panel icon-label)
        (.add panel (Box/createRigidArea (Dimension. 0 15)))))

    ;; App name
    (let [app-label (create-styled-label "PotatoClient"
                                         {:font-size 24
                                          :font-style Font/BOLD})]
      (.setAlignmentX app-label JLabel/CENTER_ALIGNMENT)
      (.add panel app-label)
      (.add panel (Box/createRigidArea (Dimension. 0 10))))

    ;; Version info
    (let [version-label (create-styled-label (str (i18n/tr :app-version) ": " version)
                                             {:font-size 14})]
      (.setAlignmentX version-label JLabel/CENTER_ALIGNMENT)
      (.add panel version-label)
      (.add panel (Box/createRigidArea (Dimension. 0 5))))

    ;; Build type
    (let [build-label (create-styled-label (str (i18n/tr :build-type) ": " build-type)
                                           {:font-size 12
                                            :font-style Font/ITALIC})]
      (.setAlignmentX build-label JLabel/CENTER_ALIGNMENT)
      (.add panel build-label)
      (.add panel (Box/createRigidArea (Dimension. 0 20))))

    ;; Description
    (let [desc-label (create-styled-label (i18n/tr :about-text)
                                          {:font-size 12})]
      (.setAlignmentX desc-label JLabel/CENTER_ALIGNMENT)
      (.setHorizontalAlignment desc-label SwingConstants/CENTER)
      (.setMaximumSize desc-label (Dimension. 400 100))
      (.add panel desc-label))

    panel)) 
 (m/=> create-info-panel [:=> [:cat] [:fn (partial instance? JPanel)]])

(defn- create-button-panel
  "Create the button panel with OK button."
  [dialog]
  (let [panel (seesaw/flow-panel :align :center
                                 :hgap 10
                                 :vgap 10)]
    (.setBorder panel (BorderFactory/createEmptyBorder 10 10 10 10))

    (let [ok-button (seesaw/button :text (i18n/tr :ok)
                                   :icon (theme/key->icon :check-icon)
                                   :font {:size 14}
                                   :preferred-size [120 :by 70]
                                   :listen [:action (fn [_] (seesaw/dispose! dialog))])]
      (.add panel ok-button))

    panel)) 
 (m/=> create-button-panel [:=> [:cat [:fn (partial instance? JDialog)]] [:fn (partial instance? JPanel)]])

(defn show-about-dialog
  "Show the About dialog."
  [parent]
  (let [dialog (JDialog. parent (i18n/tr :about-title) true)]

    ;; Configure dialog
    (.setDefaultCloseOperation dialog JDialog/DISPOSE_ON_CLOSE)
    (.setResizable dialog false)

    ;; Create layout
    (let [content-pane (.getContentPane dialog)]
      (.setLayout content-pane (BorderLayout.))
      (.add content-pane (create-info-panel) BorderLayout/CENTER)
      (.add content-pane (create-button-panel dialog) BorderLayout/SOUTH))

    ;; Size and position
    (.pack dialog)
    (.setLocationRelativeTo dialog parent)

    ;; Show dialog
    (seesaw/invoke-later
      (.setVisible dialog true)))) 
 (m/=> show-about-dialog [:=> [:cat [:fn {:error/message "must be a JFrame"} (partial instance? JFrame)]] :nil])