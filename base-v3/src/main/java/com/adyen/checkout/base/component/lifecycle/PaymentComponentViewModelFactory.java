/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.base.component.lifecycle;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;

/**
 * A {@link ViewModelProvider.Factory} to create {@link PaymentComponentViewModel}.
 */
public final class PaymentComponentViewModelFactory implements ViewModelProvider.Factory {

    private final PaymentMethod mPaymentMethod;
    private final Configuration mConfiguration;

    /**
     * Creates a {@code AndroidViewModelFactory}.
     *
     * @param paymentMethod a {@link PaymentMethod} to pass in {@link PaymentComponentViewModel}
     * @param configuration a {@link Configuration} to pass in {@link PaymentComponentViewModel}
     */
    public PaymentComponentViewModelFactory(@NonNull PaymentMethod paymentMethod, @NonNull Configuration configuration) {
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
