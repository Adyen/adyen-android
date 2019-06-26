/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/4/2019.
 */

package com.adyen.checkout.base.component;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.Configuration;
import com.adyen.checkout.base.DataProvider;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.models.TestInputData;
import com.adyen.checkout.base.models.TestOutputData;
import com.adyen.checkout.base.models.TestPaymentMethod;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.IOException;

public class BaseComponentTest {

    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    BasePaymentComponent<Configuration, TestInputData, TestOutputData, TestPaymentMethod> mBaseComponent;

    PaymentMethod paymentMethod;
    ClassLoader classLoader;

    @Before
    public void init() throws IOException, JSONException {
        classLoader = this.getClass().getClassLoader();
        paymentMethod = DataProvider.getPaymentMethodResponse(classLoader).getPaymentMethods().get(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void initBaseComponent_notSupportedPaymentMethod_expectException() throws IOException, JSONException {
        mBaseComponent = new BasePaymentComponent<Configuration, TestInputData, TestOutputData, TestPaymentMethod>(DataProvider.getPaymentMethodResponse(
                classLoader).getPaymentMethods().get(0),
                null) {
            @NonNull
            @Override
            protected TestOutputData onInputDataChanged(@NonNull TestInputData inputData) {
                return new TestOutputData("");
            }

            @NonNull
            @Override
            protected TestOutputData createOutputData(@NonNull PaymentMethod paymentMethod) {
                return new TestOutputData("");
            }

            @NonNull
            @Override
            protected PaymentComponentState createComponentState() {
                return null;
            }

            @NonNull
            @Override
            public String getPaymentMethodType() {
                return "something";
            }
        };
    }

    @Test
    public void initBaseComponent_SupportedPaymentMethod_expectOutputData() {
        mBaseComponent = getBaseComponent();

        mBaseComponent.observeOutputData(mockLifecycleOwner(), new Observer<TestOutputData>() {
            @Override
            public void onChanged(@Nullable TestOutputData testOutputData) {
                Assert.assertEquals(testOutputData.type, paymentMethod.getType());
            }
        });
        Assert.assertEquals(mBaseComponent.getOutputData().type, paymentMethod.getType());

    }

    @Test
    public void initBaseComponent_ChangeInputData_expectPaymentDetails() {
        mBaseComponent = getBaseComponent();
        mBaseComponent.inputDataChanged(new TestInputData());

        mBaseComponent.observe(mockLifecycleOwner(), new Observer<PaymentComponentState>() {
            @Override
            public void onChanged(@Nullable PaymentComponentState paymentComponentState) {
                Assert.assertNotNull(paymentComponentState.getData());
            }
        });
    }

    @Test
    public void initBaseComponent_SupportedPaymentMethod_notExpectPaymentDetails() {
        mBaseComponent = getBaseComponent();
        mBaseComponent.inputDataChanged(new TestInputData(false));

        mBaseComponent.observe(mockLifecycleOwner(), new Observer<PaymentComponentState>() {
            @Override
            public void onChanged(@Nullable PaymentComponentState paymentComponentState) {
                // this should get called
                // if get called test will fail
                Assert.assertEquals(1, 2);
            }
        });
    }

    private BasePaymentComponent getBaseComponent() {
        BasePaymentComponent baseComponent = new BasePaymentComponent<Configuration, TestInputData, TestOutputData, TestPaymentMethod>(paymentMethod, null) {
            @NonNull
            @Override
            protected TestOutputData onInputDataChanged(@NonNull TestInputData inputData) {
                TestOutputData outputData = new TestOutputData("", inputData.isValid);
                return outputData;
            }

            @NonNull
            @Override
            protected TestOutputData createOutputData(@NonNull PaymentMethod paymentMethod) {
                return new TestOutputData(paymentMethod.getType());
            }

            @NonNull
            @Override
            protected PaymentComponentState<TestPaymentMethod> createComponentState() {
                return new PaymentComponentState<>(new TestPaymentMethod(), true);
            }

            @NonNull
            @Override
            public String getPaymentMethodType() {
                return paymentMethod.getType();
            }
        };

        return baseComponent;
    }

    private static LifecycleOwner mockLifecycleOwner() {
        LifecycleOwner owner = mock(LifecycleOwner.class);
        LifecycleRegistry lifecycle = new LifecycleRegistry(owner);
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        when(owner.getLifecycle()).thenReturn(lifecycle);
        return owner;

    }
}