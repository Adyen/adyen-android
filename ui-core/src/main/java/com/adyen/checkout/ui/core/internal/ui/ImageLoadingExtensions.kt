/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 14/12/2022.
 */

package com.adyen.checkout.ui.core.internal.ui

import android.content.Context
import android.content.ContextWrapper
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.core.old.internal.ui.DefaultImageLoader
import com.adyen.checkout.core.old.internal.ui.ImageLoader
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.R
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
    fun Context?.getLifecycleOwner(): LifecycleOwner? {
        var context: Context? = this
        while (true) {
            when (context) {
                is LifecycleOwner -> return context
                !is ContextWrapper -> return null
                else -> context = context.baseContext
            }
        }
    }

    if (drawable == null) {
        setImageResource(placeholder)
    }

    context.getLifecycleOwner()?.lifecycleScope?.launch {
        imageLoader.load(
            url,
            onSuccess = { setImageBitmap(it) },
            onError = { e ->
                adyenLog(AdyenLogLevel.WARN) { "Failed loading image for $url - ${e::class.simpleName}: ${e.message}" }
                setImageResource(errorFallback)
            },
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
    load(environment.checkoutShopperBaseUrl.toString() + path, imageLoader, placeholder, errorFallback)
}

@Suppress("LongParameterList")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun ImageView.loadLogo(
    environment: Environment,
    txVariant: String,
    txSubVariant: String = "",
    size: LogoSize = LogoSize.SMALL,
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
    size: LogoSize,
    txVariant: String,
    txSubVariant: String,
    densityExtension: String,
): String {
    val txString = if (txSubVariant.isEmpty()) txVariant else "$txVariant/$txSubVariant"
    return "images/logos/$size/$txString$densityExtension.png"
}
