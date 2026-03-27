package will.sudoku.web

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * LRU Cache for solved puzzles to improve performance.
 * Uses normalized puzzle string as key to avoid duplicate solves.
 */
class PuzzleCache(private val maxSize: Int = 1000) {
    
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val accessOrder = mutableListOf<String>()
    private val lock = Any()
    
    // Statistics
    private val hits = AtomicLong(0)
    private val misses = AtomicLong(0)
    private val evictions = AtomicLong(0)
    
    data class CacheEntry(
        val solution: String?,
        val solved: Boolean,
        val solveTimeMs: Double,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Normalize puzzle string for consistent cache keys.
     * Removes whitespace and ensures consistent formatting.
     */
    fun normalizePuzzle(puzzle: String): String {
        return puzzle.trim()
            .replace("\\s+".toRegex(), "")
            .replace("\n", "")
            .replace("\r", "")
    }
    
    /**
     * Get cached solution if available.
     */
    fun get(puzzle: String): CacheEntry? {
        val key = normalizePuzzle(puzzle)
        val entry = cache[key]
        
        if (entry != null) {
            hits.incrementAndGet()
            // Update access order
            synchronized(lock) {
                accessOrder.remove(key)
                accessOrder.add(key)
            }
            return entry
        }
        
        misses.incrementAndGet()
        return null
    }
    
    /**
     * Store solution in cache.
     */
    fun put(puzzle: String, solution: String?, solved: Boolean, solveTimeMs: Double) {
        val key = normalizePuzzle(puzzle)
        
        synchronized(lock) {
            // Remove if already exists (to update access order)
            if (cache.containsKey(key)) {
                accessOrder.remove(key)
            }
            
            // Evict oldest if at capacity
            while (cache.size >= maxSize && accessOrder.isNotEmpty()) {
                val oldest = accessOrder.removeAt(0)
                cache.remove(oldest)
                evictions.incrementAndGet()
            }
            
            // Add new entry
            cache[key] = CacheEntry(solution, solved, solveTimeMs)
            accessOrder.add(key)
        }
    }
    
    /**
     * Get cache statistics.
     */
    fun getStats(): CacheStats {
        val totalRequests = hits.get() + misses.get()
        val hitRate = if (totalRequests > 0) {
            hits.get().toDouble() / totalRequests
        } else {
            0.0
        }
        
        return CacheStats(
            size = cache.size,
            maxSize = maxSize,
            hits = hits.get(),
            misses = misses.get(),
            hitRate = hitRate,
            evictions = evictions.get()
        )
    }
    
    /**
     * Clear the cache.
     */
    fun clear() {
        synchronized(lock) {
            cache.clear()
            accessOrder.clear()
            hits.set(0)
            misses.set(0)
            evictions.set(0)
        }
    }
}

data class CacheStats(
    val size: Int,
    val maxSize: Int,
    val hits: Long,
    val misses: Long,
    val hitRate: Double,
    val evictions: Long
)
