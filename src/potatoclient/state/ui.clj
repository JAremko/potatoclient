(ns potatoclient.state.ui
  "UI component state management.")

;; UI component references for updates
(defonce ^:private ui-refs
  (atom {}))

(defn register-ui-element!
  "Register a UI element for later updates."
  [element-key element]
  {:pre [(keyword? element-key)
         (some? element)]}
  (swap! ui-refs assoc element-key element))

(defn get-ui-element
  "Get a registered UI element."
  [element-key]
  {:pre [(keyword? element-key)]}
  (get @ui-refs element-key))

(defn all-ui-elements
  "Get all registered UI element keys."
  []
  (keys @ui-refs))

;; Legacy compatibility
(def ui-elements ui-refs)