/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import com.adyen.checkout.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin

class KtlintConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val ktlint by configurations.creating

            dependencies {
                ktlint(libs.ktlint.cli) {
                    attributes {
                        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
                    }
                }
            }

            val ktLintTask = tasks.register<JavaExec>("ktlint") {
                description = "Check Kotlin code style"
                group = LifecycleBasePlugin.VERIFICATION_GROUP

                val inputFiles = project.fileTree("src") {
                    include("**/*.kt", "**/*.kts")
                    exclude("**/build/**", "**/.gradle/**")
                }
                inputs.files(inputFiles)

                val outputDir = project.layout.buildDirectory.dir("reports/ktlint").get()
                val outputFile = outputDir.file("ktlint-report.txt")
                outputs.file(outputFile)

                classpath = ktlint
                mainClass.set("com.pinterest.ktlint.Main")
                args(
                    "--reporter=plain",
                    "--reporter=plain,output=${outputFile}",
                    "**/src/**/main/**/*.kt",
                    "**.kts",
                    "!**/build/**",
                )
            }

            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
                dependsOn(ktLintTask)
            }

            tasks.register<JavaExec>("ktlintFormat") {
                description = "Fix Kotlin code style deviations."
                group = "formatting"

                classpath = ktlint
                mainClass.set("com.pinterest.ktlint.Main")
                args(
                    "-F",
                    "**/src/**/*.kt",
                    "**.kts",
                    "!**/build/**",
                )
            }
        }
    }
}
