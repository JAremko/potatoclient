(ns potatoclient.state-test
  "Tests for state management, particularly seesaw binding cleanup."
  (:require [clojure.test :refer [deftest is testing]]
            [potatoclient.state :as state]
            [seesaw.bind :as bind]))

(deftest test-seesaw-binding-cleanup
  (testing "Seesaw bindings are detected and removed correctly"
    ;; Reset state to known value
    (state/reset-state!)

    ;; Get initial watcher count
    (let [initial-watchers (.getWatches state/app-state)
          initial-count (count initial-watchers)]

      ;; Create a test atom to track binding calls
      (let [call-count (atom 0)
            test-value (atom nil)]

        ;; Create a seesaw binding that increments counter when triggered
        (bind/bind
          state/app-state
          (bind/transform :connection)
          (bind/b-do [conn]
                     (swap! call-count inc)
                     (reset! test-value conn)))

        ;; Verify watcher was added with expected naming pattern
        (let [new-watchers (.getWatches state/app-state)
              new-count (count new-watchers)]
          (is (> new-count initial-count)
              "Watcher count should increase after binding")

          ;; Check that a bindable-atom-watcher was added
          (let [watcher-keys (keys new-watchers)
                seesaw-watchers (filter #(and (keyword? %)
                                              (when-let [name-str (name %)]
                                                (clojure.string/starts-with?
                                                  name-str "bindable-atom-watcher")))
                                        watcher-keys)]
            (is (seq seesaw-watchers)
                "Should find at least one bindable-atom-watcher key")))

        ;; Trigger the binding by changing state
        (state/set-connection-url! "test-url-1")
        (Thread/sleep 10) ; Give binding time to trigger

        ;; Verify binding was triggered
        (is (= 1 @call-count)
            "Binding should have been called once")
        (is (= {:url "test-url-1" :connected? false :latency-ms nil :reconnect-count 0}
               @test-value)
            "Binding should have received the connection state")

        ;; Clean up seesaw bindings
        (state/cleanup-seesaw-bindings!)

        ;; Verify watchers were removed
        (let [cleaned-watchers (.getWatches state/app-state)
              cleaned-count (count cleaned-watchers)
              seesaw-watchers (filter #(and (keyword? %)
                                            (when-let [name-str (name %)]
                                              (clojure.string/starts-with?
                                                name-str "bindable-atom-watcher")))
                                      (keys cleaned-watchers))]
          (is (= cleaned-count initial-count)
              "Watcher count should return to initial after cleanup")
          (is (empty? seesaw-watchers)
              "No seesaw watchers should remain after cleanup"))

        ;; Verify binding no longer triggers
        (state/set-connection-url! "test-url-2")
        (Thread/sleep 10) ; Give time to ensure no trigger

        (is (= 1 @call-count)
            "Binding should NOT have been called again after cleanup")
        (is (= {:url "test-url-1" :connected? false :latency-ms nil :reconnect-count 0}
               @test-value)
            "Test value should remain unchanged after cleanup")))))

(deftest test-multiple-bindings-cleanup
  (testing "Multiple seesaw bindings are all removed"
    (state/reset-state!)

    (let [initial-count (count (.getWatches state/app-state))
          counter1 (atom 0)
          counter2 (atom 0)
          counter3 (atom 0)]

      ;; Create multiple bindings
      (bind/bind state/app-state
                 (bind/transform #(get-in % [:ui :theme]))
                 (bind/b-do [_] (swap! counter1 inc)))

      (bind/bind state/app-state
                 (bind/transform #(get-in % [:ui :locale]))
                 (bind/b-do [_] (swap! counter2 inc)))

      (bind/bind state/app-state
                 (bind/transform #(get-in % [:connection :connected?]))
                 (bind/b-do [_] (swap! counter3 inc)))

      ;; Verify watchers were added
      (let [with-bindings-count (count (.getWatches state/app-state))]
        (is (= (+ initial-count 3) with-bindings-count)
            "Should have added 3 watchers"))

      ;; Trigger state changes
      (state/set-theme! :sol-light)
      (state/set-locale! :ukrainian)
      (state/set-connected! true)
      (Thread/sleep 50)

      ;; Verify all bindings triggered (may trigger multiple times during state updates)
      (is (pos? @counter1) "Theme binding should trigger")
      (is (pos? @counter2) "Locale binding should trigger")
      (is (pos? @counter3) "Connection binding should trigger")

      ;; Record counts before cleanup
      (let [count1-before @counter1
            count2-before @counter2
            count3-before @counter3]

        ;; Clean up all bindings
        (state/cleanup-seesaw-bindings!)

        ;; Verify all seesaw watchers removed (but other watchers may remain)
        (let [final-count (count (.getWatches state/app-state))
              seesaw-watchers (filter #(and (keyword? %)
                                           (when-let [name-str (name %)]
                                             (clojure.string/starts-with?
                                               name-str "bindable-atom-watcher")))
                                     (keys (.getWatches state/app-state)))]
          (is (<= final-count initial-count)
              "Should have no more watchers than initially")
          (is (empty? seesaw-watchers)
              "All seesaw watchers should be removed"))

        ;; Verify bindings no longer trigger
        (state/set-theme! :sol-dark)
        (state/set-locale! :english)
        (state/set-connected! false)
        (Thread/sleep 50)

        (is (= count1-before @counter1) "Theme binding should NOT trigger after cleanup")
        (is (= count2-before @counter2) "Locale binding should NOT trigger after cleanup")
        (is (= count3-before @counter3) "Connection binding should NOT trigger after cleanup")))))

(deftest test-cleanup-preserves-non-seesaw-watchers
  (testing "cleanup-seesaw-bindings! only removes seesaw watchers"
    (state/reset-state!)

    (let [custom-watcher-called (atom 0)]

      ;; Add a custom watcher that should NOT be removed
      (state/add-watch-handler :my-custom-watcher
                               (fn [_ _ _ _]
                                 (swap! custom-watcher-called inc)))

      ;; Add a seesaw binding
      (let [seesaw-called (atom 0)]
        (bind/bind state/app-state
                   (bind/transform :connection)
                   (bind/b-do [_] (swap! seesaw-called inc)))

        ;; Trigger both watchers
        (state/set-connection-url! "test1")
        (Thread/sleep 10)

        (is (= 1 @custom-watcher-called) "Custom watcher should trigger")
        (is (= 1 @seesaw-called) "Seesaw binding should trigger")

        ;; Clean up seesaw bindings
        (state/cleanup-seesaw-bindings!)

        ;; Verify custom watcher still works
        (state/set-connection-url! "test2")
        (Thread/sleep 10)

        (is (= 2 @custom-watcher-called)
            "Custom watcher should still trigger after cleanup")
        (is (= 1 @seesaw-called)
            "Seesaw binding should NOT trigger after cleanup"))

      ;; Clean up custom watcher
      (state/remove-watch-handler :my-custom-watcher))))

(deftest test-watcher-naming-pattern-detection
  (testing "Verify seesaw creates expected watcher key patterns"
    (state/reset-state!)

    ;; Create a binding and examine the watcher key
    (bind/bind state/app-state
               (bind/transform identity)
               (bind/b-do [_] nil))

    (let [watcher-keys (keys (.getWatches state/app-state))
          seesaw-keys (filter #(and (keyword? %)
                                    (when-let [n (name %)]
                                      (or (clojure.string/starts-with? n "bindable-atom-watcher")
                                          (clojure.string/starts-with? n "bindable-agent-watcher")
                                          (clojure.string/starts-with? n "bindable-ref-watcher"))))
                              watcher-keys)]

      (is (seq seesaw-keys)
          "Should find seesaw watcher with expected naming pattern")

      ;; Verify the key matches expected pattern (has gensym suffix)
      (when-let [key (first seesaw-keys)]
        (let [key-str (name key)]
          (is (re-matches #"bindable-atom-watcher\d+" key-str)
              (str "Watcher key should match pattern bindable-atom-watcherXXXX, got: " key-str))))

      ;; Clean up
      (state/cleanup-seesaw-bindings!))))