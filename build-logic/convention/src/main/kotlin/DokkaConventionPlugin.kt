/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import java.time.Year

class DokkaConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.dokka")
            apply(plugin = "org.jetbrains.dokka-javadoc")

            val projectName = name
            val mainSourceDir = file("src/main/java")

            extensions.configure<DokkaExtension> {
                moduleName = projectName

                dokkaPublications.configureEach {
                    suppressInheritedMembers = true
                    failOnWarning = true
                }

                dokkaSourceSets.configureEach {
                    sourceLink {
                        localDirectory = mainSourceDir
                        remoteUrl("https://github.com/Adyen/adyen-android/tree/main/$projectName/src/main/java")
                        remoteLineSuffix = "#L"
                    }
                }

                (pluginsConfiguration as ExtensionAware).extensions.configure<DokkaHtmlPluginParameters>("html") {
                    footerMessage = "Copyright (c) ${Year.now()} Adyen N.V."
                }
            }
        }
    }
}
