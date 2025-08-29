(ns potatoclient.state "Application state management for PotatoClient UI.\n\n  This namespace provides the core UI state atom and basic accessors\n  for managing application UI state." (:require [clojure.string :as str]) (:import (java.net URI) (java.util Locale)))

(def initial-state "Initial application UI state" {:connection {:url "", :connected? false, :latency-ms nil, :reconnect-count 0}, :ui {:theme :sol-dark, :locale :english, :fullscreen? false, :read-only-mode? false, :show-overlay? true}, :processes {:state-proc {:pid nil, :status :stopped}, :cmd-proc {:pid nil, :status :stopped}, :heat-video {:pid nil, :status :stopped}, :day-video {:pid nil, :status :stopped}}, :stream-processes {}, :session {:user nil, :started-at nil}, :server-state {:system {:battery-level 0, :localization "en", :recording false, :mode :day, :temperature-c 20.0}, :lrf {:distance 0.0, :scan-mode "single", :target-locked false}, :gps {:latitude 0.0, :longitude 0.0, :altitude 0.0, :fix-type "none", :satellites 0, :hdop 99.9, :use-manual false}, :compass {:heading 0.0, :pitch 0.0, :roll 0.0, :unit "degrees", :calibrated false}, :rotary {:azimuth 0.0, :elevation 0.0, :azimuth-velocity 0.0, :elevation-velocity 0.0, :moving false, :mode :manual}, :camera-day {:zoom 1.0, :focus-mode :auto, :exposure-mode :auto, :brightness 50, :contrast 50, :recording false}, :camera-heat {:zoom 1.0, :palette :white-hot, :brightness 50, :contrast 50, :nuc-status :idle, :recording false}, :glass-heater {:enabled false, :temperature-c 20.0, :target-temp-c 25.0, :power-percent 0}}})

(defonce app-state (atom initial-state))

(defn get-connection-url "Get current connection URL." #:malli{:schema [:=> [:cat] [:maybe :string]]} [] (get-in (clojure.core/deref app-state) [:connection :url]))

(defn set-connection-url! "Set connection URL." #:malli{:schema [:=> [:cat :string] :map]} [url] (swap! app-state assoc-in [:connection :url] url))

(defn connected? "Check if connected to server." #:malli{:schema [:=> [:cat] :boolean]} [] (get-in (clojure.core/deref app-state) [:connection :connected?] false))

(defn set-connected! "Set connection status." #:malli{:schema [:=> [:cat :boolean] :map]} [connected?] (swap! app-state assoc-in [:connection :connected?] connected?))

(defn get-domain "Get current domain from connection URL." #:malli{:schema [:=> [:cat] :string]} [] (if-let [url (get-connection-url)] (try (let [uri (URI. url) host (.getHost uri)] (or host "localhost")) (catch Exception _ "localhost")) "localhost"))

(defn get-locale "Get current locale." #:malli{:schema [:=> [:cat] [:enum :english :ukrainian]]} [] (get-in (clojure.core/deref app-state) [:ui :locale] :english))

(defn set-locale! "Set current locale and update Java default." #:malli{:schema [:=> [:cat [:enum :english :ukrainian]] :map]} [locale] (let [locale-map {:english ["en" "US"], :ukrainian ["uk" "UA"]} [lang country] (get locale-map locale ["en" "US"])] (Locale/setDefault (Locale/forLanguageTag (str lang "-" country)))) (swap! app-state assoc-in [:ui :locale] locale))

(defn get-theme "Get current theme." #:malli{:schema [:=> [:cat] :keyword]} [] (get-in (clojure.core/deref app-state) [:ui :theme] :sol-dark))

(defn set-theme! "Set UI theme." #:malli{:schema [:=> [:cat :keyword] :map]} [theme] (swap! app-state assoc-in [:ui :theme] theme))

(defn fullscreen? "Check if in fullscreen mode." #:malli{:schema [:=> [:cat] :boolean]} [] (get-in (clojure.core/deref app-state) [:ui :fullscreen?] false))

(defn set-fullscreen! "Set fullscreen mode." #:malli{:schema [:=> [:cat :boolean] :map]} [fullscreen?] (swap! app-state assoc-in [:ui :fullscreen?] fullscreen?))

(defn get-session "Get current session info." #:malli{:schema [:=> [:cat] :map]} [] (get (clojure.core/deref app-state) :session {}))

(defn start-session! "Start a new session." #:malli{:schema [:=> [:cat [:maybe :string]] :map]} [user] (swap! app-state assoc :session {:user user, :started-at (System/currentTimeMillis)}))

(defn end-session! "End current session." #:malli{:schema [:=> [:cat] :map]} [] (swap! app-state assoc :session {:user nil, :started-at nil}))

(defn get-process-state "Get state of a specific process." #:malli{:schema [:=> [:cat [:enum :state-proc :cmd-proc :heat-video :day-video]] [:maybe :map]]} [process-key] (get-in (clojure.core/deref app-state) [:processes process-key]))

(defn process-running? "Check if a process is running." #:malli{:schema [:=> [:cat [:enum :state-proc :cmd-proc :heat-video :day-video]] :boolean]} [process-key] (= :running (get-in (clojure.core/deref app-state) [:processes process-key :status])))

(defn update-process-status! "Update process status." #:malli{:schema [:=> [:cat [:enum :state-proc :cmd-proc :heat-video :day-video] [:maybe :pos-int] [:enum :running :stopped :error]] :map]} [process-key pid status] (swap! app-state assoc-in [:processes process-key] {:pid pid, :status status}))

(defn add-stream-process! "Add stream process info." #:malli{:schema [:=> [:cat [:enum :heat :day] :map] :map]} [stream-type process-map] (let [process-key (case stream-type :heat :heat-video :day :day-video)] (swap! app-state assoc-in [:stream-processes process-key] process-map)))

(defn get-stream-process "Get stream process info." #:malli{:schema [:=> [:cat [:enum :heat :day]] [:maybe :map]]} [stream-type] (let [process-key (case stream-type :heat :heat-video :day :day-video)] (get-in (clojure.core/deref app-state) [:stream-processes process-key])))

(defn remove-stream-process! "Remove stream process." #:malli{:schema [:=> [:cat [:enum :heat :day]] :map]} [stream-type] (let [process-key (case stream-type :heat :heat-video :day :day-video)] (swap! app-state update :stream-processes dissoc process-key)))

(defn get-all-stream-processes "Get all stream processes." #:malli{:schema [:=> [:cat] [:map-of keyword? map?]]} [] (get (clojure.core/deref app-state) :stream-processes {}))

(defn get-server-state "Get the current server state." #:malli{:schema [:=> [:cat] :map]} [] (:server-state (clojure.core/deref app-state)))

(defn update-server-state! "Update server state." #:malli{:schema [:=> [:cat :map] :map]} [updates] (swap! app-state update :server-state merge updates))

(defn get-subsystem-state "Get state for a specific subsystem." #:malli{:schema [:=> [:cat :keyword] [:maybe :map]]} [subsystem] (get-in (clojure.core/deref app-state) [:server-state subsystem]))

(defn read-only-mode? "Check if in read-only mode." #:malli{:schema [:=> [:cat] :boolean]} [] (get-in (clojure.core/deref app-state) [:ui :read-only-mode?] false))

(defn set-read-only-mode! "Set read-only mode." #:malli{:schema [:=> [:cat :boolean] :map]} [enabled?] (swap! app-state assoc-in [:ui :read-only-mode?] enabled?))

(defn show-overlay? "Check if overlay should be shown." #:malli{:schema [:=> [:cat] :boolean]} [] (get-in (clojure.core/deref app-state) [:ui :show-overlay?] true))

(defn set-show-overlay! "Set overlay visibility." #:malli{:schema [:=> [:cat :boolean] :map]} [show?] (swap! app-state assoc-in [:ui :show-overlay?] show?))

(defn get-connection-latency "Get connection latency in milliseconds." #:malli{:schema [:=> [:cat] [:maybe :pos-int]]} [] (get-in (clojure.core/deref app-state) [:connection :latency-ms]))

(defn get-reconnect-count "Get reconnection attempt count." #:malli{:schema [:=> [:cat] :int]} [] (get-in (clojure.core/deref app-state) [:connection :reconnect-count] 0))

(defn set-connection-state! "Set complete connection state." #:malli{:schema [:=> [:cat :boolean [:maybe :string] [:maybe :pos-int]] :map]} [connected? url latency-ms] (swap! app-state update :connection (fn [conn] (cond-> (assoc conn :connected? connected?) url (assoc :url url) latency-ms (assoc :latency-ms latency-ms)))))

(defn increment-reconnect-count! "Increment reconnection attempt counter." #:malli{:schema [:=> [:cat] :map]} [] (swap! app-state update-in [:connection :reconnect-count] (fnil inc 0)))

(defn reset-reconnect-count! "Reset reconnection attempt counter." #:malli{:schema [:=> [:cat] :map]} [] (swap! app-state assoc-in [:connection :reconnect-count] 0))

(defn add-watch-handler "Add a watch handler to app-state." #:malli{:schema [:=> [:cat :keyword :fn] :nil]} [key handler-fn] (add-watch app-state key handler-fn) nil)

(defn remove-watch-handler "Remove a watch handler from app-state." #:malli{:schema [:=> [:cat :keyword] :nil]} [key] (remove-watch app-state key) nil)

(defn cleanup-seesaw-bindings! "Remove all watchers added by seesaw.bind from app-state.\n  Seesaw bind uses gensym keys like :bindable-atom-watcherXXXXX\n  This scans and removes all such watchers." #:malli{:schema [:=> [:cat] :nil]} [] (let [watchers (keys (.getWatches app-state)) seesaw-watchers (filter (fn* [p1__275#] (and (keyword? p1__275#) (when-let [name-str (name p1__275#)] (or (str/starts-with? name-str "bindable-atom-watcher") (str/starts-with? name-str "bindable-agent-watcher") (str/starts-with? name-str "bindable-ref-watcher"))))) watchers)] (doseq [watcher-key seesaw-watchers] (remove-watch app-state watcher-key)) nil))

(defn current-state "Get a snapshot of entire app state (for debugging)." #:malli{:schema [:=> [:cat] :map]} [] (clojure.core/deref app-state))

(defn reset-state! "Reset entire app state to initial values." #:malli{:schema [:=> [:cat] :map]} [] (reset! app-state initial-state))