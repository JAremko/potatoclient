package potatoclient.java;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import javax.net.ssl.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.security.cert.X509Certificate;
import static potatoclient.java.Constants.*;

public class WebSocketManager {
    public interface EventCallback {
        void onConnected(String url);
        void onDisconnected(int code, String reason, boolean remote);
        void onError(String message);
        void onVideoData(ByteBuffer data);
        boolean isRunning();
    }
    
    private static final SSLContext SSL_CONTEXT = createTrustAllSSLContext();
    
    private final String streamUrl;
    private final EventCallback callback;
    private final ScheduledExecutorService reconnectExecutor;
    
    private volatile WebSocketClient wsClient;
    private volatile ScheduledFuture<?> reconnectTask;
    private final AtomicBoolean shouldReconnect = new AtomicBoolean(false);
    private final AtomicBoolean intentionalDisconnect = new AtomicBoolean(false);
    private final AtomicLong reconnectAttempts = new AtomicLong(0);
    
    public WebSocketManager(String streamUrl, EventCallback callback, ScheduledExecutorService reconnectExecutor) {
        this.streamUrl = streamUrl;
        this.callback = callback;
        this.reconnectExecutor = reconnectExecutor;
    }
    
    private static SSLContext createTrustAllSSLContext() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            return sc;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SSL context", e);
        }
    }
    
    public void connect() {
        intentionalDisconnect.set(false);
        shouldReconnect.set(true);
        
        try {
            URI uri = new URI(streamUrl);
            Map<String, String> headers = new HashMap<>();
            headers.put("Origin", "https://" + uri.getHost());
            headers.put("User-Agent", WS_USER_AGENT);
            headers.put("Cache-Control", WS_CACHE_CONTROL);
            headers.put("Pragma", WS_PRAGMA);
            
            wsClient = new WebSocketClient(uri, headers) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    reconnectAttempts.set(0); // Reset reconnect attempts on successful connection
                    callback.onConnected(streamUrl);
                }
                
                @Override
                public void onMessage(String message) {
                    // Ignore text messages
                }
                
                @Override
                public void onMessage(ByteBuffer bytes) {
                    if (callback.isRunning()) {
                        callback.onVideoData(bytes);
                    }
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    callback.onDisconnected(code, reason != null ? reason : "unknown", remote);
                    if (!intentionalDisconnect.get() && shouldReconnect.get()) {
                        scheduleReconnect();
                    }
                }
                
                @Override
                public void onError(Exception ex) {
                    callback.onError(ex != null ? ex.getMessage() : "unknown");
                    if (!intentionalDisconnect.get() && shouldReconnect.get()) {
                        scheduleReconnect();
                    }
                }
            };
            
            // Configure SSL for WSS
            if (streamUrl.startsWith("wss://")) {
                wsClient.setSocketFactory(SSL_CONTEXT.getSocketFactory());
            }
            
            wsClient.setConnectionLostTimeout(0); // Disable built-in ping/pong
            wsClient.connect();
            
        } catch (Exception e) {
            callback.onError("Failed to connect: " + e.getMessage());
            if (!intentionalDisconnect.get() && shouldReconnect.get()) {
                scheduleReconnect();
            }
        }
    }
    
    public void disconnect() {
        intentionalDisconnect.set(true);
        shouldReconnect.set(false);
        cancelReconnectTask();
        
        WebSocketClient client = wsClient;
        if (client != null && !client.isClosed()) {
            wsClient = null;
            try {
                client.close();
                Thread.sleep(WEBSOCKET_CLOSE_WAIT_MS);
            } catch (Exception ignored) {
                // Best effort close
            }
        }
    }
    
    private void scheduleReconnect() {
        if (reconnectTask != null && !reconnectTask.isDone()) {
            return; // Already scheduled
        }
        
        long attempts = reconnectAttempts.incrementAndGet();
        
        reconnectTask = reconnectExecutor.schedule(() -> {
            if (shouldReconnect.get() && !intentionalDisconnect.get()) {
                attemptReconnect();
            }
        }, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
    }
    
    private void attemptReconnect() {
        // Close existing WebSocket if any
        WebSocketClient client = wsClient;
        if (client != null && !client.isClosed()) {
            wsClient = null;
            try {
                client.close();
                Thread.sleep(RECONNECT_CLOSE_WAIT_MS);
            } catch (Exception ignored) {
                // Best effort
            }
        }
        
        // Only reconnect if we're still supposed to be showing
        if (shouldReconnect.get() && !intentionalDisconnect.get() && callback.isRunning()) {
            connect();
        }
    }
    
    private void cancelReconnectTask() {
        if (reconnectTask != null && !reconnectTask.isDone()) {
            reconnectTask.cancel(true);
            reconnectTask = null;
        }
    }
    
    public boolean isConnected() {
        return wsClient != null && wsClient.isOpen();
    }
    
    public long getReconnectAttempts() {
        return reconnectAttempts.get();
    }
}