(ns generator.naming-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [generator.naming :as naming]
            [malli.core :as m]
            [malli.generator :as mg]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]))

(deftest normalize-for-filesystem-test
  (testing "normalizes package parts to lowercase"
    (is (= "daycamera" (naming/normalize-for-filesystem "DayCamera")))
    (is (= "heatcamera" (naming/normalize-for-filesystem "HeatCamera")))
    (is (= "rotaryplatform" (naming/normalize-for-filesystem "RotaryPlatform")))
    (is (= "lrf_calib" (naming/normalize-for-filesystem "Lrf_calib")))
    (is (= "daycamglassheater" (naming/normalize-for-filesystem "DayCamGlassHeater")))
    (is (= "cmd" (naming/normalize-for-filesystem "cmd")))
    (is (= "ser" (naming/normalize-for-filesystem "ser")))))

(deftest proto-package->clj-namespace-test
  (testing "converts protobuf packages to Clojure namespaces"
    (is (= "potatoclient.proto.cmd" (naming/proto-package->clj-namespace "cmd")))
    (is (= "potatoclient.proto.cmd.daycamera" (naming/proto-package->clj-namespace "cmd.DayCamera")))
    (is (= "potatoclient.proto.cmd.heatcamera" (naming/proto-package->clj-namespace "cmd.HeatCamera")))
    (is (= "potatoclient.proto.cmd.rotaryplatform" (naming/proto-package->clj-namespace "cmd.RotaryPlatform")))
    (is (= "potatoclient.proto.cmd.lrf_calib" (naming/proto-package->clj-namespace "cmd.Lrf_calib")))
    (is (= "potatoclient.proto.cmd.daycamglassheater" (naming/proto-package->clj-namespace "cmd.DayCamGlassHeater")))
    (is (= "potatoclient.proto.ser.jonquidata" (naming/proto-package->clj-namespace "ser.JonQuiData")))))

(deftest proto-package->file-path-test
  (testing "converts packages to file paths"
    (is (= "cmd.clj" (naming/proto-package->file-path "cmd")))
    (is (= "cmd/daycamera.clj" (naming/proto-package->file-path "cmd.DayCamera")))
    (is (= "cmd/lrf_calib.clj" (naming/proto-package->file-path "cmd.Lrf_calib")))
    (is (= "ser/jonquidata.clj" (naming/proto-package->file-path "ser.JonQuiData")))))

(deftest proto-type->spec-keyword-test
  (testing "converts type references to spec keywords"
    ;; Single part (rare but possible)
    (is (= :root (naming/proto-type->spec-keyword "Root")))
    (is (= :root (naming/proto-type->spec-keyword ".Root")))
    
    ;; Two parts - package.Type
    (is (= :cmd/root (naming/proto-type->spec-keyword "cmd.Root")))
    (is (= :cmd/root (naming/proto-type->spec-keyword ".cmd.Root")))
    
    ;; Three parts - package.Type.Message
    (is (= :cmd.daycamera/root (naming/proto-type->spec-keyword ".cmd.DayCamera.Root")))
    (is (= :cmd.heatcamera/root (naming/proto-type->spec-keyword ".cmd.HeatCamera.Root")))
    (is (= :cmd.rotaryplatform/root (naming/proto-type->spec-keyword ".cmd.RotaryPlatform.Root")))
    (is (= :cmd.lrf_calib/root (naming/proto-type->spec-keyword ".cmd.Lrf_calib.Root")))
    (is (= :cmd.daycamglassheater/root (naming/proto-type->spec-keyword ".cmd.DayCamGlassHeater.Root")))
    
    ;; Nested messages with kebab-case conversion
    (is (= :cmd.heatcamera/set-dde-level (naming/proto-type->spec-keyword ".cmd.HeatCamera.SetDDELevel")))
    (is (= :cmd.rotaryplatform/rotate-azimuth-to (naming/proto-type->spec-keyword ".cmd.RotaryPlatform.RotateAzimuthTo")))
    
    ;; Enum references
    (is (= :ser.jonquidata/client-type (naming/proto-type->spec-keyword ".ser.JonQuiData.ClientType")))))

(deftest proto-package->alias-test
  (testing "extracts alias from protobuf package"
    (is (= "daycamera" (naming/proto-package->alias "cmd.DayCamera")))
    (is (= "heatcamera" (naming/proto-package->alias "cmd.HeatCamera")))
    (is (= "cmd" (naming/proto-package->alias "cmd")))
    (is (= "ser" (naming/proto-package->alias "ser")))))

(deftest proto-name->clojure-fn-name-test
  (testing "converts protobuf names to Clojure function names"
    (is (= "root" (naming/proto-name->clojure-fn-name "Root")))
    (is (= "set-dde-level" (naming/proto-name->clojure-fn-name "SetDDELevel")))
    (is (= "rotate-azimuth-to" (naming/proto-name->clojure-fn-name "RotateAzimuthTo")))
    (is (= "enable-dde" (naming/proto-name->clojure-fn-name "EnableDDE")))
    (is (= "client-type" (naming/proto-name->clojure-fn-name "ClientType")))))

(deftest roundtrip-conversions-test
  (testing "conversions maintain consistency"
    ;; Package -> namespace -> file path
    (let [packages ["cmd.DayCamera" "cmd.HeatCamera" "cmd.Lrf_calib" "ser.JonQuiData"]]
      (doseq [pkg packages]
        (let [ns (naming/proto-package->clj-namespace pkg)
              file (naming/proto-package->file-path pkg)]
          (testing (str "package " pkg)
            (is (string? ns))
            (is (string? file))
            (is (re-matches #"^[a-z._]+$" ns))
            (is (str/ends-with? file ".clj"))))))
    
    ;; Type ref -> keyword consistency
    (let [refs [".cmd.DayCamera.Root" ".cmd.HeatCamera.SetDDELevel" ".ser.JonQuiData.ClientType"]]
      (doseq [ref refs]
        (let [kw (naming/proto-type->spec-keyword ref)]
          (testing (str "type ref " ref)
            (is (keyword? kw))
            (is (namespace kw))
            (is (not (str/includes? (namespace kw) "-")))))))) ; namespaces use lowercase, not kebab

(deftest spec-validation-test
  (testing "proto package spec"
    (is (m/validate naming/proto-package-spec "cmd"))
    (is (m/validate naming/proto-package-spec "cmd.DayCamera"))
    (is (m/validate naming/proto-package-spec "cmd.Lrf_calib"))
    (is (not (m/validate naming/proto-package-spec "")))
    (is (not (m/validate naming/proto-package-spec "Cmd"))) ; must start lowercase
    (is (not (m/validate naming/proto-package-spec "cmd."))))
  
  (testing "proto type ref spec"
    (is (m/validate naming/proto-type-ref-spec ".cmd.DayCamera.Root"))
    (is (m/validate naming/proto-type-ref-spec "cmd.Root"))
    (is (not (m/validate naming/proto-type-ref-spec "cmd"))) ; needs at least 2 parts
    (is (not (m/validate naming/proto-type-ref-spec ""))))
  
  (testing "clojure namespace spec"
    (is (m/validate naming/clojure-namespace-spec "cmd"))
    (is (m/validate naming/clojure-namespace-spec "cmd.daycamera"))
    (is (m/validate naming/clojure-namespace-spec "cmd.lrf_calib"))
    (is (not (m/validate naming/clojure-namespace-spec "cmd.DayCamera"))) ; no caps
    (is (not (m/validate naming/clojure-namespace-spec "cmd."))))
  
  (testing "clojure keyword spec"
    (is (m/validate naming/clojure-keyword-spec :cmd.daycamera/root))
    (is (m/validate naming/clojure-keyword-spec :ser.types/client-type))
    (is (not (m/validate naming/clojure-keyword-spec :no-namespace))) ; needs namespace
    (is (not (m/validate naming/clojure-keyword-spec "not-a-keyword"))))
  
  (testing "file path spec"
    (is (m/validate naming/file-path-spec "cmd.clj"))
    (is (m/validate naming/file-path-spec "cmd/daycamera.clj"))
    (is (m/validate naming/file-path-spec "cmd/lrf_calib.clj"))
    (is (not (m/validate naming/file-path-spec "cmd/DayCamera.clj"))) ; no caps
    (is (not (m/validate naming/file-path-spec "cmd/daycamera"))) ; needs .clj
    (is (not (m/validate naming/file-path-spec "cmd/day-camera.clj"))))) ; hyphens become underscores

(deftest lossless-conversion-test
  (testing "proto type to keyword conversions are lossless"
    ;; Test roundtrip for various proto types
    (let [test-types [".cmd.DayCamera.Root"
                      ".cmd.HeatCamera.SetDDELevel"
                      ".ser.JonQuiData.ClientType"
                      ".cmd.Lrf_calib.Root"
                      ".cmd.GPS.Status"
                      ".cmd.CV.ProcessFrame"]]
      (doseq [proto-type test-types]
        (testing (str "roundtrip for " proto-type)
          (let [kw (naming/proto-type->keyword proto-type)
                back (naming/keyword->proto-type kw)]
            (is (= "potatoclient.proto" (namespace kw)))
            (is (= proto-type back))))))
    
    ;; Test without leading dot - always adds dot back
    (is (= :potatoclient.proto/cmd.Root (naming/proto-type->keyword "cmd.Root")))
    (is (= ".cmd.Root" (naming/keyword->proto-type :potatoclient.proto/cmd.Root))))
  
  (testing "proto package to keyword conversions are lossless"
    (let [test-packages ["cmd"
                         "cmd.DayCamera"
                         "cmd.HeatCamera"
                         "ser.JonQuiData"
                         "cmd.Lrf_calib"
                         "cmd.GPS"]]
      (doseq [pkg test-packages]
        (testing (str "roundtrip for " pkg)
          (let [kw (naming/proto-package->keyword pkg)
                back (naming/keyword->proto-package kw)]
            (is (= "potatoclient.proto" (namespace kw)))
            (is (= pkg back)))))))
  
  (testing "spec validation for proto-encoded keywords"
    (is (naming/valid-proto-encoded-keyword? :potatoclient.proto/cmd.DayCamera.Root))
    (is (naming/valid-proto-encoded-keyword? :potatoclient.proto/cmd))
    (is (not (naming/valid-proto-encoded-keyword? :potatoclient.proto/.cmd.Root))) ; no leading dots
    (is (not (naming/valid-proto-encoded-keyword? :wrong.namespace/cmd.Root)))
    (is (not (naming/valid-proto-encoded-keyword? :potatoclient.proto/Cmd)))) ; must start lowercase
  
  (testing "proto field to clojure key"
    (is (= :protocol-version (naming/proto-field->clojure-key "protocol_version")))
    (is (= :client-type (naming/proto-field->clojure-key "clientType")))
    (is (= :set-dde-level (naming/proto-field->clojure-key "setDDELevel")))
    (is (= :from-cv-subsystem (naming/proto-field->clojure-key "from_cv_subsystem"))))))

;; =============================================================================
;; Generative Testing
;; =============================================================================

(defspec proto-type-keyword-roundtrip-spec 300
  (prop/for-all [proto-type (mg/generator naming/proto-type-ref-spec)]
    (let [kw (naming/proto-type->keyword proto-type)
          back (naming/keyword->proto-type kw)
          ;; Normalize both to have leading dot for comparison
          normalized-input (if (str/starts-with? proto-type ".")
                            proto-type
                            (str "." proto-type))]
      (and (naming/valid-proto-encoded-keyword? kw)
           (= normalized-input back)))))

(defspec proto-package-keyword-roundtrip-spec 300
  (prop/for-all [proto-pkg (mg/generator naming/proto-package-spec)]
    (let [kw (naming/proto-package->keyword proto-pkg)
          back (naming/keyword->proto-package kw)]
      (and (naming/valid-proto-encoded-keyword? kw)
           (= proto-pkg back)))))

(defspec proto-package-filesystem-consistency-spec 300
  (prop/for-all [proto-pkg (mg/generator naming/proto-package-spec)]
    (let [ns (naming/proto-package->clj-namespace proto-pkg)
          file (naming/proto-package->file-path proto-pkg)
          alias (naming/proto-package->alias proto-pkg)]
      (and (naming/valid-clojure-namespace? ns)
           (naming/valid-file-path? file)
           (string? alias)
           (= alias (str/lower-case alias))))))

(defspec proto-name-conversion-spec 300
  (prop/for-all [proto-name (mg/generator naming/proto-identifier-spec)]
    (let [fn-name (naming/proto-name->clojure-fn-name proto-name)]
      (and (string? fn-name)
           (re-matches #"^[a-z][a-z0-9-]*$" fn-name)))))

(defspec proto-field-conversion-spec 300
  (prop/for-all [field-name (mg/generator [:or
                                          ;; snake_case
                                          [:re #"^[a-z][a-z0-9]*(_[a-z][a-z0-9]*)*$"]
                                          ;; camelCase
                                          [:re #"^[a-z][a-zA-Z0-9]*$"]])]
    (let [kw (naming/proto-field->clojure-key field-name)]
      (and (keyword? kw)
           (nil? (namespace kw))
           (re-matches #"^[a-z][a-z0-9-]*$" (name kw))))))