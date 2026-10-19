/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import com.adyen.checkout.ResolveDependenciesTask
import com.android.build.gradle.tasks.JavaDocGenerationTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import java.util.Properties
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

    private fun Project.registerResolveDependenciesTask(): TaskProvider<ResolveDependenciesTask> =
        // Prevent duplicate registration
        if (TASK_NAME in tasks.names) {
            tasks.named<ResolveDependenciesTask>(TASK_NAME)
        } else {
            val isRootProject = this == rootProject
            tasks.register<ResolveDependenciesTask>(TASK_NAME) {
                group = "help"
                description = "Resolves all dependencies to generate verification metadata."
                doNotTrackState("This task must always run to ensure the latest dependencies are resolved")

                // Resolve project dependencies
                dependencies.from(Callable { configurations.resolvableArtifactFiles() })

                // Resolve buildscript dependencies
                dependencies.from(Callable { buildscript.configurations.resolvableArtifactFiles() })

                // Resolve Dokka's dynamic dependencies
                dependencies.from(
                    Callable {
                        tasks.withType(JavaDocGenerationTask::class.java).map {
                            files(it.dokkaPlugins, it.dokkaRuntimeClasspath, it.dokkaCoreClasspath)
                        }
                    },
                )

                if (isRootProject) {
                    // Resolve AAPT2 artifacts for all platforms
                    dependencies.from(Callable { aapt2ArtifactFilesForAllPlatforms() })
                }
            }
        }

    /**
     * AGP resolves the AAPT2 artifact of the platform it is running on from a detached configuration, so it is not
     * covered by [resolvableArtifactFiles] and only the artifact of the current platform ends up in the verification
     * metadata. Resolve the artifacts of all platforms instead, so that the verification metadata can be generated on
     * any machine.
     */
    private fun Project.aapt2ArtifactFilesForAllPlatforms(): FileCollection {
        val version = aapt2Version() ?: error("Could not determine AAPT2 version")

        val aapt2Dependencies = AAPT2_PLATFORMS
            .map { dependencies.create("$AAPT2_GROUP:$AAPT2_NAME:$version:$it") }
            .toTypedArray()

        return configurations.detachedConfiguration(*aapt2Dependencies)
            .incoming
            .artifactView { lenient(true) }
            .files
    }

    /**
     * The AAPT2 version consists of the AGP version and the AAPT2 build number, which AGP both bundle as resources.
     */
    private fun aapt2Version(): String? {
        val agpVersion = agpResourceProperty(AGP_VERSION_RESOURCE, AGP_VERSION_PROPERTY) ?: return null
        val aapt2BuildNumber = agpResourceProperty(AAPT2_VERSION_RESOURCE, AAPT2_VERSION_PROPERTY) ?: return null

        return "$agpVersion-$aapt2BuildNumber"
    }

    private fun agpResourceProperty(resource: String, property: String): String? {
        return JavaDocGenerationTask::class.java
            .getResourceAsStream(resource)
            ?.use { inputStream -> Properties().apply { load(inputStream) } }
            ?.getProperty(property)
    }

    private fun ConfigurationContainer.resolvableArtifactFiles(): List<FileCollection> {
        return this
            .filter { it.isCanBeResolved }
            .map { it.incoming.artifactView { lenient(true) }.files }
    }

    companion object {
        private const val TASK_NAME = "resolveDependencies"
        private const val AAPT2_GROUP = "com.android.tools.build"
        private const val AAPT2_NAME = "aapt2"
        private const val AAPT2_VERSION_RESOURCE =
            "/com/android/build/gradle/internal/res/aapt2_version.properties"
        private const val AAPT2_VERSION_PROPERTY = "aapt2Version"
        private const val AGP_VERSION_RESOURCE = "/com/android/build/api/extension/impl/version.properties"
        private const val AGP_VERSION_PROPERTY = "buildVersion"
        private val AAPT2_PLATFORMS = listOf("linux", "osx", "windows")
    }
}
