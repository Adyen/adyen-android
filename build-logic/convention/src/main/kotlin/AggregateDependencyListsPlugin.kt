/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import com.adyen.checkout.AggregateDependencyListsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class AggregateDependencyListsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            // Example call from command line: ./gradlew aggregateDependencyLists -PoutputFileName=deps.txt -PincludeModules=true
            tasks.register<AggregateDependencyListsTask>("aggregateDependencyLists") {
                val filteredSubProjects = subprojects.filter { it.plugins.hasPlugin("generate-dependency-list") }

                filteredSubProjects.forEach {
                    dependsOn(it.tasks.named("generateDependencyList"))
                }

                dependencyLists.from(
                    filteredSubProjects.map {
                        it.layout.buildDirectory.file("outputs/dependency_list/${it.name}.txt")
                    },
                )

                val outputFileName = project.providers
                    .gradleProperty("outputFileName")
                    .getOrElse("dependency_list.txt")
                outputFile.set(project.layout.buildDirectory.file("outputs/dependency_list/$outputFileName"))

                includeModules.set(project.providers.gradleProperty("includeModules").map { it.toBoolean() })
            }
        }
    }
}
