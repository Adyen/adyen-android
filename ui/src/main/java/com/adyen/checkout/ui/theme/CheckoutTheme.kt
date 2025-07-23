/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

import androidx.compose.runtime.Immutable

// TODO - Add KDocs
@Immutable
data class CheckoutTheme(
    val colors: CheckoutColors = CheckoutColors.light(),
    val textStyles: AdyenTextStyles = AdyenTextStyles.default(),
    val attributes: CheckoutAttributes = CheckoutAttributes.default(),
    val elements: AdyenElements = AdyenElements.default(),
)
