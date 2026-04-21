package com.adyen.checkout.card.old

/**
 * Used in [CardConfiguration.Builder.kcpAuthVisibility] to show or hide the KCP authentication input field.
 */
@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
enum class KCPAuthVisibility {
    SHOW,
    HIDE,
}
