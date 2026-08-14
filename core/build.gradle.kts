plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.aws.lambda.java.core)
    // Build-time only: read by native-image when building a GraalVM image, absent at runtime.
    compileOnly(libs.graalvm.nativeimage)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
}
