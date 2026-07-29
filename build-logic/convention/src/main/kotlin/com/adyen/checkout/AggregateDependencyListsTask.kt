/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

package com.adyen.checkout

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class AggregateDependencyListsTask : DefaultTask() {

    @get:InputFiles
    abstract val dependencyLists: ConfigurableFileCollection

    @get:Input
    @get:Optional
    abstract val includeModules: Property<Boolean>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun aggregate() {
        val groupedDependencies = dependencyLists.files.flatMap { file ->
            // The files are named after the module name
            val moduleName = file.nameWithoutExtension
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
