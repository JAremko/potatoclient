(defn test-fn
  "Test function" {:malli/schema [:=> [:cat :int] :int]}
  [x]
  (inc x))

(defn test-fn2
  "Another test" {:malli/schema [:=> [:cat [:fn (fn* [p1__1234#] (instance? File p1__1234#))]] :boolean]}
  [file]
  (.exists file))
