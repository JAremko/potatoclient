(require '[potatoclient.proto.cmd :as cmd])
(require '[potatoclient.proto.ser :as ser])

;; Test protocol version validation
(println "Testing protocol version validation:")
(println "valid-root-protocol-version? 1 =>" (cmd/valid-root-protocol-version? 1))
(println "valid-root-protocol-version? 0 =>" (cmd/valid-root-protocol-version? 0))
(println "valid-root-protocol-version? -1 =>" (cmd/valid-root-protocol-version? -1))

;; Test RGB color validation
(println "\nTesting RGB color validation:")
(println "valid-rgb-color-red? 128 =>" (ser/valid-rgb-color-red? 128))
(println "valid-rgb-color-red? 0 =>" (ser/valid-rgb-color-red? 0))
(println "valid-rgb-color-red? 255 =>" (ser/valid-rgb-color-red? 255))
(println "valid-rgb-color-red? 256 =>" (ser/valid-rgb-color-red? 256))
(println "valid-rgb-color-red? -1 =>" (ser/valid-rgb-color-red? -1))