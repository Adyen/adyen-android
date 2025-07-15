/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 12/12/2022.
 */

package com.adyen.checkout.bacs

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.bacs.internal.ui.BacsComponentViewType
import com.adyen.checkout.bacs.internal.ui.BacsDirectDebitDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
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
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class, LoggingExtension::class)
internal class BacsDirectDebitComponentTest(
    @Mock private val bacsDirectDebitDelegate: BacsDirectDebitDelegate,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
    @Mock private val componentEventHandler: ComponentEventHandler<BacsDirectDebitComponentState>,
) {

    private lateinit var component: BacsDirectDebitComponent

    @BeforeEach
    fun before() {
        whenever(bacsDirectDebitDelegate.viewFlow) doReturn MutableStateFlow(BacsComponentViewType.INPUT)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        component = BacsDirectDebitComponent(
            bacsDirectDebitDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
    }

    @Test
    fun `when component is created then delegates are initialized`() {
        verify(bacsDirectDebitDelegate).initialize(component.viewModelScope)
        verify(genericActionDelegate).initialize(component.viewModelScope)
        verify(componentEventHandler).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared then delegates are cleared`() {
        component.invokeOnCleared()

        verify(bacsDirectDebitDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when observe is called then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<BacsDirectDebitComponentState>) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(bacsDirectDebitDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(component.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegates is called`() {
        component.removeObserver()

        verify(bacsDirectDebitDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized then view flow should match bacs delegate view flow`() = runTest {
        component.viewFlow.test {
            assertEquals(BacsComponentViewType.INPUT, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `when bacs delegate view flow emits a value then component view flow should match that value`() = runTest {
        val bacsDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(bacsDirectDebitDelegate.viewFlow) doReturn bacsDelegateViewFlow
        component = BacsDirectDebitComponent(
            bacsDirectDebitDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )

        component.viewFlow.test {
            assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())

            bacsDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }

    @Test
    fun `when action delegate view flow emits a value then component view flow should match that value`() = runTest {
        val actionDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn actionDelegateViewFlow
        component = BacsDirectDebitComponent(
            bacsDirectDebitDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )

        component.viewFlow.test {
            // this value should match the value of the main delegate and not the action delegate
            // and in practice the initial value of the action delegate view flow is always null so it should be ignored
            assertEquals(BacsComponentViewType.INPUT, awaitItem())

            actionDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }

    @Test
    fun `when setConfirmationMode is called then delegate setMode is called`() {
        whenever(actionHandlingComponent.activeDelegate) doReturn bacsDirectDebitDelegate
        component.setConfirmationMode()
        verify(bacsDirectDebitDelegate).setMode(BacsDirectDebitMode.CONFIRMATION)
    }

    @Test
    fun `when setInputMode is called then delegate setMode is called`() {
        whenever(actionHandlingComponent.activeDelegate) doReturn bacsDirectDebitDelegate
        component.setInputMode()
        verify(bacsDirectDebitDelegate).setMode(BacsDirectDebitMode.INPUT)
    }

    @Test
    fun `when isConfirmationRequired, then delegate is called`() {
        component.isConfirmationRequired()
        verify(bacsDirectDebitDelegate).isConfirmationRequired()
    }

    @Test
    fun `when submit is called and active delegate is the payment delegate, then delegate onSubmit is called`() {
        whenever(component.delegate).thenReturn(bacsDirectDebitDelegate)
        component.submit()
        verify(bacsDirectDebitDelegate).onSubmit()
    }

    @Test
    fun `when submit is called and active delegate is the action delegate, then delegate onSubmit is not called`() {
        whenever(component.delegate).thenReturn(genericActionDelegate)
        component.submit()
        verify(bacsDirectDebitDelegate, never()).onSubmit()
    }
}
