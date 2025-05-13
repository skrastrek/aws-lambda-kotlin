plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core"))
    api(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
}
