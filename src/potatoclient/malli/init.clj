(ns potatoclient.malli.init
  "Early initialization of Malli registry for load-time availability.

   This namespace MUST be required before any namespace that uses m/=> declarations.
   It ensures all schemas are registered and available at load time.

   Usage:
   - Add (:require [potatoclient.malli.init]) as the FIRST require in any namespace using m/=>
   - Or use :prep-lib in deps.edn to ensure it loads first"
  (:require [potatoclient.malli.registry :as registry]))

;; Force immediate initialization at load time
(registry/setup-global-registry!)

;; Load and register all spec namespaces at load time
;; These must be loaded in dependency order
(require 'potatoclient.specs.common)       ; Common specs like :angle/magnetic-declination
(require 'potatoclient.ui-specs)          ; Basic UI specs, no dependencies
(require 'potatoclient.specs.cmd.root)    ; Command specs
(require 'potatoclient.specs.state.root)  ; State specs

;; Export a flag to verify initialization
(def initialized?
   "Flag indicating Malli registry and specs have been initialized."
  true)
