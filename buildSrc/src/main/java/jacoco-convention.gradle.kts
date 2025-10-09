/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/10/2025.
 */

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("jacoco")
}

private val libs = the<LibrariesForLibs>()

configure<JacocoPluginExtension> {
    toolVersion = libs.versions.jacoco.get()
}

tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = excludes.orEmpty() + coverageExclusions
    }
}

plugins.withType<BasePlugin>().configureEach {
    val android = project.extensions.getByType<BaseExtension>()

    android.buildTypes.configureEach {
        if (name == "debug") {
            enableUnitTestCoverage = true
        }
    }

    android.buildTypes.forEach { buildType ->
        val buildTypeName = buildType.name.replaceFirstChar { it.uppercase() }
        val testTaskName = "test${buildTypeName}UnitTest"

        tasks.register<JacocoReport>("jacoco${buildTypeName}TestReport") {
            group = "Reporting"
            description = "Generate JaCoCo report for ${buildType.name} tests"
            dependsOn(testTaskName)

            reports {
                xml.required.set(true)
                html.required.set(false)
                csv.required.set(false)
            }

            val mainSrc = android.sourceSets.getByName("main").java.srcDirs
            val debugSrc = android.sourceSets.getByName("debug").java.srcDirs
            sourceDirectories.setFrom(files(mainSrc, debugSrc))

            val directories = fileTree(layout.buildDirectory.dir("intermediates/javac/${buildType.name}")) {
                exclude(coverageExclusions)
            } + fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/${buildType.name}")) {
                exclude(coverageExclusions)
            }
            classDirectories.setFrom(directories)

            executionData.setFrom(
                fileTree(layout.buildDirectory) {
                    include(
                        "outputs/unit_test_code_coverage/${buildType.name}UnitTest/$testTaskName.exec",
                        "jacoco/$testTaskName.exec",
                        "outputs/code-coverage/connected/*coverage.ec",
                    )
                },
            )
        }
    }
}
