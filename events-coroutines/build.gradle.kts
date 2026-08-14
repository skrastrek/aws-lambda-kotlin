dependencies {
    api(project(":coroutines"))
    api(project(":events"))
    implementation(libs.kotlinx.serialization.json)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}
