package potatoclient.java;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * WebSocket client implementation using Java 17's built-in HttpClient.
 * Supports ignoring SSL certificate errors for development/testing.
 */
public class WebSocketClientBuiltIn {
    private final URI serverUri;
    private final Map<String, String> headers;
    private final Consumer<ByteBuffer> onBinaryMessage;
    private final Runnable onConnect;
    private final BiConsumer<Integer, String> onClose;
    private final Consumer<Throwable> onError;
    
    private final AtomicReference<WebSocket> webSocketRef = new AtomicReference<>();
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private final AtomicBoolean shouldReconnect = new AtomicBoolean(false);
    private final HttpClient httpClient;
    
    // Buffer for accumulating partial messages
    private ByteBuffer messageBuffer = ByteBuffer.allocate(1024 * 1024); // 1MB initial size
    
    public WebSocketClientBuiltIn(URI serverUri, Map<String, String> headers,
                                  Consumer<ByteBuffer> onBinaryMessage,
                                  Runnable onConnect,
                                  BiConsumer<Integer, String> onClose,
                                  Consumer<Throwable> onError) {
        this.serverUri = serverUri;
        this.headers = headers;
        this.onBinaryMessage = onBinaryMessage;
        this.onConnect = onConnect;
        this.onClose = onClose;
        this.onError = onError;
        
        // Create HttpClient with trust-all SSL context for wss:// connections
        this.httpClient = createHttpClient();
    }
    
    private HttpClient createHttpClient() {
        try {
            // Create trust-all SSL context
            SSLContext sslContext = createTrustAllSSLContext();
            
            return HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        } catch (Exception e) {
            System.err.println("Failed to create HttpClient with custom SSL context, using default: " + e.getMessage());
            return HttpClient.newHttpClient();
        }
    }
    
    private static SSLContext createTrustAllSSLContext() throws Exception {
        TrustAllTrustManager trustAll = new TrustAllTrustManager();
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new X509TrustManager[]{ trustAll }, new SecureRandom());
        return sslContext;
    }
    
    public void connect() {
        if (isConnecting.compareAndSet(false, true)) {
            shouldReconnect.set(true);
            performConnect();
        }
    }
    
    private void performConnect() {
        try {
            System.out.println("Connecting to WebSocket: " + serverUri);
            
            WebSocket.Builder builder = httpClient.newWebSocketBuilder();
            
            // Add custom headers
            headers.forEach(builder::header);
            
            CompletableFuture<WebSocket> future = builder.buildAsync(serverUri, new WebSocketListener());
            
            future.whenComplete((webSocket, throwable) -> {
                if (throwable != null) {
                    handleConnectionError(throwable);
                } else {
                    webSocketRef.set(webSocket);
                    isConnecting.set(false);
                    if (onConnect != null) {
                        onConnect.run();
                    }
                }
            });
            
        } catch (Exception e) {
            handleConnectionError(e);
        }
    }
    
    private void handleConnectionError(Throwable error) {
        System.err.println("WebSocket connection error: " + error.getMessage());
        isConnecting.set(false);
        if (onError != null) {
            onError.accept(error);
        }
        
        // Attempt reconnection if enabled
        if (shouldReconnect.get()) {
            scheduleReconnect();
        }
    }
    
    private void scheduleReconnect() {
        // Simple reconnection with 1 second delay
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                if (shouldReconnect.get() && !isConnecting.get()) {
                    performConnect();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    public void send(byte[] data) {
        WebSocket ws = webSocketRef.get();
        if (ws != null && !ws.isOutputClosed()) {
            ws.sendBinary(ByteBuffer.wrap(data), true)
                .exceptionally(throwable -> {
                    System.err.println("Failed to send binary data: " + throwable.getMessage());
                    if (onError != null) {
                        onError.accept(throwable);
                    }
                    return null;
                });
        }
    }
    
    public void close() {
        shouldReconnect.set(false);
        WebSocket ws = webSocketRef.getAndSet(null);
        if (ws != null && !ws.isOutputClosed()) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "Client closing")
                .exceptionally(throwable -> {
                    System.err.println("Error during close: " + throwable.getMessage());
                    return null;
                });
        }
    }
    
    public boolean isOpen() {
        WebSocket ws = webSocketRef.get();
        return ws != null && !ws.isOutputClosed() && !ws.isInputClosed();
    }
    
    private class WebSocketListener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("WebSocket opened");
            webSocket.request(1);
        }
        
        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            try {
                // Handle the binary message
                if (messageBuffer.remaining() < data.remaining()) {
                    // Expand buffer if needed
                    ByteBuffer newBuffer = ByteBuffer.allocate(messageBuffer.capacity() + data.remaining() + 1024 * 1024);
                    messageBuffer.flip();
                    newBuffer.put(messageBuffer);
                    messageBuffer = newBuffer;
                }
                
                messageBuffer.put(data);
                
                if (last) {
                    // Complete message received
                    messageBuffer.flip();
                    
                    // Create a copy for the callback
                    ByteBuffer messageCopy = ByteBuffer.allocate(messageBuffer.remaining());
                    messageCopy.put(messageBuffer);
                    messageCopy.flip();
                    
                    messageBuffer.clear();
                    
                    // Invoke callback
                    if (onBinaryMessage != null) {
                        onBinaryMessage.accept(messageCopy);
                    }
                }
                
                webSocket.request(1);
                return CompletableFuture.completedFuture(null);
                
            } catch (Exception e) {
                System.err.println("Error processing binary message: " + e.getMessage());
                if (onError != null) {
                    onError.accept(e);
                }
                webSocket.request(1);
                return CompletableFuture.completedFuture(null);
            }
        }
        
        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            // We don't expect text messages for video streaming
            System.out.println("Unexpected text message received: " + data);
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }
        
        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.err.println("WebSocket error: " + error.getMessage());
            if (onError != null) {
                onError.accept(error);
            }
            
            // Trigger reconnection
            webSocketRef.set(null);
            if (shouldReconnect.get()) {
                scheduleReconnect();
            }
        }
        
        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.println("WebSocket closed: " + statusCode + " - " + reason);
            webSocketRef.set(null);
            
            if (onClose != null) {
                onClose.accept(statusCode, reason);
            }
            
            // Trigger reconnection if not a normal closure
            if (shouldReconnect.get() && statusCode != WebSocket.NORMAL_CLOSURE) {
                scheduleReconnect();
            }
            
            return CompletableFuture.completedFuture(null);
        }
    }
    
    // Trust manager that accepts all certificates (insecure!)
    private static class TrustAllTrustManager extends X509ExtendedTrustManager {
        @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
        @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
        @Override public void checkClientTrusted(X509Certificate[] chain, String authType, java.net.Socket socket) {}
        @Override public void checkServerTrusted(X509Certificate[] chain, String authType, java.net.Socket socket) {}
        @Override public void checkClientTrusted(X509Certificate[] chain, String authType, javax.net.ssl.SSLEngine engine) {}
        @Override public void checkServerTrusted(X509Certificate[] chain, String authType, javax.net.ssl.SSLEngine engine) {}
        @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
    }
}