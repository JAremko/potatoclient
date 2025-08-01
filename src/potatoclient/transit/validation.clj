(ns potatoclient.transit.validation
  "Transit message validation using Malli schemas"
  (:require [com.fulcrologic.guardrails.malli.core :refer [>defn >defn- =>]]
            [potatoclient.specs :as specs]
            [malli.core :as m]
            [potatoclient.logging :as logging]))

(>defn- validate-with-schema
  "Validate a value against a schema and return [valid? errors]"
  [value schema]
  [any? any? => [:tuple boolean? [:maybe map?]]]
  (if (m/validate schema value)
    [true nil]
    [false (m/explain schema value)]))

(>defn- validate-event-message
  "Validate an event message based on its event type"
  [message]
  [map? => [:tuple boolean? [:maybe map?]]]
  (let [event-type (get-in message [:payload :type])]
    (case event-type
      "navigation" (validate-with-schema 
                     (:payload message) 
                     ::specs/navigation-event-payload)
      "gesture" (validate-with-schema 
                  (:payload message) 
                  ::specs/gesture-event-payload)
      "window" (validate-with-schema 
                 (:payload message) 
                 ::specs/window-event-payload)
      ;; For other event types, just validate basic structure
      (validate-with-schema message ::specs/event-message))))

(>defn validate-message
  "Validate a Transit message against its schema"
  [message]
  [map? => [:tuple boolean? [:maybe map?]]]
  (let [msg-type (:msg-type message)]
    (case msg-type
      "command" (validate-with-schema message ::specs/command-message)
      "response" (validate-with-schema message ::specs/response-message)
      "request" (validate-with-schema message 
                                     [:map
                                      [:msg-type [:= "request"]]
                                      [:msg-id uuid?]
                                      [:timestamp pos-int?]
                                      [:payload ::specs/request-payload]])
      "log" (validate-with-schema message 
                                 [:map
                                  [:msg-type [:= "log"]]
                                  [:msg-id uuid?]
                                  [:timestamp pos-int?]
                                  [:payload ::specs/log-payload]])
      "error" (validate-with-schema message 
                                   [:map
                                    [:msg-type [:= "error"]]
                                    [:msg-id uuid?]
                                    [:timestamp pos-int?]
                                    [:payload ::specs/error-payload]])
      "status" (validate-with-schema message 
                                    [:map
                                     [:msg-type [:= "status"]]
                                     [:msg-id uuid?]
                                     [:timestamp pos-int?]
                                     [:payload ::specs/status-payload]])
      "metric" (validate-with-schema message 
                                    [:map
                                     [:msg-type [:= "metric"]]
                                     [:msg-id uuid?]
                                     [:timestamp pos-int?]
                                     [:payload ::specs/metric-payload]])
      "event" (validate-event-message message)
      [false {:error "Unknown message type" :type msg-type}])))

(>defn log-validation-error
  "Log validation errors for debugging"
  [message errors]
  [map? map? => nil?]
  (logging/log-warn 
    {:msg "Transit message validation failed"
     :msg-type (:msg-type message)
     :msg-id (:msg-id message) 
     :errors errors})
  nil)