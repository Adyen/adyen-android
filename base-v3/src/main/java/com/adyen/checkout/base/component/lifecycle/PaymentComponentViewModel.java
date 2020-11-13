/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.base.component.lifecycle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.adyen.checkout.base.PaymentComponent;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.component.PaymentMethodDelegate;
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails;

/**
 * Base class of a PaymentComponent as a ViewModel.
 *
 * @param <ConfigurationT> A Configuration object although optional is required to construct a Component.
 * @param <ComponentStateT> The {@link PaymentComponentState} this Component returns as a result.
 */
public abstract class PaymentComponentViewModel<
        ConfigurationT extends Configuration,
        ComponentStateT extends PaymentComponentState<? extends PaymentMethodDetails>>
        extends ViewModel
        implements PaymentComponent<ComponentStateT, ConfigurationT> {

    protected final PaymentMethodDelegate mPaymentMethodDelegate;
    protected final ConfigurationT mConfiguration;

    public PaymentComponentViewModel(@NonNull PaymentMethodDelegate paymentMethodDelegate, @NonNull ConfigurationT configuration) {
        mPaymentMethodDelegate = paymentMethodDelegate;
        mConfiguration = configuration;
    }

    @NonNull
    public ConfigurationT getConfiguration() {
        return mConfiguration;
    }
}
