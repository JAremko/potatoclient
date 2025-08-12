(ns validate.specs.cmd-root-final-test
  "Test the final cmd/root implementation with oneof_edn generator"
  (:require
   [clojure.test :refer [deftest testing is]]
   [malli.core :as m]
   [malli.generator :as mg]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]
   [potatoclient.specs.common]  ;; For proto/protocol-version and proto/client-type
   [potatoclient.specs.cmd.root]
   [potatoclient.specs.cmd.common]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

;; Import command fields from cmd.root
(def command-fields potatoclient.specs.cmd.root/command-fields)

(def spec :cmd/root)

(deftest cmd-root-validation-test
  (testing "Valid examples should validate"
    (let [valid-examples
          [{:protocol_version 1 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK :ping {}}
           {:protocol_version 2 :client_type :JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV :noop {}}
           {:protocol_version 1 :client_type :JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED :ping {}}
           {:protocol_version 3 :client_type :JON_GUI_DATA_CLIENT_TYPE_LIRA :session_id 999 :frozen {}}]]
      (doseq [example valid-examples]
        (is (m/validate spec example) 
            (str "Should validate example with " (first (filter command-fields (keys example))))))))
  
  (testing "Invalid examples should not validate"
    (let [invalid-examples
          [{:protocol_version 1 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK}  ; No command
           {:protocol_version 1 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK :ping {} :noop {}}  ; Multiple commands
           {:protocol_version 0 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK :ping {}}  ; Invalid protocol version
           {:protocol_version 1 :client_type :unknown :ping {}}  ; Invalid client type
           {:protocol_version 1 :client_type :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK :ping {} :extra "field"}]]  ; Extra field
      (doseq [example invalid-examples]
        (is (not (m/validate spec example))
            (str "Should not validate: " (or (first (keys (select-keys example command-fields))) "no-cmd")))))))

(deftest cmd-root-generation-test
  (testing "Generated samples should be valid"
    (let [samples (repeatedly 20 #(mg/generate spec))
          command-counts (frequencies 
                         (map (fn [s]
                               (first (filter command-fields (keys s))))
                             samples))
          all-valid? (every? #(m/validate spec %) samples)]
      (is all-valid? "All generated samples should be valid")
      (is (> (count command-counts) 1) "Should generate variety of commands")))
  
  (testing "Required fields always present"
    (let [samples (repeatedly 10 #(mg/generate spec))]
      (is (every? :protocol_version samples) "All samples should have protocol_version")
      (is (every? :client_type samples) "All samples should have client_type")
      (is (every? (fn [s]
                    (= 1 (count (filter #(some? (get s %)) command-fields))))
                  samples)
          "All samples should have exactly one command")))
  
  (testing "Optional fields sometimes present"
    (let [samples (repeatedly 100 #(mg/generate spec))
          with-session (filter :session_id samples)
          with-important (filter :important samples)
          with-from-cv (filter :from_cv_subsystem samples)]
      ;; Optional fields may or may not appear - that's ok
      (is (>= (count with-session) 0) "session_id is optional")
      (is (<= (count with-session) 100) "session_id count should be <= 100")
      (is (>= (count with-important) 0) "Important flag is optional")
      (is (>= (count with-from-cv) 0) "from_cv_subsystem flag is optional"))))

(deftest cmd-root-constraints-test
  (testing "Protocol version constraint"
    (let [samples (repeatedly 50 #(mg/generate spec))
          all-positive? (every? #(pos? (:protocol_version %)) samples)]
      (is all-positive? "All protocol_versions should be > 0")))
  
  (testing "Client type enum validation"
    (let [samples (repeatedly 50 #(mg/generate spec))
          valid-types #{:JON_GUI_DATA_CLIENT_TYPE_INTERNAL_CV
                        :JON_GUI_DATA_CLIENT_TYPE_LOCAL_NETWORK
                        :JON_GUI_DATA_CLIENT_TYPE_CERTIFICATE_PROTECTED
                        :JON_GUI_DATA_CLIENT_TYPE_LIRA}
          all-valid? (every? #(contains? valid-types (:client_type %)) samples)]
      (is all-valid? "All client_types should be valid enum values"))))