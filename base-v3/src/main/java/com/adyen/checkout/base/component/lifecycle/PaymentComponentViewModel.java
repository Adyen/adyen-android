/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.base.component.lifecycle;

import androidx.lifecycle.ViewModel;
import androidx.annotation.NonNull;

import com.adyen.checkout.base.PaymentComponent;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;

/**
 * Base class of a PaymentComponent as a ViewModel.
 *
 * @param <ConfigurationT> A Configuration object although optional is required to construct a Component.
 * @param <ComponentStateT> The {@link PaymentComponentState} this Component returns as a result.
 */
public abstract class PaymentComponentViewModel<ConfigurationT extends Configuration, ComponentStateT extends PaymentComponentState>
        extends ViewModel
        implements PaymentComponent<ComponentStateT, ConfigurationT> {
    private final PaymentMethod mPaymentMethod;

    private final ConfigurationT mConfiguration;

    public PaymentComponentViewModel(@NonNull PaymentMethod paymentMethod, @NonNull ConfigurationT configuration) {
        mPaymentMethod = paymentMethod;
        mConfiguration = configuration;
    }

    @NonNull
    public PaymentMethod getPaymentMethod() {
        return mPaymentMethod;
    }

    @NonNull
    public ConfigurationT getConfiguration() {
        return mConfiguration;
    }
}
