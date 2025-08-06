(ns potatoclient.proto
  "Stub definitions for clj-kondo to understand PotatoClient custom macros")

(defmacro defmsg
  "Stub for defmsg macro - defines a protocol buffer message handler"
  [name & body]
  `(def ~name ~@body))