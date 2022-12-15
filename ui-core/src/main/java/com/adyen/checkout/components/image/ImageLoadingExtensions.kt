/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/12/2022.
 */

package com.adyen.checkout.components.image

import android.content.Context
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RestrictTo
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.components.api.LogoApi
import com.adyen.checkout.components.ui.R
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.image.DefaultImageLoader
import com.adyen.checkout.core.image.ImageLoader
import kotlinx.coroutines.launch

// Re-use the same instance to ensure the cache is working optimally
private var localImageLoader: ImageLoader? = null
private val Context.imageLoader: ImageLoader
    get() {
        localImageLoader?.let { return it }
        val newImageLoader = DefaultImageLoader(this)
        localImageLoader = newImageLoader
        return newImageLoader
    }

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun ImageView.load(
    url: String,
    imageLoader: ImageLoader = context.imageLoader,
    @DrawableRes placeholder: Int = R.drawable.ic_placeholder_image,
    @DrawableRes errorFallback: Int = R.drawable.ic_placeholder_image,
) {
    setImageResource(placeholder)
    findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
        imageLoader.load(
            url,
            onSuccess = { setImageBitmap(it) },
            onError = { setImageResource(errorFallback) }
        )
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun ImageView.load(
    environment: Environment,
    path: String,
    imageLoader: ImageLoader = context.imageLoader,
    @DrawableRes placeholder: Int = R.drawable.ic_placeholder_image,
    @DrawableRes errorFallback: Int = R.drawable.ic_placeholder_image,
) {
    load(environment.baseUrl + path, imageLoader, placeholder, errorFallback)
}

@Suppress("LongParameterList")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun ImageView.loadLogo(
    environment: Environment,
    txVariant: String,
    txSubVariant: String = "",
    size: LogoApi.Size = LogoApi.Size.SMALL,
    imageLoader: ImageLoader = context.imageLoader,
    @DrawableRes placeholder: Int = R.drawable.ic_placeholder_image,
    @DrawableRes errorFallback: Int = R.drawable.ic_placeholder_image,
) {
    val densityExtension = this.resources.displayMetrics.densityDpi.getDensityExtension()
    val logoPath = buildLogoPath(size, txVariant, txSubVariant, densityExtension)
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

private fun buildLogoPath(
    size: LogoApi.Size,
    txVariant: String,
    txSubVariant: String,
    densityExtension: String,
): String {
    val txString = if (txSubVariant.isEmpty()) txVariant else "$txVariant/$txSubVariant"
    return "images/logos/$size/$txString$densityExtension.png"
}
