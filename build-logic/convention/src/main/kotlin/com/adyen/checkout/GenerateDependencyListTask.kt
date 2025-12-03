/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

package com.adyen.checkout

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.result.ResolutionResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal abstract class GenerateDependencyListTask @Inject constructor() : DefaultTask() {

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val resolutionResult: Property<ResolutionResult>

    @TaskAction
    fun generateList() {
        val file = outputFile.asFile.get()
        file.parentFile.mkdirs()

        file.writer().use { fileWriter ->
            resolutionResult.get()
                .allDependencies
                .filterIsInstance<ResolvedDependencyResult>()
                .map { it.selected.id.displayName }
                .distinct()
                .sorted()
                .forEach { fileWriter.appendLine(it) }
        }
    }
}
