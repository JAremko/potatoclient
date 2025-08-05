package potatoclient.transit

import org.junit.Test
import org.junit.Assert.*
import potatoclient.kotlin.transit.*
import ser.JonSharedData
import ser.JonSharedDataTypes
import ser.JonSharedDataRotary
import ser.JonSharedDataDayCamera
import ser.JonSharedDataHeatCamera
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay

class StateSubprocessTest {

    @Test
    fun testSimpleStateConverterBasicState() {
        // Create a simple GUI state
        val guiState = JonSharedData.JonGUIState.newBuilder()
            .setClientType(JonSharedDataTypes.JonGuiDataClientType.JON_GUI_DATA_CLIENT_TYPE_DAY_CAMERA)
            .setReadonly(false)
            .build()
        
        val transitMap = SimpleStateConverter.convertToTransit(guiState)
        
        assertNotNull(transitMap)
        assertEquals("day-camera", transitMap["clientType"])
        assertEquals(false, transitMap["readonly"])
    }

    @Test
    fun testSimpleStateConverterWithRotaryData() {
        // Create rotary state
        val rotaryState = JonSharedDataRotary.JonGuiDataRotary.newBuilder()
            .setRotaryMode(JonSharedDataTypes.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_OFF)
            .setAzimuthScanRangeMin(-180.0)
            .setAzimuthScanRangeMax(180.0)
            .setAzimuthCurrentAngle(45.0)
            .setElevationCurrentAngle(30.0)
            .build()
        
        val guiState = JonSharedData.JonGUIState.newBuilder()
            .setRotary(rotaryState)
            .build()
        
        val transitMap = SimpleStateConverter.convertToTransit(guiState)
        
        assertNotNull(transitMap["rotary"])
        val rotary = transitMap["rotary"] as Map<*, *>
        assertEquals("off", rotary["rotaryMode"])
        assertEquals(-180.0, rotary["azimuthScanRangeMin"])
        assertEquals(180.0, rotary["azimuthScanRangeMax"])
        assertEquals(45.0, rotary["azimuthCurrentAngle"])
        assertEquals(30.0, rotary["elevationCurrentAngle"])
    }

    @Test
    fun testSimpleStateConverterWithDayCameraData() {
        // Create day camera state
        val dayCameraState = JonSharedDataDayCamera.JonGuiDataDayCamera.newBuilder()
            .setDeviceName("Sony Camera")
            .setCameraDeviceState(JonSharedDataTypes.JonGuiDataCameraDeviceState.JON_GUI_DATA_CAMERA_DEVICE_STATE_READY)
            .setPalette(JonSharedDataTypes.JonGuiDataCameraPalette.JON_GUI_DATA_CAMERA_PALETTE_BW)
            .setDigitalZoom(2.5)
            .setOpticalZoom(1.5)
            .setCanZoomIn(true)
            .setCanZoomOut(true)
            .setShutterSpeed(0.033) // ~30fps
            .build()
        
        val guiState = JonSharedData.JonGUIState.newBuilder()
            .setDayCamera(dayCameraState)
            .build()
        
        val transitMap = SimpleStateConverter.convertToTransit(guiState)
        
        assertNotNull(transitMap["dayCamera"])
        val camera = transitMap["dayCamera"] as Map<*, *>
        assertEquals("Sony Camera", camera["deviceName"])
        assertEquals("ready", camera["cameraDeviceState"])
        assertEquals("bw", camera["palette"])
        assertEquals(2.5, camera["digitalZoom"])
        assertEquals(1.5, camera["opticalZoom"])
        assertEquals(true, camera["canZoomIn"])
        assertEquals(true, camera["canZoomOut"])
        assertEquals(0.033, camera["shutterSpeed"] as Double, 0.001)
    }

    @Test
    fun testStateDebouncing() = runBlocking {
        val debouncer = StateDebouncer()
        
        // Create two identical states
        val state1 = JonSharedData.JonGUIState.newBuilder()
            .setReadonly(true)
            .build()
        
        val state2 = JonSharedData.JonGUIState.newBuilder()
            .setReadonly(true)
            .build()
        
        // First state should not be debounced
        assertTrue("First state should pass", debouncer.shouldSend(state1))
        
        // Identical state should be debounced
        assertFalse("Identical state should be debounced", debouncer.shouldSend(state2))
        
        // Different state should pass
        val state3 = JonSharedData.JonGUIState.newBuilder()
            .setReadonly(false)
            .build()
        assertTrue("Different state should pass", debouncer.shouldSend(state3))
    }

    @Test
    fun testRateLimiting() = runBlocking {
        val limiter = RateLimiter(5) // 5 Hz
        
        // Should allow first 5 immediately
        var allowed = 0
        repeat(10) {
            if (limiter.tryAcquire()) {
                allowed++
            }
        }
        
        assertEquals("Should allow exactly 5 initially", 5, allowed)
        
        // Wait for token refill
        delay(250) // Wait 250ms, should allow ~1 more
        
        assertTrue("Should allow at least one more after delay", limiter.tryAcquire())
        
        limiter.shutdown()
    }

    @Test
    fun testComplexStateConversion() {
        // Create a complex state with all subsystems
        val rotaryState = JonSharedDataRotary.JonGuiDataRotary.newBuilder()
            .setRotaryMode(JonSharedDataTypes.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_MANUAL)
            .setAzimuthCurrentAngle(90.0)
            .setElevationCurrentAngle(45.0)
            .build()
        
        val dayCameraState = JonSharedDataDayCamera.JonGuiDataDayCamera.newBuilder()
            .setDeviceName("Camera1")
            .setCameraDeviceState(JonSharedDataTypes.JonGuiDataCameraDeviceState.JON_GUI_DATA_CAMERA_DEVICE_STATE_READY)
            .build()
        
        val heatCameraState = ser.JonSharedDataHeatCamera.JonGuiDataHeatCamera.newBuilder()
            .setDeviceName("FLIR")
            .setCameraDeviceState(JonSharedDataTypes.JonGuiDataCameraDeviceState.JON_GUI_DATA_CAMERA_DEVICE_STATE_STANDBY)
            .build()
        
        val guiState = JonSharedData.JonGUIState.newBuilder()
            .setClientType(JonSharedDataTypes.JonGuiDataClientType.JON_GUI_DATA_CLIENT_TYPE_TECHNICIAN)
            .setReadonly(false)
            .setRotary(rotaryState)
            .setDayCamera(dayCameraState)
            .setHeatCamera(heatCameraState)
            .build()
        
        val transitMap = SimpleStateConverter.convertToTransit(guiState)
        
        // Verify top-level fields
        assertEquals("technician", transitMap["clientType"])
        assertEquals(false, transitMap["readonly"])
        
        // Verify subsystems exist
        assertNotNull(transitMap["rotary"])
        assertNotNull(transitMap["dayCamera"])
        assertNotNull(transitMap["heatCamera"])
        
        // Verify subsystem data
        val rotary = transitMap["rotary"] as Map<*, *>
        assertEquals("manual", rotary["rotaryMode"])
        assertEquals(90.0, rotary["azimuthCurrentAngle"])
        
        val dayCamera = transitMap["dayCamera"] as Map<*, *>
        assertEquals("Camera1", dayCamera["deviceName"])
        assertEquals("ready", dayCamera["cameraDeviceState"])
        
        val heatCamera = transitMap["heatCamera"] as Map<*, *>
        assertEquals("FLIR", heatCamera["deviceName"])
        assertEquals("standby", heatCamera["cameraDeviceState"])
    }

    @Test
    fun testEnumConversions() {
        // Test client type conversions
        assertEquals("viewer", SimpleStateConverter.convertClientType(
            JonSharedDataTypes.JonGuiDataClientType.JON_GUI_DATA_CLIENT_TYPE_VIEWER))
        assertEquals("day-camera", SimpleStateConverter.convertClientType(
            JonSharedDataTypes.JonGuiDataClientType.JON_GUI_DATA_CLIENT_TYPE_DAY_CAMERA))
        assertEquals("heat-camera", SimpleStateConverter.convertClientType(
            JonSharedDataTypes.JonGuiDataClientType.JON_GUI_DATA_CLIENT_TYPE_HEAT_CAMERA))
        assertEquals("technician", SimpleStateConverter.convertClientType(
            JonSharedDataTypes.JonGuiDataClientType.JON_GUI_DATA_CLIENT_TYPE_TECHNICIAN))
        
        // Test rotary mode conversions
        assertEquals("off", SimpleStateConverter.convertRotaryMode(
            JonSharedDataTypes.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_OFF))
        assertEquals("manual", SimpleStateConverter.convertRotaryMode(
            JonSharedDataTypes.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_MANUAL))
        assertEquals("scanning", SimpleStateConverter.convertRotaryMode(
            JonSharedDataTypes.JonGuiDataRotaryMode.JON_GUI_DATA_ROTARY_MODE_SCANNING))
        
        // Test camera state conversions
        assertEquals("initializing", SimpleStateConverter.convertCameraDeviceState(
            JonSharedDataTypes.JonGuiDataCameraDeviceState.JON_GUI_DATA_CAMERA_DEVICE_STATE_INITIALIZING))
        assertEquals("ready", SimpleStateConverter.convertCameraDeviceState(
            JonSharedDataTypes.JonGuiDataCameraDeviceState.JON_GUI_DATA_CAMERA_DEVICE_STATE_READY))
        assertEquals("failed", SimpleStateConverter.convertCameraDeviceState(
            JonSharedDataTypes.JonGuiDataCameraDeviceState.JON_GUI_DATA_CAMERA_DEVICE_STATE_FAILED))
    }
}