# aws-lambda-kotlin
Kotlin Multiplatform library for AWS Lambda. Targets: `jvm`, `js` (Node.js), `linuxX64`, `linuxArm64`, `macosArm64`. The `runtime` module has no `js` target: the Node.js Lambda runtime polls for events itself.

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

## Node.js

Export a function that delegates to your handler and deploy the compiled output on a `nodejs` runtime, with `<module>.handler` as the handler:

```kotlin
// build.gradle.kts
kotlin {
    js {
        nodejs()
        binaries.library()
        useCommonJs()
    }
}
```

```kotlin
@JsExport
fun handler(event: Any?, context: NodeLambdaContext): Promise<Any?> = MyHandler.handleNode(event, context)
```

Blocking handlers return the result directly instead of a `Promise`.
