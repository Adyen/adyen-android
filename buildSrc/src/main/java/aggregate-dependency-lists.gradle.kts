/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/10/2025.
 */

abstract class AggregateDependencyListsTask : DefaultTask() {

    @get:InputFiles
    abstract val dependencyLists: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val includeModules: Property<Boolean>

    @TaskAction
    fun aggregate() {
        val groupedDependencies = dependencyLists.files.flatMap { file ->
            // The module name is derived from the input file's path.
            val moduleName = file.parentFile.parentFile.parentFile.parentFile.name
            file.useLines { lines ->
                lines.map {
                    DependencyUsage(it, moduleName)
                }.toList()
            }
        }

        outputFile.get().asFile.writer().use { writer ->
            if (includeModules.getOrElse(false)) {
                groupedDependencies
                    .groupBy { it.dependency }
                    .toSortedMap()
                    .forEach { (dep, usages) ->
                        val modules = usages.map { it.module }.distinct().sorted()
                        writer.appendLine("$dep - used by: [${modules.joinToString()}]")
                    }
            } else {
                groupedDependencies
                    .map { it.dependency }
                    .distinct()
                    .sorted()
                    .forEach { dependency -> writer.appendLine(dependency) }
            }
        }
    }
}

private data class DependencyUsage(val dependency: String, val module: String)

// Example call from command line: ./gradlew aggregateDependencyLists -PoutputFileName=deps.txt -PincludeModules=true
val aggregateDependencyLists = tasks.register<AggregateDependencyListsTask>("aggregateDependencyLists") {
    val filteredSubProjects = subprojects.filter { it.plugins.hasPlugin("generate-dependency-list") }

    filteredSubProjects.forEach {
        dependsOn(it.tasks.named("generateDependencyList"))
    }

    dependencyLists.from(
        filteredSubProjects.map {
            it.layout.buildDirectory.file("outputs/dependency_list/dependency_list.txt")
        },
    )

    val outputFileName = project.providers.gradleProperty("outputFileName").getOrElse("dependency_list.txt")
    outputFile.set(project.layout.buildDirectory.file("outputs/dependency_list/$outputFileName"))

    includeModules.set(project.providers.gradleProperty("includeModules").map { it.toBoolean() })
}

// Deprecated task
tasks.register("dependencyList") {
    dependsOn(aggregateDependencyLists)
    doFirst {
        logger.warn("This task is deprecated. Use 'aggregateDependencyLists' instead.")
    }
}
