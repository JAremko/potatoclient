(ns potatoclient.test-harness
  "Test harness initialization for main app tests"
  (:require
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-pronto :as oneof]))

;; Initialize the global registry with oneof-pronto schema FIRST
(registry/setup-global-registry! (oneof/register-oneof-pronto-schema!))

;; NOW load the specs which will register themselves
(require '[potatoclient.specs.common :as common])
(require '[potatoclient.specs.cmd-root :as cmd-root])

(println "Main app test harness initialized:
  ✓ Malli global registry configured
  ✓ Custom oneof-pronto schema registered
  ✓ Common specs registered
  ✓ Ready for testing")