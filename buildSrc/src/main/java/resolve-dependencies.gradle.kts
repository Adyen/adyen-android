/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/4/2025.
 */

tasks.register("resolveDependencies") {
    notCompatibleWithConfigurationCache("Uses project during execution")
    doNotTrackState("This task must always run to ensure the latest dependencies are resolved")

    doLast {
        allprojects {
            logger.lifecycle("Resolving dependencies for ${project.name}")
            project.buildscript.configurations.resolveDependencies()
            project.configurations.resolveDependencies()
        }
    }
}

private fun ConfigurationContainer.resolveDependencies() {
    this
        .filter { it.isCanBeResolved }
        .forEach { config ->
            config.incoming.artifactView { lenient(true) }.artifacts.failures.forEach {
                logger.info(it.message)
            }
        }
}
