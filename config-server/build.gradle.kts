plugins {
    kotlin("jvm")
}

group = "com.example"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-server-core:3.4.0")
    implementation("io.ktor:ktor-server-netty:3.4.0")
    implementation("ch.qos.logback:logback-classic:1.5.13")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}