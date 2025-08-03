package potatoclient.java.transit;

/**
 * Common actions for command messages.
 * These constants ensure consistency between Clojure and Kotlin code.
 */
public final class CommandActions {
    public static final String STOP = "stop";
    public static final String SHUTDOWN = "shutdown";
    public static final String PING = "ping";
    public static final String STATUS = "status";
    public static final String SHOW = "show";
    public static final String HIDE = "hide";
    public static final String PAUSE = "pause";
    public static final String RESUME = "resume";
    public static final String CONNECT = "connect";
    public static final String DISCONNECT = "disconnect";
    public static final String SET_RATE_LIMIT = "set-rate-limit";
    public static final String SET_DEBOUNCE = "set-debounce";
    
    // Private constructor to prevent instantiation
    private CommandActions() {}
}