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
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod;

/**
 * A {@link ViewModelProvider.Factory} to create {@link PaymentComponentViewModel}.
 */
public final class PaymentComponentViewModelFactory implements ViewModelProvider.Factory {

    private final PaymentMethod mPaymentMethod;
    private final StoredPaymentMethod mStoredPaymentMethod;
    private final Configuration mConfiguration;

    /**
     * Creates a {@code AndroidViewModelFactory}.
     *
     * @param paymentMethod the {@link PaymentMethod} to pass in {@link PaymentComponentViewModel}
     * @param configuration the {@link Configuration} to pass in {@link PaymentComponentViewModel}
     */
    public PaymentComponentViewModelFactory(@NonNull PaymentMethod paymentMethod, @NonNull Configuration configuration) {
        mPaymentMethod = paymentMethod;
        mStoredPaymentMethod = null;
        mConfiguration = configuration;
    }

    /**
     * Creates a {@code AndroidViewModelFactory}.
     *
     * @param storedPaymentMethod the {@link StoredPaymentMethod} to pass in {@link PaymentComponentViewModel}
     * @param configuration the {@link Configuration} to pass in {@link PaymentComponentViewModel}
     */
    public PaymentComponentViewModelFactory(@NonNull StoredPaymentMethod storedPaymentMethod, @NonNull Configuration configuration) {
        mPaymentMethod = null;
        mStoredPaymentMethod = storedPaymentMethod;
        mConfiguration = configuration;
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        try {
            if (mStoredPaymentMethod == null) {
                return modelClass.getConstructor(
                        mPaymentMethod.getClass(),
                        mConfiguration.getClass()
                ).newInstance(mPaymentMethod, mConfiguration);
            } else {
                return modelClass.getConstructor(
                        mStoredPaymentMethod.getClass(),
                        mConfiguration.getClass()
                ).newInstance(mPaymentMethod, mConfiguration);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of component: " + modelClass, e);
        }
    }
}
