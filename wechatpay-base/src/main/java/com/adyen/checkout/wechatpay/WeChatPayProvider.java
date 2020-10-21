/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/9/2019.
 */

package com.adyen.checkout.wechatpay;

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

public class WeChatPayProvider implements PaymentComponentProvider<WeChatPayComponent, WeChatPayConfiguration> {

    @NonNull
    @Override
    public WeChatPayComponent get(@NonNull FragmentActivity activity, @NonNull PaymentMethod paymentMethod,
            @NonNull WeChatPayConfiguration configuration) throws CheckoutException {
        final PaymentComponentViewModelFactory factory = new PaymentComponentViewModelFactory(paymentMethod, configuration);
        return ViewModelProviders.of(activity, factory).get(WeChatPayComponent.class);
    }

    @NonNull
    @Override
    public WeChatPayComponent get(@NonNull Fragment fragment, @NonNull PaymentMethod paymentMethod, @NonNull WeChatPayConfiguration configuration)
            throws CheckoutException {
        final PaymentComponentViewModelFactory factory = new PaymentComponentViewModelFactory(paymentMethod, configuration);
        final WeChatPayComponent component = ViewModelProviders.of(fragment, factory).get(WeChatPayComponent.class);
        if (fragment.getActivity() == null) {
            throw new CheckoutException("WeChatPay Component needs to be initiated on a Fragment that is attached to an Activity.");
        }
        return component;
    }

    @Override
    public void isAvailable(
            @NonNull Application applicationContext,
            @NonNull PaymentMethod paymentMethod,
            @NonNull WeChatPayConfiguration configuration,
            @NonNull ComponentAvailableCallback<WeChatPayConfiguration> callback) {

        callback.onAvailabilityResult(WeChatPayUtils.isAvailable(applicationContext), paymentMethod, configuration);
    }
}
