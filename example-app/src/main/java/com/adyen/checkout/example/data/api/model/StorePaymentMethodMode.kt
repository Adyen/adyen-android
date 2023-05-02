package com.adyen.checkout.example.data.api.model

import androidx.annotation.Keep

@Keep
enum class StorePaymentMethodMode(val mode: String) {
    DISABLED("disabled"),
    ASK_FOR_CONSENT("askForConsent"),
    ENABLED("enabled")
}
