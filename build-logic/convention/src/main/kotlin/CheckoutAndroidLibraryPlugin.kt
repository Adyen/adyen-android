/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import com.adyen.checkout.libs
import com.android.build.api.dsl.LibraryExtension
import kotlinx.validation.ApiValidationExtension
import kotlinx.validation.KotlinApiBuildTask
import kotlinx.validation.KotlinApiCompareTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.plugin.extraProperties

class CheckoutAndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")
            apply(plugin = "checkout.dependency.list.generate")
            apply(plugin = "checkout.detekt")
            apply(plugin = "checkout.dokka")
            apply(plugin = "checkout.jacoco")
            apply(plugin = "checkout.ktlint")
            apply(plugin = "checkout.publish")
            apply(plugin = "checkout.sonar")

            extensions.configure<LibraryExtension> {
                compileSdk = libs.versions.compile.sdk.get().toInt()

                defaultConfig {
                    minSdk = libs.versions.min.sdk.get().toInt()

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    consumerProguardFiles("consumer-rules.pro")

                    buildConfigField("String", "CHECKOUT_VERSION", "\"${rootProject.extraProperties["versionName"]}\"")
                }

                buildFeatures {
                    buildConfig = true
                }

                publishing {
                    singleVariant("release") {
                        withSourcesJar()
                        withJavadocJar()
                    }
                }
            }

            configureBcvWorkaround()
        }
    }

    /**
     * Workaround for BCV + AGP 9 incompatibility.
     * See: https://github.com/Kotlin/binary-compatibility-validator/issues/312
     *
     * AGP 9 blocks the kotlin-android plugin, which BCV relies on to register apiDump/apiCheck tasks
     * for Android library modules. We manually register the BCV tasks by hooking into the compilation
     * output directly. Once BCV is updated to support AGP 9, this workaround can be removed.
     */
    private fun Project.configureBcvWorkaround() {
        val apiValidation = rootProject.extensions.findByType(ApiValidationExtension::class.java) ?: return
        if (name in apiValidation.ignoredProjects) return

        val kotlinVersion = libs.versions.kotlin.get()

        // BCV's prepareJvmValidationClasspath() adds ASM but skips kotlin-metadata-jvm because
        // the kotlin-android withPlugin callback never fires under AGP 9.
        dependencies.add("bcv-rt-jvm-cp", "org.jetbrains.kotlin:kotlin-metadata-jvm:$kotlinVersion")

        afterEvaluate {
            val dumpFileName = "$name.api"
            val apiDirName = "api"

            val apiBuild = tasks.register<KotlinApiBuildTask>("apiBuild") {
                description = "Builds Kotlin API for 'release' compilation of $name"
                inputClassesDirs.from(tasks.named("compileReleaseKotlin").map { it.outputs.files })
                inputClassesDirs.from(tasks.named("compileReleaseJavaWithJavac").map { it.outputs.files })
                outputApiFile.set(layout.buildDirectory.file("$apiDirName/$dumpFileName"))
                runtimeClasspath.from(configurations.named("bcv-rt-jvm-cp-resolver"))

                // Strip Compose-generated ComposableSingletons classes from the API dump.
                // These are compiler-generated, not real public API, and their names are unstable
                // (they change between KGP and AGP 9 built-in Kotlin compilation).
                doLast {
                    val apiFile = outputApiFile.get().asFile
                    if (!apiFile.exists()) return@doLast
                    val lines = apiFile.readLines()
                    val result = StringBuilder()
                    var skip = false
                    for (line in lines) {
                        if (!skip && line.contains("class ") && line.contains("ComposableSingletons$")) {
                            skip = true
                            continue
                        }
                        if (skip) {
                            if (line == "}") {
                                skip = false
                            }
                            continue
                        }
                        result.append(line).append('\n')
                    }
                    apiFile.writeText(result.toString().replace(Regex("\n{3,}"), "\n\n"))
                }
            }

            val apiCheck = tasks.register<KotlinApiCompareTask>("apiCheck") {
                group = "verification"
                description =
                    "Checks signatures of public API against the golden value in API folder for $name"
                projectApiFile.set(file("$apiDirName/$dumpFileName"))
                generatedApiFile.set(apiBuild.flatMap { it.outputApiFile })
            }

            tasks.register("apiDump") {
                group = "other"
                description = "Syncs the API file for $name"
                dependsOn(apiBuild)
                val fromProvider = apiBuild.flatMap { it.outputApiFile }
                val toFile = file("$apiDirName/$dumpFileName")
                inputs.file(fromProvider)
                outputs.file(toFile)
                doLast {
                    val source = fromProvider.get().asFile
                    if (source.exists()) {
                        toFile.parentFile.mkdirs()
                        source.copyTo(toFile, overwrite = true)
                    } else {
                        toFile.delete()
                    }
                }
            }

            tasks.named("check") { dependsOn(apiCheck) }
        }
    }
}
