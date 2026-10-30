/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/10/2026.
 */

package com.adyen.checkout.dropin.internal.analytics

import com.adyen.checkout.core.analytics.internal.AnalyticsEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class StoredPaymentMethodManagementEventsTest {

    @Test
    fun `when rendered is called then event has correct component and type`() {
        // WHEN
        val event = StoredPaymentMethodManagementEvents.rendered()

        // THEN
        assertEquals("storedPaymentMethodManagement", event.component)
        assertEquals(AnalyticsEvent.Info.Type.RENDERED, event.type)
        assertNull(event.target)
    }

    @Test
    fun `when removeClicked is called then event has correct component, type and target`() {
        // WHEN
        val event = StoredPaymentMethodManagementEvents.removeClicked()

        // THEN
        assertEquals("storedPaymentMethodManagement", event.component)
        assertEquals(AnalyticsEvent.Info.Type.CLICKED, event.type)
        assertEquals("stored_payment_method_remove_button", event.target)
    }
}
