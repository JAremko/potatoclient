(ns proto-explorer.nested-mapping-design
  "Design for the generated static mapping file structure.
  
  The key insight: We traverse top-down, starting from a known root type,
  and at each level we know the parent's Java class, allowing us to
  disambiguate child keywords.")

;; Example of what the generated file should look like:

(comment
  ;; Generated file: proto_type_mapping_nested.clj
  
  (ns potatoclient.specs.proto-type-mapping-nested
    "Auto-generated nested proto type mappings.
    Maps Java protobuf classes to their child fields and types.")
  
  ;; Primary mapping: Java class -> keyword -> child Java class
  (def class->children
    {"cmd.JonSharedCmd$Root"
     {:ping "cmd.JonSharedCmd$Ping"
      :noop "cmd.JonSharedCmd$Noop"
      :rotary-platform "cmd.RotaryPlatform.JonSharedCmdRotary$Root"
      :cv "cmd.CV.JonSharedCmdCv$Root"
      :system "cmd.System.JonSharedCmdSystem$Root"}
     
     "cmd.RotaryPlatform.JonSharedCmdRotary$Root"
     {:start "cmd.RotaryPlatform.JonSharedCmdRotary$Start"
      :stop "cmd.RotaryPlatform.JonSharedCmdRotary$Stop"
      :axis "cmd.RotaryPlatform.JonSharedCmdRotary$Axis"
      :set-mode "cmd.RotaryPlatform.JonSharedCmdRotary$SetMode"
      :rotate-to-ndc "cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC"}
     
     "cmd.RotaryPlatform.JonSharedCmdRotary$Axis"
     {:azimuth "cmd.RotaryPlatform.JonSharedCmdRotary$Azimuth"
      :elevation "cmd.RotaryPlatform.JonSharedCmdRotary$Elevation"}
     
     "cmd.System.JonSharedCmdSystem$Root"
     {:start-rec "cmd.System.JonSharedCmdSystem$StartRec"
      :stop-rec "cmd.System.JonSharedCmdSystem$StopRec"
      :stop-a-ll "cmd.System.JonSharedCmdSystem$StopALl"}
     
     ;; ... many more entries
     })
  
  ;; Helper: Get child class for a keyword given parent class
  (defn get-child-class
    "Get the Java class for a child keyword given the parent class.
    Returns nil if not found."
    [parent-class child-keyword]
    (get-in class->children [parent-class child-keyword]))
  
  ;; Main enrichment function that works top-down
  (defn enrich-with-metadata
    "Recursively attach proto-type metadata to a command structure.
    Starts from a known root type and traverses top-down."
    ([data root-type]
     (with-meta data {:proto-type root-type}))
    ([data parent-type path]
     (cond
       ;; Map: enrich each key-value pair
       (map? data)
       (reduce-kv
         (fn [acc k v]
           (if-let [child-type (get-child-class parent-type k)]
             ;; Found the child type, recursively enrich
             (assoc acc k
                    (-> v
                        (enrich-with-metadata child-type (conj path k))
                        (with-meta {:proto-type child-type})))
             ;; No child type found, keep as-is
             (assoc acc k v)))
         {}
         data)
       
       ;; Vector/list: enrich each element with same parent type
       (sequential? data)
       (mapv #(enrich-with-metadata % parent-type path) data)
       
       ;; Leaf value: return as-is
       :else data)))
  
  ;; Entry point for command enrichment
  (defn enrich-command
    "Enrich a command with proto-type metadata.
    Automatically determines the root type from the command structure."
    [command]
    (let [root-type (cond
                      ;; Root commands like ping/noop
                      (or (:ping command) (:noop command))
                      "cmd.JonSharedCmd$Root"
                      
                      ;; Domain commands
                      (:rotary-platform command)
                      "cmd.JonSharedCmd$Root"
                      
                      (:cv command)
                      "cmd.JonSharedCmd$Root"
                      
                      ;; Add more root type detection as needed
                      :else
                      (throw (ex-info "Cannot determine root proto type"
                                      {:command command})))]
      (enrich-with-metadata command root-type []))))

;; Example usage:
(comment
  (def cmd {:rotary-platform {:start {}}})
  
  (enrich-command cmd)
  ;; =>
  ;; ^{:proto-type "cmd.JonSharedCmd$Root"}
  ;; {:rotary-platform
  ;;   ^{:proto-type "cmd.RotaryPlatform.JonSharedCmdRotary$Root"}
  ;;   {:start
  ;;     ^{:proto-type "cmd.RotaryPlatform.JonSharedCmdRotary$Start"}
  ;;     {}}}
  
  ;; The beauty: No ambiguity! 
  ;; We know :start is under :rotary-platform parent,
  ;; so we get the correct Start class
  )

;; Additional optimizations we could generate:

(comment
  ;; Set of leaf node classes (no children)
  (def leaf-classes
    #{"cmd.JonSharedCmd$Ping"
      "cmd.JonSharedCmd$Noop"
      "cmd.RotaryPlatform.JonSharedCmdRotary$Start"
      "cmd.RotaryPlatform.JonSharedCmdRotary$Stop"
      ;; ... etc
      })
  
  ;; Reverse mapping for validation: Java class -> parent classes
  (def class->parents
    {"cmd.RotaryPlatform.JonSharedCmdRotary$Start"
     #{"cmd.RotaryPlatform.JonSharedCmdRotary$Root"}
     
     "cmd.System.JonSharedCmdSystem$Start"  
     #{"cmd.System.JonSharedCmdSystem$Root"}
     ;; ... etc
     })
  
  ;; Field types for scalar values (not message types)
  (def class->scalar-fields
    {"cmd.RotaryPlatform.JonSharedCmdRotary$RotateToNDC"
     {:x :double
      :y :double
      :channel :int32}
     ;; ... etc
     })
  )