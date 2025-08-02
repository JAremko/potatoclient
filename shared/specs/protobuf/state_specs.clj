(ns protobuf.state-specs
  "Malli specifications for state (ser.*) protobuf messages"
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
    :ser/JonGUIState
    [:map
 {:closed true}
 [:system {:optional true} [:maybe [:ref :message]]]
 [:camera_heat {:optional true} [:maybe [:ref :message]]]
 [:camera_day {:optional true} [:maybe [:ref :message]]]
 [:gps {:optional true} [:maybe [:ref :message]]]
 [:time {:optional true} [:maybe [:ref :message]]]
 [:compass_calibration {:optional true} [:maybe [:ref :message]]]
 [:lrf {:optional true} [:maybe [:ref :message]]]
 [:meteo_internal {:optional true} [:maybe [:ref :message]]]
 [:rec_osd {:optional true} [:maybe [:ref :message]]]
 [:rotary {:optional true} [:maybe [:ref :message]]]
 [:day_cam_glass_heater {:optional true} [:maybe [:ref :message]]]
 [:actual_space_time {:optional true} [:maybe [:ref :message]]]
 [:compass {:optional true} [:maybe [:ref :message]]]
 [:protocol_version {:optional true} [:maybe [:int {:min 0}]]]]


    :ser/JonGuiDataActualSpaceTime
    [:map
 {:closed true}
 [:azimuth {:optional true} [:maybe :double]]
 [:elevation {:optional true} [:maybe :double]]
 [:bank {:optional true} [:maybe :double]]
 [:latitude {:optional true} [:maybe :double]]
 [:longitude {:optional true} [:maybe :double]]
 [:altitude {:optional true} [:maybe :double]]
 [:timestamp {:optional true} [:maybe :int]]]


    :ser/JonGuiDataCameraDay
    [:map
 {:closed true}
 [:iris_pos {:optional true} [:maybe :double]]
 [:zoom_table_pos_max {:optional true} [:maybe :int]]
 [:fx_mode {:optional true} [:maybe [:ref :enum]]]
 [:clahe_level {:optional true} [:maybe :double]]
 [:focus_pos {:optional true} [:maybe :double]]
 [:auto_iris {:optional true} [:maybe :boolean]]
 [:zoom_pos {:optional true} [:maybe :double]]
 [:digital_zoom_level {:optional true} [:maybe :double]]
 [:infrared_filter {:optional true} [:maybe :boolean]]
 [:zoom_table_pos {:optional true} [:maybe :int]]
 [:auto_focus {:optional true} [:maybe :boolean]]]


    :ser/JonGuiDataCameraHeat
    [:map
 {:closed true}
 [:agc_mode {:optional true} [:maybe [:ref :enum]]]
 [:zoom_table_pos_max {:optional true} [:maybe :int]]
 [:fx_mode {:optional true} [:maybe [:ref :enum]]]
 [:clahe_level {:optional true} [:maybe :double]]
 [:filter {:optional true} [:maybe [:ref :enum]]]
 [:dde_enabled {:optional true} [:maybe :boolean]]
 [:zoom_pos {:optional true} [:maybe :double]]
 [:digital_zoom_level {:optional true} [:maybe :double]]
 [:zoom_table_pos {:optional true} [:maybe :int]]
 [:dde_level {:optional true} [:maybe :int]]
 [:auto_focus {:optional true} [:maybe :boolean]]]


    :ser/JonGuiDataCompass
    [:map
 {:closed true}
 [:azimuth {:optional true} [:maybe :double]]
 [:elevation {:optional true} [:maybe :double]]
 [:bank {:optional true} [:maybe :double]]
 [:offsetAzimuth {:optional true} [:maybe :double]]
 [:offsetElevation {:optional true} [:maybe :double]]
 [:magneticDeclination {:optional true} [:maybe :double]]
 [:calibrating {:optional true} [:maybe :boolean]]]


    :ser/JonGuiDataCompassCalibration
    [:map
 {:closed true}
 [:stage {:optional true} [:maybe [:int {:min 0}]]]
 [:final_stage {:optional true} [:maybe [:int {:min 0}]]]
 [:target_azimuth {:optional true} [:maybe :double]]
 [:target_elevation {:optional true} [:maybe :double]]
 [:target_bank {:optional true} [:maybe :double]]
 [:status {:optional true} [:maybe [:ref :enum]]]]


    :ser/JonGuiDataDayCamGlassHeater
    [:map
 {:closed true}
 [:temperature {:optional true} [:maybe :double]]
 [:status {:optional true} [:maybe :boolean]]]


    :ser/JonGuiDataGps
    [:map
 {:closed true}
 [:longitude {:optional true} [:maybe :double]]
 [:latitude {:optional true} [:maybe :double]]
 [:altitude {:optional true} [:maybe :double]]
 [:manual_longitude {:optional true} [:maybe :double]]
 [:manual_latitude {:optional true} [:maybe :double]]
 [:manual_altitude {:optional true} [:maybe :double]]
 [:fix_type {:optional true} [:maybe [:ref :enum]]]
 [:use_manual {:optional true} [:maybe :boolean]]]


    :ser/JonGuiDataLrf
    [:map
 {:closed true}
 [:is_scanning {:optional true} [:maybe :boolean]]
 [:is_measuring {:optional true} [:maybe :boolean]]
 [:measure_id {:optional true} [:maybe :int]]
 [:target {:optional true} [:maybe [:ref :message]]]
 [:pointer_mode {:optional true} [:maybe [:ref :enum]]]
 [:fogModeEnabled {:optional true} [:maybe :boolean]]
 [:is_refining {:optional true} [:maybe :boolean]]]


    :ser/JonGuiDataMeteo
    [:map
 {:closed true}
 [:temperature {:optional true} [:maybe :double]]
 [:humidity {:optional true} [:maybe :double]]
 [:pressure {:optional true} [:maybe :double]]]


    :ser/JonGuiDataRecOsd
    [:map
 {:closed true}
 [:screen {:optional true} [:maybe [:ref :enum]]]
 [:heat_osd_enabled {:optional true} [:maybe :boolean]]
 [:day_osd_enabled {:optional true} [:maybe :boolean]]
 [:heat_crosshair_offset_horizontal {:optional true} [:maybe :int]]
 [:heat_crosshair_offset_vertical {:optional true} [:maybe :int]]
 [:day_crosshair_offset_horizontal {:optional true} [:maybe :int]]
 [:day_crosshair_offset_vertical {:optional true} [:maybe :int]]]


    :ser/JonGuiDataRotary
    [:map
 {:closed true}
 [:platform_bank {:optional true} [:maybe :double]]
 [:elevation {:optional true} [:maybe :double]]
 [:is_scanning {:optional true} [:maybe :boolean]]
 [:is_moving {:optional true} [:maybe :boolean]]
 [:elevation_speed {:optional true} [:maybe :double]]
 [:sun_azimuth {:optional true} [:maybe :double]]
 [:mode {:optional true} [:maybe [:ref :enum]]]
 [:sun_elevation {:optional true} [:maybe :double]]
 [:scan_target {:optional true} [:maybe :int]]
 [:platform_azimuth {:optional true} [:maybe :double]]
 [:azimuth {:optional true} [:maybe :double]]
 [:is_scanning_paused {:optional true} [:maybe :boolean]]
 [:use_rotary_as_compass {:optional true} [:maybe :boolean]]
 [:azimuth_speed {:optional true} [:maybe :double]]
 [:platform_elevation {:optional true} [:maybe :double]]
 [:scan_target_max {:optional true} [:maybe :int]]
 [:current_scan_node {:optional true} [:maybe [:ref :message]]]]


    :ser/JonGuiDataSystem
    [:map
 {:closed true}
 [:cur_video_rec_dir_month {:optional true} [:maybe :int]]
 [:vampire_mode {:optional true} [:maybe :boolean]]
 [:cur_video_rec_dir_year {:optional true} [:maybe :int]]
 [:disk_space {:optional true} [:maybe :int]]
 [:gpu_load {:optional true} [:maybe :double]]
 [:cpu_temperature {:optional true} [:maybe :double]]
 [:important_rec_enabled {:optional true} [:maybe :boolean]]
 [:low_disk_space {:optional true} [:maybe :boolean]]
 [:gpu_temperature {:optional true} [:maybe :double]]
 [:power_consumption {:optional true} [:maybe :double]]
 [:cur_video_rec_dir_second {:optional true} [:maybe :int]]
 [:tracking {:optional true} [:maybe :boolean]]
 [:cur_video_rec_dir_hour {:optional true} [:maybe :int]]
 [:geodesic_mode {:optional true} [:maybe :boolean]]
 [:stabilization_mode {:optional true} [:maybe :boolean]]
 [:cv_dumping {:optional true} [:maybe :boolean]]
 [:loc {:optional true} [:maybe [:ref :enum]]]
 [:cur_video_rec_dir_minute {:optional true} [:maybe :int]]
 [:cur_video_rec_dir_day {:optional true} [:maybe :int]]
 [:cpu_load {:optional true} [:maybe :double]]
 [:rec_enabled {:optional true} [:maybe :boolean]]
 [:no_disk_space {:optional true} [:maybe :boolean]]]


    :ser/JonGuiDataTarget
    [:map
 {:closed true}
 [:distance_2d {:optional true} [:maybe :double]]
 [:observer_longitude {:optional true} [:maybe :double]]
 [:observer_latitude {:optional true} [:maybe :double]]
 [:uuid_part1 {:optional true} [:maybe :int]]
 [:uuid_part3 {:optional true} [:maybe :int]]
 [:target_longitude {:optional true} [:maybe :double]]
 [:session_id {:optional true} [:maybe :int]]
 [:type {:optional true} [:maybe [:int {:min 0}]]]
 [:target_color {:optional true} [:maybe [:ref :message]]]
 [:observer_fix_type {:optional true} [:maybe [:ref :enum]]]
 [:target_id {:optional true} [:maybe :int]]
 [:observer_azimuth {:optional true} [:maybe :double]]
 [:target_altitude {:optional true} [:maybe :double]]
 [:observer_altitude {:optional true} [:maybe :double]]
 [:observer_bank {:optional true} [:maybe :double]]
 [:target_latitude {:optional true} [:maybe :double]]
 [:distance_3b {:optional true} [:maybe :double]]
 [:observer_elevation {:optional true} [:maybe :double]]
 [:uuid_part4 {:optional true} [:maybe :int]]
 [:timestamp {:optional true} [:maybe :int]]
 [:uuid_part2 {:optional true} [:maybe :int]]]


    :ser/JonGuiDataTime
    [:map
 {:closed true}
 [:timestamp {:optional true} [:maybe :int]]
 [:manual_timestamp {:optional true} [:maybe :int]]
 [:zone_id {:optional true} [:maybe :int]]
 [:use_manual_time {:optional true} [:maybe :boolean]]]


    :ser/RgbColor
    [:map
 {:closed true}
 [:red {:optional true} [:maybe [:int {:min 0}]]]
 [:green {:optional true} [:maybe [:int {:min 0}]]]
 [:blue {:optional true} [:maybe [:int {:min 0}]]]]


    :ser/ScanNode
    [:map
 {:closed true}
 [:index {:optional true} [:maybe :int]]
 [:DayZoomTableValue {:optional true} [:maybe :int]]
 [:HeatZoomTableValue {:optional true} [:maybe :int]]
 [:azimuth {:optional true} [:maybe :double]]
 [:elevation {:optional true} [:maybe :double]]
 [:linger {:optional true} [:maybe :double]]
 [:speed {:optional true} [:maybe :double]]]
   }))

(def schema
  "Schema with registry"
  [:schema {:registry registry}])
