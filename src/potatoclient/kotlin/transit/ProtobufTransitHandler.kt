package potatoclient.kotlin.transit

import com.cognitect.transit.Keyword
import com.cognitect.transit.ReadHandler
import com.cognitect.transit.Reader
import com.cognitect.transit.Symbol
import com.cognitect.transit.TransitFactory
import com.cognitect.transit.WriteHandler
import com.cognitect.transit.Writer
import com.google.protobuf.Message
import java.lang.reflect.Method

/**
 * Transit handler that uses metadata to instantiate the correct protobuf class.
 *
 * The Clojure side sends maps with metadata containing:
 * - :proto-type - The fully qualified Java class name
 * - :proto-path - The logical path in the command hierarchy
 *
 * This handler reads the metadata and uses reflection to build the correct
 * protobuf message.
 */
class ProtobufTransitHandler {
    /**
     * Custom ReadHandler that processes wrapped protobuf commands
     */
    class ProtobufReadHandler : ReadHandler<Message, Map<*, *>> {
        override fun fromRep(rep: Map<*, *>): Message {
            // Check if this is a wrapped protobuf command
            val type = rep[TransitFactory.keyword("type")] as? String

            if (type == "protobuf-command") {
                // Extract type information and data
                val protoType =
                    rep[TransitFactory.keyword("proto-type")] as? String
                        ?: throw IllegalArgumentException("No proto-type found in protobuf command")

                val protoPath = rep[TransitFactory.keyword("proto-path")] as? List<*>
                val data =
                    rep[TransitFactory.keyword("data")] as? Map<*, *>
                        ?: throw IllegalArgumentException("No data found in protobuf command")

                return buildProtobuf(protoType, data, protoPath)
            } else {
                // Fallback: try to find embedded metadata (older approach)
                val metaKey = TransitFactory.keyword("proto-meta")
                val metadata = rep[metaKey] as? Map<*, *>

                if (metadata != null) {
                    val protoType =
                        metadata[TransitFactory.keyword("proto-type")] as? String
                            ?: throw IllegalArgumentException("No proto-type in metadata")

                    val protoPath = metadata[TransitFactory.keyword("proto-path")] as? List<*>
                    val data = rep.filterKeys { it != metaKey }

                    return buildProtobuf(protoType, data, protoPath)
                } else {
                    throw IllegalArgumentException("Not a valid protobuf command structure")
                }
            }
        }

        private fun buildProtobuf(
            className: String,
            data: Map<*, *>,
            path: List<*>?,
        ): Message {
            try {
                // Load the protobuf class
                val clazz = Class.forName(className)

                // Get the newBuilder method
                val newBuilderMethod = clazz.getMethod("newBuilder")
                val builder = newBuilderMethod.invoke(null) as Message.Builder

                // Populate the builder based on the data structure
                populateBuilder(builder, data, path)

                return builder.build()
            } catch (e: Exception) {
                throw IllegalArgumentException(
                    "Failed to build protobuf of type $className: ${e.message}",
                    e,
                )
            }
        }

        private fun populateBuilder(
            builder: Message.Builder,
            data: Map<*, *>,
            path: List<*>?,
        ) {
            // For each field in the data map
            data.forEach { (key, value) ->
                val fieldName =
                    when (key) {
                        is Keyword -> key.toString()
                        else -> key.toString()
                    }

                // Convert kebab-case to snake_case for protobuf
                val protoFieldName = fieldName.replace("-", "_")

                when (value) {
                    is Map<*, *> -> {
                        // Nested message - need to handle submessage building
                        handleNestedMessage(builder, protoFieldName, value)
                    }
                    is List<*> -> {
                        // Repeated field
                        handleRepeatedField(builder, protoFieldName, value)
                    }
                    else -> {
                        // Simple field
                        setFieldValue(builder, protoFieldName, value)
                    }
                }
            }
        }

        private fun handleNestedMessage(
            parentBuilder: Message.Builder,
            fieldName: String,
            nestedData: Map<*, *>,
        ) {
            // Find the nested builder method (e.g., getGotoBuilder)
            val getBuilderMethodName = "get${capitalize(fieldName)}Builder"

            try {
                val getBuilderMethod = parentBuilder.javaClass.getMethod(getBuilderMethodName)
                val nestedBuilder = getBuilderMethod.invoke(parentBuilder) as Message.Builder

                // Recursively populate the nested builder
                populateBuilder(nestedBuilder, nestedData, null)

                // The nested builder is automatically set on the parent
            } catch (e: NoSuchMethodException) {
                // Try alternate method names or handle specially
                handleSpecialCases(parentBuilder, fieldName, nestedData)
            }
        }

        private fun handleRepeatedField(
            builder: Message.Builder,
            fieldName: String,
            values: List<*>,
        ) {
            // Handle repeated fields - implementation depends on field type
            val addMethodName = "add${capitalize(fieldName)}"

            values.forEach { value ->
                try {
                    val addMethod = findMethod(builder.javaClass, addMethodName, value)
                    addMethod?.invoke(builder, convertValue(value))
                } catch (e: Exception) {
                    println("Failed to add repeated value: ${e.message}")
                }
            }
        }

        private fun setFieldValue(
            builder: Message.Builder,
            fieldName: String,
            value: Any?,
        ) {
            if (value == null) return

            val setMethodName = "set${capitalize(fieldName)}"

            try {
                val setMethod = findMethod(builder.javaClass, setMethodName, value)
                setMethod?.invoke(builder, convertValue(value))
            } catch (e: Exception) {
                println("Failed to set field $fieldName: ${e.message}")
            }
        }

        private fun handleSpecialCases(
            builder: Message.Builder,
            fieldName: String,
            data: Map<*, *>,
        ) {
            // Handle special cases like oneof fields
            // For example, Root messages often have multiple possible submessages

            when (fieldName) {
                "goto", "start", "stop", "halt" -> {
                    // These might be oneof cases in RotaryPlatform
                    val methodName = "set${capitalize(fieldName)}"
                    try {
                        // First try to get the submessage builder
                        val innerBuilderMethod =
                            builder.javaClass.getMethod(
                                "${methodName}Builder",
                            )
                        // This returns the builder directly set on parent
                    } catch (e: Exception) {
                        // Fallback handling
                    }
                }
            }
        }

        private fun findMethod(
            clazz: Class<*>,
            methodName: String,
            paramValue: Any?,
        ): Method? =
            clazz.methods.find { method ->
                method.name == methodName &&
                    method.parameterCount == 1 &&
                    isCompatibleType(method.parameterTypes[0], paramValue)
            }

        private fun isCompatibleType(
            paramType: Class<*>,
            value: Any?,
        ): Boolean {
            if (value == null) return !paramType.isPrimitive

            return when {
                paramType.isAssignableFrom(value.javaClass) -> true
                paramType == Double::class.java && value is Number -> true
                paramType == Float::class.java && value is Number -> true
                paramType == Long::class.java && value is Number -> true
                paramType == Integer::class.java && value is Number -> true
                paramType == String::class.java -> true
                else -> false
            }
        }

        private fun convertValue(value: Any?): Any? =
            when (value) {
                is Keyword -> value.toString()
                is Symbol -> value.toString()
                is Long -> value
                is Double -> value
                is Float -> value
                is Boolean -> value
                is String -> value
                else -> value
            }

        private fun capitalize(s: String): String =
            s.split("_").joinToString("") { part ->
                part.replaceFirstChar { it.uppercase() }
            }
    }

    /**
     * Custom WriteHandler for protobuf messages to Transit
     */
    class ProtobufWriteHandler : WriteHandler<Message, Map<String, Any?>> {
        override fun tag(o: Message): String = "protobuf"

        override fun rep(o: Message): Map<String, Any?> {
            // Convert protobuf to map representation
            // This would use the Message's descriptor to extract fields
            return protobufToMap(o)
        }

        override fun stringRep(o: Message): String? = null

        override fun <V> getVerboseHandler(): WriteHandler<Message, V>? = null

        private fun protobufToMap(message: Message): Map<String, Any?> {
            val result = mutableMapOf<String, Any?>()

            message.allFields.forEach { (field, value) ->
                val fieldName = field.name.replace("_", "-")
                result[fieldName] =
                    when {
                        field.isRepeated -> (value as List<*>).map { convertFieldValue(it) }
                        field.type == com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE ->
                            protobufToMap(value as Message)
                        else -> convertFieldValue(value)
                    }
            }

            return result
        }

        private fun convertFieldValue(value: Any?): Any? =
            when (value) {
                is Message -> protobufToMap(value)
                is Enum<*> -> value.name.lowercase().replace("_", "-")
                else -> value
            }
    }

    companion object {
        /**
         * Create a Transit reader with protobuf support
         */
        fun createReader(inputStream: java.io.InputStream): Reader {
            val customHandlers =
                mapOf(
                    "protobuf" to ProtobufReadHandler(),
                )

            return TransitFactory.reader(
                TransitFactory.Format.MSGPACK,
                inputStream,
                customHandlers,
            )
        }

        /**
         * Create a Transit writer with protobuf support
         */
        fun createWriter(outputStream: java.io.OutputStream): Writer<Any> {
            val customHandlers = mutableMapOf<Class<*>, WriteHandler<*, *>>()
            customHandlers[Message::class.java] = ProtobufWriteHandler()

            return TransitFactory.writer<Any>(
                TransitFactory.Format.MSGPACK,
                outputStream,
                TransitFactory.writeHandlerMap(customHandlers),
            )
        }
    }
}

/**
 * Extension functions for cleaner usage
 */
fun TransitCommunicator.withProtobufSupport(): TransitCommunicator {
    // This would wrap the communicator with protobuf handlers
    // Implementation depends on TransitCommunicator structure
    return this
}
