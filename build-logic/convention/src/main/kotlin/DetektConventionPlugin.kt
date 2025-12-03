/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class DetektConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val libs = the<LibrariesForLibs>()

            apply(plugin = "io.gitlab.arturbosch.detekt")

            extensions.configure<DetektExtension> {
                toolVersion = libs.versions.detekt.get()

                config.setFrom("${rootProject.rootDir}/config/detekt/detekt.yml")
                baseline = file("${rootProject.rootDir}/config/detekt/detekt-baseline.xml")
                parallel = true
                buildUponDefaultConfig = true
            }

            tasks.withType<Detekt>().configureEach {
                reports {
                    xml {
                        required = true
                        outputLocation = file("${layout.buildDirectory.get().asFile}/reports/detekt/detekt-results.xml")
                    }
                    html.required = false
                }

                jvmTarget = JavaVersion.VERSION_11.toString()
            }

            dependencies {
                "detektPlugins"(libs.detekt)
            }
        }
    }
}
