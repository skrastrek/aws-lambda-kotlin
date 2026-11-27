plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(plugin(libs.plugins.kotlin.multiplatform))
    implementation(plugin(libs.plugins.kotlin.serialization))
    implementation(plugin(libs.plugins.ktlint))
    implementation(plugin(libs.plugins.maven.publish))
}

// Turns a version catalog plugin alias into its Gradle plugin marker artifact.
fun plugin(plugin: Provider<PluginDependency>) = plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
