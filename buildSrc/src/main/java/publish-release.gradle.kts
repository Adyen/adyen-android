/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/10/2025.
 */

import java.io.FileInputStream
import java.net.URI
import java.util.Properties

plugins {
    id("maven-publish")
    id("signing")
}

private val extension = project.extensions.create<PublishReleasePluginExtension>("checkoutPublishing")

project.extra.set("signing.keyId", "")
project.extra.set("signing.password", "")
project.extra.set("signing.secretKeyRingFile", "")
project.extra.set("sonatypeCentralPortalUsername", "")
project.extra.set("sonatypeCentralPortalPassword", "")
project.extra.set("sonatypeStagingProfileId", "")

private val secretPropsFile: File = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    val p = Properties()
    p.load(FileInputStream(secretPropsFile))
    p.forEach { (name, value) ->
        project.extra.set(name.toString(), value)
    }
} else {
    project.extra.set("signing.keyId", System.getenv("SIGNING_KEY_ID"))
    project.extra.set("signing.password", System.getenv("SIGNING_PASSWORD"))
    project.extra.set("signing.secretKeyRingFile", System.getenv("SIGNING_SECRET_KEY_RING_FILE"))
    project.extra.set("sonatypeCentralPortalUsername", System.getenv("SONATYPE_CENTRAL_PORTAL_USERNAME"))
    project.extra.set("sonatypeCentralPortalPassword", System.getenv("SONATYPE_CENTRAL_PORTAL_PASSWORD"))
    project.extra.set("sonatypeStagingProfileId", System.getenv("SONATYPE_STAGING_PROFILE_ID"))
}

private val checkoutGroupId = "com.adyen.checkout"
private val versionName: String by rootProject.extra

private val companyName = "Adyen N.V."
private val companyUrl = "https://www.adyen.com/"

private val githubUrl = "https://github.com/Adyen/adyen-android"
private val scmUrl = "scm:git:git://github.com/Adyen/adyen-android.git"

group = checkoutGroupId

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])

                val mavenArtifactId: String by extension.id
                val mavenArtifactName: String by extension.name
                val mavenArtifactDescription: String by extension.description

                groupId = checkoutGroupId
                artifactId = mavenArtifactId
                version = versionName

                artifact(tasks.named("dokkaJavadocJar"))

                pom {
                    name = mavenArtifactName
                    description = mavenArtifactDescription
                    url = githubUrl

                    licenses {
                        license {
                            name = "MIT License"
                            url = "https://opensource.org/licenses/MIT"
                        }
                    }

                    organization {
                        name = companyName
                        url = companyUrl
                    }

                    developers {
                        developer {
                            name = "Checkout"
                            organization = companyName
                            organizationUrl = companyUrl
                        }
                    }

                    scm {
                        connection = scmUrl
                        developerConnection = scmUrl
                        url = githubUrl
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            url = URI.create("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = project.extra["sonatypeCentralPortalUsername"] as? String
                password = project.extra["sonatypeCentralPortalPassword"] as? String
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

interface PublishReleasePluginExtension {
    val id: Property<String>
    val name: Property<String>
    val description: Property<String>
}
