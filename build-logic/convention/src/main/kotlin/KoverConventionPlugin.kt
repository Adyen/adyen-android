/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/7/2026.
 */

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class KoverConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlinx.kover")

            extensions.configure<KoverProjectExtension> {
                reports {
                    filters {
                        excludes {
                            classes(
                                // Android
                                "*.BuildConfig",

                                // Data binding
                                "*.databinding.*",

                                // Activities, fragments, views, etc.
                                "*.*Activity",
                                "*.*Activity$*",
                                "*.*Fragment",
                                "*.*Fragment$*",
                                "*.*View",
                                "*.*View$*",
                                "*.*ViewHolder",
                                "*.*ViewHolder$*",

                                // Compose
                                "*.ComposableSingletons",
                                "*.ComposableSingletons$*",

                                // Parcelize
                                "*.*\$Creator",

                                // Custom views not following *View naming
                                "*.AddressFormInput",
                                "*.AdyenSwipeToRevealLayout",
                                "*.AdyenTextInputEditText",
                                "*.CardNumberInput",
                                "*.DefaultPayButton",
                                "*.ExpiryDateInput",
                                "*.GiftCardNumberInput",
                                "*.IbanInput",
                                "*.PayButton",
                                "*.SecurityCodeInput",
                                "*.SocialSecurityNumberInput",

                                // Project specific files that can't be tested
                                "*.*ComponentProvider",
                                "*.*DropInService",
                                "*.*ViewProvider",
                                "*.AdyenLogKt",
                                "*.CheckCompileOnlyKt",
                                "*.ContextExtensionsKt",
                                "*.DropIn",
                                "*.DropInKt",
                                "*.FragmentExtensionsKt",
                                "*.FragmentProviderKt",
                                "*.ImageLoadingExtensionsKt",
                                "*.ImageSaver",
                                "*.InstallmentFilter",
                                "*.LazyArgumentsKt",
                                "*.LifecycleExtensionsKt",
                                "*.LogcatLogger",
                                "*.PdfOpener",
                                "*.ResultExtKt",
                                "*.RunCompileOnlyKt",
                                "*.ViewExtensionsKt",
                                "*.ViewModelExtKt",

                                // Example-app and test-core
                                "com.adyen.checkout.example.*",
                                "com.adyen.checkout.test.*",
                            )

                            annotatedBy(
                                "androidx.compose.runtime.Composable",
                            )
                        }
                    }
                }
            }
        }
    }
}
