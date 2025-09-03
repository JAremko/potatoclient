(ns potatoclient.runtime
  "Runtime detection utilities"
  (:require
            [malli.core :as m] [clojure.java.io :as io]))

(defn release-build?
  "Check if this is a release build by checking:
   1. System property (command line -D flag)
   2. Environment variable
   3. Embedded RELEASE marker file in JAR"
  []
  (boolean
    (or (System/getProperty "potatoclient.release")
        (System/getenv "POTATOCLIENT_RELEASE")
        (io/resource "RELEASE")))) 
 (m/=> release-build? [:=> [:cat] :boolean])