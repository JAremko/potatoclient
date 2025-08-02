(ns protobuf.cmd-specs
  "Malli specifications for command (cmd.*) protobuf messages"
  (:require [malli.core :as m]
            [malli.generator :as mg]
            [protobuf.malli-oneof :as oneof]))

;; Generated from protobuf definitions
;; NOTE: These specs are automatically generated and may not include
;; all buf.validate constraints. Use as a structural baseline.

(def registry
  "Malli registry for protobuf messages"
  (merge
   ;; Include the custom :oneof schema type
   {:oneof oneof/-oneof-schema}
   ;; Generated specs
   {
    :cmd/CV/DumpStart
    [:map {:closed true}]


    :cmd/CV/DumpStop
    [:map {:closed true}]


    :cmd/CV/Root
    [:oneof
 #:error{:message
         "Exactly one of [\"set_auto_focus\" \"start_track_ndc\" \"stop_track\" \"vampire_mode_enable\" \"vampire_mode_disable\" \"stabilization_mode_enable\" \"stabilization_mode_disable\" \"dump_start\" \"dump_stop\"] must be set"}
 :set_auto_focus
 [:maybe [:ref :message]]
 :start_track_ndc
 [:maybe [:ref :message]]
 :stop_track
 [:maybe [:ref :message]]
 :vampire_mode_enable
 [:maybe [:ref :message]]
 :vampire_mode_disable
 [:maybe [:ref :message]]
 :stabilization_mode_enable
 [:maybe [:ref :message]]
 :stabilization_mode_disable
 [:maybe [:ref :message]]
 :dump_start
 [:maybe [:ref :message]]
 :dump_stop
 [:maybe [:ref :message]]]


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
    [:oneof
 #:error{:message
         "Exactly one of [\"start\" \"stop\" \"set_magnetic_declination\" \"set_offset_angle_azimuth\" \"set_offset_angle_elevation\" \"set_use_rotary_position\" \"start_calibrate_long\" \"start_calibrate_short\" \"calibrate_next\" \"calibrate_cencel\" \"get_meteo\"] must be set"}
 :start
 [:maybe [:ref :message]]
 :stop
 [:maybe [:ref :message]]
 :set_magnetic_declination
 [:maybe [:ref :message]]
 :set_offset_angle_azimuth
 [:maybe [:ref :message]]
 :set_offset_angle_elevation
 [:maybe [:ref :message]]
 :set_use_rotary_position
 [:maybe [:ref :message]]
 :start_calibrate_long
 [:maybe [:ref :message]]
 :start_calibrate_short
 [:maybe [:ref :message]]
 :calibrate_next
 [:maybe [:ref :message]]
 :calibrate_cencel
 [:maybe [:ref :message]]
 :get_meteo
 [:maybe [:ref :message]]]


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
    [:oneof
 #:error{:message
         "Exactly one of [\"start\" \"stop\" \"turn_on\" \"turn_off\" \"get_meteo\"] must be set"}
 :start
 [:maybe [:ref :message]]
 :stop
 [:maybe [:ref :message]]
 :turn_on
 [:maybe [:ref :message]]
 :turn_off
 [:maybe [:ref :message]]
 :get_meteo
 [:maybe [:ref :message]]]


    :cmd/DayCamGlassHeater/Start
    [:map {:closed true}]


    :cmd/DayCamGlassHeater/Stop
    [:map {:closed true}]


    :cmd/DayCamGlassHeater/TurnOff
    [:map {:closed true}]


    :cmd/DayCamGlassHeater/TurnOn
    [:map {:closed true}]


    :cmd/DayCamera/Focus
    [:oneof
 #:error{:message
         "Exactly one of [\"set_value\" \"move\" \"halt\" \"offset\" \"reset_focus\" \"save_to_table_focus\"] must be set"}
 :set_value
 [:maybe [:ref :message]]
 :move
 [:maybe [:ref :message]]
 :halt
 [:maybe [:ref :message]]
 :offset
 [:maybe [:ref :message]]
 :reset_focus
 [:maybe [:ref :message]]
 :save_to_table_focus
 [:maybe [:ref :message]]]


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
    [:oneof
 #:error{:message
         "Exactly one of [\"focus\" \"zoom\" \"set_iris\" \"set_infra_red_filter\" \"start\" \"stop\" \"photo\" \"set_auto_iris\" \"halt_all\" \"set_fx_mode\" \"next_fx_mode\" \"prev_fx_mode\" \"get_meteo\" \"refresh_fx_mode\" \"set_digital_zoom_level\" \"set_clahe_level\" \"shift_clahe_level\"] must be set"}
 :focus
 [:maybe [:ref :message]]
 :zoom
 [:maybe [:ref :message]]
 :set_iris
 [:maybe [:ref :message]]
 :set_infra_red_filter
 [:maybe [:ref :message]]
 :start
 [:maybe [:ref :message]]
 :stop
 [:maybe [:ref :message]]
 :photo
 [:maybe [:ref :message]]
 :set_auto_iris
 [:maybe [:ref :message]]
 :halt_all
 [:maybe [:ref :message]]
 :set_fx_mode
 [:maybe [:ref :message]]
 :next_fx_mode
 [:maybe [:ref :message]]
 :prev_fx_mode
 [:maybe [:ref :message]]
 :get_meteo
 [:maybe [:ref :message]]
 :refresh_fx_mode
 [:maybe [:ref :message]]
 :set_digital_zoom_level
 [:maybe [:ref :message]]
 :set_clahe_level
 [:maybe [:ref :message]]
 :shift_clahe_level
 [:maybe [:ref :message]]]


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
    [:oneof
 #:error{:message
         "Exactly one of [\"set_value\" \"move\" \"halt\" \"set_zoom_table_value\" \"next_zoom_table_pos\" \"prev_zoom_table_pos\" \"offset\" \"reset_zoom\" \"save_to_table\"] must be set"}
 :set_value
 [:maybe [:ref :message]]
 :move
 [:maybe [:ref :message]]
 :halt
 [:maybe [:ref :message]]
 :set_zoom_table_value
 [:maybe [:ref :message]]
 :next_zoom_table_pos
 [:maybe [:ref :message]]
 :prev_zoom_table_pos
 [:maybe [:ref :message]]
 :offset
 [:maybe [:ref :message]]
 :reset_zoom
 [:maybe [:ref :message]]
 :save_to_table
 [:maybe [:ref :message]]]


    :cmd/Frozen
    [:map {:closed true}]


    :cmd/Gps/GetMeteo
    [:map {:closed true}]


    :cmd/Gps/Root
    [:oneof
 #:error{:message
         "Exactly one of [\"start\" \"stop\" \"set_manual_position\" \"set_use_manual_position\" \"get_meteo\"] must be set"}
 :start
 [:maybe [:ref :message]]
 :stop
 [:maybe [:ref :message]]
 :set_manual_position
 [:maybe [:ref :message]]
 :set_use_manual_position
 [:maybe [:ref :message]]
 :get_meteo
 [:maybe [:ref :message]]]


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
    [:oneof
 #:error{:message
         "Exactly one of [\"zoom\" \"set_agc\" \"set_filter\" \"start\" \"stop\" \"photo\" \"zoom_in\" \"zoom_out\" \"zoom_stop\" \"focus_in\" \"focus_out\" \"focus_stop\" \"calibrate\" \"set_dde_level\" \"enable_dde\" \"disable_dde\" \"set_auto_focus\" \"focus_step_plus\" \"focus_step_minus\" \"set_fx_mode\" \"next_fx_mode\" \"prev_fx_mode\" \"get_meteo\" \"shift_dde\" \"refresh_fx_mode\" \"reset_zoom\" \"save_to_table\" \"set_calib_mode\" \"set_digital_zoom_level\" \"set_clahe_level\" \"shift_clahe_level\"] must be set"}
 :zoom
 [:maybe [:ref :message]]
 :set_agc
 [:maybe [:ref :message]]
 :set_filter
 [:maybe [:ref :message]]
 :start
 [:maybe [:ref :message]]
 :stop
 [:maybe [:ref :message]]
 :photo
 [:maybe [:ref :message]]
 :zoom_in
 [:maybe [:ref :message]]
 :zoom_out
 [:maybe [:ref :message]]
 :zoom_stop
 [:maybe [:ref :message]]
 :focus_in
 [:maybe [:ref :message]]
 :focus_out
 [:maybe [:ref :message]]
 :focus_stop
 [:maybe [:ref :message]]
 :calibrate
 [:maybe [:ref :message]]
 :set_dde_level
 [:maybe [:ref :message]]
 :enable_dde
 [:maybe [:ref :message]]
 :disable_dde
 [:maybe [:ref :message]]
 :set_auto_focus
 [:maybe [:ref :message]]
 :focus_step_plus
 [:maybe [:ref :message]]
 :focus_step_minus
 [:maybe [:ref :message]]
 :set_fx_mode
 [:maybe [:ref :message]]
 :next_fx_mode
 [:maybe [:ref :message]]
 :prev_fx_mode
 [:maybe [:ref :message]]
 :get_meteo
 [:maybe [:ref :message]]
 :shift_dde
 [:maybe [:ref :message]]
 :refresh_fx_mode
 [:maybe [:ref :message]]
 :reset_zoom
 [:maybe [:ref :message]]
 :save_to_table
 [:maybe [:ref :message]]
 :set_calib_mode
 [:maybe [:ref :message]]
 :set_digital_zoom_level
 [:maybe [:ref :message]]
 :set_clahe_level
 [:maybe [:ref :message]]
 :shift_clahe_level
 [:maybe [:ref :message]]]


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
    [:oneof
 #:error{:message
         "Exactly one of [\"set_zoom_table_value\" \"next_zoom_table_pos\" \"prev_zoom_table_pos\"] must be set"}
 :set_zoom_table_value
 [:maybe [:ref :message]]
 :next_zoom_table_pos
 [:maybe [:ref :message]]
 :prev_zoom_table_pos
 [:maybe [:ref :message]]]


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
    [:oneof
 #:error{:message "Exactly one of [\"refine_target\"] must be set"}
 :refine_target
 [:maybe [:ref :message]]]


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
    [:oneof
 #:error{:message
         "Exactly one of [\"measure\" \"scan_on\" \"scan_off\" \"start\" \"stop\" \"target_designator_off\" \"target_designator_on_mode_a\" \"target_designator_on_mode_b\" \"enable_fog_mode\" \"disable_fog_mode\" \"set_scan_mode\" \"new_session\" \"get_meteo\" \"refine_on\" \"refine_off\"] must be set"}
 :measure
 [:maybe [:ref :message]]
 :scan_on
 [:maybe [:ref :message]]
 :scan_off
 [:maybe [:ref :message]]
 :start
 [:maybe [:ref :message]]
 :stop
 [:maybe [:ref :message]]
 :target_designator_off
 [:maybe [:ref :message]]
 :target_designator_on_mode_a
 [:maybe [:ref :message]]
 :target_designator_on_mode_b
 [:maybe [:ref :message]]
 :enable_fog_mode
 [:maybe [:ref :message]]
 :disable_fog_mode
 [:maybe [:ref :message]]
 :set_scan_mode
 [:maybe [:ref :message]]
 :new_session
 [:maybe [:ref :message]]
 :get_meteo
 [:maybe [:ref :message]]
 :refine_on
 [:maybe [:ref :message]]
 :refine_off
 [:maybe [:ref :message]]]


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
    [:oneof
 #:error{:message
         "Exactly one of [\"set\" \"save\" \"reset\" \"shift\"] must be set"}
 :set
 [:maybe [:ref :message]]
 :save
 [:maybe [:ref :message]]
 :reset
 [:maybe [:ref :message]]
 :shift
 [:maybe [:ref :message]]]


    :cmd/Lrf_calib/ResetOffsets
    [:map {:closed true}]


    :cmd/Lrf_calib/Root
    [:oneof
 #:error{:message "Exactly one of [\"day\" \"heat\"] must be set"}
 :day
 [:maybe [:ref :message]]
 :heat
 [:maybe [:ref :message]]]


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
    [:oneof
 #:error{:message
         "Exactly one of [\"show_default_screen\" \"show_lrf_measure_screen\" \"show_lrf_result_screen\" \"show_lrf_result_simplified_screen\" \"enable_heat_osd\" \"disable_heat_osd\" \"enable_day_osd\" \"disable_day_osd\"] must be set"}
 :show_default_screen
 [:maybe [:ref :message]]
 :show_lrf_measure_screen
 [:maybe [:ref :message]]
 :show_lrf_result_screen
 [:maybe [:ref :message]]
 :show_lrf_result_simplified_screen
 [:maybe [:ref :message]]
 :enable_heat_osd
 [:maybe [:ref :message]]
 :disable_heat_osd
 [:maybe [:ref :message]]
 :enable_day_osd
 [:maybe [:ref :message]]
 :disable_day_osd
 [:maybe [:ref :message]]]


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
    [:map
 {:closed true}
 [:protocol_version {:optional true} [:maybe [:int {:min 0}]]]
 [:session_id {:optional true} [:maybe [:int {:min 0}]]]
 [:important {:optional true} [:maybe :boolean]]
 [:from_cv_subsystem {:optional true} [:maybe :boolean]]
 [:client_type {:optional true} [:maybe [:ref :enum]]]
 [:payload
  {:optional true}
  [:oneof
   #:error{:message
           "Exactly one of [\"day_camera\" \"heat_camera\" \"gps\" \"compass\" \"lrf\" \"lrf_calib\" \"rotary\" \"osd\" \"ping\" \"noop\" \"frozen\" \"system\" \"cv\" \"day_cam_glass_heater\" \"lira\"] must be set"}
   :day_camera
   [:maybe [:ref :message]]
   :heat_camera
   [:maybe [:ref :message]]
   :gps
   [:maybe [:ref :message]]
   :compass
   [:maybe [:ref :message]]
   :lrf
   [:maybe [:ref :message]]
   :lrf_calib
   [:maybe [:ref :message]]
   :rotary
   [:maybe [:ref :message]]
   :osd
   [:maybe [:ref :message]]
   :ping
   [:maybe [:ref :message]]
   :noop
   [:maybe [:ref :message]]
   :frozen
   [:maybe [:ref :message]]
   :system
   [:maybe [:ref :message]]
   :cv
   [:maybe [:ref :message]]
   :day_cam_glass_heater
   [:maybe [:ref :message]]
   :lira
   [:maybe [:ref :message]]]]]


    :cmd/RotaryPlatform/Axis
    [:map
 {:closed true}
 [:azimuth {:optional true} [:maybe [:ref :message]]]
 [:elevation {:optional true} [:maybe [:ref :message]]]]


    :cmd/RotaryPlatform/Azimuth
    [:oneof
 #:error{:message
         "Exactly one of [\"set_value\" \"rotate_to\" \"rotate\" \"relative\" \"relative_set\" \"halt\"] must be set"}
 :set_value
 [:maybe [:ref :message]]
 :rotate_to
 [:maybe [:ref :message]]
 :rotate
 [:maybe [:ref :message]]
 :relative
 [:maybe [:ref :message]]
 :relative_set
 [:maybe [:ref :message]]
 :halt
 [:maybe [:ref :message]]]


    :cmd/RotaryPlatform/Elevation
    [:oneof
 #:error{:message
         "Exactly one of [\"set_value\" \"rotate_to\" \"rotate\" \"relative\" \"relative_set\" \"halt\"] must be set"}
 :set_value
 [:maybe [:ref :message]]
 :rotate_to
 [:maybe [:ref :message]]
 :rotate
 [:maybe [:ref :message]]
 :relative
 [:maybe [:ref :message]]
 :relative_set
 [:maybe [:ref :message]]
 :halt
 [:maybe [:ref :message]]]


    :cmd/RotaryPlatform/GetMeteo
    [:map {:closed true}]


    :cmd/RotaryPlatform/Halt
    [:map {:closed true}]


    :cmd/RotaryPlatform/HaltAzimuth
    [:map {:closed true}]


    :cmd/RotaryPlatform/HaltElevation
    [:map {:closed true}]


    :cmd/RotaryPlatform/Root
    [:oneof
 #:error{:message
         "Exactly one of [\"start\" \"stop\" \"axis\" \"set_platform_azimuth\" \"set_platform_elevation\" \"set_platform_bank\" \"halt\" \"set_use_rotary_as_compass\" \"rotate_to_gps\" \"set_origin_gps\" \"set_mode\" \"rotate_to_ndc\" \"scan_start\" \"scan_stop\" \"scan_pause\" \"scan_unpause\" \"get_meteo\" \"scan_prev\" \"scan_next\" \"scan_refresh_node_list\" \"scan_select_node\" \"scan_delete_node\" \"scan_update_node\" \"scan_add_node\"] must be set"}
 :start
 [:maybe [:ref :message]]
 :stop
 [:maybe [:ref :message]]
 :axis
 [:maybe [:ref :message]]
 :set_platform_azimuth
 [:maybe [:ref :message]]
 :set_platform_elevation
 [:maybe [:ref :message]]
 :set_platform_bank
 [:maybe [:ref :message]]
 :halt
 [:maybe [:ref :message]]
 :set_use_rotary_as_compass
 [:maybe [:ref :message]]
 :rotate_to_gps
 [:maybe [:ref :message]]
 :set_origin_gps
 [:maybe [:ref :message]]
 :set_mode
 [:maybe [:ref :message]]
 :rotate_to_ndc
 [:maybe [:ref :message]]
 :scan_start
 [:maybe [:ref :message]]
 :scan_stop
 [:maybe [:ref :message]]
 :scan_pause
 [:maybe [:ref :message]]
 :scan_unpause
 [:maybe [:ref :message]]
 :get_meteo
 [:maybe [:ref :message]]
 :scan_prev
 [:maybe [:ref :message]]
 :scan_next
 [:maybe [:ref :message]]
 :scan_refresh_node_list
 [:maybe [:ref :message]]
 :scan_select_node
 [:maybe [:ref :message]]
 :scan_delete_node
 [:maybe [:ref :message]]
 :scan_update_node
 [:maybe [:ref :message]]
 :scan_add_node
 [:maybe [:ref :message]]]


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
    [:oneof
 #:error{:message
         "Exactly one of [\"start_all\" \"stop_all\" \"reboot\" \"power_off\" \"localization\" \"reset_configs\" \"start_rec\" \"stop_rec\" \"mark_rec_important\" \"unmark_rec_important\" \"enter_transport\" \"geodesic_mode_enable\" \"geodesic_mode_disable\"] must be set"}
 :start_all
 [:maybe [:ref :message]]
 :stop_all
 [:maybe [:ref :message]]
 :reboot
 [:maybe [:ref :message]]
 :power_off
 [:maybe [:ref :message]]
 :localization
 [:maybe [:ref :message]]
 :reset_configs
 [:maybe [:ref :message]]
 :start_rec
 [:maybe [:ref :message]]
 :stop_rec
 [:maybe [:ref :message]]
 :mark_rec_important
 [:maybe [:ref :message]]
 :unmark_rec_important
 [:maybe [:ref :message]]
 :enter_transport
 [:maybe [:ref :message]]
 :geodesic_mode_enable
 [:maybe [:ref :message]]
 :geodesic_mode_disable
 [:maybe [:ref :message]]]


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
   }))

(def schema
  "Schema with registry"
  [:schema {:registry registry}])
