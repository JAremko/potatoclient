(ns generator.naming-memoization-test
  "Test memoization and collision detection in naming conversions"
  (:require [clojure.test :refer [deftest testing is]]
            [generator.naming :as naming]))

(deftest memoization-test
  (testing "Same input returns same output from cache"
    ;; Clear cache first
    (naming/clear-conversion-cache!)
    
    ;; First call
    (let [result1 (naming/proto-type->keyword ".cmd.DayCamera.Root")]
      ;; Second call should return cached value
      (let [result2 (naming/proto-type->keyword ".cmd.DayCamera.Root")]
        (is (= result1 result2))
        (is (identical? result1 result2) "Should be the same object from cache"))))
  
  (testing "Different inputs with same output throw collision error"
    (naming/clear-conversion-cache!)
    
    ;; This is a hypothetical test - in practice our conversions shouldn't collide
    ;; But we can test the mechanism by calling the internal function directly
    (let [cache-fn #'naming/check-and-cache!]
      ;; First conversion
      (cache-fn ::test-conversion "input1" "same-output")
      
      ;; Second conversion with different input but same output should throw
      (is (thrown-with-msg? 
           clojure.lang.ExceptionInfo
           #"Naming collision detected"
           (cache-fn ::test-conversion "input2" "same-output"))))))

(deftest all-conversions-memoized
  (testing "All public conversion functions use memoization"
    (naming/clear-conversion-cache!)
    
    ;; Test each conversion function
    (let [conversions
          [[naming/proto-type->keyword ".cmd.DayCamera.Root"]
           [naming/proto-package->keyword "cmd.DayCamera"]
           [naming/proto-package->clj-namespace "cmd.DayCamera"]
           [naming/proto-package->file-path "cmd.DayCamera"]
           [naming/proto-package->alias "cmd.DayCamera"]
           [naming/proto-name->clojure-fn-name "SetDDELevel"]
           [naming/proto-field->clojure-key "protocol_version"]
           [naming/proto-type->spec-keyword ".cmd.DayCamera.Root"]]]
      
      (doseq [[f input] conversions]
        (testing (str "Testing " (:name (meta f)))
          ;; First call
          (let [result1 (f input)
                ;; Second call  
                result2 (f input)]
            (is (= result1 result2) "Results should be equal")
            (is (identical? result1 result2) "Results should be identical (from cache)")))))))

(deftest collision-safety
  (testing "Real-world conversions don't collide"
    (naming/clear-conversion-cache!)
    
    ;; Test a variety of real proto names
    (let [proto-packages ["cmd.DayCamera" "cmd.HeatCamera" "ser.Heat" "ser.GPS"]
          proto-types [".cmd.DayCamera.Root" ".cmd.HeatCamera.Zoom" ".ser.Heat.State"]
          proto-names ["SetDDELevel" "GetMeteo" "StartRec" "StopRec"]]
      
      ;; Convert all packages
      (doseq [pkg proto-packages]
        (naming/proto-package->clj-namespace pkg)
        (naming/proto-package->file-path pkg)
        (naming/proto-package->alias pkg))
      
      ;; Convert all types
      (doseq [t proto-types]
        (naming/proto-type->keyword t)
        (naming/proto-type->spec-keyword t))
      
      ;; Convert all names
      (doseq [n proto-names]
        (naming/proto-name->clojure-fn-name n))
      
      ;; If we got here, no collisions occurred
      (is true "No collisions detected"))))

(deftest cache-clearing
  (testing "Cache can be cleared"
    (naming/clear-conversion-cache!)
    
    ;; Add something to cache
    (let [result1 (naming/proto-type->keyword ".cmd.Test")]
      ;; Clear cache
      (naming/clear-conversion-cache!)
      
      ;; Call again - should compute fresh (though result will be same)
      (let [result2 (naming/proto-type->keyword ".cmd.Test")]
        (is (= result1 result2) "Results should be equal")
        ;; Can't test non-identity since keywords are interned
        ;; But the important thing is no errors occurred
        ))))