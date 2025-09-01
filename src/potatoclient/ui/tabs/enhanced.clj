(ns potatoclient.ui.tabs.enhanced
  "Enhanced tabbed panel with horizontal layout and detachable windows.
  
  Provides a tabbed interface where each tab can be detached into its own
  window. Tab selection is managed through the app state atom for persistence
  and synchronized updates."
  (:require [potatoclient.i18n :as i18n]
            [potatoclient.state :as state]
            [potatoclient.theme :as theme]
            [potatoclient.ui.bind-group :as bg]
            [potatoclient.ui.debounce :as debounce]
            [potatoclient.ui.tabs.detachable :as detachable]
            [seesaw.core :as seesaw]
            [seesaw.bind :as bind])
  (:import [javax.swing JTabbedPane JPanel JButton JFrame]
           [java.awt BorderLayout FlowLayout]))

(defn- create-tab-header
  "Create a tab header panel with title and window toggle button.
  
  Parameters:
    tab-key - The tab identifier
    title - The tab title text
    parent-frame - The parent frame for detached windows
    content-factory - Function that creates tab content with optional binding group
  
  Returns a JPanel with the tab header."
  {:malli/schema [:=> [:cat :keyword :string [:fn #(instance? JFrame %)] [:=> [:cat [:maybe :keyword]] :any]]
                  [:fn #(instance? JPanel %)]]}
  [tab-key title parent-frame content-factory]
  (let [panel (JPanel. (FlowLayout. FlowLayout/LEFT 5 0))
        label (seesaw/label :text title)
        ;; Use a checkbox which should work better with selection binding
        checkbox (seesaw/checkbox :text "⧉" ; Window icon character
                                  :selected? (state/tab-has-window? tab-key) ; Initialize from state
                                  :tip (i18n/tr :detach-window-tip)
                                  :font {:size 12})]

    ;; Bind checkbox selection to app state
    (bg/bind-group (keyword (str "tab-header-" (name tab-key)))
                   state/app-state
                   (bind/transform (fn [state]
                                     (get-in state [:ui :tab-properties tab-key :has-window] false)))
                   (bind/selection checkbox))

    ;; Handle checkbox changes - update window state based on checkbox state
    (seesaw/listen checkbox :action
                   (fn [e]
                     (let [selected (seesaw/selection checkbox)]
                       ;; Update window state based on checkbox
                       (if selected
                         (when-not (state/tab-has-window? tab-key)
                           (detachable/create-detached-window! tab-key content-factory parent-frame))
                         (when (state/tab-has-window? tab-key)
                           (detachable/close-detached-window! tab-key))))))

    ;; Add components to panel
    (.add panel label)
    (.add panel checkbox)
    panel))

(defn- create-tab-content-wrapper
  "Create a wrapper panel for tab content in the main window."
  {:malli/schema [:=> [:cat :keyword [:=> [:cat [:maybe :keyword]] :any]] [:fn #(instance? JPanel %)]]}
  [tab-key content-factory]
  (let [wrapper (JPanel. (BorderLayout.))
        ;; For main window content, pass nil for binding group (uses direct seesaw bind)
        content (content-factory nil)]
    (.add wrapper content BorderLayout/CENTER)
    wrapper))

(defn create-enhanced-tabs
  "Create an enhanced tabbed panel with horizontal tabs and detachable windows.
  
  Parameters:
    parent-frame - The parent frame for positioning detached windows
    tabs-config - Vector of tab configurations, each being a map with:
                  :key - Keyword identifier for the tab
                  :title - Display title for the tab
                  :content-factory - Function that takes optional binding-group key
                                     and creates the tab content
  
  Returns a JTabbedPane configured with all tabs.
  
  Example:
    (create-enhanced-tabs frame
      [{:key :controls
        :title \"Controls\"
        :content-factory (fn [binding-group]
                          (if binding-group
                            ;; Use bg/bind-group for detached window
                            (create-controls-with-bindings binding-group)
                            ;; Use regular seesaw/bind for main window
                            (create-controls-with-direct-bindings)))}])"
  {:malli/schema [:=> [:cat [:fn #(instance? JFrame %)] [:sequential :map]]
                  [:fn #(instance? JTabbedPane %)]]}
  [parent-frame tabs-config]
  (let [tabbed-pane (JTabbedPane. JTabbedPane/TOP)]

    ;; Configure tabbed pane
    (.setTabLayoutPolicy tabbed-pane JTabbedPane/SCROLL_TAB_LAYOUT)

    ;; Add each tab
    (doseq [{:keys [key title content-factory]} tabs-config]
      (let [;; Create tab content wrapper
            content (create-tab-content-wrapper key content-factory)

            ;; Create custom tab header with window button
            header (create-tab-header key title parent-frame content-factory)]

        ;; Add tab to pane
        (.addTab tabbed-pane title content)

        ;; Set custom tab component (header with button)
        (let [index (.indexOfComponent tabbed-pane content)]
          (.setTabComponentAt tabbed-pane index header))))

    ;; Bind tab selection to app state (with debouncing)
    (let [debounced-state (debounce/debounce-atom state/app-state 100)]
      ;; Update tabbed pane when state changes
      (bg/bind-group :tab-selection
                     debounced-state
                     (bind/transform (fn [_] (state/get-active-tab)))
                     (bind/b-do [tab-key]
                                (dotimes [i (.getTabCount tabbed-pane)]
                                  (let [tab-config (nth tabs-config i nil)]
                                    (when (and tab-config (= (:key tab-config) tab-key))
                                      (.setSelectedIndex tabbed-pane i))))))

      ;; Update state when tab selection changes
      (seesaw/listen tabbed-pane :change
                     (fn [_]
                       (let [index (.getSelectedIndex tabbed-pane)]
                         (when (>= index 0)
                           (when-let [tab-config (nth tabs-config index nil)]
                             (state/set-active-tab! (:key tab-config))))))))

    tabbed-pane))

(defn- create-placeholder-content
  "Create placeholder content for a tab.
  
  Parameters:
    binding-group - Optional binding group key for detached windows
    text - The placeholder text
  
  Returns a panel with the placeholder content."
  {:malli/schema [:=> [:cat [:maybe :keyword] :string] [:fn #(instance? JPanel %)]]}
  [binding-group text]
  ;; For now, just return a simple label
  ;; In real implementation, you'd use binding-group to set up
  ;; bg/bind-group bindings for detached windows
  ;; or regular seesaw/bind for main window (when binding-group is nil)
  (seesaw/label :text text :halign :center))

(defn create-default-tabs
  "Create the default set of tabs for the application.
  
  Parameters:
    parent-frame - The parent frame for detached windows
  
  Returns a configured JTabbedPane with all default tabs."
  {:malli/schema [:=> [:cat [:fn #(instance? JFrame %)]] [:fn #(instance? JTabbedPane %)]]}
  [parent-frame]
  (create-enhanced-tabs parent-frame
                        [{:key :controls
                          :title (i18n/tr :tab-controls)
                          :content-factory (fn [binding-group]
                                             (create-placeholder-content binding-group "Controls panel placeholder"))}

                         {:key :day-camera
                          :title (i18n/tr :tab-day-camera)
                          :content-factory (fn [binding-group]
                                             (create-placeholder-content binding-group "Day Camera panel placeholder"))}

                         {:key :thermal-camera
                          :title (i18n/tr :tab-thermal-camera)
                          :content-factory (fn [binding-group]
                                             (create-placeholder-content binding-group "Thermal Camera panel placeholder"))}

                         {:key :modes
                          :title (i18n/tr :tab-modes)
                          :content-factory (fn [binding-group]
                                             (create-placeholder-content binding-group "Modes panel placeholder"))}

                         {:key :media
                          :title (i18n/tr :tab-media)
                          :content-factory (fn [binding-group]
                                             (create-placeholder-content binding-group "Media panel placeholder"))}]))

(defn reload-tabs!
  "Reload tabs and all detached windows (for theme/locale changes).
  
  Parameters:
    parent-frame - The parent frame
    tabbed-pane - The tabbed pane to reload
    tabs-config - The tabs configuration"
  {:malli/schema [:=> [:cat [:fn #(instance? JFrame %)]
                       [:fn #(instance? JTabbedPane %)]
                       [:sequential :map]] :nil]}
  [parent-frame tabbed-pane tabs-config]
  ;; Clean up all tab-related bindings
  (bg/clean-group :tab-selection state/app-state)
  (doseq [{:keys [key]} tabs-config]
    (bg/clean-group (keyword (str "tab-header-" (name key))) state/app-state))

  ;; Reload detached windows
  (detachable/reload-all-windows! parent-frame tabs-config)

  ;; Clear and recreate tabs
  (.removeAll tabbed-pane)

  ;; Re-add all tabs
  (doseq [{:keys [key title content-factory]} tabs-config]
    (let [content (create-tab-content-wrapper key content-factory)
          header (create-tab-header key title parent-frame content-factory)]
      (.addTab tabbed-pane title content)
      (let [index (.indexOfComponent tabbed-pane content)]
        (.setTabComponentAt tabbed-pane index header))))

  ;; Re-establish bindings
  (let [debounced-state (debounce/debounce-atom state/app-state 100)]
    (bg/bind-group :tab-selection
                   debounced-state
                   (bind/transform (fn [_] (state/get-active-tab)))
                   (bind/b-do [tab-key]
                              (dotimes [i (.getTabCount tabbed-pane)]
                                (let [tab-config (nth tabs-config i nil)]
                                  (when (and tab-config (= (:key tab-config) tab-key))
                                    (.setSelectedIndex tabbed-pane i)))))))
  nil)