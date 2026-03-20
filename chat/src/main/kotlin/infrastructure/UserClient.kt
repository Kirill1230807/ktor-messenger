package com.example.infrastructure

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.kotlin.circuitbreaker.executeSuspendFunction
import io.github.resilience4j.kotlin.retry.executeSuspendFunction
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.time.Duration

class UserClient {
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
        }
    }

    private val circuitBreaker = CircuitBreaker.of("userService", CircuitBreakerConfig.custom()
        .failureRateThreshold(50f)
        .waitDurationInOpenState(Duration.ofSeconds(10))
        .build()
    )

    private val retry = Retry.of("userService", RetryConfig.custom<RetryConfig>()
        .maxAttempts(3)
        .waitDuration(Duration.ofMillis(5000))
        .build())

    suspend fun checkUserExist(userId: Int, correlationId: String?): Boolean {
        return retry.executeSuspendFunction {
            circuitBreaker.executeSuspendFunction {
                try {
                    val response = client.get("http://localhost:8081/api/v1/users/$userId") {
                        correlationId?.let { header("X-Correlation-ID", it) }
                    }
                    response.status == HttpStatusCode.OK
                } catch (e: Exception) {
                    false
                }
            }
        }
    }
}