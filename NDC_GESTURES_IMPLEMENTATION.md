# NDC and Gesture Implementation Guide

This document details the Normalized Device Coordinates (NDC) system and gesture recognition implementation in the PotatoClient web frontend.

## Overview

The system uses pointer events to control camera movement and zoom through a sophisticated gesture recognition system. All interactions are converted to NDC space (-1 to 1 range) and sent as protobuf commands to the server.

## NDC (Normalized Device Coordinates) System

### NDC Space Definition

NDC coordinates map screen positions to a normalized -1 to 1 range:
- **X-axis**: -1 (left) to 1 (right)
- **Y-axis**: -1 (bottom) to 1 (top)
- **Origin**: (0, 0) at screen center

### NDC Conversion Functions

Located in [`ts/components/lit/panelElement/ndcHelpers.ts`](examples/web/frontend/ts/components/lit/panelElement/ndcHelpers.ts):

```typescript
// Convert client coordinates to NDC
export function clientToNDC(clientX: number, clientY: number, width: number, height: number): NDCCoordinate {
  return {
    x: (clientX / width) * 2 - 1,
    y: -((clientY / height) * 2 - 1)
  };
}

// Convert NDC to pixel coordinates
export function getNDCPixels(ndcPosition: NDCCoordinate, ndcSize: NDCSize, containerWidth: number, containerHeight: number): { left: number; top: number; width: number; height: number } {
  return {
    left: ((ndcPosition.x + 1) / 2) * containerWidth,
    top: ((1 - ndcPosition.y) / 2) * containerHeight,
    width: (ndcSize.width / 2) * containerWidth,
    height: (ndcSize.height / 2) * containerHeight
  };
}
```

### NDC Delta Calculations

For pan gestures, deltas are calculated in NDC space:

```typescript
// From pointerGestureRecognizer.ts
const ndcDeltaX = deltaX / rect.width * 2;
const ndcDeltaY = -deltaY / rect.height * 2;
```

## Gesture Recognition System

### Configuration

Gesture timings and thresholds are configured in [`static/config/interaction/gestures.json`](examples/web/frontend/static/config/interaction/gestures.json):

```json
{
  "doubleTapThreshold": 300,      // ms between taps for double-tap
  "debugVisible": false,           // Show debug overlays
  "zoomDebounceTime": 200,         // ms debounce for zoom gestures
  "moveThreshold": 20,             // pixels before pan starts
  "tapLongPressThreshold": 300,    // ms for long press detection
  "zoomThreshold": 0.01,           // Minimum pinch distance for zoom
  "swipeThreshold": 100            // pixels for swipe recognition
}
```

### Supported Gestures

The [`PointerGestureRecognizer`](examples/web/frontend/ts/components/lit/interactionObserver/pointerGestureRecognizer.ts) detects:

1. **Tap**: Single touch/click
2. **Double Tap**: Two quick taps within threshold
3. **Pan**: Drag gesture for camera rotation
4. **Swipe**: Quick directional movement
5. **Pinch Zoom**: Two-finger zoom gesture

### Gesture Events

Defined in [`pointerGestureEvents.ts`](examples/web/frontend/ts/components/lit/interactionObserver/pointerGestureEvents.ts):

```typescript
export type GestureEvent =
  | {
      type: 'tap';
      x: number;         // Screen coordinates
      y: number;
      ndcX: number;      // NDC coordinates
      ndcY: number;
    }
  | {
      type: 'panStart';
      x: number;
      y: number;
      ndcX: number;
      ndcY: number;
      resolution: Resolution;
      aspectRatio: number;
    }
  | {
      type: 'panMove';
      deltaX: number;     // Pixel deltas
      deltaY: number;
      ndcDeltaX: number;  // NDC deltas
      ndcDeltaY: number;
      x: number;
      y: number;
      resolution: Resolution;
      aspectRatio: number;
    }
  | { type: 'panStop' }
  | { type: 'zoomIn' }
  | { type: 'zoomOut' }
  // ... other gesture types
```

## Pan Gesture Implementation

### Rotary Pan Controller

The [`RotaryPanController`](examples/web/frontend/ts/components/lit/jonStreamGUI/rotaryPanController.ts) converts pan gestures to rotary platform commands:

```typescript
export class RotaryPanController {
    private static readonly UPDATE_INTERVAL = 120; // ms between commands
    
    private calculateRotationSpeeds(ndcDeltaX: number, ndcDeltaY: number): [number, number] {
        const magnitude = Math.sqrt(ndcDeltaX * ndcDeltaX + ndcDeltaY * ndcDeltaY);
        
        // Apply dead zone
        const adjustedMagnitude = Math.max(magnitude - this.currentConfig.deadZoneRadius, 0);
        const maxMagnitude = this.currentConfig.ndcThreshold - this.currentConfig.deadZoneRadius;
        const normalizedMagnitude = Math.min(adjustedMagnitude / maxMagnitude, 1);
        
        // Apply curve interpolation
        const curvedMagnitude = Math.pow(normalizedMagnitude, this.currentConfig.curveSteepness);
        
        // Calculate speed
        const speed = this.currentConfig.minRotationSpeed + 
                     (this.currentConfig.maxRotationSpeed - this.currentConfig.minRotationSpeed) * curvedMagnitude;
        
        return [
            Math.abs(normalizedDeltaX * speed),
            Math.abs(normalizedDeltaY * speed)
        ];
    }
}
```

### Zoom-Based Speed Configuration

Pan speeds vary by zoom level, configured in [`static/config/zoom/zoom_table_rotary_touch.json`](examples/web/frontend/static/config/zoom/zoom_table_rotary_touch.json):

```json
{
  "thermal": [
    {
      "index": 0,                    // Zoom level index
      "maxRotationSpeed": 0.1,       // Maximum rotation speed
      "minRotationSpeed": 0.0001,    // Minimum rotation speed
      "ndcThreshold": 0.5,           // NDC distance for max speed
      "deadZoneRadius": 0.05,        // Dead zone in NDC units
      "curveSteepness": 4.0          // Speed curve exponent
    },
    // ... more zoom levels
  ],
  "visible": [
    // ... day camera zoom levels
  ]
}
```

## Gesture to Command Flow

### 1. Gesture Detection

The [`InteractionObserverElement`](examples/web/frontend/ts/components/lit/interactionObserver/interactionObserverElement.ts) captures pointer events:

```typescript
private setupInteractions(): void {
    this.gestureRecognizer = new PointerGestureRecognizer(
        this,
        this.handleGesture.bind(this),
        {
            moveThreshold: this.config.moveThreshold,
            tapLongPressThreshold: this.config.tapLongPressThreshold,
            zoomThreshold: this.config.zoomThreshold
        }
    );
}
```

### 2. Gesture Handling

The [`InteractionHandler`](examples/web/frontend/ts/components/lit/jonStreamGUI/interactionHandler.ts) processes gestures:

```typescript
handleInteraction(event: GestureEvent): void {
    switch (event.type) {
        case 'tap':
            // Single tap - rotate to NDC position
            RotateToNDC(channel, event.ndcX, event.ndcY);
            break;
            
        case 'doubleTap':
            // Double tap - start tracking at NDC position
            CVStartTrackNDC(channel, event.ndcX, event.ndcY);
            break;
            
        case 'panMove':
            // Pan - continuous rotation
            this.rotaryPanController.updatePan(event.ndcDeltaX, event.ndcDeltaY);
            break;
            
        case 'zoomIn':
        case 'zoomOut':
            // Pinch zoom - change zoom level
            if (this.channelType === ChannelType.HEAT) {
                heatCameraNextZoomTablePos(); // or Prev
            } else {
                dayCameraNextZoomTablePos(); // or Prev
            }
            break;
    }
}
```

### 3. Command Generation

Commands are created using the command sender modules. Each gesture triggers specific command functions that build protobuf messages:

#### Tap → Rotate to NDC Position

```typescript
// From cmdRotary.ts
export function RotateToNDC(channel: Types.JonGuiDataVideoChannel, x: number, y: number): void {
    let rotateToNDCMsg = Cmd.RotaryPlatform.RotateToNDC.create({
        channel,  // HEAT or DAY channel
        x,        // NDC X coordinate (-1 to 1)
        y         // NDC Y coordinate (-1 to 1)
    });
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({rotateToNdc: rotateToNDCMsg});
    CSShared.sendCmdMessage(rootMsg);
}
```

#### Double Tap → Start CV Tracking

```typescript
// From cmdCV.ts
export function CVStartTrackNDC(channel: JonGuiDataVideoChannel, x: number, y: number): void {
    // Fetch current frame timestamp for synchronization
    const dispatch = DeviceStateDispatch.getInstance();
    
    (async () => {
        // Get frame data based on channel (DAY or HEAT)
        const frameData = channel === JonGuiDataVideoChannel.JON_GUI_DATA_VIDEO_CHANNEL_DAY
            ? await dispatch.getDayFrameData()
            : await dispatch.getHeatFrameData();
            
        // Convert BigInt timestamp to Long for protobuf
        const frameTimeLong = frameData 
            ? Long.fromString(frameData.timestamp.toString(), true)
            : Long.UZERO;
            
        // Send tracking command with frame timestamp
        sendStartTrackCommand(channel, x, y, frameTimeLong);
    })();
}

function sendStartTrackCommand(channel: JonGuiDataVideoChannel, x: number, y: number, frameTime: Long): void {
    let rootMsg = CSShared.createRootMessage();
    rootMsg.cv = Cmd.CV.Root.create({
        startTrackNdc: {
            channel: channel,
            x: x,              // NDC X coordinate
            y: y,              // NDC Y coordinate  
            frameTime: frameTime  // Frame timestamp for sync
        }
    });
    CSShared.sendCmdMessage(rootMsg);
}
```

#### Pan → Continuous Rotation

```typescript
// From cmdRotary.ts
export function rotateBoth(
    azimuthSpeed: number, 
    azimuthDirection: Types.JonGuiDataRotaryDirection, 
    elevationSpeed: number, 
    elevationDirection: Types.JonGuiDataRotaryDirection
): void {
    // Create elevation rotation command
    let rotateElevationMsg = Cmd.RotaryPlatform.Elevation.create({
        rotate: Cmd.RotaryPlatform.RotateElevation.create({
            speed: elevationSpeed,
            direction: elevationDirection
        })
    });
    
    // Create azimuth rotation command
    let rotateAzimuthMsg = Cmd.RotaryPlatform.Azimuth.create({
        rotate: Cmd.RotaryPlatform.RotateAzimuth.create({
            speed: azimuthSpeed,
            direction: azimuthDirection
        })
    });
    
    // Send both axis commands together
    CSShared.sendRotaryAxisCommand({
        elevation: rotateElevationMsg,
        azimuth: rotateAzimuthMsg
    });
}

// Stop rotation on pan end
export function rotaryHalt(): void {
    let rootMsg = CSShared.createRootMessage();
    rootMsg.rotary = Cmd.RotaryPlatform.Root.create({
        halt: Cmd.RotaryPlatform.Halt.create()
    });
    CSShared.sendCmdMessage(rootMsg);
}
```

#### Zoom → Change Camera Zoom Level

```typescript
// From cmdDayCamera.ts
export function dayCameraNextZoomTablePos(): void {
    let rootMsg = CSShared.createRootMessage();
    let zoom = Cmd.DayCamera.Zoom.create({nextZoomTablePos: {}});
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

export function dayCameraPrevZoomTablePos(): void {
    let rootMsg = CSShared.createRootMessage();
    let zoom = Cmd.DayCamera.Zoom.create({prevZoomTablePos: {}});
    rootMsg.dayCamera = Cmd.DayCamera.Root.create({zoom});
    CSShared.sendCmdMessage(rootMsg);
}

// Similar functions exist for heat camera in cmdHeatCamera.ts
```

### 4. Message Encoding and Transport

All commands flow through a shared sending mechanism:

```typescript
// From cmdSenderShared.ts
export function sendCmdMessage(rootMsg: Cmd.Root): void {
    // Check read-only mode
    if (isReadOnlyMode() && !rootMsg.ping && !rootMsg.frozen) {
        return;  // Block non-essential commands
    }

    // Set protocol version
    rootMsg.protocolVersion = 1;
    
    // Encode protobuf message to binary
    const encodedMessage = Cmd.Root.encode(rootMsg).finish();
    
    // Determine if message should be buffered
    let shouldBuffer = true;
    if (rootMsg.ping) {
        shouldBuffer = false;  // Ping messages bypass queue
    }
    
    // Post to BroadcastChannel for worker
    cmdChannel.postMessage({
        pld: encodedMessage,      // Binary payload
        shouldBuffer: shouldBuffer // Queue if disconnected
    });
}

// BroadcastChannel connects to WebSocket/WebTransport worker
export const cmdChannel = new BroadcastChannel('cmd');
```

### 5. WebSocket/WebTransport Communication

The [`cmdWorker.js`](examples/web/frontend/js/cmdWorker.js) handles the actual network communication:

```javascript
// Send command data - implements lazy reconnection
function sendCommand(data) {
    if (!data || !data.pld) return;
    
    // If connected, send immediately
    if (state.ws && state.ws.readyState === WebSocket.OPEN) {
        try {
            state.ws.send(data.pld);  // Send binary protobuf
            return;
        } catch (error) {
            logWorker(`Error sending command: ${error}`, 'error');
        }
    }
    
    // Queue message if buffering enabled
    if (data.shouldBuffer !== false) {
        state.messageQueue.push(data);
        // Limit queue size to prevent memory issues
        if (state.messageQueue.length > 100) {
            state.messageQueue.shift(); // Remove oldest
        }
    }
    
    // Attempt reconnection if needed
    if (!state.isConnecting && (!state.ws || state.ws.readyState !== WebSocket.OPEN)) {
        logWorker('Not connected, attempting to reconnect...', 'info');
        connect();
    }
}
```

The [`CmdConnectionManager`](examples/web/frontend/js/cmdConnectionManager.js) determines transport type:

```javascript
shouldUseWebTransport(transportMode) {
    // Legacy mode forces WebSocket
    if (window.location.search.includes('mode=legacy')) {
        return false;
    }
    
    // Check browser support
    if (typeof WebTransport === 'undefined') {
        return false;
    }
    
    // Use transport mode preference
    return transportMode === 'udp' || 
           (transportMode === 'auto' && !window.location.search.includes('mode=legacy'));
}

// WebSocket endpoint
getWebSocketBaseUrl() {
    const isSecure = window.location.protocol === 'https:';
    const wsProtocol = isSecure ? 'wss' : 'ws';
    return `${wsProtocol}://${currentDomain}${port}`;
}

// WebTransport endpoint  
getWebTransportUrl() {
    const wtPort = 4083; // CMD WebTransport port
    return `https://${currentDomain}:${wtPort}/`;
}
```

## Gesture Command Summary

| Gesture | Action | Command | NDC Usage |
|---------|--------|---------|-----------|
| **Tap** | Rotate camera to position | `RotateToNDC(channel, ndcX, ndcY)` | Tap position in NDC |
| **Double Tap** | Start tracking at position | `CVStartTrackNDC(channel, ndcX, ndcY)` | Tap position in NDC |
| **Pan Start** | Begin continuous rotation | `rotaryPanController.startPan()` | Start position recorded |
| **Pan Move** | Update rotation speed/direction | `rotateBoth(azSpeed, azDir, elSpeed, elDir)` | NDC deltas control speed |
| **Pan Stop** | Stop rotation | `rotaryHalt()` | - |
| **Pinch In** | Zoom in | `heatCameraNextZoomTablePos()` | - |
| **Pinch Out** | Zoom out | `heatCameraPrevZoomTablePos()` | - |
| **Swipe** | UI navigation | Custom callback | Direction only |

## Key Implementation Details

### 1. Pointer Capture
The system uses pointer capture to ensure smooth gesture tracking:
```typescript
this.element.setPointerCapture(event.pointerId);
```

### 2. Touch Action Prevention
CSS touch-action is disabled to prevent browser gestures:
```typescript
this.element.style.touchAction = 'none';
```

### 3. Aspect Ratio Compensation
Pan deltas are adjusted for aspect ratio:
```typescript
const adjustedNdcDeltaX = ndcDeltaX * aspectRatio;
```

### 4. Dead Zone Implementation
Small movements are ignored to prevent jitter:
```typescript
const magnitude = Math.sqrt(ndcDeltaX * ndcDeltaX + ndcDeltaY * ndcDeltaY);
this.isInDeadZone = magnitude <= this.currentConfig.deadZoneRadius;
```

### 5. Periodic Command Sending
Commands are sent at fixed intervals during panning:
```typescript
private startPeriodicUpdate(): void {
    this.updateInterval = setInterval(() => {
        this.sendRotaryCommands();
    }, RotaryPanController.UPDATE_INTERVAL); // 120ms
}
```

## Read-Only Mode

In read-only mode (`?interaction=readonly`), all commands except ping and frozen are blocked:

```typescript
function isReadOnlyMode(): boolean {
    return window.location.search.includes('interaction=readonly');
}

export function sendCmdMessage(rootMsg: Cmd.Root): void {
    if (isReadOnlyMode() && !rootMsg.ping && !rootMsg.frozen) {
        return;
    }
    // ... send command
}
```

## Server Communication Details

### Command Message Structure

All commands follow a hierarchical protobuf structure:

```typescript
// Root command message
Cmd.Root {
    protocolVersion: 1,
    // One of these command types:
    rotary?: Cmd.RotaryPlatform.Root,
    dayCamera?: Cmd.DayCamera.Root,
    heatCamera?: Cmd.HeatCamera.Root,
    cv?: Cmd.CV.Root,
    system?: Cmd.System.Root,
    // ... other subsystems
}
```

### Server Endpoints

The system supports two transport protocols:

1. **WebSocket (TCP)**
   - Endpoint: `wss://[domain]/ws/cmd` (port 443/8083)
   - Binary messages (ArrayBuffer)
   - Automatic reconnection with message queuing
   - Fallback for legacy browsers

2. **WebTransport (UDP)**
   - Endpoint: `https://[domain]:4083/`
   - Lower latency for real-time control
   - Requires certificate hash validation
   - Preferred for modern browsers

### Command Flow Architecture

```
User Gesture
    ↓
InteractionObserverElement (Pointer Events)
    ↓
PointerGestureRecognizer (Gesture Detection)
    ↓
InteractionHandler (Gesture → Command Mapping)
    ↓
Command Functions (Build Protobuf)
    ↓
sendCmdMessage (Encode & Route)
    ↓
BroadcastChannel ('cmd')
    ↓
cmdWorker.js (WebSocket/WebTransport)
    ↓
Server (Port 8083/4083)
```

### Special Command Behaviors

#### Frame-Synchronized Commands

CV tracking commands include frame timestamps for precise synchronization:

```typescript
// CVStartTrackNDC fetches current frame data
const frameData = await dispatch.getDayFrameData();
// Includes: timestamp (BigInt), duration (BigInt)

// Converts to protobuf-compatible Long type
const frameTimeLong = Long.fromString(frameData.timestamp.toString(), true);
```

#### Continuous Commands

Pan gestures send rotation commands every 120ms:

```typescript
// RotaryPanController periodic update
private startPeriodicUpdate(): void {
    this.updateInterval = setInterval(() => {
        this.sendRotaryCommands();
    }, RotaryPanController.UPDATE_INTERVAL); // 120ms
}
```

#### Command Buffering

Messages are queued when disconnected (except ping):

```typescript
// In cmdWorker.js
if (data.shouldBuffer !== false) {
    state.messageQueue.push(data);
    if (state.messageQueue.length > 100) {
        state.messageQueue.shift(); // FIFO with size limit
    }
}
```

### Read-Only Mode Protection

Commands are filtered based on URL parameters:

```typescript
// Check ?interaction=readonly
if (isReadOnlyMode() && !rootMsg.ping && !rootMsg.frozen) {
    return; // Only ping and frozen allowed
}
```

## Integration Points

1. **InteractionObserverElement**: Captures raw pointer events
2. **PointerGestureRecognizer**: Converts events to gestures  
3. **InteractionHandler**: Maps gestures to camera commands
4. **RotaryPanController**: Manages continuous pan movement
5. **Command Senders**: Create and send protobuf messages
6. **BroadcastChannel**: Communicates with WebSocket worker
7. **cmdWorker**: Handles network transport and queuing
8. **Server**: Processes commands on ports 8083 (WS) or 4083 (WT)

This architecture provides smooth, responsive camera control through intuitive touch and mouse gestures, with all calculations performed in normalized device coordinates for consistency across different screen sizes and resolutions. The system handles network disconnections gracefully through message queuing and automatic reconnection.