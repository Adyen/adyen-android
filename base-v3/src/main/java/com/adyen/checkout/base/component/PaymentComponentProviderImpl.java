/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */

package com.adyen.checkout.base.component;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.Configuration;
import com.adyen.checkout.base.component.lifecycle.ComponentViewModelFactory;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;

public final class PaymentComponentProviderImpl<BaseComponentT extends BasePaymentComponent, ConfigurationT extends Configuration>
        implements PaymentComponentProvider<BaseComponentT, ConfigurationT> {

    private final Class<BaseComponentT> mComponentClass;

    public PaymentComponentProviderImpl(@NonNull Class<BaseComponentT> modelClass) {
        mComponentClass = modelClass;
    }

    @NonNull
    @Override
    public BaseComponentT get(@NonNull FragmentActivity activity, @NonNull PaymentMethod paymentMethod, @NonNull ConfigurationT configuration) {
        final ComponentViewModelFactory factory = new ComponentViewModelFactory(paymentMethod, configuration);
        return ViewModelProviders.of(activity, factory).get(mComponentClass);
    }

    @NonNull
    @Override
    public BaseComponentT get(@NonNull Fragment fragment, @NonNull PaymentMethod paymentMethod, @NonNull ConfigurationT configuration) {
        final ComponentViewModelFactory factory = new ComponentViewModelFactory(paymentMethod, configuration);
        return ViewModelProviders.of(fragment, factory).get(mComponentClass);
    }
}
