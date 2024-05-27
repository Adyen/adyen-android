/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/5/2024.
 */

package com.adyen.checkout.upi.internal.ui.model

import com.adyen.checkout.core.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class UPISelectedModeTest {

    @ParameterizedTest
    @MethodSource("upiModeSource")
    fun `when upiMode is Intent, then Intent selected mode is returned`(
        upiMode: UPIMode,
        selectedMode: UPISelectedMode
    ) {
        assertEquals(selectedMode, upiMode.mapToSelectedMode())
    }

    companion object {
        @JvmStatic
        fun upiModeSource() = listOf(
            // upiMode, selectedMode
            Arguments.arguments(
                UPIMode.Intent(listOf(), UPIIntentItem.PaymentApp("", "", Environment.TEST)),
                UPISelectedMode.INTENT,
            ),
            Arguments.arguments(
                UPIMode.Intent(listOf(), UPIIntentItem.GenericApp),
                UPISelectedMode.INTENT,
            ),
            Arguments.arguments(
                UPIMode.Intent(listOf(), UPIIntentItem.ManualInput(null)),
                UPISelectedMode.INTENT,
            ),
            Arguments.arguments(
                UPIMode.Vpa,
                UPISelectedMode.VPA,
            ),
            Arguments.arguments(
                UPIMode.Qr,
                UPISelectedMode.QR,
            ),
        )
    }
}
