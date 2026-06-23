/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 23/6/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import com.adyen.checkout.core.common.test
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateFactory
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateReducer
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayComponentStateValidator
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayViewStateProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
internal class GooglePayComponentTest {

    @Test
    fun `when requiresUserInteraction is called, then it returns true`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

        assertTrue(component.requiresUserInteraction())
    }

    @Test
    fun `when setLoading is called with true, then the loading state is updated`() = runTest {
        val component = createComponent(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        val viewState = component.viewState.test(testScheduler)

        component.setLoading(true)

        assertTrue(viewState.latestValue.isLoading)
    }

    private fun createComponent(coroutineScope: CoroutineScope) = GooglePayComponent(
        analyticsManager = mock(),
        componentParams = mock<GooglePayComponentParams>(),
        sdkDataProvider = mock<SdkDataProvider>(),
        paymentMethodType = "googlepay",
        componentStateValidator = GooglePayComponentStateValidator(),
        componentStateFactory = GooglePayComponentStateFactory(),
        componentStateReducer = GooglePayComponentStateReducer(),
        viewStateProducer = GooglePayViewStateProducer(),
        coroutineScope = coroutineScope,
    )
}
