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
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(because = "Not worth caching, only resolves dependency artifacts")
internal abstract class ResolveDependenciesTask @Inject constructor() : DefaultTask() {

    @get:InputFiles
    @get:Optional
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val dependencies: ConfigurableFileCollection

    @TaskAction
    fun resolve() {
        // Declaring the artifacts as input files already forces Gradle to resolve them before this
        // action runs, without accessing the project at execution time.
        logger.lifecycle("Resolved ${dependencies.files.size} dependency artifacts")
    }
}
