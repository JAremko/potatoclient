# System Architecture Overview

PotatoClient is a high-performance multi-process video streaming client built with a clean separation of concerns between UI (Clojure) and system integration (Kotlin).

## Multi-Process Architecture

```
┌─────────────────┐     Transit/IPC    ┌──────────────────┐
│  Main Process   │ ←────────────────→ │ Command Process  │
│   (Clojure)     │                    │    (Kotlin)      │
│  - UI (Swing)   │                    │ - Protobuf       │
│  - State Mgmt   │                    │ - WebSocket      │
│  - Transit IPC  │                    └──────────────────┘
│  - No Protobuf  │     Transit/IPC    ┌──────────────────┐
└─────────────────┘ ←────────────────→ │ State Process    │
         ↑                             │    (Kotlin)      │
         │                             │ - Protobuf       │
         │         Transit/IPC         │ - Debouncing     │
         ├────────────────────────────→└──────────────────┘
         │                             ┌──────────────────┐
         └────────────────────────────→│ Video Stream 1   │
                                       │    (Kotlin)      │
                   Transit/IPC         │ - H.264/GStreamer│
         └────────────────────────────→│ - Zero-alloc     │
                                       └──────────────────┘
                                       ┌──────────────────┐
                                       │ Video Stream 2   │
                                       └──────────────────┘
```

## Process Responsibilities

### Main Process (Clojure)
- **UI Management**: Swing-based user interface
- **State Management**: Single app-db atom (re-frame pattern)
- **Process Coordination**: Launches and manages subprocesses
- **Message Routing**: Routes commands and state updates
- **No Protobuf**: Never touches protobuf directly

### Command Subprocess (Kotlin)
- **Command Translation**: Transit → Protobuf conversion
- **WebSocket Client**: Sends commands to server
- **Static Code Generation**: Uses generated handlers
- **Stateless**: No persistent state

### State Subprocess (Kotlin)
- **State Reception**: Receives protobuf state from server
- **State Translation**: Protobuf → Transit conversion
- **Debouncing**: Prevents duplicate state updates
- **WebSocket Client**: Maintains state connection

### Video Stream Subprocesses (Kotlin)
- **Video Decoding**: Hardware-accelerated H.264
- **Gesture Detection**: Mouse events → commands
- **Command Generation**: High-level commands (not just events)
- **Zero Allocation**: Optimized hot path
- **Frame Management**: Window and rendering

## Communication Protocol

All inter-process communication uses Transit with MessagePack serialization:

```clojure
;; Message envelope
{:msg-type :command     ; Message type
 :msg-id   "uuid"       ; Unique ID
 :timestamp 1234567890  ; Unix timestamp
 :payload  {...}}       ; Message-specific data
```

### Message Types
- `:command` - Control commands
- `:state-update` - Full state updates
- `:state-partial` - Incremental updates
- `:event` - UI events (gesture, window, etc.)
- `:log` - Logging messages
- `:error` - Error reports
- `:metric` - Performance metrics
- `:status` - Process lifecycle
- `:request` - Inter-process requests

## Key Design Principles

### 1. Protobuf Isolation
- Protobuf classes never loaded in Clojure
- All protobuf handling in Kotlin subprocesses
- Clean Transit interface between processes

### 2. Keywords Everywhere
- All enum values → keywords
- All map keys → keywords
- Only strings: log messages and errors

### 3. Static Code Generation
- No runtime reflection
- Generated handlers for Transit ↔ Protobuf
- Automatic command routing

### 4. Clean Architecture
- No backward compatibility
- No legacy code
- Single approach to each problem

### 5. Performance First
- Zero-allocation video path
- Lock-free communication
- Hardware acceleration

## Data Flow

### Command Flow
```
User Action → Clojure UI → Transit Command → Command Subprocess 
→ Protobuf → WebSocket → Server
```

### State Flow
```
Server → WebSocket → Protobuf → State Subprocess 
→ Transit State → Clojure app-db → UI Update
```

### Video Flow
```
Server → H.264 Stream → Video Subprocess → GStreamer Pipeline 
→ Hardware Decoder → Window
```

### Gesture Flow
```
Mouse Event → Gesture Recognizer → Command Builder 
→ Transit Command → Main Process → Command Subprocess
```

## Benefits of This Architecture

1. **Language Strengths**: Clojure for UI logic, Kotlin for system integration
2. **Clean Separation**: UI never touches system-level concerns
3. **Type Safety**: Transit provides schema validation
4. **Testability**: Each process can be tested independently
5. **Performance**: Optimized paths for video and commands
6. **Maintainability**: Clear boundaries and responsibilities