/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 2/12/2022.
 */

package com.adyen.checkout.components.imageloader

import android.widget.ImageView
import androidx.annotation.DrawableRes
import kotlinx.coroutines.CoroutineScope

interface CheckoutImageLoader {
    fun loadLogo(
        txVariant: String,
        view: ImageView,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes errorFallback: Int = 0
    )

    fun loadLogo(
        txVariant: String,
        view: ImageView,
        size: LogoSize?,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes errorFallback: Int = 0
    )

    fun loadLogo(
        txVariant: String,
        txSubVariant: String,
        view: ImageView,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes errorFallback: Int = 0
    )

    fun loadLogo(
        txVariant: String,
        txSubVariant: String,
        size: LogoSize?,
        view: ImageView,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes errorFallback: Int = 0
    )

    fun load(
        imagePath: String,
        view: ImageView,
        @DrawableRes placeholder: Int = 0,
        @DrawableRes errorFallback: Int = 0
    )

    fun initialize(coroutineScope: CoroutineScope)
}
