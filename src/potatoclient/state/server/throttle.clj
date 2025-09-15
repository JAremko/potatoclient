(ns potatoclient.state.server.throttle
  "State update throttling to limit update frequency.
   Ensures state updates don't overwhelm the UI."
  (:require [malli.core :as m])
  (:import [java.util.concurrent Executors ScheduledExecutorService TimeUnit]))

(defn create-throttler
  "Create a throttler that limits function execution to specified interval.
   
   Options:
   - :interval-ms - Minimum milliseconds between executions (default 100)
   - :on-drop - Called with dropped value when throttling (optional)
   
   Returns a throttler map with :submit and :shutdown functions."
  [{:keys [interval-ms on-drop]
    :or {interval-ms 100}}]

  (let [executor (Executors/newSingleThreadScheduledExecutor)
        pending (atom nil)
        last-execution (atom 0)
        scheduled-task (atom nil)]

    {:submit
     (fn [f value]
       (let [now (System/currentTimeMillis)
             time-since-last (- now @last-execution)]

         (if (>= time-since-last interval-ms)
           ;; Can execute immediately
           (do
             (reset! last-execution now)
             (reset! pending nil)
             (f value))

           ;; Need to throttle
           (do
             ;; Store the latest value, dropping any previous pending
             (when-let [dropped @pending]
               (when on-drop (on-drop dropped)))
             (reset! pending value)

             ;; Schedule execution if not already scheduled
             (when-not @scheduled-task
               (let [delay (- interval-ms time-since-last)
                     task (.schedule executor
                                     ^Runnable
                                     (fn []
                                       (when-let [v @pending]
                                         (reset! last-execution (System/currentTimeMillis))
                                         (reset! pending nil)
                                         (reset! scheduled-task nil)
                                         (f v)))
                                     delay
                                     TimeUnit/MILLISECONDS)]
                 (reset! scheduled-task task)))))))

     :shutdown
     (fn []
       (.shutdown executor)
       (try
         (when-not (.awaitTermination executor 1 TimeUnit/SECONDS)
           (.shutdownNow executor))
         (catch InterruptedException _
           (.shutdownNow executor))))}))

(m/=> create-throttler [:=> [:cat [:map {:closed false} [:interval-ms {:optional true} :pos-int] [:on-drop {:optional true} fn?]]] [:map [:submit fn?] [:shutdown fn?]]])

(defn shutdown-throttler
  "Shutdown a throttler, canceling any pending executions."
  [throttler]
  (when-let [shutdown (:shutdown throttler)]
    (shutdown)))

(m/=> shutdown-throttler [:=> [:cat [:map [:shutdown fn?]]] :nil])