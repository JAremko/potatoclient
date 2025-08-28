package potatoclient.kotlin

import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * Comprehensive tests for ByteBufferPool to ensure correct functionality.
 */
object ByteBufferPoolTest {
    private var testsPassed = 0
    private var testsFailed = 0
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("Running ByteBufferPool Tests...\n")
        
        // Run all tests
        testBasicAcquireRelease()
        testPreAllocation()
        testPoolSizeLimits()
        testWrongSizeBufferRejection()
        testAcquireWithCapacity()
        testClearPool()
        testDirectBuffers()
        testHeapBuffers()
        testConcurrentAcquireRelease()
        testPoolExhaustion()
        testReleaseAfterClear()
        testBufferReuse()
        testBufferState()
        testNullRelease()
        
        println("\n========================================")
        println("Test Results:")
        println("  Passed: $testsPassed")
        println("  Failed: $testsFailed")
        println("========================================")
        
        System.exit(if (testsFailed > 0) 1 else 0)
    }
    
    private fun testBasicAcquireRelease() {
        print("Testing basic acquire/release... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 10, bufferSize = 1024, direct = false)
            
            // Acquire a buffer
            val buffer = pool.acquire()
            assert(buffer.capacity() == 1024) { "Buffer capacity should be 1024" }
            assert(buffer.position() == 0) { "Buffer position should be 0" }
            assert(buffer.limit() == 1024) { "Buffer limit should be capacity" }
            
            // Use the buffer
            buffer.putInt(42)
            
            // Release it back
            pool.release(buffer)
            
            // Acquire again - should get a cleared buffer
            val buffer2 = pool.acquire()
            assert(buffer2.position() == 0) { "Reacquired buffer should be cleared" }
            assert(buffer2.getInt(0) == 0 || buffer2 === buffer) { "Buffer should be cleared or different" }
            
            pool.clear()
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testPreAllocation() {
        print("Testing pre-allocation... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 20, bufferSize = 512, direct = false)
            
            // Should pre-allocate half (10 buffers)
            assert(pool.getCurrentSize() == 10) { "Should pre-allocate 10 buffers, got ${pool.getCurrentSize()}" }
            
            // Acquire all pre-allocated buffers
            val buffers = mutableListOf<ByteBuffer>()
            repeat(10) {
                buffers.add(pool.acquire())
            }
            
            // Pool should be empty now
            assert(pool.getCurrentSize() == 0) { "Pool should be empty after acquiring pre-allocated buffers" }
            
            // Release all back
            buffers.forEach { pool.release(it) }
            
            // Should have 10 buffers again
            assert(pool.getCurrentSize() == 10) { "Should have 10 buffers after releasing" }
            
            pool.clear()
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testPoolSizeLimits() {
        print("Testing pool size limits... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 5, bufferSize = 256, direct = false)
            
            // Acquire and release more than max pool size
            val buffers = mutableListOf<ByteBuffer>()
            repeat(10) {
                buffers.add(pool.acquire())
            }
            
            // Release all - only first 5 should be pooled
            buffers.forEach { pool.release(it) }
            
            // Pool should be at max capacity
            assert(pool.getCurrentSize() == 5) { "Pool should be at max size (5), got ${pool.getCurrentSize()}" }
            
            pool.clear()
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testWrongSizeBufferRejection() {
        print("Testing wrong-size buffer rejection... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 10, bufferSize = 1024, direct = false)
            
            // Create a different sized buffer
            val wrongSizeBuffer = ByteBuffer.allocate(2048)
            
            val initialSize = pool.getCurrentSize()
            
            // Try to release it - should be rejected
            pool.release(wrongSizeBuffer)
            
            // Pool size should not change
            assert(pool.getCurrentSize() == initialSize) { "Pool size should not change when releasing wrong-sized buffer" }
            
            pool.clear()
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testAcquireWithCapacity() {
        print("Testing acquireWithCapacity... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 10, bufferSize = 1024, direct = false)
            
            // Request buffer with capacity less than pool buffer size
            val buffer1 = pool.acquireWithCapacity(512)
            assert(buffer1.capacity() == 1024) { "Should get pooled buffer for smaller capacity request" }
            
            // Request buffer with exact capacity
            val buffer2 = pool.acquireWithCapacity(1024)
            assert(buffer2.capacity() == 1024) { "Should get pooled buffer for exact capacity request" }
            
            // Request buffer with larger capacity
            val buffer3 = pool.acquireWithCapacity(2048)
            assert(buffer3.capacity() == 2048) { "Should get non-pooled buffer for larger capacity request" }
            
            // Release buffers
            pool.release(buffer1)
            pool.release(buffer2)
            pool.release(buffer3) // Should be rejected due to size
            
            // Only first two should be in pool
            val finalSize = pool.getCurrentSize()
            assert(finalSize >= 2) { "At least 2 buffers should be in pool" }
            
            pool.clear()
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testClearPool() {
        print("Testing clear pool... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 10, bufferSize = 512, direct = false)
            
            // Acquire and release some buffers
            val buffers = mutableListOf<ByteBuffer>()
            repeat(5) {
                buffers.add(pool.acquire())
            }
            buffers.forEach { pool.release(it) }
            
            assert(pool.getCurrentSize() > 0) { "Pool should have buffers before clear" }
            
            // Clear the pool
            pool.clear()
            
            assert(pool.getCurrentSize() == 0) { "Pool should be empty after clear" }
            
            // Should still be able to acquire new buffers
            val newBuffer = pool.acquire()
            assert(newBuffer.capacity() == 512) { "New buffer should have correct size" }
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testDirectBuffers() {
        print("Testing direct buffers... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 5, bufferSize = 1024, direct = true)
            
            assert(pool.isDirect()) { "Pool should report direct = true" }
            
            val buffer = pool.acquire()
            assert(buffer.isDirect) { "Acquired buffer should be direct" }
            
            pool.release(buffer)
            pool.clear()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testHeapBuffers() {
        print("Testing heap buffers... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 5, bufferSize = 1024, direct = false)
            
            assert(!pool.isDirect()) { "Pool should report direct = false" }
            
            val buffer = pool.acquire()
            assert(!buffer.isDirect) { "Acquired buffer should be heap buffer" }
            
            pool.release(buffer)
            pool.clear()
            
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testConcurrentAcquireRelease() {
        print("Testing concurrent acquire/release... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 50, bufferSize = 256, direct = false)
            val threadCount = 10
            val operationsPerThread = 100
            val barrier = CyclicBarrier(threadCount)
            val latch = CountDownLatch(threadCount)
            val errors = AtomicInteger(0)
            
            // Launch multiple threads
            repeat(threadCount) {
                thread {
                    try {
                        barrier.await() // Synchronize start
                        
                        repeat(operationsPerThread) {
                            val buffer = pool.acquire()
                            assert(buffer.capacity() == 256) { "Buffer should have correct capacity" }
                            
                            // Simulate some work
                            buffer.putInt(Thread.currentThread().id.toInt())
                            
                            pool.release(buffer)
                        }
                    } catch (e: Exception) {
                        errors.incrementAndGet()
                        e.printStackTrace()
                    } finally {
                        latch.countDown()
                    }
                }
            }
            
            // Wait for all threads
            val completed = latch.await(10, TimeUnit.SECONDS)
            assert(completed) { "Threads did not complete in time" }
            assert(errors.get() == 0) { "Errors occurred in concurrent execution" }
            
            // Pool should still be functional
            val finalBuffer = pool.acquire()
            assert(finalBuffer.capacity() == 256) { "Should be able to acquire after concurrent operations" }
            
            pool.clear()
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testPoolExhaustion() {
        print("Testing pool exhaustion... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 3, bufferSize = 128, direct = false)
            
            // Clear pre-allocated buffers
            pool.clear()
            
            // Acquire more buffers than pool size
            val buffers = mutableListOf<ByteBuffer>()
            repeat(10) {
                val buffer = pool.acquire()
                assert(buffer.capacity() == 128) { "Buffer should have correct size" }
                buffers.add(buffer)
            }
            
            // Release all
            buffers.forEach { pool.release(it) }
            
            // Pool should be at max capacity
            assert(pool.getCurrentSize() == 3) { "Pool should be at max capacity after releasing many buffers" }
            
            pool.clear()
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testReleaseAfterClear() {
        print("Testing release after clear... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 5, bufferSize = 256, direct = false)
            
            val buffer = pool.acquire()
            
            pool.clear()
            assert(pool.getCurrentSize() == 0) { "Pool should be empty after clear" }
            
            // Release buffer after pool was cleared - should work without error
            pool.release(buffer)
            
            // Buffer should be added to the empty pool
            assert(pool.getCurrentSize() == 1) { "Buffer should be added to cleared pool" }
            
            pool.clear()
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testBufferReuse() {
        print("Testing buffer reuse... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 5, bufferSize = 512, direct = false)
            
            // Clear pre-allocated to have predictable behavior
            pool.clear()
            
            // Acquire a buffer and mark it
            val buffer1 = pool.acquire()
            buffer1.putInt(0, 0xDEADBEEF.toInt())
            
            // Release it
            pool.release(buffer1)
            
            // Acquire again - should get the same buffer cleared
            val buffer2 = pool.acquire()
            
            // Buffer should be cleared
            assert(buffer2.position() == 0) { "Reused buffer position should be 0" }
            assert(buffer2.limit() == 512) { "Reused buffer limit should be capacity" }
            
            // On a cleared buffer, the int at position 0 should be 0
            // (unless it's a different buffer)
            if (buffer2 === buffer1) {
                assert(buffer2.getInt(0) == 0) { "Reused buffer should be cleared" }
            }
            
            pool.clear()
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testBufferState() {
        print("Testing buffer state management... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 5, bufferSize = 256, direct = false)
            
            val buffer = pool.acquire()
            
            // Modify buffer state
            buffer.position(100)
            buffer.limit(200)
            
            // Release with modified state
            pool.release(buffer)
            
            // Acquire again
            val buffer2 = pool.acquire()
            
            // Should get a cleared buffer
            assert(buffer2.position() == 0) { "Buffer position should be reset to 0" }
            assert(buffer2.limit() == 256) { "Buffer limit should be reset to capacity" }
            
            pool.clear()
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
    
    private fun testNullRelease() {
        print("Testing null release... ")
        try {
            val pool = ByteBufferPool(maxPoolSize = 5, bufferSize = 256, direct = false)
            
            val initialSize = pool.getCurrentSize()
            
            // Release null - should not crash or change pool
            pool.release(null)
            
            assert(pool.getCurrentSize() == initialSize) { "Pool size should not change on null release" }
            
            // Pool should still be functional
            val buffer = pool.acquire()
            assert(buffer.capacity() == 256) { "Should be able to acquire after null release" }
            
            pool.clear()
            println("PASSED")
            testsPassed++
        } catch (e: Exception) {
            println("FAILED: ${e.message}")
            e.printStackTrace()
            testsFailed++
        }
    }
}