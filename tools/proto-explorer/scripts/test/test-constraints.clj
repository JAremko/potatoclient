(require '[proto-explorer.json-to-edn :as json-edn])
(require '[clojure.pprint :as pp])

;; Load a descriptor that should have constraints
(def descriptor (json-edn/load-json-descriptor "output/json-descriptors/jon_shared_cmd_rotary.json"))

;; Find SetVelocity message
(def messages (get-in descriptor [:file 0 :message-type]))
(def set-velocity (first (filter #(= "SetVelocity" (:name %)) messages)))

(println "SetVelocity message:")
(pp/pprint set-velocity)

;; Check if options are present
(println "\nChecking for options in fields:")
(doseq [field (:field set-velocity)]
  (println "Field:" (:name field) "Options:" (:options field)))

;; Also check SetMode which has explicit validation
(def set-mode (first (filter #(= "SetMode" (:name %)) messages)))
(println "\n\nSetMode message:")
(pp/pprint set-mode)