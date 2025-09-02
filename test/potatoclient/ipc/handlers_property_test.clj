(ns potatoclient.ipc.handlers-property-test
  "Property-based tests for IPC message handlers."
  (:require
    [clojure.test :refer [deftest is testing]]
    [clojure.test.check.clojure-test :refer [defspec]]
    [clojure.test.check.properties :as prop]
    [clojure.test.check.generators :as gen]
    [potatoclient.ipc.handlers :as handlers]
    [potatoclient.test-harness :as harness])
  (:import
    (java.util.concurrent LinkedBlockingQueue TimeUnit CountDownLatch)
    (java.util.concurrent.atomic AtomicInteger)))

;; Ensure test harness is initialized
(when-not harness/initialized?
  (throw (ex-info "Test harness failed to initialize!"
                  {:initialized? harness/initialized?})))

;; ============================================================================
;; Property: Handler processes all messages
;; ============================================================================

(defspec handler-processes-all-messages-property
  50
  (prop/for-all [messages (gen/vector (gen/hash-map gen/keyword gen/any-printable) 1 100)]
    (let [processed (atom [])
          running? (atom true)
          handler (handlers/create-handler
                    {:name "test-handler"
                     :handler-fn (fn [msg] (swap! processed conj msg))
                     :running? running?})
          queue (LinkedBlockingQueue.)]
      
      ;; Add all messages to queue
      (doseq [msg messages]
        (.offer queue msg))
      
      ;; Process messages
      (let [thread (Thread.
                     (fn []
                       (while (and @running? (not (.isEmpty queue)))
                         (when-let [msg (.poll queue 10 TimeUnit/MILLISECONDS)]
                           (handlers/handle-message handler msg)))))]
        (.start thread)
        (Thread/sleep 100) ; Give time to process
        (reset! running? false)
        (.join thread 1000))
      
      ;; All messages should be processed
      (= (count messages) (count @processed)))))

;; ============================================================================
;; Property: Filtering handler only processes matching messages
;; ============================================================================

(defspec filtering-handler-property
  50
  (prop/for-all [messages (gen/vector
                            (gen/hash-map
                              :type (gen/elements [:event :command :response])
                              :id gen/nat)
                            10 50)]
    (let [processed (atom [])
          running? (atom true)
          base-handler (handlers/create-handler
                         {:name "base"
                          :handler-fn (fn [msg] (swap! processed conj msg))
                          :running? running?})
          ;; Only process :event messages
          filter-handler (handlers/create-filtering-handler
                           base-handler
                           (fn [msg] (= :event (:type msg)))
                           running?)]
      
      ;; Process all messages
      (doseq [msg messages]
        (handlers/handle-message filter-handler msg))
      
      ;; Only event messages should be processed
      (let [expected-events (filter #(= :event (:type %)) messages)]
        (= @processed expected-events)))))

;; ============================================================================
;; Property: Transforming handler applies transformation
;; ============================================================================

(defspec transforming-handler-property
  50
  (prop/for-all [numbers (gen/vector gen/nat 10 50)]
    (let [processed (atom [])
          running? (atom true)
          base-handler (handlers/create-handler
                         {:name "base"
                          :handler-fn (fn [msg] (swap! processed conj msg))
                          :running? running?})
          ;; Double all numbers
          transform-handler (handlers/create-transforming-handler
                              base-handler
                              (fn [n] (* 2 n))
                              running?)]
      
      ;; Process all numbers
      (doseq [n numbers]
        (handlers/handle-message transform-handler n))
      
      ;; All numbers should be doubled
      (= @processed (map #(* 2 %) numbers)))))

;; ============================================================================
;; Property: Composite handler delegates to all handlers
;; ============================================================================

(defspec composite-handler-property
  50
  (prop/for-all [messages (gen/vector gen/any-printable 5 20)]
    (let [results1 (atom [])
          results2 (atom [])
          results3 (atom [])
          running? (atom true)
          
          handler1 (handlers/create-handler
                     {:name "h1"
                      :handler-fn (fn [msg] (swap! results1 conj msg))
                      :running? running?})
          handler2 (handlers/create-handler
                     {:name "h2"
                      :handler-fn (fn [msg] (swap! results2 conj msg))
                      :running? running?})
          handler3 (handlers/create-handler
                     {:name "h3"
                      :handler-fn (fn [msg] (swap! results3 conj msg))
                      :running? running?})
          
          composite (handlers/create-composite-handler
                      [handler1 handler2 handler3]
                      running?)]
      
      ;; Process all messages
      (doseq [msg messages]
        (handlers/handle-message composite msg))
      
      ;; All handlers should receive all messages
      (and (= @results1 messages)
           (= @results2 messages)
           (= @results3 messages)))))

;; ============================================================================
;; Property: Error handling doesn't stop processing
;; ============================================================================

(defspec error-handling-property
  50
  (prop/for-all [messages (gen/vector gen/nat 10 30)]
    (let [processed (atom [])
          errors (atom [])
          running? (atom true)
          ;; Handler that throws on even numbers
          handler (handlers/create-handler
                    {:name "error-test"
                     :handler-fn (fn [msg]
                                   (if (even? msg)
                                     (throw (ex-info "Even number!" {:n msg}))
                                     (swap! processed conj msg)))
                     :error-fn (fn [e msg] (swap! errors conj msg))
                     :running? running?})]
      
      ;; Process all messages, catching errors
      (doseq [msg messages]
        (try
          (handlers/handle-message handler msg)
          (catch Exception e
            (handlers/on-error handler e msg))))
      
      ;; Odd numbers processed, even numbers errored
      (let [odds (filter odd? messages)
            evens (filter even? messages)]
        (and (= (set @processed) (set odds))
             (= (set @errors) (set evens)))))))

;; ============================================================================
;; Property: Queue processing maintains order
;; ============================================================================

(defspec queue-processing-order-property
  50
  (prop/for-all [messages (gen/vector gen/nat 10 50)]
    (let [processed (atom [])
          running? (atom true)
          queue (LinkedBlockingQueue.)
          handler (handlers/create-handler
                    {:name "order-test"
                     :handler-fn (fn [msg] (swap! processed conj msg))
                     :running? running?})]
      
      ;; Add messages to queue
      (doseq [msg messages]
        (.offer queue msg))
      
      ;; Process queue
      (let [latch (CountDownLatch. 1)
            thread (Thread.
                     (fn []
                       (handlers/process-queue
                         {:queue queue
                          :handler handler
                          :poll-timeout-ms 10})
                       (.countDown latch)))]
        (.start thread)
        (Thread/sleep 50)
        (reset! running? false)
        (.await latch 1000 TimeUnit/MILLISECONDS))
      
      ;; Messages should be processed in order
      (= @processed messages))))

;; ============================================================================
;; Regular Tests for Edge Cases
;; ============================================================================

(deftest handler-lifecycle-test
  (testing "Handler lifecycle management"
    (let [running? (atom true)
          call-count (AtomicInteger. 0)
          handler (handlers/create-handler
                    {:name "lifecycle"
                     :handler-fn (fn [_] (.incrementAndGet call-count))
                     :running? running?})]
      
      (testing "Handler processes while running"
        (handlers/handle-message handler :msg1)
        (handlers/handle-message handler :msg2)
        (is (= 2 (.get call-count))))
      
      (testing "Handler stops when running? is false"
        (reset! running? false)
        (is (not (handlers/should-continue? handler)))))))

(deftest error-recovery-test
  (testing "Handlers recover from errors"
    (let [processed (atom [])
          errors (atom [])
          running? (atom true)
          handler (handlers/create-handler
                    {:name "recovery"
                     :handler-fn (fn [msg]
                                   (if (= msg :error)
                                     (throw (Exception. "Test error"))
                                     (swap! processed conj msg)))
                     :error-fn (fn [_ msg] (swap! errors conj msg))
                     :running? running?})]
      
      ;; Process mix of good and bad messages
      (doseq [msg [:good1 :error :good2 :error :good3]]
        (try
          (handlers/handle-message handler msg)
          (catch Exception e
            (handlers/on-error handler e msg))))
      
      (is (= @processed [:good1 :good2 :good3]))
      (is (= @errors [:error :error])))))