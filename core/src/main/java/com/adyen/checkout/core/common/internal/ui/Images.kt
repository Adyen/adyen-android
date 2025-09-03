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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.adyen.checkout.core.common.internal.imageLoader
import com.adyen.checkout.test.R
import com.adyen.checkout.ui.internal.ImageLoader
import com.adyen.checkout.ui.internal.LogoSize
import com.adyen.checkout.ui.internal.PaymentMethodLogo as BasePaymentMethodLogo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Suppress("LongParameterList")
@Composable
fun PaymentMethodLogo(
    baseUrl: String,
    txVariant: String,
    modifier: Modifier = Modifier,
    txSubVariant: String = "",
    size: LogoSize = LogoSize.SMALL,
    contentDescription: String? = null,
    imageLoader: ImageLoader = LocalContext.current.imageLoader,
    @DrawableRes placeholder: Int = R.drawable.ic_placeholder_image,
    @DrawableRes errorFallback: Int = R.drawable.ic_placeholder_image,
) {
    BasePaymentMethodLogo(
        baseUrl = baseUrl,
        txVariant = txVariant,
        modifier = modifier,
        txSubVariant = txSubVariant,
        size = size,
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        placeholder = placeholder,
        errorFallback = errorFallback,
    )
}
