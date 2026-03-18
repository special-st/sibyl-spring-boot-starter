package st.special.sibyl.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.util.concurrent.ConcurrentHashMap

/**
 * Helper class for common SRE metrics patterns.
 *
 * Usage in your service:
 * ```
 * @Autowired lateinit var sibylMetrics: SibylMetrics
 *
 * fun processOrder(order: Order) {
 *     sibylMetrics.time("order.process") {
 *         // your logic
 *     }
 *     sibylMetrics.count("order.created", "type" to order.type)
 * }
 * ```
 */
class SibylMetrics(private val registry: MeterRegistry) {

    private val timers = ConcurrentHashMap<String, Timer>()
    private val counters = ConcurrentHashMap<String, Counter>()

    /** Time a block of code */
    fun <T> time(name: String, vararg tags: Pair<String, String>, block: () -> T): T {
        val timer = timers.getOrPut("$name-${tags.toList()}") {
            Timer.builder("sibyl.$name")
                .apply { tags.forEach { (k, v) -> tag(k, v) } }
                .register(registry)
        }
        return timer.recordCallable(block)!!
    }

    /** Increment a counter */
    fun count(name: String, vararg tags: Pair<String, String>) {
        val counter = counters.getOrPut("$name-${tags.toList()}") {
            Counter.builder("sibyl.$name")
                .apply { tags.forEach { (k, v) -> tag(k, v) } }
                .register(registry)
        }
        counter.increment()
    }

    /** Record a gauge value */
    fun gauge(name: String, value: Number, vararg tags: Pair<String, String>) {
        val tagArray = tags.flatMap { (k, v) -> listOf(k, v) }.toTypedArray()
        registry.gauge("sibyl.$name", io.micrometer.core.instrument.Tags.of(*tagArray), value)
    }
}
