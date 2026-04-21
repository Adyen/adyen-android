package com.adyen.checkout.card.old

/**
 * Used in [CardConfiguration.Builder.socialSecurityNumberVisibility] to show or hide the social security number input
 * field.
 */
@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
enum class SocialSecurityNumberVisibility {
    SHOW,
    HIDE,
}
