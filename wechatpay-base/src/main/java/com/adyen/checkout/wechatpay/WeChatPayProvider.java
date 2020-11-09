/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/9/2019.
 */

package com.adyen.checkout.wechatpay;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.adyen.checkout.base.ComponentAvailableCallback;
import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.component.lifecycle.PaymentComponentViewModelFactory;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;

public class WeChatPayProvider implements PaymentComponentProvider<WeChatPayComponent, WeChatPayConfiguration> {

    @SuppressWarnings("LambdaLast")
    @Override
    @NonNull
    public WeChatPayComponent get(
            @NonNull ViewModelStoreOwner viewModelStoreOwner,
            @NonNull PaymentMethod paymentMethod,
            @NonNull WeChatPayConfiguration configuration) {
        final PaymentComponentViewModelFactory factory = new PaymentComponentViewModelFactory(paymentMethod, configuration);
        return new ViewModelProvider(viewModelStoreOwner, factory).get(WeChatPayComponent.class);
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
