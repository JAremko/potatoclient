(ns potatoclient.dev-instrumentation
  "Development-time instrumentation using Malli.
   
   This namespace provides runtime validation of function arguments and
   return values during development, with automatic re-instrumentation
   when schemas change.
   
   Two reporting modes are available:
   1. :print - Prints pretty errors to stdout (good for REPL development)
   2. :throw - Throws prettified exceptions (better for testing and CI)
   
   Example usage:
   ;; For REPL development - just print errors
   (start! {:report :print})
   
   ;; For testing/CI - throw on validation errors  
   (start! {:report :throw})
   
   ;; With custom configuration
   (start! {:report :throw
            :width 120
            :print-length 100
            :print-level 5
            :print-meta true})"
  (:require [malli.dev :as dev]
            [malli.dev.pretty :as pretty]
            [malli.instrument :as mi]
            [malli.registry :as mr]
            [malli.experimental.lite :as mel]
            [potatoclient.logging :as logging]))

(defn start!
  "Start development instrumentation with pretty error reporting.
   
   Features:
   - Automatic instrumentation of all functions with :malli/schema metadata
   - Pretty error messages with detailed context
   - Automatic re-instrumentation when schemas change
   - CLJ-Kondo type config generation for static checking
   
   Options:
   - :report - Reporter function (defaults to pretty thrower)
             :print - prints errors to stdout (default for REPL)
             :throw - throws prettified exceptions
   - :width - Max width for pretty printing (default: 80)
   - :print-length - Max items to print in collections (default: 30)
   - :print-level - Max nesting level to print (default: 2)
   - :print-meta - Whether to print metadata (default: false)"
  ([] (start! {}))
  ([{:keys [report width print-length print-level print-meta]
     :or {report :throw
          width 80
          print-length 30
          print-level 2
          print-meta false}}]
   (logging/log-info {:msg "Starting Malli development instrumentation..."})

   ;; Ensure registry is set up with all schemas first
   (require '[potatoclient.init])
   ((resolve 'potatoclient.init/ensure-registry!))
   
   ;; Load ui-specs registry  
   (require '[potatoclient.ui-specs])

   ;; Load all namespaces that have Malli schemas
   (require '[potatoclient.cmd.builder]
            '[potatoclient.cmd.cam-day-glass-heater]
            '[potatoclient.cmd.compass]
            '[potatoclient.cmd.core]
            '[potatoclient.cmd.cv]
            '[potatoclient.cmd.day-camera]
            '[potatoclient.cmd.gps]
            '[potatoclient.cmd.heat-camera]
            '[potatoclient.cmd.lrf-alignment]
            '[potatoclient.cmd.lrf]
            '[potatoclient.cmd.osd]
            '[potatoclient.cmd.root]
            '[potatoclient.cmd.rotary]
            '[potatoclient.cmd.system]
            '[potatoclient.cmd.validation]
            '[potatoclient.config]
            '[potatoclient.dev]
            '[potatoclient.i18n]
            '[potatoclient.init]
            '[potatoclient.ipc.core]
            '[potatoclient.ipc.handlers]
            '[potatoclient.ipc.transit]
            '[potatoclient.logging]
            '[potatoclient.main]
            '[potatoclient.proto.deserialize]
            '[potatoclient.proto.serialize]
            '[potatoclient.reports]
            '[potatoclient.runtime]
            '[potatoclient.state]
            '[potatoclient.streams.config]
            '[potatoclient.streams.coordinator]
            '[potatoclient.streams.core]
            '[potatoclient.streams.events]
            '[potatoclient.streams.process]
            '[potatoclient.streams.state]
            '[potatoclient.theme]
            '[potatoclient.ui.control-panel]
            '[potatoclient.ui.debounce]
            '[potatoclient.ui.frames.connection.core]
            '[potatoclient.ui.frames.initial.core]
            '[potatoclient.ui.help.about]
            '[potatoclient.ui.help.menu]
            '[potatoclient.ui.log-viewer]
            '[potatoclient.ui.main-frame]
            '[potatoclient.ui.menu-bar]
            '[potatoclient.ui.status-bar.core]
            '[potatoclient.ui.status-bar.helpers]
            '[potatoclient.ui.status-bar.messages]
            '[potatoclient.ui.status-bar.validation]
            '[potatoclient.ui.tabs]
            '[potatoclient.ui.tabs-helpers]
            '[potatoclient.ui.tabs-windows]
            '[potatoclient.ui.utils]
            '[potatoclient.url-parser])

   ;; Collect all function schemas from loaded namespaces
   ;; This reads :malli/schema metadata from all public vars
   (logging/log-info {:msg "Collecting function schemas from all namespaces..."})
   (let [collected (mi/collect! {:ns (all-ns)})]
     (logging/log-info {:msg (str "Collected schemas from " (count collected) " functions")}))

   ;; Configure pretty printer options
   (let [printer-opts (pretty/-printer {:width width
                                        :print-length print-length
                                        :print-level print-level
                                        :print-meta print-meta})
         reporter (case report
                    :throw (pretty/thrower printer-opts)
                    :print (pretty/reporter printer-opts)
                    ;; Allow custom reporter functions
                    report)]

     ;; Start dev mode with configured error reporting
     ;; This instruments functions and watches for schema changes
     ;; Also generates clj-kondo configs automatically!
     (dev/start! {:report reporter})
     
     ;; Log that instrumentation is active
     (logging/log-info {:msg "Malli dev mode started - watching for schema changes"}))

   (logging/log-info {:msg (str "Malli instrumentation active with "
                                (if (= report :throw) "exception throwing" "error printing"))})
   (logging/log-info {:msg "CLJ-Kondo configs generated in .clj-kondo/configs/malli"})))

(defn stop!
  "Stop development instrumentation and remove all runtime checks."
  []
  (logging/log-info {:msg "Stopping Malli development instrumentation..."})
  (dev/stop!)
  (logging/log-info {:msg "Malli instrumentation stopped"}))

(defn check-all
  "Check all registered function schemas against their implementations.
   Returns a map of function -> validation errors for any mismatches."
  []
  (logging/log-info {:msg "Checking all function schemas..."})
  (let [results (mi/check)]
    (if (empty? results)
      (logging/log-info {:msg "All function schemas check out!"})
      (logging/log-warn {:msg (str "Found " (count results) " function schema violations")
                         :violations results}))
    results))

(defn instrument-ns!
  "Instrument all functions in specific namespace(s).
   Useful for targeted instrumentation."
  [& namespaces]
  (doseq [ns-sym namespaces]
    (logging/log-info {:msg (str "Instrumenting namespace: " ns-sym)})
    (mi/collect! {:ns [ns-sym]})
    (mi/instrument! {:filters [(mi/-filter-ns ns-sym)]})))