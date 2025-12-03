/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import com.adyen.checkout.coverageExclusions
import com.adyen.checkout.libs
import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.SourceDirectories
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

class JacocoConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "jacoco")

            configure<JacocoPluginExtension> {
                toolVersion = libs.versions.jacoco.get()
            }

            extensions.getByType<LibraryExtension>().buildTypes.configureEach {
                enableAndroidTestCoverage = false
                enableUnitTestCoverage = true
            }

            extensions.getByType<LibraryAndroidComponentsExtension>().onVariants { variant ->
                val objFactory = project.objects
                val allJars: ListProperty<RegularFile> = objFactory.listProperty(RegularFile::class.java)
                val allDirectories: ListProperty<Directory> = objFactory.listProperty(Directory::class.java)

                val variantName = variant.name.replaceFirstChar { it.uppercase() }
                val testTaskName = "test${variantName}UnitTest"

                val jacocoTask = tasks.register<JacocoReport>("jacoco${variantName}TestReport") {
                    group = "Reporting"
                    description = "Generate JaCoCo report for ${variant.name} tests"
                    dependsOn(testTaskName)

                    reports {
                        xml.required.set(true)
                        html.required.set(false)
                        csv.required.set(false)
                    }

                    fun SourceDirectories.Flat?.toFilePaths(): Provider<List<String>> = this
                        ?.all
                        ?.map { directories -> directories.map { it.asFile.path } }
                        ?: provider { emptyList() }

                    val javaSources = variant.sources.java.toFilePaths()

                    @Suppress("UnstableApiUsage")
                    val kotlinSources = variant.sources.kotlin.toFilePaths()
                    sourceDirectories.setFrom(files(javaSources, kotlinSources))

                    classDirectories.setFrom(
                        allJars,
                        allDirectories.map { dirs ->
                            dirs.map { dir ->
                                objFactory.fileTree().setDir(dir).exclude(coverageExclusions)
                            }
                        },
                    )

                    executionData.setFrom(
                        project.fileTree(layout.buildDirectory.map { "${it.asFile}/outputs/unit_test_code_coverage/${variant.name}UnitTest" })
                            .matching { include("**/*.exec") },
                        project.fileTree(layout.buildDirectory.map { "${it.asFile}/outputs/code_coverage/${variant.name}AndroidTest" })
                            .matching { include("**/*.ec") },
                    )
                }

                variant.artifacts.forScope(ScopedArtifacts.Scope.PROJECT)
                    .use(jacocoTask)
                    .toGet(
                        ScopedArtifact.CLASSES,
                        { _ -> allJars },
                        { _ -> allDirectories },
                    )
            }

            tasks.withType<Test>().configureEach {
                configure<JacocoTaskExtension> {
                    isIncludeNoLocationClasses = true
                    excludes = excludes.orEmpty() + coverageExclusions
                }
            }
        }
    }
}
