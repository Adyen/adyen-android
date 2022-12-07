/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2022.
 */

package com.adyen.checkout.components.extensions

import app.cash.turbine.test
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
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
            paymentMethodViewFlow = flowOf(TestComponentViewType("test")),
            genericActionViewFlow = flowOf(null),
        ).test {
            // Initial value of merged flow
            assertNull(awaitItem())

            // Initial value of paymentMethodViewFlow
            assertEquals(TestComponentViewType("test"), awaitItem())

            // Assert initial value of genericActionViewFlow is not emitted
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `when merging view flows, then subsequent values should be emitted`() = runTest {
        mergeViewFlows(
            scope = this,
            paymentMethodViewFlow = flowOf(TestComponentViewType("view 1"), TestComponentViewType("view 2")),
            genericActionViewFlow = flowOf(null, TestComponentViewType("action 1")),
        ).test {
            // Initial value of merged flow
            assertNull(awaitItem())

            assertEquals(TestComponentViewType("view 1"), awaitItem())
            assertEquals(TestComponentViewType("view 2"), awaitItem())
            assertEquals(TestComponentViewType("action 1"), awaitItem())
        }
    }

    private data class TestComponentViewType(val name: String) : ComponentViewType {
        override val viewProvider: ViewProvider
            get() = throw IllegalStateException("This should not be called from unit tests")
    }
}
