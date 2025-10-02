/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/10/2025.
 */

// sample call from command line: ./gradlew aggregateDependencyLists --no-configuration-cache -PoutputFileName=deps.txt -PincludeModules=true
tasks.register("aggregateDependencyLists") {
    val filteredSubProjects = subprojects.filter { it.plugins.hasPlugin("generate-dependency-list") }

    filteredSubProjects.forEach { dependsOn("${it.name}:generateDependencyList") }

    val outputDir = file("${project.layout.buildDirectory.asFile.get()}/outputs/dependency_list/")
    doFirst {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
    }

    doLast {
        val groupedDependencies = filteredSubProjects
            .flatMap { subproject ->
                subproject.layout.buildDirectory.file("outputs/dependency_list/dependency_list.txt")
                    .get()
                    .asFile
                    .useLines { lines ->
                        lines.map {
                            DependencyUsage(it, subproject.name)
                        }.toList()
                    }
            }

        val outputFileName = if (project.hasProperty("outputFileName")) {
            project.property("outputFileName") as String
        } else {
            "dependency_list.txt"
        }

        val fileWriter = file("$outputDir/$outputFileName").writer()

        val includeModules = project.hasProperty("includeModules") && project.property("includeModules") == "true"
        if (!includeModules) {
            groupedDependencies
                .map { it.dependency }
                .distinct()
                .sorted()
                .forEach { fileWriter.appendLine(it) }
        } else {
            groupedDependencies
                .groupBy { it.dependency }
                .toSortedMap { one, two -> one!!.compareTo(two!!) }
                .forEach { (dep, usages) ->
                    val modules = usages.map { it.module }.distinct()
                    fileWriter.appendLine("$dep - used by: [${modules.joinToString()}]")
                }
        }

        fileWriter.flush()
        fileWriter.close()
    }
}

private data class DependencyUsage(val dependency: String, val module: String)
