/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import com.android.build.api.dsl.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.plugin.extraProperties

class CheckoutAndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val libs = the<LibrariesForLibs>()

            apply(plugin = "com.android.library")
            apply(plugin = "org.jetbrains.kotlin.android")
            apply(plugin = "checkout.dependency.list.generate")
            apply(plugin = "checkout.detekt")
            apply(plugin = "checkout.dokka")

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
        }
    }
}
