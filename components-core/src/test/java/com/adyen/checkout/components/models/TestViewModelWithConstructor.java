/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/4/2019.
 */

package com.adyen.checkout.components.models;

import androidx.lifecycle.ViewModel;

import com.adyen.checkout.components.base.PaymentMethodDelegateTest;

public class TestViewModelWithConstructor extends ViewModel {

    public PaymentMethodDelegateTest paymentMethodDelegate;
    public TestConfiguration testConfiguration;

    public TestViewModelWithConstructor(PaymentMethodDelegateTest paymentMethodDelegate, TestConfiguration configuration) {
        this.paymentMethodDelegate = paymentMethodDelegate;
        this.testConfiguration = configuration;
    }
}
