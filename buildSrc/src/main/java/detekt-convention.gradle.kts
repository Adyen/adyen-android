/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/10/2025.
 */

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("io.gitlab.arturbosch.detekt")
}

private val libs = the<LibrariesForLibs>()

configure<DetektExtension> {
    toolVersion = libs.versions.detekt.get()

    config.setFrom("${rootProject.rootDir}/config/detekt/detekt.yml")
    baseline = file("${rootProject.rootDir}/config/detekt/detekt-baseline.xml")
    parallel = true
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml {
            required.set(true)
            outputLocation.set(file("${layout.buildDirectory.get().asFile}/reports/detekt/detekt-results.xml"))
        }
        html.required.set(false)
    }

    jvmTarget = JavaVersion.VERSION_11.toString()
}

dependencies {
    detektPlugins(libs.detekt)
}
