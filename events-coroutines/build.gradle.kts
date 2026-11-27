plugins {
    id("kmp-library-js")
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
