/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/2/2023.
 */

package com.adyen.checkout.upi

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.ui.core.internal.test.TestComponentViewType
import com.adyen.checkout.upi.internal.ui.UpiComponentViewType
import com.adyen.checkout.upi.internal.ui.UpiDelegate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
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

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class UpiComponentTest(
    @Mock private val upiDelegate: UpiDelegate,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
    @Mock private val componentEventHandler: ComponentEventHandler<UpiComponentState>,
) {

    private lateinit var component: UpiComponent

    @BeforeEach
    fun beforeEach() {
        whenever(upiDelegate.viewFlow) doReturn MutableStateFlow(UpiComponentViewType)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        component = UpiComponent(
            upiDelegate = upiDelegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler,
        )

        Logger.setLogcatLevel(Logger.NONE)
    }

    @Test
    fun `when component is created then delegates are initialized`() {
        verify(upiDelegate).initialize(component.viewModelScope)
        verify(genericActionDelegate).initialize(component.viewModelScope)
        verify(componentEventHandler).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared then delegates are cleared`() {
        component.invokeOnCleared()

        verify(upiDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when observe is called then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<UpiComponentState>) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(upiDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(component.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegates is called`() {
        component.removeObserver()

        verify(upiDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized then view flow should match pay by bank delegate view flow`() = runTest {
        runCurrent()
        assertEquals(UpiComponentViewType, component.viewFlow.first())
    }

    @Test
    fun `when delegate view flow emits a value then component view flow should match that value`() = runTest {
        val delegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(upiDelegate.viewFlow) doReturn delegateViewFlow
        component = UpiComponent(upiDelegate, genericActionDelegate, actionHandlingComponent, componentEventHandler)

        val viewTestFlow = component.viewFlow.test(testScheduler)
        assertEquals(TestComponentViewType.VIEW_TYPE_1, viewTestFlow.values.last())

        delegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
        assertEquals(TestComponentViewType.VIEW_TYPE_2, viewTestFlow.values.last())

        viewTestFlow.cancel()
    }

    @Test
    fun `when action delegate view flow emits a value then component view flow should match that value`() = runTest {
        val actionDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn actionDelegateViewFlow
        component = UpiComponent(upiDelegate, genericActionDelegate, actionHandlingComponent, componentEventHandler)

        val viewTestFlow = component.viewFlow.test(testScheduler)

        // this value should match the value of the main delegate and not the action delegate
        // and in practice the initial value of the action delegate view flow is always null so it should be ignored
        assertEquals(UpiComponentViewType, viewTestFlow.values.last())

        actionDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
        assertEquals(TestComponentViewType.VIEW_TYPE_2, viewTestFlow.values.last())

        viewTestFlow.cancel()
    }
}
