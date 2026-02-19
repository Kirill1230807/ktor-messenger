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

    implementation("io.swagger:swagger-generator:3.0.0-rc1")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}