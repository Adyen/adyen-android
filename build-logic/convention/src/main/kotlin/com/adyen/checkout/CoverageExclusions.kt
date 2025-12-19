/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/12/2025.
 */

package com.adyen.checkout

val coverageExclusions = listOf(
    // Android
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/R$*.class",
    "**/R.class",

    // Data binding
    "**/*BindingAdapter.*",
    "**/*BindingAdapters.*",
    "**/*ViewBinding.*",
    "**/BR$*.class",
    "**/BR.class",
    "**/DataBinderMapperImpl*.*",
    "**/DataBinderMapperImpl.*",
    "**/databinding/**",

    // Activities, Fragments, etc. (not tested with unit tests)
    "**/*Activity$*.*",
    "**/*Activity.*",
    "**/*Adapter$*.*",
    "**/*Adapter.*",
    "**/*Behavior.*",
    "**/*Dialog.*",
    "**/*Drawable.*",
    "**/*Fragment$*.*",
    "**/*Fragment.*",
    "**/*View$*.*",
    "**/*View.*",
    "**/*ViewHolder$*.*",
    "**/*ViewHolder.*",

    // Parcelize
    "**/*\$Creator",
    "**/*\$Creator*.*",

    // Activity result contract
    "**/*ActivityResults.*",
    "**/*ResultContract.*",

    // Dagger + Hilt
    "**/*_ComponentTreeDeps.*",
    "**/*_Factory.*",
    "**/*_GeneratedInjector.*",
    "**/*_HiltComponents.*",
    "**/*_HiltComponents_*.*",
    "**/*_HiltModules*.*",
    "**/*_HiltModules.*",
    "**/*_HiltModules_*.*",
    "**/*_Member*Injector.*",
    "**/*_Provide*Factory*.*",
    "**/*_Provide*Factory.*",
    "**/*_ProvideFactory.*",
    "**/Dagger*.*",
    "**/Hilt_*.*",
    "**/dagger/**",
    "**/hilt_aggregated_deps/**",

    // Test classes
    "**/*Test.*",
    "**/Test*.*",

    // Fix issue with JaCoCo on JDK
    "jdk.internal.*",

    // Custom views not following *View naming
    "**/AddressFormInput*.*",
    "**/AdyenSwipeToRevealLayout*.*",
    "**/AdyenTextInputEditText*.*",
    "**/CardNumberInput.*",
    "**/DefaultPayButton.*",
    "**/ExpiryDateInput.*",
    "**/GiftCardNumberInput.*",
    "**/IbanInput.*",
    "**/PayButton.*",
    "**/SecurityCodeInput.*",
    "**/SocialSecurityNumberInput.*",

    // Project specific files that can"t be unit tested
    "**/*ComponentProvider$*.*",
    "**/*ComponentProvider.*",
    "**/*DropInService$*.*",
    "**/*DropInService.*",
    "**/*Factory.*",
    "**/*ViewProvider.*",
    "**/AdyenLogKt*",
    "**/BuildUtils.*",
    "**/CheckCompileOnlyKt*",
    "**/ComposeExtensions*.*",
    "**/ContextExtensions*.*",
    "**/DropIn$*.*",
    "**/DropIn.*",
    "**/DropInExt*.*",
    "**/FileDownloader*.*",
    "**/FragmentExtensions*.*",
    "**/FragmentProvider$*.*",
    "**/FragmentProvider.*",
    "**/ImageLoadingExtensions*.*",
    "**/ImageLoadingExtensions.*",
    "**/ImageSaver.*",
    "**/InstallmentFilter.*",
    "**/LazyArguments*.*",
    "**/LifecycleExtensions*.*",
    "**/LogcatLogger.*",
    "**/PdfOpener.*",
    "**/ResultExtKt*",
    "**/RunCompileOnlyKt*",
    "**/ViewExtensions*.*",
    "**/ViewModelExt*.*",

    // Example app and test-core are not applicable
    "com/adyen/checkout/example/**",
    "com/adyen/checkout/test/**",
)

