/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/4/2025.
 */

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.dokka.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.sonarqube.gradle.plugin)
    // Needed to make sure Hilt and AGP use an aligned version
    implementation(libs.javapoet)

    // Workaround to make libs available in the project
    // This error can be safely ignored
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
