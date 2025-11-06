/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/9/2025.
 */

package com.adyen.checkout.core.common.internal.ui

import androidx.annotation.DrawableRes
import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.imageLoader
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.ImageLoader
import com.adyen.checkout.ui.internal.LogoSize
import com.adyen.checkout.ui.internal.NetworkImage

/**
 * Composable that loads a logo using the Adyen environment and txVariant.
 *
 * @param txVariant The txVariant to be handled.
 * @param environment The [Environment] to be used for internal network calls.
 * @param modifier The modifier to be applied to the Image.
 * @param imageLoader The ImageLoader instance to use. Defaults to the one provided by the Context.
 * @param size The [LogoSize] required to download the correct sized image.
 * @param contentDescription The content description for accessibility.
 * @param placeholder A drawable resource to show while the image is loading.
 * @param errorFallback A drawable resource to show if the image fails to load.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Suppress("LongParameterList")
@Composable
fun CheckoutNetworkLogo(
    txVariant: String,
    environment: Environment,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = LocalContext.current.imageLoader,
    txSubVariant: String = "",
    size: LogoSize = LogoSize.SMALL,
    contentDescription: String? = null,
    @DrawableRes placeholder: Int = R.drawable.ic_placeholder_image,
    @DrawableRes errorFallback: Int = R.drawable.ic_placeholder_image,
) {
    val resources = LocalResources.current
    val fullUrl = remember(resources, txVariant, environment, txSubVariant, size) {
        val densityExtension = resources.displayMetrics.densityDpi.getDensityExtension()
        val logoPath = buildLogoPath(size, txVariant, txSubVariant, densityExtension)
        environment.checkoutShopperBaseUrl.toString() + logoPath
    }

    NetworkImage(
        url = fullUrl,
        modifier = modifier,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        placeholder = placeholder,
        errorFallback = errorFallback,
    )
}

private fun Int.getDensityExtension(): String {
    return when {
        this <= android.util.DisplayMetrics.DENSITY_LOW -> "-ldpi"
        this <= android.util.DisplayMetrics.DENSITY_MEDIUM -> "" // no extension
        this <= android.util.DisplayMetrics.DENSITY_HIGH -> "-hdpi"
        this <= android.util.DisplayMetrics.DENSITY_XHIGH -> "-xhdpi"
        this <= android.util.DisplayMetrics.DENSITY_XXHIGH -> "-xxhdpi"
        else -> "-xxxhdpi"
    }
}

private fun buildLogoPath(
    size: LogoSize,
    txVariant: String,
    txSubVariant: String,
    densityExtension: String,
): String {
    val txString = if (txSubVariant.isEmpty()) txVariant else "$txVariant/$txSubVariant"
    return "images/logos/$size/$txString$densityExtension.png"
}
