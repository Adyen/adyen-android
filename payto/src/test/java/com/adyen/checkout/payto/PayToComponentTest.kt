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
import com.adyen.checkout.payto.internal.ui.DefaultPayToDelegate
import com.adyen.checkout.payto.internal.ui.PayToComponentViewType
import com.adyen.checkout.payto.internal.ui.StoredPayToDelegate
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
    @Mock private val defaultPayToDelegate: DefaultPayToDelegate,
    @Mock private val storedPayToDelegate: StoredPayToDelegate,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
    @Mock private val componentEventHandler: ComponentEventHandler<PayToComponentState>,
) {

    private lateinit var defaultComponent: PayToComponent
    private lateinit var storedComponent: PayToComponent

    @BeforeEach
    fun before() {
        whenever(defaultPayToDelegate.viewFlow) doReturn MutableStateFlow(PayToComponentViewType)
        whenever(storedPayToDelegate.viewFlow) doReturn MutableStateFlow(null)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        defaultComponent = PayToComponent(
            defaultPayToDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )

        storedComponent = PayToComponent(
            storedPayToDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
    }

    @Test
    fun `when component is created, then delegates are initialized`() {
        verify(defaultPayToDelegate).initialize(defaultComponent.viewModelScope)
        verify(genericActionDelegate).initialize(defaultComponent.viewModelScope)
        verify(componentEventHandler).initialize(defaultComponent.viewModelScope)
    }

    @Test
    fun `when stored component is created then delegates are initialized`() {
        verify(storedPayToDelegate).initialize(storedComponent.viewModelScope)
        verify(genericActionDelegate).initialize(storedComponent.viewModelScope)
        verify(componentEventHandler).initialize(storedComponent.viewModelScope)
    }

    @Test
    fun `when component is cleared, then delegates are cleared`() {
        defaultComponent.invokeOnCleared()

        verify(defaultPayToDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when stored component is cleared then delegates are cleared`() {
        storedComponent.invokeOnCleared()

        verify(storedPayToDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when observe is called, then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<PayToComponentState>) -> Unit = {}

        defaultComponent.observe(lifecycleOwner, callback)

        verify(defaultPayToDelegate).observe(lifecycleOwner, defaultComponent.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(defaultComponent.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called, then removeObserver in delegates is called`() {
        defaultComponent.removeObserver()

        verify(defaultPayToDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized, then view flow should match delegate view flow`() = runTest {
        defaultComponent.viewFlow.test {
            assertEquals(PayToComponentViewType, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `when delegate view flow emits a value, then component view flow should match that value`() = runTest {
        val payToDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(defaultPayToDelegate.viewFlow) doReturn payToDelegateViewFlow
        defaultComponent =
            PayToComponent(defaultPayToDelegate, genericActionDelegate, actionHandlingComponent, componentEventHandler)

        defaultComponent.viewFlow.test {
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
        defaultComponent =
            PayToComponent(defaultPayToDelegate, genericActionDelegate, actionHandlingComponent, componentEventHandler)

        defaultComponent.viewFlow.test {
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
        defaultComponent.isConfirmationRequired()
        verify(defaultPayToDelegate).isConfirmationRequired()
    }

    @Test
    fun `when submit is called and active delegate is the payment delegate, then delegate onSubmit is called`() {
        whenever(defaultComponent.delegate).thenReturn(defaultPayToDelegate)
        defaultComponent.submit()
        verify(defaultPayToDelegate).onSubmit()
    }

    @Test
    fun `when submit is called and active delegate is the action delegate, then delegate onSubmit is not called`() {
        whenever(defaultComponent.delegate).thenReturn(genericActionDelegate)
        defaultComponent.submit()
        verify(defaultPayToDelegate, never()).onSubmit()
    }
}
