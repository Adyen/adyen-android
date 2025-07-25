/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/11/2024.
 */

package com.adyen.checkout.paybybankus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.paybybankus.internal.PayByBankUSComponentViewType
import com.adyen.checkout.paybybankus.internal.PayByBankUSDelegate
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.checkout.ui.core.old.internal.ui.TestComponentViewType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class, LoggingExtension::class)
internal class PayByBankUSComponentTest(
    @Mock private val payByBankUSDelegate: PayByBankUSDelegate,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
    @Mock private val componentEventHandler: ComponentEventHandler<PayByBankUSComponentState>,
) {

    private lateinit var component: PayByBankUSComponent

    @BeforeEach
    fun before() {
        whenever(payByBankUSDelegate.viewFlow) doReturn MutableStateFlow(PayByBankUSComponentViewType)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        component = PayByBankUSComponent(
            payByBankUSDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
    }

    @Test
    fun `when component is created then delegates are initialized`() {
        verify(payByBankUSDelegate).initialize(component.viewModelScope)
        verify(genericActionDelegate).initialize(component.viewModelScope)
        verify(componentEventHandler).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared then delegates are cleared`() {
        component.invokeOnCleared()

        verify(payByBankUSDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when observe is called then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<PayByBankUSComponentState>) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(payByBankUSDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(component.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegates is called`() {
        component.removeObserver()

        verify(payByBankUSDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized then view flow should match pay by bank delegate view flow`() = runTest {
        component.viewFlow.test {
            assertEquals(PayByBankUSComponentViewType, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `when pay by bank delegate view flow emits a value then component view flow should match that value`() =
        runTest {
            val payByBankDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
            whenever(payByBankUSDelegate.viewFlow) doReturn payByBankDelegateViewFlow
            component = PayByBankUSComponent(
                payByBankUSDelegate,
                genericActionDelegate,
                actionHandlingComponent,
                componentEventHandler,
            )

            component.viewFlow.test {
                assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())

                payByBankDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
                assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

                expectNoEvents()
            }
        }

    @Test
    fun `when action delegate view flow emits a value then component view flow should match that value`() = runTest {
        val actionDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn actionDelegateViewFlow
        component = PayByBankUSComponent(
            payByBankUSDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler
        )

        component.viewFlow.test {
            // this value should match the value of the main delegate and not the action delegate
            // and in practice the initial value of the action delegate view flow is always null so it should be ignored
            assertEquals(PayByBankUSComponentViewType, awaitItem())

            actionDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }
}
