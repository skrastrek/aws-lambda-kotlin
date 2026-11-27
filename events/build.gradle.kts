plugins {
    id("kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
