plugins {
    id("kmp-library")
}

kotlin {
    // For the managed Node.js Lambda runtimes.
    js {
        nodejs()
    }
}
