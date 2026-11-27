import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.vanniktech.maven.publish")
}

group = "io.skrastrek"

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(25)
        vendor = JvmVendorSpec.AMAZON
    }

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
            freeCompilerArgs.add("-Xjdk-release=21")
        }
    }

    // AWS Lambda runs on x86_64 and arm64 Linux. macosArm64 lets the native tests run on a Mac.
    linuxX64()
    linuxArm64()
    macosArm64()

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    // concurrentMain: code shared by the multithreaded targets (JVM and native), where
    // Dispatchers.IO and runBlocking exist. JS, added by kmp-library-js, sits outside it.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("concurrent") {
                withJvm()
                group("native")
            }
        }
    }

    // platform.posix links libcrypt.so.1, which the provided.al2023 Lambda image lacks. Nothing
    // here calls into it, so --as-needed drops the dependency and test binaries run on AL2023.
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.all { linkerOpts("-Wl,--as-needed") }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// Also sets org.gradle.jvm.version=21 in published Gradle module metadata
tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<AbstractTestTask>().configureEach {
    testLogging {
        events = setOf(FAILED, PASSED, SKIPPED)
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    filePermissions {
        unix("644")
    }
    dirPermissions {
        unix("755")
    }
}

ktlint {
    version = "1.8.0"
    outputToConsole = true
    verbose = true
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("io.skrastrek", "aws-lambda-kotlin-${project.name}", project.version.toString())

    pom {
        name = "aws-lambda-kotlin"
        description = "Utility for Kotlin development with AWS Lambda."
        url = "https://github.com/skrastrek/aws-lambda-kotlin"
        inceptionYear = "2024"

        scm {
            url = "https://github.com/skrastrek/aws-lambda-kotlin"
            connection = "scm:git://github.com:skrastrek/aws-lambda-kotlin.git"
            developerConnection = "scm:git://github.com:skrastrek/aws-lambda-kotlin.git"
        }

        licenses {
            license {
                name = "Apache-2.0"
                url = "https://opensource.org/licenses/Apache-2.0"
            }
        }

        developers {
            developer {
                id = "sebramsland"
                name = "Sebastian Ramsland"
                email = "sebastian@skrastrek.io"
                organizationUrl = "https://skrastrek.io"
            }
        }
    }
}
