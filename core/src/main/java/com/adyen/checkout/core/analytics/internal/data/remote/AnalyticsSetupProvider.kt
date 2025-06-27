/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/6/2025.
 */

package com.adyen.checkout.core.analytics.internal.data.remote

import com.adyen.checkout.core.internal.data.model.AnalyticsSetupRequest

internal interface AnalyticsSetupProvider {
    fun provide(): AnalyticsSetupRequest
}
