(ns potatoclient.dev-instrumentation
  "Development-time instrumentation using Malli.
   
   This namespace provides runtime validation of function arguments and
   return values during development, with automatic re-instrumentation
   when schemas change."
  (:require [malli.dev :as dev]
            [malli.dev.pretty :as pretty]
            [malli.instrument :as mi]
            [potatoclient.logging :as logging]))

(defn start!
  "Start development instrumentation with pretty error reporting.
   
   Features:
   - Automatic instrumentation of all functions with :malli/schema metadata
   - Pretty error messages with detailed context
   - Automatic re-instrumentation when schemas change
   - CLJ-Kondo type config generation for static checking"
  []
  (logging/log-info {:msg "Starting Malli development instrumentation..."})
  
  ;; Ensure ui-specs registry is loaded first
  (require '[potatoclient.ui-specs])
  
  ;; Load common namespaces that have Malli schemas
  (require '[potatoclient.i18n])
  
  ;; Collect all function schemas from loaded namespaces
  (mi/collect! {:ns (all-ns)})
  
  ;; Start dev mode with pretty error reporting
  ;; This also generates clj-kondo configs automatically!
  (dev/start! {:report (pretty/reporter
                         (pretty/-printer {:width 80
                                           :print-length 30
                                           :print-level 2}))})
  
  (logging/log-info {:msg "Malli instrumentation active - functions are being validated"})
  (logging/log-info {:msg "CLJ-Kondo configs generated in .clj-kondo/configs/malli"}))

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