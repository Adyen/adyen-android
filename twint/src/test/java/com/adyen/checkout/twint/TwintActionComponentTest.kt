package com.adyen.checkout.twint

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.action.SdkData
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionComponentEventHandler
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.extensions.invokeOnCleared
import com.adyen.checkout.test.extensions.test
import com.adyen.checkout.twint.internal.ui.TwintComponentViewType
import com.adyen.checkout.twint.internal.ui.TwintDelegate
import com.adyen.checkout.ui.core.internal.test.TestComponentViewType
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
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class TwintActionComponentTest(
    @Mock private val twintDelegate: TwintDelegate,
    @Mock private val actionComponentEventHandler: ActionComponentEventHandler,
) {

    private lateinit var component: TwintActionComponent

    @BeforeEach
    fun beforeEach() {
        whenever(twintDelegate.viewFlow) doReturn MutableStateFlow(TwintComponentViewType)
        component = TwintActionComponent(twintDelegate, actionComponentEventHandler)
    }

    @Test
    fun `when component is created, then delegate is initialized`() {
        verify(twintDelegate).initialize(component.viewModelScope)
    }

    @Test
    fun `when component is cleared, then delegate is cleared`() {
        component.invokeOnCleared()

        verify(twintDelegate).onCleared()
    }

    @Test
    fun `when observe is called, then observe in delegate is called`() {
        val lifecycleOwner = mock<LifecycleOwner>()
        val callback: (ActionComponentEvent) -> Unit = {}

        component.observe(lifecycleOwner, callback)

        verify(twintDelegate).observe(lifecycleOwner, component.viewModelScope, callback)
    }

    @Test
    fun `when removeObserver is called, then removeObserver in delegate is called`() {
        component.removeObserver()

        verify(twintDelegate).removeObserver()
    }

    @Test
    fun `when component is initialized, then view flow should match delegate view flow`() = runTest {
        val testFlow = component.viewFlow.test(testScheduler)

        assertEquals(TwintComponentViewType, testFlow.latestValue)
    }

    @Test
    fun `when delegate view flow emits a value, then component view flow should match that value`() = runTest {
        val delegateViewFlow = MutableStateFlow(TestComponentViewType.VIEW_TYPE_1)
        whenever(twintDelegate.viewFlow) doReturn delegateViewFlow
        component = TwintActionComponent(twintDelegate, actionComponentEventHandler)

        val testFlow = component.viewFlow.test(testScheduler)

        assertEquals(TestComponentViewType.VIEW_TYPE_1, testFlow.latestValue)

        delegateViewFlow.emit(TestComponentViewType.VIEW_TYPE_2)
        assertEquals(TestComponentViewType.VIEW_TYPE_2, testFlow.latestValue)
    }

    @Test
    fun `when handleAction is called, then handleAction in delegate is called`() {
        val action = SdkAction<SdkData>()
        val activity = Activity()
        component.handleAction(action, activity)

        verify(twintDelegate).handleAction(action, activity)
    }
}
