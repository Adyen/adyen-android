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
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.process.CommandLineArgumentProvider

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
                ktlint(libs.ktlint.compose.rules)
            }

            val ktLintTask = tasks.register<JavaExec>("ktlint") {
                description = "Check Kotlin code style"
                group = LifecycleBasePlugin.VERIFICATION_GROUP

                val inputFiles = kotlinFiles("**/main/**/*.kt")
                inputs.files(inputFiles)

                val outputFile = project.layout.buildDirectory.file("reports/ktlint/ktlint-report.txt")
                outputs.file(outputFile)

                classpath = ktlint
                mainClass.set("com.pinterest.ktlint.Main")
                argumentProviders.add(
                    CommandLineArgumentProvider {
                        listOf(
                            "--reporter=plain",
                            "--reporter=plain,output=${outputFile.get().asFile.absolutePath}",
                        )
                    },
                )
                pipeFilesToStdin(inputFiles)
            }

            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
                dependsOn(ktLintTask)
            }

            tasks.register<JavaExec>("ktlintFormat") {
                description = "Fix Kotlin code style deviations."
                group = "formatting"

                classpath = ktlint
                mainClass.set("com.pinterest.ktlint.Main")
                args("-F")
                pipeFilesToStdin(kotlinFiles("**/*.kt"))
            }
        }
    }

    private fun Project.kotlinFiles(sourcePattern: String): FileCollection =
        fileTree("src") { include(sourcePattern, "**/*.kts") } + fileTree(projectDir) { include("*.kts") }

    /**
     * Gives ktlint an explicit list of [files] on stdin instead of glob patterns. When every entry is an existing file,
     * ktlint skips its own directory walk, which covered the whole module including build/ and crashed on files that
     * other tasks create and delete during the build. If a listed file disappears before ktlint starts, ktlint falls
     * back to walking; running from src/ keeps that walk inside src/ unless the module has .kts files in its root.
     * Modules without src/ run from the module directory instead, because a missing working directory fails the task.
     */
    private fun JavaExec.pipeFilesToStdin(files: FileCollection) {
        val srcDir = project.file("src")
        workingDir = if (srcDir.exists()) srcDir else project.projectDir
        // The trailing "=" is intentional: an empty delimiter selects NUL, which cannot occur in a file name.
        argumentProviders.add(CommandLineArgumentProvider { listOf("--patterns-from-stdin=") })
        doFirst {
            standardInput = files.joinToString(separator = "\u0000") { it.absolutePath }.byteInputStream()
        }
    }
}
