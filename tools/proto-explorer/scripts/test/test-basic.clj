;; Basic test of Proto Explorer functionality
(require '[proto-explorer.core :as pe])

(println "\n=== Testing Proto Explorer ===\n")

;; Test 1: Basic initialization
(println "1. Initializing...")
(pe/init! {:proto-dir "." :class-path "."})

;; Test 2: Command path lookup
(println "\n2. Command path for 'rotary-goto-ndc':")
(clojure.pprint/pprint (pe/command-path "rotary-goto-ndc"))

;; Test 3: Generate builder
(println "\n3. Generated Kotlin builder:")
(println (pe/generate-builder "rotary-set-velocity"))

;; Test 4: Validation
(println "\n4. Validate command parameters:")
(clojure.pprint/pprint 
  (pe/validate-command "rotary-goto-ndc" 
                      {:channel "heat" :x 0.5 :y -0.5}))

(println "\n=== Test Complete ===")
(System/exit 0)