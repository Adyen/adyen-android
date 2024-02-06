/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/2/2024.
 */

package com.adyen.checkout.core.internal.analytics

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AdyenAnalytics {

    fun setup() {
        // See DefaultAnalyticsRepository.setupAnalytics
    }

    fun track(event: AnalyticsEvent) {
        // Queue the event
        // Send it
    }
}
