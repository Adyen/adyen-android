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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.adyen.checkout.core.common.internal.helper.LocalEnvironment
import com.adyen.checkout.core.common.internal.imageLoader
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.image.ImageLoader
import com.adyen.checkout.ui.internal.image.LogoSize
import com.adyen.checkout.ui.internal.image.NetworkImage
import com.adyen.checkout.ui.internal.theme.Dimensions

/**
 * Composable that loads a logo using the Adyen environment and txVariant.
 *
 * @param txVariant The txVariant to be handled.
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
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader = LocalContext.current.imageLoader,
    txSubVariant: String = "",
    size: LogoSize = LogoSize.SMALL,
    contentDescription: String? = null,
    @DrawableRes placeholder: Int = R.drawable.ic_placeholder_image,
    @DrawableRes errorFallback: Int = R.drawable.ic_placeholder_image,
) {
    val resources = LocalResources.current
    val environment = LocalEnvironment.current
    val fullUrl = remember(resources, txVariant, environment, txSubVariant, size) {
        val densityExtension = resources.displayMetrics.densityDpi.getDensityExtension()
        val logoPath = buildLogoPath(size, txVariant, txSubVariant, densityExtension)
        environment.checkoutShopperBaseUrl.toString() + logoPath
    }

    @Suppress("MagicNumber")
    NetworkImage(
        url = fullUrl,
        modifier = modifier
            .dropShadow(
                shape = RoundedCornerShape(Dimensions.CornerRadius),
                shadow = Shadow(
                    radius = 1.dp,
                    offset = DpOffset(x = 0.dp, 2.dp),
                    // TODO - Colors: Move this color to the Internal colors file, but not expose to the public layer
                    color = Color(0xFF121212),
                    alpha = 0.04f,
                ),
            )
            .clip(RoundedCornerShape(Dimensions.CornerRadius)),
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
