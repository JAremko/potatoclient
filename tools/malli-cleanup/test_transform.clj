(require '[malli-cleanup.simple-transformer :as st])

(def test-input "
(defn test-fn
  \"Test function\"
  {:malli/schema [:=> [:cat [:fn (fn* [p1__1234#] (instance? File p1__1234#))]] :boolean]}
  [file]
  (.exists file))
")

(println "Original:")
(println test-input)
(println "\nTransformed:")
(println (st/transform-file-content test-input))
