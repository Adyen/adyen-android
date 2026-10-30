/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/10/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.getPaymentMethods

/**
 * The source of Drop-in's analytics setup call, listing the types of the regular payment methods.
 */
internal fun CheckoutContext.dropInAnalyticsSource(): AnalyticsSource {
    return AnalyticsSource.DropIn(getPaymentMethods().map { it.type })
}
