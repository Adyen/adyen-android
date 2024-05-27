/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/5/2024.
 */

package com.adyen.checkout.upi.internal.ui

import com.adyen.checkout.components.core.App
import com.adyen.checkout.core.Environment
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class UPIAppIdUtilsTest {

    @ParameterizedTest
    @MethodSource("appListSource")
    fun `when mapToPaymentApp is called, then a mapped list is returned`(
        sourceList: List<App>,
        environment: Environment,
        expectedList: List<UPIIntentItem.PaymentApp>
    ) {
        assertEquals(expectedList, sourceList.mapToPaymentApp(environment))
    }

    companion object {
        @JvmStatic
        fun appListSource() = listOf(
            // sourceList, environment, expectedList
            Arguments.arguments(
                listOf(
                    App("id1", "name1"),
                    App("id2", "name2"),
                ),
                Environment.TEST,
                listOf(
                    UPIIntentItem.PaymentApp("id1", "name1", Environment.TEST),
                    UPIIntentItem.PaymentApp("id2", "name2", Environment.TEST),
                ),
            ),
            Arguments.arguments(
                listOf(
                    App("id1", "name1"),
                    App("", ""),
                ),
                Environment.TEST,
                listOf(
                    UPIIntentItem.PaymentApp("id1", "name1", Environment.TEST),
                ),
            ),
            Arguments.arguments(
                listOf(
                    App("id1", "name1"),
                    App(null, "name2"),
                    App("id3", null),
                ),
                Environment.TEST,
                listOf(
                    UPIIntentItem.PaymentApp("id1", "name1", Environment.TEST),
                ),
            ),
            Arguments.arguments(
                listOf(
                    App(null, "name1"),
                    App("id2", null),
                    App("", "name3"),
                    App("id4", ""),
                    App(null, null),
                ),
                Environment.TEST,
                listOf<UPIIntentItem.PaymentApp>(),
            ),
        )
    }
}
