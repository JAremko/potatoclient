(require '[proto-explorer.json-to-edn :as json-edn])
(require '[clojure.pprint :as pp])

;; Load a descriptor that should have constraints
(def descriptor (json-edn/load-json-descriptor "output/json-descriptors/jon_shared_cmd_rotary.json"))

;; Find the rotary file (it's not always at index 0)
(def rotary-file (first (filter #(re-find #"rotary" (:name %)) (:file descriptor))))
(def messages (:messageType rotary-file))
;; Note: SetVelocity doesn't exist in rotary, let's look for SetMode or SetAzimuthValue
(def set-azimuth (first (filter #(= "SetAzimuthValue" (:name %)) messages)))

(println "SetAzimuthValue message:")
(pp/pprint set-azimuth)

;; Check if options are present
(println "\nChecking for options in fields:")
(doseq [field (:field set-azimuth)]
  (println "Field:" (:name field) "Options:" (:options field)))

;; Also check SetMode which has explicit validation
(def set-mode (first (filter #(= "SetMode" (:name %)) messages)))
(println "\n\nSetMode message:")
(pp/pprint set-mode)