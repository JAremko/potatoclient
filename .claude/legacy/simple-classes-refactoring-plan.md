# Simple* Classes Refactoring Plan

## Overview
The Simple* classes were created to avoid Transit ReadHandler complexity, but with our new Action Registry, we can create a cleaner architecture.

## Current Simple* Classes

1. **SimpleCommandBuilder.kt** - Hardcoded command building with giant switch statement
2. **SimpleCommandHandlers.kt** - Another command builder, seems redundant
3. **SimpleProtobufHandlers.kt** - Transit write handlers for protobuf state messages

## Problems with Current Approach

1. **Duplication**: Both SimpleCommandBuilder and SimpleCommandHandlers do similar things
2. **Hardcoded Logic**: Giant switch statements with hardcoded command building
3. **No Validation**: No parameter validation using Action Registry
4. **Maintenance Burden**: Need to update multiple places when adding new commands
5. **Name Confusion**: "Simple" prefix doesn't convey what these classes actually do

## Proposed Refactoring

### 1. Replace SimpleCommandBuilder with ProtobufCommandBuilder

Create a new `ProtobufCommandBuilder` that:
- Uses Action Registry for validation
- Has a modular design with command builders per proto category
- Provides better error messages using registry metadata
- Single source of truth for command building

```kotlin
package potatoclient.kotlin.transit

class ProtobufCommandBuilder {
    private val builders = mapOf(
        "rotary" to RotaryCommandBuilder(),
        "cv" to CVCommandBuilder(),
        // etc...
    )
    
    fun buildCommand(action: String, params: Map<*, *>?): Result<JonSharedCmd.Root> {
        // Use ActionRegistry to validate
        if (!ActionRegistry.isKnownAction(action)) {
            return Result.failure(UnknownCommandException(action))
        }
        
        if (!ActionRegistry.hasRequiredParams(action, params ?: emptyMap())) {
            val missing = getMissingParams(action, params)
            return Result.failure(MissingParametersException(action, missing))
        }
        
        // Delegate to appropriate builder
        val category = getCommandCategory(action)
        val builder = builders[category] 
            ?: return Result.failure(NoBuilderException(action))
            
        return builder.build(action, params)
    }
}
```

### 2. Remove SimpleCommandHandlers

This class is redundant with SimpleCommandBuilder. Consolidate into the new ProtobufCommandBuilder.

### 3. Rename SimpleProtobufHandlers to ProtobufStateHandlers

The "Simple" prefix is misleading. This class handles protobuf state serialization to Transit, which is its own concern separate from commands.

```kotlin
package potatoclient.kotlin.transit

/**
 * Transit write handlers for protobuf state messages.
 * Converts protobuf state objects to Transit-compatible maps.
 */
object ProtobufStateHandlers {
    // Same implementation, better name
}
```

### 4. Create Command Builder Modules

Instead of one giant file, create focused builders:

```
potatoclient.kotlin.transit.builders/
├── BasicCommandBuilder.kt
├── RotaryCommandBuilder.kt  
├── CVCommandBuilder.kt
├── SystemCommandBuilder.kt
├── CameraCommandBuilder.kt
└── ... (one per proto category)
```

Each builder:
- Handles commands for one proto category
- Has focused, maintainable code
- Can be tested independently
- Follows single responsibility principle

### 5. Integration Points

Update these files to use new architecture:
- `CommandSubprocess.kt` - Use new ProtobufCommandBuilder
- `TransitMessageProcessor.kt` - Update imports
- Tests - Update to test new structure

## Benefits

1. **Single Source of Truth**: Action Registry defines all commands
2. **Better Validation**: Automatic parameter validation
3. **Cleaner Code**: Modular design, no giant switch statements  
4. **Better Errors**: Can provide helpful error messages using registry metadata
5. **Easier Maintenance**: Add new commands in one place (registry + builder module)
6. **Clear Names**: No more confusing "Simple" prefix

## Migration Strategy

1. Create new ProtobufCommandBuilder alongside existing code
2. Create builder modules one at a time
3. Update CommandSubprocess to use new builder
4. Test thoroughly
5. Remove old Simple* classes
6. Update documentation

## Testing Strategy

1. Unit tests for each command builder module
2. Integration tests using Action Registry
3. End-to-end tests with actual protobuf serialization
4. Regression tests to ensure no behavior changes

## TODO

- [ ] Create ProtobufCommandBuilder base class
- [ ] Create builder modules for each proto category  
- [ ] Integrate with Action Registry for validation
- [ ] Update CommandSubprocess to use new builder
- [ ] Write comprehensive tests
- [ ] Remove old Simple* classes
- [ ] Update documentation