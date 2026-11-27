plugins {
    id("kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":coroutines"))
            api(project(":events"))
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
