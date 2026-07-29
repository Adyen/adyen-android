/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI
import java.time.Year

class DokkaConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.dokka")

            val projectName = name
            val mainSourceDir = file("src/main/java")

            tasks.withType<DokkaTask>().configureEach {
                moduleName = projectName
                suppressInheritedMembers = true
                failOnWarning = true

                dokkaSourceSets.configureEach {
                    sourceLink {
                        localDirectory = mainSourceDir
                        remoteUrl = URI(
                            "https://github.com/Adyen/adyen-android/tree/main/$projectName/src/main/java",
                        ).toURL()
                        remoteLineSuffix = "#L"
                    }
                }

                pluginsMapConfiguration.put(
                    "org.jetbrains.dokka.base.DokkaBase",
                    """{ "footerMessage": "Copyright (c) ${Year.now()} Adyen N.V." }""",
                )
            }
        }
    }
}
