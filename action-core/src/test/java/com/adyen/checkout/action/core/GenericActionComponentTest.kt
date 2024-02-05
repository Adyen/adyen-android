/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/12/2022.
 */

package com.adyen.checkout.action.core

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionComponentEventHandler
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.checkout.ui.core.internal.test.TestComponentViewType
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

@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class, LoggingExtension::class)
internal class GenericActionComponentTest(
    @Mock private val actionDelegate: ActionDelegate,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
    @Mock private val actionComponentEventHandler: ActionComponentEventHandler,
) {

    private lateinit var component: GenericActionComponent

    @BeforeEach
    fun before() {
        whenever(genericActionDelegate.delegate) doReturn actionDelegate
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        component = GenericActionComponent(genericActionDelegate, actionHandlingComponent, actionComponentEventHandler)
    }

    @Test
    fun `when component is created then delegate is initialized`() {
        verify(genericActionDelegate).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared then delegate is cleared`() {
        component.invokeOnCleared()

        verify(genericActionDelegate).onCleared()
    }

    @Test
    fun `component delegate is the same as the delegate inside GenericActionDelegate`() {
        assertEquals(actionDelegate, component.delegate)
    }

    @Test
    fun `when observe is called then observe in delegate is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (ActionComponentEvent) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(genericActionDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegate is called`() {
        component.removeObserver()

        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized then view flow should match delegate view flow`() = runTest {
        component.viewFlow.test {
            assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `when delegate view flow emits a value then component view flow should match that value`() = runTest {
        val delegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn delegateViewFlow
        component = GenericActionComponent(genericActionDelegate, actionHandlingComponent, actionComponentEventHandler)

        component.viewFlow.test {
            assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())

            delegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }
}
