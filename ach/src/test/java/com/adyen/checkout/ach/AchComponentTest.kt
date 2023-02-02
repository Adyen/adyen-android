/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 31/1/2023.
 */

package com.adyen.checkout.ach

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.AchPaymentMethod
import com.adyen.checkout.components.test.TestComponentViewType
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class AchComponentTest(
    @Mock private val achDelegate: AchDelegate,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
) {

    private lateinit var component: AchComponent

    @BeforeEach
    fun before() {
        whenever(achDelegate.viewFlow) doReturn MutableStateFlow(AchComponentViewType)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        component = AchComponent(
            achDelegate,
            genericActionDelegate,
            actionHandlingComponent
        )
    }

    @Test
    fun `when component is created then delegates are initialized`() {
        verify(achDelegate).initialize(component.viewModelScope)
        verify(genericActionDelegate).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared then delegates are cleared`() {
        component.invokeOnCleared()

        verify(achDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
    }

    @Test
    fun `when observe is called then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<PaymentComponentState<AchPaymentMethod>>) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(achDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(component.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegates is called`() {
        component.removeObserver()

        verify(achDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized then view flow should match ach delegate view flow`() = runTest {
        val event = component.viewFlow.first()
        assertEquals(AchComponentViewType, event)
    }

    @Test
    fun `when ach delegate view flow emits a value then component view flow should match that value`() = runTest {
        val achDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(achDelegate.viewFlow) doReturn achDelegateViewFlow
        component = AchComponent(achDelegate, genericActionDelegate, actionHandlingComponent)
        assertEquals(TestComponentViewType.VIEW_TYPE_1, component.viewFlow.first())
        achDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
        assertEquals(TestComponentViewType.VIEW_TYPE_2, component.viewFlow.first())
    }

    @Test
    fun `when action delegate view flow emits a value then component view flow should match that value`() = runTest {
        val achDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn achDelegateViewFlow
        component = AchComponent(achDelegate, genericActionDelegate, actionHandlingComponent)

        // this value should match the value of the main delegate and not the action delegate
        // and in practice the initial value of the action delegate view flow is always null so it should be ignored
        assertEquals(AchComponentViewType, component.viewFlow.first())
        achDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
        assertEquals(TestComponentViewType.VIEW_TYPE_2, component.viewFlow.first())
    }

    @Test
    fun `when isConfirmationRequired, then delegate is called`() {
        component.isConfirmationRequired()
        verify(achDelegate).isConfirmationRequired()
    }

    @Test
    fun `when submit is called and active delegate is the payment delegate, then delegate onSubmit is called`() {
        whenever(component.delegate).thenReturn(achDelegate)
        component.submit()
        verify(achDelegate).onSubmit()
    }

    @Test
    fun `when submit is called and active delegate is the action delegate, then delegate onSubmit is not called`() {
        whenever(component.delegate).thenReturn(genericActionDelegate)
        component.submit()
        verify(achDelegate, never()).onSubmit()
    }
}
