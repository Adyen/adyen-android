/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

plugins {
    `kotlin-dsl`
}

group = "com.adyen.checkout.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.detekt.gradle.plugin)
    compileOnly(libs.dokka.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)

//    // Workaround to make libs available in the project
//    // This error can be safely ignored
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("checkoutAndroidLibrary") {
            id = libs.plugins.checkout.android.library.get().pluginId
            implementationClass = "CheckoutAndroidLibraryPlugin"
        }

        register("checkoutDetekt") {
            id = libs.plugins.checkout.detekt.get().pluginId
            implementationClass = "DetektConventionPlugin"
        }

        register("checkoutDokka") {
            id = libs.plugins.checkout.dokka.get().pluginId
            implementationClass = "DokkaConventionPlugin"
        }

        register("generateDependencyList") {
            id = libs.plugins.dependency.list.generate.get().pluginId
            implementationClass = "GenerateDependencyListPlugin"
        }
    }
}
