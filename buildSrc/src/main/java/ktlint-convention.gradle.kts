/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/10/2025.
 */

import org.gradle.accessors.dm.LibrariesForLibs

private val ktlint by configurations.creating

private val libs = the<LibrariesForLibs>()

dependencies {
    ktlint(libs.ktlint.cli) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

val ktLintTask = tasks.register<JavaExec>("ktlint") {
    description = "Check Kotlin code style"
    group = LifecycleBasePlugin.VERIFICATION_GROUP

    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args(
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
    dependsOn(ktLintTask)
}

tasks.register<JavaExec>("ktlintFormat") {
    description = "Fix Kotlin code style deviations."
    group = "formatting"

    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args(
        "-F",
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}
