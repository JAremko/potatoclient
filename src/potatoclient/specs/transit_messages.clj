(ns potatoclient.specs.transit-messages
  "Comprehensive Malli specifications for Transit messages.
  
  Validates message structure, subprocess contracts, and payload contents.
  Only active in development mode to avoid production overhead."
  (:require [malli.core :as m]
            [malli.error :as me]
            [malli.registry :as mr]
            [potatoclient.specs :as specs]
            [potatoclient.runtime :as runtime]
            [taoensso.telemere :as t]))

;; -----------------------------------------------------------------------------
;; Core Types
;; -----------------------------------------------------------------------------

(def message-type
  "Valid message types from MessageType enum"
  [:enum :command :response :request :log :error :status :metric :event
   :state-update :state-partial :stream-ready :stream-error :stream-closed])

(def subprocess-type
  "Types of subprocesses in the system"
  [:enum :command :state :video-heat :video-day :main])

(def stream-type
  "Video stream types"
  [:enum :heat :day])

;; -----------------------------------------------------------------------------
;; Message Envelope
;; -----------------------------------------------------------------------------

(def message-envelope
  "Base Transit message structure"
  [:map
   [:msg-type message-type]
   [:msg-id [:and string? [:re #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"]]]
   [:timestamp pos-int?]
   [:payload map?]])

;; -----------------------------------------------------------------------------
;; Event Messages
;; -----------------------------------------------------------------------------

(def event-type
  "Types of events"
  [:enum :window :navigation :gesture :frame :error])

(def window-event-type
  "Window event subtypes"
  [:enum :close :minimize :maximize :restore :resize :move :focus])

(def gesture-type
  "Gesture event subtypes"
  [:enum :tap :double-tap :pan-start :pan-move :pan-stop :swipe])

(def navigation-type
  "Navigation event subtypes"
  [:enum :mouse-move :mouse-click :mouse-drag :mouse-wheel])

;; Base event payload
(def event-payload-base
  [:map
   [:type event-type]
   [:timestamp {:optional true} pos-int?]
   [:stream-type {:optional true} stream-type]])

;; Window event
(def window-event-payload
  [:merge event-payload-base
   [:map
    [:type [:= :window]]
    [:window-type window-event-type]
    [:x {:optional true} int?]
    [:y {:optional true} int?]
    [:width {:optional true} pos-int?]
    [:height {:optional true} pos-int?]
    [:window-state {:optional true} int?]]])

;; Gesture event
(def gesture-event-payload
  [:merge event-payload-base
   [:map
    [:type [:= :gesture]]
    [:gesture-type gesture-type]
    [:x number?]
    [:y number?]
    [:ndc-x [:and number? [:>= -1] [:<= 1]]]
    [:ndc-y [:and number? [:>= -1] [:<= 1]]]
    [:canvas-width pos-int?]
    [:canvas-height pos-int?]
    [:aspect-ratio {:optional true} pos?]
    [:direction {:optional true} [:enum :up :down :left :right]]
    [:velocity {:optional true} number?]]])

;; Navigation event
(def navigation-event-payload
  [:merge event-payload-base
   [:map
    [:type [:= :navigation]]
    [:nav-type navigation-type]
    [:x number?]
    [:y number?]
    [:button {:optional true} [:enum 0 1 2]] ; left, middle, right
    [:wheel-rotation {:optional true} number?]
    [:frame-timestamp {:optional true} pos-int?]]])

;; Frame event (timing/performance events)
(def frame-event-payload
  [:merge event-payload-base
   [:map
    [:type [:= :frame]]
    [:frame-timestamp pos-int?]
    [:frame-duration {:optional true} pos-int?]
    [:fps {:optional true} number?]
    [:dropped-frames {:optional true} int?]]])

;; Error event (stream errors)
(def error-event-payload
  [:merge event-payload-base
   [:map
    [:type [:= :error]]
    [:error string?]
    [:code {:optional true} string?]
    [:fatal {:optional true} boolean?]]])

;; Complete event message
(def event-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :event]]
    [:payload [:or
               window-event-payload
               gesture-event-payload
               navigation-event-payload
               frame-event-payload
               error-event-payload]]]])

;; -----------------------------------------------------------------------------
;; Command Messages
;; -----------------------------------------------------------------------------

(def command-payload
  [:map
   [:action string?]
   [:params {:optional true} map?]])

(def command-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :command]]
    [:payload command-payload]]])

;; -----------------------------------------------------------------------------
;; State Update Messages
;; -----------------------------------------------------------------------------

(def state-update-payload
  [:map
   [:timestamp {:optional true} pos-int?]
   [:system {:optional true} [:map
                              [:battery-level {:optional true} [:and int? [:>= 0] [:<= 100]]]
                              [:has-data {:optional true} boolean?]]]
   [:proto-received {:optional true} boolean?]
   ;; Allow additional fields for extensibility
   ])

(def state-update-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :state-update]]
    [:payload state-update-payload]]])

;; -----------------------------------------------------------------------------
;; Log Messages
;; -----------------------------------------------------------------------------

(def log-level
  [:enum "DEBUG" "INFO" "WARN" "ERROR"])

(def log-payload
  [:map
   [:level log-level]
   [:message string?]
   [:process {:optional true} string?]
   [:stream-id {:optional true} string?]])

(def log-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :log]]
    [:payload log-payload]]])

;; -----------------------------------------------------------------------------
;; Error Messages
;; -----------------------------------------------------------------------------

(def error-payload
  [:map
   [:error string?]
   [:stack-trace {:optional true} string?]
   [:process {:optional true} string?]
   [:stream-id {:optional true} string?]])

(def error-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :error]]
    [:payload error-payload]]])

;; -----------------------------------------------------------------------------
;; Status Messages
;; -----------------------------------------------------------------------------

(def status-payload
  [:map
   [:status string?]
   [:connected {:optional true} boolean?]
   [:url {:optional true} string?]
   [:stream-id {:optional true} string?]])

(def status-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :status]]
    [:payload status-payload]]])

;; -----------------------------------------------------------------------------
;; Response Messages (for actual request/response patterns)
;; -----------------------------------------------------------------------------

(def response-payload
  [:map
   [:status [:enum :success :error]]
   [:request-id {:optional true} string?]
   [:result {:optional true} any?]
   [:error {:optional true} string?]])

(def response-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :response]]
    [:payload response-payload]]])

;; -----------------------------------------------------------------------------
;; Request Messages (from subprocesses needing action)
;; -----------------------------------------------------------------------------

(def request-payload
  [:map
   [:action string?]
   [:params {:optional true} map?]
   [:data {:optional true} map?]
   [:process {:optional true} string?]])

(def request-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :request]]
    [:payload request-payload]]])

;; -----------------------------------------------------------------------------
;; Metric Messages
;; -----------------------------------------------------------------------------

(def metric-payload
  [:map
   [:name string?]
   [:value any?]
   [:process {:optional true} string?]
   [:timestamp {:optional true} pos-int?]])

(def metric-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :metric]]
    [:payload metric-payload]]])

;; -----------------------------------------------------------------------------
;; Stream Lifecycle Messages
;; -----------------------------------------------------------------------------

(def stream-ready-payload
  [:map
   [:stream-id string?]
   [:stream-type stream-type]
   [:width pos-int?]
   [:height pos-int?]
   [:url {:optional true} string?]])

(def stream-ready-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :stream-ready]]
    [:payload stream-ready-payload]]])

(def stream-error-payload
  [:map
   [:stream-id string?]
   [:stream-type {:optional true} stream-type]
   [:error string?]
   [:code {:optional true} string?]
   [:fatal {:optional true} boolean?]])

(def stream-error-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :stream-error]]
    [:payload stream-error-payload]]])

(def stream-closed-payload
  [:map
   [:stream-id string?]
   [:stream-type {:optional true} stream-type]
   [:reason string?]
   [:code {:optional true} int?]])

(def stream-closed-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :stream-closed]]
    [:payload stream-closed-payload]]])

;; -----------------------------------------------------------------------------
;; State Partial Messages
;; -----------------------------------------------------------------------------

(def state-partial-payload
  [:map
   [:path [:vector keyword?]]  ; Path in state tree to update
   [:value any?]
   [:timestamp {:optional true} pos-int?]])

(def state-partial-message
  [:merge message-envelope
   [:map
    [:msg-type [:= :state-partial]]
    [:payload state-partial-payload]]])

;; -----------------------------------------------------------------------------
;; Subprocess Contracts
;; -----------------------------------------------------------------------------

(def subprocess-contracts
  "Defines what message types each subprocess can send and receive"
  {:video-heat {:can-send #{:event :log :error :status :metric}
                :can-receive #{:command :request}}
   :video-day  {:can-send #{:event :log :error :status :metric}
                :can-receive #{:command :request}}
   :command    {:can-send #{:log :error :status :response}
                :can-receive #{:command :request}}
   :state      {:can-send #{:state-update :state-partial :log :error :status}
                :can-receive #{:request}}
   :main       {:can-send #{:command :request}
                :can-receive #{:event :log :error :status :metric :state-update 
                              :state-partial :response}}})

;; -----------------------------------------------------------------------------
;; Message Type Registry
;; -----------------------------------------------------------------------------

(def message-type-schemas
  "Map of message type to its schema"
  {:event event-message
   :command command-message
   :state-update state-update-message
   :state-partial state-partial-message
   :log log-message
   :error error-message
   :status status-message
   :response response-message
   :request request-message
   :metric metric-message
   :stream-ready stream-ready-message
   :stream-error stream-error-message
   :stream-closed stream-closed-message})

;; -----------------------------------------------------------------------------
;; Validation Functions
;; -----------------------------------------------------------------------------

(defn validate-message-structure
  "Validates basic message envelope structure"
  [message]
  (if (m/validate message-envelope message)
    [true nil]
    [false (me/humanize (m/explain message-envelope message))]))

(defn validate-subprocess-contract
  "Validates that a subprocess is allowed to send/receive a message type"
  [subprocess-type direction msg-type]
  (let [contract (get subprocess-contracts subprocess-type)
        allowed-types (get contract (if (= direction :outgoing)
                                     :can-send
                                     :can-receive))]
    (if (contains? allowed-types msg-type)
      [true nil]
      [false {:error :contract-violation
              :message (str subprocess-type " cannot "
                           (if (= direction :outgoing) "send" "receive")
                           " " msg-type " messages")
              :allowed allowed-types}])))

(defn validate-message-type
  "Validates a specific message type against its schema"
  [message]
  (let [msg-type (:msg-type message)
        schema (get message-type-schemas msg-type)]
    (cond
      (nil? schema)
      [false {:error :unknown-message-type
              :message (str "No schema defined for message type: " msg-type)}]
      
      (m/validate schema message)
      [true nil]
      
      :else
      [false (me/humanize (m/explain schema message))])))

(defn validate-message
  "Comprehensive message validation. Returns [valid? errors]"
  ([message]
   (validate-message message nil nil))
  ([message subprocess-type direction]
   (let [;; Check structure
         [struct-valid? struct-errors] (validate-message-structure message)
         
         ;; Check contract if subprocess info provided
         [contract-valid? contract-errors] 
         (if (and subprocess-type direction)
           (validate-subprocess-contract subprocess-type direction (:msg-type message))
           [true nil])
         
         ;; Check specific message type
         [type-valid? type-errors] 
         (if struct-valid?
           (validate-message-type message)
           [false nil])]
     
     [(and struct-valid? contract-valid? type-valid?)
      (cond-> {}
        struct-errors (assoc :structure struct-errors)
        contract-errors (assoc :contract contract-errors)
        type-errors (assoc :message-type type-errors))])))

;; -----------------------------------------------------------------------------
;; Development Mode Helpers
;; -----------------------------------------------------------------------------

(defn development-mode?
  "Check if we're in development mode"
  []
  (not (runtime/release-build?)))

(defn log-validation-error
  "Log validation errors in development"
  [message errors & {:keys [subprocess direction]}]
  (when (development-mode?)
    (t/log! :error
            {:msg "Transit message validation failed"
             :subprocess subprocess
             :direction direction
             :msg-type (:msg-type message)
             :msg-id (:msg-id message)
             :errors errors
             :message message})))

(defn validate-and-log
  "Validate a message and log errors if in development mode"
  [message & {:keys [subprocess direction]}]
  (when (development-mode?)
    (let [[valid? errors] (validate-message message subprocess direction)]
      (when-not valid?
        (log-validation-error message errors 
                             :subprocess subprocess 
                             :direction direction))
      [valid? errors])))

;; -----------------------------------------------------------------------------
;; Message Creation Helpers
;; -----------------------------------------------------------------------------

(defn create-window-close-event
  "Creates a properly formatted window close event"
  [stream-type]
  {:msg-type :event
   :msg-id (str (java.util.UUID/randomUUID))
   :timestamp (System/currentTimeMillis)
   :payload {:type :window
             :window-type :close
             :stream-type stream-type}})

(defn create-gesture-event
  "Creates a properly formatted gesture event"
  [gesture-type x y ndc-x ndc-y canvas-width canvas-height stream-type]
  {:msg-type :event
   :msg-id (str (java.util.UUID/randomUUID))
   :timestamp (System/currentTimeMillis)
   :payload {:type :gesture
             :gesture-type gesture-type
             :x x
             :y y
             :ndc-x ndc-x
             :ndc-y ndc-y
             :canvas-width canvas-width
             :canvas-height canvas-height
             :stream-type stream-type}})