/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.base.component.lifecycle;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.Configuration;
import com.adyen.checkout.base.PaymentComponent;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;

public abstract class PaymentComponentViewModel<ConfigurationT extends Configuration> extends ViewModel implements PaymentComponent {

    private final PaymentMethod mPaymentMethod;

    private final ConfigurationT mConfiguration;

    public PaymentComponentViewModel(@NonNull PaymentMethod paymentMethod, @NonNull ConfigurationT configuration) {
        mPaymentMethod = paymentMethod;
        mConfiguration = configuration;
    }

    @NonNull
    protected PaymentMethod getPaymentMethod() {
        return mPaymentMethod;
    }

    @NonNull
    protected ConfigurationT getConfiguration() {
        return mConfiguration;
    }
}
