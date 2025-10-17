/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/10/2025.
 */

abstract class GenerateDependencyListTask @Inject constructor() : DefaultTask() {

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val dependencyDisplayNames: ListProperty<String>

    @TaskAction
    fun generateList() {
        val file = outputFile.asFile.get()
        file.parentFile.mkdirs()

        file.writer().use { fileWriter ->
            dependencyDisplayNames.get()
                .distinct()
                .sorted()
                .forEach { fileWriter.appendLine(it) }
        }
    }
}

tasks.register<GenerateDependencyListTask>("generateDependencyList") {
    description = "Generates a list of all resolved dependencies for the releaseRuntimeClasspath configuration."
    group = "Reporting"

    val outputDir = project.layout.buildDirectory.dir("outputs/dependency_list").get()
    outputFile.set(outputDir.file("${project.name}.txt"))

    val deps = project.configurations.named("releaseRuntimeClasspath").map { config ->
        config.incoming
            .resolutionResult
            .allDependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .map { it.selected.id.displayName }
    }

    dependencyDisplayNames.set(deps)
}
