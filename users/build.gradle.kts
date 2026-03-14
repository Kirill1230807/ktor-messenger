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
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("io.ktor:ktor-server-core:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.4.0")

    implementation("io.ktor:ktor-server-swagger:3.4.0")

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