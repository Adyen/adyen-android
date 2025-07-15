/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.util

import app.cash.turbine.test
import com.adyen.checkout.ui.core.old.internal.ui.TestComponentViewType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class FlowExtensionsTest {

    @Test
    fun `when merging view flows, then initial value of action view flow should be ignored`() =
        runTest {
            mergeViewFlows(
                scope = this,
                paymentMethodViewFlow = flowOf(TestComponentViewType.VIEW_TYPE_1),
                genericActionViewFlow = flowOf(null),
            ).test {
                // Initial value of merged flow
                assertNull(awaitItem())

                // Initial value of paymentMethodViewFlow
                assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())

                // Assert initial value of genericActionViewFlow is not emitted
                ensureAllEventsConsumed()
            }
        }

    @Test
    fun `when merging view flows, then subsequent values should be emitted`() = runTest {
        mergeViewFlows(
            scope = this,
            paymentMethodViewFlow = flowOf(
                TestComponentViewType.VIEW_TYPE_1,
                TestComponentViewType.VIEW_TYPE_2
            ),
            genericActionViewFlow = flowOf(null, TestComponentViewType.VIEW_TYPE_3),
        ).test {
            // Initial value of merged flow
            assertNull(awaitItem())

            assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())
            assertEquals(TestComponentViewType.VIEW_TYPE_3, awaitItem())
        }
    }
}
