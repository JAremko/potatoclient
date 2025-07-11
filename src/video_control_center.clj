(ns video-control-center
  "Legacy entry point - delegates to potatoclient.core"
  (:require [potatoclient.core :as core])
  (:gen-class))

(defn -main
  "Delegate to the new core namespace"
  [& args]
  (apply core/-main args))

(comment
  (-main))