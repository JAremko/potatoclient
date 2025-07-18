(ns potatoclient.runtime
  "Runtime detection utilities"
  (:require [clojure.java.io :as io]
            [com.fulcrologic.guardrails.malli.core :as gr :refer [>defn >defn- >def | ? =>]]))

(>defn release-build?
       "Check if this is a release build by checking:
   1. System property (command line -D flag)
   2. Environment variable
   3. Embedded RELEASE marker file in JAR"
       []
       [=> boolean?]
       (boolean
        (or (System/getProperty "potatoclient.release")
            (System/getenv "POTATOCLIENT_RELEASE")
            (io/resource "RELEASE"))))