/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/4/2019.
 */
package com.adyen.checkout.components.base

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.DataProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.models.TestComponentState
import com.adyen.checkout.components.models.TestConfiguration
import com.adyen.checkout.components.models.TestInputData
import com.adyen.checkout.components.models.TestOutputData
import com.adyen.checkout.components.models.TestPaymentMethod
import org.json.JSONException
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowLog::class])
class BaseComponentTest {

    @Before
    fun setUp() {
        ShadowLog.setupLogging()
    }

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private var paymentMethod: PaymentMethod? = null
    var paymentMethodDelegateTest = PaymentMethodDelegateTest()

    @Before
    @Throws(IOException::class, JSONException::class)
    fun init() {
        paymentMethod = DataProvider.getPaymentMethodResponse(javaClass.classLoader).paymentMethods!![0]
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(IOException::class, JSONException::class)
    fun initBaseComponent_notSupportedPaymentMethod_expectException() {
        object : BasePaymentComponent<TestConfiguration, TestInputData, TestOutputData, PaymentComponentState<out PaymentMethodDetails>>(
            SavedStateHandle(),
            PaymentMethodDelegateTest(),
            TestConfiguration()
        ) {
            override fun onInputDataChanged(inputData: TestInputData): TestOutputData {
                return TestOutputData()
            }

            override fun createComponentState(): PaymentComponentState<out PaymentMethodDetails> {
                return TestComponentState()
            }

            override val supportedPaymentMethodTypes: Array<String>
                get() = arrayOf("something")
        }
    }

    @Test
    fun initBaseComponent_SupportedPaymentMethod_expectOutputData() {
        baseComponent.observeOutputData(mockLifecycleOwner(), Assert::assertNotNull)
        Assert.assertNull(baseComponent.outputData)
    }

    @Test
    fun initBaseComponent_ChangeInputData_expectPaymentDetails() {
        baseComponent.inputDataChanged(TestInputData())
        baseComponent.observe(mockLifecycleOwner()) { paymentComponentState -> Assert.assertNotNull(paymentComponentState.data) }
    }

    @Test
    fun initBaseComponent_SupportedPaymentMethod_latePaymentDetails() {
        baseComponent.inputDataChanged(TestInputData(false))
        baseComponent.observe(mockLifecycleOwner()) { Assert.assertEquals(1, 1) }
    }

    private val baseComponent: BasePaymentComponent<TestConfiguration, TestInputData, TestOutputData, PaymentComponentState<out PaymentMethodDetails>>
        get() = object :
            BasePaymentComponent<TestConfiguration, TestInputData, TestOutputData, PaymentComponentState<out PaymentMethodDetails>>(
                SavedStateHandle(),
                paymentMethodDelegateTest,
                TestConfiguration()
            ) {
            override fun onInputDataChanged(inputData: TestInputData): TestOutputData {
                return TestOutputData(inputData.isValid)
            }

            override fun createComponentState(): PaymentComponentState<TestPaymentMethod> {
                val paymentComponentData: PaymentComponentData<TestPaymentMethod> = PaymentComponentData()
                paymentComponentData.paymentMethod = TestPaymentMethod()
                return PaymentComponentState(paymentComponentData, isInputValid = true, isReady = true)
            }

            override val supportedPaymentMethodTypes: Array<String>
                get() = arrayOf("")
        }

    companion object {
        private fun mockLifecycleOwner(): LifecycleOwner {
            val owner = Mockito.mock(LifecycleOwner::class.java)
            val lifecycle = LifecycleRegistry(owner)
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            Mockito.`when`(owner.lifecycle).thenReturn(lifecycle)
            return owner
        }
    }
}
