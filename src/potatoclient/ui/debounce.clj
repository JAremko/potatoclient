(ns potatoclient.ui.debounce
  "Debouncing utilities for preventing excessive updates from rapid state changes.
  
  Provides tools to create debounced atoms that delay updates until a specified
  quiet period has passed, improving performance for UI bindings and expensive
  operations."
  (:import [java.util.concurrent ScheduledExecutorService Executors TimeUnit]))

(def ^:private default-debounce-delay-ms
  "Default delay in milliseconds for debounced updates."
  300)

(def ^:private thread-pool-size
  "Number of threads in the scheduled executor pool."
  2)

(def ^:private executor
  "Shared scheduled executor for all debounce timers."
  (Executors/newScheduledThreadPool thread-pool-size))

(defn debounce-atom
  "Creates a debounced view of a source atom.
  
  The debounced atom will update only after the specified delay (in ms)
  has passed without any new changes to the source atom.
  
  Parameters:
    source-atom - The atom to debounce
    delay-ms - Delay in milliseconds (default: 300)
  
  Returns a read-only atom that updates after the delay.
  
  Example:
    (def config (atom {:search \"\"}))
    (def config-debounced (debounce-atom config 500))
    
    ;; Rapid updates to config
    (reset! config {:search \"h\"})
    (reset! config {:search \"he\"})
    (reset! config {:search \"hel\"})
    (reset! config {:search \"hello\"})
    
    ;; config-debounced will only update once with {:search \"hello\"}
    ;; after 500ms of no changes"
  {:malli/schema [:=> [:cat [:fn #(instance? clojure.lang.IDeref %)] [:? :nat-int]]
                  [:fn #(instance? clojure.lang.IDeref %)]]}
  ([source-atom] (debounce-atom source-atom default-debounce-delay-ms))
  ([source-atom delay-ms]
   (let [debounced (atom @source-atom)
         scheduled-task (atom nil)]

     ;; Watch source atom for changes
     (add-watch source-atom ::debounce
                (fn [_ _ _ new-value]
                  ;; Cancel any pending update
                  (when-let [task @scheduled-task]
                    (.cancel task false))

                  ;; Schedule new update
                  (let [task (.schedule executor
                                        ^Runnable (fn []
                                                    (reset! debounced new-value)
                                                    (reset! scheduled-task nil))
                                        delay-ms
                                        TimeUnit/MILLISECONDS)]
                    (reset! scheduled-task task))))

     ;; Return the debounced atom
     debounced)))

(defn debounce-transform
  "Creates a debounced transformation binding.
  
  Useful for expensive transformations that shouldn't run on every change.
  
  Parameters:
    source - The source bindable
    delay-ms - Delay in milliseconds
    transform-fn - Function to transform the value
  
  Returns a bindable that updates after the delay.
  
  Example:
    (require '[seesaw.bind :as bind])
    
    (bind/bind 
      slider-model
      (debounce-transform 100 #(expensive-calculation %))
      (bind/property visualization :data))"
  {:malli/schema [:=> [:cat :any :nat-int [:=> [:cat :any] :any]] :any]}
  [source delay-ms transform-fn]
  (let [source-atom (atom nil)
        debounced (debounce-atom source-atom delay-ms)]
    ;; Return a binding-compatible function
    (fn [handler]
      ;; Set up source watching
      (let [bind-fn (requiring-resolve 'seesaw.bind/bind)]
        (bind-fn source
                 (fn [v]
                   (reset! source-atom v)
                   v)))
      ;; Set up debounced watching
      (add-watch debounced ::transform
                 (fn [_ _ _ new-val]
                   (handler (transform-fn new-val))))
      ;; Return unsubscribe function
      (fn []
        (remove-watch debounced ::transform)))))

(defn cleanup-executor!
  "Shuts down the debounce executor service.
  Should be called on application shutdown."
  {:malli/schema [:=> [:cat] :nil]}
  []
  (.shutdown executor)
  nil)