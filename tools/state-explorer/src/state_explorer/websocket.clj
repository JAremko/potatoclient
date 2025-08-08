(ns state-explorer.websocket
  "WebSocket client for connecting to the state endpoint"
  (:require [hato.websocket :as ws]
            [clojure.tools.logging :as log])
  (:import [java.nio ByteBuffer]
           [javax.net.ssl SSLContext TrustManager X509TrustManager]
           [java.security.cert X509Certificate]))

;; Trust all certificates for development
(defn- create-trust-all-ssl-context []
  (let [trust-manager (reify X509TrustManager
                        (getAcceptedIssuers [_] (make-array X509Certificate 0))
                        (checkClientTrusted [_ _ _])
                        (checkServerTrusted [_ _ _]))]
    (doto (SSLContext/getInstance "TLS")
      (.init nil (into-array TrustManager [trust-manager]) nil))))

(defn connect
  "Connect to WebSocket endpoint and handle incoming messages.
  
  Options:
    :url - WebSocket URL (required)
    :on-message - Function called with binary payload (required)
    :on-connect - Function called when connected (optional)
    :on-close - Function called when connection closes (optional)
    :on-error - Function called on error (optional)"
  [{:keys [url on-message on-connect on-close on-error]
    :or {on-connect (fn [] (log/info "WebSocket connected"))
         on-close (fn [status reason] (log/info "WebSocket closed:" status reason))
         on-error (fn [error] (log/error error "WebSocket error"))}}]
  (let [ssl-context (create-trust-all-ssl-context)
        connection (promise)
        handlers {:on-open (fn [ws]
                            (deliver connection ws)
                            (on-connect))
                  
                  :on-message (fn [ws message last?]
                               ;; Handle binary messages
                               (when (instance? ByteBuffer message)
                                 (let [bytes (byte-array (.remaining message))]
                                   (.get message bytes)
                                   (on-message bytes))))
                  
                  :on-close (fn [ws status reason]
                             (on-close status reason))
                  
                  :on-error (fn [ws error]
                             (on-error error))}]
    
    (log/info "Connecting to" url)
    (try
      (let [ws-client (ws/websocket url
                                   handlers
                                   {:ssl-context ssl-context})]
        {:client ws-client
         :connection connection})
      (catch Exception e
        (log/error e "Failed to connect to WebSocket")
        (throw e)))))

(defn close
  "Close WebSocket connection"
  [{:keys [client]}]
  (when client
    (try
      (ws/close! client)
      (log/info "WebSocket connection closed")
      (catch Exception e
        (log/error e "Error closing WebSocket")))))