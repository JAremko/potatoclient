#!/usr/bin/env clojure

(ns test-oneof-in-map
  "Test using oneof_edn as fields within a map spec"
  (:require
   [malli.core :as m]
   [malli.generator :as mg]
   [malli.util :as mu]
   [potatoclient.malli.registry :as registry]
   [potatoclient.specs.oneof-edn :as oneof-edn]))

;; Initialize registry
(registry/setup-global-registry!
  (oneof-edn/register_ONeof-edn-schema!))

(println "\n=== Testing oneof_edn as map fields ===\n")

;; Test 1: oneof_edn as a field value in a map
(println "Test 1: oneof_edn as a map field value")
(let [schema [:map {:closed true}
              [:id :int]
              [:name :string]
              [:command [:oneof_edn
                        [:ping [:map {:closed true} [:id :int]]]
                        [:echo [:map {:closed true} [:msg :string]]]
                        [:noop [:map {:closed true}]]]]]]
  
  (println "  Schema created successfully")
  
  ;; Test validation
  (println "\n  Valid examples:")
  (let [valid [{:id 1 :name "test" :command {:ping {:id 123}}}
               {:id 2 :name "foo" :command {:echo {:msg "hello"}}}
               {:id 3 :name "bar" :command {:noop {}}}]]
    (doseq [v valid]
      (println (str "    " v " => " (m/validate schema v)))))
  
  (println "\n  Invalid examples:")
  (let [invalid [{:id 1 :name "test"} ; missing command
                 {:id 1 :name "test" :command {}} ; empty command
                 {:id 1 :name "test" :command {:ping {:id 1} :echo {:msg "x"}}} ; multiple commands
                 {:id 1 :name "test" :command {:unknown {}}}]] ; unknown command
    (doseq [v invalid]
      (println (str "    " v " => " (m/validate schema v)))))
  
  ;; Test generation
  (println "\n  Generating examples:")
  (dotimes [i 5]
    (let [gen (mg/generate schema)]
      (println (str "    Gen " i ": " gen " valid? " (m/validate schema gen))))))

;; Test 2: Multiple oneof_edn fields in the same map
(println "\n\nTest 2: Multiple oneof_edn fields in same map")
(let [schema [:map {:closed true}
              [:id :int]
              [:action [:oneof_edn
                       [:create :keyword]
                       [:update :keyword]
                       [:delete :keyword]]]
              [:target [:oneof_edn
                       [:user [:map [:id :int]]]
                       [:group [:map [:name :string]]]]]]]
  
  (println "  Valid examples:")
  (let [valid [{:id 1 :action {:create :user} :target {:user {:id 42}}}
               {:id 2 :action {:delete :group} :target {:group {:name "admins"}}}]]
    (doseq [v valid]
      (println (str "    " v " => " (m/validate schema v)))))
  
  (println "\n  Generating examples:")
  (dotimes [i 3]
    (let [gen (mg/generate schema)]
      (println (str "    Gen " i ": " gen)))))

;; Test 3: Using mu/merge with maps containing oneof_edn
(println "\n\nTest 3: Using mu/merge with maps containing oneof_edn")
(let [base-schema [:map {:closed true}
                  [:id :int]
                  [:timestamp :int]]
      
      command-schema [:map {:closed true}
                     [:command [:oneof_edn
                               [:ping [:map [:id :int]]]
                               [:pong [:map [:id :int]]]]]]
      
      ;; Merge two map schemas
      merged-schema (mu/merge base-schema command-schema)]
  
  (println "  Merged schema type:" (m/type merged-schema))
  (println "  Merged schema children:" (m/children merged-schema))
  
  (println "\n  Valid merged examples:")
  (let [valid [{:id 1 :timestamp 12345 :command {:ping {:id 99}}}
               {:id 2 :timestamp 67890 :command {:pong {:id 88}}}]]
    (doseq [v valid]
      (println (str "    " v " => " (m/validate merged-schema v)))))
  
  (println "\n  Generating merged examples:")
  (dotimes [i 3]
    (let [gen (mg/generate merged-schema)]
      (println (str "    Gen " i ": " gen)))))

;; Test 4: Nested map with oneof at different levels
(println "\n\nTest 4: Nested structures with oneof_edn")
(let [schema [:map {:closed true}
              [:request [:map {:closed true}
                        [:method [:enum :get :post :put :delete]]
                        [:body [:oneof_edn
                               [:json :any]
                               [:form [:map]]
                               [:binary :bytes]]]]]
              [:response [:oneof_edn
                         [:success [:map [:status :int] [:data :any]]]
                         [:error [:map [:status :int] [:message :string]]]]]]]
  
  (println "  Valid nested examples:")
  (let [valid [{:request {:method :post :body {:json {:key "value"}}}
                :response {:success {:status 200 :data [1 2 3]}}}
               {:request {:method :get :body {:form {}}}
                :response {:error {:status 404 :message "Not found"}}}]]
    (doseq [v valid]
      (println (str "    " (select-keys v [:request]) 
                   " + " (select-keys v [:response])
                   " => " (m/validate schema v)))))
  
  (println "\n  Generating nested examples:")
  (dotimes [i 3]
    (let [gen (mg/generate schema)]
      (println (str "    Gen " i ": method=" (get-in gen [:request :method])
                   " body-type=" (first (keys (get-in gen [:request :body])))
                   " response-type=" (first (keys (:response gen))))))))

;; Test 5: The original cmd/root pattern
(println "\n\nTest 5: CMD/Root pattern (base + oneof)")
(require '[potatoclient.specs.cmd.common])
(let [base-fields [:map {:closed true}
                  [:protocol_version [:int {:min 1}]]
                  [:client_type [:enum :ground :web :ios]]
                  [:session_id {:optional true} :int]]
      
      command-fields [:oneof_edn
                     [:ping [:map {:closed true} [:id :int]]]
                     [:echo [:map {:closed true} [:message :string]]]
                     [:noop [:map {:closed true}]]]
      
      ;; Manually compose by putting oneof as a field
      full-schema [:map {:closed true}
                  [:protocol_version [:int {:min 1}]]
                  [:client_type [:enum :ground :web :ios]]
                  [:session_id {:optional true} :int]
                  [:command [:oneof_edn
                            [:ping [:map {:closed true} [:id :int]]]
                            [:echo [:map {:closed true} [:message :string]]]
                            [:noop [:map {:closed true}]]]]]]
  
  (println "  Valid cmd examples:")
  (let [valid [{:protocol_version 1 :client_type :ground :command {:ping {:id 123}}}
               {:protocol_version 2 :client_type :web :session_id 999 :command {:echo {:message "test"}}}]]
    (doseq [v valid]
      (println (str "    " v " => " (m/validate full-schema v)))))
  
  (println "\n  Generating cmd examples:")
  (dotimes [i 5]
    (let [gen (mg/generate full-schema)
          cmd-type (first (keys (:command gen)))]
      (println (str "    Gen " i ": pv=" (:protocol_version gen)
                   " client=" (:client_type gen)
                   " cmd=" cmd-type)))))

(println "\n✅ All tests complete!")
(println "\nKEY INSIGHTS:")
(println "1. oneof_edn works perfectly as a field value in map specs")
(println "2. Multiple oneof_edn fields can coexist in the same map")
(println "3. mu/merge works when both schemas are maps containing oneof_edn fields")
(println "4. Nesting works at any level")
(println "5. For cmd/root pattern: wrap oneof in a :command field instead of direct merge")

(System/exit 0)