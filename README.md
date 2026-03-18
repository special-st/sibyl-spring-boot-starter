# sibyl-spring-boot-starter

Spring Boot starter for Sibyl System monitoring. Adds Micrometer + Prometheus metrics with SRE best practices.

## What it does

- Exposes `/actuator/prometheus` endpoint (Prometheus format)
- Adds common tags (`service`, `env`) to all metrics
- Registers JVM, GC, thread, processor, uptime metrics
- Provides `SibylMetrics` helper for custom business metrics
- Auto-discovered by vmagent via `prometheus.io/scrape` annotation

## Usage

### 1. Add dependency

```kotlin
// build.gradle.kts
repositories {
    maven { url = uri("https://maven.pkg.github.com/special-st/sibyl-spring-boot-starter") }
}

dependencies {
    implementation("st.special:sibyl-spring-boot-starter:0.1.0")
}
```

### 2. Configure

```yaml
# application.yml
sibyl:
  monitoring:
    service-name: my-service
    environment: prod

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

### 3. Add Kubernetes annotation for auto-discovery

```yaml
# k8s Service
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
    prometheus.io/path: "/actuator/prometheus"
```

### 4. Custom metrics

```kotlin
@Service
class OrderService(private val sibylMetrics: SibylMetrics) {

    fun createOrder(order: Order) = sibylMetrics.time("order.create") {
        // your logic
        sibylMetrics.count("order.created", "type" to order.type)
    }
}
```

## Grafana

The Sibyl System cluster includes a pre-configured "Spring Boot Apps" dashboard that visualizes:
- JVM heap usage, GC pauses
- HTTP request rate, latency P50/P95/P99
- Active threads, uptime
- Custom `sibyl.*` metrics
