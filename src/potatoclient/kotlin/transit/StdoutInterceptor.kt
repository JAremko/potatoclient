package potatoclient.transit

import java.io.OutputStream
import java.io.PrintStream

/**
 * Intercepts stdout and redirects it through Transit messaging
 * This prevents any accidental println or System.out.print from interfering with Transit protocol
 */
class StdoutInterceptor {
    companion object {
        // Store original stdout before any modifications
        private val originalStdout: PrintStream = System.out

        /**
         * Install the stdout interceptor BEFORE Transit is initialized
         * The messageProtocol will be set later when it's available
         */
        fun installEarly() {
            val interceptor = InterceptingPrintStream()
            System.setOut(interceptor)
        }

        /**
         * Set the message protocol for the interceptor
         */
        fun setMessageProtocol(messageProtocol: TransitMessageProtocol) {
            val out = System.out
            if (out is InterceptingPrintStream) {
                out.setMessageProtocol(messageProtocol)
            }
        }

        /**
         * Get the original stdout (for Transit communication only)
         */
        fun getOriginalStdout(): OutputStream = originalStdout
    }
}

/**
 * Custom PrintStream that intercepts all output and redirects through Transit
 */
private class InterceptingPrintStream : PrintStream(BufferedOutputStream(), true) {
    private var messageProtocol: TransitMessageProtocol? = null
    private val bufferedMessages = mutableListOf<String>()

    /**
     * Set the message protocol - will flush any buffered messages
     */
    fun setMessageProtocol(protocol: TransitMessageProtocol) {
        synchronized(this) {
            messageProtocol = protocol
            // Flush any buffered messages
            bufferedMessages.forEach { msg ->
                try {
                    protocol.sendInfo("[STDOUT-BUFFERED] $msg")
                } catch (e: Exception) {
                    System.err.println("[STDOUT-INTERCEPT-ERROR] Failed to send buffered: $msg")
                }
            }
            bufferedMessages.clear()
        }
    }

    override fun println(x: String?) {
        sendMessage(x ?: "null")
    }

    override fun print(x: String?) {
        sendMessage(x ?: "null")
    }

    override fun println(x: Any?) {
        sendMessage(x?.toString() ?: "null")
    }

    override fun print(x: Any?) {
        sendMessage(x?.toString() ?: "null")
    }

    override fun println() {
        sendMessage("")
    }

    override fun println(x: Boolean) {
        sendMessage(x.toString())
    }

    override fun print(x: Boolean) {
        sendMessage(x.toString())
    }

    override fun println(x: Char) {
        sendMessage(x.toString())
    }

    override fun print(x: Char) {
        sendMessage(x.toString())
    }

    override fun println(x: Int) {
        sendMessage(x.toString())
    }

    override fun print(x: Int) {
        sendMessage(x.toString())
    }

    override fun println(x: Long) {
        sendMessage(x.toString())
    }

    override fun print(x: Long) {
        sendMessage(x.toString())
    }

    override fun println(x: Float) {
        sendMessage(x.toString())
    }

    override fun print(x: Float) {
        sendMessage(x.toString())
    }

    override fun println(x: Double) {
        sendMessage(x.toString())
    }

    override fun print(x: Double) {
        sendMessage(x.toString())
    }

    override fun println(x: CharArray) {
        sendMessage(String(x))
    }

    override fun print(x: CharArray) {
        sendMessage(String(x))
    }

    private fun sendMessage(message: String) {
        synchronized(this) {
            val protocol = messageProtocol
            if (protocol != null) {
                try {
                    protocol.sendInfo("[STDOUT] $message")
                } catch (e: Exception) {
                    // Fallback to stderr if Transit fails
                    System.err.println("[STDOUT-INTERCEPT-ERROR] Failed to send: $message")
                    System.err.println("[STDOUT-INTERCEPT-ERROR] ${e.message}")
                }
            } else {
                // Buffer messages until protocol is available
                bufferedMessages.add(message)
                if (bufferedMessages.size > 1000) {
                    // Prevent unbounded growth - drop oldest messages
                    bufferedMessages.removeAt(0)
                }
            }
        }
    }
}

/**
 * Dummy output stream for the intercepting PrintStream
 */
private class BufferedOutputStream : OutputStream() {
    override fun write(b: Int) {
        // Do nothing - all output is intercepted at PrintStream level
    }
}
