rootProject.name = "ktor-messenger"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("users")
include("chat")
include("notifications")