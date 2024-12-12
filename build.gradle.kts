// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.diffplug.spotless") version "6.22.0"
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    spotless {
        kotlin {
            target("**/*.kt")  // Include all Kotlin files

            // Use ktlint for Kotlin formatting
            ktlint("1.5.0") // Replace with the desired ktlint version
        }

        format("misc") {
            target("**/*.md", "**/*.yaml", "**/*.yml")
            trimTrailingWhitespace()
            endWithNewline()
        }

        format("xml") {
            target("**/*.xml")
            indentWithSpaces()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    afterEvaluate {
        tasks.named("preBuild") {
            dependsOn("spotlessApply")
        }
    }
}