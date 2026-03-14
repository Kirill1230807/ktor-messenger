val exposed_version: String by project

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = "com.example"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-gson:3.4.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.4.0")
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-server-core:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")

    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("io.ktor:ktor-server-swagger:3.4.0")

    implementation("io.ktor:ktor-client-core:3.4.0")
    implementation("io.ktor:ktor-client-cio:3.4.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.0")

    // Resilience4j
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.2.0")
    implementation("io.github.resilience4j:resilience4j-retry:2.2.0")
    implementation("io.github.resilience4j:resilience4j-kotlin:2.2.0")

    implementation("io.ktor:ktor-server-netty:3.4.0")
    implementation("io.ktor:ktor-server-config-yaml:3.4.0")

    implementation("ch.qos.logback:logback-classic:1.5.13")

    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.h2database:h2:2.3.232")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}