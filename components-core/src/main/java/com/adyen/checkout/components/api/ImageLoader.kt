/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 26/7/2019.
 */
package com.adyen.checkout.components.api

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.adyen.checkout.components.api.LogoApi.Companion.getInstance
import com.adyen.checkout.components.api.LogoTask.LogoCallback
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import java.lang.ref.WeakReference

/**
 * Loading Image from LogoApi.
 */
class ImageLoader(private val logoApi: LogoApi) {
    private val callbacks: MutableMap<String, LogoCallback> = HashMap()
    private val imageViews: MutableMap<String, WeakReference<ImageView>> = HashMap()

    /**
     * Load image to ImageView with place holder before load and error fallback image.
     */
    @JvmOverloads
    fun load(
        txVariant: String,
        view: ImageView,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes errorFallback: Int = 0
    ) {
        load(txVariant, "", view, placeholder, errorFallback)
    }

    /**
     * Load image to ImageView with place holder before load and error fallback image.
     */
    @JvmOverloads
    fun load(
        txVariant: String,
        view: ImageView,
        size: LogoApi.Size?,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes errorFallback: Int = 0
    ) {
        load(txVariant, "", view, size, placeholder, errorFallback)
    }

    /**
     * Load image to ImageView with place holder before load and error fallback image.
     */
    @JvmOverloads
    fun load(
        txVariant: String,
        txSubVariant: String,
        view: ImageView,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes errorFallback: Int = 0
    ) {
        load(txVariant, txSubVariant, view, LogoApi.DEFAULT_SIZE, placeholder, errorFallback)
    }

    /**
     * Load image to ImageView with place holder before load and error fallback image.
     */
    @Suppress("LongParameterList")
    @JvmOverloads
    fun load(
        txVariant: String,
        txSubVariant: String,
        view: ImageView,
        size: LogoApi.Size?,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes errorFallback: Int = 0
    ) {
        if (placeholder != 0) {
            view.setImageResource(placeholder)
        }
        val id = txVariant + txSubVariant + view.hashCode()
        if (callbacks.containsKey(id)) {
            callbacks.remove(id)
            imageViews.remove(id)
        }
        val callback: LogoCallback = object : LogoCallback {
            override fun onLogoReceived(drawable: BitmapDrawable) {
                val imageView = imageViews[id]?.get()
                if (imageView != null) {
                    imageView.setImageDrawable(drawable)
                } else {
                    Logger.e(TAG, "ImageView is null for received Logo - $id")
                }
                callbacks.remove(id)
                imageViews.remove(id)
            }

            override fun onReceiveFailed() {
                val imageView = imageViews[id]?.get()
                if (imageView != null) {
                    imageView.setImageResource(errorFallback)
                } else {
                    Logger.e(TAG, "ImageView is null for failed Logo - $id")
                }
                callbacks.remove(id)
                imageViews.remove(id)
            }
        }
        imageViews[id] = WeakReference(view)
        callbacks[id] = callback
        logoApi.getLogo(txVariant, txSubVariant, size, callback)
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmStatic
        fun getInstance(context: Context, environment: Environment): ImageLoader {
            return ImageLoader(getInstance(environment, context.resources.displayMetrics))
        }
    }
}
