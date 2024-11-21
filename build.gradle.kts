import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

allprojects {
    group = "io.skrastrek.aws.lambda.kotlin"
    repositories {
        mavenCentral()
        google()
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.ktlint).apply(false)
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    tasks.withType<Test> {
        useJUnitPlatform()
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

    extensions.configure<KtlintExtension> {
        version = "1.4.1"
        outputToConsole = true
        verbose = true
    }

    configure<KotlinJvmProjectExtension> {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
            vendor.set(JvmVendorSpec.AMAZON)
        }
    }
}
