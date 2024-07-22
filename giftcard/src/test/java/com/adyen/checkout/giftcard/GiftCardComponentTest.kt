/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2022.
 */

package com.adyen.checkout.giftcard

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.giftcard.internal.ui.GiftCardComponentViewType
import com.adyen.checkout.giftcard.internal.ui.GiftCardDelegate
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.checkout.ui.core.internal.ui.TestComponentViewType
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class, LoggingExtension::class)
internal class GiftCardComponentTest(
    @Mock private val giftCardDelegate: GiftCardDelegate,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
    @Mock private val componentEventHandler: ComponentEventHandler<GiftCardComponentState>,
) {

    private lateinit var component: GiftCardComponent

    @BeforeEach
    fun before() {
        whenever(giftCardDelegate.viewFlow) doReturn MutableStateFlow(GiftCardComponentViewType)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        component = GiftCardComponent(
            giftCardDelegate = giftCardDelegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler,
        )
    }

    @Test
    fun `when component is created then delegates are initialized`() {
        verify(giftCardDelegate).initialize(component.viewModelScope)
        verify(genericActionDelegate).initialize(component.viewModelScope)
        verify(componentEventHandler).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared then delegates are cleared`() {
        component.invokeOnCleared()

        verify(giftCardDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when observe is called then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<GiftCardComponentState>) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(giftCardDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(component.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegates is called`() {
        component.removeObserver()

        verify(giftCardDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized then view flow should match gift card delegate view flow`() = runTest {
        component.viewFlow.test {
            assertEquals(GiftCardComponentViewType, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `when gift card delegate view flow emits a value then component view flow should match that value`() = runTest {
        val giftCardDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(giftCardDelegate.viewFlow) doReturn giftCardDelegateViewFlow
        component = GiftCardComponent(
            giftCardDelegate = giftCardDelegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler,
        )

        component.viewFlow.test {
            assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())

            giftCardDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }

    @Test
    fun `when action delegate view flow emits a value then component view flow should match that value`() = runTest {
        val actionDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn actionDelegateViewFlow
        component = GiftCardComponent(
            giftCardDelegate = giftCardDelegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler,
        )

        component.viewFlow.test {
            // this value should match the value of the main delegate and not the action delegate
            // and in practice the initial value of the action delegate view flow is always null so it should be ignored
            assertEquals(GiftCardComponentViewType, awaitItem())

            actionDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }

    @Test
    fun `when isConfirmationRequired, then delegate is called`() {
        component.isConfirmationRequired()
        verify(giftCardDelegate).isConfirmationRequired()
    }

    @Test
    fun `when resolveBalanceResult is called and active delegate is the payment delegate, then delegate resolveBalanceResult is called`() {
        whenever(component.delegate).thenReturn(giftCardDelegate)
        component.resolveBalanceResult(BALANCE_RESULT)
        verify(giftCardDelegate).resolveBalanceResult(BALANCE_RESULT)
    }

    @Test
    fun `when resolveBalanceResult is called and active delegate is the action delegate, then delegate resolveBalanceResult is not called`() {
        whenever(component.delegate).thenReturn(genericActionDelegate)
        component.resolveBalanceResult(BALANCE_RESULT)
        verify(giftCardDelegate, never()).resolveBalanceResult(BALANCE_RESULT)
    }

    @Test
    fun `when resolveOrderResponse is called and active delegate is the payment delegate, then delegate resolveOrderResponse is called`() {
        whenever(component.delegate).thenReturn(giftCardDelegate)
        component.resolveOrderResponse(ORDER_RESPONSE)
        verify(giftCardDelegate).resolveOrderResponse(ORDER_RESPONSE)
    }

    @Test
    fun `when resolveOrderResponse is called and active delegate is the action delegate, then delegate resolveOrderResponse is not called`() {
        whenever(component.delegate).thenReturn(genericActionDelegate)
        component.resolveOrderResponse(ORDER_RESPONSE)
        verify(giftCardDelegate, never()).resolveOrderResponse(ORDER_RESPONSE)
    }

    @Test
    fun `when submit is called and active delegate is the payment delegate, then delegate onSubmit is called`() {
        whenever(component.delegate).thenReturn(giftCardDelegate)
        component.submit()
        verify(giftCardDelegate).onSubmit()
    }

    @Test
    fun `when submit is called and active delegate is the action delegate, then delegate onSubmit is not called`() {
        whenever(component.delegate).thenReturn(genericActionDelegate)
        component.submit()
        verify(giftCardDelegate, never()).onSubmit()
    }

    companion object {
        private val BALANCE_RESULT = BalanceResult(
            balance = null,
            transactionLimit = null,
        )

        private val ORDER_RESPONSE = OrderResponse(
            pspReference = "psp",
            orderData = "orderData",
            amount = null,
            remainingAmount = null,
        )
    }
}
