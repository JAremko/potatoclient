(ns potatoclient.ui.bind-group
  "Grouped binding utilities that extend seesaw.bind with key-based management.
   Allows creating groups of bindings that can be cleaned up selectively."
  (:require
    [malli.core :as m]
    [seesaw.bind :as bind]))

(def ^:private binding-groups
  "Tracks all active binding groups. Structure:
   {atom-ref {group-key [unsub-fn ...]}}"
  (atom {}))

(defn bind-group
  "Creates a binding chain like seesaw.bind/bind, but associates it with a group key.
   This allows selective cleanup of bindings by group.
   
   Parameters:
     group-key - A keyword or other value to identify this group of bindings
     first-source - The source bindable (atom, property, etc.)
     target - The target bindable  
     & more - Additional targets in the chain
     
   Returns the composite bindable (like regular bind).
   
   Example:
     (bind-group :settings-panel my-atom (property label :text))
     ; Later, clean up just the settings panel bindings:
     (clean-group :settings-panel my-atom)"
  [group-key first-source target & more]
  (let [source-atom (when (instance? clojure.lang.IDeref first-source)
                      first-source)
        ; Create the binding using regular seesaw bind
        binding (apply bind/bind first-source target more)]

    ; Track this binding if source is an atom/ref/agent
    (when source-atom
      (swap! binding-groups update-in [source-atom group-key]
             (fnil conj []) binding))

    binding))

(defn clean-group
  "Removes all bindings associated with a specific group key for an atom.
   
   Parameters:
     group-key - The group key used when creating the bindings
     atom-ref - The atom/ref/agent that was the source of the bindings
     
   Returns the number of bindings cleaned up."
  [group-key atom-ref]
  (let [groups @binding-groups
        bindings-to-clean (get-in groups [atom-ref group-key] [])]

    ; Call each binding as a function to unsubscribe
    (doseq [binding bindings-to-clean]
      (when (ifn? binding)
        (binding)))

    ; Remove the group from tracking
    (swap! binding-groups update atom-ref dissoc group-key)

    ; Clean up empty entries
    (when (empty? (get @binding-groups atom-ref))
      (swap! binding-groups dissoc atom-ref))

    (count bindings-to-clean)))

(defn clean-all-groups
  "Removes all binding groups for a specific atom.
   
   Parameters:
     atom-ref - The atom/ref/agent to clean all groups for
     
   Returns the total number of bindings cleaned up."
  [atom-ref]
  (let [groups (get @binding-groups atom-ref {})
        group-keys (keys groups)]
    (reduce + 0 (map #(clean-group % atom-ref) group-keys))))

(defn list-groups
  "Returns a list of active group keys for an atom.
   
   Parameters:
     atom-ref - The atom/ref/agent to list groups for
     
   Returns a set of group keys."
  [atom-ref]
  (set (keys (get @binding-groups atom-ref {}))))

(defn group-count
  "Returns the number of bindings in a specific group.
   
   Parameters:
     group-key - The group key
     atom-ref - The atom/ref/agent
     
   Returns the count of bindings in the group."
  [group-key atom-ref]
  (count (get-in @binding-groups [atom-ref group-key] [])))

;; Convenience macros for common patterns

(defmacro with-binding-group
  "Creates a context where all bindings are associated with a temporary group.
   The group is automatically cleaned up when the body completes.
   
   Example:
     (with-binding-group [group-key my-atom]
       (bind-group group-key my-atom (property label1 :text))
       (bind-group group-key my-atom (property label2 :text))
       ; ... do something ...
       ) ; bindings are cleaned up here"
  [[group-key atom-ref] & body]
  `(let [group-key# ~group-key
         atom-ref# ~atom-ref]
     (try
       ~@body
       (finally
         (clean-group group-key# atom-ref#)))))

(defn bind-group-property
  "Convenience function to bind an atom to a widget property with a group key.
   
   Parameters:
     group-key - The group key  
     atom-ref - The source atom
     widget - The target widget
     property-name - The property keyword (e.g., :text, :enabled?)
     
   Example:
     (bind-group-property :panel-bindings my-atom label :text)"
  [group-key atom-ref widget property-name]
  (bind-group group-key atom-ref (bind/property widget property-name)))

(defn bind-group-selection
  "Convenience function to bind an atom to a widget selection with a group key.
   
   Parameters:
     group-key - The group key
     atom-ref - The source atom  
     widget - The target widget (checkbox, combobox, etc.)
     
   Example:
     (bind-group-selection :panel-bindings my-atom checkbox)"
  [group-key atom-ref widget]
  (bind-group group-key atom-ref (bind/selection widget)))

(defn bind-group-transform
  "Creates a grouped binding with a transformation function.
   
   Parameters:
     group-key - The group key
     atom-ref - The source atom
     transform-fn - Function to transform the value
     target - The target bindable
     
   Example:
     (bind-group-transform :panel-bindings 
                           my-atom 
                           #(str \"Value: \" %)
                           (property label :text))"
  [group-key atom-ref transform-fn target]
  (bind-group group-key atom-ref (bind/transform transform-fn) target))

;; Advanced features

(defn replace-group
  "Atomically replaces all bindings in a group with new ones.
   First cleans the existing group, then creates new bindings.
   
   Parameters:
     group-key - The group key
     atom-ref - The source atom
     new-bindings - A vector of [target & more] for each binding
     
   Example:
     (replace-group :panel-bindings my-atom
       [[(property label1 :text)]
        [(transform str) (property label2 :text)]])"
  [group-key atom-ref new-bindings]
  (clean-group group-key atom-ref)
  (doseq [binding-args new-bindings]
    (apply bind-group group-key atom-ref binding-args)))

(defn debug-groups
  "Returns debug information about all active binding groups.
   Useful for development and troubleshooting."
  []
  (let [groups @binding-groups]
    (into {}
          (for [[atom-ref group-map] groups]
            [atom-ref (into {}
                            (for [[group-key bindings] group-map]
                              [group-key (count bindings)]))]))))

;; Arrow specs for all functions
(m/=> bind-group [:function [:varargs [:cat :any :any :any] :any] :any])
(m/=> clean-group [:=> [:cat :any :any] :int])
(m/=> clean-all-groups [:=> [:cat :any] :int])
(m/=> list-groups [:=> [:cat :any] [:set :any]])
(m/=> group-count [:=> [:cat :any :any] :int])
(m/=> bind-group-property [:=> [:cat :any :any :any :keyword] :any])
(m/=> bind-group-selection [:=> [:cat :any :any :any] :any])
(m/=> bind-group-transform [:=> [:cat :any :any [:=> [:cat :any] :any] :any] :any])
(m/=> replace-group [:=> [:cat :any :any [:sequential [:sequential :any]]] :nil])
(m/=> debug-groups [:=> [:cat] [:map-of :any [:map-of :any :int]]])