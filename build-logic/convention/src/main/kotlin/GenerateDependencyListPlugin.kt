/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import com.adyen.checkout.GenerateDependencyListTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.kotlin.dsl.register

class GenerateDependencyListPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            tasks.register<GenerateDependencyListTask>("generateDependencyList") {
                description =
                    "Generates a list of all resolved dependencies for the releaseRuntimeClasspath configuration."
                group = "Reporting"

                val outputDir = project.layout.buildDirectory.dir("outputs/dependency_list").get()
                outputFile.set(outputDir.file("${project.name}.txt"))

                resolvedDependencies.set(
                    configurations.named("releaseRuntimeClasspath").map { config ->
                        config.incoming.resolutionResult.allDependencies
                            .filterIsInstance<ResolvedDependencyResult>()
                            .map { it.selected.id.displayName }
                    },
                )
            }
        }
    }
}
