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
import androidx.lifecycle.ViewModelProvider;

import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.component.PaymentMethodDelegate;
import com.adyen.checkout.base.component.StoredPaymentMethodDelegate;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod;

/**
 * A {@link ViewModelProvider.Factory} to create {@link PaymentComponentViewModel}.
 */
public final class PaymentComponentViewModelFactory implements ViewModelProvider.Factory {

    private final PaymentMethodDelegate mPaymentMethodDelegate;
    private final StoredPaymentMethodDelegate mStoredPaymentMethodDelegate;
    private final Configuration mConfiguration;

    /**
     * Creates a {@code AndroidViewModelFactory}.
     *
     * @param paymentMethodDelegate the {@link PaymentMethod} to pass in {@link PaymentComponentViewModel}
     * @param configuration the {@link Configuration} to pass in {@link PaymentComponentViewModel}
     */
    public PaymentComponentViewModelFactory(@NonNull PaymentMethodDelegate paymentMethodDelegate, @NonNull Configuration configuration) {
        mPaymentMethodDelegate = paymentMethodDelegate;
        mStoredPaymentMethodDelegate = null;
        mConfiguration = configuration;
    }

    /**
     * Creates a {@code AndroidViewModelFactory}.
     *
     * @param storedPaymentMethodDelegate the {@link StoredPaymentMethod} to pass in {@link PaymentComponentViewModel}
     * @param configuration the {@link Configuration} to pass in {@link PaymentComponentViewModel}
     */
    public PaymentComponentViewModelFactory(@NonNull StoredPaymentMethodDelegate storedPaymentMethodDelegate, @NonNull Configuration configuration) {
        mPaymentMethodDelegate = null;
        mStoredPaymentMethodDelegate = storedPaymentMethodDelegate;
        mConfiguration = configuration;
    }

    @Override
    @NonNull
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        try {
            if (mStoredPaymentMethodDelegate == null) {
                return modelClass.getConstructor(
                        mPaymentMethodDelegate.getClass(),
                        mConfiguration.getClass()
                ).newInstance(mPaymentMethodDelegate, mConfiguration);
            } else {
                return modelClass.getConstructor(
                        mStoredPaymentMethodDelegate.getClass(),
                        mConfiguration.getClass()
                ).newInstance(mStoredPaymentMethodDelegate, mConfiguration);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of component: " + modelClass, e);
        }
    }
}
