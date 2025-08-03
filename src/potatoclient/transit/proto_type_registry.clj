(ns potatoclient.transit.proto-type-registry
  "Registry that maps command domains to their protobuf Java class names.
  
  This uses the auto-generated mapping from proto-explorer to determine
  the correct protobuf type based on the command structure."
  (:require [potatoclient.specs.proto-type-mapping :as proto-mapping]))

;; Re-export the generated mapping for backward compatibility
(def domain->proto-type proto-mapping/domain->proto-type)

;; Re-export the infer function too
(def infer-proto-type proto-mapping/infer-proto-type)

(defn wrap-command-with-type
  "Wrap a command with its inferred proto type.
  
  If the proto type can't be inferred, returns the command unchanged
  with a warning in metadata."
  [command]
  (if-let [proto-type (infer-proto-type command)]
    (with-meta command {:proto-type proto-type})
    (with-meta command {:warning "Could not infer proto type"})))

;; =============================================================================
;; Simplified Send Command
;; =============================================================================

(defn prepare-command
  "Prepare a command for sending to Kotlin.
  
  Automatically infers the proto type from the command structure.
  No need to specify the spec or proto type explicitly!"
  [command]
  (let [proto-type (infer-proto-type command)]
    (if proto-type
      {:type :protobuf-command
       :proto-type proto-type
       :data command}
      (throw (ex-info "Could not infer proto type for command"
                      {:command command
                       :available-domains (keys domain->proto-type)})))))

;; =============================================================================
;; Usage Examples
;; =============================================================================

(comment
  ;; Simple root command
  (infer-proto-type {:ping {}})
  ;; => "cmd.JonSharedCmd$Root"
  
  ;; Domain-specific command
  (infer-proto-type {:rotary-platform {:goto {:azimuth 180.0}}})
  ;; => "cmd.RotaryPlatform.JonSharedCmdRotary$Root"
  
  ;; Prepare for sending
  (prepare-command {:cv {:start-track-ndc {:x 0.5 :y 0.5}}})
  ;; => {:type :protobuf-command
  ;;     :proto-type "cmd.CV.JonSharedCmdCv$Root"
  ;;     :data {:cv {:start-track-ndc {:x 0.5 :y 0.5}}}}
  )