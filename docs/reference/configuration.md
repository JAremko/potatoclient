# Configuration Reference

PotatoClient configuration files and their options.

## Configuration Files

### Gesture Configuration

**Location**: `resources/config/gestures.edn`

Controls gesture recognition and pan speed behavior.

#### Gesture Recognition Parameters

```clojure
{:gesture-config
 {:move-threshold 20              ; Pixels before pan gesture starts
  :tap-long-press-threshold 300   ; Milliseconds for long press detection
  :double-tap-threshold 300       ; Max milliseconds between taps for double-tap
  :swipe-threshold 100           ; Minimum pixels for swipe recognition
  :pan-update-interval 120       ; Milliseconds between pan command updates
  :double-tap-tolerance 10}}     ; Pixel tolerance for double-tap position
```

**Parameter Details**:

- **move-threshold**: Minimum movement in pixels before a drag becomes a pan gesture. Prevents accidental panning.
- **tap-long-press-threshold**: Time in ms to distinguish between tap and long press.
- **double-tap-threshold**: Maximum time between two taps to register as double-tap.
- **swipe-threshold**: Minimum distance for swipe gesture recognition.
- **pan-update-interval**: Throttle pan updates to prevent overwhelming the system.
- **double-tap-tolerance**: How close taps must be to count as double-tap.

#### Zoom-Based Speed Configuration

Controls rotation speed at different zoom levels for each camera:

```clojure
:zoom-speed-config
{:heat [{:zoom-table-index 0      ; Zoom level (0-4)
         :max-rotation-speed 0.1   ; Maximum rotation speed
         :min-rotation-speed 0.0001 ; Minimum rotation speed
         :ndc-threshold 0.5        ; NDC distance for max speed
         :dead-zone-radius 0.05    ; Center dead zone (NDC units)
         :curve-steepness 4.0}     ; Speed curve exponential factor
        ; ... more zoom levels
        ]
 :day [; ... similar structure
      ]}
```

**Speed Calculation**:
- Speed increases exponentially from center to edge
- Dead zone prevents drift near center
- Formula: `speed = min + (max - min) * ((distance / threshold) ^ steepness)`

### Logging Configuration

**Location**: `resources/logback.xml`

Controls application logging behavior.

```xml
<configuration>
  <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/potatoclient-dev.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>logs/potatoclient-dev-%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
      <maxHistory>7</maxHistory>
    </rollingPolicy>
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  
  <root level="INFO">
    <appender-ref ref="FILE"/>
    <appender-ref ref="CONSOLE"/>
  </root>
</configuration>
```

**Key Settings**:
- Log files rotate daily
- Keeps 7 days of compressed logs
- Separate log files for each subprocess
- Console output for development

### Internationalization

**Location**: `resources/i18n/*.edn`

Language-specific text and messages.

```clojure
;; resources/i18n/en.edn
{:app-title "PotatoClient"
 :menu {:file "File"
        :file-exit "Exit"
        :file-preferences "Preferences"
        :view "View"
        :view-theme "Theme"
        :help "Help"
        :help-about "About"}
 :buttons {:connect "Connect"
           :disconnect "Disconnect"
           :record "Record"
           :stop "Stop"}
 ; ... more translations
}
```

## Environment Variables

### Development Variables

```bash
# GStreamer debugging (0-9, higher = more verbose)
GST_DEBUG=3

# Java options
JAVA_OPTS="-Xmx4g -XX:+UseG1GC"

# Disable subprocess for testing
POTATOCLIENT_NO_SUBPROCESS=true

# Custom config directory
POTATOCLIENT_CONFIG_DIR=/path/to/config
```

### Runtime Variables

```bash
# Video stream URLs
HEAT_STREAM_URL=rtsp://camera1/stream
DAY_STREAM_URL=rtsp://camera2/stream

# Command server
COMMAND_SERVER_URL=wss://server:8443/commands

# Enable debug logging
DEBUG=true
```

## JVM Options

### Memory Settings

```bash
# Development (in deps.edn :dev alias)
-Xms512m          # Initial heap size
-Xmx2g            # Maximum heap size
-XX:MaxMetaspaceSize=256m

# Production (in Makefile)
-Xms1g
-Xmx4g
-XX:+UseG1GC      # G1 garbage collector
-XX:MaxGCPauseMillis=50
```

### Performance Tuning

```bash
# Enable tiered compilation
-XX:+TieredCompilation

# Direct linking for production
-Dclojure.compiler.direct-linking=true

# Disable reflection warnings in production
-Dclojure.compiler.disable-locals-clearing=false
```

## Application Settings

### Window Preferences

Stored in user preferences:

```clojure
{:window {:width 1280
          :height 720
          :x 100
          :y 100
          :maximized false}
 :theme :dark
 :language :en
 :video {:heat-visible true
         :day-visible true
         :side-by-side true}}
```

### Connection Settings

```clojure
{:connection {:url "wss://example.com:8443"
              :reconnect true
              :reconnect-delay 5000
              :timeout 30000}
 :auth {:method :token
        :token "..."}}
```

## Configuration Loading

### Load Order

1. Built-in defaults
2. Classpath resources (`resources/`)
3. Config directory (`~/.potatoclient/`)
4. Environment variables
5. Command-line arguments

### Custom Configuration

```clojure
;; Override gesture config
(require '[potatoclient.config :as config])

(config/merge-config!
  {:gesture-config
   {:move-threshold 10}})  ; More sensitive

;; Load from file
(config/load-config-file! "custom-config.edn")
```

## Validation

All configuration is validated at startup:

```clojure
;; Gesture config schema
[:map
 [:gesture-config
  [:map
   [:move-threshold pos-int?]
   [:tap-long-press-threshold pos-int?]
   ; ... etc
   ]]
 [:zoom-speed-config
  [:map
   [:heat [:vector zoom-config-schema]]
   [:day [:vector zoom-config-schema]]]]]
```

Invalid configuration prevents startup with clear error messages.

## Best Practices

### Do's

1. ✓ Keep configuration in EDN format
2. ✓ Validate all config at startup
3. ✓ Document all config options
4. ✓ Provide sensible defaults
5. ✓ Allow environment overrides

### Don'ts

1. ✗ Don't hardcode configuration
2. ✗ Don't mix config with code
3. ✗ Don't use mutable config
4. ✗ Don't skip validation
5. ✗ Don't expose secrets

## Troubleshooting

### Config Not Loading

```bash
# Check config search path
java -jar potatoclient.jar --show-config-path

# Validate config file
java -jar potatoclient.jar --validate-config config.edn
```

### Performance Issues

```clojure
;; Reduce pan update frequency
{:gesture-config {:pan-update-interval 200}}

;; Adjust zoom speeds
{:zoom-speed-config
 {:heat [{:max-rotation-speed 0.05 ; ... }]}}
```

## See Also

- [Gesture Calibration](../guides/gesture-calibration.md)
- [Environment Setup](../development/getting-started.md)
- [Debugging Guide](../guides/debugging-subprocesses.md)