/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/10/2026.
 */

package com.adyen.checkout.dropin.internal.analytics

import com.adyen.checkout.core.analytics.internal.GenericEvents

internal object StoredPaymentMethodManagementEvents {

    fun rendered() = GenericEvents.rendered(component = ANALYTICS_COMPONENT)

    fun removeClicked() = GenericEvents.clicked(
        component = ANALYTICS_COMPONENT,
        target = ANALYTICS_TARGET_REMOVE_BUTTON,
    )

    private const val ANALYTICS_COMPONENT = "storedPaymentMethodManagement"
    private const val ANALYTICS_TARGET_REMOVE_BUTTON = "stored_payment_method_remove_button"
}
