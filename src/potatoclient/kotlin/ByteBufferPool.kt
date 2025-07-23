package potatoclient.kotlin

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * High-performance thread-safe pool for direct ByteBuffers.
 * Optimized for video streaming with zero-allocation on the hot path.
 */
class ByteBufferPool(
    private val maxPoolSize: Int,
    private val bufferSize: Int,
    private val direct: Boolean
) {
    // Use ConcurrentLinkedQueue for lock-free operations
    private val pool = ConcurrentLinkedQueue<ByteBuffer>()
    
    // Separate currentSize for fast capacity checks
    @Volatile
    private var currentSize = 0
    
    // Performance metrics - use padding to avoid false sharing
    @Volatile private var p0: Long = 0L; @Volatile private var p1: Long = 0L
    private val acquireCount = AtomicLong(0)
    @Volatile private var p2: Long = 0L; @Volatile private var p3: Long = 0L
    private val hitCount = AtomicLong(0)
    @Volatile private var p4: Long = 0L; @Volatile private var p5: Long = 0L
    private val missCount = AtomicLong(0)
    @Volatile private var p6: Long = 0L; @Volatile private var p7: Long = 0L
    private val returnCount = AtomicLong(0)
    @Volatile private var p8: Long = 0L; @Volatile private var p9: Long = 0L
    private val dropCount = AtomicLong(0)
    
    init {
        // Pre-allocate half the pool to reduce startup allocations
        val preAllocate = maxPoolSize / 2
        val buffers = ArrayList<ByteBuffer>(preAllocate)
        repeat(preAllocate) {
            buffers.add(allocateBuffer())
        }
        // Add all at once to minimize contention
        pool.addAll(buffers)
        currentSize = preAllocate
    }
    
    /**
     * Acquire a buffer from the pool or allocate a new one.
     * The returned buffer is cleared and ready for use.
     */
    fun acquire(): ByteBuffer {
        acquireCount.incrementAndGet()
        
        // Fast path - try to get from pool
        val buffer = pool.poll()
        return if (buffer != null) {
            hitCount.incrementAndGet()
            currentSize--
            buffer.clear()
            buffer
        } else {
            // Pool miss - allocate new buffer
            missCount.incrementAndGet()
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
        
        returnCount.incrementAndGet()
        
        // Quick capacity check without synchronization
        val size = currentSize
        if (size < maxPoolSize) {
            buffer.clear()
            if (pool.offer(buffer)) {
                currentSize = size + 1
            } else {
                // Extremely rare - queue full despite size check
                dropCount.incrementAndGet()
            }
        } else {
            dropCount.incrementAndGet()
            // Pool is full, let GC handle it
        }
    }
    
    /**
     * Acquire a buffer and ensure it has at least the requested capacity.
     * If the standard buffer is too small, a larger non-pooled buffer is returned.
     */
    fun acquireWithCapacity(minCapacity: Int): ByteBuffer {
        return if (minCapacity <= bufferSize) {
            acquire()
        } else {
            // Need larger buffer - don't use pool
            missCount.incrementAndGet()
            if (direct) ByteBuffer.allocateDirect(minCapacity) else ByteBuffer.allocate(minCapacity)
        }
    }
    
    private fun allocateBuffer(): ByteBuffer {
        return if (direct) ByteBuffer.allocateDirect(bufferSize) else ByteBuffer.allocate(bufferSize)
    }
    
    /**
     * Get pool statistics for monitoring
     */
    fun getStats(): PoolStats {
        val acquires = acquireCount.get()
        val hits = hitCount.get()
        val misses = missCount.get()
        val hitRate = if (acquires > 0) hits.toDouble() / acquires else 0.0
        
        return PoolStats(
            currentSize = currentSize,
            maxSize = maxPoolSize,
            bufferSize = bufferSize,
            direct = direct,
            acquireCount = acquires,
            hitCount = hits,
            missCount = misses,
            returnCount = returnCount.get(),
            dropCount = dropCount.get(),
            hitRate = hitRate
        )
    }
    
    /**
     * Clear the pool and release resources
     */
    fun clear() {
        while (pool.poll() != null) {
            currentSize--
            // Direct buffers will be cleaned up by GC
        }
    }

    data class PoolStats(
        val currentSize: Int,
        val maxSize: Int,
        val bufferSize: Int,
        val direct: Boolean,
        val acquireCount: Long,
        val hitCount: Long,
        val missCount: Long,
        val returnCount: Long,
        val dropCount: Long,
        val hitRate: Double
    ) {
        override fun toString(): String {
            return "ByteBufferPool[size=$currentSize/$maxSize, bufferSize=$bufferSize, direct=$direct, " +
                    "acquires=$acquireCount, hits=$hitCount, misses=$missCount, returns=$returnCount, " +
                    "drops=$dropCount, hitRate=${String.format("%.2f", hitRate * 100)}%]"
        }
    }
}