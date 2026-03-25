package st.special.sibyl.redis

open class SibylLockException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class LockAlreadyHeldException(key: String) : SibylLockException("Lock already held: $key")

class LockAcquisitionFailedException(key: String, cause: Throwable? = null) :
    SibylLockException("Failed to acquire lock: $key", cause)

class LockTimeoutException(key: String, timeoutSeconds: Long) :
    SibylLockException("Timed out waiting for lock: $key (timeout: ${timeoutSeconds}s)")

class RedisConnectionException(message: String, cause: Throwable) :
    SibylLockException(message, cause)
