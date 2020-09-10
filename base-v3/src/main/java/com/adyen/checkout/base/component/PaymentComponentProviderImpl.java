/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */

package com.adyen.checkout.base.component;

import android.app.Application;
import androidx.lifecycle.ViewModelProviders;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.adyen.checkout.base.ComponentAvailableCallback;
import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.component.lifecycle.PaymentComponentViewModelFactory;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.core.exception.CheckoutException;

public final class PaymentComponentProviderImpl<BaseComponentT extends BasePaymentComponent, ConfigurationT extends Configuration>
        implements PaymentComponentProvider<BaseComponentT, ConfigurationT> {

    private final Class<BaseComponentT> mComponentClass;

    public PaymentComponentProviderImpl(@NonNull Class<BaseComponentT> modelClass) {
        mComponentClass = modelClass;
    }

    @NonNull
    @Override
    public BaseComponentT get(@NonNull FragmentActivity activity, @NonNull PaymentMethod paymentMethod, @NonNull ConfigurationT configuration)
            throws CheckoutException {
        final PaymentComponentViewModelFactory factory = new PaymentComponentViewModelFactory(paymentMethod, configuration);
        return ViewModelProviders.of(activity, factory).get(mComponentClass);
    }

    @NonNull
    @Override
    public BaseComponentT get(@NonNull Fragment fragment, @NonNull PaymentMethod paymentMethod, @NonNull ConfigurationT configuration)
            throws CheckoutException {
        final PaymentComponentViewModelFactory factory = new PaymentComponentViewModelFactory(paymentMethod, configuration);
        return ViewModelProviders.of(fragment, factory).get(mComponentClass);
    }

    @Override
    public void isAvailable(
            @NonNull Application applicationContext,
            @NonNull PaymentMethod paymentMethod,
            @NonNull ConfigurationT config,
            @NonNull ComponentAvailableCallback<ConfigurationT> callback) {
        callback.onAvailabilityResult(true, paymentMethod, config);
    }
}
