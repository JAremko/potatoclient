(ns potatoclient.state.ui
  "UI component state management."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn ?]]))

;; UI component references for updates
(defonce ^:private ui-refs
  (atom {}))

(>defn register-ui-element!
  "Register a UI element for later updates."
  [element-key element]
  [keyword? [:or :potatoclient.specs/jbutton :potatoclient.specs/jtoggle-button :potatoclient.specs/jframe :potatoclient.specs/jpanel] => nil?]
  (swap! ui-refs assoc element-key element))

(>defn get-ui-element
  "Get a registered UI element."
  [element-key]
  [keyword? => (? [:or :potatoclient.specs/jbutton :potatoclient.specs/jtoggle-button :potatoclient.specs/jframe :potatoclient.specs/jpanel])]
  (get @ui-refs element-key))

(>defn all-ui-elements
  "Get all registered UI element keys."
  []
  [=> [:sequential keyword?]]
  (keys @ui-refs))

;; Legacy compatibility
(def ui-elements
  "Legacy alias for ui-refs atom."
  ui-refs)