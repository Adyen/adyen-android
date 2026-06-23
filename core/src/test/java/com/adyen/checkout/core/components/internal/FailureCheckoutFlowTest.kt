/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/6/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.error.CheckoutError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class FailureCheckoutFlowTest {

    @Nested
    inner class InitTest {

        @Test
        fun `when created, then failure is emitted with the error message`() {
            val dispatcher = TestComponentRequestDispatcher()

            FailureCheckoutFlow(
                errorMessage = "test error",
                componentRequestDispatcher = dispatcher,
            )

            assertEquals(
                CheckoutError(
                    code = CheckoutError.ErrorCode.PAYMENT_METHOD_FAILURE,
                    message = "test error",
                ),
                dispatcher.lastFailure,
            )
        }
    }

    @Nested
    inner class PropertiesTest {

        @Test
        fun `when created, then paymentComponent is null`() {
            val flow = createFailedCheckoutFlow()

            assertNull(flow.paymentComponent)
        }

        @Test
        fun `when created, then actionComponent is null`() {
            val flow = createFailedCheckoutFlow()

            assertNull(flow.actionComponent)
        }

        @Test
        fun `when created, then navigation emits nothing`() = runTest {
            val flow = createFailedCheckoutFlow()

            val result = runCatching { flow.navigation.first() }

            assertFalse(result.isSuccess)
        }
    }

    @Nested
    inner class RequiresUserInteractionTest {

        @Test
        fun `when requiresUserInteraction is called, then false is returned`() {
            val flow = createFailedCheckoutFlow()

            assertFalse(flow.requiresUserInteraction())
        }
    }

    private fun createFailedCheckoutFlow(
        errorMessage: String = "test error",
    ) = FailureCheckoutFlow(
        errorMessage = errorMessage,
        componentRequestDispatcher = TestComponentRequestDispatcher(),
    )

    private class TestComponentRequestDispatcher : ComponentRequestDispatcher {

        var lastFailure: CheckoutError? = null

        override suspend fun additionalDetails(
            data: com.adyen.checkout.core.action.data.ActionComponentData,
        ) = com.adyen.checkout.core.components.AdditionalDetailsResult.Completion("")

        override fun complete(resultCode: com.adyen.checkout.core.common.CheckoutResultCode) = Unit

        override fun failure(error: CheckoutError) {
            lastFailure = error
        }
    }
}
