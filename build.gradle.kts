import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

allprojects {
    group = "io.skrastrek"
    repositories {
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.ktlint).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.vanniktech.maven.publish")

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            optIn = listOf("kotlin.time.ExperimentalTime")
        }
    }

    configure<MavenPublishBaseExtension> {
        publishToMavenCentral()
        signAllPublications()

        coordinates("io.skrastrek", "aws-lambda-kotlin-${project.name}", project.version.toString())

        pom {
            name = "aws-lambda-kotlin"
            description = "Utility for Kotlin development with AWS Lambda."
            url = "https://github.com/skrastrek/aws-lambda-kotlin"
            inceptionYear = "2024"
            packaging = "jar"

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
        version = "1.5.0"
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
