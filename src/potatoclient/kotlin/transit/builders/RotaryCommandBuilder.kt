package potatoclient.kotlin.transit

import cmd.JonSharedCmd
import cmd.RotaryPlatform.JonSharedCmdRotary
import com.cognitect.transit.TransitFactory
import ser.JonSharedDataTypes

/**
 * Builder for rotary platform commands
 */
object RotaryCommandBuilder {
    
    fun build(action: String, params: Map<*, *>): Result<JonSharedCmd.Root> {
        val rotaryMsg = when (action) {
            // Basic control
            "rotary-start" -> buildStart()
            "rotary-stop" -> buildStop()
            "rotary-halt" -> buildHalt()
            
            // Movement commands
            "rotary-rotate-to-ndc" -> buildRotateToNDC(params)
            "rotary-goto-ndc" -> buildRotateToNDC(params) // Alias
            "rotary-rotate-to" -> buildRotateTo(params)
            "rotary-rotate-to-ndc-with-timeout" -> buildRotateToNDCWithTimeout(params)
            "rotary-rotate-to-with-timeout" -> buildRotateToWithTimeout(params)
            "rotary-set-velocity" -> buildSetVelocity(params)
            
            // Axis-specific commands
            "rotary-axis-azimuth-halt" -> buildAxisAzimuthHalt()
            "rotary-axis-azimuth-relative" -> buildAxisAzimuthRelative(params)
            "rotary-axis-azimuth-set-position" -> buildAxisAzimuthSetPosition(params)
            "rotary-axis-azimuth-set-velocity" -> buildAxisAzimuthSetVelocity(params)
            "rotary-axis-elevation-halt" -> buildAxisElevationHalt()
            "rotary-axis-elevation-relative" -> buildAxisElevationRelative(params)
            "rotary-axis-elevation-set-position" -> buildAxisElevationSetPosition(params)
            "rotary-axis-elevation-set-velocity" -> buildAxisElevationSetVelocity(params)
            
            // Speed commands
            "rotary-speed-set-azimuth" -> buildSpeedSetAzimuth(params)
            "rotary-speed-set-elevation" -> buildSpeedSetElevation(params)
            "rotary-speed-set-zoom-dependent" -> buildSpeedSetZoomDependent(params)
            
            // Park commands
            "rotary-park" -> buildPark()
            "rotary-park-shortest" -> buildParkShortest()
            "rotary-unpark" -> buildUnpark()
            
            // Calibration
            "rotary-calibrate-encoders" -> buildCalibrateEncoders()
            "rotary-calibrate-magnetic-sensors" -> buildCalibrateMagneticSensors()
            
            // Configuration
            "rotary-set-north-offset" -> buildSetNorthOffset(params)
            "rotary-set-debug-mode" -> buildSetDebugMode(params)
            "rotary-get-debug-info" -> buildGetDebugInfo()
            "rotary-get-config" -> buildGetConfig()
            "rotary-reset-config" -> buildResetConfig()
            
            // Stabilization
            "rotary-stabilization-enable" -> buildStabilizationEnable()
            "rotary-stabilization-disable" -> buildStabilizationDisable()
            "rotary-stabilization-force-off" -> buildStabilizationForceOff()
            
            else -> return Result.failure(
                IllegalArgumentException("Unknown rotary command: $action")
            )
        }
        
        return rotaryMsg.map { rotary ->
            JonSharedCmd.Root.newBuilder()
                .setProtocolVersion(1)
                .setRotary(rotary)
                .build()
        }
    }
    
    private fun buildStart(): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setStart(JonSharedCmdRotary.Start.newBuilder().build())
            .build()
    )
    
    private fun buildStop(): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setStop(JonSharedCmdRotary.Stop.newBuilder().build())
            .build()
    )
    
    private fun buildHalt(): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setHalt(JonSharedCmdRotary.Halt.newBuilder().build())
            .build()
    )
    
    private fun buildRotateToNDC(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val channel = getStringParam(params, "channel")
            ?: return Result.failure(IllegalArgumentException("Missing channel parameter"))
        val x = getFloatParam(params, "x")
            ?: return Result.failure(IllegalArgumentException("Missing x parameter"))
        val y = getFloatParam(params, "y")
            ?: return Result.failure(IllegalArgumentException("Missing y parameter"))
        
        val rotateToNdc = JonSharedCmdRotary.RotateToNDC.newBuilder()
            .setChannel(parseChannel(channel))
            .setX(x)
            .setY(y)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder()
                .setRotateToNdc(rotateToNdc)
                .build()
        )
    }
    
    private fun buildRotateTo(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val azimuth = getFloatParam(params, "azimuth")
            ?: return Result.failure(IllegalArgumentException("Missing azimuth parameter"))
        val elevation = getFloatParam(params, "elevation")
            ?: return Result.failure(IllegalArgumentException("Missing elevation parameter"))
        
        val rotateTo = JonSharedCmdRotary.RotateTo.newBuilder()
            .setAzimuth(azimuth)
            .setElevation(elevation)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder()
                .setRotateTo(rotateTo)
                .build()
        )
    }
    
    private fun buildRotateToNDCWithTimeout(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val channel = getStringParam(params, "channel")
            ?: return Result.failure(IllegalArgumentException("Missing channel parameter"))
        val x = getFloatParam(params, "x")
            ?: return Result.failure(IllegalArgumentException("Missing x parameter"))
        val y = getFloatParam(params, "y")
            ?: return Result.failure(IllegalArgumentException("Missing y parameter"))
        val timeout = getIntParam(params, "timeout")
            ?: return Result.failure(IllegalArgumentException("Missing timeout parameter"))
        
        val rotateToNdc = JonSharedCmdRotary.RotateToNDCWithTimeout.newBuilder()
            .setChannel(parseChannel(channel))
            .setX(x)
            .setY(y)
            .setTimeout(timeout)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder()
                .setRotateToNdcWithTimeout(rotateToNdc)
                .build()
        )
    }
    
    private fun buildRotateToWithTimeout(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val azimuth = getFloatParam(params, "azimuth")
            ?: return Result.failure(IllegalArgumentException("Missing azimuth parameter"))
        val elevation = getFloatParam(params, "elevation")
            ?: return Result.failure(IllegalArgumentException("Missing elevation parameter"))
        val timeout = getIntParam(params, "timeout")
            ?: return Result.failure(IllegalArgumentException("Missing timeout parameter"))
        
        val rotateTo = JonSharedCmdRotary.RotateToWithTimeout.newBuilder()
            .setAzimuth(azimuth)
            .setElevation(elevation)
            .setTimeout(timeout)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder()
                .setRotateToWithTimeout(rotateTo)
                .build()
        )
    }
    
    private fun buildSetVelocity(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val azSpeed = getFloatParam(params, "azimuth-speed")
            ?: return Result.failure(IllegalArgumentException("Missing azimuth-speed parameter"))
        val elSpeed = getFloatParam(params, "elevation-speed")
            ?: return Result.failure(IllegalArgumentException("Missing elevation-speed parameter"))
        
        val azDirStr = getStringParam(params, "azimuth-direction") ?: "clockwise"
        val elDirStr = getStringParam(params, "elevation-direction") ?: "clockwise"
        
        val azDir = parseRotaryDirection(azDirStr)
        val elDir = parseRotaryDirection(elDirStr)
        
        // Build azimuth and elevation messages
        val azimuthRotate = JonSharedCmdRotary.RotateAzimuth.newBuilder()
            .setSpeed(azSpeed)
            .setDirection(azDir)
            .build()
        
        val elevationRotate = JonSharedCmdRotary.RotateElevation.newBuilder()
            .setSpeed(elSpeed)
            .setDirection(elDir)
            .build()
        
        val azimuthMsg = JonSharedCmdRotary.Azimuth.newBuilder()
            .setRotate(azimuthRotate)
            .build()
        
        val elevationMsg = JonSharedCmdRotary.Elevation.newBuilder()
            .setRotate(elevationRotate)
            .build()
        
        val axisMsg = JonSharedCmdRotary.Axis.newBuilder()
            .setAzimuth(azimuthMsg)
            .setElevation(elevationMsg)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder()
                .setAxis(axisMsg)
                .build()
        )
    }
    
    // Axis-specific commands
    private fun buildAxisAzimuthHalt(): Result<JonSharedCmdRotary.Root> {
        val halt = JonSharedCmdRotary.HaltAzimuth.newBuilder().build()
        val azimuth = JonSharedCmdRotary.Azimuth.newBuilder().setHalt(halt).build()
        val axis = JonSharedCmdRotary.Axis.newBuilder().setAzimuth(azimuth).build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder().setAxis(axis).build()
        )
    }
    
    private fun buildAxisElevationHalt(): Result<JonSharedCmdRotary.Root> {
        val halt = JonSharedCmdRotary.HaltElevation.newBuilder().build()
        val elevation = JonSharedCmdRotary.Elevation.newBuilder().setHalt(halt).build()
        val axis = JonSharedCmdRotary.Axis.newBuilder().setElevation(elevation).build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder().setAxis(axis).build()
        )
    }
    
    private fun buildAxisAzimuthRelative(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val relative = JonSharedCmdRotary.RelativeAzimuth.newBuilder()
            .setValue(value)
            .build()
        val azimuth = JonSharedCmdRotary.Azimuth.newBuilder().setRelative(relative).build()
        val axis = JonSharedCmdRotary.Axis.newBuilder().setAzimuth(azimuth).build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder().setAxis(axis).build()
        )
    }
    
    private fun buildAxisElevationRelative(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val relative = JonSharedCmdRotary.RelativeElevation.newBuilder()
            .setValue(value)
            .build()
        val elevation = JonSharedCmdRotary.Elevation.newBuilder().setRelative(relative).build()
        val axis = JonSharedCmdRotary.Axis.newBuilder().setElevation(elevation).build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder().setAxis(axis).build()
        )
    }
    
    private fun buildAxisAzimuthSetPosition(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setPos = JonSharedCmdRotary.SetPositionAzimuth.newBuilder()
            .setValue(value)
            .build()
        val azimuth = JonSharedCmdRotary.Azimuth.newBuilder().setPosition(setPos).build()
        val axis = JonSharedCmdRotary.Axis.newBuilder().setAzimuth(azimuth).build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder().setAxis(axis).build()
        )
    }
    
    private fun buildAxisElevationSetPosition(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setPos = JonSharedCmdRotary.SetPositionElevation.newBuilder()
            .setValue(value)
            .build()
        val elevation = JonSharedCmdRotary.Elevation.newBuilder().setPosition(setPos).build()
        val axis = JonSharedCmdRotary.Axis.newBuilder().setElevation(elevation).build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder().setAxis(axis).build()
        )
    }
    
    private fun buildAxisAzimuthSetVelocity(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val speed = getFloatParam(params, "speed")
            ?: return Result.failure(IllegalArgumentException("Missing speed parameter"))
        val dirStr = getStringParam(params, "direction") ?: "clockwise"
        
        val rotate = JonSharedCmdRotary.RotateAzimuth.newBuilder()
            .setSpeed(speed)
            .setDirection(parseRotaryDirection(dirStr))
            .build()
        val azimuth = JonSharedCmdRotary.Azimuth.newBuilder().setRotate(rotate).build()
        val axis = JonSharedCmdRotary.Axis.newBuilder().setAzimuth(azimuth).build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder().setAxis(axis).build()
        )
    }
    
    private fun buildAxisElevationSetVelocity(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val speed = getFloatParam(params, "speed")
            ?: return Result.failure(IllegalArgumentException("Missing speed parameter"))
        val dirStr = getStringParam(params, "direction") ?: "clockwise"
        
        val rotate = JonSharedCmdRotary.RotateElevation.newBuilder()
            .setSpeed(speed)
            .setDirection(parseRotaryDirection(dirStr))
            .build()
        val elevation = JonSharedCmdRotary.Elevation.newBuilder().setRotate(rotate).build()
        val axis = JonSharedCmdRotary.Axis.newBuilder().setElevation(elevation).build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder().setAxis(axis).build()
        )
    }
    
    // Speed commands
    private fun buildSpeedSetAzimuth(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setAzimuth = JonSharedCmdRotary.SetAzimuth.newBuilder()
            .setValue(value)
            .build()
        val speed = JonSharedCmdRotary.Speed.newBuilder()
            .setAzimuth(setAzimuth)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder().setSpeed(speed).build()
        )
    }
    
    private fun buildSpeedSetElevation(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setElevation = JonSharedCmdRotary.SetElevation.newBuilder()
            .setValue(value)
            .build()
        val speed = JonSharedCmdRotary.Speed.newBuilder()
            .setElevation(setElevation)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder().setSpeed(speed).build()
        )
    }
    
    private fun buildSpeedSetZoomDependent(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val enable = getBooleanParam(params, "enable")
            ?: return Result.failure(IllegalArgumentException("Missing enable parameter"))
        
        val setZoomDep = JonSharedCmdRotary.SetZoomDependent.newBuilder()
            .setEnable(enable)
            .build()
        val speed = JonSharedCmdRotary.Speed.newBuilder()
            .setZoomDependent(setZoomDep)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder().setSpeed(speed).build()
        )
    }
    
    // Park commands
    private fun buildPark(): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setPark(JonSharedCmdRotary.Park.newBuilder().build())
            .build()
    )
    
    private fun buildParkShortest(): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setParkShortest(JonSharedCmdRotary.ParkShortest.newBuilder().build())
            .build()
    )
    
    private fun buildUnpark(): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setUnpark(JonSharedCmdRotary.Unpark.newBuilder().build())
            .build()
    )
    
    // Calibration commands
    private fun buildCalibrateEncoders(): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setCalibrateEncoders(JonSharedCmdRotary.CalibrateEncoders.newBuilder().build())
            .build()
    )
    
    private fun buildCalibrateMagneticSensors(): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setCalibrateMagneticSensors(JonSharedCmdRotary.CalibrateMagneticSensors.newBuilder().build())
            .build()
    )
    
    // Configuration commands
    private fun buildSetNorthOffset(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val value = getFloatParam(params, "value")
            ?: return Result.failure(IllegalArgumentException("Missing value parameter"))
        
        val setNorth = JonSharedCmdRotary.SetNorthOffset.newBuilder()
            .setValue(value)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder()
                .setNorthOffset(setNorth)
                .build()
        )
    }
    
    private fun buildSetDebugMode(params: Map<*, *>): Result<JonSharedCmdRotary.Root> {
        val enable = getBooleanParam(params, "enable")
            ?: return Result.failure(IllegalArgumentException("Missing enable parameter"))
        
        val setDebug = JonSharedCmdRotary.SetDebugMode.newBuilder()
            .setEnable(enable)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder()
                .setDebugMode(setDebug)
                .build()
        )
    }
    
    private fun buildGetDebugInfo(): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setGetDebugInfo(JonSharedCmdRotary.GetDebugInfo.newBuilder().build())
            .build()
    )
    
    private fun buildGetConfig(): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setGetConfig(JonSharedCmdRotary.GetConfig.newBuilder().build())
            .build()
    )
    
    private fun buildResetConfig(): Result<JonSharedCmdRotary.Root> = Result.success(
        JonSharedCmdRotary.Root.newBuilder()
            .setResetConfig(JonSharedCmdRotary.ResetConfig.newBuilder().build())
            .build()
    )
    
    // Stabilization commands
    private fun buildStabilizationEnable(): Result<JonSharedCmdRotary.Root> {
        val enable = JonSharedCmdRotary.Enable.newBuilder().build()
        val stab = JonSharedCmdRotary.Stabilization.newBuilder()
            .setEnable(enable)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder()
                .setStabilization(stab)
                .build()
        )
    }
    
    private fun buildStabilizationDisable(): Result<JonSharedCmdRotary.Root> {
        val disable = JonSharedCmdRotary.Disable.newBuilder().build()
        val stab = JonSharedCmdRotary.Stabilization.newBuilder()
            .setDisable(disable)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder()
                .setStabilization(stab)
                .build()
        )
    }
    
    private fun buildStabilizationForceOff(): Result<JonSharedCmdRotary.Root> {
        val forceOff = JonSharedCmdRotary.ForceOff.newBuilder().build()
        val stab = JonSharedCmdRotary.Stabilization.newBuilder()
            .setForceOff(forceOff)
            .build()
        
        return Result.success(
            JonSharedCmdRotary.Root.newBuilder()
                .setStabilization(stab)
                .build()
        )
    }
    
    // Helper functions
    private fun getStringParam(params: Map<*, *>, key: String): String? {
        return params[key] as? String ?: params[TransitFactory.keyword(key)] as? String
    }
    
    private fun getFloatParam(params: Map<*, *>, key: String): Float? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return (value as? Number)?.toFloat()
    }
    
    private fun getIntParam(params: Map<*, *>, key: String): Int? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return (value as? Number)?.toInt()
    }
    
    private fun getBooleanParam(params: Map<*, *>, key: String): Boolean? {
        val value = params[key] ?: params[TransitFactory.keyword(key)]
        return value as? Boolean
    }
    
    private fun parseChannel(channelStr: String): JonSharedDataTypes.JonGuiDataVideoChannel =
        when (channelStr.lowercase()) {
            "heat" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
            "day" -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_DAY
            else -> JonSharedDataTypes.JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_HEAT
        }
    
    private fun parseRotaryDirection(dirStr: String): JonSharedDataTypes.JonGuiDataRotaryDirection =
        when (dirStr.lowercase()) {
            "counter-clockwise", "ccw" -> 
                JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_COUNTER_CLOCKWISE
            else -> 
                JonSharedDataTypes.JonGuiDataRotaryDirection.JON_GUI_DATA_ROTARY_DIRECTION_CLOCKWISE
        }
}