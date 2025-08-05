(println "Loading json-to-edn...")
(load-file "src/proto_explorer/json_to_edn.clj")
(println "OK")

(println "Loading spec-generator...")
(load-file "src/proto_explorer/spec_generator.clj")
(println "OK")

(println "\nTesting case conversion:")
(println (proto-explorer.json-to-edn/snake->kebab "message_type"))
(println (proto-explorer.json-to-edn/camel->kebab "messageType"))

(println "\nTesting type mapping:")
(println (proto-explorer.spec-generator/proto-type->malli :type-string))

(println "\nAll basic tests passed!")