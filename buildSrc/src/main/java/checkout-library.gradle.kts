/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/10/2025.
 */

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("generate-dependency-list")
    id("publish-release")
    id("detekt-convention")
    id("dokka-convention")
    id("jacoco-convention")
    id("ktlint-convention")
}

private val libs = the<LibrariesForLibs>()

android {
    compileSdkVersion(libs.versions.compile.sdk.get().toInt())

    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "CHECKOUT_VERSION", "\"${rootProject.ext["versionName"]}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}
