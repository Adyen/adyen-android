/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/12/2022.
 */

package com.adyen.checkout.voucher

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.adyen.checkout.components.core.action.VoucherAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionComponentEventHandler
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.checkout.ui.core.internal.test.TestComponentViewType
import com.adyen.checkout.voucher.internal.ui.VoucherComponentViewType
import com.adyen.checkout.voucher.internal.ui.VoucherDelegate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class VoucherComponentTest(
    @Mock private val voucherDelegate: VoucherDelegate,
    @Mock private val actionComponentEventHandler: ActionComponentEventHandler,
) {

    private lateinit var component: VoucherComponent

    @BeforeEach
    fun before() {
        AdyenLogger.setLogLevel(Logger.NONE)

        whenever(voucherDelegate.viewFlow) doReturn MutableStateFlow(VoucherComponentViewType)
        component = VoucherComponent(voucherDelegate, actionComponentEventHandler)
    }

    @Test
    fun `when component is created then delegate is initialized`() {
        verify(voucherDelegate).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared then delegate is cleared`() {
        component.invokeOnCleared()

        verify(voucherDelegate).onCleared()
    }

    @Test
    fun `when observe is called then observe in delegate is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (ActionComponentEvent) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(voucherDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegate is called`() {
        component.removeObserver()

        verify(voucherDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized then view flow should match delegate view flow`() = runTest {
        component.viewFlow.test {
            assertEquals(VoucherComponentViewType, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `when delegate view flow emits a value then component view flow should match that value`() = runTest {
        val delegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(voucherDelegate.viewFlow) doReturn delegateViewFlow
        component = VoucherComponent(voucherDelegate, actionComponentEventHandler)

        component.viewFlow.test {
            assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())

            delegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }

    @Test
    fun `when handleAction is called then handleAction in delegate is called`() {
        val action = VoucherAction()
        val activity = Activity()
        component.handleAction(action, activity)

        verify(voucherDelegate).handleAction(action, activity)
    }
}
