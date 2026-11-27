# aws-lambda-kotlin
Kotlin Multiplatform library for AWS Lambda. Targets: `jvm`, `linuxX64`, `linuxArm64`, `macosArm64`.

## Kotlin/Native

Build a Linux executable and start the custom runtime from `main`:

```kotlin
// build.gradle.kts
kotlin {
    linuxArm64 {
        binaries.executable {
            entryPoint = "com.example.main"
            baseName = "bootstrap"
            // platform.posix links libcrypt.so.1, which the provided.al2023 runtime lacks.
            linkerOpts("-Wl,--as-needed")
        }
    }
}
```

```kotlin
fun main() = LambdaNativeRuntime.BufferedResponse(MyHandler()).start()
```

Deploy the resulting `bootstrap.kexe` as `bootstrap` on the `provided.al2023` runtime.
