(ns taoensso.telemere
  "Stub definitions for clj-kondo to understand Telemere logging macros")

(defmacro log!
  "Stub for Telemere log! macro"
  [& args]
  nil)

(defmacro with-signal
  "Stub for with-signal macro"
  [level opts & body]
  `(do ~@body))

(defmacro with-ctx
  "Stub for with-ctx macro"
  [ctx & body]
  `(do ~@body))

(defmacro with-ctx+
  "Stub for with-ctx+ macro"
  [ctx & body]
  `(do ~@body))

(defmacro with-middleware
  "Stub for with-middleware macro"
  [middleware & body]
  `(do ~@body))

(defmacro with-handler
  "Stub for with-handler macro"
  [handler & body]
  `(do ~@body))

(defmacro with-min-level
  "Stub for with-min-level macro"
  [level & body]
  `(do ~@body))

(defmacro trace!
  "Stub for trace! macro"
  [& args]
  nil)

(defmacro debug!
  "Stub for debug! macro"
  [& args]
  nil)

(defmacro info!
  "Stub for info! macro"
  [& args]
  nil)

(defmacro warn!
  "Stub for warn! macro"
  [& args]
  nil)

(defmacro error!
  "Stub for error! macro"
  [& args]
  nil)

(defmacro fatal!
  "Stub for fatal! macro"
  [& args]
  nil)