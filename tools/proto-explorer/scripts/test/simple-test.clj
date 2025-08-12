(println "Loading json-to-edn...")
(load-file "src/proto_explorer/json_to_edn.clj")
(println "OK")

(println "\nTesting case preservation (normalize-key):")
(println (proto-explorer.json-to-edn/normalize-key "message_type"))  ; => :message_type
(println (proto-explorer.json-to-edn/normalize-key "messageType"))   ; => :messageType
(println (proto-explorer.json-to-edn/normalize-key "TYPE_STRING"))   ; => :TYPE_STRING

(println "\nAll basic tests passed!")