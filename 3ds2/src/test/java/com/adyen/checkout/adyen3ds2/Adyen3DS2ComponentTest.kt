/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/12/2022.
 */

package com.adyen.checkout.adyen3ds2

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.adyen.checkout.adyen3ds2.internal.ui.Adyen3DS2ComponentViewType
import com.adyen.checkout.adyen3ds2.internal.ui.Adyen3DS2Delegate
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.base.ActionComponentEventHandler
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.test.TestComponentViewType
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.threeds2.customization.UiCustomization
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
internal class Adyen3DS2ComponentTest(
    @Mock private val adyen3DS2Delegate: Adyen3DS2Delegate,
    @Mock private val actionComponentEventHandler: ActionComponentEventHandler,
) {

    private lateinit var component: Adyen3DS2Component

    @BeforeEach
    fun before() {
        Logger.setLogcatLevel(Logger.NONE)

        whenever(adyen3DS2Delegate.viewFlow) doReturn MutableStateFlow(Adyen3DS2ComponentViewType)
        component = Adyen3DS2Component(adyen3DS2Delegate, actionComponentEventHandler)
    }

    @Test
    fun `when component is created then delegate is initialized`() {
        verify(adyen3DS2Delegate).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared then delegate is cleared`() {
        component.invokeOnCleared()

        verify(adyen3DS2Delegate).onCleared()
    }

    @Test
    fun `when observe is called then observe in delegate is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (ActionComponentEvent) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(adyen3DS2Delegate).observe(lifecycleOwner, component.viewModelScope, callback)
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegate is called`() {
        component.removeObserver()

        verify(adyen3DS2Delegate).removeObserver()
    }

    @Test
    fun `when component is initialized then view flow should match delegate view flow`() = runTest {
        component.viewFlow.test {
            assertEquals(Adyen3DS2ComponentViewType, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `when delegate view flow emits a value then component view flow should match that value`() = runTest {
        val delegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(adyen3DS2Delegate.viewFlow) doReturn delegateViewFlow
        component = Adyen3DS2Component(adyen3DS2Delegate, actionComponentEventHandler)

        component.viewFlow.test {
            assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())

            delegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
            assertEquals(TestComponentViewType.VIEW_TYPE_2, awaitItem())

            expectNoEvents()
        }
    }

    @Test
    fun `when handleAction is called then handleAction in delegate is called`() {
        val action = Threeds2Action()
        val activity = Activity()
        component.handleAction(action, activity)

        verify(adyen3DS2Delegate).handleAction(action, activity)
    }

    @Test
    fun `when handleIntent is called then handleIntent in delegate is called`() {
        val intent = Intent()
        component.handleIntent(intent)

        verify(adyen3DS2Delegate).handleIntent(intent)
    }

    @Test
    fun `when setUiCustomization is called then setUiCustomization in delegate is called`() {
        val uiCustomization = UiCustomization()
        component.setUiCustomization(uiCustomization)

        verify(adyen3DS2Delegate).set3DS2UICustomization(uiCustomization)
    }
}
