/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 5/12/2022.
 */

package com.adyen.checkout.components.imageloader

import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.adyen.checkout.components.imageloader.repository.DefaultImageLoaderRepository
import com.adyen.checkout.components.imageloader.repository.ImageLoaderRepository
import com.adyen.checkout.core.api.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class DefaultImageLoader(
    private val imageLoaderRepository: ImageLoaderRepository
) : CheckoutImageLoader {

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var densityExtension: String = ""

    override fun loadLogo(
        txVariant: String,
        view: ImageView,
        @DrawableRes placeholder: Int,
        @DrawableRes errorFallback: Int
    ) {
        loadLogo(txVariant, "", view, placeholder, errorFallback)
    }

    override fun loadLogo(
        txVariant: String,
        view: ImageView,
        size: LogoSize?,
        @DrawableRes placeholder: Int,
        @DrawableRes errorFallback: Int
    ) {
        loadLogo(txVariant, "", size, view, placeholder, errorFallback)
    }

    override fun loadLogo(
        txVariant: String,
        txSubVariant: String,
        view: ImageView,
        @DrawableRes placeholder: Int,
        @DrawableRes errorFallback: Int
    ) {
        loadLogo(txVariant, txSubVariant, LogoSize.SMALL, view, placeholder, errorFallback)
    }

    override fun loadLogo(
        txVariant: String,
        txSubVariant: String,
        size: LogoSize?,
        view: ImageView,
        @DrawableRes placeholder: Int,
        @DrawableRes errorFallback: Int
    ) {
        densityExtension = view.resources.displayMetrics.densityDpi.getDensityExtension()
        val logoPath = buildLogoPath(txVariant, txSubVariant, size)
        coroutineScope.launch(Dispatchers.Main) {
            view.setImageResource(placeholder)
            val bitmap: Bitmap? = imageLoaderRepository.load(logoPath)
            bitmap?.let { view.setImageBitmap(it) } ?: run { view.setImageResource(errorFallback) }
        }
    }

    override fun load(
        imagePath: String,
        view: ImageView,
        placeholder: Int,
        errorFallback: Int
    ) {
        coroutineScope.launch(Dispatchers.Main) {
            view.setImageResource(placeholder)
            val bitmap: Bitmap? = imageLoaderRepository.load(imagePath)
            bitmap?.let { view.setImageBitmap(it) } ?: run { view.setImageResource(errorFallback) }
        }
    }

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
    }

    private fun buildLogoPath(txVariant: String, txSubVariant: String?, size: LogoSize?): String {
        val txString = if (txSubVariant.isNullOrEmpty()) txVariant else "$txVariant/$txSubVariant"
        return String.format(LOGO_PATH, getSizeVariant(size), txString + densityExtension)
    }

    private fun getSizeVariant(size: LogoSize?): String {
        return (size ?: LogoSize.SMALL).toString()
    }

    companion object {
        private const val LOGO_PATH = "images/logos/%1\$s/%2\$s.png"

        private var instance: AtomicReference<DefaultImageLoader>? = null

        @JvmStatic
        fun with(environment: Environment): DefaultImageLoader {
            val checkInstance = instance
            if (checkInstance == null) {
                val created = AtomicReference(
                    DefaultImageLoader(
                        imageLoaderRepository = DefaultImageLoaderRepository.getInstance(environment)
                    )
                )
                instance = created
                return created.get()
            }
            return checkInstance.get()
        }
    }
}

enum class LogoSize {
    /**
     * Size for small logos (height: 26dp).
     */
    SMALL,

    /**
     * Size for medium logos (height: 50dp).
     */
    MEDIUM,

    /**
     * Size for large logos (height: 100dp).
     */
    LARGE;

    override fun toString(): String {
        return name.lowercase()
    }
}

internal fun Int.getDensityExtension(): String {
    return when {
        this <= DisplayMetrics.DENSITY_LOW -> "-ldpi"
        this <= DisplayMetrics.DENSITY_MEDIUM -> "" // no extension
        this <= DisplayMetrics.DENSITY_HIGH -> "-hdpi"
        this <= DisplayMetrics.DENSITY_XHIGH -> "-xhdpi"
        this <= DisplayMetrics.DENSITY_XXHIGH -> "-xxhdpi"
        else -> "-xxxhdpi"
    }
}
