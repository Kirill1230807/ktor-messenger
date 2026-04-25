import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.event.*
import kotlin.time.Duration.Companion.seconds

val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        meterBinders = listOf(
            io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics(),
            io.micrometer.core.instrument.binder.jvm.JvmGcMetrics(),
            io.micrometer.core.instrument.binder.system.ProcessorMetrics()
        )
    }

    install(RateLimit) {
        register {
            rateLimiter(limit = 10, refillPeriod = 1.seconds)
            requestKey { call -> call.request.origin.remoteHost }
        }
    }

    routing {
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
        rateLimit {
            get("/health") {
                call.respond(mapOf("status" to "UP"))
            }
        }
    }
}
