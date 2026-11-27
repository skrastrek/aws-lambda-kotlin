pluginManagement {
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "aws-lambda-kotlin"

include(
    "core",
    "coroutines",
    "events",
    "events-coroutines",
    "runtime",
)
