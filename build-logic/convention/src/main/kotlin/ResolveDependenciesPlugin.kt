/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.logging.Logger

class ResolveDependenciesPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            tasks.register("resolveDependencies") {
                notCompatibleWithConfigurationCache("Uses project during execution")
                doNotTrackState("This task must always run to ensure the latest dependencies are resolved")

                doLast {
                    allprojects {
                        logger.lifecycle("Resolving dependencies for ${project.name}")
                        project.buildscript.configurations.resolveDependencies(logger)
                        project.configurations.resolveDependencies(logger)
                    }
                }
            }
        }
    }

    private fun ConfigurationContainer.resolveDependencies(logger: Logger) {
        this
            .filter { it.isCanBeResolved }
            .forEach { config ->
                config.incoming.artifactView { lenient(true) }.artifacts.failures.forEach {
                    logger.info(it.message)
                }
            }
    }
}
