(ns state-explorer.websocket-simple
  "Simplified WebSocket client using Java's built-in HTTP client"
  (:require [clojure.tools.logging :as log])
  (:import [java.net URI]
           [java.net.http HttpClient WebSocket WebSocket$Listener]
           [java.nio ByteBuffer]
           [java.time Duration]
           [java.util.concurrent CompletableFuture]
           [javax.net.ssl SSLContext TrustManager X509TrustManager]
           [java.security.cert X509Certificate]))

(defn- create-trust-all-ssl-context []
  (let [trust-manager (reify X509TrustManager
                        (getAcceptedIssuers [_] 
                          (make-array X509Certificate 0))
                        (checkClientTrusted [_ _ _])
                        (checkServerTrusted [_ _ _]))]
    (doto (SSLContext/getInstance "TLS")
      (.init nil (into-array TrustManager [trust-manager]) nil))))

(defn connect
  "Connect to WebSocket using Java 11+ HTTP client"
  [{:keys [url on-message on-connect on-close on-error]
    :or {on-connect #(log/info "Connected")
         on-close #(log/info "Closed")
         on-error #(log/error % "Error")}}]
  
  (let [ssl-context (create-trust-all-ssl-context)
        client (-> (HttpClient/newBuilder)
                  (.sslContext ssl-context)
                  (.connectTimeout (Duration/ofSeconds 10))
                  .build)
        ws-promise (promise)
        listener (reify WebSocket$Listener
                  (onOpen [_ websocket]
                    (log/info "WebSocket opened")
                    (deliver ws-promise websocket)
                    (.request websocket 1)
                    (on-connect)
                    nil)
                  
                  (onBinary [_ websocket data last?]
                    (let [bytes (byte-array (.remaining data))]
                      (.get data bytes)
                      (on-message bytes))
                    (.request websocket 1)
                    (CompletableFuture/completedFuture nil))
                  
                  (onClose [_ websocket status-code reason]
                    (on-close status-code reason)
                    (CompletableFuture/completedFuture nil))
                  
                  (onError [_ websocket error]
                    (on-error error)
                    nil))]
    
    (log/info "Connecting to" url)
    (try
      (let [builder (.newWebSocketBuilder client)
            _ (.header builder "Origin" (str "https://" (.getHost (URI. url))))
            future (.buildAsync builder (URI. url) listener)]
        {:client @ws-promise
         :future future})
      (catch Exception e
        (log/error e "Failed to connect")
        (throw e)))))

(defn close [{:keys [client]}]
  (when client
    (try
      (.sendClose client WebSocket/NORMAL_CLOSURE "Done")
      (log/info "Connection closed")
      (catch Exception e
        (log/error e "Error closing")))))