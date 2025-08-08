(ns cmd-explorer.ts-parser
  "Parse TypeScript files to extract function signatures"
  (:require [instaparse.core :as insta]
            [clojure.string :as str]
            [clojure.java.io :as io]))

;; Simple regex-based parser (Instaparse is overkill for this)
(defn parse-function-signature
  "Parse a TypeScript function signature line"
  [line]
  (when-let [match (re-matches #"export\s+(async\s+)?function\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*\((.*?)\)\s*:?\s*([^{]*)\s*\{?" line)]
    (let [[_ async-part func-name params-str return-type] match]
      {:async? (boolean async-part)
       :name func-name
       :params (when-not (str/blank? params-str)
                (map str/trim (str/split params-str #",")))
       :return-type (if (str/blank? return-type)
                     "void"
                     (str/trim return-type))})))

(defn parse-ts-file
  "Parse a TypeScript file and extract function information"
  [file-path]
  (let [content (slurp file-path)
        ;; First pass - find all export function lines
        lines (str/split-lines content)
        functions (atom [])]
    
    ;; Process each line looking for export function
    (doseq [[idx line] (map-indexed vector lines)]
      (when (re-find #"^export\s+(async\s+)?function\s+" line)
        (when-let [parsed (parse-function-signature line)]
          (swap! functions conj
                 (assoc parsed
                        :line (inc idx)  ; 1-based line numbers
                        :signature line)))))
    
    @functions))

(defn extract-functions-from-directory
  "Extract all functions from TypeScript files in a directory"
  [dir-path]
  (let [cmd-dir (io/file dir-path "cmdSender")
        files (filter #(and (.isFile %)
                           (str/ends-with? (.getName %) ".ts")
                           (not= (.getName %) "cmdSenderShared.ts"))
                     (file-seq cmd-dir))]
    
    (reduce (fn [acc file]
              (let [rel-path (str "cmdSender/" (.getName file))
                    functions (parse-ts-file file)]
                (if (seq functions)
                  (assoc acc rel-path functions)
                  acc)))
            {}
            files)))

(defn function->kebab-case
  "Convert TypeScript function name to Clojure kebab-case"
  [ts-name]
  (-> ts-name
      ;; Handle acronyms first (GPS, LRF, OSD, CV, DDE)
      (str/replace #"GPS" "-gps-")
      (str/replace #"LRF" "-lrf-")
      (str/replace #"OSD" "-osd-")
      (str/replace #"CV" "-cv-")
      (str/replace #"DDE" "-dde-")
      ;; Insert hyphens before capitals
      (str/replace #"([a-z])([A-Z])" "$1-$2")
      ;; Convert to lowercase
      str/lower-case
      ;; Clean up multiple hyphens
      (str/replace #"-+" "-")
      ;; Remove leading/trailing hyphens
      (str/replace #"^-|-$" "")))

(defn infer-proto-message
  "Infer the protobuf message type from function name and file"
  [file-name func-name]
  (let [;; Extract just the filename from the path
        file-base (-> file-name
                     (str/replace #".*/" "")  ; Remove path prefix
                     (str/replace #"cmdSender/" ""))  ; Remove cmdSender/ if present
        module-map {"cmdDayCamera.ts" "DayCamera"
                    "cmdHeatCamera.ts" "HeatCamera"
                    "cmdRotary.ts" "RotaryPlatform"
                    "cmdLRF.ts" "Lrf"
                    "cmdLRFAlignment.ts" "Lrf_calib"
                    "cmdCompass.ts" "Compass"
                    "cmdGps.ts" "Gps"
                    "cmdSystem.ts" "System"
                    "cmdOSD.ts" "OSD"
                    "cmdCamDayGlassHeater.ts" "DayCamGlassHeater"
                    "cmdCV.ts" "CV"}
        base-module (get module-map file-base "Unknown")]
    
    ;; Infer specific message based on function name patterns
    ;; Note: These are approximations - actual TS may use different message types
    (cond
      ;; Camera controls
      (str/includes? func-name "Zoom") (str base-module ".Zoom")
      (str/includes? func-name "Focus") (str base-module ".Focus")
      (str/includes? func-name "Iris") (str base-module ".Iris")
      (str/includes? func-name "Filter") (str base-module ".SetFilters")
      
      ;; Rotary specific - more specific patterns
      (and (= base-module "RotaryPlatform")
           (str/includes? func-name "scanStart")) (str base-module ".ScanStart")
      (and (= base-module "RotaryPlatform")
           (str/includes? func-name "scanStop")) (str base-module ".ScanStop")
      (and (= base-module "RotaryPlatform")
           (str/includes? func-name "scanPrev")) (str base-module ".ScanPrev")
      (and (= base-module "RotaryPlatform")
           (str/includes? func-name "scanNext")) (str base-module ".ScanNext")
      (and (= base-module "RotaryPlatform")
           (str/includes? func-name "scanPause")) (str base-module ".ScanPause")
      (and (= base-module "RotaryPlatform")
           (str/includes? func-name "scanUnpause")) (str base-module ".ScanUnpause")
      (and (= base-module "RotaryPlatform")
           (str/includes? func-name "Azimuth")) (str base-module ".Azimuth")
      (and (= base-module "RotaryPlatform") 
           (str/includes? func-name "Elevation")) (str base-module ".Elevation")
      
      ;; LRF specific
      (and (= base-module "Lrf")
           (str/includes? func-name "Measure")) (str base-module ".Measure")
      (and (= base-module "Lrf")
           (str/includes? func-name "FogMode")) (str base-module ".FogMode")
      
      ;; Heat camera specific
      (and (= base-module "HeatCamera")
           (str/includes? func-name "Agc")) (str base-module ".SetAGC")
      (and (= base-module "HeatCamera")
           (str/includes? func-name "Filter")) (str base-module ".SetFilters")
      (str/includes? func-name "Dde") (str base-module ".Dde")
      (str/includes? func-name "Fx") (str base-module ".Fx")
      (str/includes? func-name "Clahe") (str base-module ".Clahe")
      
      ;; Default to Root - most commands actually use Root with nested messages
      :else (str base-module ".Root"))))

(defn generate-function-tasks
  "Generate TODO tasks for a function"
  [file-name {:keys [line name params return-type async?]}]
  (let [clj-name (function->kebab-case name)
        proto-msg (infer-proto-message file-name name)
        ;; Only add Root exploration if the message is nested (has a dot)
        root-msg (when (and (str/includes? proto-msg ".")
                           (not (str/ends-with? proto-msg ".Root")))
                  (str/replace proto-msg #"\..*" ".Root"))]
    {:name clj-name
     :ts-name name
     :line line
     :file file-name
     :proto-msg proto-msg
     :tasks (filter some?
                   [(format "Read TS source at %s:%d" file-name line)
                    (format "Proto-explore: bb spec %s" proto-msg)
                    (when root-msg
                      (format "Proto-explore: bb spec %s" root-msg))
                    "Document constraints from buf.validate"
                    "Define Malli spec with generators"
                    "Implement guardrailed function"
                    "Add unit tests (positive, negative, boundary)"
                    "Add generative tests (300+ runs)"
                    "Test with mock server"
                    "Verify binary compatibility"
                    "Ensure all tests pass"
                    (format "Commit: 'Implement %s with tests'" clj-name)
                    "Update todo.md - mark completed items"])}))

(defn group-functions-by-module
  "Group functions by their source module"
  [functions]
  (reduce (fn [acc func]
            (let [module (-> (:file func)
                           (str/replace #"cmdSender/" "")
                           (str/replace #"\.ts$" ""))]
              (update acc module (fnil conj []) func)))
          {}
          functions))

(defn extract-all-functions
  "Extract all functions and generate tasks"
  [ts-dir]
  (let [functions-by-file (extract-functions-from-directory ts-dir)]
    (mapcat (fn [[file funcs]]
              (map #(generate-function-tasks file %) funcs))
            functions-by-file)))