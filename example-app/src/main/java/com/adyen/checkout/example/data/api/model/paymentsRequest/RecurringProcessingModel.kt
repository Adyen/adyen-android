/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/5/2023.
 */

package com.adyen.checkout.example.data.api.model.paymentsRequest

import androidx.annotation.Keep

@Keep
enum class RecurringProcessingModel(val recurringModel: String) {
    SUBSCRIPTION("Subscription"),
    CARD_ON_FILE("CardOnFile"),
    UNSCHEDULED_CARD_ON_FILE("UnscheduledCardOnFile")
}
