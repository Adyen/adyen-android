/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2026.
 */

import com.adyen.checkout.libs
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.BasePlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

/**
 * Applies the JVM target every module is built against, so that it is declared in a single place.
 *
 * The two kinds of module are pinned differently. Android modules only get a bytecode target, and are compiled by
 * whichever JDK runs Gradle, which is what AGP expects. Plain Kotlin JVM modules get a toolchain, because nothing
 * else would pin them at all.
 */
class JavaConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val jvmTarget = JavaVersion.toVersion(libs.versions.jvm.target.get())

            // The Kotlin support built into AGP derives its own JVM target from these values, so there is nothing to
            // configure on the Kotlin side.
            plugins.withType<BasePlugin> {
                extensions.configure<CommonExtension> {
                    compileOptions.sourceCompatibility = jvmTarget
                    compileOptions.targetCompatibility = jvmTarget
                }
            }

            plugins.withType<KotlinPluginWrapper> {
                extensions.configure<KotlinJvmProjectExtension> {
                    jvmToolchain(jvmTarget.majorVersion.toInt())
                }
            }
        }
    }
}
