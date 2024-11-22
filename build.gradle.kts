import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

allprojects {
    group = "io.skrastrek.aws.lambda.kotlin"
    repositories {
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.ktlint).apply(false)
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    configure<SigningExtension> {
        val signingKey = providers.environmentVariable("GPG_SIGNING_KEY")
        val signingPassphrase = providers.environmentVariable("GPG_SIGNING_PASSPHRASE")

        if (signingKey.isPresent && signingPassphrase.isPresent) {
            useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
            val extension = extensions.getByName("publishing") as PublishingExtension
            sign(extension.publications)
        }
    }

    configure<PublishingExtension> {
        publications.register<MavenPublication>("maven") {
            artifactId = project.name
            from(components["kotlin"])
            pom {
                name = "AWS Lambda Kotlin"
                description = "Utility for Kotlin development with AWS Lambda."
                url = "https://github.com/skrastrek/aws-lambda-kotlin"
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

        repositories {
            maven {
                val releaseRepo = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotRepo = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

                name = "OSSRH"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotRepo else releaseRepo)

                credentials {
                    username = System.getenv("OSSRH_USERNAME")
                    password = System.getenv("OSSRH_PASSWORD")
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
