/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/4/2025.
 */

package com.adyen.checkout.card.internal.analytics

import com.adyen.checkout.components.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.components.core.internal.analytics.DirectAnalyticsEventCreation

@OptIn(DirectAnalyticsEventCreation::class)
internal object CardEvents {

    fun cardScannerAvailable(
        component: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = AnalyticsEvent.Log.Type.CARD_SCANNER,
        subType = "CardScannerAvailable",
    )

    fun cardScannerUnavailable(
        component: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = AnalyticsEvent.Log.Type.CARD_SCANNER,
        subType = "CardScannerUnavailable",
    )

    fun cardScannerPresented(
        component: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = AnalyticsEvent.Log.Type.CARD_SCANNER,
        subType = "CardScannerPresented",
    )

    fun cardScannerCancelled(
        component: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = AnalyticsEvent.Log.Type.CARD_SCANNER,
        subType = "CardScannerCancelled",
    )

    fun cardScannerSuccess(
        component: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = AnalyticsEvent.Log.Type.CARD_SCANNER,
        subType = "CardScannerSuccess",
    )

    fun cardScannerFailure(
        component: String,
    ) = AnalyticsEvent.Log(
        component = component,
        type = AnalyticsEvent.Log.Type.CARD_SCANNER,
        subType = "CardScannerFailure",
    )
}
