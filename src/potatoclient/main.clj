(ns potatoclient.main
  "Main entry point for PotatoClient"
  (:require [potatoclient.core :as core])
  (:gen-class))

(defn -main
  "Main entry point"
  [& args]
  (apply core/-main args))

(comment
  (-main))