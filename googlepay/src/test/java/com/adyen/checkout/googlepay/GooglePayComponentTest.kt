/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/12/2022.
 */

package com.adyen.checkout.googlepay

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.googlepay.internal.ui.DefaultGooglePayDelegate
import com.adyen.checkout.googlepay.internal.ui.GooglePayComponentViewType
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.checkout.test.extensions.test
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
internal class GooglePayComponentTest(
    @Mock private val googlePayDelegate: DefaultGooglePayDelegate,
    @Mock private val genericActionDelegate: GenericActionDelegate,
    @Mock private val actionHandlingComponent: DefaultActionHandlingComponent,
    @Mock private val componentEventHandler: ComponentEventHandler<GooglePayComponentState>,
) {

    private lateinit var component: GooglePayComponent

    @BeforeEach
    fun before() {
        whenever(googlePayDelegate.viewFlow) doReturn MutableStateFlow(GooglePayComponentViewType)
        whenever(genericActionDelegate.viewFlow) doReturn MutableStateFlow(null)

        component = GooglePayComponent(
            googlePayDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
    }

    @Test
    fun `when component is created then delegates are initialized`() {
        verify(googlePayDelegate).initialize(component.viewModelScope)
        verify(genericActionDelegate).initialize(component.viewModelScope)
        verify(componentEventHandler).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared then delegates are cleared`() {
        component.invokeOnCleared()

        verify(googlePayDelegate).onCleared()
        verify(genericActionDelegate).onCleared()
        verify(componentEventHandler).onCleared()
    }

    @Test
    fun `when observe is called then observe in delegates is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (PaymentComponentEvent<GooglePayComponentState>) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(googlePayDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
        verify(genericActionDelegate).observe(eq(lifecycleOwner), eq(component.viewModelScope), any())
    }

    @Test
    fun `when removeObserver is called then removeObserver in delegates is called`() {
        component.removeObserver()

        verify(googlePayDelegate).removeObserver()
        verify(genericActionDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized, then view flow should match google pay delegate view flow`() = runTest {
        val viewFlow = component.viewFlow.test(testScheduler)

        assertEquals(GooglePayComponentViewType, viewFlow.latestValue)
    }

    @Test
    fun `when google pay delegate view flow emits a value then component view flow should match that value`() =
        runTest {
            val delegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
            whenever(googlePayDelegate.viewFlow) doReturn delegateViewFlow
            component = GooglePayComponent(
                googlePayDelegate,
                genericActionDelegate,
                actionHandlingComponent,
                componentEventHandler,
            )

            val testViewFlow = component.viewFlow.test(testScheduler)

            assertEquals(TestComponentViewType.VIEW_TYPE_1, testViewFlow.latestValue)

            delegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)

            assertEquals(TestComponentViewType.VIEW_TYPE_2, testViewFlow.latestValue)
        }

    @Test
    fun `when action delegate view flow emits a value then component view flow should match that value`() = runTest {
        val actionDelegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(genericActionDelegate.viewFlow) doReturn actionDelegateViewFlow
        component = GooglePayComponent(
            googlePayDelegate = googlePayDelegate,
            genericActionDelegate = genericActionDelegate,
            actionHandlingComponent = actionHandlingComponent,
            componentEventHandler = componentEventHandler,
        )

        val testViewFlow = component.viewFlow.test(testScheduler)

        // this value should match the value of the main delegate and not the action delegate
        // and in practice the initial value of the action delegate view flow is always null so it should be ignored
        assertEquals(GooglePayComponentViewType, testViewFlow.latestValue)

        actionDelegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)

        assertEquals(TestComponentViewType.VIEW_TYPE_2, testViewFlow.latestValue)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `when startGooglePayScreen is called then delegate startGooglePayScreen is called`() {
        val activity = Activity()
        val requestCode = 1
        component.startGooglePayScreen(activity, requestCode)
        verify(googlePayDelegate).startGooglePayScreen(activity, requestCode)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `when handleActivityResult is called then delegate handleActivityResult is called`() {
        val requestCode = 1
        val intent = Intent()
        component.handleActivityResult(requestCode, intent)
        verify(googlePayDelegate).handleActivityResult(requestCode, intent)
    }

    @Test
    fun `when submit is called and active delegate is the payment delegate, then delegate onSubmit is called`() {
        component = GooglePayComponent(
            googlePayDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
        whenever(component.delegate).thenReturn(googlePayDelegate)

        component.submit()

        verify(googlePayDelegate).onSubmit()
    }

    @Test
    fun `when submit is called and active delegate is the action delegate, then delegate onSubmit is not called`() {
        component = GooglePayComponent(
            googlePayDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
        whenever(component.delegate).thenReturn(genericActionDelegate)

        component.submit()

        verify(googlePayDelegate, never()).onSubmit()
    }

    @Test
    fun `when isConfirmationRequired and delegate is default, then delegate is called`() {
        component = GooglePayComponent(
            googlePayDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
        whenever(component.delegate).thenReturn(googlePayDelegate)

        component.isConfirmationRequired()

        verify(googlePayDelegate).isConfirmationRequired()
    }

    @Test
    fun `when setInteractionBlocked is called, then delegate is called`() {
        component = GooglePayComponent(
            googlePayDelegate,
            genericActionDelegate,
            actionHandlingComponent,
            componentEventHandler,
        )
        whenever(component.delegate).thenReturn(googlePayDelegate)

        component.setInteractionBlocked(true)

        verify(googlePayDelegate).setInteractionBlocked(true)
    }

    @Test
    fun `when getGooglePayButtonParameters is called, then delegate is called`() {
        component.getGooglePayButtonParameters()

        verify(googlePayDelegate).getGooglePayButtonParameters()
    }
}
