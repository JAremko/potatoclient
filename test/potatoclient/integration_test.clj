(ns potatoclient.integration-test
  "Integration tests for state, status bar, and UI components."
  (:require [clojure.test :refer [deftest is testing]]
            [potatoclient.state :as state]
            [potatoclient.ui.status-bar.core :as status-bar-core]
            [potatoclient.ui.status-bar.messages :as status-bar]
            [potatoclient.ui.status-bar.helpers :as helpers]
            [potatoclient.ui.tabs :as tabs]
            [potatoclient.i18n :as i18n])
  (:import (javax.swing JPanel JTabbedPane JFrame)))

(deftest test-complete-workflow
  (testing "Complete workflow: state changes trigger status updates"
    ;; Initialize
    (state/reset-state!)
    (i18n/load-translations!)

    ;; Test theme change workflow
    (state/set-theme! :sol-light)
    (status-bar/set-theme-changed! :sol-light)
    (is (= :sol-light (state/get-theme)) "Theme should be updated")
    (is (= :info (get-in @state/app-state [:ui :status :type])) "Status should be info")
    (is (re-find #"Sol Light" (get-in @state/app-state [:ui :status :message]))
        "Status should mention theme name")

    ;; Test connection workflow
    (status-bar/set-connecting! "example.com")
    (state/set-connection-url! "example.com")
    (is (= "example.com" (state/get-connection-url)) "URL should be set")
    (is (re-find #"example\.com" (get-in @state/app-state [:ui :status :message]))
        "Status should show connecting")

    (state/set-connected! true)
    (status-bar/set-connected! "example.com")
    (is (state/connected?) "Should be connected")
    (is (= :info (get-in @state/app-state [:ui :status :type])) "Connected status should be info")

    ;; Test error workflow
    (let [error-fn (fn [] (throw (Exception. "Connection lost")))]
      (try
        (status-bar/with-error-handler error-fn)
        (catch Exception _))

      (is (= :error (get-in @state/app-state [:ui :status :type]))
          "Error should set error status")
      (is (some? @helpers/last-error-atom) "Error should be stored")
      (is (= "Connection lost" (:message @helpers/last-error-atom))
          "Error message should match"))

    ;; Test state validation remains valid through workflow
    (state/reset-state!)
    (is (state/valid-state?) "State should be valid after reset")))

(deftest test-ui-component-creation
  (testing "UI components can be created and are valid"
    (i18n/load-translations!)

    ;; Test status bar creation
    (let [status-bar (status-bar-core/create)]
      (is (instance? JPanel status-bar) "Status bar should be a JPanel"))

    ;; Test tabs creation
    (let [parent-frame (JFrame.)
          tabs (tabs/create-default-tabs parent-frame)]
      (is (instance? JTabbedPane tabs) "Tabs should be a JTabbedPane")
      (is (> (.getTabCount tabs) 0) "Should have at least one tab"))))

(deftest test-multilingual-workflow
  (testing "Status messages work in multiple languages"
    (state/reset-state!)
    (i18n/load-translations!)

    ;; Test English
    (state/set-locale! :english)
    (status-bar/set-config-saved!)
    (let [en-msg (get-in @state/app-state [:ui :status :message])]
      (is (= "Configuration saved" en-msg) "English message"))

    ;; Test Ukrainian
    (state/set-locale! :ukrainian)
    (status-bar/set-config-saved!)
    (let [uk-msg (get-in @state/app-state [:ui :status :message])]
      (is (= "Конфігурацію збережено" uk-msg) "Ukrainian message"))

    ;; Reset
    (state/set-locale! :english)))

(deftest test-state-validation-integration
  (testing "State validation works with real operations"
    (state/reset-state!)

    ;; Valid operations should maintain valid state
    (state/set-theme! :sol-dark)
    (is (state/valid-state?) "After theme change")

    (state/set-connection-url! "test.com")
    (is (state/valid-state?) "After URL set")

    (state/set-connected! true)
    (is (state/valid-state?) "After connected")

    (state/update-process-status! :heat-video 12345 :running)
    (is (state/valid-state?) "After process update")

    ;; Safe-swap should warn but continue in dev
    (let [output (with-out-str
                   (state/safe-swap! assoc-in [:ui :theme] :invalid))]
      (when-not (potatoclient.runtime/release-build?)
        (is (re-find #"WARNING" output) "Should warn about invalid state")))

    ;; Direct invalid change should be detected
    (swap! state/app-state assoc-in [:server-state :system :battery-level] 200)
    (is (not (state/valid-state?)) "Invalid battery level should fail validation")))

(deftest test-error-recovery
  (testing "System can recover from errors"
    (state/reset-state!)
    (i18n/load-translations!)

    ;; Simulate error
    (status-bar/set-error! "System error")
    (swap! helpers/last-error-atom assoc
           :message "Test error"
           :stack-trace "Stack trace here")

    (is (= :error (get-in @state/app-state [:ui :status :type]))
        "Should be in error state")

    ;; Recovery
    (status-bar/clear!)
    (reset! helpers/last-error-atom nil)

    (is (= :info (get-in @state/app-state [:ui :status :type]))
        "Should recover to info state")
    (is (= "" (get-in @state/app-state [:ui :status :message]))
        "Message should be cleared")
    (is (nil? @helpers/last-error-atom)
        "Error atom should be cleared")))