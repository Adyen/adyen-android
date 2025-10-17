/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/10/2025.
 */

plugins {
    id("org.sonarqube")
}

sonar {
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
