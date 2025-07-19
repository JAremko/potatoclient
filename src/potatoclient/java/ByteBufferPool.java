package potatoclient.java;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-performance thread-safe pool for direct ByteBuffers.
 * Optimized for video streaming with zero-allocation on the hot path.
 */
public class ByteBufferPool {
    private final ConcurrentLinkedQueue<ByteBuffer> pool = new ConcurrentLinkedQueue<>();
    private final int maxPoolSize;
    private final int bufferSize;
    private final boolean direct;
    private final AtomicInteger currentSize = new AtomicInteger(0);
    
    // Performance metrics
    private final AtomicLong acquireCount = new AtomicLong(0);
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong returnCount = new AtomicLong(0);
    private final AtomicLong dropCount = new AtomicLong(0);
    
    public ByteBufferPool(int maxPoolSize, int bufferSize, boolean direct) {
        this.maxPoolSize = maxPoolSize;
        this.bufferSize = bufferSize;
        this.direct = direct;
        
        // Pre-allocate half the pool to reduce startup allocations
        int preAllocate = maxPoolSize / 2;
        for (int i = 0; i < preAllocate; i++) {
            ByteBuffer buffer = allocateBuffer();
            pool.offer(buffer);
            currentSize.incrementAndGet();
        }
    }
    
    /**
     * Acquire a buffer from the pool or allocate a new one.
     * The returned buffer is cleared and ready for use.
     */
    public ByteBuffer acquire() {
        acquireCount.incrementAndGet();
        
        ByteBuffer buffer = pool.poll();
        if (buffer != null) {
            hitCount.incrementAndGet();
            currentSize.decrementAndGet();
            buffer.clear();
            return buffer;
        }
        
        // Pool miss - allocate new buffer
        missCount.incrementAndGet();
        return allocateBuffer();
    }
    
    /**
     * Return a buffer to the pool for reuse.
     * The buffer will be cleared before being returned to the pool.
     */
    public void release(ByteBuffer buffer) {
        if (buffer == null || buffer.capacity() != bufferSize) {
            return; // Don't pool wrong-sized buffers
        }
        
        returnCount.incrementAndGet();
        
        // Only return to pool if under capacity
        if (currentSize.get() < maxPoolSize) {
            buffer.clear();
            if (pool.offer(buffer)) {
                currentSize.incrementAndGet();
            } else {
                dropCount.incrementAndGet();
            }
        } else {
            dropCount.incrementAndGet();
            // Let GC handle it
        }
    }
    
    /**
     * Acquire a buffer and ensure it has at least the requested capacity.
     * If the standard buffer is too small, a larger non-pooled buffer is returned.
     */
    public ByteBuffer acquireWithCapacity(int minCapacity) {
        if (minCapacity <= bufferSize) {
            return acquire();
        }
        
        // Need larger buffer - don't use pool
        missCount.incrementAndGet();
        return direct ? ByteBuffer.allocateDirect(minCapacity) : ByteBuffer.allocate(minCapacity);
    }
    
    private ByteBuffer allocateBuffer() {
        return direct ? ByteBuffer.allocateDirect(bufferSize) : ByteBuffer.allocate(bufferSize);
    }
    
    /**
     * Get pool statistics for monitoring
     */
    public PoolStats getStats() {
        long acquires = acquireCount.get();
        long hits = hitCount.get();
        long misses = missCount.get();
        double hitRate = acquires > 0 ? (double) hits / acquires : 0.0;
        
        return new PoolStats(
            currentSize.get(),
            maxPoolSize,
            bufferSize,
            direct,
            acquires,
            hits,
            misses,
            returnCount.get(),
            dropCount.get(),
            hitRate
        );
    }
    
    /**
     * Clear the pool and release resources
     */
    public void clear() {
        while (pool.poll() != null) {
            currentSize.decrementAndGet();
            // Direct buffers will be cleaned up by GC
        }
    }

    public record PoolStats(int currentSize, int maxSize, int bufferSize, boolean direct, long acquireCount,
                            long hitCount, long missCount, long returnCount, long dropCount, double hitRate) {

        @Override
            public String toString() {
                return String.format(
                        "ByteBufferPool[size=%d/%d, bufferSize=%d, direct=%b, " +
                                "acquires=%d, hits=%d, misses=%d, returns=%d, drops=%d, hitRate=%.2f%%]",
                        currentSize, maxSize, bufferSize, direct,
                        acquireCount, hitCount, missCount, returnCount, dropCount, hitRate * 100
                );
            }
        }
}