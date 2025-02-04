/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/2/2025.
 */

package com.adyen.checkout.payto

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.payto.internal.ui.PayToComponentViewType
import com.adyen.checkout.payto.internal.ui.PayToDelegate
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.checkout.ui.core.internal.ui.TestComponentViewType
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class, LoggingExtension::class)
internal class PayToComponentTest(
    @Mock private val payToDelegate: PayToDelegate,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
    @Mock private val componentEventHandler: ComponentEventHandler<PayToComponentState>,
) {

    private lateinit var component: PayToComponent

    @BeforeEach
    fun before() {
        whenever(payToDelegate.viewFlow) doReturn MutableStateFlow(PayToComponentViewType)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        component = PayToComponent(
            payToDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
    }

    @Test
    fun `when component is created, then delegates are initialized`() {
        verify(payToDelegate).initialize(component.viewModelScope)
        verify(genericActionDelegate).initialize(component.viewModelScope)
        verify(componentEventHandler).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared, then delegates are cleared`() {
        component.invokeOnCleared()

        verify(payToDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when observe is called, then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<PayToComponentState>) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(payToDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(component.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called, then removeObserver in delegates is called`() {
        component.removeObserver()

        verify(payToDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized, then view flow should match delegate view flow`() = runTest {
        component.viewFlow.test {
            assertEquals(PayToComponentViewType, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `when delegate view flow emits a value, then component view flow should match that value`() = runTest {
        val payToDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(payToDelegate.viewFlow) doReturn payToDelegateViewFlow
        component = PayToComponent(payToDelegate, genericActionDelegate, actionHandlingComponent, componentEventHandler)

        component.viewFlow.test {
            assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())

            payToDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }

    @Test
    fun `when action delegate view flow emits a value, then component view flow should match that value`() = runTest {
        val actionDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn actionDelegateViewFlow
        component = PayToComponent(payToDelegate, genericActionDelegate, actionHandlingComponent, componentEventHandler)

        component.viewFlow.test {
            // this value should match the value of the main delegate and not the action delegate
            // and in practice the initial value of the action delegate view flow is always null so it should be ignored
            assertEquals(PayToComponentViewType, awaitItem())

            actionDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }

    @Test
    fun `when isConfirmationRequired, then delegate is called`() {
        component.isConfirmationRequired()
        verify(payToDelegate).isConfirmationRequired()
    }

    @Test
    fun `when submit is called and active delegate is the payment delegate, then delegate onSubmit is called`() {
        whenever(component.delegate).thenReturn(payToDelegate)
        component.submit()
        verify(payToDelegate).onSubmit()
    }

    @Test
    fun `when submit is called and active delegate is the action delegate, then delegate onSubmit is not called`() {
        whenever(component.delegate).thenReturn(genericActionDelegate)
        component.submit()
        verify(payToDelegate, never()).onSubmit()
    }
}
