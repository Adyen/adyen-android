/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import com.adyen.checkout.ResolveDependenciesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.util.concurrent.Callable

class ResolveDependenciesPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val resolveTask = registerResolveDependenciesTask()

            if (target == rootProject) {
                subprojects {
                    val subprojectResolveTask = registerResolveDependenciesTask()
                    resolveTask.configure { dependsOn(subprojectResolveTask) }
                }
            }
        }
    }

    private fun Project.registerResolveDependenciesTask(): TaskProvider<ResolveDependenciesTask> {
        return tasks.register<ResolveDependenciesTask>("resolveDependencies") {
            doNotTrackState("This task must always run to ensure the latest dependencies are resolved")

            dependencies.from(Callable { configurations.resolvableArtifactFiles() })
            dependencies.from(Callable { buildscript.configurations.resolvableArtifactFiles() })
        }
    }

    private fun ConfigurationContainer.resolvableArtifactFiles(): List<FileCollection> {
        return this
            .filter { it.isCanBeResolved }
            .map { it.incoming.artifactView { lenient(true) }.files }
    }
}
