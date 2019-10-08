/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.base.component.lifecycle;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;

public final class ComponentViewModelFactory implements ViewModelProvider.Factory {

    private final PaymentMethod mPaymentMethod;
    private final Configuration mConfiguration;

    /**
     * Creates a {@code AndroidViewModelFactory}.
     *
     * @param paymentMethod a {@link PaymentMethod} to pass in {@link PaymentComponentViewModel}
     * @param configuration a {@link Configuration} to pass in {@link PaymentComponentViewModel}
     */
    public ComponentViewModelFactory(@NonNull PaymentMethod paymentMethod, @NonNull Configuration configuration) {
        mPaymentMethod = paymentMethod;
        mConfiguration = configuration;
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        try {
            return modelClass.getConstructor(mPaymentMethod.getClass(), mConfiguration.getClass()).newInstance(mPaymentMethod, mConfiguration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of component: " + modelClass, e);
        }
    }
}
