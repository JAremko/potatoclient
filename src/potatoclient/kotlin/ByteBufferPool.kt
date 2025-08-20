package potatoclient.kotlin

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * High-performance thread-safe pool for direct ByteBuffers.
 * Optimized for video streaming with zero-allocation on the hot path.
 */
class ByteBufferPool(
    private val maxPoolSize: Int,
    private val bufferSize: Int,
    private val direct: Boolean,
) {
    // Use ConcurrentLinkedQueue for lock-free operations
    private val pool = ConcurrentLinkedQueue<ByteBuffer>()

    // Use AtomicInteger for thread-safe size tracking
    private val currentSize = AtomicInteger(0)

    init {
        // Pre-allocate half the pool to reduce startup allocations
        val preAllocate = maxPoolSize / 2
        val buffers = ArrayList<ByteBuffer>(preAllocate)
        repeat(preAllocate) {
            buffers.add(allocateBuffer())
        }
        // Add all at once to minimize contention
        pool.addAll(buffers)
        currentSize.set(preAllocate)
    }

    /**
     * Acquire a buffer from the pool or allocate a new one.
     * The returned buffer is cleared and ready for use.
     */
    fun acquire(): ByteBuffer {
        // Fast path - try to get from pool
        val buffer = pool.poll()
        return if (buffer != null) {
            currentSize.decrementAndGet()
            buffer.clear()
            buffer
        } else {
            // Pool miss - allocate new buffer
            allocateBuffer()
        }
    }

    /**
     * Return a buffer to the pool for reuse.
     * The buffer will be cleared before being returned to the pool.
     */
    fun release(buffer: ByteBuffer?) {
        // Fast path checks
        if (buffer == null || buffer.capacity() != bufferSize) {
            return // Don't pool wrong-sized buffers
        }

        // Check if pool has space
        if (currentSize.get() < maxPoolSize) {
            buffer.clear()
            if (pool.offer(buffer)) {
                currentSize.incrementAndGet()
            }
            // If offer fails (extremely rare), let GC handle it
        }
        // Pool is full, let GC handle it
    }

    /**
     * Acquire a buffer and ensure it has at least the requested capacity.
     * If the standard buffer is too small, a larger non-pooled buffer is returned.
     */
    fun acquireWithCapacity(minCapacity: Int): ByteBuffer =
        if (minCapacity <= bufferSize) {
            acquire()
        } else {
            // Need larger buffer - don't use pool
            if (direct) ByteBuffer.allocateDirect(minCapacity) else ByteBuffer.allocate(minCapacity)
        }

    private fun allocateBuffer(): ByteBuffer =
        if (direct) {
            ByteBuffer.allocateDirect(bufferSize)
        } else {
            ByteBuffer.allocate(bufferSize)
        }

    /**
     * Get the current pool size (for testing/monitoring).
     */
    fun getCurrentSize(): Int = currentSize.get()

    /**
     * Get the maximum pool size.
     */
    fun getMaxPoolSize(): Int = maxPoolSize

    /**
     * Get the buffer size.
     */
    fun getBufferSize(): Int = bufferSize

    /**
     * Check if this pool uses direct buffers.
     */
    fun isDirect(): Boolean = direct

    /**
     * Clear the pool and release resources.
     */
    fun clear() {
        while (pool.poll() != null) {
            currentSize.decrementAndGet()
            // Direct buffers will be cleaned up by GC
        }
    }
}
