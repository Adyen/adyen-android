package com.adyen.checkout.example.data.api.model

import androidx.annotation.Keep

@Keep
enum class RecurringProcessingModel(val recurringModel: String) {
    SUBSCRIPTION("Subscription"),
    CARD_ON_FILE("CardOnFile"),
    UNSCHEDULED_CARD_ON_FILE("UnscheduledCardOnFile")
}
