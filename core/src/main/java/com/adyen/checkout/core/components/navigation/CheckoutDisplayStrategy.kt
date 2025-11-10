/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/10/2025.
 */

package com.adyen.checkout.core.components.navigation

/**
 * Defines how the content of a [CheckoutNavigationKey] should be displayed.
 */
enum class CheckoutDisplayStrategy {
    /**
     * The content is displayed directly within the current layout.
     * This is suitable for payment method forms that should be part of the rest of the content of your screen.
     */
    INLINE,

    /**
     * The content is displayed in a full-screen dialog.
     * This is typically used for secondary screens, like a phone number picker or address input.
     */
    FULL_SCREEN_DIALOG,
}
