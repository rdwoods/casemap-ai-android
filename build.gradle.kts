plugins {
    id("com.android.application") version "9.4.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("com.google.devtools.ksp") version "2.3.6" apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.services) apply false
}

buildscript {
    configurations.classpath {
        resolutionStrategy.force("com.squareup:javapoet:1.13.0")
    }
}