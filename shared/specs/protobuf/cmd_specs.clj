(ns protobuf.cmd-specs
  "Malli specifications for command (cmd.*) protobuf messages"
  (:require [malli.core :as m]
            [malli.generator :as mg]))

;; Generated from protobuf definitions
;; NOTE: These specs are automatically generated and may not include
;; all buf.validate constraints. Use as a structural baseline.

(def registry
  "Malli registry for protobuf messages"
  {
   :cmd/CV/DumpStart
   [:map {:closed true}]


   :cmd/CV/DumpStop
   [:map {:closed true}]


   :cmd/CV/Root
   [:and
 [:map
  {:closed true}
  [:set_auto_focus {:optional true} [:maybe [:ref :message]]]
  [:dump_stop {:optional true} [:maybe [:ref :message]]]
  [:vampire_mode_enable {:optional true} [:maybe [:ref :message]]]
  [:stabilization_mode_enable
   {:optional true}
   [:maybe [:ref :message]]]
  [:dump_start {:optional true} [:maybe [:ref :message]]]
  [:start_track_ndc {:optional true} [:maybe [:ref :message]]]
  [:stop_track {:optional true} [:maybe [:ref :message]]]
  [:stabilization_mode_disable
   {:optional true}
   [:maybe [:ref :message]]]
  [:vampire_mode_disable {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:set_auto_focus :start_track_ndc :stop_track :vampire_mode_enable :vampire_mode_disable :stabilization_mode_enable :stabilization_mode_disable :dump_start :dump_stop] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:set_auto_focus
        :start_track_ndc
        :stop_track
        :vampire_mode_enable
        :vampire_mode_disable
        :stabilization_mode_enable
        :stabilization_mode_disable
        :dump_start
        :dump_stop]))))]]]


   :cmd/CV/SetAutoFocus
   [:map
 {:closed true}
 [:channel {:optional true} [:maybe [:ref :enum]]]
 [:value {:optional true} [:maybe :boolean]]]


   :cmd/CV/StabilizationModeDisable
   [:map {:closed true}]


   :cmd/CV/StabilizationModeEnable
   [:map {:closed true}]


   :cmd/CV/StartTrackNDC
   [:map
 {:closed true}
 [:channel {:optional true} [:maybe [:ref :enum]]]
 [:x {:optional true} [:maybe :double]]
 [:y {:optional true} [:maybe :double]]
 [:frame_time {:optional true} [:maybe [:int {:min 0}]]]]


   :cmd/CV/StopTrack
   [:map {:closed true}]


   :cmd/CV/VampireModeDisable
   [:map {:closed true}]


   :cmd/CV/VampireModeEnable
   [:map {:closed true}]


   :cmd/Compass/CalibrateCencel
   [:map {:closed true}]


   :cmd/Compass/CalibrateNext
   [:map {:closed true}]


   :cmd/Compass/CalibrateStartLong
   [:map {:closed true}]


   :cmd/Compass/CalibrateStartShort
   [:map {:closed true}]


   :cmd/Compass/GetMeteo
   [:map {:closed true}]


   :cmd/Compass/Next
   [:map {:closed true}]


   :cmd/Compass/Root
   [:and
 [:map
  {:closed true}
  [:set_offset_angle_elevation
   {:optional true}
   [:maybe [:ref :message]]]
  [:set_magnetic_declination {:optional true} [:maybe [:ref :message]]]
  [:start_calibrate_long {:optional true} [:maybe [:ref :message]]]
  [:start {:optional true} [:maybe [:ref :message]]]
  [:calibrate_next {:optional true} [:maybe [:ref :message]]]
  [:calibrate_cencel {:optional true} [:maybe [:ref :message]]]
  [:start_calibrate_short {:optional true} [:maybe [:ref :message]]]
  [:stop {:optional true} [:maybe [:ref :message]]]
  [:set_offset_angle_azimuth {:optional true} [:maybe [:ref :message]]]
  [:set_use_rotary_position {:optional true} [:maybe [:ref :message]]]
  [:get_meteo {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:start :stop :set_magnetic_declination :set_offset_angle_azimuth :set_offset_angle_elevation :set_use_rotary_position :start_calibrate_long :start_calibrate_short :calibrate_next :calibrate_cencel :get_meteo] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:start
        :stop
        :set_magnetic_declination
        :set_offset_angle_azimuth
        :set_offset_angle_elevation
        :set_use_rotary_position
        :start_calibrate_long
        :start_calibrate_short
        :calibrate_next
        :calibrate_cencel
        :get_meteo]))))]]]


   :cmd/Compass/SetMagneticDeclination
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/Compass/SetOffsetAngleAzimuth
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/Compass/SetOffsetAngleElevation
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/Compass/SetUseRotaryPosition
   [:map {:closed true} [:flag {:optional true} [:maybe :boolean]]]


   :cmd/Compass/Start
   [:map {:closed true}]


   :cmd/Compass/Stop
   [:map {:closed true}]


   :cmd/DayCamGlassHeater/GetMeteo
   [:map {:closed true}]


   :cmd/DayCamGlassHeater/Root
   [:and
 [:map
  {:closed true}
  [:start {:optional true} [:maybe [:ref :message]]]
  [:stop {:optional true} [:maybe [:ref :message]]]
  [:turn_on {:optional true} [:maybe [:ref :message]]]
  [:turn_off {:optional true} [:maybe [:ref :message]]]
  [:get_meteo {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:start :stop :turn_on :turn_off :get_meteo] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:start :stop :turn_on :turn_off :get_meteo]))))]]]


   :cmd/DayCamGlassHeater/Start
   [:map {:closed true}]


   :cmd/DayCamGlassHeater/Stop
   [:map {:closed true}]


   :cmd/DayCamGlassHeater/TurnOff
   [:map {:closed true}]


   :cmd/DayCamGlassHeater/TurnOn
   [:map {:closed true}]


   :cmd/DayCamera/Focus
   [:and
 [:map
  {:closed true}
  [:set_value {:optional true} [:maybe [:ref :message]]]
  [:move {:optional true} [:maybe [:ref :message]]]
  [:halt {:optional true} [:maybe [:ref :message]]]
  [:offset {:optional true} [:maybe [:ref :message]]]
  [:reset_focus {:optional true} [:maybe [:ref :message]]]
  [:save_to_table_focus {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:set_value :move :halt :offset :reset_focus :save_to_table_focus] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:set_value
        :move
        :halt
        :offset
        :reset_focus
        :save_to_table_focus]))))]]]


   :cmd/DayCamera/GetMeteo
   [:map {:closed true}]


   :cmd/DayCamera/GetPos
   [:map {:closed true}]


   :cmd/DayCamera/Halt
   [:map {:closed true}]


   :cmd/DayCamera/HaltAll
   [:map {:closed true}]


   :cmd/DayCamera/Move
   [:map
 {:closed true}
 [:target_value {:optional true} [:maybe :double]]
 [:speed {:optional true} [:maybe :double]]]


   :cmd/DayCamera/NextFxMode
   [:map {:closed true}]


   :cmd/DayCamera/NextZoomTablePos
   [:map {:closed true}]


   :cmd/DayCamera/Offset
   [:map {:closed true} [:offset_value {:optional true} [:maybe :double]]]


   :cmd/DayCamera/Photo
   [:map {:closed true}]


   :cmd/DayCamera/PrevFxMode
   [:map {:closed true}]


   :cmd/DayCamera/PrevZoomTablePos
   [:map {:closed true}]


   :cmd/DayCamera/RefreshFxMode
   [:map {:closed true}]


   :cmd/DayCamera/ResetFocus
   [:map {:closed true}]


   :cmd/DayCamera/ResetZoom
   [:map {:closed true}]


   :cmd/DayCamera/Root
   [:and
 [:map
  {:closed true}
  [:refresh_fx_mode {:optional true} [:maybe [:ref :message]]]
  [:zoom {:optional true} [:maybe [:ref :message]]]
  [:set_fx_mode {:optional true} [:maybe [:ref :message]]]
  [:halt_all {:optional true} [:maybe [:ref :message]]]
  [:set_clahe_level {:optional true} [:maybe [:ref :message]]]
  [:set_infra_red_filter {:optional true} [:maybe [:ref :message]]]
  [:start {:optional true} [:maybe [:ref :message]]]
  [:shift_clahe_level {:optional true} [:maybe [:ref :message]]]
  [:stop {:optional true} [:maybe [:ref :message]]]
  [:photo {:optional true} [:maybe [:ref :message]]]
  [:get_meteo {:optional true} [:maybe [:ref :message]]]
  [:prev_fx_mode {:optional true} [:maybe [:ref :message]]]
  [:focus {:optional true} [:maybe [:ref :message]]]
  [:set_iris {:optional true} [:maybe [:ref :message]]]
  [:set_auto_iris {:optional true} [:maybe [:ref :message]]]
  [:next_fx_mode {:optional true} [:maybe [:ref :message]]]
  [:set_digital_zoom_level {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:focus :zoom :set_iris :set_infra_red_filter :start :stop :photo :set_auto_iris :halt_all :set_fx_mode :next_fx_mode :prev_fx_mode :get_meteo :refresh_fx_mode :set_digital_zoom_level :set_clahe_level :shift_clahe_level] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:focus
        :zoom
        :set_iris
        :set_infra_red_filter
        :start
        :stop
        :photo
        :set_auto_iris
        :halt_all
        :set_fx_mode
        :next_fx_mode
        :prev_fx_mode
        :get_meteo
        :refresh_fx_mode
        :set_digital_zoom_level
        :set_clahe_level
        :shift_clahe_level]))))]]]


   :cmd/DayCamera/SaveToTable
   [:map {:closed true}]


   :cmd/DayCamera/SaveToTableFocus
   [:map {:closed true}]


   :cmd/DayCamera/SetAutoIris
   [:map {:closed true} [:value {:optional true} [:maybe :boolean]]]


   :cmd/DayCamera/SetClaheLevel
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/DayCamera/SetDigitalZoomLevel
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/DayCamera/SetFxMode
   [:map {:closed true} [:mode {:optional true} [:maybe [:ref :enum]]]]


   :cmd/DayCamera/SetInfraRedFilter
   [:map {:closed true} [:value {:optional true} [:maybe :boolean]]]


   :cmd/DayCamera/SetIris
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/DayCamera/SetValue
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/DayCamera/SetZoomTableValue
   [:map {:closed true} [:value {:optional true} [:maybe :int]]]


   :cmd/DayCamera/ShiftClaheLevel
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/DayCamera/Start
   [:map {:closed true}]


   :cmd/DayCamera/Stop
   [:map {:closed true}]


   :cmd/DayCamera/Zoom
   [:and
 [:map
  {:closed true}
  [:reset_zoom {:optional true} [:maybe [:ref :message]]]
  [:offset {:optional true} [:maybe [:ref :message]]]
  [:set_zoom_table_value {:optional true} [:maybe [:ref :message]]]
  [:move {:optional true} [:maybe [:ref :message]]]
  [:set_value {:optional true} [:maybe [:ref :message]]]
  [:halt {:optional true} [:maybe [:ref :message]]]
  [:prev_zoom_table_pos {:optional true} [:maybe [:ref :message]]]
  [:next_zoom_table_pos {:optional true} [:maybe [:ref :message]]]
  [:save_to_table {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:set_value :move :halt :set_zoom_table_value :next_zoom_table_pos :prev_zoom_table_pos :offset :reset_zoom :save_to_table] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:set_value
        :move
        :halt
        :set_zoom_table_value
        :next_zoom_table_pos
        :prev_zoom_table_pos
        :offset
        :reset_zoom
        :save_to_table]))))]]]


   :cmd/Frozen
   [:map {:closed true}]


   :cmd/Gps/GetMeteo
   [:map {:closed true}]


   :cmd/Gps/Root
   [:and
 [:map
  {:closed true}
  [:start {:optional true} [:maybe [:ref :message]]]
  [:stop {:optional true} [:maybe [:ref :message]]]
  [:set_manual_position {:optional true} [:maybe [:ref :message]]]
  [:set_use_manual_position {:optional true} [:maybe [:ref :message]]]
  [:get_meteo {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:start :stop :set_manual_position :set_use_manual_position :get_meteo] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:start
        :stop
        :set_manual_position
        :set_use_manual_position
        :get_meteo]))))]]]


   :cmd/Gps/SetManualPosition
   [:map
 {:closed true}
 [:latitude {:optional true} [:maybe :double]]
 [:longitude {:optional true} [:maybe :double]]
 [:altitude {:optional true} [:maybe :double]]]


   :cmd/Gps/SetUseManualPosition
   [:map {:closed true} [:flag {:optional true} [:maybe :boolean]]]


   :cmd/Gps/Start
   [:map {:closed true}]


   :cmd/Gps/Stop
   [:map {:closed true}]


   :cmd/HeatCamera/Calibrate
   [:map {:closed true}]


   :cmd/HeatCamera/DisableDDE
   [:map {:closed true}]


   :cmd/HeatCamera/EnableDDE
   [:map {:closed true}]


   :cmd/HeatCamera/FocusIn
   [:map {:closed true}]


   :cmd/HeatCamera/FocusOut
   [:map {:closed true}]


   :cmd/HeatCamera/FocusStepMinus
   [:map {:closed true}]


   :cmd/HeatCamera/FocusStepPlus
   [:map {:closed true}]


   :cmd/HeatCamera/FocusStop
   [:map {:closed true}]


   :cmd/HeatCamera/GetMeteo
   [:map {:closed true}]


   :cmd/HeatCamera/Halt
   [:map {:closed true}]


   :cmd/HeatCamera/NextFxMode
   [:map {:closed true}]


   :cmd/HeatCamera/NextZoomTablePos
   [:map {:closed true}]


   :cmd/HeatCamera/Photo
   [:map {:closed true}]


   :cmd/HeatCamera/PrevFxMode
   [:map {:closed true}]


   :cmd/HeatCamera/PrevZoomTablePos
   [:map {:closed true}]


   :cmd/HeatCamera/RefreshFxMode
   [:map {:closed true}]


   :cmd/HeatCamera/ResetZoom
   [:map {:closed true}]


   :cmd/HeatCamera/Root
   [:and
 [:map
  {:closed true}
  [:refresh_fx_mode {:optional true} [:maybe [:ref :message]]]
  [:reset_zoom {:optional true} [:maybe [:ref :message]]]
  [:set_calib_mode {:optional true} [:maybe [:ref :message]]]
  [:focus_in {:optional true} [:maybe [:ref :message]]]
  [:zoom {:optional true} [:maybe [:ref :message]]]
  [:set_fx_mode {:optional true} [:maybe [:ref :message]]]
  [:set_clahe_level {:optional true} [:maybe [:ref :message]]]
  [:set_filter {:optional true} [:maybe [:ref :message]]]
  [:focus_step_plus {:optional true} [:maybe [:ref :message]]]
  [:set_auto_focus {:optional true} [:maybe [:ref :message]]]
  [:start {:optional true} [:maybe [:ref :message]]]
  [:shift_clahe_level {:optional true} [:maybe [:ref :message]]]
  [:focus_step_minus {:optional true} [:maybe [:ref :message]]]
  [:shift_dde {:optional true} [:maybe [:ref :message]]]
  [:stop {:optional true} [:maybe [:ref :message]]]
  [:photo {:optional true} [:maybe [:ref :message]]]
  [:get_meteo {:optional true} [:maybe [:ref :message]]]
  [:prev_fx_mode {:optional true} [:maybe [:ref :message]]]
  [:zoom_stop {:optional true} [:maybe [:ref :message]]]
  [:focus_out {:optional true} [:maybe [:ref :message]]]
  [:enable_dde {:optional true} [:maybe [:ref :message]]]
  [:focus_stop {:optional true} [:maybe [:ref :message]]]
  [:set_dde_level {:optional true} [:maybe [:ref :message]]]
  [:set_agc {:optional true} [:maybe [:ref :message]]]
  [:zoom_out {:optional true} [:maybe [:ref :message]]]
  [:calibrate {:optional true} [:maybe [:ref :message]]]
  [:next_fx_mode {:optional true} [:maybe [:ref :message]]]
  [:disable_dde {:optional true} [:maybe [:ref :message]]]
  [:save_to_table {:optional true} [:maybe [:ref :message]]]
  [:zoom_in {:optional true} [:maybe [:ref :message]]]
  [:set_digital_zoom_level {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:zoom :set_agc :set_filter :start :stop :photo :zoom_in :zoom_out :zoom_stop :focus_in :focus_out :focus_stop :calibrate :set_dde_level :enable_dde :disable_dde :set_auto_focus :focus_step_plus :focus_step_minus :set_fx_mode :next_fx_mode :prev_fx_mode :get_meteo :shift_dde :refresh_fx_mode :reset_zoom :save_to_table :set_calib_mode :set_digital_zoom_level :set_clahe_level :shift_clahe_level] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:zoom
        :set_agc
        :set_filter
        :start
        :stop
        :photo
        :zoom_in
        :zoom_out
        :zoom_stop
        :focus_in
        :focus_out
        :focus_stop
        :calibrate
        :set_dde_level
        :enable_dde
        :disable_dde
        :set_auto_focus
        :focus_step_plus
        :focus_step_minus
        :set_fx_mode
        :next_fx_mode
        :prev_fx_mode
        :get_meteo
        :shift_dde
        :refresh_fx_mode
        :reset_zoom
        :save_to_table
        :set_calib_mode
        :set_digital_zoom_level
        :set_clahe_level
        :shift_clahe_level]))))]]]


   :cmd/HeatCamera/SaveToTable
   [:map {:closed true}]


   :cmd/HeatCamera/SetAGC
   [:map {:closed true} [:value {:optional true} [:maybe [:ref :enum]]]]


   :cmd/HeatCamera/SetAutoFocus
   [:map {:closed true} [:value {:optional true} [:maybe :boolean]]]


   :cmd/HeatCamera/SetCalibMode
   [:map {:closed true}]


   :cmd/HeatCamera/SetClaheLevel
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/HeatCamera/SetDDELevel
   [:map {:closed true} [:value {:optional true} [:maybe :int]]]


   :cmd/HeatCamera/SetDigitalZoomLevel
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/HeatCamera/SetFilters
   [:map {:closed true} [:value {:optional true} [:maybe [:ref :enum]]]]


   :cmd/HeatCamera/SetFxMode
   [:map {:closed true} [:mode {:optional true} [:maybe [:ref :enum]]]]


   :cmd/HeatCamera/SetValue
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/HeatCamera/SetZoomTableValue
   [:map {:closed true} [:value {:optional true} [:maybe :int]]]


   :cmd/HeatCamera/ShiftClaheLevel
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/HeatCamera/ShiftDDE
   [:map {:closed true} [:value {:optional true} [:maybe :int]]]


   :cmd/HeatCamera/Start
   [:map {:closed true}]


   :cmd/HeatCamera/Stop
   [:map {:closed true}]


   :cmd/HeatCamera/Zoom
   [:and
 [:map
  {:closed true}
  [:set_zoom_table_value {:optional true} [:maybe [:ref :message]]]
  [:next_zoom_table_pos {:optional true} [:maybe [:ref :message]]]
  [:prev_zoom_table_pos {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:set_zoom_table_value :next_zoom_table_pos :prev_zoom_table_pos] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:set_zoom_table_value
        :next_zoom_table_pos
        :prev_zoom_table_pos]))))]]]


   :cmd/HeatCamera/ZoomIn
   [:map {:closed true}]


   :cmd/HeatCamera/ZoomOut
   [:map {:closed true}]


   :cmd/HeatCamera/ZoomStop
   [:map {:closed true}]


   :cmd/Lira/JonGuiDataLiraTarget
   [:map
 {:closed true}
 [:target_elevation {:optional true} [:maybe :double]]
 [:uuid_part1 {:optional true} [:maybe :int]]
 [:target_azimuth {:optional true} [:maybe :double]]
 [:uuid_part3 {:optional true} [:maybe :int]]
 [:target_longitude {:optional true} [:maybe :double]]
 [:target_altitude {:optional true} [:maybe :double]]
 [:target_latitude {:optional true} [:maybe :double]]
 [:uuid_part4 {:optional true} [:maybe :int]]
 [:distance {:optional true} [:maybe :double]]
 [:timestamp {:optional true} [:maybe :int]]
 [:uuid_part2 {:optional true} [:maybe :int]]]


   :cmd/Lira/Refine_target
   [:map
 {:closed true}
 [:target {:optional true} [:maybe [:ref :message]]]]


   :cmd/Lira/Root
   [:and
 [:map
  {:closed true}
  [:refine_target {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message "Exactly one of [:refine_target] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:refine_target]))))]]]


   :cmd/Lrf/DisableFogMode
   [:map {:closed true}]


   :cmd/Lrf/EnableFogMode
   [:map {:closed true}]


   :cmd/Lrf/GetMeteo
   [:map {:closed true}]


   :cmd/Lrf/Measure
   [:map {:closed true}]


   :cmd/Lrf/NewSession
   [:map {:closed true}]


   :cmd/Lrf/RefineOff
   [:map {:closed true}]


   :cmd/Lrf/RefineOn
   [:map {:closed true}]


   :cmd/Lrf/Root
   [:and
 [:map
  {:closed true}
  [:refine_on {:optional true} [:maybe [:ref :message]]]
  [:scan_on {:optional true} [:maybe [:ref :message]]]
  [:start {:optional true} [:maybe [:ref :message]]]
  [:scan_off {:optional true} [:maybe [:ref :message]]]
  [:measure {:optional true} [:maybe [:ref :message]]]
  [:stop {:optional true} [:maybe [:ref :message]]]
  [:set_scan_mode {:optional true} [:maybe [:ref :message]]]
  [:refine_off {:optional true} [:maybe [:ref :message]]]
  [:target_designator_on_mode_a
   {:optional true}
   [:maybe [:ref :message]]]
  [:get_meteo {:optional true} [:maybe [:ref :message]]]
  [:disable_fog_mode {:optional true} [:maybe [:ref :message]]]
  [:enable_fog_mode {:optional true} [:maybe [:ref :message]]]
  [:target_designator_off {:optional true} [:maybe [:ref :message]]]
  [:new_session {:optional true} [:maybe [:ref :message]]]
  [:target_designator_on_mode_b
   {:optional true}
   [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:measure :scan_on :scan_off :start :stop :target_designator_off :target_designator_on_mode_a :target_designator_on_mode_b :enable_fog_mode :disable_fog_mode :set_scan_mode :new_session :get_meteo :refine_on :refine_off] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:measure
        :scan_on
        :scan_off
        :start
        :stop
        :target_designator_off
        :target_designator_on_mode_a
        :target_designator_on_mode_b
        :enable_fog_mode
        :disable_fog_mode
        :set_scan_mode
        :new_session
        :get_meteo
        :refine_on
        :refine_off]))))]]]


   :cmd/Lrf/ScanOff
   [:map {:closed true}]


   :cmd/Lrf/ScanOn
   [:map {:closed true}]


   :cmd/Lrf/SetScanMode
   [:map {:closed true} [:mode {:optional true} [:maybe [:ref :enum]]]]


   :cmd/Lrf/Start
   [:map {:closed true}]


   :cmd/Lrf/Stop
   [:map {:closed true}]


   :cmd/Lrf/TargetDesignatorOff
   [:map {:closed true}]


   :cmd/Lrf/TargetDesignatorOnModeA
   [:map {:closed true}]


   :cmd/Lrf/TargetDesignatorOnModeB
   [:map {:closed true}]


   :cmd/Lrf_calib/Offsets
   [:and
 [:map
  {:closed true}
  [:set {:optional true} [:maybe [:ref :message]]]
  [:save {:optional true} [:maybe [:ref :message]]]
  [:reset {:optional true} [:maybe [:ref :message]]]
  [:shift {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:set :save :reset :shift] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:set :save :reset :shift]))))]]]


   :cmd/Lrf_calib/ResetOffsets
   [:map {:closed true}]


   :cmd/Lrf_calib/Root
   [:and
 [:map
  {:closed true}
  [:day {:optional true} [:maybe [:ref :message]]]
  [:heat {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message "Exactly one of [:day :heat] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:day :heat]))))]]]


   :cmd/Lrf_calib/SaveOffsets
   [:map {:closed true}]


   :cmd/Lrf_calib/SetOffsets
   [:map
 {:closed true}
 [:x {:optional true} [:maybe :int]]
 [:y {:optional true} [:maybe :int]]]


   :cmd/Lrf_calib/ShiftOffsetsBy
   [:map
 {:closed true}
 [:x {:optional true} [:maybe :int]]
 [:y {:optional true} [:maybe :int]]]


   :cmd/Noop
   [:map {:closed true}]


   :cmd/OSD/DisableDayOSD
   [:map {:closed true}]


   :cmd/OSD/DisableHeatOSD
   [:map {:closed true}]


   :cmd/OSD/EnableDayOSD
   [:map {:closed true}]


   :cmd/OSD/EnableHeatOSD
   [:map {:closed true}]


   :cmd/OSD/Root
   [:and
 [:map
  {:closed true}
  [:show_default_screen {:optional true} [:maybe [:ref :message]]]
  [:show_lrf_measure_screen {:optional true} [:maybe [:ref :message]]]
  [:show_lrf_result_screen {:optional true} [:maybe [:ref :message]]]
  [:show_lrf_result_simplified_screen
   {:optional true}
   [:maybe [:ref :message]]]
  [:enable_heat_osd {:optional true} [:maybe [:ref :message]]]
  [:disable_heat_osd {:optional true} [:maybe [:ref :message]]]
  [:enable_day_osd {:optional true} [:maybe [:ref :message]]]
  [:disable_day_osd {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:show_default_screen :show_lrf_measure_screen :show_lrf_result_screen :show_lrf_result_simplified_screen :enable_heat_osd :disable_heat_osd :enable_day_osd :disable_day_osd] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:show_default_screen
        :show_lrf_measure_screen
        :show_lrf_result_screen
        :show_lrf_result_simplified_screen
        :enable_heat_osd
        :disable_heat_osd
        :enable_day_osd
        :disable_day_osd]))))]]]


   :cmd/OSD/ShowDefaultScreen
   [:map {:closed true}]


   :cmd/OSD/ShowLRFMeasureScreen
   [:map {:closed true}]


   :cmd/OSD/ShowLRFResultScreen
   [:map {:closed true}]


   :cmd/OSD/ShowLRFResultSimplifiedScreen
   [:map {:closed true}]


   :cmd/Ping
   [:map {:closed true}]


   :cmd/Root
   [:and
 [:map
  {:closed true}
  [:osd {:optional true} [:maybe [:ref :message]]]
  [:ping {:optional true} [:maybe [:ref :message]]]
  [:client_type {:optional true} [:maybe [:ref :enum]]]
  [:system {:optional true} [:maybe [:ref :message]]]
  [:noop {:optional true} [:maybe [:ref :message]]]
  [:cv {:optional true} [:maybe [:ref :message]]]
  [:gps {:optional true} [:maybe [:ref :message]]]
  [:session_id {:optional true} [:maybe [:int {:min 0}]]]
  [:important {:optional true} [:maybe :boolean]]
  [:lrf {:optional true} [:maybe [:ref :message]]]
  [:lira {:optional true} [:maybe [:ref :message]]]
  [:lrf_calib {:optional true} [:maybe [:ref :message]]]
  [:rotary {:optional true} [:maybe [:ref :message]]]
  [:day_cam_glass_heater {:optional true} [:maybe [:ref :message]]]
  [:heat_camera {:optional true} [:maybe [:ref :message]]]
  [:compass {:optional true} [:maybe [:ref :message]]]
  [:day_camera {:optional true} [:maybe [:ref :message]]]
  [:from_cv_subsystem {:optional true} [:maybe :boolean]]
  [:frozen {:optional true} [:maybe [:ref :message]]]
  [:protocol_version {:optional true} [:maybe [:int {:min 0}]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:day_camera :heat_camera :gps :compass :lrf :lrf_calib :rotary :osd :ping :noop :frozen :system :cv :day_cam_glass_heater :lira] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:day_camera
        :heat_camera
        :gps
        :compass
        :lrf
        :lrf_calib
        :rotary
        :osd
        :ping
        :noop
        :frozen
        :system
        :cv
        :day_cam_glass_heater
        :lira]))))]]]


   :cmd/RotaryPlatform/Axis
   [:map
 {:closed true}
 [:azimuth {:optional true} [:maybe [:ref :message]]]
 [:elevation {:optional true} [:maybe [:ref :message]]]]


   :cmd/RotaryPlatform/Azimuth
   [:and
 [:map
  {:closed true}
  [:set_value {:optional true} [:maybe [:ref :message]]]
  [:rotate_to {:optional true} [:maybe [:ref :message]]]
  [:rotate {:optional true} [:maybe [:ref :message]]]
  [:relative {:optional true} [:maybe [:ref :message]]]
  [:relative_set {:optional true} [:maybe [:ref :message]]]
  [:halt {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:set_value :rotate_to :rotate :relative :relative_set :halt] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:set_value
        :rotate_to
        :rotate
        :relative
        :relative_set
        :halt]))))]]]


   :cmd/RotaryPlatform/Elevation
   [:and
 [:map
  {:closed true}
  [:set_value {:optional true} [:maybe [:ref :message]]]
  [:rotate_to {:optional true} [:maybe [:ref :message]]]
  [:rotate {:optional true} [:maybe [:ref :message]]]
  [:relative {:optional true} [:maybe [:ref :message]]]
  [:relative_set {:optional true} [:maybe [:ref :message]]]
  [:halt {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:set_value :rotate_to :rotate :relative :relative_set :halt] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:set_value
        :rotate_to
        :rotate
        :relative
        :relative_set
        :halt]))))]]]


   :cmd/RotaryPlatform/GetMeteo
   [:map {:closed true}]


   :cmd/RotaryPlatform/Halt
   [:map {:closed true}]


   :cmd/RotaryPlatform/HaltAzimuth
   [:map {:closed true}]


   :cmd/RotaryPlatform/HaltElevation
   [:map {:closed true}]


   :cmd/RotaryPlatform/Root
   [:and
 [:map
  {:closed true}
  [:scan_prev {:optional true} [:maybe [:ref :message]]]
  [:set_platform_bank {:optional true} [:maybe [:ref :message]]]
  [:rotate_to_gps {:optional true} [:maybe [:ref :message]]]
  [:set_mode {:optional true} [:maybe [:ref :message]]]
  [:scan_pause {:optional true} [:maybe [:ref :message]]]
  [:set_origin_gps {:optional true} [:maybe [:ref :message]]]
  [:scan_add_node {:optional true} [:maybe [:ref :message]]]
  [:scan_next {:optional true} [:maybe [:ref :message]]]
  [:scan_stop {:optional true} [:maybe [:ref :message]]]
  [:start {:optional true} [:maybe [:ref :message]]]
  [:rotate_to_ndc {:optional true} [:maybe [:ref :message]]]
  [:set_use_rotary_as_compass
   {:optional true}
   [:maybe [:ref :message]]]
  [:scan_start {:optional true} [:maybe [:ref :message]]]
  [:stop {:optional true} [:maybe [:ref :message]]]
  [:scan_select_node {:optional true} [:maybe [:ref :message]]]
  [:get_meteo {:optional true} [:maybe [:ref :message]]]
  [:scan_delete_node {:optional true} [:maybe [:ref :message]]]
  [:scan_update_node {:optional true} [:maybe [:ref :message]]]
  [:set_platform_elevation {:optional true} [:maybe [:ref :message]]]
  [:halt {:optional true} [:maybe [:ref :message]]]
  [:axis {:optional true} [:maybe [:ref :message]]]
  [:scan_unpause {:optional true} [:maybe [:ref :message]]]
  [:set_platform_azimuth {:optional true} [:maybe [:ref :message]]]
  [:scan_refresh_node_list {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:start :stop :axis :set_platform_azimuth :set_platform_elevation :set_platform_bank :halt :set_use_rotary_as_compass :rotate_to_gps :set_origin_gps :set_mode :rotate_to_ndc :scan_start :scan_stop :scan_pause :scan_unpause :get_meteo :scan_prev :scan_next :scan_refresh_node_list :scan_select_node :scan_delete_node :scan_update_node :scan_add_node] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:start
        :stop
        :axis
        :set_platform_azimuth
        :set_platform_elevation
        :set_platform_bank
        :halt
        :set_use_rotary_as_compass
        :rotate_to_gps
        :set_origin_gps
        :set_mode
        :rotate_to_ndc
        :scan_start
        :scan_stop
        :scan_pause
        :scan_unpause
        :get_meteo
        :scan_prev
        :scan_next
        :scan_refresh_node_list
        :scan_select_node
        :scan_delete_node
        :scan_update_node
        :scan_add_node]))))]]]


   :cmd/RotaryPlatform/RotateAzimuth
   [:map
 {:closed true}
 [:speed {:optional true} [:maybe :double]]
 [:direction {:optional true} [:maybe [:ref :enum]]]]


   :cmd/RotaryPlatform/RotateAzimuthRelative
   [:map
 {:closed true}
 [:value {:optional true} [:maybe :double]]
 [:speed {:optional true} [:maybe :double]]
 [:direction {:optional true} [:maybe [:ref :enum]]]]


   :cmd/RotaryPlatform/RotateAzimuthRelativeSet
   [:map
 {:closed true}
 [:value {:optional true} [:maybe :double]]
 [:direction {:optional true} [:maybe [:ref :enum]]]]


   :cmd/RotaryPlatform/RotateAzimuthTo
   [:map
 {:closed true}
 [:target_value {:optional true} [:maybe :double]]
 [:speed {:optional true} [:maybe :double]]
 [:direction {:optional true} [:maybe [:ref :enum]]]]


   :cmd/RotaryPlatform/RotateElevation
   [:map
 {:closed true}
 [:speed {:optional true} [:maybe :double]]
 [:direction {:optional true} [:maybe [:ref :enum]]]]


   :cmd/RotaryPlatform/RotateElevationRelative
   [:map
 {:closed true}
 [:value {:optional true} [:maybe :double]]
 [:speed {:optional true} [:maybe :double]]
 [:direction {:optional true} [:maybe [:ref :enum]]]]


   :cmd/RotaryPlatform/RotateElevationRelativeSet
   [:map
 {:closed true}
 [:value {:optional true} [:maybe :double]]
 [:direction {:optional true} [:maybe [:ref :enum]]]]


   :cmd/RotaryPlatform/RotateElevationTo
   [:map
 {:closed true}
 [:target_value {:optional true} [:maybe :double]]
 [:speed {:optional true} [:maybe :double]]]


   :cmd/RotaryPlatform/RotateToGPS
   [:map
 {:closed true}
 [:latitude {:optional true} [:maybe :double]]
 [:longitude {:optional true} [:maybe :double]]
 [:altitude {:optional true} [:maybe :double]]]


   :cmd/RotaryPlatform/RotateToNDC
   [:map
 {:closed true}
 [:channel {:optional true} [:maybe [:ref :enum]]]
 [:x {:optional true} [:maybe :double]]
 [:y {:optional true} [:maybe :double]]]


   :cmd/RotaryPlatform/ScanAddNode
   [:map
 {:closed true}
 [:index {:optional true} [:maybe :int]]
 [:DayZoomTableValue {:optional true} [:maybe :int]]
 [:HeatZoomTableValue {:optional true} [:maybe :int]]
 [:azimuth {:optional true} [:maybe :double]]
 [:elevation {:optional true} [:maybe :double]]
 [:linger {:optional true} [:maybe :double]]
 [:speed {:optional true} [:maybe :double]]]


   :cmd/RotaryPlatform/ScanDeleteNode
   [:map {:closed true} [:index {:optional true} [:maybe :int]]]


   :cmd/RotaryPlatform/ScanNext
   [:map {:closed true}]


   :cmd/RotaryPlatform/ScanPause
   [:map {:closed true}]


   :cmd/RotaryPlatform/ScanPrev
   [:map {:closed true}]


   :cmd/RotaryPlatform/ScanRefreshNodeList
   [:map {:closed true}]


   :cmd/RotaryPlatform/ScanSelectNode
   [:map {:closed true} [:index {:optional true} [:maybe :int]]]


   :cmd/RotaryPlatform/ScanStart
   [:map {:closed true}]


   :cmd/RotaryPlatform/ScanStop
   [:map {:closed true}]


   :cmd/RotaryPlatform/ScanUnpause
   [:map {:closed true}]


   :cmd/RotaryPlatform/ScanUpdateNode
   [:map
 {:closed true}
 [:index {:optional true} [:maybe :int]]
 [:DayZoomTableValue {:optional true} [:maybe :int]]
 [:HeatZoomTableValue {:optional true} [:maybe :int]]
 [:azimuth {:optional true} [:maybe :double]]
 [:elevation {:optional true} [:maybe :double]]
 [:linger {:optional true} [:maybe :double]]
 [:speed {:optional true} [:maybe :double]]]


   :cmd/RotaryPlatform/SetAzimuthValue
   [:map
 {:closed true}
 [:value {:optional true} [:maybe :double]]
 [:direction {:optional true} [:maybe [:ref :enum]]]]


   :cmd/RotaryPlatform/SetElevationValue
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/RotaryPlatform/SetMode
   [:map {:closed true} [:mode {:optional true} [:maybe [:ref :enum]]]]


   :cmd/RotaryPlatform/SetOriginGPS
   [:map
 {:closed true}
 [:latitude {:optional true} [:maybe :double]]
 [:longitude {:optional true} [:maybe :double]]
 [:altitude {:optional true} [:maybe :double]]]


   :cmd/RotaryPlatform/SetPlatformAzimuth
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/RotaryPlatform/SetPlatformBank
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/RotaryPlatform/SetPlatformElevation
   [:map {:closed true} [:value {:optional true} [:maybe :double]]]


   :cmd/RotaryPlatform/Start
   [:map {:closed true}]


   :cmd/RotaryPlatform/Stop
   [:map {:closed true}]


   :cmd/RotaryPlatform/setUseRotaryAsCompass
   [:map {:closed true} [:flag {:optional true} [:maybe :boolean]]]


   :cmd/System/DisableGeodesicMode
   [:map {:closed true}]


   :cmd/System/EnableGeodesicMode
   [:map {:closed true}]


   :cmd/System/EnterTransport
   [:map {:closed true}]


   :cmd/System/MarkRecImportant
   [:map {:closed true}]


   :cmd/System/PowerOff
   [:map {:closed true}]


   :cmd/System/Reboot
   [:map {:closed true}]


   :cmd/System/ResetConfigs
   [:map {:closed true}]


   :cmd/System/Root
   [:and
 [:map
  {:closed true}
  [:geodesic_mode_disable {:optional true} [:maybe [:ref :message]]]
  [:stop_rec {:optional true} [:maybe [:ref :message]]]
  [:stop_all {:optional true} [:maybe [:ref :message]]]
  [:power_off {:optional true} [:maybe [:ref :message]]]
  [:localization {:optional true} [:maybe [:ref :message]]]
  [:geodesic_mode_enable {:optional true} [:maybe [:ref :message]]]
  [:reset_configs {:optional true} [:maybe [:ref :message]]]
  [:start_all {:optional true} [:maybe [:ref :message]]]
  [:mark_rec_important {:optional true} [:maybe [:ref :message]]]
  [:reboot {:optional true} [:maybe [:ref :message]]]
  [:unmark_rec_important {:optional true} [:maybe [:ref :message]]]
  [:start_rec {:optional true} [:maybe [:ref :message]]]
  [:enter_transport {:optional true} [:maybe [:ref :message]]]]
 [:and
  [:fn
   #:error{:message
           "Exactly one of [:start_all :stop_all :reboot :power_off :localization :reset_configs :start_rec :stop_rec :mark_rec_important :unmark_rec_important :enter_transport :geodesic_mode_enable :geodesic_mode_disable] must be set"}
   (clojure.core/fn
    [m__8212__auto__]
    (clojure.core/=
     1
     (clojure.core/count
      (clojure.core/filter
       (fn*
        [p1__8211__8213__auto__]
        (clojure.core/contains?
         m__8212__auto__
         p1__8211__8213__auto__))
       [:start_all
        :stop_all
        :reboot
        :power_off
        :localization
        :reset_configs
        :start_rec
        :stop_rec
        :mark_rec_important
        :unmark_rec_important
        :enter_transport
        :geodesic_mode_enable
        :geodesic_mode_disable]))))]]]


   :cmd/System/SetLocalization
   [:map {:closed true} [:loc {:optional true} [:maybe [:ref :enum]]]]


   :cmd/System/StartALl
   [:map {:closed true}]


   :cmd/System/StartRec
   [:map {:closed true}]


   :cmd/System/StopALl
   [:map {:closed true}]


   :cmd/System/StopRec
   [:map {:closed true}]


   :cmd/System/UnmarkRecImportant
   [:map {:closed true}]
})

(def schema
  "Schema with registry"
  [:schema {:registry registry}])
