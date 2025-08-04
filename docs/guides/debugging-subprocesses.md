# Debugging Subprocesses Guide

This guide explains how to debug Kotlin subprocesses in PotatoClient's multi-process architecture.

## Overview

PotatoClient uses separate Kotlin processes for:
- **Command Subprocess**: Handles Transit → Protobuf conversion
- **State Subprocess**: Handles Protobuf → Transit conversion  
- **Video Subprocesses**: H.264 decoding and display (one per stream)

## Subprocess Logging

### Development Mode Logging

In development, each subprocess creates its own log file:

```bash
# Log locations
logs/
├── command-subprocess-20250804-143022.log
├── state-subprocess-20250804-143022.log
├── video-stream-heat-20250804-143022.log
└── video-stream-day-20250804-143022.log
```

### Viewing Logs

```bash
# Follow specific subprocess log
tail -f logs/command-subprocess-*.log

# View all subprocess logs
tail -f logs/*subprocess*.log

# Search for errors
grep -i error logs/*subprocess*.log

# View with timestamps
less logs/state-subprocess-*.log
```

### Log Levels

Set log levels via environment variables:

```bash
# Maximum verbosity for command subprocess
POTATOCLIENT_LOG_LEVEL=TRACE make dev

# Or in code
System.setProperty("potatoclient.log.level", "DEBUG")
```

## Common Issues and Solutions

### Issue: Subprocess Won't Start

**Symptoms**: 
- Main process hangs waiting for subprocess
- No subprocess log files created

**Debug Steps**:

1. Check Java process list:
```bash
# See all Java processes
jps -v | grep potato

# Or with more detail
ps aux | grep -E "java.*potato"
```

2. Enable verbose subprocess launching:
```clojure
;; In REPL
(log/set-level! :potatoclient.transit.subprocess-launcher :trace)
```

3. Check stderr capture:
```clojure
;; Subprocess errors are logged
(defn- log-subprocess-errors [process subprocess-name]
  (future
    (with-open [reader (io/reader (.getErrorStream process))]
      (doseq [line (line-seq reader)]
        (log/error "Subprocess" subprocess-name "stderr:" line)))))
```

### Issue: Transit Communication Failures

**Symptoms**:
- "Invalid Transit data" errors
- Subprocess appears to hang
- No response to commands

**Debug Steps**:

1. Enable Transit debug logging:
```kotlin
// In Kotlin subprocess
class DebugTransitCommunicator : TransitCommunicator() {
    override fun sendMessage(message: Any) {
        logger.debug("Sending Transit: ${toJson(message)}")
        super.sendMessage(message)
    }
}
```

2. Capture raw Transit data:
```clojure
;; In Clojure
(defn debug-transit-writer [out]
  (let [writer (transit/writer out :msgpack)]
    (reify
      java.io.Closeable
      (close [_] (.close out))
      
      Object
      (write [_ msg]
        (log/debug "Writing Transit:" (pr-str msg))
        (transit/write writer msg)))))
```

3. Test Transit round-trip:
```clojure
;; In REPL
(require '[potatoclient.transit.core :as t])

(let [test-msg {:msg-type :command :payload {:ping {}}}
      encoded (t/encode-transit test-msg)
      decoded (t/decode-transit encoded)]
  (= test-msg decoded))  ; Should be true
```

### Issue: Protobuf Conversion Errors

**Symptoms**:
- "Failed to build protobuf" in logs
- ClassNotFoundException for protobuf classes
- NullPointerException in command handling

**Debug Steps**:

1. Verify protobuf classes exist:
```bash
# Check compiled classes
find target/classes -name "*.class" | grep -i cmd

# Should see:
# target/classes/cmd/JonSharedCmd$Root.class
# target/classes/cmd/JonSharedCmd$Ping.class
# etc.
```

2. Enable protobuf debug mode:
```kotlin
// Add to GeneratedCommandHandlers.kt temporarily
private fun debugCommand(command: Map<String, Any?>) {
    logger.debug("Command structure: ${command.keys}")
    command.forEach { (k, v) ->
        logger.debug("  $k -> ${v?.javaClass?.simpleName}: $v")
    }
}
```

3. Test specific command:
```clojure
;; Send test command
(cmd/send-command! {:ping {}})

;; Check subprocess log for handling
```

### Issue: Video Stream Problems

**Symptoms**:
- Black video canvas
- "Decoder not found" errors
- Frame drops or stuttering

**Debug Steps**:

1. Check GStreamer installation:
```bash
# List available decoders
gst-inspect-1.0 | grep -i h264

# Test pipeline
gst-launch-1.0 videotestsrc ! videoconvert ! autovideosink
```

2. Enable GStreamer debugging:
```bash
# Run with GStreamer debug output
GST_DEBUG=3 make dev

# Or specific component
GST_DEBUG=h264parse:5,avdec_h264:5 make dev
```

3. Monitor video metrics:
```kotlin
// In VideoStreamManager
private fun logMetrics() {
    logger.info("Stream $streamType metrics:")
    logger.info("  Frames received: $framesReceived")
    logger.info("  Frames decoded: $framesDecoded")
    logger.info("  Frames dropped: $framesDropped")
    logger.info("  Decoder: $currentDecoder")
}
```

## Advanced Debugging Techniques

### Remote JVM Debugging

1. Start subprocess with debug port:
```clojure
;; Modify subprocess-launcher.clj
(defn- build-subprocess-command [subprocess-name main-class]
  (concat
    ["java"]
    (when (= subprocess-name "command")
      ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"])
    ["-cp" (get-classpath)
     main-class]))
```

2. Connect from IntelliJ IDEA:
- Run → Edit Configurations → Add → Remote JVM Debug
- Port: 5005
- Module classpath: potatoclient

### Memory Profiling

```bash
# Start with profiling
JAVA_OPTS="-XX:+UseG1GC -XX:+PrintGCDetails -Xlog:gc*:file=logs/gc.log" make dev

# Analyze GC logs
grep "Full GC" logs/gc.log
```

### Subprocess Health Checks

Add health monitoring:

```clojure
(defn check-subprocess-health []
  (doseq [[name process] @process-registry]
    (let [alive? (.isAlive process)]
      (log/info name "process alive?" alive?)
      (when-not alive?
        (log/error name "process died with exit code" (.exitValue process))))))

;; Run periodically
(future
  (while true
    (Thread/sleep 5000)
    (check-subprocess-health)))
```

## Debugging Checklist

### Before Starting

- [ ] Clean build: `make clean && make proto && make compile-kotlin`
- [ ] Check Java version: `java -version` (must be 17+)
- [ ] Verify GStreamer: `gst-inspect-1.0 --version`
- [ ] Clear old logs: `rm logs/*subprocess*.log`

### During Debugging

- [ ] Check subprocess log files exist
- [ ] Look for exceptions in stderr
- [ ] Verify Transit message flow
- [ ] Monitor system resources (CPU, memory)
- [ ] Check for zombie processes

### Common Commands

```bash
# Find hung processes
ps aux | grep defunct

# Kill all subprocess
pkill -f "potatoclient.*subprocess"

# Check port usage (for remote debug)
lsof -i :5005

# Monitor subprocess CPU
top -p $(pgrep -f command-subprocess -d,)
```

## Testing Subprocesses Individually

### Test Command Subprocess

```kotlin
// Create standalone test
fun main() {
    val comm = TransitCommunicator(System.`in`, System.out)
    
    // Send test response
    comm.sendMessage(mapOf(
        "msg-type" to "response",
        "msg-id" to "test-1", 
        "payload" to mapOf("status" to "ok")
    ))
}
```

### Test with Mock Video Stream

```bash
cd tools/mock-video-stream

# Start mock server
make start-server

# In another terminal, start client
make dev

# Commands are logged to console
```

## Best Practices

1. **Always check logs first** - Most issues are logged
2. **Use structured logging** - Include context in log messages
3. **Test incrementally** - Verify each layer works
4. **Monitor resources** - Subprocesses can consume significant CPU/memory
5. **Clean shutdown** - Ensure subprocesses exit cleanly

## See Also

- [Transit Protocol](../architecture/transit-protocol.md)
<!-- TODO: Create this file
- [Kotlin Subprocess Architecture](../architecture/kotlin-subprocess.md)
-->
- [Mock Video Stream Tool](../tools/mock-video-stream.md)