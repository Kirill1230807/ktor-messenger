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

    implementation("io.ktor:ktor-server-swagger:3.4.0")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")

    // rabbitmq
    implementation("com.rabbitmq:amqp-client:5.20.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("io.ktor:ktor-server-netty:3.4.0")
    implementation("io.ktor:ktor-server-config-yaml:3.4.0")

    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.h2database:h2:2.3.232")

    implementation("ch.qos.logback:logback-classic:1.5.13")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}