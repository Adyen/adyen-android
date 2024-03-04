/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/3/2024.
 */

package com.adyen.checkout.components.core.internal.analytics.data.remote

import com.adyen.checkout.components.core.internal.data.model.AnalyticsSetupRequest

internal interface AnalyticsSetupProvider {
    fun provide(): AnalyticsSetupRequest
}
