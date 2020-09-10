/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/4/2019.
 */

package com.adyen.checkout.base.component.lifecycle;

import com.adyen.checkout.base.DataProvider;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.PaymentMethodsApiResponse;
import com.adyen.checkout.base.models.TestConfiguration;
import com.adyen.checkout.base.models.TestViewModel;
import com.adyen.checkout.base.models.TestViewModelWithConstructor;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class PaymentPaymentComponentViewModelFactoryTest {

    ClassLoader classLoader;
    PaymentMethod mPaymentMethod;
    PaymentMethodsApiResponse mPaymentsResponse;
    TestConfiguration mTestConfiguration;

    @Before
    public void init() throws IOException, JSONException {
        classLoader = this.getClass().getClassLoader();
        mPaymentsResponse = DataProvider.getPaymentMethodResponse(classLoader);
        mPaymentMethod = mPaymentsResponse.getPaymentMethods().get(0);
        mTestConfiguration = new TestConfiguration();
    }

    @Test(expected = RuntimeException.class)
    public void initComponentViewModel_ViewModelWithoutConstructor_expectRuntimeException() {
        PaymentComponentViewModelFactory paymentComponentViewModelFactory = new PaymentComponentViewModelFactory(mPaymentMethod, mTestConfiguration);
        paymentComponentViewModelFactory.create(TestViewModel.class);
    }

    @Test
    public void initComponentViewModel_ViewModelWithConstructor_expectSamePaymentMethodAndConfiguration() {
        PaymentComponentViewModelFactory paymentComponentViewModelFactory = new PaymentComponentViewModelFactory(mPaymentMethod, mTestConfiguration);
        TestViewModelWithConstructor viewModel = paymentComponentViewModelFactory.create(TestViewModelWithConstructor.class);

        Assert.assertEquals(viewModel.paymentMethod, mPaymentMethod);
        Assert.assertEquals(viewModel.testConfiguration, mTestConfiguration);
    }

}