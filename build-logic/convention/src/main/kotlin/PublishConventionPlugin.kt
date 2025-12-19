/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.util.Properties
import javax.inject.Inject

class PublishConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "maven-publish")
            apply(plugin = "signing")

            val extension = extensions.create<PublishConventionPluginExtension>("checkoutPublishing")

            initializeProperties()
            configurePublishing(extension)
            configureSigning()
        }
    }

    private fun Project.initializeProperties() {
        extraProperties.set("signing.keyId", "")
        extraProperties.set("signing.password", "")
        extraProperties.set("signing.secretKeyRingFile", "")
        extraProperties.set("sonatypeCentralPortalUsername", "")
        extraProperties.set("sonatypeCentralPortalPassword", "")
        extraProperties.set("sonatypeStagingProfileId", "")

        val secretPropsFile: File = project.rootProject.file("local.properties")
        if (secretPropsFile.exists()) {
            val p = Properties()
            p.load(FileInputStream(secretPropsFile))
            p.forEach { (name, value) ->
                extraProperties.set(name.toString(), value)
            }
        } else {
            extraProperties.set("signing.keyId", System.getenv("SIGNING_KEY_ID"))
            extraProperties.set("signing.password", System.getenv("SIGNING_PASSWORD"))
            extraProperties.set("signing.secretKeyRingFile", System.getenv("SIGNING_SECRET_KEY_RING_FILE"))
            extraProperties.set("sonatypeCentralPortalUsername", System.getenv("SONATYPE_CENTRAL_PORTAL_USERNAME"))
            extraProperties.set("sonatypeCentralPortalPassword", System.getenv("SONATYPE_CENTRAL_PORTAL_PASSWORD"))
            extraProperties.set("sonatypeStagingProfileId", System.getenv("SONATYPE_STAGING_PROFILE_ID"))
        }
    }

    private fun Project.configurePublishing(extension: PublishConventionPluginExtension) {
        val versionName: String by project.rootProject.extraProperties

        val checkoutGroupId = "com.adyen.checkout"
        val companyName = "Adyen N.V."
        val companyUrl = "https://www.adyen.com/"
        val githubUrl = "https://github.com/Adyen/adyen-android"
        val scmUrl = "scm:git:git://github.com/Adyen/adyen-android.git"

        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("release") {
                    afterEvaluate {
                        from(components.getByName("release"))

                        groupId = checkoutGroupId
                        artifactId = extension.id.get()
                        version = versionName

                        pom {
                            name = extension.name
                            description = extension.description
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
                    url = if (versionName.endsWith("SNAPSHOT")) {
                        URI.create("https://central.sonatype.com/repository/maven-snapshots/")
                    } else {
                        URI.create("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                    }
                    credentials {
                        username = extraProperties["sonatypeCentralPortalUsername"] as? String
                        password = extraProperties["sonatypeCentralPortalPassword"] as? String
                    }
                }
            }
        }
    }

    private fun Project.configureSigning() {
        extensions.configure<SigningExtension> {
            val publishing = extensions.getByType<PublishingExtension>()
            sign(publishing.publications)
        }
    }
}

abstract class PublishConventionPluginExtension @Inject constructor(
    objFactory: ObjectFactory
) {
    val id: Property<String> = objFactory.property()
    val name: Property<String> = objFactory.property()
    val description: Property<String> = objFactory.property()
}
