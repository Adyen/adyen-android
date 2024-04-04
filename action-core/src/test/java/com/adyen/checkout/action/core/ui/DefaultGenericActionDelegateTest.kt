/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/9/2022.
 */

package com.adyen.checkout.action.core.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.action.core.Test3DS2Delegate
import com.adyen.checkout.action.core.TestActionDelegate
import com.adyen.checkout.action.core.internal.ui.ActionDelegateProvider
import com.adyen.checkout.action.core.internal.ui.DefaultGenericActionDelegate
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.action.Threeds2ChallengeAction
import com.adyen.checkout.components.core.action.Threeds2FingerprintAction
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParamsMapper
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.ui.core.internal.test.TestComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class DefaultGenericActionDelegateTest(
    @Mock private val activity: Activity,
    @Mock private val actionDelegateProvider: ActionDelegateProvider,
) {

    private lateinit var genericActionDelegate: DefaultGenericActionDelegate
    private lateinit var testDelegate: TestActionDelegate

    @BeforeEach
    fun beforeEach() {
        genericActionDelegate = createDelegate()
        whenever(activity.application) doReturn Application()

        testDelegate = TestActionDelegate()
        whenever(actionDelegateProvider.getDelegate(any(), any(), any(), any())) doReturn testDelegate
    }

    @Test
    fun `when handle action is called the correct delegate is created`() = runTest {
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.handleAction(RedirectAction(), activity)

        assertEquals(testDelegate, genericActionDelegate.delegate)
    }

    @Test
    fun `when handle action is called again a new delegate is created`() = runTest {
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.handleAction(RedirectAction(), activity)

        assertEquals(testDelegate, genericActionDelegate.delegate)

        val newDelegate = TestActionDelegate()

        whenever(actionDelegateProvider.getDelegate(any(), any(), any(), any())) doReturn newDelegate

        genericActionDelegate.handleAction(RedirectAction(), activity)

        assertEquals(newDelegate, genericActionDelegate.delegate)
    }

    @Test
    fun `when inner delegate produces error then error is propagated to the generic delegate`() = runTest {
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.exceptionFlow.test {
            genericActionDelegate.handleAction(RedirectAction(), activity)

            val exception = CheckoutException("test exception")
            testDelegate.exceptionFlow.tryEmit(exception)

            assertEquals(exception, awaitItem())
        }
    }

    @Test
    fun `when inner delegate produces details then details are propagated to the generic delegate`() = runTest {
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.detailsFlow.test {
            genericActionDelegate.handleAction(RedirectAction(), activity)

            val details = ActionComponentData()
            testDelegate.detailsFlow.tryEmit(details)

            assertEquals(details, awaitItem())
        }
    }

    @Test
    fun `when inner delegate produces a view then view is propagated to the generic delegate`() = runTest {
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.viewFlow.test {
            genericActionDelegate.handleAction(RedirectAction(), activity)

            assertNull(awaitItem())

            testDelegate.viewFlow.tryEmit(TestComponentViewType.VIEW_TYPE_1)

            assertEquals(TestComponentViewType.VIEW_TYPE_1, awaitItem())
        }
    }

    @Test
    fun `when handleIntent is called without a delegate then exception is thrown`() = runTest {
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.exceptionFlow.test {
            genericActionDelegate.handleIntent(Intent())

            val exception = awaitItem()
            assert(exception is ComponentException)
            assertEquals("handleIntent should not be called before handleAction", exception.message)
        }
    }

    @Test
    fun `when initialize is called on the generic delegate then it's also called on the inner delegate`() = runTest {
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.handleAction(RedirectAction(), activity)

        assertTrue(testDelegate.initializeCalled)
    }

    @Test
    fun `when onCleared is called on the generic delegate then it's also called on the inner delegate`() = runTest {
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.handleAction(RedirectAction(), activity)

        genericActionDelegate.onCleared()

        assertTrue(testDelegate.onClearedCalled)
    }

    @Test
    fun `when handleAction is called on the generic delegate then it's also called on the inner delegate`() = runTest {
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.handleAction(RedirectAction(), activity)

        assertTrue(testDelegate.handleActionCalled)
    }

    @Test
    fun `when handleIntent is called on the generic delegate then it's also called on the inner delegate`() = runTest {
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.handleAction(RedirectAction(), activity)

        genericActionDelegate.handleIntent(Intent())

        assertTrue(testDelegate.handleIntentCalled)
    }

    @Test
    fun `when refreshStatus is called on the generic delegate then it's also called on the inner delegate`() = runTest {
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.handleAction(RedirectAction(), activity)
        genericActionDelegate.refreshStatus()

        assertTrue(testDelegate.refreshStatusCalled)
    }

    @Test
    fun `when handleAction is called with a Threeds2ChallengeAction the inner delegate is not re-created`() = runTest {
        val adyen3DS2Delegate = Test3DS2Delegate()
        whenever(
            actionDelegateProvider.getDelegate(any(), any(), any(), any()),
        ) doReturn adyen3DS2Delegate

        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.handleAction(Threeds2FingerprintAction(), activity)

        assertEquals(adyen3DS2Delegate, genericActionDelegate.delegate)
        assertTrue(adyen3DS2Delegate.handleActionCalled)

        adyen3DS2Delegate.handleActionCalled = false
        whenever(actionDelegateProvider.getDelegate(any(), any(), any(), any())) doReturn testDelegate

        genericActionDelegate.handleAction(Threeds2ChallengeAction(), activity)

        assertEquals(adyen3DS2Delegate, genericActionDelegate.delegate)
        assertTrue(adyen3DS2Delegate.handleActionCalled)
    }

    @Test
    fun `when process died during handling action, then handleIntent should restore state and continue`() {
        val savedStateHandle = SavedStateHandle().apply {
            set(DefaultGenericActionDelegate.ACTION_KEY, RedirectAction())
        }
        genericActionDelegate = createDelegate(savedStateHandle)
        genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        genericActionDelegate.handleIntent(Intent())

        assertTrue(testDelegate.handleIntentCalled)
    }

    private fun createDelegate(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): DefaultGenericActionDelegate {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )

        return DefaultGenericActionDelegate(
            observerRepository = ActionObserverRepository(),
            savedStateHandle = savedStateHandle,
            checkoutConfiguration = configuration,
            componentParams = GenericComponentParamsMapper(CommonComponentParamsMapper())
                .mapToParams(configuration, Locale.US, null, null),
            actionDelegateProvider = actionDelegateProvider,
            application = Application(),
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
