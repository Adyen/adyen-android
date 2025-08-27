/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/8/2025.
 */

package com.adyen.checkout.ui.internal

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.annotation.RestrictTo
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.adyen.checkout.test.R

/**
 * Represents the state of an image loading operation.
 */
private sealed class ImageLoadState {
    object Loading : ImageLoadState()
    data class Success(val bitmap: Bitmap) : ImageLoadState()
    object Error : ImageLoadState()
}

/**
 * Composable that loads an image from a URL using the provided ImageLoader.
 *
 * This is the core composable that handles the state management of the image loading process.
 *
 * @param url The URL of the image to load.
 * @param modifier The modifier to be applied to the Image.
 * @param contentDescription The content description for accessibility.
 * @param imageLoader The ImageLoader instance to use. Defaults to the one provided by the Context.
 * @param placeholder A painter to show while the image is loading.
 * @param errorFallback A painter to show if the image fails to load.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    imageLoader: ImageLoader,
    placeholder: Painter,
    errorFallback: Painter,
    modifier: Modifier = Modifier,
) {
    // State to hold the result of the image loading
    var imageLoadState by remember(url) { mutableStateOf<ImageLoadState>(ImageLoadState.Loading) }

    // Trigger the image loading effect when the URL or imageLoader changes
    LaunchedEffect(url, imageLoader) {
        imageLoader.load(
            url = url,
            onSuccess = { bitmap ->
                imageLoadState = ImageLoadState.Success(bitmap)
            },
            onError = {
                // TODO - Logger needed in ui module
                // adyenLog(AdyenLogLevel.WARN) {
                //  "Failed loading image for $url - ${it::class.simpleName}: ${it.message}"
                // }
                imageLoadState = ImageLoadState.Error
            }
        )
    }

    // Display the image based on the current state
    when (val state = imageLoadState) {
        is ImageLoadState.Loading -> {
            Image(
                painter = placeholder,
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
        is ImageLoadState.Success -> {
            Image(
                bitmap = state.bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
        is ImageLoadState.Error -> {
            Image(
                painter = errorFallback,
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
    }
}

/**
 * A convenience overload for NetworkImage that uses drawable resource IDs for placeholder and error.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    @DrawableRes placeholder: Int = R.drawable.ic_placeholder_image,
    @DrawableRes errorFallback: Int = R.drawable.ic_placeholder_image,
) {
    NetworkImage(
        url = url,
        modifier = modifier,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        placeholder = painterResource(id = placeholder),
        errorFallback = painterResource(id = errorFallback)
    )
}

/**
 * A composable to load a logo using the Adyen environment and txVariant.
 * This mirrors the functionality of the existing ImageView.loadLogo extension function.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Suppress("LongParameterList")
@Composable
fun PaymentMethodLogo(
    baseUrl: String,
    txVariant: String,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    txSubVariant: String = "",
    size: LogoSize = LogoSize.SMALL,
    contentDescription: String? = null,
    @DrawableRes placeholder: Int = R.drawable.ic_placeholder_image,
    @DrawableRes errorFallback: Int = R.drawable.ic_placeholder_image,
) {
    val context = LocalContext.current
    val fullUrl = remember(context, baseUrl, size, txVariant, txSubVariant) {
        val densityExtension = context.resources.displayMetrics.densityDpi.getDensityExtension()
        val logoPath = buildLogoPath(size, txVariant, txSubVariant, densityExtension)
        baseUrl + logoPath
    }

    NetworkImage(
        url = fullUrl,
        modifier = modifier,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        placeholder = placeholder,
        errorFallback = errorFallback
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
