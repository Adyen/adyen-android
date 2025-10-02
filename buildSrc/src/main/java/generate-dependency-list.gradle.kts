/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/10/2025.
 */

tasks.register("generateDependencyList") {
    notCompatibleWithConfigurationCache("Uses project during execution")
    doNotTrackState("This task must always run to ensure the latest dependencies are resolved")

    val outputDir = file("${project.layout.buildDirectory.asFile.get()}/outputs/dependency_list/")
    doFirst {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
    }

    doLast {
        val outputFileName = "dependency_list.txt"
        val file = file("$outputDir/$outputFileName")
        val fileWriter = file.writer()

        configurations["releaseRuntimeClasspath"]
            .incoming
            .resolutionResult
            .allDependencies
            .map { (it as ResolvedDependencyResult).selected.id.displayName }
            .distinct()
            .sorted()
            .forEach { fileWriter.appendLine(it) }

        fileWriter.flush()
        fileWriter.close()
    }
}
