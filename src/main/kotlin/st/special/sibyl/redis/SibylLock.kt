package st.special.sibyl.redis

import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration
import java.util.UUID

class SibylLock(
    private val redis: StringRedisTemplate,
    private val properties: SibylRedisProperties,
) {

    /**
     * Try-lock: acquire the lock and run [block]. If the lock is already held, throws [LockAlreadyHeldException].
     * On Redis connection failure, throws [RedisConnectionException].
     * The lock is always released after [block] completes (or fails).
     *
     * @param key lock key (will be prefixed with sibyl.redis.lock.prefix)
     * @param ttlSeconds lock TTL — auto-releases if holder crashes
     * @param block the work to do while holding the lock
     */
    fun <T> withLock(
        key: String,
        ttlSeconds: Long = properties.lock.defaultTtlSeconds,
        block: () -> T,
    ): T {
        val lockKey = properties.lock.prefix + key
        val lockValue = UUID.randomUUID().toString()

        val acquired = try {
            redis.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofSeconds(ttlSeconds)) ?: false
        } catch (e: Exception) {
            throw RedisConnectionException("Redis connection failed while acquiring lock: $lockKey", e)
        }

        if (!acquired) {
            throw LockAlreadyHeldException(key)
        }

        try {
            return block()
        } finally {
            releaseSafely(lockKey, lockValue)
        }
    }

    /**
     * Waiting lock (suspend): wait until the lock becomes available, then run [onAcquired].
     * If the lock is released by someone else before we acquire it, [onReleased] is called instead.
     * On timeout or Redis failure, throws the appropriate exception.
     *
     * @param key lock key
     * @param ttlSeconds lock TTL once acquired
     * @param timeoutSeconds max time to wait for the lock
     * @param pollIntervalMillis how often to check if the lock is free
     * @param onAcquired called when this caller acquires the lock
     * @param onReleased called when the lock was released but we didn't need to acquire it
     */
    suspend fun <T> withWaitingLock(
        key: String,
        ttlSeconds: Long = properties.lock.defaultTtlSeconds,
        timeoutSeconds: Long = properties.lock.waitingTimeoutSeconds,
        pollIntervalMillis: Long = properties.lock.waitingPollIntervalMillis,
        onAcquired: suspend () -> T,
        onReleased: suspend () -> T,
    ): T {
        val lockKey = properties.lock.prefix + key
        val lockValue = UUID.randomUUID().toString()

        val deadline = System.currentTimeMillis() + (timeoutSeconds * 1000)

        // First check: is the lock currently held?
        val wasHeld = try {
            redis.hasKey(lockKey) == true
        } catch (e: Exception) {
            throw RedisConnectionException("Redis connection failed while checking lock: $lockKey", e)
        }

        // If it was already held, wait for it to be released
        if (wasHeld) {
            while (System.currentTimeMillis() < deadline) {
                val stillHeld = try {
                    redis.hasKey(lockKey) == true
                } catch (e: Exception) {
                    throw RedisConnectionException("Redis connection failed while waiting for lock: $lockKey", e)
                }

                if (!stillHeld) {
                    // Lock was released by the previous holder — call onReleased
                    return onReleased()
                }

                kotlinx.coroutines.delay(pollIntervalMillis)
            }

            throw LockTimeoutException(key, timeoutSeconds)
        }

        // Lock was not held — try to acquire it
        val acquired = try {
            redis.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofSeconds(ttlSeconds)) ?: false
        } catch (e: Exception) {
            throw RedisConnectionException("Redis connection failed while acquiring lock: $lockKey", e)
        }

        if (!acquired) {
            // Race condition: someone else grabbed it between our check and acquire
            // Fall into waiting mode
            while (System.currentTimeMillis() < deadline) {
                val stillHeld = try {
                    redis.hasKey(lockKey) == true
                } catch (e: Exception) {
                    throw RedisConnectionException("Redis connection failed while waiting for lock: $lockKey", e)
                }

                if (!stillHeld) {
                    return onReleased()
                }

                kotlinx.coroutines.delay(pollIntervalMillis)
            }

            throw LockTimeoutException(key, timeoutSeconds)
        }

        try {
            return onAcquired()
        } finally {
            releaseSafely(lockKey, lockValue)
        }
    }

    /**
     * Release lock only if we still own it (compare-and-delete via Lua script).
     */
    private fun releaseSafely(lockKey: String, lockValue: String) {
        try {
            val script = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
            """.trimIndent()
            redis.execute(
                org.springframework.data.redis.core.script.DefaultRedisScript(script, Long::class.java),
                listOf(lockKey),
                lockValue,
            )
        } catch (_: Exception) {
            // Best-effort release — lock will expire via TTL
        }
    }
}
