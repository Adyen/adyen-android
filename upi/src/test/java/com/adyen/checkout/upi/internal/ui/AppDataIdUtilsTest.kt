/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/5/2024.
 */

package com.adyen.checkout.upi.internal.ui

import com.adyen.checkout.components.core.AppData
import com.adyen.checkout.core.Environment
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class AppDataIdUtilsTest {

    @ParameterizedTest
    @MethodSource("appListSource")
    fun `when mapToPaymentApp is called, then a mapped list is returned`(
        sourceList: List<AppData>,
        environment: Environment,
        selectedItemId: String?,
        expectedList: List<UPIIntentItem.PaymentApp>
    ) {
        assertEquals(expectedList, sourceList.mapToPaymentApp(environment, selectedItemId))
    }

    companion object {
        @JvmStatic
        fun appListSource() = listOf(
            // sourceList, environment, selectedItemId, expectedList
            Arguments.arguments(
                listOf(
                    AppData("id1", "name1"),
                    AppData("id2", "name2"),
                ),
                Environment.TEST,
                "id1",
                listOf(
                    UPIIntentItem.PaymentApp("id1", "name1", Environment.TEST, true),
                    UPIIntentItem.PaymentApp("id2", "name2", Environment.TEST, false),
                ),
            ),
            Arguments.arguments(
                listOf(
                    AppData("id1", "name1"),
                    AppData("id2", "name2"),
                ),
                Environment.TEST,
                null,
                listOf(
                    UPIIntentItem.PaymentApp("id1", "name1", Environment.TEST, false),
                    UPIIntentItem.PaymentApp("id2", "name2", Environment.TEST, false),
                ),
            ),
            Arguments.arguments(
                listOf(
                    AppData("id1", "name1"),
                    AppData("id2", "name2"),
                ),
                Environment.TEST,
                null,
                listOf(
                    UPIIntentItem.PaymentApp("id1", "name1", Environment.TEST, false),
                    UPIIntentItem.PaymentApp("id2", "name2", Environment.TEST, false),
                ),
            ),
            Arguments.arguments(
                listOf(
                    AppData("id1", "name1"),
                    AppData("", ""),
                ),
                Environment.TEST,
                null,
                listOf(
                    UPIIntentItem.PaymentApp("id1", "name1", Environment.TEST, false),
                ),
            ),
            Arguments.arguments(
                listOf(
                    AppData("id1", "name1"),
                    AppData(null, "name2"),
                    AppData("id3", null),
                ),
                Environment.TEST,
                null,
                listOf(
                    UPIIntentItem.PaymentApp("id1", "name1", Environment.TEST, false),
                ),
            ),
            Arguments.arguments(
                listOf(
                    AppData(null, "name1"),
                    AppData("id2", null),
                    AppData("", "name3"),
                    AppData("id4", ""),
                    AppData(null, null),
                ),
                Environment.TEST,
                null,
                listOf<UPIIntentItem.PaymentApp>(),
            ),
        )
    }
}
