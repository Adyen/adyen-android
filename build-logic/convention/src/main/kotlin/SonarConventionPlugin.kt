/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import com.adyen.checkout.coverageExclusions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.sonarqube.gradle.SonarExtension

class SonarConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.sonarqube")

            extensions.configure<SonarExtension> {
                properties {
                    property(
                        "sonar.androidLint.reportPaths",
                        "${layout.buildDirectory.get().asFile}/reports/lint-results-debug.xml",
                    )
                    property(
                        "sonar.kotlin.detekt.reportPaths",
                        "${layout.buildDirectory.get().asFile}/reports/detekt/detekt-results.xml",
                    )
                    property(
                        "sonar.coverage.jacoco.xmlReportPaths",
                        "${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoDebugTestReport/jacocoDebugTestReport.xml",
                    )

                    val exclusions = coverageExclusions.joinToString()
                    property("sonar.coverage.exclusions", exclusions)
                }
            }
        }
    }
}
