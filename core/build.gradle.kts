plugins {
    id("kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.json.io)
        }
        jvmMain.dependencies {
            api(libs.aws.lambda.java.core)
        }
    }
}
