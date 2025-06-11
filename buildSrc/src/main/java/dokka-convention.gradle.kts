import java.time.Year

/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/4/2025.
 */

plugins {
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

dokka {
    moduleName = project.name
    dokkaPublications.configureEach {
        suppressInheritedMembers = true
        failOnWarning = true
    }
    dokkaSourceSets.named("main") {
        sourceLink {
            localDirectory = file("src/main/java")
            remoteUrl("https://github.com/Adyen/adyen-android/tree/main/${project.name}/src/main/java")
            remoteLineSuffix = "#L"
        }
    }
    pluginsConfiguration.html {
        footerMessage.set("Copyright (c) ${Year.now()} Adyen N.V.")
    }
}

tasks.register<Jar>("dokkaJavadocJar") {
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}
