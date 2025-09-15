(ns potatoclient.state.server.websocket
  "WebSocket client for state ingress from /ws/ws_state endpoint.
   Handles connection, reconnection, and message reception."
  (:require [potatoclient.logging :as logging]
            [malli.core :as m])
  (:import [java.net URI]
           [java.net.http HttpClient WebSocket WebSocket$Listener]
           [java.nio ByteBuffer]
           [java.time Duration]
           [java.util.concurrent CompletableFuture]
           [javax.net.ssl SSLContext TrustManager X509TrustManager]
           [java.security.cert X509Certificate]))

(defn- create-trust-all-ssl-context
  "Create an SSL context that trusts all certificates.
   For development use only."
  []
  (let [trust-manager (reify X509TrustManager
                        (checkClientTrusted [_ _ _])
                        (checkServerTrusted [_ _ _])
                        (getAcceptedIssuers [_] (make-array X509Certificate 0)))
        ssl-context (SSLContext/getInstance "TLS")]
    (.init ssl-context nil (into-array TrustManager [trust-manager]) nil)
    ssl-context))

(m/=> create-trust-all-ssl-context [:=> [:cat] any?])

(defn- create-websocket-listener
  "Create a WebSocket listener with message handling."
  [message-buffer on-message on-connect on-close on-error]
  (reify WebSocket$Listener
    (onOpen [_ ws]
      (logging/log-debug {:msg "WebSocket opened"})
      (on-connect)
      (.request ws 1))

    (onBinary [_ ws data last?]
      (try
        ;; Accumulate data
        (swap! message-buffer
               (fn [existing]
                 (let [new-size (+ (.remaining existing) (.remaining data))
                       combined (ByteBuffer/allocate new-size)]
                   (.put combined existing)
                   (.put combined data)
                   (.flip combined)
                   combined)))

        ;; If this is the last part, process the complete message
        (when last?
          (let [complete-buffer @message-buffer
                byte-array (byte-array (.remaining complete-buffer))]
            (.get complete-buffer byte-array)
            (on-message byte-array)
            ;; Reset buffer for next message
            (reset! message-buffer (ByteBuffer/allocate 0))))

        (.request ws 1)
        (CompletableFuture/completedStage nil)
        (catch Exception e
          (logging/log-error {:msg "Error processing binary message" :error e})
          (on-error e)
          (.request ws 1)
          (CompletableFuture/completedStage nil))))

    (onClose [_ _ status-code reason]
      (logging/log-debug {:msg (str "WebSocket closing: " status-code " " reason)})
      (on-close status-code reason))

    (onError [_ _ error]
      (logging/log-error {:msg "WebSocket error" :error error})
      (on-error error))))

(m/=> create-websocket-listener [:=> [:cat any? fn? fn? fn? fn?] any?])

(defn connect
  "Connect to WebSocket endpoint with automatic binary message handling.
   
   Options:
   - :url - WebSocket URL (required)
   - :on-message - Handler for complete binary messages (required)
   - :on-connect - Called when connected (optional)
   - :on-close - Called with [status-code reason] when closed (optional)
   - :on-error - Called with exception on error (optional)
   
   Returns a connection map with :client and :ws keys."
  [{:keys [url on-message on-connect on-close on-error]
    :or {on-connect #(logging/log-debug {:msg "WebSocket connected"})
         on-close (fn [code reason] (logging/log-debug {:msg (str "WebSocket closed: " code " " reason)}))
         on-error #(logging/log-error {:msg "WebSocket error" :error %})}}]

  (let [client (-> (HttpClient/newBuilder)
                   (.sslContext (create-trust-all-ssl-context))
                   (.connectTimeout (Duration/ofSeconds 10))
                   (.build))

        ;; Buffer to accumulate message parts
        message-buffer (atom (ByteBuffer/allocate 0))

        ;; Create the listener
        listener (create-websocket-listener
                   message-buffer on-message on-connect on-close on-error)

        ;; Build and connect with Origin header
        builder (.newWebSocketBuilder client)
        uri (URI/create url)
        _ (.header builder "Origin" (str "https://" (.getHost uri)))
        ws-future (.buildAsync builder uri listener)
        ws (.get ws-future)]

    {:client client
     :ws ws}))

(m/=> connect [:=> [:cat [:map {:closed false}
                          [:url :string]
                          [:on-message fn?]
                          [:on-connect {:optional true} fn?]
                          [:on-close {:optional true} fn?]
                          [:on-error {:optional true} fn?]]]
               [:map [:client any?] [:ws any?]]])

(defn close
  "Close WebSocket connection."
  [{:keys [ws]}]
  (when ws
    (.sendClose ws WebSocket/NORMAL_CLOSURE "Client closing")
    (.join (.abort ws))))

(m/=> close [:=> [:cat [:map [:ws any?]]] :nil])

(defn connected?
  "Check if WebSocket is connected."
  [{:keys [ws]}]
  (and ws (not (.isInputClosed ws)) (not (.isOutputClosed ws))))