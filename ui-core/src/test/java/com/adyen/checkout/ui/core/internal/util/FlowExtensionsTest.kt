/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2022.
 */

package com.adyen.checkout.ui.core.internal.util

import app.cash.turbine.test
import com.adyen.checkout.ui.core.internal.ui.TestComponentViewType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class FlowExtensionsTest {

    @Test
    fun `when merging view flows, then initial value of action view flow should be ignored`() = runTest {
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
            paymentMethodViewFlow = flowOf(TestComponentViewType.VIEW_TYPE_1, TestComponentViewType.VIEW_TYPE_2),
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
