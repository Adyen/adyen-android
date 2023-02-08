package com.adyen.checkout.example.data.api.model

enum class StorePaymentMethodMode(val mode: String) {
    DISABLED("disabled"),
    ASK_FOR_CONSENT("askForConsent"),
    ENABLED("enabled")
}
