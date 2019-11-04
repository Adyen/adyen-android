/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */

package com.adyen.checkout.card;

import android.app.Application;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.adyen.checkout.base.ComponentAvailableCallback;
import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.component.lifecycle.ComponentViewModelFactory;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;

public class CardComponentProvider implements PaymentComponentProvider<CardComponent, CardConfiguration> {

    @NonNull
    @Override
    public CardComponent get(@NonNull FragmentActivity activity, @NonNull PaymentMethod paymentMethod, @NonNull CardConfiguration configuration) {
        final ComponentViewModelFactory factory = new ComponentViewModelFactory(paymentMethod, configuration);
        return ViewModelProviders.of(activity, factory).get(CardComponent.class);
    }

    @NonNull
    @Override
    public CardComponent get(@NonNull Fragment fragment, @NonNull PaymentMethod paymentMethod, @NonNull CardConfiguration configuration) {
        final ComponentViewModelFactory factory = new ComponentViewModelFactory(paymentMethod, configuration);
        return ViewModelProviders.of(fragment, factory).get(CardComponent.class);
    }

    @Override
    public void isAvailable(
            @NonNull Application applicationContext,
            @NonNull PaymentMethod paymentMethod,
            @NonNull CardConfiguration configuration,
            @NonNull ComponentAvailableCallback<CardConfiguration> callback) {

        final boolean isPubKeyAvailable = !TextUtils.isEmpty(configuration.getPublicKey());
        callback.onAvailabilityResult(isPubKeyAvailable, paymentMethod, configuration);
    }
}
