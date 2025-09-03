(ns potatoclient.ui.utils
  "Utility functions for UI operations, including debouncing and other helpers
  adapted from the ArcherBC2 example project."
  (:require
            [malli.core :as m] [seesaw.core :as seesaw]
            [seesaw.cursor :as cursor]
            [potatoclient.logging :as logging])
  (:import (java.awt Component)
           (javax.swing.text JTextComponent)))

(defn mk-debounced-transform
  "Creates a debounced transform function for use with seesaw.bind.
  
  This prevents rapid updates from overwhelming the UI by only propagating
  changes when the value actually differs from the last propagated value.
  
  Usage:
    (bind/bind *state
               (bind/some (mk-debounced-transform #(get % :value)))
               (bind/value widget))
  
  The transform function `xf` is called with the state and should return
  the value to be propagated. The value is only propagated if it differs
  from the last propagated value (using Clojure's equality semantics).
  
  This is particularly useful for:
  - Slider movements that generate many intermediate values
  - Text field updates during typing
  - Any binding that might fire rapidly with intermediate values
  
  Example with a slider:
    (def *state (atom {:brightness 50}))
    
    (def slider (seesaw/slider :min 0 :max 100 :value 50))
    
    ;; Without debouncing, this would update on every pixel of movement
    (bind/bind *state
               (bind/some (mk-debounced-transform #(:brightness %)))
               (bind/value slider))
  
  Example with complex state extraction:
    (bind/bind *state
               (bind/some (mk-debounced-transform 
                           #(get-in % [:profile :settings :volume])))
               (bind/value volume-slider))"
  [xf]
  (let [*last-val (atom nil)]
    (fn [state]
      (let [last-val @*last-val
            new-val (xf state)]
        (when (or (nil? last-val)
                  (not= new-val last-val))
          (reset! *last-val new-val)
          new-val))))) 
 (m/=> mk-debounced-transform [:=> [:cat :ifn] :ifn])

(defn debounce
  "Creates a debounced version of a function that delays execution until
  a specified quiet period has elapsed.
  
  Parameters:
  - f: The function to debounce
  - wait-ms: The number of milliseconds to wait before calling f
  
  Returns a new function that when called, will delay invoking f until
  after wait-ms milliseconds have elapsed since the last time it was invoked.
  
  Usage:
    (def save-settings! 
      (debounce (fn [settings] 
                  (spit \"settings.edn\" settings))
                1000))
    
    ;; Multiple rapid calls...
    (save-settings! {:volume 20})
    (save-settings! {:volume 30})  
    (save-settings! {:volume 40})
    ;; Only the last call with {:volume 40} executes after 1 second
  
  This is useful for:
  - Auto-save functionality
  - Search-as-you-type features
  - Expensive computations triggered by user input
  - API calls that shouldn't be made too frequently" [f wait-ms]
  (let [timeout (atom nil)]
    (fn [& args]
      (when @timeout
        (future-cancel @timeout))
      (reset! timeout
              (future
                (try
                  (Thread/sleep (long wait-ms))
                  (apply f args)
                  (catch Exception e
                    (logging/log-error {:msg "Error in debounced function"
                                        :error e})))))))) 
 (m/=> debounce [:=> [:cat :ifn :pos-int] :ifn])

(defn throttle
  "Creates a throttled version of a function that limits execution to at most
  once per specified time period.
  
  Parameters:
  - f: The function to throttle
  - period-ms: The minimum number of milliseconds between invocations
  
  Returns a new function that when called repeatedly, will invoke f at most
  once per period-ms milliseconds, using the most recent arguments.
  
  Usage:
    (def update-preview!
      (throttle (fn [text]
                  (render-markdown-preview text))
                100))
    
    ;; During rapid typing, preview updates at most every 100ms
  
  This differs from debounce in that it guarantees regular execution
  during continuous calls, rather than waiting for a quiet period."
  [f period-ms]
  (let [last-call (atom 0)]
    (fn [& args]
      (let [now (System/currentTimeMillis)
            time-since-last (- now @last-call)]
        (when (>= time-since-last period-ms)
          (reset! last-call now)
          (apply f args)))))) 
 (m/=> throttle [:=> [:cat :ifn :pos-int] :ifn])

(defn batch-updates
  "Batches multiple UI updates into a single EDT invocation for better performance.
  
  Parameters:
  - updates: A sequence of zero-argument functions that perform UI updates
  
  Usage:
    (batch-updates
      [#(seesaw/config! label1 :text \"Updated\")
       #(seesaw/config! label2 :visible? false)  
       #(seesaw/config! button :enabled? true)])
  
  This is more efficient than multiple separate invoke-later calls." [updates]
  (seesaw/invoke-later
    (doseq [update-fn updates]
      (update-fn)))) 
 (m/=> batch-updates [:=> [:cat [:sequential :ifn]] :nil])

(defn with-busy-cursor
  "Executes a function while showing a busy cursor, then restores the original cursor.
  
  Parameters:
  - component: The component to set the cursor on (usually a frame or panel)
  - f: The function to execute
  
  Usage:
    (with-busy-cursor frame
      (fn []
        (Thread/sleep (long 2000))  ; Simulate long operation
        (process-data)))
  
  The cursor is guaranteed to be restored even if an exception occurs."
  [component f]
  (let [original-cursor (.getCursor component)
        busy-cursor (cursor/cursor :wait)]
    (try
      (seesaw.core/invoke-now
        (.setCursor component busy-cursor))
      (f)
      (finally
        (seesaw.core/invoke-now
          (.setCursor component original-cursor)))))) 
 (m/=> with-busy-cursor [:=> [:cat [:fn {:error/message "must be a Swing Component"} (partial instance? Component)] :ifn] :any])

(defn preserve-selection
  "Preserves the selection and scroll position of a text component during updates.
  
  Parameters:
  - text-component: A text component (text area, text field, etc.)
  - update-fn: A function that updates the text component
  
  Usage:
    (preserve-selection text-area
      (fn []
        (seesaw/text! text-area (process-text (seesaw/text text-area)))))"
  [text-component update-fn]
  (let [caret-pos (.getCaretPosition text-component)
        selection-start (.getSelectionStart text-component)
        selection-end (.getSelectionEnd text-component)]
    (update-fn)
    (when (pos? (count (seesaw.core/text text-component)))
      (.setCaretPosition text-component
                         (min caret-pos
                              (dec (count (seesaw.core/text text-component)))))
      (when (not= selection-start selection-end)
        (.setSelectionStart text-component selection-start)
        (.setSelectionEnd text-component selection-end))))) 
 (m/=> preserve-selection [:=> [:cat [:fn {:error/message "must be a text component"} (partial instance? JTextComponent)] :ifn] :nil])