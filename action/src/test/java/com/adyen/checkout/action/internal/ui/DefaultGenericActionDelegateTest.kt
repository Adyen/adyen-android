/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 20/9/2022.
 */

package com.adyen.checkout.action.internal.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.adyen.checkout.action.GenericActionConfiguration
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.base.GenericComponentParamsMapper
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.model.payments.response.Threeds2FingerprintAction
import com.adyen.checkout.components.repository.ActionObserverRepository
import com.adyen.checkout.components.test.TestComponentViewType
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.Logger
import com.adyen.threeds2.customization.UiCustomization
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
@ExtendWith(MockitoExtension::class)
internal class DefaultGenericActionDelegateTest(
    @Mock private val activity: Activity,
    @Mock private val actionDelegateProvider: ActionDelegateProvider,
) {
    private lateinit var genericActionDelegate: DefaultGenericActionDelegate
    private lateinit var testDelegate: TestActionDelegate

    @BeforeEach
    fun beforeEach() {
        val configuration = GenericActionConfiguration.Builder(
            Locale.US,
            Environment.TEST,
            TEST_CLIENT_KEY
        ).build()
        genericActionDelegate = DefaultGenericActionDelegate(
            ActionObserverRepository(),
            SavedStateHandle(),
            configuration,
            GenericComponentParamsMapper().mapToParams(configuration),
            actionDelegateProvider
        )
        whenever(activity.application) doReturn Application()

        testDelegate = TestActionDelegate()
        whenever(actionDelegateProvider.getDelegate(any(), any(), any(), any())) doReturn testDelegate

        Logger.setLogcatLevel(Logger.NONE)
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
    fun `when set3DS2UICustomization is called on the generic delegate after handleAction then it's also called on the 3DS2 delegate`() =
        runTest {
            val adyen3DS2Delegate = Test3DS2Delegate()
            whenever(
                actionDelegateProvider.getDelegate(any(), any(), any(), any())
            ) doReturn adyen3DS2Delegate

            genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            genericActionDelegate.handleAction(Threeds2Action(), activity)

            assertEquals(adyen3DS2Delegate, genericActionDelegate.delegate)

            val uiCustomization = UiCustomization()
            genericActionDelegate.set3DS2UICustomization(uiCustomization)

            assertEquals(uiCustomization, adyen3DS2Delegate.uiCustomization)
        }

    @Test
    fun `when set3DS2UICustomization is called on the generic delegate before handleAction then it's also called on the 3DS2 delegate`() =
        runTest {
            val adyen3DS2Delegate = Test3DS2Delegate()
            whenever(
                actionDelegateProvider.getDelegate(any(), any(), any(), any())
            ) doReturn adyen3DS2Delegate

            genericActionDelegate.initialize(CoroutineScope(UnconfinedTestDispatcher()))

            val uiCustomization = UiCustomization()
            genericActionDelegate.set3DS2UICustomization(uiCustomization)

            genericActionDelegate.handleAction(Threeds2Action(), activity)

            assertEquals(adyen3DS2Delegate, genericActionDelegate.delegate)

            assertEquals(uiCustomization, adyen3DS2Delegate.uiCustomization)
        }

    @Test
    fun `when handleAction is called with a Threeds2ChallengeAction the inner delegate is not re-created`() = runTest {
        val adyen3DS2Delegate = Test3DS2Delegate()
        whenever(
            actionDelegateProvider.getDelegate(any(), any(), any(), any())
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

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
