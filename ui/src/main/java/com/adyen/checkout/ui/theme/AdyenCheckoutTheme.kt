/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

data class AdyenCheckoutTheme(
    val colors: AdyenColors = adyenCheckoutLightColors(),
    val textStyles: AdyenTextStyles,
    val elements: AdyenElements,
)
