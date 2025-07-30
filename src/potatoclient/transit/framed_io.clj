(ns potatoclient.transit.framed-io
  "Framed I/O for Transit messages with length prefix.
  
  Matches the Kotlin implementation that uses 4-byte big-endian length prefix."
  (:require [com.fulcrologic.guardrails.malli.core :refer [=> >defn >defn-]])
  (:import [java.io InputStream OutputStream ByteArrayOutputStream]
           [java.nio ByteBuffer ByteOrder]))

(>defn- read-length-prefix
  "Read 4-byte big-endian length prefix from input stream"
  [^InputStream in]
  [[:fn #(instance? InputStream %)] => pos-int?]
  (let [length-bytes (byte-array 4)]
    (loop [offset 0]
      (when (< offset 4)
        (let [n (.read in length-bytes offset (- 4 offset))]
          (if (= n -1)
            (throw (java.io.EOFException. "Unexpected end of stream reading length prefix"))
            (recur (+ offset n))))))
    (-> (ByteBuffer/wrap length-bytes)
        (.order ByteOrder/BIG_ENDIAN)
        (.getInt))))

(>defn- write-length-prefix
  "Write 4-byte big-endian length prefix to output stream"
  [^OutputStream out length]
  [[:fn #(instance? OutputStream %)] pos-int? => nil?]
  (let [buffer (ByteBuffer/allocate 4)]
    (.order buffer ByteOrder/BIG_ENDIAN)
    (.putInt buffer length)
    (.write out (.array buffer)))
  nil)

(>defn make-framed-input-stream
  "Create a framed input stream that reads messages with length prefix"
  [^InputStream in]
  [[:fn #(instance? InputStream %)] => [:fn #(instance? InputStream %)]]
  (let [current-frame (atom nil)
        frame-pos (atom 0)
        frame-size (atom 0)]
    (proxy [InputStream] []
      (read
        ([]
         (locking this
           (letfn [(read-next-byte []
                     (cond
                       ;; No current frame
                       (nil? @current-frame)
                       (let [length (read-length-prefix in)
                             data (byte-array length)]
                         ;; Read entire frame
                         (loop [offset 0]
                           (when (< offset length)
                             (let [n (.read in data offset (- length offset))]
                               (if (= n -1)
                                 ;; Check if this is a shutdown scenario
                                 (if (Thread/interrupted)
                                   (throw (java.io.InterruptedIOException. "Stream read interrupted during shutdown"))
                                   (throw (java.io.EOFException. "Unexpected end of stream reading frame")))
                                 (recur (+ offset n))))))
                         (reset! current-frame data)
                         (reset! frame-pos 0)
                         (reset! frame-size length)
                         ;; Return first byte
                         (let [b (bit-and (aget data 0) 0xFF)]
                           (reset! frame-pos 1)
                           b))

                       ;; End of current frame
                       (>= @frame-pos @frame-size)
                       (do
                         (reset! current-frame nil)
                         (read-next-byte))

                       ;; Read from current frame
                       :else
                       (let [pos @frame-pos
                             b (bit-and (aget @current-frame pos) 0xFF)]
                         (swap! frame-pos inc)
                         b)))]
             (read-next-byte))))
        ([b off len]
         (locking this
           (letfn [(read-bytes [idx]
                     (if (or (>= idx len)
                             (>= @frame-pos @frame-size))
                       idx
                       (let [next-byte (.read this)]
                         (if (= next-byte -1)
                           idx
                           (do
                             (aset b (+ off idx) (unchecked-byte next-byte))
                             (read-bytes (inc idx)))))))]
             (let [first-byte (.read this)]
               (if (= first-byte -1)
                 -1
                 (do
                   (aset b off (unchecked-byte first-byte))
                   (read-bytes 1))))))))
      (close []
        (.close ^InputStream in)))))

(>defn make-framed-output-stream
  "Create a framed output stream that writes messages with length prefix"
  [^OutputStream out]
  [[:fn #(instance? OutputStream %)] => [:fn #(instance? OutputStream %)]]
  (let [buffer (ByteArrayOutputStream.)]
    (proxy [OutputStream] []
      (write
        ([b]
         (if (integer? b)
           (.write buffer b)
           (.write buffer ^bytes b 0 (alength ^bytes b))))
        ([b off len]
         (.write buffer b off len)))
      (flush []
        (locking this
          (let [data (.toByteArray buffer)]
            (when (pos? (alength data))
              (write-length-prefix out (alength data))
              (.write out data)
              (.flush out)
              (.reset buffer)))))
      (close []
        (.flush this)
        (.close ^OutputStream out)))))
