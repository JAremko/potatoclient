(ns generator.naming-test
  (:require [clojure.test :refer [deftest testing is]]
            [generator.naming :as naming]
            [malli.core :as m]))

(deftest normalize-package-part-test
  (testing "normalizes package parts to lowercase"
    (is (= "daycamera" (naming/normalize-package-part "DayCamera")))
    (is (= "heatcamera" (naming/normalize-package-part "HeatCamera")))
    (is (= "rotaryplatform" (naming/normalize-package-part "RotaryPlatform")))
    (is (= "lrf_calib" (naming/normalize-package-part "Lrf_calib")))
    (is (= "daycamglassheater" (naming/normalize-package-part "DayCamGlassHeater")))
    (is (= "cmd" (naming/normalize-package-part "cmd")))
    (is (= "ser" (naming/normalize-package-part "ser")))))

(deftest package->namespace-test
  (testing "converts protobuf packages to Clojure namespaces"
    (is (= "cmd" (naming/package->namespace "cmd")))
    (is (= "cmd.daycamera" (naming/package->namespace "cmd.DayCamera")))
    (is (= "cmd.heatcamera" (naming/package->namespace "cmd.HeatCamera")))
    (is (= "cmd.rotaryplatform" (naming/package->namespace "cmd.RotaryPlatform")))
    (is (= "cmd.lrf_calib" (naming/package->namespace "cmd.Lrf_calib")))
    (is (= "cmd.daycamglassheater" (naming/package->namespace "cmd.DayCamGlassHeater")))
    (is (= "ser.jonquidata" (naming/package->namespace "ser.JonQuiData")))))

(deftest package->file-path-test
  (testing "converts packages to file paths"
    (is (= "cmd.clj" (naming/package->file-path "cmd")))
    (is (= "cmd/daycamera.clj" (naming/package->file-path "cmd.DayCamera")))
    (is (= "cmd/lrf_calib.clj" (naming/package->file-path "cmd.Lrf_calib")))
    (is (= "ser/jonquidata.clj" (naming/package->file-path "ser.JonQuiData")))))

(deftest type-ref->keyword-test
  (testing "converts type references to keywords"
    ;; Single part (rare but possible)
    (is (= :root (naming/type-ref->keyword "Root")))
    (is (= :root (naming/type-ref->keyword ".Root")))
    
    ;; Two parts - package.Type
    (is (= :cmd/root (naming/type-ref->keyword "cmd.Root")))
    (is (= :cmd/root (naming/type-ref->keyword ".cmd.Root")))
    
    ;; Three parts - package.Type.Message
    (is (= :cmd.daycamera/root (naming/type-ref->keyword ".cmd.DayCamera.Root")))
    (is (= :cmd.heatcamera/root (naming/type-ref->keyword ".cmd.HeatCamera.Root")))
    (is (= :cmd.rotaryplatform/root (naming/type-ref->keyword ".cmd.RotaryPlatform.Root")))
    (is (= :cmd.lrf_calib/root (naming/type-ref->keyword ".cmd.Lrf_calib.Root")))
    (is (= :cmd.daycamglassheater/root (naming/type-ref->keyword ".cmd.DayCamGlassHeater.Root")))
    
    ;; Nested messages with kebab-case conversion
    (is (= :cmd.heatcamera/set-dde-level (naming/type-ref->keyword ".cmd.HeatCamera.SetDDELevel")))
    (is (= :cmd.rotaryplatform/rotate-azimuth-to (naming/type-ref->keyword ".cmd.RotaryPlatform.RotateAzimuthTo")))
    
    ;; Enum references
    (is (= :ser.jonquidata/client-type (naming/type-ref->keyword ".ser.JonQuiData.ClientType")))))

(deftest namespace->alias-test
  (testing "extracts alias from full namespace"
    (is (= "daycamera" (naming/namespace->alias "potatoclient.proto.cmd.daycamera")))
    (is (= "heatcamera" (naming/namespace->alias "potatoclient.proto.cmd.heatcamera")))
    (is (= "cmd" (naming/namespace->alias "potatoclient.proto.cmd")))
    (is (= "types" (naming/namespace->alias "potatoclient.proto.ser")))))

(deftest proto-name->clojure-name-test
  (testing "converts protobuf names to Clojure names"
    (is (= "root" (naming/proto-name->clojure-name "Root")))
    (is (= "set-dde-level" (naming/proto-name->clojure-name "SetDDELevel")))
    (is (= "rotate-azimuth-to" (naming/proto-name->clojure-name "RotateAzimuthTo")))
    (is (= "enable-dde" (naming/proto-name->clojure-name "EnableDDE")))
    (is (= "client-type" (naming/proto-name->clojure-name "ClientType")))))

(deftest roundtrip-conversions-test
  (testing "conversions maintain consistency"
    ;; Package -> namespace -> file path
    (let [packages ["cmd.DayCamera" "cmd.HeatCamera" "cmd.Lrf_calib" "ser.JonQuiData"]]
      (doseq [pkg packages]
        (let [ns (naming/package->namespace pkg)
              file (naming/package->file-path pkg)]
          (testing (str "package " pkg)
            (is (string? ns))
            (is (string? file))
            (is (re-matches #"^[a-z._]+$" ns))
            (is (str/ends-with? file ".clj"))))))
    
    ;; Type ref -> keyword consistency
    (let [refs [".cmd.DayCamera.Root" ".cmd.HeatCamera.SetDDELevel" ".ser.JonQuiData.ClientType"]]
      (doseq [ref refs]
        (let [kw (naming/type-ref->keyword ref)]
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
    (is (m/validate naming/clojure-namespace-spec "cmd.lrf-calib"))
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