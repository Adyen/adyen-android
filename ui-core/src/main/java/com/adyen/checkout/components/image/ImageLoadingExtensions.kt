/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/12/2022.
 */

package com.adyen.checkout.components.image

import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.adyen.checkout.components.api.LogoApi
import com.adyen.checkout.components.ui.R
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.image.DefaultImageLoader
import com.adyen.checkout.core.image.ImageLoader

fun ImageView.load(
    url: String,
    imageLoader: ImageLoader = DefaultImageLoader,
    @DrawableRes placeholder: Int = R.drawable.ic_placeholder_image,
    @DrawableRes errorFallback: Int = R.drawable.ic_placeholder_image,
) {
    setImageResource(placeholder)
    imageLoader.load(
        url,
        onSuccess = { byteArray ->
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            setImageBitmap(bitmap)
        },
        onError = {
            setImageResource(errorFallback)
        }
    )
}

fun ImageView.load(
    environment: Environment,
    path: String,
    imageLoader: ImageLoader = DefaultImageLoader,
    @DrawableRes placeholder: Int = R.drawable.ic_placeholder_image,
    @DrawableRes errorFallback: Int = R.drawable.ic_placeholder_image,
) {
    load(environment.baseUrl + path, imageLoader, placeholder, errorFallback)
}

fun ImageView.loadLogo(
    environment: Environment,
    txVariant: String,
    txSubVariant: String = "",
    size: LogoApi.Size = LogoApi.Size.SMALL,
    imageLoader: ImageLoader = DefaultImageLoader,
    @DrawableRes placeholder: Int = R.drawable.ic_placeholder_image,
    @DrawableRes errorFallback: Int = R.drawable.ic_placeholder_image,
) {
    val densityExtension = this.resources.displayMetrics.densityDpi.getDensityExtension()
    val logoPath = densityExtension.buildLogoPath(txVariant, txSubVariant, size)
    load(environment, logoPath, imageLoader, placeholder, errorFallback)
}

private fun Int.getDensityExtension(): String {
    return when {
        this <= DisplayMetrics.DENSITY_LOW -> "-ldpi"
        this <= DisplayMetrics.DENSITY_MEDIUM -> "" // no extension
        this <= DisplayMetrics.DENSITY_HIGH -> "-hdpi"
        this <= DisplayMetrics.DENSITY_XHIGH -> "-xhdpi"
        this <= DisplayMetrics.DENSITY_XXHIGH -> "-xxhdpi"
        else -> "-xxxhdpi"
    }
}

private fun String.buildLogoPath(
    txVariant: String,
    txSubVariant: String,
    size: LogoApi.Size
): String {
    val logoPath = "images/logos/%1\$s/%2\$s.png"
    val txString = if (txSubVariant.isEmpty()) txVariant else "$txVariant/$txSubVariant"
    return String.format(logoPath, size.toString(), txString + this)
}
